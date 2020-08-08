package org.springframework.netty.http.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.netty.http.HandlerMapping;
import org.springframework.netty.http.codec.QueryDecoder;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public abstract class RequestContextHolder {


   /* public static HttpServletRequest getRequest() {
        return request;
    }*/

    public static FullHttpRequest getRequest(ChannelHandlerContext ctx) {
        FullHttpRequest request = ctx.channel().attr(HandlerMapping.HTTP_REQUEST).get();

        return request;
    }



    public static QueryDecoder getRequestQuery(ChannelHandlerContext ctx) {

        QueryDecoder query = ctx.channel().attr(HandlerMapping.REQUEST_QUERY_URI).get();

        return query;
    }

    public String getRequestURI(ChannelHandlerContext ctx) {
        QueryStringDecoder query = ctx.channel().attr(HandlerMapping.REQUEST_QUERY_URI).get();
        return query.uri();
    }

    public String getRequestPath(ChannelHandlerContext ctx) {
        QueryStringDecoder query = ctx.channel().attr(HandlerMapping.REQUEST_QUERY_URI).get();
        return query.path();
    }


}
