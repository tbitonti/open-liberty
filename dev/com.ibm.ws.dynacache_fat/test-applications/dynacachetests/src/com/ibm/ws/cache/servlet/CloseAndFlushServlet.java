package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CloseAndFlushServlet extends HttpServlet {

  //**
  //** init( ServletConfig config )
  //**
  public void init( ServletConfig config ) throws ServletException      {
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
     String action = req.getParameter("action");
     resp.setContentType("text/html");
     if (action == null) {
        resp.sendError(500, "'action' parameter cannot be null");
     }

     if (action.equalsIgnoreCase("closestream")) {
        OutputStream os = resp.getOutputStream();
        for (int i=0;i<100;i++) os.write(i);
        os.close();
        return;
     }

     if (action.equalsIgnoreCase("closewriter")) {
        PrintWriter out = resp.getWriter();
        out.print("The Quick Brown Fox Jumped Over the Lazy Dog");
        out.println("<TABLE id=\"TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
        out.close();
        return;
     }


     if (action.equalsIgnoreCase("flushstream")) {
        OutputStream os = resp.getOutputStream();
        for (int i=0;i<100;i++) os.write(i);
        os.flush();
        for (int i=100;i<200;i++) os.write(i);
        return;
     }


     if (action.equalsIgnoreCase("flushwriter")) {
        PrintWriter out = resp.getWriter();
        out.print("The Quick Brown Fox Jumped Over the Lazy Dog");
        out.println("<TABLE id=\"TimeStamp1\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
        out.flush();
        out.print("And Jill Came Tumbling After");
        out.println("<TABLE id=\"TimeStamp2\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
        return;
     }
   }

}
