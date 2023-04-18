// 1.3, 10/9/07
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class CacheUtilityServlet extends HttpServlet {
	private PrintWriter out;

	private com.ibm.ws.cache.intf.DCache cache;
	private String msg = "";

	//**
	//** init( ServletConfig config )
	//**
	public void init( ServletConfig config ) throws ServletException {
		super.init( config );
	}


	//**
	//** destroy()
	//**
	public void destroy() {
		super.destroy();
	}


	//**
	//** doGet( HttpServletRequest req, HttpServletResponse resp )
	//**
	public void doGet( HttpServletRequest req, HttpServletResponse resp )
	throws ServletException, IOException {
		processRequest( req, resp );
	}


	//**
	//** doPost( HttpServletRequest req, HttpServletResponse resp )
	//**
	public void doPost( HttpServletRequest req, HttpServletResponse resp )
	throws ServletException, IOException {
		processRequest( req, resp );
	}


	//**
	//** processRequest( HttpServletRequest req, HttpServletResponse resp )
	//**
	public void processRequest( HttpServletRequest req, HttpServletResponse resp )
	throws ServletException, IOException {

		out = resp.getWriter();
		resp.setContentType("text/html");
		out.println("<html><body>");
		msg = "";
		String cacheid = req.getParameter("cacheid");
		String depid = req.getParameter("depid");
		if (cacheid != null) {

			com.ibm.websphere.cache.Cache ca = com.ibm.websphere.cache.DynamicCacheAccessor.getCache();

			cache = (com.ibm.ws.cache.intf.DCache)ca;

			if ( cache == null ) {
				msg = "Error: Can't get an instance of cache";
			}

			//if (!(cache.getSwapToDisk())) {
			//	msg = "Error: Disk offLoad is not enabled";
			//}
			cacheid = cacheid.replaceAll("http:/", "http://");
			cacheid = cacheid.replaceAll("http:///", "http://");
			com.ibm.websphere.cache.CacheEntry ce = cache.getEntry(cacheid);
			if (ce != null) {
				Object value = ce.getValue();
				msg = "Data: " + value;
				if (depid == null) {
					Enumeration e = ce.getDataIds();
					while (e.hasMoreElements()) {
						msg = "Error: cacheid=" + cacheid + "\nFound depid:" + e.nextElement();
						break;
					}
				} else {
					Enumeration e = ce.getDataIds();
					while (e.hasMoreElements()) {
						String did= (String)e.nextElement();
						if (!(depid.equals(did))) {
							msg = "Error: cacheid=" + cacheid + "\nFound depid not match expected=" + depid + " but received=" + did;
							break;
						}
					}
				}
			} else {
				msg = "Error: cacheid=" + cacheid + "\ncache data is NULL";
			}

		} else {
			msg = "Error: No cacheid parameter";
		}

		out.println("<br>" + msg);

		out.println("</body></html>");
	}

	public String getStackTrace(Throwable oThrowable) {
		if (oThrowable == null)
			return null;
		StringWriter oStringWriter = new StringWriter();
		PrintWriter  oPrintWriter  = new PrintWriter(oStringWriter);
		oThrowable.printStackTrace(oPrintWriter);

		return oStringWriter.toString();
	}
}

