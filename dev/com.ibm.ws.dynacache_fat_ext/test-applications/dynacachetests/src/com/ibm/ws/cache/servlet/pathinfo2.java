package com.ibm.ws.cache.servlet;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;  

public class pathinfo2 extends pathinfo1 {

   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{

      out.println("<table id=\"pathinfo1\"><tr><td>In pathinfo"+pathinfo+"<BR></td><td>");

      out.println("In pathinfo "+pathinfo+"<br>");
      timeStamp("TimeStamp2");
      
      out.println("<TABLE id=\"pathinfo\"><TR><TD>path info = "
                  +request.getPathInfo()
                  +"</TD></TR></TABLE>");
      out.println("<TABLE id=\"_pathinfo\"><TR><TD>javax.servlet.include.path_info = "
                  +(String) request.getAttribute("javax.servlet.include.path_info")
                  +"</TD></TR></TABLE>");   
   }
}
