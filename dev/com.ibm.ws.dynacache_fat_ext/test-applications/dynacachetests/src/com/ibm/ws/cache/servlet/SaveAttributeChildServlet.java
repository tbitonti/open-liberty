package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SaveAttributeChildServlet extends CacheTestCase {

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        out.println("<TABLE id=\"att1\"><TR><TD>" + request.getAttribute("att1") + "</TD></TR></TABLE>");

        out.println("<TABLE id=\"att2\"><TR><TD>" + request.getAttribute("att2") + "</TD></TR></TABLE>");
        out.println("<TABLE id=\"att3\"><TR><TD>" + request.getAttribute("att3") + "</TD></TR></TABLE>");

    }

}
