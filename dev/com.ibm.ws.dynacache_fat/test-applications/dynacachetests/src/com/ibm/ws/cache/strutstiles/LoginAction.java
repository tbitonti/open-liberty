package com.ibm.ws.cache.strutstiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;



public class LoginAction extends Action {

    public ActionForward perform(
        ActionMapping aMapping,
        ActionForm aForm,
        HttpServletRequest aRequest,
        HttpServletResponse aResponse)
    {
	
	String userid = ((LoginForm)aForm).getuserid();
	String password = ((LoginForm)aForm).getPassword();
	LoginBean lbean = new LoginBean(userid, password);
	HttpSession session = aRequest.getSession(true);
	session.setAttribute("lbean", lbean);
	return aMapping.findForward("displayinfo");
    }
}

