package com.ibm.ws.cache.strutstiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class StrutsSaveAttributesParent extends Action {

    public ActionForward perform(
        ActionMapping aMapping,
        ActionForm aForm,
        HttpServletRequest aRequest,
        HttpServletResponse aResponse)
    {
			 
	    	aRequest.setAttribute("att1",new Long(System.currentTimeMillis())); 
		return aMapping.findForward("savAttrChild");	    
        	    	    	  
			
  }

}

