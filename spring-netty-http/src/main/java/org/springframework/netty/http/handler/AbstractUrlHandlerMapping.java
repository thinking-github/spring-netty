package org.springframework.netty.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;
import org.springframework.netty.http.HandlerMapping;
import org.springframework.netty.http.codec.QueryDecoder;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 * <p>
 * org.springframework.web.servlet.handler.AbstractUrlHandlerMapping
 */
public abstract class AbstractUrlHandlerMapping extends AbstractHandlerMapping implements HandlerMapping, Ordered {

    private Object rootHandler;

    private boolean useTrailingSlashMatch = false;

    private boolean lazyInitHandlers = false;

    private final Map<String, Object> handlerMap = new LinkedHashMap<String, Object>();

    /**
     * Set the root handler for this handler mapping, that is,
     * the handler to be registered for the root path ("/").
     * <p>Default is {@code null}, indicating no root handler.
     */
    public void setRootHandler(Object rootHandler) {
        this.rootHandler = rootHandler;
    }

    /**
     * Return the root handler for this handler mapping (registered for "/"),
     * or {@code null} if none.
     */
    public Object getRootHandler() {
        return this.rootHandler;
    }

    /**
     * Whether to match to URLs irrespective of the presence of a trailing slash.
     * If enabled a URL pattern such as "/users" also matches to "/users/".
     * <p>The default value is {@code false}.
     */
    public void setUseTrailingSlashMatch(boolean useTrailingSlashMatch) {
        this.useTrailingSlashMatch = useTrailingSlashMatch;
    }

    /**
     * Whether to match to URLs irrespective of the presence of a trailing slash.
     */
    public boolean useTrailingSlashMatch() {
        return this.useTrailingSlashMatch;
    }

    /**
     * Set whether to lazily initialize handlers. Only applicable to
     * singleton handlers, as prototypes are always lazily initialized.
     * Default is "false", as eager initialization allows for more efficiency
     * through referencing the controller objects directly.
     * <p>If you want to allow your controllers to be lazily initialized,
     * make them "lazy-init" and set this flag to true. Just making them
     * "lazy-init" will not work, as they are initialized through the
     * references from the handler mapping in this case.
     */
    public void setLazyInitHandlers(boolean lazyInitHandlers) {
        this.lazyInitHandlers = lazyInitHandlers;
    }


    @Override
    protected Object getHandlerInternal(final ChannelHandlerContext ctx, FullHttpRequest request,
                                        QueryDecoder query) throws Exception {

        String lookupPath = query.path();
        Object handler = lookupHandler(lookupPath, request,query);
        if (handler == null) {
            // We need to care for the default handler directly, since we need to
            // expose the PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE for it as well.
            Object rawHandler = null;
            if ("/".equals(lookupPath)) {
                rawHandler = getRootHandler();
            }
            if (rawHandler == null) {
                rawHandler = getDefaultHandler();
            }
        }
        if (handler != null && logger.isDebugEnabled()) {
            logger.debug("Mapping [" + lookupPath + "] to " + handler);
        } else if (handler == null && logger.isTraceEnabled()) {
            logger.trace("No handler mapping found for [" + lookupPath + "]");
        }
        return handler;
    }


    /**
     * Look up a handler instance for the given URL path.
     * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
     * and various Ant-style pattern matches, e.g. a registered "/t*" matches
     * both "/test" and "/team". For details, see the AntPathMatcher class.
     * <p>Looks for the most exact pattern, where most exact is defined as
     * the longest path pattern.
     *
     * @param urlPath URL the bean is mapped to
     * @param request current HTTP request (to expose the path within the mapping to)
     * @return the associated handler instance, or {@code null} if not found
     * @see org.springframework.util.AntPathMatcher
     */
    protected Object lookupHandler(String urlPath, FullHttpRequest request,QueryDecoder query) throws Exception {
        // Direct match?
        Object handler = this.handlerMap.get(urlPath);
        if (handler != null) {
            return handler;
        }

        // Pattern match?
        List<String> matchingPatterns = new ArrayList<String>();
        for (String registeredPattern : this.handlerMap.keySet()) {
            if (getPathMatcher().match(registeredPattern, urlPath)) {
                matchingPatterns.add(registeredPattern);
            }
            else if (useTrailingSlashMatch()) {
                if (!registeredPattern.endsWith("/") && getPathMatcher().match(registeredPattern + "/", urlPath)) {
                    matchingPatterns.add(registeredPattern + "/");
                }
            }
        }

        String bestMatch = null;
        Comparator<String> patternComparator = getPathMatcher().getPatternComparator(urlPath);
        if (!matchingPatterns.isEmpty()) {
            Collections.sort(matchingPatterns, patternComparator);
            if (logger.isDebugEnabled()) {
                logger.debug("Matching patterns for request [" + urlPath + "] are " + matchingPatterns);
            }
            bestMatch = matchingPatterns.get(0);
        }
        if (bestMatch != null) {
            handler = this.handlerMap.get(bestMatch);
            if (handler == null) {
                if (bestMatch.endsWith("/")) {
                    handler = this.handlerMap.get(bestMatch.substring(0, bestMatch.length() - 1));
                }
                if (handler == null) {
                    throw new IllegalStateException(
                            "Could not find handler for best pattern match [" + bestMatch + "]");
                }
            }
            // Bean name or resolved handler?

            // There might be multiple 'best patterns', let's make sure we have the correct URI template variables
            // for all of them
            //Map<String, String> uriTemplateVariables = new LinkedHashMap<String, String>();
            Map<String, String> uriTemplateVariables = null;
            for (String matchingPattern : matchingPatterns) {
                if (patternComparator.compare(bestMatch, matchingPattern) == 0) {
                    Map<String, String> vars = getPathMatcher().extractUriTemplateVariables(matchingPattern, urlPath);

                    uriTemplateVariables = vars;
                    query.setUriTemplateVariables(uriTemplateVariables);

                    //Map<String, String> decodedVars = getUrlPathHelper().decodePathVariables(request, vars);
                    //uriTemplateVariables.putAll(decodedVars);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("URI Template variables for request [" + urlPath + "] are " + uriTemplateVariables);
            }


            return handler;
        }

        // No handler found...
        return null;
    }


    /**
     * Register the specified handler for the given URL paths.
     *
     * @param urlPaths the URLs that the bean should be mapped to
     * @param beanName the name of the handler bean
     * @throws BeansException        if the handler couldn't be registered
     * @throws IllegalStateException if there is a conflicting handler registered
     */
    protected void registerHandler(String[] urlPaths, String beanName) throws BeansException, IllegalStateException {
        Assert.notNull(urlPaths, "URL path array must not be null");
        for (String urlPath : urlPaths) {
            registerHandler(urlPath, beanName);
        }
    }

    /**
     * Register the specified handler for the given URL path.
     *
     * @param urlPath the URL the bean should be mapped to
     * @param handler the handler instance or handler bean name String
     *                (a bean name will automatically be resolved into the corresponding handler bean)
     * @throws Exception             if the handler couldn't be registered
     * @throws IllegalStateException if there is a conflicting handler registered
     */
    protected void registerHandler(String urlPath, Object handler) throws BeansException, IllegalStateException {
        Assert.notNull(urlPath, "URL path must not be null");
        Assert.notNull(handler, "Handler object must not be null");
        Object resolvedHandler = handler;

        // Eagerly resolve handler if referencing singleton via name.
        if (!this.lazyInitHandlers && handler instanceof String) {
            String handlerName = (String) handler;
            if (getApplicationContext().isSingleton(handlerName)) {
                resolvedHandler = getApplicationContext().getBean(handlerName);
            }
        }

        Object mappedHandler = this.handlerMap.get(urlPath);
        if (mappedHandler != null) {
            if (mappedHandler != resolvedHandler) {
                throw new IllegalStateException(
                        "Cannot map " + getHandlerDescription(handler) + " to URL path [" + urlPath +
                                "]: There is already " + getHandlerDescription(mappedHandler) + " mapped.");
            }
        } else {
            if (urlPath.equals("/")) {
                if (logger.isInfoEnabled()) {
                    logger.info("Root mapping to " + getHandlerDescription(handler));
                }
                setRootHandler(resolvedHandler);
            } else if (urlPath.equals("/*")) {
                if (logger.isInfoEnabled()) {
                    logger.info("Default mapping to " + getHandlerDescription(handler));
                }
                setDefaultHandler(resolvedHandler);
            } else {
                this.handlerMap.put(urlPath, resolvedHandler);
                if (logger.isInfoEnabled()) {
                    logger.info("Mapped URL path [" + urlPath + "] onto " + getHandlerDescription(handler));
                }
            }
        }
    }

    protected void registerHandler(Object handler, String... paths) throws Exception {
        if (ObjectUtils.isEmpty(paths)) {
            throw new IllegalArgumentException("URL path must not be null");
        }
        for (String path : paths) {
            registerHandler(path, handler);
        }
    }

    /**
     * Register the specified handler for the given URL paths.
     *
     * @param urlPaths the URLs that the bean should be mapped to
     * @param handler  handler object
     * @throws BeansException        if the handler couldn't be registered
     * @throws IllegalStateException if there is a conflicting handler registered
     */
    protected void registerHandler(String[] urlPaths, Object handler) throws IllegalStateException {
        Assert.notNull(urlPaths, "URL path array must not be null");
        for (String urlPath : urlPaths) {
            registerHandler(urlPath, handler);
        }
    }


    private String getHandlerDescription(Object handler) {
        return "handler " + (handler instanceof String ? "'" + handler + "'" : "of type [" + handler.getClass() + "]");
    }

}
