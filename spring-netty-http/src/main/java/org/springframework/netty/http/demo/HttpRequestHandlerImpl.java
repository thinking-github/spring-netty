package org.springframework.netty.http.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpUtil.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-11
 */
@ChannelHandler.Sharable
public class HttpRequestHandlerImpl extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandlerImpl.class);


    public HttpRequestHandlerImpl() {
        logger.info("HttpRequestHandler initialize... ");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //100 Continue
        if (is100ContinueExpected(request)) {
            ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
        }


        boolean keepAlive = HttpUtil.isKeepAlive(request);

        boolean keepAliveDefault = request.protocolVersion().isKeepAliveDefault();

        //System.out.println("nihao");
        Map<String, String> map = new HashMap<>();
        map.put("id", "10");
        map.put("name", "thinking");
        map.put("nickname", "thinking");
        map.put("age", "19");
        map.put("sex", "1");
        map.put("icon", "thinking");
        map.put("thinking1", "thinking1");
        map.put("thinking2", "thinking2");
        map.put("thinking3", "thinking3");
        map.put("thinking4", "thinking4");
        map.put("thinking5", "thinking5");
        map.put("thinking6", "thinking6");
        map.put("thinking7", "thinking7");
        map.put("thinking8", "thinking8");
        map.put("thinking9", "thinking9");

        byte[] content = objectMapper.writeValueAsBytes(map);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content));

        HttpHeaders httpHeaders = response.headers();
        httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, "text/json; charset=UTF-8");
        httpHeaders.set(CONTENT_LENGTH, response.content().readableBytes());


        if (keepAlive) {
            if (!request.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, KEEP_ALIVE);
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers().set(CONNECTION, CLOSE);
        }

        //HttpUtil.setKeepAlive(response,keepAlive);

        ChannelFuture f = ctx.write(response);

        if (!keepAlive) {
            f.addListener(CLOSE);
        }

        //ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
       // System.out.println("added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
       // System.out.println("removed");
    }


}
