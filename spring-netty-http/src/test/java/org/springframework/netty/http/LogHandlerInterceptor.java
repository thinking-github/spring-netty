package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-19
 */
public class LogHandlerInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(ChannelHandlerContext ctx, HttpRequest request, Object handler) throws Exception {
        System.out.println("preHandle");
        return true;
    }

    @Override
    public void postHandle(ChannelHandlerContext ctx, HttpRequest request, Object handler, Object result) throws Exception {
        System.out.println("postHandle");
    }

    @Override
    public void afterCompletion(ChannelHandlerContext ctx, HttpRequest request, Object handler, Exception ex) throws Exception {

        System.out.println("afterCompletion");
    }
}
