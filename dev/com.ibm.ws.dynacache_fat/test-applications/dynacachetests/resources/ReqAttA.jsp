<HTML>
<BODY>
<H2>
<% out.println("Setting a Request Attribute  iteratively  5 times and retrieving the attribute in the included JSP file"); %>
</H2>
<TABLE id="ReqAttr" border="2"> 

<%for (int i=0;i<5;i++){%>
<TR><TD>
<%request.setAttribute("ReqAttr", "I am required B "+ new Integer(i).toString());%>
<% out.println((String)(request.getAttribute("ReqAttr"))); %>
</TD><TD>
<jsp:include page="ReqAttB.jsp" flush="true" />
</TD></TR>
<%}%>
</TABLE>
</BODY>
</HTML>
