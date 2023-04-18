package com.ibm.ws.cache.strutstiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.*;
import java.io.*;

public class StrutsSaveAttributesChild extends Action {

    public ActionForward perform(
        ActionMapping aMapping,
        ActionForm aForm,
        HttpServletRequest aRequest,
        HttpServletResponse aResponse)
    {
	try{
	
	    PrintWriter out = aResponse.getWriter(); 	    	    
	   //Object att1 = aRequest.getAttribute("att1");
	    out.println("Here is the request attribute");
	   out.println("<TABLE id=\"att1\"><TR><TD>"+aRequest.getAttribute("att1")+"</TD></TR></TABLE>");

	}
	catch(Exception e){}
	
	return null;
  }

}
