package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CrossWebApp2 extends CacheTestCaseB {

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        timeStamp("timeStamp2");
        table("context_path2", (String) request.getAttribute("javax.servlet.include.context_path"));
        getServletContext().getContext("/dynacachetests/moduleA").getRequestDispatcher("/CrossWebApp3").include(request, response);
    }
}
