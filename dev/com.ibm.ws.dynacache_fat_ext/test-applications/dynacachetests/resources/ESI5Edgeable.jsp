<%@page session="false" %>
ESIInclude5
<% 
String arg = request.getParameter("arg1");
if (arg != null) 
   out.println(arg);
%>
