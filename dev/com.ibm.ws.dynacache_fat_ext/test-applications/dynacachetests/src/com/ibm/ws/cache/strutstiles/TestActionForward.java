package com.ibm.ws.cache.strutstiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class TestActionForward extends Action {

    public ActionForward perform(
        ActionMapping aMapping,
        ActionForm aForm,
        HttpServletRequest aRequest,
        HttpServletResponse aResponse
    ) {
	String type = aRequest.getParameter("type");
	if(type == null)
	    type="default";
	if(type.equalsIgnoreCase("forwardtojsp"))
	    return aMapping.findForward("tojsp");
	else if(type.equalsIgnoreCase("forwardtoservlet"))
	    return aMapping.findForward("toservlet");
	else if(type.equalsIgnoreCase("forwardtostruts"))
	    return aMapping.findForward("tostruts");
	else if(type.equalsIgnoreCase("forwardtotile"))
	    return aMapping.findForward("totile");
	else if(type.equalsIgnoreCase("esiforward"))
	    return aMapping.findForward("esiforward");
	else if(type.equalsIgnoreCase("esiParentConsume"))
	    return aMapping.findForward("esiParentConsume");
	else
    	    return null;
    }

}

