package org.springframework.netty.http;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.netty.http.config.annotation.InterceptorRegistry;
import org.springframework.netty.http.config.annotation.NettyWebConfigurer;
import org.springframework.netty.http.converter.HttpMessageConverter;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
@Configuration
//@EnableNettyWeb
public class NettyHttpConfig implements NettyWebConfigurer, ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       // registry.addInterceptor(new LogHandlerInterceptor());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {

    }

    @Override
    public ExecutorService getThreadPoolExecutor() {
        return null;
    }

    @Override
    public Validator getValidator() {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }
}
