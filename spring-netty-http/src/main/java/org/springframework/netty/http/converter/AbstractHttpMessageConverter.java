package org.springframework.netty.http.converter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.netty.http.HttpOutputMessage;
import org.springframework.netty.http.HttpUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public abstract class AbstractHttpMessageConverter <T> implements HttpMessageConverter<T>{

    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    private List<MediaType> supportedMediaTypes = Collections.emptyList();

    private Charset defaultCharset;


    /**
     * Construct an {@code AbstractHttpMessageConverter} with no supported media types.
     * @see #setSupportedMediaTypes
     */
    protected AbstractHttpMessageConverter() {
    }

    /**
     * Construct an {@code AbstractHttpMessageConverter} with one supported media type.
     * @param supportedMediaType the supported media type
     */
    protected AbstractHttpMessageConverter(MediaType supportedMediaType) {
        setSupportedMediaTypes(Collections.singletonList(supportedMediaType));
    }

    /**
     * Construct an {@code AbstractHttpMessageConverter} with multiple supported media types.
     * @param supportedMediaTypes the supported media types
     */
    protected AbstractHttpMessageConverter(MediaType... supportedMediaTypes) {
        setSupportedMediaTypes(Arrays.asList(supportedMediaTypes));
    }

    /**
     * Construct an {@code AbstractHttpMessageConverter} with a default charset and
     * multiple supported media types.
     * @param defaultCharset the default character set
     * @param supportedMediaTypes the supported media types
     * @since 4.3
     */
    protected AbstractHttpMessageConverter(Charset defaultCharset, MediaType... supportedMediaTypes) {
        this.defaultCharset = defaultCharset;
        setSupportedMediaTypes(Arrays.asList(supportedMediaTypes));
    }


    /**
     * Set the list of {@link MediaType} objects supported by this converter.
     */
    public void setSupportedMediaTypes(List<MediaType> supportedMediaTypes) {
        Assert.notEmpty(supportedMediaTypes, "MediaType List must not be empty");
        this.supportedMediaTypes = new ArrayList<MediaType>(supportedMediaTypes);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.unmodifiableList(this.supportedMediaTypes);
    }

    /**
     * Set the default character set, if any.
     * @since 4.3
     */
    public void setDefaultCharset(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    /**
     * Return the default character set, if any.
     * @since 4.3
     */
    public Charset getDefaultCharset() {
        return this.defaultCharset;
    }


    /**
     * This implementation checks if the given class is {@linkplain #supports(Class) supported},
     * and if the {@linkplain #getSupportedMediaTypes() supported media types}
     * the given media type.
     */
    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return supports(clazz) && canRead(mediaType);
    }

    /**
     * Returns {@code true} if any of the {@linkplain #setSupportedMediaTypes(List)
     * supported} media types  the
     * given media type.
     * @param mediaType the media type to read, can be {@code null} if not specified.
     * Typically the value of a {@code Content-Type} header.
     * @return {@code true} if the supported media types include the media type,
     * or if the media type is {@code null}
     */
    protected boolean canRead(MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        for (MediaType supportedMediaType : getSupportedMediaTypes()) {
            if (supportedMediaType.includes(mediaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This implementation checks if the given class is
     * {@linkplain #supports(Class) supported}, and if the
     * {@linkplain #getSupportedMediaTypes() supported} media types
     * {@linkplain MediaType#includes(MediaType) include} the given media type.
     */
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return supports(clazz) && canWrite(mediaType);
    }

    /**
     * Returns {@code true} if the given media type includes any of the
     * {@linkplain #setSupportedMediaTypes(List) supported media types}.
     * @param mediaType the media type to write, can be {@code null} if not specified.
     * Typically the value of an {@code Accept} header.
     * @return {@code true} if the supported media types are compatible with the media type,
     * or if the media type is {@code null}
     */
    protected boolean canWrite(MediaType mediaType) {
        if (mediaType == null || MediaType.ALL.equals(mediaType)) {
            return true;
        }
        for (MediaType supportedMediaType : getSupportedMediaTypes()) {
            if (supportedMediaType.isCompatibleWith(mediaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This implementation simple delegates to .
     * Future implementations might add some default behavior, however.
     */
    @Override
    public final T read(Class<? extends T> clazz, ChannelHandlerContext ctx, FullHttpRequest inputMessage)
            throws IOException, HttpMessageNotReadableException {

        return readInternal(clazz, inputMessage);
    }

    /**
     * This implementation sets the default headers by calling {@link #addDefaultHeaders},
     * and then calls {@link #writeInternal}.
     */
    @Override
    public final void write(final T t, MediaType contentType, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        final HttpHeaders headers = outputMessage.headers();
        addDefaultHeaders(headers, t, contentType);

        writeInternal(t, outputMessage);
    }

    /**
     * Add default headers to the output message.
     * <p>This implementation delegates to {@link #getDefaultContentType(Object)} if a
     * content type was not provided, set if necessary the default character set, calls
     * {@link #getContentLength}, and sets the corresponding headers.
     * @since 4.2
     */
    protected void addDefaultHeaders(HttpHeaders headers, T t, MediaType contentType) throws IOException {
        CharSequence hContentType =  HttpUtils.getMimeType(headers);
        if (hContentType == null) {
            MediaType contentTypeToUse = contentType;
            if (contentType == null || contentType.isWildcardType() || contentType.isWildcardSubtype()) {
                contentTypeToUse = getDefaultContentType(t);
            }
            else if (MediaType.APPLICATION_OCTET_STREAM.equals(contentType)) {
                MediaType mediaType = getDefaultContentType(t);
                contentTypeToUse = (mediaType != null ? mediaType : contentTypeToUse);
            }
            if (contentTypeToUse != null) {
                if (contentTypeToUse.getCharset() == null) {
                    Charset defaultCharset = getDefaultCharset();
                    if (defaultCharset != null) {
                        contentTypeToUse = new MediaType(contentTypeToUse, defaultCharset);
                    }
                }
                HttpUtils.setContentType(headers,contentTypeToUse.toString());
            }
        }
        MediaType mediaType  = hContentType != null ? MediaType.parseMediaType(hContentType.toString()) : null;
        if (HttpUtils.getContentLength(headers) < 0 && !headers.contains(HttpHeaderNames.TRANSFER_ENCODING)) {
            Long contentLength = getContentLength(t, mediaType);
            if (contentLength != null) {
                HttpUtils.setContentLength(headers,contentLength);
            }
        }
    }

    /**
     * Returns the default content type for the given type. Called when {@link #write}
     * is invoked without a specified content type parameter.
     * <p>By default, this returns the first element of the
     * {@link #setSupportedMediaTypes(List) supportedMediaTypes} property, if any.
     * Can be overridden in subclasses.
     * @param t the type to return the content type for
     * @return the content type, or {@code null} if not known
     */
    protected MediaType getDefaultContentType(T t) throws IOException {
        List<MediaType> mediaTypes = getSupportedMediaTypes();
        return (!mediaTypes.isEmpty() ? mediaTypes.get(0) : null);
    }

    /**
     * Returns the content length for the given type.
     * <p>By default, this returns {@code null}, meaning that the content length is unknown.
     * Can be overridden in subclasses.
     * @param t the type to return the content length for
     * @return the content length, or {@code null} if not known
     */
    protected Long getContentLength(T t, MediaType contentType) throws IOException {
        return null;
    }


    /**
     * Indicates whether the given class is supported by this converter.
     * @param clazz the class to test for support
     * @return {@code true} if supported; {@code false} otherwise
     */
    protected abstract boolean supports(Class<?> clazz);

    /**
     * Abstract template method that reads the actual object. Invoked from {@link #read}.
     * @param clazz the type of object to return
     * @param inputMessage the HTTP input message to read from
     * @return the converted object
     * @throws IOException in case of I/O errors
     */
    protected abstract T readInternal(Class<? extends T> clazz, FullHttpRequest inputMessage)
            throws IOException;

    /**
     * Abstract template method that writes the actual body. Invoked from {@link #write}.
     * @param t the object to write to the output message
     * @param outputMessage the HTTP output message to write to
     * @throws IOException in case of I/O errors
     */
    protected abstract void writeInternal(T t, HttpOutputMessage outputMessage)
            throws IOException;

}
