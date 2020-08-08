package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 */
public interface HandlerAdapter {

    boolean supports(Object handler);


    Object handle(ChannelHandlerContext ctx,FullHttpRequest request, Object handler) throws Exception;


    long getLastModified(FullHttpRequest request, Object handler);

}
