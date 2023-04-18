package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CrossWebApp4 extends CacheTestCaseB {

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        timeStamp("timeStamp4");
    }
}
