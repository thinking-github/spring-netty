package org.springframework.boot.actuate.endpoint;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-08-25
 */
public class HealthMvcEndpoint extends org.springframework.boot.actuate.netty.endpoint.HealthMvcEndpoint {


    public HealthMvcEndpoint(boolean springHealth) {
        super(springHealth);
    }
}
