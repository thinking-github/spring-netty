package org.springframework.netty.http.config.annotation;

import org.springframework.netty.http.HandlerExceptionResolver;
import org.springframework.netty.http.converter.HttpMessageConverter;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * WebMvcConfigurer
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-16
 */
public interface NettyWebConfigurer {


    /**
     * @param registry
     */
    void addInterceptors(InterceptorRegistry registry);


    /**
     * @param converters
     */
    void configureMessageConverters(List<HttpMessageConverter<?>> converters);


    void extendMessageConverters(List<HttpMessageConverter<?>> converters);

    /**
     * @param exceptionResolvers
     */
    void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers);

    /**
     * customize thread pool  Executor
     *
     * @return
     */
    ExecutorService getThreadPoolExecutor();


    Validator getValidator();

    /**
     * customize Configuration
     *
     * @return
     */
    Map<String, Object> getProperties();


}
