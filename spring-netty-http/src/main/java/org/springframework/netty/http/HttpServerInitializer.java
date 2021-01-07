package org.springframework.netty.http;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import org.nbone.spring.boot.actuate.metrics.netty.ByteBufAllocatorMetrics;
import org.springframework.netty.http.demo.HttpRequestHandlerImpl;
import org.springframework.netty.http.metrics.ConnectionMetrics;
import org.springframework.util.ClassUtils;

import java.util.Collections;

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

    private NioEndpoint nioEndpoint;

    private ConnectionMetrics connectionMetrics;

    private final static boolean meterRegistryAvailable  = ClassUtils.isPresent("io.micrometer.core.instrument.MeterRegistry",
            HttpServerInitializer.class.getClassLoader());

    private final static boolean byteBufMetrics  = ClassUtils.isPresent("org.nbone.spring.boot.actuate.metrics.netty.ByteBufAllocatorMetrics",
            HttpServerInitializer.class.getClassLoader());


    public HttpServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    public HttpServerInitializer(SslContext sslCtx, DispatcherHandler dispatcherHandler,NioEndpoint nioEndpoint) {
        this.sslCtx = sslCtx;
        this.dispatcherHandler = dispatcherHandler;
        this.nioEndpoint = nioEndpoint;
        if (nioEndpoint != null && meterRegistryAvailable) {
            connectionMetrics = new ConnectionMetrics(nioEndpoint, Collections.emptyList());
        }
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(channel.alloc()));
        }

        if(meterRegistryAvailable && byteBufMetrics){
            ByteBufAllocator alloc = channel.alloc();
            if (alloc instanceof PooledByteBufAllocator) {
                ByteBufAllocatorMetrics.INSTANCE.registerMetrics("pooled", ((PooledByteBufAllocator) alloc).metric());
            }
            else if (alloc instanceof UnpooledByteBufAllocator) {
                ByteBufAllocatorMetrics.INSTANCE.registerMetrics("unpooled", ((UnpooledByteBufAllocator) alloc).metric());
            }
        }

        if(nioEndpoint != null){
            pipeline.addLast("nioEndpoint",nioEndpoint);
            if(connectionMetrics != null){
                connectionMetrics.registerMetrics();
            }

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
