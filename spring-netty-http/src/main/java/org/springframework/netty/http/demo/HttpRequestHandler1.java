package org.springframework.netty.http.demo;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-22
 */
//@ChannelHandler.Sharable
public class HttpRequestHandler1 extends ChannelInboundHandlerAdapter {

    public static AttributeKey<String> HTTP_REQUEST_STRING = AttributeKey.valueOf("http_request_string");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println( ctx.channel().attr(HTTP_REQUEST_STRING).get());
        ctx.channel().attr(HTTP_REQUEST_STRING).set("HttpRequestHandler1");
        ctx.fireChannelRead(msg);
    }
}
