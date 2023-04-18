<%@page import="com.ibm.websphere.servlet.cache.*" %>
<html>
<body>
<TABLE id=TimeStamp>
<TR><TD>TimeStamp:</TD><TD><%=System.currentTimeMillis()%></TD></TR>
<TR>
<TD>IsUncacheable:</TD>
<TD>
<% 	
	HttpServletRequest req = (HttpServletRequest)request;
   	while (true) {
   		if (req instanceof ServletCacheRequest) {
        	boolean unCacheable=((ServletCacheRequest)req).isUncacheable();
            out.println(unCacheable);
            break;
        }
        if (! (req instanceof HttpServletRequestWrapper)) {
        	break;
        }
        req = (HttpServletRequest)((HttpServletRequestWrapper)req).getRequest();
          
	}
%>
</TD></TR></TABLE>
</body>
</html>
