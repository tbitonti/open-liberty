package com.ibm.ws.cache.strutstiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class StrutsDNCFragment extends Action {

    public ActionForward perform(
        ActionMapping aMapping,
        ActionForm aForm,
        HttpServletRequest aRequest,
        HttpServletResponse aResponse)
    {
	
	
	    String param = aRequest.getParameter("whoami");
	    if(param == null)
		param = "grandparent";	
	    if(param.equalsIgnoreCase("grandparent")) {
		return aMapping.findForward("parent");	    
	    }
	    else
		return null;	    	    	  
			
  }

}

