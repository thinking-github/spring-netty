package org.springframework.netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
@Controller
@RequestMapping("/servlet/user")
public class HelloRequestController implements HttpRequestHandler {

    public HelloRequestController() {
        System.out.println("HelloRequest.........");
    }

    @Override
    public Object handleRequest(ChannelHandlerContext ctx, FullHttpRequest request, Object input)
            throws IOException {

        //System.out.println(1/0);
        Map<String, String> map = new HashMap<>();
        map.put("id", "10");
        map.put("name", "thinking");
        map.put("nickname", "thinking");
        map.put("age", "19");
        map.put("sex", "1");
        map.put("icon", "thinking");
        map.put("thinking1", "thinking1");
        map.put("thinking2", "thinking2");
        map.put("thinking3", "thinking3");
        map.put("thinking4", "thinking4");
        map.put("thinking5", "thinking5");
        map.put("thinking6", "thinking6");
        map.put("thinking7", "thinking7");
        map.put("thinking8", "thinking8");
        map.put("thinking9", "thinking9");

        return map;
    }
}
