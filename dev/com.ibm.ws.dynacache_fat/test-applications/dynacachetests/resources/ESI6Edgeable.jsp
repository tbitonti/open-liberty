<%@page session="false" %>
ESIInclude6
<% 
String arg = request.getParameter("arg1");
if (arg != null) 
   out.println(arg);
%>
