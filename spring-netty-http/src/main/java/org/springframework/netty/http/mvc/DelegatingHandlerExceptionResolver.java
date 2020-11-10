package org.springframework.netty.http.mvc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.netty.http.HandlerExceptionResolver;
import org.springframework.netty.http.HttpOutputMessage;
import org.springframework.netty.http.HttpResponseImpl;
import org.springframework.netty.http.ResponseEntity;
import org.springframework.netty.http.converter.HttpMessageConverter;

import java.util.Collections;
import java.util.List;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-20
 */
public class DelegatingHandlerExceptionResolver implements HandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(DelegatingHandlerExceptionResolver.class);
    private List<HandlerExceptionResolver> resolvers;

    private List<HttpMessageConverter<?>> messageConverters;

    private MessageConverterMethodProcessor messageConverterMethodProcessor;


    public DelegatingHandlerExceptionResolver() {

    }

    public DelegatingHandlerExceptionResolver(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
        this.messageConverterMethodProcessor = new MessageConverterMethodProcessor(messageConverters);
    }

    /**
     * Set the list of exception resolvers to delegate to.
     */
    public void setExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        this.resolvers = exceptionResolvers;
    }

    /**
     * Return the list of exception resolvers to delegate to.
     */
    public List<HandlerExceptionResolver> getExceptionResolvers() {
        return (this.resolvers != null ? Collections.unmodifiableList(this.resolvers) :
                Collections.<HandlerExceptionResolver>emptyList());
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


    /**
     * Resolve the exception by iterating over the list of configured exception resolvers.
     * The first one to return a ModelAndView instance wins. Otherwise {@code null} is returned.
     */
    @Override
    public Object resolveException(ChannelHandlerContext ctx, FullHttpRequest request, Object handler, Throwable ex) {
        if (resolvers != null) {
            for (HandlerExceptionResolver handlerExceptionResolver : this.resolvers) {
                Object mav = handlerExceptionResolver.resolveException(ctx, request, handler, ex);
                if (mav != null) {
                    return convertHttpResponse(mav, request);
                }
            }
        }

        return null;
    }


    private HttpResponse convertHttpResponse(Object response, FullHttpRequest request) {
        Object body;
        HttpResponseStatus status = null;
        if ((response instanceof HttpResponse)) {
            return (HttpResponse) response;
        } else if (response instanceof ResponseEntity) {
            ResponseEntity responseEntity = (ResponseEntity) response;
            body = responseEntity.getBody();
            status = responseEntity.getStatus();
        } else if (response instanceof org.springframework.http.ResponseEntity) {
            org.springframework.http.ResponseEntity responseEntity = (org.springframework.http.ResponseEntity) response;
            body = responseEntity.getBody();
            status = HttpResponseStatus.valueOf(responseEntity.getStatusCode().value());
        } else {
            body = response;
        }

        HttpOutputMessage outputMessage = new HttpResponseImpl(status);
        if (body == null) {
            return outputMessage;
        }
        try {
            messageConverterMethodProcessor.writeWithMessageConverters(body, request, outputMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return outputMessage;
    }


}
