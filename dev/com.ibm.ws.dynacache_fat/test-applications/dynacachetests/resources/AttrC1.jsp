<TABLE id=TimeStampC1><TR><TD>C1 <%=System.currentTimeMillis()%></TD></TR></TABLE>

<% request.setAttribute("foo","foo1"); %>

<jsp:include page="AttrD1.jsp" flush="true" />
