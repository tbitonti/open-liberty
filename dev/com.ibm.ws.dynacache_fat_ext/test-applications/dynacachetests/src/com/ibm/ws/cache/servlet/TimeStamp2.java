// 1.1, 5/26/04
// IBM Confidential OCO Source Material
// 5724-i63, 5724-H88 (C) COPYRIGHT International Business Machines Corp. 1997, 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class TimeStamp2 extends TimeStamp {

   public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
      out.println("<TABLE id=\"TimeStamp2\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
   }
}
