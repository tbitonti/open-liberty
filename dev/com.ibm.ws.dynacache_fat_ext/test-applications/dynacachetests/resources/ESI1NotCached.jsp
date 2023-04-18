<%@page session="false" %>
ESIInclude1
</td></tr>
<tr><td>
<% String param2 = request.getParameter("param2"); %>
<jsp:include page="<%= param2 %>" flush="true" />
