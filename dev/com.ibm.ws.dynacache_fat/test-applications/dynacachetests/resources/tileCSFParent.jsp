
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>


<table id="parent">
<tr><td><%= System.currentTimeMillis() %></td></tr></table>
<tiles:insert page="tileCSFChild1.jsp?arg1=CSF" flush="true" />	
<tiles:insert page="tileCSFChild2.jsp?arg1=CSF" flush="true" />


