
<TABLE id="DNCGrandParentBegin"><TR><TD>DNCGrandParentBegin:<%=System.currentTimeMillis()%></TD></TR></TABLE>
<BR>

<% String value= "DNCParent1.jsp?test=" + request.getParameter("test"); %>
<jsp:include page="<%=value%>" flush="true"/>

<BR>
<jsp:include page="DNCParent2.jsp" flush="true"/>
<BR>
<TABLE id="DNCGrandParentEnd"><TR><TD>DNCGrandParentEnd:<%=System.currentTimeMillis()%></TD></TR></TABLE>
