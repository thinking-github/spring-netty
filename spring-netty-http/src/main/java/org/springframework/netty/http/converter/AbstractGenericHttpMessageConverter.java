package org.springframework.netty.http.converter;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.netty.http.HttpOutputMessage;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public  abstract  class AbstractGenericHttpMessageConverter<T> extends AbstractHttpMessageConverter<T>
        implements GenericHttpMessageConverter<T> {

    /**
     * Construct an {@code AbstractGenericHttpMessageConverter} with no supported media types.
     * @see #setSupportedMediaTypes
     */
    protected AbstractGenericHttpMessageConverter() {
    }

    /**
     * Construct an {@code AbstractGenericHttpMessageConverter} with one supported media type.
     * @param supportedMediaType the supported media type
     */
    protected AbstractGenericHttpMessageConverter(MediaType supportedMediaType) {
        super(supportedMediaType);
    }

    /**
     * Construct an {@code AbstractGenericHttpMessageConverter} with multiple supported media type.
     * @param supportedMediaTypes the supported media types
     */
    protected AbstractGenericHttpMessageConverter(MediaType... supportedMediaTypes) {
        super(supportedMediaTypes);
    }


    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return (type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType));
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return canWrite(clazz, mediaType);
    }

    /**
     * This implementation sets the default headers by calling {@link #addDefaultHeaders},
     * and then calls {@link #writeInternal}.
     */
    public final void write(final T t, final Type type, MediaType contentType, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        final HttpHeaders headers = outputMessage.headers();
        addDefaultHeaders(headers, t, contentType);
        writeInternal(t, type, outputMessage);
    }

    @Override
    protected void writeInternal(T t, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        writeInternal(t, null, outputMessage);
    }

    /**
     * Abstract template method that writes the actual body. Invoked from {@link #write}.
     * @param t the object to write to the output message
     * @param type the type of object to write (may be {@code null})
     * @param outputMessage the HTTP output message to write to
     * @throws IOException in case of I/O errors
     * @throws HttpMessageNotWritableException in case of conversion errors
     */
    protected abstract void writeInternal(T t, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException;

}
