package com.ibm.ws.cache.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;  

public class page2 extends page1 {

   
   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
      out.println("In page "+page+"<br>");
      out.println("<TABLE id=\"tracker\"><TR><TD>tracker = "
                  +(String) request.getAttribute("tracker")
                  +"</TD></TR></TABLE>");
      out.println("<TABLE id=\"Uncached TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
        
      request.setAttribute("Caller", "Page 2");
      _include("page4", request, response);

   }
}
