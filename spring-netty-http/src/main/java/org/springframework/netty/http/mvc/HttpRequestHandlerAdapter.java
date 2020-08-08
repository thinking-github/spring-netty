package org.springframework.netty.http.mvc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.netty.http.HandlerAdapter;
import org.springframework.netty.http.HttpOutputMessage;
import org.springframework.netty.http.HttpRequestHandler;
import org.springframework.netty.http.HttpResponseImpl;
import org.springframework.netty.http.converter.HttpMessageConverter;
import org.springframework.netty.http.support.GenericsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.netty.http.HandlerMapping.*;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 */
public class HttpRequestHandlerAdapter implements HandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandlerAdapter.class);
    private List<HttpMessageConverter<?>> messageConverters;

    private MessageConverterMethodProcessor messageConverterMethodProcessor;

    private Map<Class, Class> findCache = new HashMap<Class, Class>();


    public HttpRequestHandlerAdapter() {

    }


    public HttpRequestHandlerAdapter(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
        this.messageConverterMethodProcessor = new MessageConverterMethodProcessor(messageConverters);
    }

    /**
     * Provide the converters to use in argument resolvers and return value
     * handlers that support reading and/or writing to the body of the
     * request and response.
     */
    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
        this.messageConverterMethodProcessor = new MessageConverterMethodProcessor(messageConverters);
    }

    /**
     * Return the configured message body converters.
     */
    public List<HttpMessageConverter<?>> getMessageConverters() {
        return this.messageConverters;
    }


    @Override
    public boolean supports(Object handler) {
        return (handler instanceof HttpRequestHandler);
    }

    @Override
    public Object handle(ChannelHandlerContext ctx, FullHttpRequest request, Object handler)
            throws Exception {
        ByteBuf contentByte = request.content();
        Object inputBody = null;
        if (contentByte.readableBytes() > 0) {
            Class inputClass = findCache.get(handler.getClass());
            if (inputClass == null) {
                inputClass = GenericsUtils.getInterfaceGenericType(handler.getClass(), 0, 0);
                findCache.put(handler.getClass(), inputClass);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("HttpRequest Body : {}", contentByte.toString(CharsetUtil.UTF_8));
            }
            inputBody = messageConverterMethodProcessor.readWithMessageConverters(request, inputClass);
        }


        long startTime = System.currentTimeMillis();
        ctx.channel().attr(REQUEST_HANDLER_START_TIME).set(startTime);

        Object response = ((HttpRequestHandler) handler).handleRequest(ctx, request, inputBody);

        long endTime = System.currentTimeMillis();
        ctx.channel().attr(REQUEST_HANDLER_EXECUTE_TIME).set(endTime - startTime);
        ctx.channel().attr(REQUEST_HANDLER_END_TIME).set(endTime);

        if ((response instanceof HttpResponse)) {
            return response;
        }

        HttpOutputMessage outputMessage = new HttpResponseImpl();
        messageConverterMethodProcessor.writeWithMessageConverters(response, request, outputMessage);
        return outputMessage;
    }


    @Override
    public long getLastModified(FullHttpRequest request, Object handler) {
        return -1L;
    }


}
