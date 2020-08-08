package org.springframework.netty.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

/**
 *
 * @author thinking
 * @version 1.0
 * @since 2020-04-21
 */
@Slf4j
@Configuration
public class ContextStartedEventListener implements ApplicationListener<ContextStartedEvent>{


    @Override
    public void onApplicationEvent(ContextStartedEvent event) {


        log.info("ContextStartedEvent  ...");
    }
}
