package com.ibm.ws.cache.async;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MyDefinedServlet2
 */
public class MyDefinedServlet2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static int cntr = 0;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyDefinedServlet2() {
        super();
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
        sos.println("MyDefinedServlet2 invoked");
        cntr++;
        sos.println("<div id=\"COUNTER_VALUE\">"+ cntr+"</div>");
        sos.println("MyDefinedServlet2 done");
	}

}
