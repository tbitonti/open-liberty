// 1.3, 6/2/03
// IBM Confidential OCO Source Material
// 5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import com.ibm.websphere.servlet.cache.*;
import com.ibm.websphere.cache.*;
import com.ibm.websphere.cache.DynamicCacheAccessor;
import java.io.*;

public class page3 extends page1 implements CacheableServlet  {


   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
      out.println("In page "+page+"<br>");
      out.println("<TABLE id=\"tracker\"><TR><TD>tracker = "
                  +(String) request.getAttribute("tracker")
                  +"</TD></TR></TABLE>");
      if (getId(request) == null)
         out.println("<TABLE id=\"Uncached TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
      else
         out.println("<TABLE id=\"Cached TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");

      if (DynamicCacheAccessor.isCachingEnabled()) {
         com.ibm.websphere.servlet.cache.FragmentInfo fragmentInfo = ((ServletCacheRequest) request).getFragmentInfo();

         fragmentInfo.addDataId("Deltron Zero");
         fragmentInfo.setTimeLimit(25);
      }

      out.println("setting attribute Caller to  \"Page 3\"");
      request.setAttribute("Caller", "Page 3");
      _include("page5", request, response);

      _include("page6", request, response);

   }

   public String getId(HttpServletRequest request) {
        if (request.getAttribute("no-cache") != null) return null;
        return this.getClass().getName();
    }


    public int getSharingPolicy(HttpServletRequest request) {
        return EntryInfo.NOT_SHARED;
    }


}
