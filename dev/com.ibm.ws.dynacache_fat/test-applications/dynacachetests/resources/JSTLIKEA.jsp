<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<TABLE id="ALL">
<TR><TD>
<TABLE id="JSTL Parent Begin">
	<TR>
		<TD>JSTL Parent Begin:<%=System.currentTimeMillis()%></TD>
	</TR>
</TABLE>
<BR>

<% out.flush(); %>
<c:import url="JSTLParent1.jsp" var="testcache" />
<% out.flush(); %>
<BR>

<% out.flush(); %>
<c:import url="DNCParent2.jsp"  var="dnc" />
<% out.flush(); %>

<BR>
<TABLE id="DNC">
	<TR>
		<TD><c:out value="${dnc}" /></TD>
	</TR>
</TABLE>

<TABLE id="testcache">
	<TR>
		<TD><c:out value="${testcache}" /></TD>
	</TR>
</TABLE>

<TABLE id="JSTL Parent End">
	<TR>
		<TD>JSTLGrandParentEnd:<%=System.currentTimeMillis()%></TD>
	</TR>
</TABLE>
</TD></TR>
</TABLE>