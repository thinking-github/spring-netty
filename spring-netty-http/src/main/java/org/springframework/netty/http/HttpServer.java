package org.springframework.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
 * ServerProperties
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-11
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class HttpServer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private static final ChannelGroup allChannels = new DefaultChannelGroup("nettyHttpServer", GlobalEventExecutor.INSTANCE);

    @Value("${server.port:8080}")
    private int port = 8080;

    @Value("${server.ssl.enabled:false}")
    private boolean secure = false;

    private ServerBootstrapFactory bootstrapFactory = new ServerBootstrapFactory();

    private DispatcherHandler dispatcherHandler;

    @Autowired(required = false)
    private HttpServerConfigurer httpServerConfigurer;

    public HttpServer(DispatcherHandler dispatcherHandler) {
        this.dispatcherHandler = dispatcherHandler;
    }

    public HttpServer(DispatcherHandler dispatcherHandler, HttpServerConfigurer httpServerConfigurer) {
        this.dispatcherHandler = dispatcherHandler;
        this.httpServerConfigurer = httpServerConfigurer;
    }

    public HttpServer(int port) {
        this.port = port;
    }

    public void setHttpServerConfigurer(HttpServerConfigurer httpServerConfigurer) {
        this.httpServerConfigurer = httpServerConfigurer;
    }

    public Channel startup() throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (secure) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        ServerBootstrap bootstrap = bootstrapFactory.newServerBootstrap(-1);

        //options...
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);


        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 4096);
        bootstrap.childOption(ChannelOption.SO_RCVBUF, 4096);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        if (httpServerConfigurer != null) {
            httpServerConfigurer.configure(bootstrap);
        }


        //.handler(new LoggingHandler(LogLevel.ERROR))
        bootstrap.childHandler(new HttpServerInitializer(sslCtx, dispatcherHandler));

        ChannelFuture f = bootstrap.bind(new InetSocketAddress(port)).sync();
        Channel mainChannel = f.channel();
        allChannels.add(mainChannel);

        logger.info("netty http server start up on port : " + port);

        //FIXME 会阻塞Spring后处理
        //Wait until the server socket is closed. Thread gets blocked.
        //f.channel().closeFuture().sync();
        return mainChannel;

    }


    /**
     * Releases all resources associated with this server so the JVM can
     * shutdown cleanly. Call this method to finish using the server. To utilize
     * the default shutdown hook in main() provided by RestExpress, call
     * awaitShutdown() instead.
     * <p/>
     * Same as shutdown(false);
     */
    @PreDestroy
    public void shutdown() {
        shutdown(false);
    }

    /**
     * Releases all resources associated with this server so the JVM can
     * shutdown cleanly. Call this method to finish using the server. To utilize
     * the default shutdown hook in main() provided by RestExpress, call
     * awaitShutdown() instead.
     *
     * @param shouldWait true if shutdown() should wait for the shutdown of each thread group.
     */
    public void shutdown(boolean shouldWait) {
        ChannelGroupFuture channelFuture = allChannels.close();
        bootstrapFactory.shutdownGracefully(shouldWait);
        channelFuture.awaitUninterruptibly();
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //ApplicationContext parent = event.getApplicationContext().getParent();
        logger.info("netty http server for spring ContextRefreshedEvent");
        try {
            this.startup();
        } catch (Exception e) {
            throw new RuntimeException("netty http server starting failed.", e);
        }

    }


}
