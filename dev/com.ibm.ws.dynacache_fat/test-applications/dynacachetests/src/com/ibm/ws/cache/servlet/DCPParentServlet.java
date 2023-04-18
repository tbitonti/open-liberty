// 1.1, 2/19/03
// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.servlet;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

//import com.ibm.ws.cache.spi.DistributedMapFactory;
//import java.util.Properties;

//------------------------------------------------------------
//
//------------------------------------------------------------ 
public class DCPParentServlet extends CacheTestCase {

	public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		out.println("This is the DCPParent: <TABLE id=\"DCPParent1\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
		out.println("<BR/>");
		
		getServletContext().getRequestDispatcher("/DCP").include(request,response);

		out.println("This is the DCPParent: <TABLE id=\"DCPParent2\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
		out.println("<BR/>");
		

	}

	

}
