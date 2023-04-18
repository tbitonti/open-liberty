package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CrossWebApp1 extends CacheTestCaseA {

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        timeStamp("timeStamp1");
        table("context_path1", request.getContextPath());

        String uri = "/CrossWebApp2";
        String cache = request.getParameter("cache2");
        if (cache != null && cache.equals("yes")) {
            uri += "?cache=yes";
        }
        getServletContext().getContext("/dynacachetests/moduleB").getRequestDispatcher(uri).include(request, response);
    }
}
