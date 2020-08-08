package org.springframework.netty.http;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
@SpringBootApplication
public class NettyApplication1 {

    public static void main(String[] args) throws Exception {
        //SpringApplication.run(NettyApplication1.class, args);
        new SpringApplicationBuilder(NettyApplication1.class).web(false).run(args);
    }


}
