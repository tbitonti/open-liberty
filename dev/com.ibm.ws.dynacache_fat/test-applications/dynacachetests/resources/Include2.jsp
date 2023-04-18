<%@page session="false" %>
<jsp:useBean id="outputForInclude2" type="java.lang.Integer" scope="request" />
<H2>Include 2 <%= outputForInclude2 %></H2>
<TABLE id="include2" BORDER="2" BGCOLOR="#DDDDFF" cellpadding="5">
<tr><td><%= outputForInclude2 %></td></tr>
<tr><td><%= outputForInclude2 %></td></tr>
</table>
