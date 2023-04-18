<%@page session="false" %>

ESIQueryStringParent
<TABLE id="parentTimeStamp"><TR><TD><%=System.currentTimeMillis()%></TD></TR></TABLE>

<jsp:include page="ESIQueryStringChild.jsp" flush="true"/>