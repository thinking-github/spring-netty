package org.springframework.netty.http.support;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.netty.http.HandlerExceptionResolver;
import org.springframework.netty.http.HandlerMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-20
 */
public class DefaultHandlerExceptionResolver implements HandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHandlerExceptionResolver.class);

    private static int ERROR_CODE = 0;
    private static int initialized = 0;


    @Override
    public Object resolveException(ChannelHandlerContext ctx, FullHttpRequest request, Object handler, Throwable ex) {
        logger.error(ex.getMessage() + " URI: " + request.uri(), ex);

        Map<String, Object> result = new HashMap<>();
        result.put("logId", request.headers().get(HandlerMapping.REQUEST_ID_HEADER_NAME));
        result.put("code", ERROR_CODE);
        result.put("message", ex.getMessage());
        result.put("exception", ex.getClass().getSimpleName());
        result.put("timestamp", System.currentTimeMillis());

       /* if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {

        } else {

        }*/
        return result;
    }


    /**
     * initialize success and  error code
     *
     * @param errorCode
     */
    public final static void initialize(int errorCode) {
        if (initialized >= 1) {
            throw new IllegalStateException("success and error code initialized");
        }
        ERROR_CODE = errorCode;
        initialized++;
    }


}
