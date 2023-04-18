<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<html><body>

<TABLE id=TimeStamp><TR><TD>A1 <%=System.currentTimeMillis()%></TD></TR></TABLE>

<% out.flush(); %>
<c:import url="static.html" />
<% out.flush(); %>

</body></html>