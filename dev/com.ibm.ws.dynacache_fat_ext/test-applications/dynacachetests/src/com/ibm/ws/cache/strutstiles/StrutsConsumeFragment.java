package com.ibm.ws.cache.strutstiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.*;
import java.io.*;

public class StrutsConsumeFragment extends Action {

    public ActionForward perform(
        ActionMapping aMapping,
        ActionForm aForm,
        HttpServletRequest aRequest,
        HttpServletResponse aResponse)
    {
	try{
	
	    String param = aRequest.getParameter("whoami");
	    if(param == null)
		param = "default";
	    PrintWriter out = aResponse.getWriter(); 	    
	    if(param.equalsIgnoreCase("grandparent")) 
		return aMapping.findForward("parent");
	    else if(param.equalsIgnoreCase("parent")){
		out.println(System.currentTimeMillis());
		return null;
	    }
	    else
		return null;	    	    	  
	}
	catch(Exception e){return null;}
		
  }

}

