  <%@page extends="com.ibm.websphere.servlet.cache.CacheableJspPage"%>
  <%@page import="javax.servlet.*" %>
  <%@page import="javax.servlet.http.*" %>
  <%@page import="java.security.cert.*" %>
  <%@page import="com.ibm.websphere.servlet.cache.*" %>
  <%@page contentType="text/html;charset=SJIS" %>

  <html>
  <head>
    <title>snoop.xsp</title>
  </head>

  <body>
    <!-- Print out the request information -->
    <TABLE id=TimeStamp><TR><TD><%=System.currentTimeMillis()%></TD></TR></TABLE>
    <h1>Request information:</h1>
    generated cacheid: <%=("japan;include=" + (request.getAttribute("javax.servlet.include.request_uri") == null)).toString()%>
    <pre>
<%!
    public String
    getId(HttpServletRequest request)
    {
        return "japan;include=" + (request.getAttribute("javax.servlet.include.request_uri") == null);
    }


    public int
    getSharingPolicy(HttpServletRequest request)
    {
        return com.ibm.websphere.cache.EntryInfo.SHARED_PULL;
    }
%>
<%
    com.ibm.websphere.servlet.cache.FragmentInfo fragmentInfo =
        ((ServletCacheRequest) request).getFragmentInfo();

    //fragmentInfo is null if (cacheIt == false) or (cacheId == null)
    if (fragmentInfo != null) {
        //showing what other things can set of cache metadata on jsp
        //explicitly setting default values
        //     fragmentInfo.setCacheIt(true);
        fragmentInfo.setTimeLimit(-1); //no time limit

        //creating a dataId
        fragmentInfo.addDataId("foo");
    }

    out.println("request object: " + request);
    out.println("response object: " + response);
%>
      <%= "Attribute 'foo': " + request.getAttribute("foo") %>

      <%= "Request method: " + request.getMethod() %>
      <%= "Request Template: " + request.getRequestURI() %>
      <%= "Request protocol: " + request.getProtocol() %>
      <%= "Servlet path: " + request.getServletPath() %>
      <%= "Path info: " + request.getPathInfo() %>
      <%= "Path translated: " + request.getPathTranslated() %>
      <%= "Query string: " + request.getQueryString() %>
      <%= "Content length: " + request.getContentLength() %>
      <%= "Content type: " + request.getContentType() %>
      <%= "Server name: " + request.getServerName()%>
      <%= "Server port: " + request.getServerPort()%>
      <%= "Remote user: " + request.getRemoteUser()%>
      <%= "Remote address: " + request.getRemoteAddr() %>
      <%= "Remote host: " + request.getRemoteHost()%>
      <%= "Authorization scheme: " + request.getAuthType() %>
      <%= "<p>Yes =  \u306f\u3044" %>
      <%= "<p>No  =  \u3044\u3044\u3048" %>

    </pre>

  </body>
  </html>
