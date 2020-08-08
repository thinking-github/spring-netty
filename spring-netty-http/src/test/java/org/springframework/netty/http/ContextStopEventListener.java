package org.springframework.netty.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;

/**
 * Created by thinking on 2020-04-22.
 *
 * @author thinking
 * @version 1.0
 * @since 2020-04-22
 */
@Slf4j
@Configuration
public class ContextStopEventListener implements ApplicationListener<ContextStoppedEvent> {

    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        log.info("ContextStoppedEvent  ...");
    }
}
