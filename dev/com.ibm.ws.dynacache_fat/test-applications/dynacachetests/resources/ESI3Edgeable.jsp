<%@page session="false" %>
ESIInclude3
<% 
String arg = request.getParameter("arg1");
if (arg != null) 
   out.println(arg);
%>
