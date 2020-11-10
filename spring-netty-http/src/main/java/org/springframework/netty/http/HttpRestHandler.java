package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.IOException;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-08-25
 */
public abstract class HttpRestHandler<T> implements RestHandler<T> {


    @Override
    public <R> R put(ChannelHandlerContext ctx, FullHttpRequest request, T inputBody)
            throws IOException {
        return null;
    }

    @Override
    public <R> R patch(ChannelHandlerContext ctx, FullHttpRequest request, T inputBody)
            throws IOException {
        return null;
    }

    @Override
    public <R> R delete(ChannelHandlerContext ctx, FullHttpRequest request)
            throws IOException {
        return null;
    }


    @Override
    public <R> R handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, T inputBody)
            throws IOException {
        HttpMethod method = request.method();

        Object result = null;
        if (method.equals(HttpMethod.GET)) {
            result = get(ctx, request);
        } else if (method.equals(HttpMethod.POST)) {

            result = post(ctx, request, inputBody);
        } else if (method.equals(HttpMethod.PUT)) {

            result = put(ctx, request, inputBody);

        } else if (method.equals(HttpMethod.PATCH)) {
            result = patch(ctx, request, inputBody);

        } else if (method.equals(HttpMethod.DELETE)) {
            result = delete(ctx, request);
        } else {
            //
            // Note that this means NO servlet supports whatever
            // method was requested, anywhere on this server.
            //
            result = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_IMPLEMENTED);

            // resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
        }

        return (R) result;
    }

}
