package org.springframework.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import org.springframework.netty.http.demo.HttpRequestHandlerImpl;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-11
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    // DEFAULT MAX 1MB
    private static final int DEFAULT_MAX_CONTENT_LENGTH = 1024 * 1024;

    private int maxContentLength = DEFAULT_MAX_CONTENT_LENGTH;

    private final SslContext sslCtx;

    private HttpRequestHandlerImpl httpRequestHandler;

    //private DefaultEventExecutorGroup  eventExecutorGroup = new DefaultEventExecutorGroup(200);

    private DispatcherHandler dispatcherHandler;


    public HttpServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    public HttpServerInitializer(SslContext sslCtx, DispatcherHandler dispatcherHandler) {
        this.sslCtx = sslCtx;
        this.dispatcherHandler = dispatcherHandler;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(channel.alloc()));
        }

        // http 编解码
        pipeline.addLast(new HttpServerCodec());
        // http 消息聚合器  maxContentLength 1024 *1024
        pipeline.addLast("httpAggregator", new HttpObjectAggregator(maxContentLength));
        // 请求处理器
        if (dispatcherHandler != null) {
            //pipeline.addLast(new HttpRequestHandler1());
            //pipeline.addLast(eventExecutorGroup,"dispatcherChannelHandler",dispatcherHandler);
            pipeline.addLast("dispatcherChannelHandler", dispatcherHandler);
        } else {
            if (httpRequestHandler == null) {
                httpRequestHandler = new HttpRequestHandlerImpl();
            }
            pipeline.addLast(httpRequestHandler);
        }

    }

}
