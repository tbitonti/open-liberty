package com.ibm.ws.cache.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class CrossWebApp3 extends CacheTestCaseA {

   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
      timeStamp("timeStamp3");
      table("context_path3",(String)request.getAttribute("javax.servlet.include.context_path"));
   }
}
