
<TABLE id="DNCParent1Begin"><TR><TD>DNCParent1Begin:<%=System.currentTimeMillis()%></TD></TR></TABLE>
<BR>

<% String value= "DNCChild1.jsp?test=" + request.getParameter("test"); %>
<jsp:include page="<%=value%>" flush="true"/>

<BR>
<jsp:include page="DNCChild2.jsp" flush="true"/>
<BR>
<TABLE id="DNCParent1End"><TR><TD>DNCParent1End:<%=System.currentTimeMillis()%></TD></TR></TABLE>
