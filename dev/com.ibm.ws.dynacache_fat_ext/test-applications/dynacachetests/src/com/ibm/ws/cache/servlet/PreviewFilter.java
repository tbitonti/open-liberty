package com.ibm.ws.cache.servlet;

import javax.servlet.*;

public class PreviewFilter implements Filter 
{
    
    // implement the required init method
    public void init(FilterConfig fc)
    {
        
    }

    // implement the required doFilter method...this is where most of 
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    {
        try
        {
        	String parm = request.getParameter("previewRequest");
        	if (parm == null)
        		request.setAttribute("previewRequest",null);
        	else if (parm.equals("true"))
        		request.setAttribute("previewRequest","true");
        	else if (parm.equals("false"))
        		request.setAttribute("previewRequest","false");
        
        	request.setAttribute("testAttrib", "true");
        	
        	chain.doFilter(request, response);
        }
        catch (Throwable t)
        {
            // handle problem...
        }
    }

    // implement the required destroy method
    public void destroy()
    {
        
    }
}