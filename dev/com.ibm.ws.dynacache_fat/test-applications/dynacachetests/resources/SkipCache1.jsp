<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<body>
<TABLE id="ParentTimeStamp"><TR><TD><%=System.currentTimeMillis()%></TD></TR></TABLE>
<jsp:include page="SkipCacheChild1.jsp" flush="true"/>
<% request.setAttribute("prettypleaseskipcaching", "any value"); %>
<jsp:include page="SkipCacheChild2.jsp" flush="true"/>
</body>
</html>


