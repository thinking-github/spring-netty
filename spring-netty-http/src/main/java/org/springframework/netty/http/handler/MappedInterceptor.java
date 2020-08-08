package org.springframework.netty.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.springframework.netty.http.HandlerInterceptor;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 */
public class MappedInterceptor implements HandlerInterceptor {

    private final String[] includePatterns;

    private final String[] excludePatterns;

    private final HandlerInterceptor interceptor;

    private PathMatcher pathMatcher;


    /**
     * Create a new MappedInterceptor instance.
     *
     * @param includePatterns the path patterns to map (empty for matching to all paths)
     * @param interceptor     the HandlerInterceptor instance to map to the given patterns
     */
    public MappedInterceptor(String[] includePatterns, HandlerInterceptor interceptor) {
        this(includePatterns, null, interceptor);
    }

    /**
     * Create a new MappedInterceptor instance.
     *
     * @param includePatterns the path patterns to map (empty for matching to all paths)
     * @param excludePatterns the path patterns to exclude (empty for no specific excludes)
     * @param interceptor     the HandlerInterceptor instance to map to the given patterns
     */
    public MappedInterceptor(String[] includePatterns, String[] excludePatterns, HandlerInterceptor interceptor) {
        this.includePatterns = includePatterns;
        this.excludePatterns = excludePatterns;
        this.interceptor = interceptor;
    }


    /**
     * Configure a PathMatcher to use with this MappedInterceptor instead of the one passed
     * by default to the {@link #matches(String, org.springframework.util.PathMatcher)} method.
     * <p>This is an advanced property that is only required when using custom PathMatcher
     * implementations that support mapping metadata other than the Ant-style path patterns
     * supported by default.
     */
    public void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    /**
     * The configured PathMatcher, or {@code null} if none.
     */
    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    /**
     * The path into the application the interceptor is mapped to.
     */
    public String[] getPathPatterns() {
        return this.includePatterns;
    }

    /**
     * The actual {@link HandlerInterceptor} reference.
     */
    public HandlerInterceptor getInterceptor() {
        return this.interceptor;
    }


    /**
     * Determine a match for the given lookup path.
     *
     * @param lookupPath  the current request path
     * @param pathMatcher a path matcher for path pattern matching
     */
    public boolean matches(String lookupPath, PathMatcher pathMatcher) {
        PathMatcher pathMatcherToUse = (this.pathMatcher != null ? this.pathMatcher : pathMatcher);
        if (!ObjectUtils.isEmpty(this.excludePatterns)) {
            for (String pattern : this.excludePatterns) {
                if (pathMatcherToUse.match(pattern, lookupPath)) {
                    return false;
                }
            }
        }
        if (ObjectUtils.isEmpty(this.includePatterns)) {
            return true;
        }
        for (String pattern : this.includePatterns) {
            if (pathMatcherToUse.match(pattern, lookupPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preHandle(ChannelHandlerContext ctx, HttpRequest request, Object handler)
            throws Exception {

        return this.interceptor.preHandle(ctx, request, handler);
    }

    @Override
    public void postHandle(ChannelHandlerContext ctx, HttpRequest request, Object handler,Object result) throws Exception {

        this.interceptor.postHandle(ctx, request, handler,result);
    }

    @Override
    public void afterCompletion(ChannelHandlerContext ctx, HttpRequest request, Object handler,
                                Exception ex) throws Exception {

        this.interceptor.afterCompletion(ctx, request, handler, ex);
    }

}
