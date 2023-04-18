<%@ page import="com.ibm.ws.cache.strutstiles.LoginBean" %>

<% LoginBean lbean = (LoginBean)session.getAttribute("lbean");
 %>
UserId:
<TABLE id="userid"><TR><TD><%=lbean.getUserId()%> <%= System.currentTimeMillis() %>
</TD></TR></TABLE>
<BR>
Password:
<TABLE id="password"><TR><TD><%=lbean.getPassword()%> <%= System.currentTimeMillis() %>
</TD></TR></TABLE>