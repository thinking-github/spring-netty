package org.springframework.netty.http.converter;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.http.MediaType;
import org.springframework.netty.http.HttpOutputMessage;
import org.springframework.netty.http.HttpUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-20
 */
public class ByteArrayHttpMessageConverter extends AbstractHttpMessageConverter<byte[]> {

    /**
     * Create a new instance of the {@code ByteArrayHttpMessageConverter}.
     */
    public ByteArrayHttpMessageConverter() {
        super(new MediaType("application", "octet-stream"), MediaType.ALL);
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return byte[].class == clazz;
    }

    @Override
    public byte[] readInternal(Class<? extends byte[]> clazz, FullHttpRequest inputMessage) throws IOException {
        long contentLength = HttpUtils.getContentLength(inputMessage.headers());

      /*  ByteArrayOutputStream bos =
                new ByteArrayOutputStream(contentLength >= 0 ? (int) contentLength : StreamUtils.BUFFER_SIZE);
        StreamUtils.copy(inputMessage.getBody(), bos);
        bos.toByteArray();*/

        return inputMessage.content().array();
    }

    @Override
    protected Long getContentLength(byte[] bytes, MediaType contentType) {
        return (long) bytes.length;
    }

    @Override
    protected void writeInternal(byte[] bytes, HttpOutputMessage outputMessage) throws IOException {
        //StreamUtils.copy(bytes, outputMessage.getBody());
        outputMessage.setBody(Unpooled.wrappedBuffer(bytes));
    }

}
