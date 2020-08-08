package org.springframework.netty.http;

import io.netty.bootstrap.ServerBootstrap;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-16
 */
public interface HttpServerConfigurer {

    void configure(ServerBootstrap bootstrap);

}
