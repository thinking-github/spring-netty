package org.springframework.netty.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-11-26
 */
@ChannelHandler.Sharable
public class NioEndpoint extends ChannelInboundHandlerAdapter {

    private final AtomicLong totalConnections = new AtomicLong();

    private final AtomicInteger connectionCount = new AtomicInteger();
    private final AtomicInteger registeredCount = new AtomicInteger();

    private final AtomicInteger errorCount = new AtomicInteger();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        registeredCount.incrementAndGet();
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        registeredCount.decrementAndGet();
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connectionCount.incrementAndGet();
        totalConnections.incrementAndGet();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionCount.decrementAndGet();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        errorCount.incrementAndGet();
        super.exceptionCaught(ctx, cause);
    }

    public int getConnectionCount() {
        return connectionCount.get();
    }

    public int getRegisteredCount() {
        return registeredCount.get();
    }

}
