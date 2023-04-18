package com.ibm.ws.cache.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;  

public class page1 extends CacheTestCase {

   protected int page = 0;
   protected static String[] colors = {
                         "AAFFEE",
                         "AAAAFF",
                         "AABBAA",
                         "AACCBB",
                         "AADDCC",
                         "AAEEDD",
                         "AAEEAA",
                         "AAFFBB",
                         "AAAACC",
                         "AABBDD",
                         "AACCEE",
                         "AADDFF"};

   public void init(ServletConfig c) throws ServletException {            
      super.init(c);
      page = Integer.parseInt(getClass().getName().substring("com.ibm.ws.cache.servlet.page".length()));
   }

   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
      String cache = request.getParameter("cache");
      if (cache != null && cache.equals("no")) {
         request.setAttribute("no-cache", "no");
      }

      out.println("<table id=\"page1\"><tr><td>In page"+page+"<BR></td><td>");

      out.println("In page "+page+"<br>");
      timeStamp("Uncached TimeStamp");
      request.setAttribute("tracker", getClass().getName());
      out.println("<TABLE id=\"tracker\"><TR><TD>tracker = "
                  +(String) request.getAttribute("tracker")
                  +"</TD></TR></TABLE>");
        
      _include("page3", request, response);

      request.setAttribute("Caller", "Page 1");
      _include("page4", request, response);

      _include("page2", request, response);

      out.println("</td><td>Back In page"+page+"</td></tr></table>");
   }

   public void _include(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      
      out.println("<BR><table id=\""+target+"\"  BGCOLOR=\"#"+colors[page]+"\"><tr><td>In page"+page+", calling "+target+"<BR>"
                  +"</td>");
      out.println("<td>");
      getServletContext().getRequestDispatcher("/servlet/"+target).include(request,response);
      out.println("</td><td>In page"+page+", back from "+target+"<BR>"
                  +"</td></tr></table>");
   }


}
