package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.IOException;

/**
 * rest request handler
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-24
 */
public interface RestHandler<T> extends HttpRequestHandler<T> {

    /**
     * handler http post request
     *
     * @param ctx
     * @param request
     * @param <R>
     * @return
     * @throws IOException
     */
    <R> R post(ChannelHandlerContext ctx, FullHttpRequest request, T inputBody)
            throws IOException;

    /**
     * handler http get request
     *
     * @param ctx
     * @param request
     * @param <R>
     * @return
     * @throws IOException
     */

    <R> R get(ChannelHandlerContext ctx, FullHttpRequest request)
            throws IOException;


    /**
     * handler http put request
     *
     * @param ctx
     * @param request
     * @param <R>
     * @return
     * @throws IOException
     */
    <R> R put(ChannelHandlerContext ctx, FullHttpRequest request, T inputBody)
            throws IOException;


    /**
     * handler http patch request
     *
     * @param ctx
     * @param request
     * @param <R>
     * @return
     * @throws IOException
     */
    <R> R patch(ChannelHandlerContext ctx, FullHttpRequest request, T inputBody)
            throws IOException;


    /**
     * handler http delete request
     *
     * @param ctx
     * @param request
     * @param <R>
     * @return
     * @throws IOException
     */
    <R> R delete(ChannelHandlerContext ctx, FullHttpRequest request)
            throws IOException;
}
