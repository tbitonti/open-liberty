// 1.1, 2/19/03
// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import com.ibm.ws.cache.spi.DistributedMapFactory;
//import java.util.Properties;

//------------------------------------------------------------
//
//------------------------------------------------------------ 
public class DCPGrandParentServlet extends CacheTestCase {

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        out.println("This is the DCPGrandParent: <TABLE id=\"DCPGrandParent1\"><TR><TD>" + System.currentTimeMillis() + "</TD></TR></TABLE>");
        out.println("<BR/>");

        getServletContext().getRequestDispatcher("/DCPParent").include(request, response);

        out.println("This is the DCPGrandParent: <TABLE id=\"DCPGrandParent2\"><TR><TD>" + System.currentTimeMillis() + "</TD></TR></TABLE>");
        out.println("<BR/>");

    }

}
