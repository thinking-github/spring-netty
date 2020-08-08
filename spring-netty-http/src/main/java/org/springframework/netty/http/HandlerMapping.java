package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import org.springframework.netty.http.codec.QueryDecoder;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-14
 */
public interface HandlerMapping {

    AttributeKey<QueryDecoder> REQUEST_QUERY_URI = AttributeKey.valueOf("request_query_uri");
    AttributeKey<FullHttpRequest> HTTP_REQUEST = AttributeKey.valueOf("http_request");


    /**
     * 请求方法key 可用于打点
     */
    AttributeKey<String> REQUEST_HANDLER_METHOD_KEY = AttributeKey.valueOf("request_handler_method_key");


    /*
     * 请求调度开始时间戳
     */
    AttributeKey<Long> REQUEST_DISPATCH_START_TIME = AttributeKey.valueOf("request_dispatch_start_time");
    /**
     * 请求调度结束时间戳
     */
    AttributeKey<Long> REQUEST_DISPATCH_END_TIME = AttributeKey.valueOf("request_dispatch_end_time");



    /*
     * 请求处理开始时间戳
     */
    AttributeKey<Long> REQUEST_HANDLER_START_TIME = AttributeKey.valueOf("request_handler_start_time");
    /*
     * 请求处理结束时间戳
     */
    AttributeKey<Long> REQUEST_HANDLER_END_TIME = AttributeKey.valueOf("request_handler_end_time");


    /**
     * 请求处理时间
     */
    AttributeKey<Long> REQUEST_HANDLER_EXECUTE_TIME = AttributeKey.valueOf("request_handler_execute_time");


    String REQUEST_ID_HEADER_NAME = "x-request-id";


    HandlerExecutionChain getHandler(ChannelHandlerContext ctx, FullHttpRequest request)
            throws Exception;

}
