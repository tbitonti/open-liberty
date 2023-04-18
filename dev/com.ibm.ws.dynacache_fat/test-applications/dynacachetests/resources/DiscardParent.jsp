<html>
<body>
<h1>WCM Discard JSP Content Test</h1>
<h3>If test is working, then you will only see the content of A1, A2, & B2</h3>
<table id="Parent">
<tr><td><%= System.currentTimeMillis() %></td></tr>
<tr><td><jsp:include page="DiscardA1.jsp" flush="true" /></td></tr>
<tr><td><jsp:include page="DiscardA2.jsp" flush="true" /></td></tr>
</table>
</body>
</html>
