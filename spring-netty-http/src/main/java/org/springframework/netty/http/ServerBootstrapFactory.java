package org.springframework.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * com.sun.javafx.PlatformUtil
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-24
 */
public class ServerBootstrapFactory {

    private static final Logger logger = LoggerFactory.getLogger(ServerBootstrapFactory.class);

    private static final String os = System.getProperty("os.name");
    private static final String version = System.getProperty("os.version");

    private static final boolean WINDOWS = os.startsWith("Windows");
    private static final boolean MAC = os.startsWith("Mac");
    private static final boolean LINUX = os.startsWith("Linux");
    private static final boolean SOLARIS = os.startsWith("SunOS");

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public ServerBootstrap newServerBootstrap(int ioThreadCount) {
        if (useEpoll()) {
            return newEpollServerBootstrap(ioThreadCount);
        }

        return newNioServerBootstrap(ioThreadCount);
    }

    public void shutdownGracefully(boolean shouldWait) {
        Future<?> workerFuture = workerGroup.shutdownGracefully();
        Future<?> bossFuture = bossGroup.shutdownGracefully();

        if (shouldWait) {
            workerFuture.awaitUninterruptibly();
            bossFuture.awaitUninterruptibly();
        }
    }

    private ServerBootstrap newNioServerBootstrap(int ioThreadCount) {
        logger.info("netty server used NIO.");
        if (ioThreadCount > 0) {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(ioThreadCount);
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
        }

        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class);
    }

    private ServerBootstrap newEpollServerBootstrap(int ioThreadCount) {
        logger.info("netty server used Epoll.");
        if (ioThreadCount > 0) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(ioThreadCount);
        } else {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
        }

        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(EpollServerSocketChannel.class);
    }

    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }


    private boolean useEpoll() {
        return LINUX && Epoll.isAvailable();
    }


}
