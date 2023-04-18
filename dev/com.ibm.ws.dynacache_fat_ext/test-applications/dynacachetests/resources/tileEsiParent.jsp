<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>


<HEAD>
<%@page session="false" %>



</HEAD>
<BODY>
I am the tile parent !!!
<table id="esiParent">
<tr><td><%= System.currentTimeMillis() %></td></tr>
</table>
<% String type = request.getParameter("type");
if(type.equals("edgeable"))
    out.println("The parent is edgeable");
%>
Here is my child
<tiles:insert page="/tileEsiChild.jsp?arg1=xxx" flush="true"/>

Here is another child
<tiles:insert page=="TimeStamp?arg1=1" flush="true"/>
</BODY>
