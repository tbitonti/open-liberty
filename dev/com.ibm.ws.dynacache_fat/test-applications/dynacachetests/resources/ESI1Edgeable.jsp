<%@page session="false" %>
<%
String param2 = request.getParameter("arg1");
%>
ESIInclude1
<%
if (param2 != null)
   out.println(param2);
%>
</td></tr>
<tr><td>
<% 
param2 = request.getParameter("arg1");
if (param2 != null)
   param2 = new StringBuffer("ESI2Edgeable.jsp?arg1=").append(param2).toString(); 
else 
   param2 = request.getParameter("param2"); 
%>
<jsp:include page="<%= param2 %>" flush="true" />
