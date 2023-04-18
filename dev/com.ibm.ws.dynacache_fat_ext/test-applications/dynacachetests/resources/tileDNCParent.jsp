<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>

<HEAD>


</HEAD>
<html>
<body>

<table id="parent">
<tr><td><%= System.currentTimeMillis() %></td></tr></table>
<tiles:insert page="tileCSFChild1.jsp?arg1=DNC" flush="true" />	
<tiles:insert page="tileCSFChild2.jsp?arg1=DNC" flush="true" />
</body>

</html>