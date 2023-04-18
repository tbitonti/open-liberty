<%@page import="com.ibm.websphere.servlet.cache.*" %>

<TABLE id="DNCChild1TimeStamp"><TR><TD>DNCChild1:<%=System.currentTimeMillis()%></TD></TR></TABLE>

<% 
 if (request.getParameter("test") != null){
 	 if (request.getParameter("test").equals("test8") ||
 	 	 request.getParameter("test").equals("test9") ||
 	 	 request.getParameter("test").equals("test10"))
		((ServletCacheResponse) response).setDoNotConsume(true);
	}
%>
