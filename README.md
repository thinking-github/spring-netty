# spring-netty
netty http server  netty web mvc

启用 netty http server java config
```java
@Configuration
@EnableNettyWeb
public class NettyHttpConfig implements HttpServerConfigurer, NettyWebConfigurer {
    
    //支持配置netty TCP 参数设置
    @Override
    public void configure(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
    }


     //支持用户自定义拦截器全局业务处理 LogIdInterceptor /reporterInterceptor
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry.addInterceptor(new LogIdInterceptor());
        //registry.addInterceptor(reporterInterceptor());
    }

    //http 消息转换器 默认已添加JSON 处理器，可不设置
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

    }

     //http 异常处理，可以定义异常处理
    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {

    }

    //可以为空，如果为空，默认使用框架自带的线程池，同时支持线程大小等参数设置
    @Override
    public ExecutorService getThreadPoolExecutor() {
        return null;
    }

    //可以为空，如果为空，默认使用spring容器启动时自带的BeanValidator验证
    @Override
    public Validator getValidator() {
        return null;
    }
}


```


web http request handler controller

//复用spring 注解 

//org.springframework.stereotype.Controller
//org.springframework.web.bind.annotation.RequestMapping

```java

@Controller
@RequestMapping(value = "/v1/cfg", produces = MediaType.APPLICATION_JSON_VALUE)
public class AbRequestController implements HttpRequestHandler<ClientRequest> {

    @Resource
    private AbTestService abTestService;


    @Override
    public Object handleRequest(ChannelHandlerContext ctx, FullHttpRequest request,
                                ClientRequest inputBody) throws IOException {
        // 业务处理方法
        List<?> exps = abTestService.getExpConfig(inputBody);



        //monitor report key name
        ctx.channel().attr(REQUEST_HANDLER_METHOD_KEY).set(MetricConst.ABTEST_GET_EXP_CONFIG);

        return ApiResponse.success(exps);
    }
    
      @Data
        public static class ClientRequest{
           private String clientId;
           private String osId;
           private String locale;
           private int  mode;
           private string packageName;
        }
}

```

请求测试API 接口
```
curl --location --request POST 'http://127.0.0.1:9090/v1/cfg' \
--header 'Content-Type: application/json' \
--data '{
    "clientId": "qe3tb13j6c1202",
    "osId": "1",
    "locale": null,
    "mode": 1,
    "packageName": "com.example.browser"
}'
```