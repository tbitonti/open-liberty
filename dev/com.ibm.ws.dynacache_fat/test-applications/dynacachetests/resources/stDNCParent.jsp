<html>
<body>
<h1>Do-Not-Consume Subfragments Test</h1>
<table id="DNC1">
<tr><td><%= System.currentTimeMillis() %></td></tr>
<tr><td><jsp:include page="stDNCGrandChild.jsp" flush="true" />
</table>
</body>
</html>
