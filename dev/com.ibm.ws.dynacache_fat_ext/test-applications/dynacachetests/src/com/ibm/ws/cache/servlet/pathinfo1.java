package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class pathinfo1 extends CacheTestCase {

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
        pathinfo = Integer.parseInt(getClass().getName().substring("com.ibm.ws.cache.servlet.pathinfo".length()));
    }

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String cache = request.getParameter("cache");
        if (cache != null && cache.equals("no")) {
            request.setAttribute("no-cache", "no");
        }

        out.println("<table id=\"pathinfo1\"><tr><td>In pathinfo" + pathinfo + "<BR></td><td>");

        out.println("In pathinfo " + pathinfo + "<br>");
        timeStamp("TimeStamp1");

        out.println("<TABLE id=\"pathinfo\"><TR><TD>path info = "
                    + request.getPathInfo()
                    + "</TD></TR></TABLE>");
        out.println("<TABLE id=\"_pathinfo\"><TR><TD>javax.servlet.include.path_info = "
                    + (String) request.getAttribute("javax.servlet.include.path_info")
                    + "</TD></TR></TABLE>");
        _include("pathinfo2", request, response);

        out.println("</td><td>Back In pathinfo" + pathinfo + "</td></tr></table>");
    }

    public void _include(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = target;
        if (request.getParameter("genPathInfo") != null) {
            uri += "/" + System.currentTimeMillis();
        }

        out.println("<BR><table id=\"" + target + "\"  BGCOLOR=\"#" + colors[pathinfo] + "\"><tr><td>In pathinfo" + pathinfo + ", calling " + uri + "<BR>"
                    + "</td>");
        out.println("<td>");
        getServletContext().getRequestDispatcher(uri).include(request, response);
        out.println("</td><td>In pathinfo" + pathinfo + ", back from " + uri + "<BR>"
                    + "</td></tr></table>");
    }
}
