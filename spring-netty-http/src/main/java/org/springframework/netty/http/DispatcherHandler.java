package org.springframework.netty.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ThrowableUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.netty.http.context.RequestContextHolder;
import org.springframework.netty.http.util.CountSampling;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-13
 */
@ChannelHandler.Sharable
public class DispatcherHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * Well-known name for the HandlerMapping object in the bean factory for this namespace.
     * Only used when "detectAllHandlerMappings" is turned off.
     *
     * @see #setDetectAllHandlerMappings
     */
    public static final String HANDLER_MAPPING_BEAN_NAME = "nettyHandlerMapping";

    /**
     * Well-known name for the HandlerAdapter object in the bean factory for this namespace.
     * Only used when "detectAllHandlerAdapters" is turned off.
     *
     * @see #setDetectAllHandlerAdapters
     */
    public static final String HANDLER_ADAPTER_BEAN_NAME = "nettyHandlerAdapter";

    /**
     * Well-known name for the HandlerExceptionResolver object in the bean factory for this namespace.
     * Only used when "detectAllHandlerExceptionResolvers" is turned off.
     * @see #setDetectAllHandlerExceptionResolvers
     */
    public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "nettyHandlerExceptionResolver";

    /**
     * Log category to use when no mapped handler is found for a request.
     */
    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

    /**
     * Name of the class path resource (relative to the DispatcherServlet class)
     * that defines DispatcherServlet's default strategy names.
     */
    private static final String DEFAULT_STRATEGIES_PATH = "DispatcherHandler.properties";

    /**
     * Additional logger to use when no mapped handler is found for a request.
     */
    protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

    private static final Properties defaultStrategies;

    public final static String MESSAGE_TEMPLATE = "{\"code\": %s,\"message\": \"%s\",\"exception\": \"%s\"}";

    static {
        // Load default strategy implementations from properties file.
        // This is currently strictly internal and not meant to be customized
        // by application developers.
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherHandler.class);
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load '" + DEFAULT_STRATEGIES_PATH + "': " + ex.getMessage());
        }
    }


    /**
     * Detect all HandlerMappings or just expect "handlerMapping" bean?
     */
    private boolean detectAllHandlerMappings = true;

    /**
     * Detect all HandlerAdapters or just expect "handlerAdapter" bean?
     */
    private boolean detectAllHandlerAdapters = true;

    /** Detect all HandlerExceptionResolvers or just expect "handlerExceptionResolver" bean? */
    private boolean detectAllHandlerExceptionResolvers = true;

    /**
     * Throw a NoHandlerFoundException if no Handler was found to process this request?
     **/
    private boolean throwExceptionIfNoHandlerFound = false;

    /**
     * List of HandlerMappings used by this netty http
     */
    private List<HandlerMapping> handlerMappings;

    /**
     * List of HandlerAdapters used by this netty http
     */
    private List<HandlerAdapter> handlerAdapters;

    /** List of HandlerExceptionResolvers used by this servlet */
    private List<HandlerExceptionResolver> handlerExceptionResolvers;


    private ExecutorService executorService;

    private CountSampling countSampling;

    private String[] directBackUrls = {"/favicon.ico"};


    public DispatcherHandler(ExecutorService executorService) {
        this.executorService = executorService;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (acceptInboundMessage(msg)) {
            @SuppressWarnings("unchecked")
            FullHttpRequest imsg = (FullHttpRequest) msg;
            // channelRead0(ctx, imsg);
            asyncDispatch(ctx, imsg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        doDispatch(ctx, request);
    }

    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        doDispatch(ctx, request);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        invokeHandlerException(ctx,cause);
    }


    /**
     * Set whether to detect all HandlerMapping beans in this servlet's context. Otherwise,
     * just a single bean with name "handlerMapping" will be expected.
     * <p>Default is "true". Turn this off if you want this servlet to use a single
     * HandlerMapping, despite multiple HandlerMapping beans being defined in the context.
     */
    public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
        this.detectAllHandlerMappings = detectAllHandlerMappings;
    }

    /**
     * Set whether to detect all HandlerAdapter beans in this servlet's context. Otherwise,
     * just a single bean with name "handlerAdapter" will be expected.
     * <p>Default is "true". Turn this off if you want this servlet to use a single
     * HandlerAdapter, despite multiple HandlerAdapter beans being defined in the context.
     */
    public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
        this.detectAllHandlerAdapters = detectAllHandlerAdapters;
    }

    /**
     * Set whether to detect all HandlerExceptionResolver beans in this servlet's context. Otherwise,
     * just a single bean with name "handlerExceptionResolver" will be expected.
     * <p>Default is "true". Turn this off if you want this servlet to use a single
     * HandlerExceptionResolver, despite multiple HandlerExceptionResolver beans being defined in the context.
     */
    public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
        this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        initStrategies(applicationContext);
    }

    public void setCountSampling(CountSampling countSampling) {
        this.countSampling = countSampling;
    }

    protected void initStrategies(ApplicationContext context) {
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
    }

    /**
     * Initialize the HandlerMappings used by this class.
     * <p>If no HandlerMapping beans are defined in the BeanFactory for this namespace,
     * we default to BeanNameUrlHandlerMapping.
     */
    private void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = null;

        if (this.detectAllHandlerMappings) {
            // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerMapping> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<HandlerMapping>(matchingBeans.values());
                // We keep HandlerMappings in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerMappings);
            }
        } else {
            try {
                HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
                this.handlerMappings = Collections.singletonList(hm);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerMapping later.
            }
        }

        // Ensure we have at least one HandlerMapping, by registering
        // a default HandlerMapping if no other mappings are found.
        if (this.handlerMappings == null) {
            this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerMappings found in servlet 'dispatcherHandler': using default");
            }
        }
    }

    /**
     * Initialize the HandlerAdapters used by this class.
     * <p>If no HandlerAdapter beans are defined in the BeanFactory for this namespace,
     * we default to SimpleControllerHandlerAdapter.
     */
    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = null;

        if (this.detectAllHandlerAdapters) {
            // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerAdapter> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerAdapters = new ArrayList<HandlerAdapter>(matchingBeans.values());
                // We keep HandlerAdapters in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerAdapters);
            }
        } else {
            try {
                HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
                this.handlerAdapters = Collections.singletonList(ha);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerAdapter later.
            }
        }

        // Ensure we have at least some HandlerAdapters, by registering
        // default HandlerAdapters if no other adapters are found.
        if (this.handlerAdapters == null) {
            this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerAdapters found in servlet 'dispatcherHandler': using default");
            }
        }
    }

    /**
     * Initialize the HandlerExceptionResolver used by this class.
     * <p>If no bean is defined with the given name in the BeanFactory for this namespace,
     * we default to no exception resolver.
     */
    private void initHandlerExceptionResolvers(ApplicationContext context) {
        this.handlerExceptionResolvers = null;

        if (this.detectAllHandlerExceptionResolvers) {
            // Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerExceptionResolvers = new ArrayList<HandlerExceptionResolver>(matchingBeans.values());
                // We keep HandlerExceptionResolvers in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
            }
        }
        else {
            try {
                HandlerExceptionResolver her =
                        context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
                this.handlerExceptionResolvers = Collections.singletonList(her);
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Ignore, no HandlerExceptionResolver is fine too.
            }
        }

        // Ensure we have at least some HandlerExceptionResolvers, by registering
        // default HandlerExceptionResolvers if no other resolvers are found.
        if (this.handlerExceptionResolvers == null) {
            this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerExceptionResolvers found in servlet 'dispatcherHandler': using default");
            }
        }
    }


    /**
     * Create a List of default strategy objects for the given strategy interface.
     * <p>The default implementation uses the "DispatcherServlet.properties" file (in the same
     * package as the DispatcherServlet class) to determine the class names. It instantiates
     * the strategy objects through the context's BeanFactory.
     *
     * @param context           the current WebApplicationContext
     * @param strategyInterface the strategy interface
     * @return the List of corresponding strategy objects
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
        String key = strategyInterface.getName();
        String value = defaultStrategies.getProperty(key);
        if (value != null) {
            String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
            List<T> strategies = new ArrayList<T>(classNames.length);
            for (String className : classNames) {
                try {
                    Class<?> clazz = ClassUtils.forName(className, DispatcherHandler.class.getClassLoader());
                    Object strategy = createDefaultStrategy(context, clazz);
                    strategies.add((T) strategy);
                } catch (ClassNotFoundException ex) {
                    throw new BeanInitializationException(
                            "Could not find DispatcherServlet's default strategy class [" + className +
                                    "] for interface [" + key + "]", ex);
                } catch (LinkageError err) {
                    throw new BeanInitializationException(
                            "Error loading DispatcherServlet's default strategy class [" + className +
                                    "] for interface [" + key + "]: problem with class file or dependent class", err);
                }
            }
            return strategies;
        } else {
            return new LinkedList<T>();
        }
    }

    /**
     * Create a default strategy.
     * <p>The default implementation uses
     * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean}.
     *
     * @param context the current WebApplicationContext
     * @param clazz   the strategy implementation class to instantiate
     * @return the fully configured strategy instance
     * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean
     */
    protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
        return context.getAutowireCapableBeanFactory().createBean(clazz);
    }


    protected void asyncDispatch(ChannelHandlerContext ctx, FullHttpRequest request) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                invokeDoDispatch(ctx, request);
            }
        });
    }
    private  void invokeDoDispatch(ChannelHandlerContext ctx, FullHttpRequest request){
        try {
            doDispatch(ctx, request);
        } catch (Exception e) {
            invokeExceptionCaught(ctx,e);
        }
    }

    protected void doDispatch(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        long count = countSampling.getCount();
        boolean sampling = countSampling.next();
        ctx.channel().attr(HandlerMapping.REQUEST_DISPATCH_COUNT_SAMPLING).set(sampling);
        if (logger.isDebugEnabled() && sampling) {
            long startTime = System.currentTimeMillis();
            ctx.channel().attr(HandlerMapping.REQUEST_DISPATCH_START_TIME).set(startTime);
            logger.debug("---> {} {}, count={}", request.method().name(), request.uri(), count);
            logger.debug("---> http headers : {}", request.headers());
        }
        HandlerExecutionChain mappedHandler = null;

        Object result = null;
        try {
            Exception dispatchException = null;
            try {
                mappedHandler = getHandler(ctx, request);
                if (mappedHandler == null || mappedHandler.getHandler() == null) {
                    noHandlerFound(ctx, request);
                    return;
                }

                // Determine handler adapter for the current request.
                HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());


                if (!mappedHandler.applyPreHandle(ctx, request)) {
                    return;
                }


                // Actually invoke the handler.
                result = ha.handle(ctx, request, mappedHandler.getHandler());


                mappedHandler.applyPostHandle(ctx, request, result);

            } catch (Exception ex) {
                dispatchException = ex;

            } catch (Throwable err) {
                dispatchException = new NestedServletException("Handler dispatch failed", err);
            }

            processDispatchResult(ctx, request, mappedHandler, result, dispatchException);


        } catch (Exception ex) {
            triggerAfterCompletion(ctx, request, mappedHandler, ex);

        } catch (Throwable err) {
            triggerAfterCompletion(ctx, request, mappedHandler,
                    new NestedServletException("Handler processing failed", err));
        } finally {

            //SimpleChannelInboundHandler autoRelease
            //request.release();

            // Override channelRead must release
            ReferenceCountUtil.release(request);
        }
    }

    /**
     * Handle the result of handler selection and handler invocation, which is
     * either a ModelAndView or an Exception to be resolved to a ModelAndView.
     */
    private void processDispatchResult(ChannelHandlerContext ctx, FullHttpRequest request,
                                       HandlerExecutionChain mappedHandler, Object result, Exception exception) throws Exception {

        Object mv = null;
        if (exception != null) {
            mv = processHandlerException(ctx, request, exception);
            if (result == null && mv != null) {
                result = mv;
            }
        }

        // not exception
        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(ctx, request, null);
        }

        write(ctx, request, result);
    }

    /**
     * write data client
     *
     * @param ctx
     * @param request
     * @param result
     */
    private void write(ChannelHandlerContext ctx, FullHttpRequest request, Object result) throws Exception{
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        FullHttpResponse response = null;

        if (result instanceof FullHttpResponse) {
            response = (FullHttpResponse) result;
        } else if (result == null) {
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.EMPTY_BUFFER);

        } else {
            throw new IllegalStateException("result data format not support " + result.getClass());
        }

        HttpHeaders httpHeaders = response.headers();

        if (!httpHeaders.contains(CONTENT_LENGTH)) {
            httpHeaders.set(CONTENT_LENGTH, response.content().readableBytes());
        }

        Boolean sampling = ctx.channel().attr(HandlerMapping.REQUEST_DISPATCH_COUNT_SAMPLING).get();
        if (logger.isDebugEnabled() && sampling) {
            long endTime = System.currentTimeMillis();
            Long startTime = ctx.channel().attr(HandlerMapping.REQUEST_DISPATCH_START_TIME).get();
            if (startTime == null) {
                startTime = endTime;
            }
            logger.debug("<--- {} {} ({}ms)", response.protocolVersion(), response.status().code(), endTime - startTime);
            logger.debug("http headers: {}", response.headers());
            logger.debug("<--- END HTTP ({}-byte body)", response.content().readableBytes());
        }


        if (!keepAlive) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        }
    }


    private void invokeExceptionCaught(ChannelHandlerContext ctx, final Throwable cause) {
        try {
            exceptionCaught(ctx, cause);
        } catch (Throwable error) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "An exception {}" +
                                "was thrown by a user handler's exceptionCaught() " +
                                "method while handling the following exception:",
                        ThrowableUtil.stackTraceToString(error), cause);
            } else if (logger.isWarnEnabled()) {
                logger.warn(
                        "An exception '{}' [enable DEBUG level for full stacktrace] " +
                                "was thrown by a user handler's exceptionCaught() " +
                                "method while handling the following exception:", error, cause);
            }
        }
    }


    protected Object processHandlerException(ChannelHandlerContext ctx,FullHttpRequest request, Exception ex) throws Exception {

        // Check registered HandlerExceptionResolvers...
        Object exMv = null;
        for (HandlerExceptionResolver handlerExceptionResolver : this.handlerExceptionResolvers) {
            exMv = handlerExceptionResolver.resolveException(ctx, request, null, ex);
            if (exMv != null) {
                break;
            }
        }

        if (exMv != null) {
            return exMv;
        }

        throw ex;
    }

    private void invokeHandlerException(ChannelHandlerContext ctx, Throwable ex) throws Exception {
        FullHttpRequest request = RequestContextHolder.getRequest(ctx);
        Object exMv = processHandlerException(ctx,request, (Exception) ex);
        FullHttpResponse response = null;
        if (exMv instanceof FullHttpResponse) {
            response = (FullHttpResponse) exMv;
        } else {
            String message = String.format(MESSAGE_TEMPLATE,0,ex.getMessage(),ex.getClass().getSimpleName());
            response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, Unpooled.wrappedBuffer(message.getBytes()));
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Return the HandlerExecutionChain for this request.
     * <p>Tries all handler mappings in order.
     *
     * @param request current HTTP request
     * @return the HandlerExecutionChain, or {@code null} if no handler could be found
     */
    protected HandlerExecutionChain getHandler(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        for (HandlerMapping hm : this.handlerMappings) {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Testing handler map [" + hm + "] in DispatcherHandler with name dispatcherHandler");
            }
            HandlerExecutionChain handler = hm.getHandler(ctx, request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }


    /**
     * Return the HandlerAdapter for this handler object.
     *
     * @param handler the handler object to find an adapter for
     * @throws Exception if no HandlerAdapter can be found for the handler. This is a fatal error.
     */
    protected HandlerAdapter getHandlerAdapter(Object handler) throws Exception {
        for (HandlerAdapter ha : this.handlerAdapters) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing handler adapter [" + ha + "]");
            }
            if (ha.supports(handler)) {
                return ha;
            }
        }
        throw new Exception("No adapter for handler [" + handler +
                "]: The DispatcherHandler configuration needs to include a HandlerAdapter that supports this handler");
    }




    /**
     * No handler found -> set appropriate HTTP response status.
     *
     * @param ctx     current HTTP  Context
     * @param request current HTTP request
     * @throws Exception if preparing the response failed
     */
    protected void noHandlerFound(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (pageNotFoundLogger.isWarnEnabled()) {
            pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + request.uri() +
                    "] in DispatcherHandler with name dispatcherHandler");
        }
        if (this.throwExceptionIfNoHandlerFound) {
            throw new NoHandlerFoundException(request.method().name(), request.uri(), request.headers());
        } else {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            //response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }


    private void triggerAfterCompletion(ChannelHandlerContext ctx, FullHttpRequest request,
                                        HandlerExecutionChain mappedHandler, Exception ex) throws Exception {

        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(ctx, request, ex);
        }
        throw ex;
    }


}
