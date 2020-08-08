package org.springframework.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponse;

import java.io.IOException;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-20
 */
public interface HttpOutputMessage extends HttpResponse {


    /**
     * Return the body of the message as an output stream.
     *
     * @return the output stream body (never {@code null})
     * @throws IOException in case of I/O errors
     */
    ByteBuf getBody() throws IOException;


    void setBody(ByteBuf content) throws IOException;
}
