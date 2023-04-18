package com.ibm.ws.cache.async;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class DefinedServlet
 */
@WebServlet(name="MyAnnotatedServlet", urlPatterns="/MyAnnotatedServlet")
public class AnnotatedServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public AnnotatedServlet() {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    ServletOutputStream sos = response.getOutputStream();
	    sos.println("AnnotatedServlet invoked");
	    HttpSession sess = request.getSession(true);
	    Integer cntr = (Integer)sess.getAttribute("cntr");
	    if (cntr==null) {
	        cntr = Integer.valueOf(1);
	    } else {
	        int newInt = cntr.intValue()+1;
	        cntr = Integer.valueOf(newInt);
	    }
	    sess.setAttribute("cntr", cntr);
	    sos.println("<div id=\"COUNTER_VALUE\">"+ cntr.toString()+"</div>");
	    sos.println("AnnotatedServlet done");
	}

}
