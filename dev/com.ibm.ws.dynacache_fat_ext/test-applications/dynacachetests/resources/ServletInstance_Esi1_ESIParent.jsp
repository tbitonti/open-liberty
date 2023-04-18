<%@page session="false" %>
<html><body>
I am the Parent and my time is: <%=System.currentTimeMillis()%><BR>
I am including ServletInstance_Esi2_ESIChild.jsp<BR>
<%try{Thread.sleep(150);}catch(Exception e){}%>
<jsp:include page="ServletInstance_Esi2_ESIChild.jsp?parm2=c" flush="true"/>
<P>
<BR>
ServletInstance_Esi1_ESIParent.jsp should be cached in<BR>
cache instance named ServletInstance_Esi1.<BR>
<BR>
<BR>
ServletInstance_Esi2_ESIChild.jsp should be cached in<BR>
cache instance named ServletInstance_Esi2.<BR>
