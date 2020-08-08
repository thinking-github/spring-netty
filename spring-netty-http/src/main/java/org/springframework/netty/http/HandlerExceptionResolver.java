package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-20
 */
public interface HandlerExceptionResolver {



    /**
     * Try to resolve the given exception that got thrown during handler execution
     *
     * @param ctx
     * @param handler
     * @param ex
     * @return
     */
    Object resolveException(ChannelHandlerContext ctx, FullHttpRequest request, Object handler, Throwable ex);
}
