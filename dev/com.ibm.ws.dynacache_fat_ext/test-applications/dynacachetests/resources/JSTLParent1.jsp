<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<TABLE id="JSTLParent1Begin"><TR><TD>JSTLParent1Begin:<%=System.currentTimeMillis()%></TD></TR></TABLE>
<BR>


<% out.flush(); %>
<c:import url="JSTLChild1.jsp" />
<% out.flush(); %>

<BR>
<% out.flush(); %>
<c:import url="DNCChild2.jsp" />
<% out.flush(); %>
<BR>
<TABLE id="JSTLParent1End"><TR><TD>JSTLParent1End:<%=System.currentTimeMillis()%></TD></TR></TABLE>
