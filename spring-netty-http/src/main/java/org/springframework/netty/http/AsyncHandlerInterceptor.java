package org.springframework.netty.http;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 */
public interface AsyncHandlerInterceptor extends HandlerInterceptor {

    /**
     * Called instead of {@code postHandle} and {@code afterCompletion}, when
     * the a handler is being executed concurrently.
     * <p>Implementations may use the provided request and response but should
     * avoid modifying them in ways that would conflict with the concurrent
     * execution of the handler. A typical use of this method would be to
     * clean up thread-local variables.
     *
     * @param request the current request
     * @param handler the handler that started async
     *                execution, for type and/or instance examination
     * @throws Exception in case of errors
     */
    void afterConcurrentHandlingStarted(FullHttpRequest request, Object handler)
            throws Exception;

}
