package com.asyncsample.api;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        if (code != null)
            response.setStatus(Integer.parseInt(code));

        HttpSession session = request.getSession(true);
        if (session.isNew())
            response.addCookie(new Cookie("bigcookie", "This is a test cookies that was created on " + new Date() + " and is used by the jetty http/2 test servlet."));
        response.setHeader("Custom", "Value");
        response.setContentType("text/plain");
        String content = "Hello from Jetty using " + request.getProtocol() + "\n";
        content += "uri=" + request.getRequestURI() + "\n";
        content += "session=" + session.getId() + (session.isNew() ? "(New)\n" : "\n");
        content += "date=" + new Date() + "\n";

        // for (Cookie c : request.getCookies())
        // content+="cookie "+c.getName()+"="+c.getValue()+"\n";

        response.setContentLength(content.length());
        response.getOutputStream().print(content);
    }
};
