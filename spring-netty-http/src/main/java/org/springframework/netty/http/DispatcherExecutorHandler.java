package org.springframework.netty.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-13
 */
@ChannelHandler.Sharable
public class DispatcherExecutorHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    //业务处理线程池
    //@Bean(destroyMethod = "shutdown")
/*    public static LifecycleExecutorService businessExecutor() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(200, 500,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(25000),
                new ThreadPoolExecutor.AbortPolicy());
        return new DefaultLifecycleExecutorService(threadPoolExecutor);
    }*/

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {


        /*        businessExecutor.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 创建http响应
                                        String msg = null;
                                        try {
                                            msg = objectMapper.writeValueAsString(result());
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }
                                        FullHttpResponse response = new DefaultFullHttpResponse(
                                                HttpVersion.HTTP_1_1,
                                                HttpResponseStatus.OK,
                                                Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));

                                        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/json; charset=UTF-8");
                                        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                    }
                                },
                new LogIdCallback(10000));*/

    }


}
