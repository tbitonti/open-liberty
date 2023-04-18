// 1.14, 2/4/04
// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.http.*;

import com.ibm.websphere.command.CacheableCommand;
import com.ibm.websphere.command.CommandException;
import com.ibm.ws.cache.command.Quote;
import com.ibm.ws.cache.command.QuoteCommand;

public class PreviewChildServlet extends HttpServlet {
	
	
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("PreviewChildServlet");
		out.println("<TABLE id=\"ServletTimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
		CacheableCommand command = null;
        command = new QuoteCommand();
            
        try {
        	((Quote)command).setTicker("abc");
			command.execute();	
		} catch (CommandException e) {
			response.sendError(500,e.getMessage());
	        e.printStackTrace();
	        e.printStackTrace(out);
		}
		out.println("<TABLE id=\"CommandTimeStamp\"><TR><TD>" + ((Quote)command).getPrice() + "</TD></TR></TABLE>");
	}
	
}