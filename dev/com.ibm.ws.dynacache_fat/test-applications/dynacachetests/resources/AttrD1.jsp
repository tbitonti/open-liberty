<%@page import="com.ibm.websphere.servlet.cache.*" %>
<% 
if (response instanceof ServletCacheResponse) {
	((ServletCacheResponse) response).setDoNotConsume(true); 
}
%>

<TABLE id=TimeStampD1><TR><TD>D1 <%=System.currentTimeMillis()%></TD></TR></TABLE>

<TABLE id=foo><TR><TD><%=request.getAttribute("foo")%></TD></TR></TABLE>