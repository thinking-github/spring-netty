package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-09
 */
@WebServlet(urlPatterns = "/servlet/user")
public class UserServlet extends HttpServlet {

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.println("nihao............init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //System.out.println("nihao ");
        long start = System.currentTimeMillis();
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
        //resp.getWriter().write(objectMapper.writeValueAsString(map));
        resp.getOutputStream().print(objectMapper.writeValueAsString(map));

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("nihao ");
    }
}
