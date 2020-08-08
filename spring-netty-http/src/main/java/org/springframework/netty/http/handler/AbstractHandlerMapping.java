package org.springframework.netty.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.netty.http.HandlerExecutionChain;
import org.springframework.netty.http.HandlerInterceptor;
import org.springframework.netty.http.HandlerMapping;
import org.springframework.netty.http.codec.QueryDecoder;
import org.springframework.netty.http.context.RequestContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 */
public abstract class AbstractHandlerMapping  extends ApplicationObjectSupport
        implements HandlerMapping, Ordered {

    /**
     * Logger that is available to subclasses
     */
    protected final Log logger = LogFactory.getLog(getClass());

    private Object defaultHandler;

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final List<Object> interceptors = new ArrayList<Object>();

    private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<HandlerInterceptor>();


    private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

    /**
     * Set the default handler for this handler mapping.
     * This handler will be returned if no specific mapping was found.
     * <p>Default is {@code null}, indicating no default handler.
     */
    public void setDefaultHandler(Object defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    /**
     * Return the default handler for this handler mapping,
     * or {@code null} if none.
     */
    public Object getDefaultHandler() {
        return this.defaultHandler;
    }





    /**
     * Initializes the interceptors.
     * @see #extendInterceptors(java.util.List)
     * @see #initInterceptors()
     */
    @Override
    protected void initApplicationContext() throws BeansException {
        extendInterceptors(this.interceptors);
        detectMappedInterceptors(this.adaptedInterceptors);
        initInterceptors();
    }

    /**
     * Extension hook that subclasses can override to register additional interceptors,
     * given the configured interceptors (see {@link #setInterceptors}).
     * <p>Will be invoked before {@link #initInterceptors()} adapts the specified
     * interceptors into {@link HandlerInterceptor} instances.
     * <p>The default implementation is empty.
     * @param interceptors the configured interceptor List (never {@code null}), allowing
     * to add further interceptors before as well as after the existing interceptors
     */
    protected void extendInterceptors(List<Object> interceptors) {
    }

    /**
     * Detect beans of type {@link MappedInterceptor} and add them to the list of mapped interceptors.
     * <p>This is called in addition to any {@link MappedInterceptor}s that may have been provided
     * via {@link #setInterceptors}, by default adding all beans of type {@link MappedInterceptor}
     * from the current context and its ancestors. Subclasses can override and refine this policy.
     * @param mappedInterceptors an empty list to add {@link MappedInterceptor} instances to
     */
    protected void detectMappedInterceptors(List<HandlerInterceptor> mappedInterceptors) {
        mappedInterceptors.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        getApplicationContext(), MappedInterceptor.class, true, false).values());
    }

    /**
     * Initialize the specified interceptors, checking for {@link MappedInterceptor}s and
     * adapting {@link HandlerInterceptor}s  if necessary.
     * @see #setInterceptors
     * @see #adaptInterceptor
     */
    protected void initInterceptors() {
        if (!this.interceptors.isEmpty()) {
            for (int i = 0; i < this.interceptors.size(); i++) {
                Object interceptor = this.interceptors.get(i);
                if (interceptor == null) {
                    throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
                }
                this.adaptedInterceptors.add(adaptInterceptor(interceptor));
            }
        }
    }



    protected HandlerInterceptor adaptInterceptor(Object interceptor) {
        if (interceptor instanceof HandlerInterceptor) {
            return (HandlerInterceptor) interceptor;
        }
        else {
            throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
        }
    }



    @Override
    public final HandlerExecutionChain getHandler(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //QueryDecoder query = RequestContextHolder.getRequestQuery(ctx);
        QueryDecoder query = new QueryDecoder(request.uri());
        ctx.channel().attr(HandlerMapping.REQUEST_QUERY_URI).set(query);
        ctx.channel().attr(HandlerMapping.HTTP_REQUEST).set(request);

        Object handler = getHandlerInternal(ctx, request, query);
        if (handler == null) {
            handler = getDefaultHandler();
        }
        if (handler == null) {
            return null;
        }
        // Bean name or resolved handler?
        if (handler instanceof String) {
            String handlerName = (String) handler;
            handler = getApplicationContext().getBean(handlerName);
        }

        HandlerExecutionChain executionChain = getHandlerExecutionChain(handler, request, query);

        return executionChain;
    }


    protected abstract Object getHandlerInternal(ChannelHandlerContext ctx, FullHttpRequest request,
                                                 QueryDecoder query) throws Exception;


    /**
     * Build a {@link HandlerExecutionChain} for the given handler, including
     * applicable interceptors.
     * <p>The default implementation builds a standard {@link HandlerExecutionChain}
     * with the given handler, the handler mapping's common interceptors, and any
     * {@link MappedInterceptor}s matching to the current request URL. Interceptors
     * are added in the order they were registered. Subclasses may override this
     * in order to extend/rearrange the list of interceptors.
     * <p><b>NOTE:</b> The passed-in handler object may be a raw handler or a
     * pre-built {@link HandlerExecutionChain}. This method should handle those
     * two cases explicitly, either building a new {@link HandlerExecutionChain}
     * or extending the existing chain.
     * <p>For simply adding an interceptor in a custom subclass, consider calling
     * {@code super.getHandlerExecutionChain(handler, request)} and invoking
     * {@link HandlerExecutionChain#addInterceptor} on the returned chain object.
     *
     * @param handler the resolved handler instance (never {@code null})
     * @param request current HTTP request
     * @return the HandlerExecutionChain (never {@code null})
     */
    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, FullHttpRequest request,
                                                             QueryStringDecoder query) {
        HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ?
                (HandlerExecutionChain) handler : new HandlerExecutionChain(handler));

        String lookupPath = query.path();

        for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
            if (interceptor instanceof MappedInterceptor) {
                MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptor;
                if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
                    chain.addInterceptor(mappedInterceptor.getInterceptor());
                }
            } else {
                chain.addInterceptor(interceptor);
            }
        }
        return chain;
    }



    /**
     * Set the PathMatcher implementation to use for matching URL paths
     * against registered URL patterns. Default is AntPathMatcher.
     * @see org.springframework.util.AntPathMatcher
     */
    public void setPathMatcher(PathMatcher pathMatcher) {
        Assert.notNull(pathMatcher, "PathMatcher must not be null");
        this.pathMatcher = pathMatcher;
        //this.globalCorsConfigSource.setPathMatcher(pathMatcher);
    }

    /**
     * Return the PathMatcher implementation to use for matching URL paths
     * against registered URL patterns.
     */
    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    /**
     * Set the interceptors to apply for all handlers mapped by this handler mapping.
     * <p>Supported interceptor types are HandlerInterceptor, WebRequestInterceptor, and MappedInterceptor.
     * Mapped interceptors apply only to request URLs that match its path patterns.
     * Mapped interceptor beans are also detected by type during initialization.
     * @param interceptors array of handler interceptors
     * @see #adaptInterceptor
     */
    public void setInterceptors(Object... interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
    }

    /**
     * Specify the order value for this HandlerMapping bean.
     * <p>The default value is {@code Ordered.LOWEST_PRECEDENCE}, meaning non-ordered.
     * @see org.springframework.core.Ordered#getOrder()
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }


}