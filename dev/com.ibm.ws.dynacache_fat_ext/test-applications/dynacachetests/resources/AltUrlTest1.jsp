<%@page import="java.lang.String" session="true" %>
<jsp:useBean id="parent" type="java.lang.String" scope="request" />
<html<<body>
AltUrlTest1<BR>
My parent:<%=parent%><BR>
<p>

<jsp:include page="AltUrlTest2.jsp" flush="true"/>
  
<jsp:include page="AltUrlTest3.jsp" flush="true"/>
<jsp:include page="AltUrlTest4.jsp" flush="true"/>
<jsp:include page="AltUrlTest5.jsp" flush="true"/>
