package org.springframework.netty.http.handler;

import org.springframework.beans.BeansException;
import org.springframework.util.CollectionUtils;

import javax.servlet.annotation.WebServlet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 */
public class SimpleUrlHandlerMapping extends AbstractUrlHandlerMapping {

    private final Map<String, Object> urlMap = new LinkedHashMap<String, Object>();

    /**
     * Map URL paths to handler bean names.
     * This is the typical way of configuring this HandlerMapping.
     * <p>Supports direct URL matches and Ant-style pattern matches. For syntax
     * details, see the {@link org.springframework.util.AntPathMatcher} javadoc.
     * @param mappings properties with URLs as keys and bean names as values
     * @see #setUrlMap
     */
    public void setMappings(Properties mappings) {
        CollectionUtils.mergePropertiesIntoMap(mappings, this.urlMap);
    }


    /**
     * Set a Map with URL paths as keys and handler beans (or handler bean names)
     * as values. Convenient for population with bean references.
     * <p>Supports direct URL matches and Ant-style pattern matches. For syntax
     * details, see the {@link org.springframework.util.AntPathMatcher} javadoc.
     * @param urlMap map with URLs as keys and beans as values
     * @see #setMappings
     */
    public void setUrlMap(Map<String, ?> urlMap) {
        this.urlMap.putAll(urlMap);
    }

    /**
     * Allow Map access to the URL path mappings, with the option to add or
     * override specific entries.
     * <p>Useful for specifying entries directly, for example via "urlMap[myKey]".
     * This is particularly useful for adding or overriding entries in child
     * bean definitions.
     */
    public Map<String, ?> getUrlMap() {
        return this.urlMap;
    }



    @Override
    public void initApplicationContext() throws BeansException {
        super.initApplicationContext();
        registerHandlers(this.urlMap);


    }

    /**
     * Register all handlers specified in the URL map for the corresponding paths.
     *
     * @param urlMap Map with URL paths as keys and handler beans or bean names as values
     * @throws BeansException             if a handler couldn't be registered
     * @throws IllegalStateException if there is a conflicting handler registered
     */
    protected void registerHandlers(Map<String, Object> urlMap) throws BeansException {
        if (urlMap.isEmpty()) {
            logger.warn("Neither 'urlMap' nor 'mappings' set on SimpleUrlHandlerMapping");
        } else {
            for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
                String url = entry.getKey();
                Object handler = entry.getValue();
                // Prepend with slash if not already present.
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
                // Remove whitespace from handler bean name.
                if (handler instanceof String) {
                    handler = ((String) handler).trim();
                }
                registerHandler(url, handler);
            }
        }
    }

    @Override
    public void registerHandler(String[] urlPaths, Object handler) throws IllegalStateException {
        super.registerHandler(urlPaths, handler);
    }
}
