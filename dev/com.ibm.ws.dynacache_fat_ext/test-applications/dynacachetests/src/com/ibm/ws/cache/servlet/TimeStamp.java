package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TimeStamp extends CacheTestCase {

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        out.println("<TABLE id=\"TimeStamp\"><TR><TD>" + System.currentTimeMillis() + "</TD></TR></TABLE>");
    }
}
