package org.springframework.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-04-23
 */
public abstract class JsonHttpServlet extends HttpServlet {

    @Autowired
    private ObjectMapper objectMapper;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp, Object o) throws ServletException, IOException {

    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp, Object o) throws ServletException, IOException {

    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp,Object o) throws ServletException, IOException {
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp, null);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }


}
