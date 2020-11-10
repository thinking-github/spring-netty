package org.springframework.netty.http.config.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.netty.http.HandlerExceptionResolver;
import org.springframework.netty.http.converter.HttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * DelegatingWebMvcConfiguration
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-16
 */
@Configuration
public class DelegatingNettyWebConfiguration extends NettyWebConfigurationSupport {

    private final NettyWebConfigurerComposite configurers = new NettyWebConfigurerComposite();

    @Autowired(required = false)
    public void setConfigurers(List<NettyWebConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.configurers.addNettyWebConfigurers(configurers);
        }
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        this.configurers.addInterceptors(registry);
    }


    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        this.configurers.configureMessageConverters(converters);
    }

    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        this.configurers.configureHandlerExceptionResolvers(exceptionResolvers);
    }

    @Override
    protected ExecutorService getThreadPoolExecutor() {
        return this.configurers.getThreadPoolExecutor();
    }

    @Override
    protected Validator getValidator() {
        return this.configurers.getValidator();
    }

    @Override
    protected Map<String, Object> getProperties() {
        return this.configurers.getProperties();
    }
}
