package org.springframework.netty.http.config.annotation;

import org.springframework.netty.http.HandlerExceptionResolver;
import org.springframework.netty.http.converter.HttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * WebMvcConfigurerComposite
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-16
 */
class NettyWebConfigurerComposite implements NettyWebConfigurer {

    private final List<NettyWebConfigurer> delegates = new ArrayList<NettyWebConfigurer>();


    public void addNettyWebConfigurers(List<NettyWebConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.delegates.addAll(configurers);
        }
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        for (NettyWebConfigurer delegate : this.delegates) {
            delegate.addInterceptors(registry);
        }
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (NettyWebConfigurer delegate : this.delegates) {
            delegate.configureMessageConverters(converters);
        }
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        for (NettyWebConfigurer delegate : this.delegates) {
            delegate.configureHandlerExceptionResolvers(exceptionResolvers);
        }
    }

    @Override
    public ExecutorService getThreadPoolExecutor() {
        ExecutorService selected = null;
        for (NettyWebConfigurer configurer : this.delegates) {
            ExecutorService executor = configurer.getThreadPoolExecutor();
            if (executor != null) {
                if (selected != null) {
                    throw new IllegalStateException("No unique Executor found: {" +
                            selected + ", " + executor + "}");
                }
                selected = executor;
            }
        }
        return selected;
    }

    @Override
    public Validator getValidator() {
        Validator selected = null;
        for (NettyWebConfigurer configurer : this.delegates) {
            Validator validator = configurer.getValidator();
            if (validator != null) {
                if (selected != null) {
                    throw new IllegalStateException("No unique Validator found: {" +
                            selected + ", " + validator + "}");
                }
                selected = validator;
            }
        }
        return selected;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> selected = null;
        for (NettyWebConfigurer configurer : this.delegates) {
            Map<String, Object> properties = configurer.getProperties();
            if (properties != null) {
                if (selected != null) {
                    throw new IllegalStateException("No unique properties found: {" +
                            selected + ", " + properties + "}");
                }
                selected = properties;
            }
        }
        return selected;
    }
}
