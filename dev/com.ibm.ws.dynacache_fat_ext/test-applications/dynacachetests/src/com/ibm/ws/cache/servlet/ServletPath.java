package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletPath extends CacheTestCase {

    protected int pathinfo = 0;
    protected static String[] colors = {
                                        "AAFFEE",
                                        "AAAAFF",
                                        "AABBAA",
                                        "AACCBB",
                                        "AADDCC",
                                        "AAEEDD",
                                        "AAEEAA",
                                        "AAFFBB",
                                        "AAAACC",
                                        "AABBDD",
                                        "AACCEE",
                                        "AADDFF" };

    @Override
    public void init(ServletConfig c) throws ServletException {
        super.init(c);

    }

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String cache = request.getParameter("cache");
        if (cache != null && cache.equals("no")) {
            request.setAttribute("no-cache", "no");
        }

        timeStamp("TimeStamp_ServletPath");

        out.println("<TABLE id=\"servletpath\"><TR><TD>servlet path  = "
                    + request.getServletPath()
                    + "</TD></TR></TABLE>");
        out.println("<TABLE id=\"_servletPath\"><TR><TD>javax.servlet.include.servlet_path = "
                    + (String) request.getAttribute("javax.servlet.include.servlet_path")
                    + "</TD></TR></TABLE>");
    }

}
