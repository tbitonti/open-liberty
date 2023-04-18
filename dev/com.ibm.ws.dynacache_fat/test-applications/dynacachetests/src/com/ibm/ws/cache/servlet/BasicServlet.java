package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BasicServlet extends CacheTestCase {

    @Override
    public void performTest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //Tamera's code - added nonser parameter to test caching non-serializable objects
        String nonser = request.getParameter("nonser");
        if (nonser != null && nonser.equals("true")) {

            try {

                request.setAttribute("nonSerObject", new Object());

                Hashtable ht = new Hashtable();
                ht.put("one", new String("one"));
                ht.put("two", new Object());
                ht.put("three", new Integer((int) (Math.random() * 100000)));

                request.setAttribute("myHashtable", ht);
                request.setAttribute("myString", "Tamera");

                //Hashtable ht1 = (Hashtable) request.getAttribute("myHashtable");
                //String myString = (String) request.getAttribute("myString");

                //out.println("<br>mystring = "+myString);
                //out.println("<br> Hashtable = "+ht1);

                getServletContext().getRequestDispatcher("/drs?method=setNonSerAttribute").forward(request, response);

                //out.println("<br>after forward ? ");

            } catch (Exception e) {
                out.println("<br> Caught exception! e= " + e);

                //System.out.println("<br> Caught exception! e= "+e);
            }

        } //end of Tamera's code

        String action = request.getParameter("action");

        if (action.equals("cookie")) {
            response.addCookie(new Cookie("testCookie", "testCookieValue"));
            timeStamp("TimeStamp");
        } else if (action.equals("locale") || action.equals("filter")) {
            timeStamp("TimeStamp");
        } else if (action.equals("sendRedirect")) {
            response.sendRedirect("TimeStamp");
        } else {
            response.sendError(500, "BasicServlet called with invalid action parm " + action);
        }

    }

}
