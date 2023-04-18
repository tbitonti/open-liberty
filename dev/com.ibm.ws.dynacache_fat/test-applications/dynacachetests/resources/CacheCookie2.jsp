<%
String cookieval = new Long(System.currentTimeMillis()).toString();
response.addCookie(new Cookie("cookie1", cookieval));
response.addCookie(new Cookie("cookie2", cookieval));
response.addCookie(new Cookie("cookie3", cookieval));
%>

<TABLE id="TimeStamp"><TR><TD><%=System.currentTimeMillis()%></TD></TR></TABLE>
