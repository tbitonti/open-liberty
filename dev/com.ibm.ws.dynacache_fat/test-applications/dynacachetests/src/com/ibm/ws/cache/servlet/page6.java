package com.ibm.ws.cache.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import com.ibm.websphere.servlet.cache.*;
import com.ibm.websphere.cache.*;
import com.ibm.websphere.cache.DynamicCacheAccessor;



public class page6 extends page1 implements CacheableServlet  {


   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
      out.println("In page "+page+"<br>");
      out.println("<TABLE id=\"tracker\"><TR><TD>tracker = "
                  +(String) request.getAttribute("tracker")
                  +"</TD></TR></TABLE>");
      if (getId(request) == null)
         out.println("<TABLE id=\"Uncached TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
      else
         out.println("<TABLE id=\"Cached TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
      request.setAttribute("tracker", null);

      if (DynamicCacheAccessor.isCachingEnabled()) {
         com.ibm.websphere.servlet.cache.FragmentInfo fragmentInfo = ((ServletCacheRequest) request).getFragmentInfo();

         fragmentInfo.addDataId("White Stripes");
      }

   }

   public String getId(HttpServletRequest request) {
      if (request.getAttribute("no-cache") != null) return null;
      return this.getClass().getName();
    }


    public int getSharingPolicy(HttpServletRequest request) {
        return EntryInfo.NOT_SHARED;
    }


}
