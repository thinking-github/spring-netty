package org.springframework.netty.http.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.BaseUnits;
import org.springframework.netty.http.NioEndpoint;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-09-28
 */
public class ConnectionMetrics {

    private NioEndpoint nioEndpoint;

    private final Iterable<Tag> tags;

    private boolean initialize = false;

    public ConnectionMetrics(NioEndpoint nioEndpoint, Iterable<Tag> tags) {
        this.nioEndpoint = nioEndpoint;
        this.tags = tags;
    }

    public void registerMetrics() {
        registerMetrics(io.micrometer.core.instrument.Metrics.globalRegistry);
    }
    public void registerMetrics(MeterRegistry registry) {
        if (initialize) {
            return;
        }
        if (registry == null) {
            registry = io.micrometer.core.instrument.Metrics.globalRegistry;
        }
        Gauge.builder("netty.connections.active", nioEndpoint, NioEndpoint::getConnectionCount)
                .tags(tags)
                .baseUnit(BaseUnits.CONNECTIONS)
                .description("The state of connections in the netty connections active")
                .register(registry);

        Gauge.builder("netty.connections.registered", nioEndpoint, NioEndpoint::getRegisteredCount)
                .tags(tags)
                .baseUnit(BaseUnits.CONNECTIONS)
                .description("The state of connections in the netty connections registered")
                .register(registry);
        initialize = true;
    }
}
