<%@page session="false" %>
<% 
String param2 = request.getParameter("arg1");
if (param2 != null) {
   param2 = new StringBuffer("ESI3Edgeable.jsp?arg1=").append(param2).toString();
   %>
ESIInclude2 <%= request.getParameter("arg1") %>
</td></tr>
<tr><td>
<jsp:include page="<%= param2 %>" flush="true" />
<% 
} else { %>
ESIInclude2
<%}%>




