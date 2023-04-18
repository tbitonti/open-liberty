package com.ibm.ws.cache.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SaveAttributeParentServlet extends CacheTestCase {

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        try {

            request.setAttribute("att1", new Long(System.currentTimeMillis()));
            java.lang.Thread.sleep(5);
            request.setAttribute("att2", new Long(System.currentTimeMillis()));
            java.lang.Thread.sleep(5);
            request.setAttribute("att3", new Long(System.currentTimeMillis()));
            getServletContext().getRequestDispatcher("SavAttrChild").include(request, response);
        } catch (Exception ex) {
        }
    }

}
