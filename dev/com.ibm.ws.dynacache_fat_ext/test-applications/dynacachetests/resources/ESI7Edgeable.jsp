<%@page session="false" %>
ESIInclude7
<% 
String arg = request.getParameter("arg1");
if (arg != null) 
   out.println(arg);
%>
