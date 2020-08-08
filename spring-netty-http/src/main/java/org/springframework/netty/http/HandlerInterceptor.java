package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 */
public interface HandlerInterceptor {

    boolean preHandle(ChannelHandlerContext ctx, HttpRequest request, Object handler)
            throws Exception;


    void postHandle(ChannelHandlerContext ctx, HttpRequest request, Object handler,Object result)
            throws Exception;


    void afterCompletion(ChannelHandlerContext ctx, HttpRequest request, Object handler, Exception ex)
            throws Exception;

}

