package org.springframework.netty.http;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-24
 */
public class EpollTest {


    public static void main(String[] args) {
        System.out.println(Epoll.isAvailable());
        //System.out.println(KQueue.isAvailable());
    }
}
