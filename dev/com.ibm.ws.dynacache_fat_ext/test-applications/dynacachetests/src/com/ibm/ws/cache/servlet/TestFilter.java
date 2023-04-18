package com.ibm.ws.cache.servlet;

import java.io.*;
import javax.servlet.*;

public class TestFilter implements Filter 
{
    public void init(FilterConfig fc)
    {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    {
       String action = request.getParameter("action");
       if (action != null && action.equals("filter")) {
          try {
              PrintWriter pw = response.getWriter();
              pw.println("...preprocessing...");
              chain.doFilter(request, response);
              pw.println("...postprocessing...");
          }
          catch (Exception e) {e.printStackTrace();}
       } else {
          try {
              chain.doFilter(request, response);
          }
          catch (Exception e) {e.printStackTrace();}
       }
    }

    public void destroy()
    {
    }
}