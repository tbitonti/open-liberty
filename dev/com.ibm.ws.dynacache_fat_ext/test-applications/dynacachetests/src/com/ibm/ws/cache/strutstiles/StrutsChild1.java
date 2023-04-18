package com.ibm.ws.cache.strutstiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.*;
import java.io.*;

public class StrutsChild1 extends Action {

    public ActionForward perform(
        ActionMapping aMapping,
        ActionForm aForm,
        HttpServletRequest aRequest,
        HttpServletResponse aResponse)
    {
	try{
	
	    PrintWriter out = aResponse.getWriter(); 	    
	    out.println("I am a struts action child");	    
	    out.println("<TABLE id=\"StrutsTimeStamp\"><TR><TD>"+System.currentTimeMillis()+"</TD></TR></TABLE>");
	}
	catch(Exception e){}
	
	return null;
  }

}

