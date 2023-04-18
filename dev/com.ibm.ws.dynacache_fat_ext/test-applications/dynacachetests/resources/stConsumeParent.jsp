<html>
<body>
<h1>Consume Subfragments Test</h1>
<h3>If test is working, then both numbers in the table will remain constant</h3>
<table id="CSF2">
<tr><td><%= System.currentTimeMillis() %></td></tr>
<tr><td><jsp:include page="stConsumeFragment.do?whoami=parent" flush="true" />
</table>
</body>
</html>
