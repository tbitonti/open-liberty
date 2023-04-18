package com.ibm.ws.cache.async;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * Application Lifecycle Listener implementation class MyListener
 *
 */
public class MyListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public MyListener() {
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        ServletRegistration.Dynamic reg = sc.addServlet("MyProgrammaticServlet", "com.ibm.ws.cache.async.ProgrammaticServlet");
        reg.addMapping("/MyProgrammaticServlet");
        
        ServletRegistration.Dynamic reg1 = sc.addServlet("MyProgrammaticServlet2", ProgrammaticServlet2.class);
        reg1.addMapping("/MyProgrammaticServlet2");
        
        try {
            ProgrammaticServlet3 ps3 = sc.createServlet(ProgrammaticServlet3.class);
            ServletRegistration.Dynamic reg2 = sc.addServlet("MyProgrammaticServlet3", ps3);
            reg2.addMapping("/MyProgrammaticServlet3");
        } catch (ServletException e) {
            e.printStackTrace();
            System.out.println("creating and mapping the servlet, ProgrammaticServlet3, failed");
        }

    }
	
}
