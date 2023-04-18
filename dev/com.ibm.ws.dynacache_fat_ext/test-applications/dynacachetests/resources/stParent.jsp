<html>
<body>
<h1>Consume Subfragments Test</h1>
<h3>If test is working, then both numbers in the table will remain constant</h3>
<table id="CSF1">
<tr><td><%= System.currentTimeMillis() %></td></tr>
<tr><td><jsp:include page="stGrandChild.jsp" flush="true" />
</table>
</body>
</html>
