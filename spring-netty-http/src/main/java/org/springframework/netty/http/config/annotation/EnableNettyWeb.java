package org.springframework.netty.http.config.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingNettyWebConfiguration.class)
public @interface EnableNettyWeb {

}
