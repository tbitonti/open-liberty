<html>
<head>
<meta http-equiv='Content-Type' content='text/html; charset=iso-8859-1'>
<title></title>
<STYLE type="text/css">
<!--
INPUT {
	font-family: Arial;
	font-size: 9px;
	padding-left: 0px;
	padding-right: 0px;
	margin-right: 2px;
	margin-left: 2px
}

SELECT {
	font-family: Arial;
	font-size: 9px
}

TR {
	font-family: Arial;
	font-size: 9px
}

BODY {
	font-family: Arial;
	font-size: 13px
}
-->
</STYLE>
<SCRIPT type="text/javascript">
function func_1() {

}

function _JumpURL(url) 
{
  if (url != "")
  {
    window.location = url;
  }
}</SCRIPT></head>
<body  leftmargin='0' topmargin='0' marginwidth='0' marginheight='0' background="login-background.jpg" >
<%com.ibm.ws.cache.servlet.DRSServlet drsservlet = com.ibm.ws.cache.servlet.DRSServlet.getInstance();%>
ServletInstanceWorkArea.jsp - <B><FONT COLOR="RED">Remember to check WebSphere logs for errors.</FONT></B>
<!--TABLE border='0' cellpadding='3' cellspacing='0' width='100%'height='0%' background='login-background.jpg'>
	<TBODY>
		<TR>
<TD NOWRAP-->

<BR>
<BR>
        
<A TARGET=workArea1
href="/dynacachetests/drs?quietMode=1&method=testServletCacheInstance_Clear&cacheName=services/cache/servletInstance_1">Clear</A>
<BR>
<BR>

<A TARGET=workArea1
href="/dynacachetests/STMTestServlet_shareNone">STMTestServlet_shareNone</A>
&nbsp;&nbsp;&nbsp;
<A TARGET=workArea1
href="/dynacachetests/drs?quietMode=1&method=testServletCacheInstance_Verify&cacheName=services/cache/servletInstance_1&cacheId=/dynacachetests/STMTestServlet_shareNone">Verify</A>

<BR>
<BR>

<A TARGET=workArea1
href="/dynacachetests/STMTestServlet_sharePush">STMTestServlet_sharePush</A>
&nbsp;&nbsp;&nbsp;
<A TARGET=workArea1
href="/dynacachetests/drs?quietMode=1&method=testServletCacheInstance_Verify&cacheName=services/cache/servletInstance_1&cacheId=/dynacachetests/STMTestServlet_sharePush">Verify</A>

<BR>
<BR>

<A TARGET=workArea1
href="/dynacachetests/STMTestServlet_sharePushPull">STMTestServlet_sharePushPull</A>
&nbsp;&nbsp;&nbsp;
<A TARGET=workArea1
href="/dynacachetests/drs?quietMode=1&method=testServletCacheInstance_Verify&cacheName=services/cache/servletInstance_1&cacheId=/dynacachetests/STMTestServlet_sharePushPull">Verify</A>

<BR>
<BR>

<A TARGET=workArea1
href="/dynacachetests/STMTestServlet_shareDefault">STMTestServlet_shareDefault</A>
&nbsp;&nbsp;&nbsp;
<A TARGET=workArea1
href="/dynacachetests/drs?quietMode=1&method=testServletCacheInstance_Verify&cacheName=services/cache/servletInstance_1&cacheId=/dynacachetests/STMTestServlet_shareDefault">Verify</A>

<BR>
<BR>



<IFRAME HEIGHT="92%" WIDTH="98%" FRAMEBORDER="1" NAME="workArea1">







