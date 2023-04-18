package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class IncludeTestServlet extends HttpServlet {
  //**
  //** init( ServletConfig config )
  //**
  static Random r = new Random();
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
  	
  	
    String type = req.getParameter("type");
    if (type == null){
	type= "default";	
    }
  	
    if (type.equals("requestForward")){
	req.setAttribute("type","requestForward");
	getServletContext().getRequestDispatcher("/IncludeTimeStamp").forward(req,resp);	
    }
    else if (type.equals("requestInclude")){
	req.setAttribute("type","requestInclude");
	getServletContext().getRequestDispatcher("/IncludeTimeStamp").include(req,resp);
    }
    else if (type.equals("namedForward")){
	req.setAttribute("type","namedForward");
	getServletContext().getNamedDispatcher("IncludeTimeStamp").forward(req,resp);	
    }
    else if (type.equals("namedInclude")){
	req.setAttribute("type","namedInclude");
	getServletContext().getNamedDispatcher("IncludeTimeStamp").include(req,resp);		
    }
    else{
	int random = Math.abs(r.nextInt()%2000);
	req.setAttribute("output",new Integer(random));
	getServletContext().getRequestDispatcher("/CacheTest.jsp").forward(req,resp);	
    } 

  }

}
