package com.ibm.ws.cache.servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class reqparmlisttest extends HttpServlet {

   public void service(HttpServletRequest request, HttpServletResponse response) {

      PrintWriter out = null;
      try {
         out = response.getWriter();
      } catch (Exception e) {e.printStackTrace();}
      response.setContentType("text/html");

      out.println("<HTML><BODY>");
      out.println("<TABLE id=\"TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
      out.println("</BODY></HTML>");
   }

}
