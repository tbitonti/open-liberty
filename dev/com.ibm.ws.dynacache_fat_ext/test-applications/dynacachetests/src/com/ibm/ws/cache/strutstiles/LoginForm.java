package com.ibm.ws.cache.strutstiles;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class LoginForm extends ActionForm {

	private String userid = null;
	private String password = null;

	/**
	 * Get accessid
	 * @return String
	 */
	public String getuserid() {
		return userid;
	}

	/**
	 * Set accessid
	 * @param <code>String</code>
	 */
	public void setuserid(String a) {
		this.userid = a;
	}

	/**
	 * Get password
	 * @return String
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set password
	 * @param <code>String</code>
	 */
	public void setPassword(String p) {
		this.password = p;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {

		// Reset values are provided as samples only. Change as appropriate.

		userid = null;
		password = null;

	}

}
