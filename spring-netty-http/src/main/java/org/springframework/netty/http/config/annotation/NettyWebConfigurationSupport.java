package org.springframework.netty.http.config.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.netty.http.DispatcherHandler;
import org.springframework.netty.http.HandlerExceptionResolver;
import org.springframework.netty.http.HttpServer;
import org.springframework.netty.http.converter.FormHttpMessageConverter;
import org.springframework.netty.http.converter.HttpMessageConverter;
import org.springframework.netty.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.netty.http.handler.SimpleUrlHandlerMapping;
import org.springframework.netty.http.mvc.DelegatingHandlerExceptionResolver;
import org.springframework.netty.http.mvc.HttpRequestHandlerAdapter;
import org.springframework.netty.http.support.DefaultHandlerExceptionResolver;
import org.springframework.netty.http.util.CountSampling;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Validator;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * WebMvcConfigurationSupport
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-16
 */
public class NettyWebConfigurationSupport implements ApplicationContextAware,EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(NettyWebConfigurationSupport.class);

    private static final boolean jaxb2Present =
            ClassUtils.isPresent("javax.xml.bind.Binder",
                    NettyWebConfigurationSupport.class.getClassLoader());

    private static final boolean jackson2Present =
            ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper",
                    NettyWebConfigurationSupport.class.getClassLoader()) &&
                    ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator",
                            NettyWebConfigurationSupport.class.getClassLoader());

    private static final boolean jackson2XmlPresent =
            ClassUtils.isPresent("com.fasterxml.jackson.dataformat.xml.XmlMapper",
                    NettyWebConfigurationSupport.class.getClassLoader());

    private static final boolean gsonPresent =
            ClassUtils.isPresent("com.google.gson.Gson",
                    NettyWebConfigurationSupport.class.getClassLoader());

    private Environment environment;

    private ApplicationContext applicationContext;

    private List<Object> interceptors;

    private List<HttpMessageConverter<?>> messageConverters;

    private ExecutionProperties executionProperties = new ExecutionProperties();

    private HttpServer httpServer;

    private Map<String,Object> properties;

    private CountSampling countSampling = new CountSampling();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        initExecutionProperties();
    }

    /**
     * Set the Spring {@link ApplicationContext}, e.g. for resource loading.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        // RequestMapping
        Map<String, Object> handlers = applicationContext.getBeansWithAnnotation(RequestMapping.class);
        SimpleUrlHandlerMapping handlerMapping = simpleUrlHandlerMapping();
        for (Map.Entry<String, Object> entry : handlers.entrySet()) {
            Object handler = entry.getValue();
            RequestMapping requestMapping = AnnotationUtils.getAnnotation(handler.getClass(), RequestMapping.class);
            String[] path = requestMapping.value();
            //RequestMethod[] requestMethods = requestMapping.method();

            handlerMapping.registerHandler(path, handler);
        }
    }


    /**
     * Return the associated Spring {@link ApplicationContext}.
     */
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    /**
     * Set the interceptors to apply for all handlers mapped by this handler mapping.
     * <p>Supported interceptor types are HandlerInterceptor, WebRequestInterceptor, and MappedInterceptor.
     * Mapped interceptors apply only to request URLs that match its path patterns.
     * Mapped interceptor beans are also detected by type during initialization.
     * @param interceptors array of handler interceptors
     */
    public void setInterceptors(Object... interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
    }

    /**
     * Provide access to the shared handler interceptors used to configure
     */
    protected final Object[] getInterceptors() {
        if (this.interceptors == null) {
            InterceptorRegistry registry = new InterceptorRegistry();
            addInterceptors(registry);
            this.interceptors = registry.getInterceptors();
        }
        return this.interceptors.toArray();
    }


    /**
     * Override this method to add Spring MVC interceptors for
     * pre- and post-processing of controller invocation.
     * @see InterceptorRegistry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
    }


    /**
     * Provides access to the shared {@link HttpMessageConverter HttpMessageConverters}
     * <p>This method cannot be overridden; use {@link #configureMessageConverters} instead.
     * Also see {@link #addDefaultHttpMessageConverters} for adding default message converters.
     */
    protected final List<HttpMessageConverter<?>> getMessageConverters() {
        if (this.messageConverters == null) {
            this.messageConverters = new ArrayList<HttpMessageConverter<?>>();
            configureMessageConverters(this.messageConverters);
            if (this.messageConverters.isEmpty()) {
                addDefaultHttpMessageConverters(this.messageConverters);
            }
            extendMessageConverters(this.messageConverters);
        }
        return this.messageConverters;
    }

    /**
     * Override this method to add custom {@link HttpMessageConverter HttpMessageConverters}
     * <p>Adding converters to the list turns off the default converters that would
     * otherwise be registered by default. Also see {@link #addDefaultHttpMessageConverters}
     * for adding default message converters.
     * @param converters a list to add message converters to (initially an empty list)
     */
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    /**
     * Override this method to extend or modify the list of converters after it has
     * been configured. This may be useful for example to allow default converters
     * to be registered and then insert a custom converter through this method.
     * @param converters the list of configured converters to extend
     * @since 4.1.3
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    /**
     * Adds a set of default HttpMessageConverter instances to the given list.
     * Subclasses can call this method from {@link #configureMessageConverters}.
     * @param messageConverters the list to add the default message converters to
     */
    protected final void addDefaultHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        if (jackson2Present) {
            ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
            if (objectMapper == null) {
                objectMapper = Jackson2ObjectMapperBuilder.json().applicationContext(this.applicationContext).build();
            }
            messageConverters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        } else if (gsonPresent) {
            //messageConverters.add(new GsonHttpMessageConverter());
        }

        // support application/x-www-form-urlencoded
        messageConverters.add(new FormHttpMessageConverter());
    }


    /**
     * Override this method to provide a custom {@link Executor}.
     */
    protected ExecutorService getThreadPoolExecutor(){
        return null;
    }


    /**
     * Override this method to provide a custom {@link Validator}.
     */
    protected Validator getValidator() {
        return null;
    }


    /**
     * Override this method to provide a custom  Configuration{@link Properties}.
     */
    protected Map<String, Object> getProperties() {
        return null;
    }




    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(0);
        mapping.setInterceptors(getInterceptors());
        return mapping;
    }

    /**
     * Returns a {@link HttpRequestHandlerAdapter} for processing requests
     * with {@link HttpRequestHandler}s.
     */
    @Bean
    public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
        HttpRequestHandlerAdapter adapter =  new HttpRequestHandlerAdapter();
        adapter.setMessageConverters(getMessageConverters());
        return adapter;
    }

    /**
     * Returns a {@link DelegatingHandlerExceptionResolver} containing a list of exception
     * resolvers obtained either through {@link #configureHandlerExceptionResolvers} or
     * through {@link #addDefaultHandlerExceptionResolvers}.
     * <p><strong>Note:</strong> This method cannot be made final due to CGLIB constraints.
     * Rather than overriding it, consider overriding {@link #configureHandlerExceptionResolvers}
     * which allows for providing a list of resolvers.
     */
    @Bean
    public HandlerExceptionResolver handlerExceptionResolver() {
        List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<HandlerExceptionResolver>();
        configureHandlerExceptionResolvers(exceptionResolvers);
        if (exceptionResolvers.isEmpty()) {
            addDefaultHandlerExceptionResolvers(exceptionResolvers);
        }

        DelegatingHandlerExceptionResolver delegating = new DelegatingHandlerExceptionResolver();
        delegating.setExceptionResolvers(exceptionResolvers);
        delegating.setMessageConverters(getMessageConverters());
        return delegating;
    }

    /**
     * Override this method to configure the list of
     * {@link HandlerExceptionResolver HandlerExceptionResolvers} to use.
     * <p>Adding resolvers to the list turns off the default resolvers that would otherwise
     * be registered by default. Also see {@link #addDefaultHandlerExceptionResolvers}
     * that can be used to add the default exception resolvers.
     * @param exceptionResolvers a list to add exception resolvers to (initially an empty list)
     */
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {

    }

    /**
     * A method available to subclasses for adding default
     * {@link HandlerExceptionResolver HandlerExceptionResolvers}.
     * <p>Adds the following exception resolvers:
     * <ul>
     */
    protected final void addDefaultHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(new DefaultHandlerExceptionResolver());
    }


    @Bean
    public DispatcherHandler dispatcherHandler() {
        this.properties = getProperties();
        DispatcherHandler dispatcherHandler = new DispatcherHandler(executorService());

        if (this.properties != null) {
            Integer min = (Integer) this.getProperties().get("count.sampling.min");
            Integer interval = (Integer) this.getProperties().get("count.sampling.interval");
            if (min != null) {
                countSampling.setMin(min);
            }
            if (interval != null) {
                countSampling.setInterval(interval);
            }
        }
        dispatcherHandler.setCountSampling(countSampling);
        return dispatcherHandler;
    }

    @Bean
    public HttpServer httpServer() {
        HttpServer httpServer = new HttpServer(dispatcherHandler());
        this.httpServer = httpServer;
        return httpServer;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService executorService() {
        ExecutorService executorService = getThreadPoolExecutor();
        if (executorService == null) {
            if(logger.isInfoEnabled()){
                logger.info("netty web threadPool " + executionProperties.toString());
            }
            ThreadPoolExecutorFactoryBean taskExecutor = new ThreadPoolExecutorFactoryBean();
            taskExecutor.setCorePoolSize(executionProperties.getCoreSize());
            taskExecutor.setMaxPoolSize(executionProperties.getMaxSize());
            taskExecutor.setQueueCapacity(executionProperties.getQueueCapacity());
            taskExecutor.setThreadNamePrefix(executionProperties.getThreadNamePrefix());
            taskExecutor.setKeepAliveSeconds(executionProperties.getKeepAlive());
            taskExecutor.initialize();
            executorService = taskExecutor.getObject();
        }
        return executorService;
    }

    private void initExecutionProperties() {
        Integer coreSize = environment.getProperty("server.netty.corePoolSize", Integer.class);
        Integer maxSize = environment.getProperty("server.netty.max-threads", Integer.class);
        Integer capacity = environment.getProperty("server.netty.queueCapacity", Integer.class);

        if (coreSize == null) {
            coreSize = environment.getProperty("server.tomcat.min-spare-threads", Integer.class);
        }
        if (maxSize == null) {
            maxSize = environment.getProperty("server.tomcat.max-threads", Integer.class);
        }

        if (coreSize == null) {
            coreSize = environment.getProperty("server.undertow.worker-threads", Integer.class);
            maxSize = coreSize;
        }

        if (coreSize != null) {
            executionProperties.setCoreSize(coreSize);
        }
        if (maxSize != null) {
            executionProperties.setMaxSize(maxSize);
        }
        if (capacity != null) {
            executionProperties.setQueueCapacity(capacity);
        }else {
            if(maxSize != null){
                executionProperties.setQueueCapacity(maxSize * 10);
            }
        }
    }


    protected Map<String, MediaType> getDefaultMediaTypes() {
        Map<String, MediaType> map = new HashMap<String, MediaType>(4);
       /* if (romePresent) {
            map.put("atom", MediaType.APPLICATION_ATOM_XML);
            map.put("rss", MediaType.APPLICATION_RSS_XML);
        }*/
        if (jaxb2Present || jackson2XmlPresent) {
            map.put("xml", MediaType.APPLICATION_XML);
        }
        if (jackson2Present || gsonPresent) {
            map.put("json", MediaType.APPLICATION_JSON);
        }
        return map;
    }



}
