<%@page session="false" %>
<jsp:useBean id="output" type="java.lang.Integer" scope="request" />
<% request.setAttribute("outputForInclude1",output); %>
<% request.setAttribute("outputForInclude2",output); %>
<HTML>
<title>CacheTest <%= output%></title>
<body topmargin="0" leftmargin="40" marginwidth="0" marginheight="0" bgcolor="#0066CC">
<font color="#FFFFFF">
<H2>CacheTest <%= output %></H2>
<br>
<jsp:include page="Include1.jsp" flush="true" />
<jsp:include page="Include2.jsp" flush="true" />
<br>
<H2>Back in main page</H2>
<TABLE id="maintable" BORDER="2" BGCOLOR="#DDDDFF" cellpadding="5">
<br>
<% for (int i=0;i<12;i++) {    %>
   <tr>
   <% for (int j=0;j<8;j++) {    %>
      <td><%= output %></td>
   <% } %>
   </tr>
<% }  %>
</table>
