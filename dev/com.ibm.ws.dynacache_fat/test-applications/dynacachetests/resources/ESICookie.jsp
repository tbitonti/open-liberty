<%@page session="false" %>
<%
response.addCookie(new Cookie("testCookie","testCookieValue"));
%>

ESICookie
<TABLE id="childTimeStamp"><TR><TD><%=System.currentTimeMillis()%></TD></TR></TABLE>
