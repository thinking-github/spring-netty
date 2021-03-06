package org.springframework.netty.http.converter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.springframework.http.MediaType;
import org.springframework.netty.http.HttpOutputMessage;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public interface HttpMessageConverter<T> {

    /**
     * Indicates whether the given class can be read by this converter.
     * @param clazz the class to test for readability
     * @param mediaType the media type to read (can be {@code null} if not specified);
     * typically the value of a {@code Content-Type} header.
     * @return {@code true} if readable; {@code false} otherwise
     */
    boolean canRead(Class<?> clazz, MediaType mediaType);

    /**
     * Indicates whether the given class can be written by this converter.
     * @param clazz the class to test for writability
     * @param mediaType the media type to write (can be {@code null} if not specified);
     * typically the value of an {@code Accept} header.
     * @return {@code true} if writable; {@code false} otherwise
     */
    boolean canWrite(Class<?> clazz, MediaType mediaType);

    /**
     * Return the list of supported by this converter.
     * @return the list of supported media types
     */
    List<MediaType> getSupportedMediaTypes();

    /**
     * Read an object of the given type from the given input message, and returns it.
     * @param clazz the type of object to return. This type must have previously been passed to the
     * {@link #canRead canRead} method of this interface, which must have returned {@code true}.
     * @param request the HTTP input message to read from
     * @return the converted object
     * @throws IOException in case of I/O errors
     */
    T read(Class<? extends T> clazz, ChannelHandlerContext ctx, FullHttpRequest request) throws IOException;

    /**
     * Write an given object to the given output message.
     * @param t the object to write to the output message. The type of this object must have previously been
     * passed to the {@link #canWrite canWrite} method of this interface, which must have returned {@code true}.
     * @param contentType the content type to use when writing. May be {@code null} to indicate that the
     * default content type of the converter must be used. If not {@code null}, this media type must have
     * previously been passed to the {@link #canWrite canWrite} method of this interface, which must have
     * returned {@code true}.
     * @param outputMessage the message to write to
     * @throws IOException in case of I/O errors
     */
    void write(T t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException;


    //FullHttpResponse write(T t, MediaType contentType) throws IOException;

}
