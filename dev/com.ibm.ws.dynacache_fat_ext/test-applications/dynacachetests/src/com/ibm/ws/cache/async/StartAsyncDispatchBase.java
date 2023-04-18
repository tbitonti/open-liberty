package com.ibm.ws.cache.async;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet 3.0 implementation class StartAsyncDispatchBase
 */
public class StartAsyncDispatchBase extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String BEFORE_THREAD="before startAsync on thread->[";
	public static final String START_CHARS = "startChars->[";
	public static final String NUM_CHARS = "numChars->[";

    /**
     * Default constructor. 
     */
    public StartAsyncDispatchBase() {}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    PrintWriter pw = response.getWriter();
	    pw.println("in doGet of"+this.getServletName());
	    pw.println("request.isAsyncSupported()->"+request.isAsyncSupported());
	    pw.println("dispatcherType is ["+request.getDispatcherType()+"]");
	    if (request.getAttribute("completedFirstRun")!=null) {
	        pw.println("After dispatch on new thread");
	        pw.println ("<div id=\"Timestamp_after_dispatch\">"+ System.currentTimeMillis()+"</div>");
	        
	        if (request.isAsyncStarted()){
                pw.println("ERROR: isAsyncStarted should return false after dispatch");
            }
	        
	        if (request.getParameter("implicitComplete")==null) {
	            AsyncContext asyncContext = request.getAsyncContext();
	            if (asyncContext!=request.getAttribute("previousAsyncContext"))
	                pw.println("ERROR: async context from startAsync and getAsyncContext do not match");
	            asyncContext.complete();
	        }
	        else {
	            pw.println("implicitComplete");
	        }
        }
	    else {
	        pw.println(BEFORE_THREAD+request.getParameter("threadNum")+"]");
	        pw.println ("<div id=\"Timestamp_before_dispatch\">"+ System.currentTimeMillis()+"</div>");
	        
	        if (request.isAsyncStarted()){
	            pw.println("ERROR: isAsyncStarted shouldn't return true until after startAsync");
	        }
	        
	        response.addHeader("testHeader", "testHeaderValue");
	        
	        if (request.getParameter("flush")!=null){
	            response.flushBuffer();
	        }
	        
	        AsyncContext asyncContext = request.startAsync();
	        
	        String sleepTime = request.getParameter("sleepTime");
	        if (null != sleepTime){
	        	asyncContext.setTimeout(Integer.parseInt(sleepTime)/2); //this shld result in timeout of the async task
	        }
	        
	        if (!request.isAsyncStarted()){
                pw.println("ERROR: isAsyncStarted should return true after startAsync");
            }
	        
	        request.setAttribute("previousAsyncContext",asyncContext);
	        
    		
    		AsyncRunnable r = new AsyncRunnable(asyncContext,pw,request);
    		asyncContext.start(r);
	    }
	}
	
	private class AsyncRunnable implements Runnable{
		
        private AsyncContext asyncContext;
		private PrintWriter printWriter;
		private HttpServletRequest request;

		public AsyncRunnable(AsyncContext asyncContext, PrintWriter printWriter, HttpServletRequest request){
			this.asyncContext=asyncContext;
			this.printWriter=printWriter;
			this.request = request;
		}
		
		public void run() {
			printWriter.println(NUM_CHARS+request.getParameter("numChars")+"]");
			int numChars = Integer.valueOf(request.getParameter("numChars")).intValue();
			printWriter.print(START_CHARS);
			String digits = new String("0123456789");
			for (int i=0;i<numChars;i++){
				int remainder = i%10;
				printWriter.print(digits.charAt(remainder));
			}
			printWriter.println("]");
			
		    request.setAttribute("completedFirstRun", "true");
		    String servletContextStr = request.getParameter("context");
		    String path = request.getParameter("path");

		    
	        String sleepTime = request.getParameter("sleepTime");
	        if (null != sleepTime){
	        	long sleep = Long.parseLong(sleepTime.trim());
	        	try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
	        
	        if (null != request.getParameter("exception")){
	        	Integer.parseInt("This exception is thrown to prevent the caching of the fragments");
	        }
		   
		    if (servletContextStr!=null){
		        if (path!=null){
		            ServletContext servletContext = request.getServletContext().getContext(servletContextStr);
		            asyncContext.dispatch(servletContext, path);
		            
		        } else {
		            printWriter.print("ERROR: must provide a path when providing a different context");
		            asyncContext.complete();
		        }
		    } else if (path!=null){
		        asyncContext.dispatch(path);
		    } else {
		        asyncContext.dispatch();
		    }
		    
		}		
	}
}
