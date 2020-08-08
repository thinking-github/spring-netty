package org.springframework.boot.actuate.endpoint;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.boot.actuate.health.Health;
import org.springframework.netty.http.HttpRequestHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.Principal;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-04-13
 */
//@Controller
//@RequestMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthMvcEndpoint implements HttpRequestHandler<Object> {

    @Resource
    private HealthEndpoint healthEndpoint;

    private volatile CachedHealth cachedHealth;

    public static Health UP = Health.up().build();

    public boolean springHealth = true;

    public HealthMvcEndpoint(boolean springHealth) {
        this.springHealth = springHealth;
    }

    @Override
    public <R> R handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, Object inputBody) throws IOException {

        Health health = getHealth(request, null);
        return (R) health;
    }

    private Health getHealth(FullHttpRequest request, Principal principal) {
        if (springHealth) {
            Health currentHealth = getCurrentHealth();
            return currentHealth;
        }

        return UP;
    }

    private Health getCurrentHealth() {

        long timeToLive = healthEndpoint.getTimeToLive();
        //<= 1000ms not cache
        if (timeToLive <= 1000) {
            return healthEndpoint.invoke();
        }

        long accessTime = System.currentTimeMillis();
        CachedHealth cached = this.cachedHealth;
        if (cached == null || cached.isStale(accessTime, timeToLive)) {
            Health health = healthEndpoint.invoke();
            this.cachedHealth = new CachedHealth(health, accessTime);
            return health;
        }
        return cached.getHealth();
    }


    static class CachedHealth {

        private final Health health;

        private final long creationTime;

        CachedHealth(Health health, long creationTime) {
            this.health = health;
            this.creationTime = creationTime;
        }

        public boolean isStale(long accessTime, long timeToLive) {
            return (accessTime - this.creationTime) >= timeToLive;
        }

        public Health getHealth() {
            return this.health;
        }

    }

}
