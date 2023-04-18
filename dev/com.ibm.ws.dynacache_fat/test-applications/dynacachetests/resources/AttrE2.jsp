<%@page import="com.ibm.websphere.servlet.cache.*" %>
<% 
if (response instanceof ServletCacheResponse) {
	((ServletCacheResponse) response).setDoNotConsume(true); 
}
%>

<TABLE id=TimeStampE2><TR><TD>E2 <%=System.currentTimeMillis()%></TD></TR></TABLE>

<TABLE id=foo><TR><TD>foo <%=request.getAttribute("foo") %></TD></TR></TABLE>

