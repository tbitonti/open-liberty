/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WLP Copyright IBM Corp. 2014
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class Servlet31TestsServlet extends HttpServlet {
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String datalen = "65556";
        PrintWriter out = null;

        if (action.equals("setContentLengthLong")) {
            response.setContentType("text/html");
            out = response.getWriter();
            String pre = "<HTML><HEAD><TITLE>CacheTest</TITLE></HEAD><BODY>";
            String timestamp = "<TABLE id=TimeStamp><TR><TD>" + String.valueOf(System.currentTimeMillis()) + "</TD></TR></TABLE>";
            String post = "</BODY></HTML>";
            String output = pre + timestamp + post;
            long output_long = output.length();
            response.setContentLengthLong(output_long);
            out.println(output);

        } else {
            response.sendError(500, "Servlet31TestsServlet called with invalid action parm " + action);
        }
    }
}
