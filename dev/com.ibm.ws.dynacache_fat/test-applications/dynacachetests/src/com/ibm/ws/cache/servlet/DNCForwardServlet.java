// 1.14, 2/4/04
// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

public class DNCForwardServlet extends HttpServlet {
	
	
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
	
	    String value = request.getParameter("test");
		getServletContext().getRequestDispatcher("/DNCGrandParent.jsp?test="+value).forward(request,response);
		
	}
	
}