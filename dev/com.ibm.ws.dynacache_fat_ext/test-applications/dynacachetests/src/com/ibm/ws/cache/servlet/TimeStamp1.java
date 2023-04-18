package com.ibm.ws.cache.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class TimeStamp1 extends TimeStamp {

   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
      out.println("<TABLE id=\"TimeStamp1\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
   }
}
