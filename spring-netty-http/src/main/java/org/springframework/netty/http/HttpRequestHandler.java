package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.IOException;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-13
 */
public interface HttpRequestHandler<T> {


    <R> R handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, T inputBody)
            throws IOException;


}
