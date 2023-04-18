<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>

<html>
I am the tile parent !!!
<table id="TimeStampParent">
<tr><td><%= System.currentTimeMillis() %></td></tr></table>
Here is my child
<BR>
<% String type = request.getParameter("type");
String name = null;
if(type == null)
	type="jsp";
if(type.equals("jsp"))
	name ="/tileChild.jsp?arg1=xxx";
else if(type.equals("servlet"))
	name ="/tileChildServlet";
else if(type.equals("strut"))
	name="/basic.do?arg1=xxx";
%>                 

<tiles:insert page="<%= name %>" flush="true"/>	
</html>

