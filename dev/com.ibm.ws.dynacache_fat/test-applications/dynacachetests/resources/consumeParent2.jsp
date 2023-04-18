<html>
<body>
<h1>Consume Subfragments Test 2</h1>
<h3>If test is working, then both numbers in the table will remain constant</h3>
<table id="test">
<tr><td><%= System.currentTimeMillis() %></td></tr>
<tr><td><jsp:include page="consumeInclude.jsp" flush="true" />
</table>
</body>
</html>
