<%@page import="com.ibm.websphere.servlet.cache.*" %>
<% 
if (response instanceof ServletCacheResponse) {
	((ServletCacheResponse) response).setDoNotConsume(true); 
}
%>

<TABLE id=TimeStampC2><TR><TD>C2 <%=System.currentTimeMillis()%></TD></TR></TABLE>

<% 
String ts ="foo1" + System.currentTimeMillis();
request.setAttribute("foo",ts); 
%>