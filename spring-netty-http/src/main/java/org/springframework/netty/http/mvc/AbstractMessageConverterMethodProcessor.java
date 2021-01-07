package org.springframework.netty.http.mvc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.netty.http.HttpOutputMessage;
import org.springframework.netty.http.HttpUtils;
import org.springframework.netty.http.converter.GenericHttpMessageConverter;
import org.springframework.netty.http.converter.HttpMessageConverter;
import org.springframework.netty.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public abstract class AbstractMessageConverterMethodProcessor {

    private static final Set<HttpMethod> SUPPORTED_METHODS = new HashSet<HttpMethod>();

    static {
        SUPPORTED_METHODS.add(HttpMethod.POST);
        SUPPORTED_METHODS.add(HttpMethod.PUT);
        SUPPORTED_METHODS.add(HttpMethod.PATCH);
    }

    private static final Object NO_VALUE = new Object();
    protected final Log logger = LogFactory.getLog(getClass());

    protected final List<HttpMessageConverter<?>> messageConverters;

    protected final List<MediaType> allSupportedMediaTypes;

    private static final MediaType MEDIA_TYPE_APPLICATION = new MediaType("application");



    public AbstractMessageConverterMethodProcessor(List<HttpMessageConverter<?>> converters) {

        Assert.notEmpty(converters, "'messageConverters' must not be empty");
        this.messageConverters = converters;
        this.allSupportedMediaTypes = getAllSupportedMediaTypes(converters);
    }

    /**
     * Return the media types supported by all provided message converters sorted
     * by specificity via {@link MediaType#sortBySpecificity(List)}.
     */
    private static List<MediaType> getAllSupportedMediaTypes(List<HttpMessageConverter<?>> messageConverters) {
        Set<MediaType> allSupportedMediaTypes = new LinkedHashSet<MediaType>();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
        }
        List<MediaType> result = new ArrayList<MediaType>(allSupportedMediaTypes);
        MediaType.sortBySpecificity(result);
        return Collections.unmodifiableList(result);
    }


    protected <T> Object readWithMessageConverters(ChannelHandlerContext ctx,FullHttpRequest inputMessage, Type targetType)
            throws IOException, HttpMediaTypeNotSupportedException {
        MediaType contentType = HttpUtils.getContentType(inputMessage);
        boolean noContentType = false;

        if (contentType == null) {
            noContentType = true;
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        Class<T> targetClass = (targetType instanceof Class ? (Class<T>) targetType : null);
        if (targetClass == null) {
            ResolvableType resolvableType = ResolvableType.forType(targetType);
            targetClass = (Class<T>) resolvableType.resolve();
        }

        HttpMethod httpMethod = inputMessage.method();
        Object body = NO_VALUE;
        try {
            for (HttpMessageConverter<?> converter : this.messageConverters) {
                Class<HttpMessageConverter<?>> converterType = (Class<HttpMessageConverter<?>>) converter.getClass();
                if (converter instanceof GenericHttpMessageConverter) {
                    GenericHttpMessageConverter<?> genericConverter = (GenericHttpMessageConverter<?>) converter;
                    if (genericConverter.canRead(targetType, null, contentType)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Read [" + targetType + "] as \"" + contentType + "\" with [" + converter + "]");
                        }
                        if (inputMessage.content() != null) {
                            body = genericConverter.read(targetType, null, inputMessage);
                        }
                        break;
                    }
                } else if (targetClass != null) {
                    if (converter.canRead(targetClass, contentType)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Read [" + targetType + "] as \"" + contentType + "\" with [" + converter + "]");
                        }
                        if (inputMessage.content() != null) {
                            body = ((HttpMessageConverter<T>) converter).read(targetClass, ctx,inputMessage);
                        }
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex);
        }

        if (body == NO_VALUE) {
            if (httpMethod == null || !SUPPORTED_METHODS.contains(httpMethod) ||
                    (noContentType && inputMessage.content() == null)) {
                return null;
            }
            throw new HttpMediaTypeNotSupportedException(contentType, this.allSupportedMediaTypes);
        }

        return body;
    }


    protected <T> void writeWithMessageConverters(T value, FullHttpRequest request, HttpOutputMessage outputMessage) throws IOException, HttpMediaTypeNotAcceptableException {

        Object outputValue;
        Class<?> valueType;
        Type declaredType;
        if (value instanceof CharSequence) {
            outputValue = value.toString();
            valueType = String.class;
            declaredType = String.class;
        } else {
            outputValue = value;
            valueType = (value != null ? value.getClass() : null);
            declaredType = null;
        }

        List<MediaType> requestedMediaTypes = getAcceptableMediaTypes(request);
        List<MediaType> producibleMediaTypes = getProducibleMediaTypes(request, valueType, declaredType);

        if (outputValue != null && producibleMediaTypes.isEmpty()) {
            throw new IllegalArgumentException("No converter found for return value of type: " + valueType);
        }

        Set<MediaType> compatibleMediaTypes = new LinkedHashSet<MediaType>();
        for (MediaType requestedType : requestedMediaTypes) {
            for (MediaType producibleType : producibleMediaTypes) {
                if (requestedType.isCompatibleWith(producibleType)) {
                    compatibleMediaTypes.add(getMostSpecificMediaType(requestedType, producibleType));
                }
            }
        }
        if (compatibleMediaTypes.isEmpty()) {
            if (outputValue != null) {
                throw new HttpMediaTypeNotAcceptableException(producibleMediaTypes);
            }
            return;
        }

        List<MediaType> mediaTypes = new ArrayList<MediaType>(compatibleMediaTypes);
        MediaType.sortBySpecificityAndQuality(mediaTypes);

        MediaType selectedMediaType = null;
        for (MediaType mediaType : mediaTypes) {
            if (mediaType.isConcrete()) {
                selectedMediaType = mediaType;
                break;
            }
            else if (mediaType.equals(MediaType.ALL) || mediaType.equals(MEDIA_TYPE_APPLICATION)) {
                selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
                break;
            }
        }

        //实体对象序列化
        if (selectedMediaType != null) {
            for (HttpMessageConverter<?> messageConverter : this.messageConverters) {
                if (messageConverter instanceof GenericHttpMessageConverter) {
                    if (((GenericHttpMessageConverter) messageConverter).canWrite(
                            declaredType, valueType, selectedMediaType)) {

                        if (outputValue != null) {
                            ((GenericHttpMessageConverter) messageConverter).write(
                                    outputValue, declaredType, selectedMediaType, outputMessage);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Written [" + outputValue + "] as \"" + selectedMediaType +
                                        "\" using [" + messageConverter + "]");
                            }
                        }
                        return;
                    }
                } else if (messageConverter.canWrite(valueType, selectedMediaType)) {
                    if (outputValue != null) {
                        ((HttpMessageConverter) messageConverter).write(outputValue, selectedMediaType, outputMessage);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Written [" + outputValue + "] as \"" + selectedMediaType +
                                    "\" using [" + messageConverter + "]");
                        }
                    }
                    return;
                }
            }
        }

        if (outputValue != null) {
            throw new HttpMediaTypeNotAcceptableException(this.allSupportedMediaTypes);
        }
    }


    @SuppressWarnings("unchecked")
    protected List<MediaType> getProducibleMediaTypes(FullHttpRequest request, Class<?> valueClass) {
        return getProducibleMediaTypes(request,valueClass,null);
    }
    /**
     * Returns the media types that can be produced:
     * <ul>
     * <li>The producible media types specified in the request mappings, or
     * <li>Media types of configured converters that can write the specific return value, or
     * <li>{@link MediaType#ALL}
     * </ul>
     *
     * @since 4.2
     */
    @SuppressWarnings("unchecked")
    protected List<MediaType> getProducibleMediaTypes(FullHttpRequest request, Class<?> valueClass, Type declaredType) {
        Set<MediaType> mediaTypes = null;
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            return new ArrayList<MediaType>(mediaTypes);
        } else if (!this.allSupportedMediaTypes.isEmpty()) {
            List<MediaType> result = new ArrayList<MediaType>();
            for (HttpMessageConverter<?> converter : this.messageConverters) {
                if (converter instanceof GenericHttpMessageConverter && declaredType != null) {
                    if (((GenericHttpMessageConverter<?>) converter).canWrite(declaredType, valueClass, null)) {
                        result.addAll(converter.getSupportedMediaTypes());
                    }
                } else if (converter.canWrite(valueClass, null)) {
                    result.addAll(converter.getSupportedMediaTypes());
                }
            }
            return result;
        } else {
            return Collections.singletonList(MediaType.ALL);
        }
    }


    private List<MediaType> getAcceptableMediaTypes(FullHttpRequest request) {
        List<MediaType> mediaTypes = HttpUtils.getAccepts(request);
        return (mediaTypes.isEmpty() ? Collections.singletonList(MediaType.ALL) : mediaTypes);
    }


    /**
     * Return the more specific of the acceptable and the producible media types
     * with the q-value of the former.
     */
    private MediaType getMostSpecificMediaType(MediaType acceptType, MediaType produceType) {
        MediaType produceTypeToUse = produceType.copyQualityValue(acceptType);
        return (MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceTypeToUse) <= 0 ? acceptType : produceTypeToUse);
    }


}
