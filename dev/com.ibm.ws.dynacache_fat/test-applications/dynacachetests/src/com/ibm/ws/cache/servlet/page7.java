package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.EntryInfo;
import com.ibm.websphere.servlet.cache.CacheableServlet;



public class page7 extends page1 implements CacheableServlet  {


   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
      out.println("In page "+page+"<br>");
      out.println("<TABLE id=\"tracker\"><TR><TD>tracker = "
                  +(String) request.getAttribute("tracker")
                  +"</TD></TR></TABLE>");
      out.println("<BR>set tracker to "+getClass().getName());
      request.setAttribute("tracker", getClass().getName());
      if (getId(request) == null)
         out.println("<TABLE id=\"Uncached TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
      else
         out.println("<TABLE id=\"Cached TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");

   }

   public String getId(HttpServletRequest request) {
      if (request.getAttribute("no-cache") != null) return null;
      return this.getClass().getName();
    }


    public int getSharingPolicy(HttpServletRequest request) {
        return EntryInfo.NOT_SHARED;
    }


}
