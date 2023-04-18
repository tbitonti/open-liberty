package com.ibm.ws.cache.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public abstract class CacheTestCaseA extends HttpServlet {

   public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      pre(response);
      performTest(request, response);
      post();
   }

   public abstract void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;

   public void timeStamp(String id) {
      table(id, String.valueOf(System.currentTimeMillis()));
   }

   public void table(String id, String cell) {
      out.println("<TABLE id=\""+id+"\"><TR><TD>"+cell+"</TD></TR></TABLE>");
   }

   protected PrintWriter out  = null;

   protected void pre(HttpServletResponse resp) throws IOException {
      resp.setContentType("text/html");
      out = resp.getWriter();
      out.println("<HTML><HEAD><TITLE>CacheTest</TITLE></HEAD><BODY>");
   }

   protected void post() {
      out.println("</BODY></HTML>");
   }

}
