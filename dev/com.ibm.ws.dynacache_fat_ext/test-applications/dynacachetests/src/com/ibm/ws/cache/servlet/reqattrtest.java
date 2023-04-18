package com.ibm.ws.cache.servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class reqattrtest extends HttpServlet {

   public void service(HttpServletRequest request, HttpServletResponse response) {

      String arg = request.getParameter("arg");
      String meth = request.getParameter("method");

      if (arg != null || meth != null) {
         //then some manipulation of attributes needs to happen

         request.setAttribute("IAmRequired", "present");

         //we only want to forward if these attributes have not yet been set. Otherwise we want to print
         //the form and the timestamp
         boolean forward = (request.getAttribute("arg") == null) && (request.getAttribute("int") == null);

         if (arg != null) {
            request.setAttribute("arg",arg);
         }

         if (meth != null) {
            request.setAttribute("int",new Integer(meth));
         }

         if (forward)
            try {
               getServletContext().getRequestDispatcher(request.getServletPath()).forward(request,response);
               return;
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      PrintWriter out = null;

      try {
         out = response.getWriter();
      } catch (Exception e) {
         e.printStackTrace();
      }

      response.setContentType("text/html");
      out.println("<HTML><BODY>");
      String attr = (String) request.getAttribute("arg");

      if (attr != null) {
         out.println("Attribute set to " + attr);
      }
      else {
      	 Integer iattr = (Integer) request.getAttribute("int");
      	 if (iattr != null)
         	out.println("Attribute set to Integer " + iattr);
	  }

      out.println("<FORM id=\"submit\" ACTION=\""+request.getRequestURI()+"\" METHOD=\"get\"><BR>");
      out.println("<INPUT TYPE=\"submit\" NAME=\"arg\" VALUE=\"1\"><BR><BR>");
      out.println("<INPUT TYPE=\"submit\" NAME=\"arg\" VALUE=\"2\"><BR><BR>");
      out.println("<INPUT TYPE=\"submit\" NAME=\"method\" VALUE=\"3\"><BR><BR>");
      out.println("<INPUT TYPE=\"submit\" NAME=\"method\" VALUE=\"4\"><BR><BR>");
      out.println("</FORM>");
      out.println("<TABLE id=\"TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
      out.println("</BODY></HTML>");

   }

}
