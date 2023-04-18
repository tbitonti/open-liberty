package com.ibm.ws.cache.strutstiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.*;
import java.io.*;

public class BasicStrutsTest extends Action {

    public ActionForward perform(
        ActionMapping aMapping,
        ActionForm aForm,
        HttpServletRequest aRequest,
        HttpServletResponse aResponse)
    {
	try{
	
	    PrintWriter out = aResponse.getWriter(); 
	    String param = aRequest.getParameter("arg1");
	    
	    out.println("arg1:"+param);
	    out.println("<TABLE id=\"TimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
	}
	catch(Exception e){}
	
	return null;
  }

}
