package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class BufferWriterServlet extends HttpServlet {
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
     resp.setContentType("text/html");
     PrintWriter out = resp.getWriter();
     resp.setBufferSize(16384);
     if (resp.getBufferSize()<16384) {
        resp.sendError(500,"buffer size is smaller than set buffer size");
        return;
     }
     out.println("<html><body>");
     out.println("<table id=\"badtable\" /><tr><td>bad</td></tr></table>");
     out.println("<pre>");
     for (int i=0;i<10000;i++)
        out.print(" ");
     out.println("</pre>");
     out.println("</html></body>");
     // reset and start over
     resp.resetBuffer();
     out.println("<html><body>");
     out.println("<table id=\"goodtable\" /><tr><td>reset worked</td></tr></table>");
     out.println("</html></body>");
     out.close();
  }

}
