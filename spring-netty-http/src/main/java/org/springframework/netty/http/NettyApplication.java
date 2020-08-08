package org.springframework.netty.http;

/**
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-13
 */
public class NettyApplication {

    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer(8080);// 8081为启动端口
        server.startup();

    }
}
