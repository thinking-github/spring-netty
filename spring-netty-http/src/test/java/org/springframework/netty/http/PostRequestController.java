package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
@Controller
@RequestMapping("/servlet/user1")
public class PostRequestController implements HttpRequestHandler<BaseInfo> {


    @Override
    public Object handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, BaseInfo inputBody) throws IOException {
        return inputBody;
    }
}
