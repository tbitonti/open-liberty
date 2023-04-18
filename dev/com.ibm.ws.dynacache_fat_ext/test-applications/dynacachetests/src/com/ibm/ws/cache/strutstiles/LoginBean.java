package com.ibm.ws.cache.strutstiles;

import java.io.Serializable;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class LoginBean implements Serializable {
	
	private String userid="";
	private String password="";
	
	
	
	public LoginBean(String uid,String passwd){		
		this.userid=uid;
		this.password=passwd;
	}
	
	public String getUserId()
	{
	  return userid ;
	}
	public String getPassword()
	{
	  return password ;
	}

	public void setUserId(String uid){
	    this.userid = uid;
	}

	public void setPassword(String passwd){
	    this.password = passwd;
	}
	 
  
    }

