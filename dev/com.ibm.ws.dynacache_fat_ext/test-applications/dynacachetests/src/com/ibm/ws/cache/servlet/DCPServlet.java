// 1.1, 2/19/03
// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.servlet.cache.DynamicContentProvider;
import com.ibm.websphere.servlet.cache.ServletCacheResponse;

//import com.ibm.ws.cache.spi.DistributedMapFactory;
//import java.util.Properties;

//------------------------------------------------------------
//
//------------------------------------------------------------ 
public class DCPServlet extends CacheTestCase {

	public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		out.println("This is the DCP: <TABLE id=\"DCP1\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
		out.println("<BR/>");

		DynamicContentProvider dcp = new DynamicContentProviderImpl();
		ServletCacheResponse scr = null;
		//scr = (ServletCacheResponse)((HttpServletResponseWrapper) response);
		scr = (ServletCacheResponse)(response);
		scr.addDynamicContentProvider(dcp);

		out.println("This is the DCP: <TABLE id=\"DCP2\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
		out.println("<BR/>");

	}

	class DynamicContentProviderImpl implements DynamicContentProvider {

		DynamicContentProviderImpl() {
		}

		public void provideDynamicContent(HttpServletRequest request, OutputStream streamWriter) throws IOException {
			
			String dynamicContent = "<BR/><B>dynamic content </B> <TABLE id=\"DynamicContent\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>";
			streamWriter.write(dynamicContent.getBytes());
		}

		public void provideDynamicContent(HttpServletRequest request, Writer streamWriter) throws IOException {
			
			String dynamicContent = "<BR/><B>dynamic content </B> <TABLE id=\"DynamicContent\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>";
			streamWriter.write(dynamicContent.toCharArray());
		}
	}

}
