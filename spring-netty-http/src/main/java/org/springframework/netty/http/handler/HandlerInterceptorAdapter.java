package org.springframework.netty.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.springframework.netty.http.HandlerInterceptor;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 */
public class HandlerInterceptorAdapter implements HandlerInterceptor {

    /**
     * This implementation always returns {@code true}.
     */
    @Override
    public boolean preHandle(ChannelHandlerContext ctx, HttpRequest request, Object handler) throws Exception {
        return true;
    }

    /**
     * This implementation is empty.
     */
    @Override
    public void postHandle(ChannelHandlerContext ctx, HttpRequest request, Object handler,Object result) throws Exception {

    }

    /**
     * This implementation is empty.
     */
    @Override
    public void afterCompletion(ChannelHandlerContext ctx, HttpRequest request, Object handler, Exception ex) throws Exception {

    }
}
