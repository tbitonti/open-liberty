<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<TABLE id="JSTLGrandParentBegin"><TR><TD>JSTLGrandParentBegin:<%=System.currentTimeMillis()%></TD></TR></TABLE>
<BR>

<% out.flush(); %>
<c:import url="JSTLParent1.jsp" />
<% out.flush(); %>

<BR>
<% out.flush(); %>
<c:import url="DNCParent2.jsp" />
<% out.flush(); %>
<BR>
<TABLE id="JSTLGrandParentEnd"><TR><TD>JSTLGrandParentEnd:<%=System.currentTimeMillis()%></TD></TR></TABLE>
