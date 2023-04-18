<%@page session="false" %>
<jsp:useBean id="outputForInclude1" type="java.lang.Integer" scope="request" />
<H2>Include 1 <%= outputForInclude1 %></H2>
<TABLE id="include1" BORDER="2" BGCOLOR="#DDDDFF" cellpadding="5">
<tr><td><%= outputForInclude1 %></td></tr>
</table>
