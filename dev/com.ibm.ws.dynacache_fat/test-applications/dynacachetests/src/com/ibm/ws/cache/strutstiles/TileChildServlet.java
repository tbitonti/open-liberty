package com.ibm.ws.cache.strutstiles;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import com.ibm.ws.cache.servlet.CacheTestCase;

public class TileChildServlet extends CacheTestCase {

   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
      
      out.println("<TABLE id=\"TimeStampServlet\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
   }
}


