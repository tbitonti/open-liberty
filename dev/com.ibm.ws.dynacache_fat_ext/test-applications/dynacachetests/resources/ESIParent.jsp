<%@page session="false" %>
<html><body>
I am the Parent <BR>
Here is my child:<BR>
<jsp:include page="ESIChild.jsp?parm2=c" flush="true"/>
<P>
I am the Parent<BR>
Here is another child:<br>
<jsp:include page="TimeStamp?arg1=1" flush="true"/>

