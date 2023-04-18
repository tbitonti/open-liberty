<h1>A1</h1>
<table id="A1">
<tr><td><%= System.currentTimeMillis() %></td></tr>
<% request.setAttribute ("discardJSPContent", "true"); %>
<tr><td><jsp:include page="DiscardB1.jsp" flush="true" /></td></tr>
<% request.setAttribute ("discardJSPContent", "false"); %>
</table>
