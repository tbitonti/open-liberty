<html><body>

<TABLE id=TimeStampA3><TR><TD>A3 <%=System.currentTimeMillis()%></TD></TR></TABLE>

<% 
String saveThisAttr = "" + System.currentTimeMillis();
request.setAttribute("saveThisAttr",saveThisAttr); 
request.setAttribute("dontSaveThisAttr", saveThisAttr);
%>

<jsp:include page="AttrB3.jsp" flush="true" />

</body></html>
