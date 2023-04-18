<%@page session="false" %>
ESIInclude4
<% 
String arg = request.getParameter("arg1");
if (arg != null) 
   out.println(arg);
%>
