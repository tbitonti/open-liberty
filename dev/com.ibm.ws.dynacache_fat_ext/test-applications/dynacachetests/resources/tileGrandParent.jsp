
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>



<HEAD>


</HEAD>

<html>

<h1>Consume Subfragments Test</h1>
<h3>If test is working, then all the timestamps in the table will remain constant</h3>
<table id="GP">
<tr><td><%= System.currentTimeMillis() %></td></tr></table>
<% String param2 = request.getParameter("arg1");
if (param2 != null)
   param2 = new StringBuffer("/tileCSFParent.jsp?arg1=").append(param2).toString(); 
else 
   param2 = "/tileCSFParent.jsp";
%>

<tiles:insert page="<%= param2 %>" flush="true" />



</html>
