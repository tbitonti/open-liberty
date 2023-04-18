<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>


<html:html>
<HEAD>


</HEAD>

<html>
<body>
<h1>Consume Subfragments Test</h1>
<h3>If test is working, then all the timestamps in the table will remain constant</h3>
<table id="GP">
<tr><td><%= System.currentTimeMillis() %></td></tr></table>
<tr><td><tile:insert page="tileDNCParent.jsp" flush="true" />

</body>
</html>
