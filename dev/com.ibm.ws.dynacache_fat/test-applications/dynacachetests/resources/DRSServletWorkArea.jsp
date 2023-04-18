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
DMapTestServlet - <B><FONT COLOR="RED">Remember to check WebSphere logs for errors.</FONT></B>
<!--TABLE border='0' cellpadding='3' cellspacing='0' width='100%'height='0%' background='login-background.jpg'>
	<TBODY>
		<TR>
<TD NOWRAP-->
			<TABLE border="1">         
				<TBODY>
                
					<TR>
						<TD><BR>Loop<BR>Count</TD>
						<TD><BR>TTL<BR>Sec</TD>
						<TD>Global<BR>TTL<BR>Sec</TD>
						<TD><BR>Share<BR>Type</TD>
						<TD>Global<BR>Share<BR>Type</TD>
						<TD><BR>Map<BR>Type</TD>
					</TR>
                    
                    <FORM METHOD="GET" ACTION="">
					<TR>
						<TD><INPUT type="text"
							name="loopCount" size="7" maxlength="5" value="<%=drsservlet.loopCount%>"></TD>
						
                        <TD><INPUT type="text" name="ttl" size="7" maxlength="5" value="<%=drsservlet.ttl%>"></TD>
						
                        <TD><INPUT type="text" name="globalTtl" size="7" maxlength="5" value="<%=drsservlet.globalTtl%>"></TD>
						
                        <TD><SELECT name="shareType">
				<OPTION value="NONE"     <%=drsservlet.shareType==drsservlet.SHARE_TYPE_NONE?"selected":""%>>None</OPTION>
				<OPTION value="PUSH"     <%=drsservlet.shareType==drsservlet.SHARE_TYPE_PUSH?"selected":""%>>Push</OPTION>
				<OPTION value="PULL"     <%=drsservlet.shareType==drsservlet.SHARE_TYPE_PULL?"selected":""%>>Pull</OPTION>
				<OPTION value="PUSH-PULL"<%=drsservlet.shareType==drsservlet.SHARE_TYPE_PUSH_PULL?"selected":""%>>Push Pull</OPTION>            
				<OPTION></OPTION>
			</SELECT></TD>
            
                        <TD><SELECT name="globalShareType">
				<OPTION value="NONE"     <%=drsservlet.globalShareType==drsservlet.SHARE_TYPE_NONE?"selected":""%>>None</OPTION>
				<OPTION value="PUSH"     <%=drsservlet.globalShareType==drsservlet.SHARE_TYPE_PUSH?"selected":""%>>Push</OPTION>
				<OPTION value="PULL"     <%=drsservlet.globalShareType==drsservlet.SHARE_TYPE_PULL?"selected":""%>>Pull</OPTION>
				<OPTION value="PUSH-PULL"<%=drsservlet.globalShareType==drsservlet.SHARE_TYPE_PUSH_PULL?"selected":""%>>Push Pull</OPTION>            
				<OPTION></OPTION>
			</SELECT></TD>
            
						<TD><SELECT name="mapType">
				<OPTION value="baseCache"<%=drsservlet.mapTypeCurrent==drsservlet.TYPE_BASE_CACHE?"selected":""%>>default</OPTION>
				<OPTION value="DMap_1"   <%=drsservlet.mapTypeCurrent==drsservlet.TYPE_DMap_1?"selected":""%>>DMap #1</OPTION>
				<OPTION value="DMap_2"   <%=drsservlet.mapTypeCurrent==drsservlet.TYPE_DMap_2?"selected":""%>>DMap #2</OPTION>
				<OPTION value="DMap_L1"  <%=drsservlet.mapTypeCurrent==drsservlet.TYPE_DMap_L1?"selected":""%>>DLockingMap</OPTION>
				<OPTION value="DMap_N1"  <%=drsservlet.mapTypeCurrent==drsservlet.TYPE_DMap_N1?"selected":""%>>DNioMap</OPTION>
				<OPTION value="DMap_OG1"  <%=drsservlet.mapTypeCurrent==drsservlet.TYPE_DMap_OG1?"selected":""%>>DMap OG #1</OPTION>
				<OPTION value="DMap_OG2"  <%=drsservlet.mapTypeCurrent==drsservlet.TYPE_DMap_OG2?"selected":""%>>DMap OG #2</OPTION>
				<OPTION></OPTION>
			</SELECT></TD>
						
						
					</TR>
                    
                    <TR>
                    
                    <TD ALIGN="RIGHT" COLSPAN=9>
                    <INPUT type="submit" name=" Set " value="Set">
                    </TD>
                    </FORM>
                    
                    </TR>
					
		</TBODY></TABLE>     
        

        
        
						<HR>


<TABLE border="0" cellspacing="0" cellpadding="0">


					<tr>
					<TD bgcolor="#000000">|</TD>

					<TD COLSPAN=2 align="center">
					String Keys<br>Serializable Value
					</TD>
					
					<TD bgcolor="#000000">|</TD>
										
					<TD COLSPAN=2 align="center">
					String Keys<br>Non-Serializable Value
					</TD>
					
					<TD bgcolor="#000000">|</TD>
										
					<TD COLSPAN=2 align="center">
					Object Keys<br>Serializable Value
					</TD>
					
					<TD bgcolor="#000000">|</TD>
					
					<TD COLSPAN=2 align="center">
					MyObject Keys<br>Serializable Value
					</TD>
					
					<TD bgcolor="#000000">|</TD>
					
					<TD COLSPAN=2 align="center">
					My Object Dep
					</TD>
					
					<TD bgcolor="#000000">|</TD>
					
					<TD COLSPAN=2 align="center">
					Content
					</TD>
					
					<TD bgcolor="#000000">|</TD>
					
					<TD COLSPAN=2 align="center">
					DataIds Test<BR> 1p,1gb,2gb,1r,1ga,2ga<BR>1i,1ga,2ga
					</TD>
					
					<TD bgcolor="#000000">|</TD>
					
                    </tr>
					    
					<tr>
                    
					<TD bgcolor="#000000">|</TD>

                    <FORM method="GET" action="">
					<td COLSPAN=2>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializablePut">
<INPUT type="submit" value="Put">
					</td>
                    </FORM>
                    
                    <TD bgcolor="#000000">|</TD>
					
                    <FORM method="GET" action="">
					<td COLSPAN=2>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapNonSerializablePut">
<INPUT type="submit" value="Put">
					</td>
                    </FORM>
                    
                    <TD bgcolor="#000000">|</TD>
					
                    <FORM method="GET" action="">
                    <td COLSPAN=2>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableObjectKeyPut">
<INPUT type="submit" value="Put">
					</td>
                    </FORM>
                    
                    <TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
                    <td COLSPAN=2>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableMyObjectKeyPut">
<INPUT type="submit" value="Put">
					</td>
                    </FORM>
                    
                    <TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
                    <td COLSPAN=2>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableMyObjectDepKeyPut">
<INPUT type="submit" value="Put">
					</td>
                    </FORM>
                    
					<TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
                    <td COLSPAN=1>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableContent">
<INPUT TYPE="HIDDEN" NAME="cacheContent" VALUE="content_1">
<INPUT type="submit" value="Put 1">
					</td>
                    </FORM>
                    
                    
                    <FORM method="GET" action="">
                    <td COLSPAN=1>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableContent">
<INPUT TYPE="HIDDEN" NAME="cacheContent" VALUE="content_2">
<INPUT type="submit" value="Put 2">
					</td>
                    </FORM>
                    
					<TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
                    <td COLSPAN=1>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapDataIds_Put">
<INPUT type="submit" value="Put">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
                    <td COLSPAN=1>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapDataIds_Get_BeforeReplace">
<INPUT type="submit" value="GetBefore">
					</td>
                    </FORM>
                    
					<TD bgcolor="#000000">|</TD>
                    
					</tr>

					<tr>
					<TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableGet">
<INPUT type="submit" value="Get">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableGetNone">
<INPUT type="submit" value="Get None">
					</td>
                    </FORM>
                    
                    <TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapNonSerializableGet">
<INPUT type="submit" value="Get">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapNonSerializableGetNone">
<INPUT type="submit" value="Get None">
					</td>
                    </FORM>
                    
                    <TD bgcolor="#000000">|</TD>

                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableObjectKeyGet">
<INPUT type="submit" value="Get">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableObjectKeyGetNone">
<INPUT type="submit" value="Get None">
					</td>
                    </FORM>
                    
                    <TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableMyObjectKeyGet">
<INPUT type="submit" value="Get">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableMyObjectKeyGetNone">
<INPUT type="submit" value="Get None">
					</td>
                    </FORM>
                    
                    <TD bgcolor="#000000">|</TD>
                    
					<td>
					</td>
                    
					<td>
					</td>
                    
					<TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableGetContent">
<INPUT TYPE="HIDDEN" NAME="cacheContent" VALUE="content_1">
<INPUT type="submit" value="Get 1">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableGetContent">
<INPUT TYPE="HIDDEN" NAME="cacheContent" VALUE="content_2">
<INPUT type="submit" value="Get 2">
					</td>
                    </FORM>
                    
		            <TD bgcolor="#000000">|</TD>
        
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapDataIds_Replace">
<INPUT type="submit" value="Replace">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapDataIds_Get_AfterReplace">
<INPUT type="submit" value="Get After">
					</td>
                    </FORM>
                    
		            <TD bgcolor="#000000">|</TD>
        
                    
					</tr>

					<tr>
					<TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapInvalidate">
<INPUT type="submit" value="Inval">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapInvalidateNonBlocking">
<INPUT type="submit" value="Inval NB">
					</td>
                    </FORM>
                    
                    <TD bgcolor="#000000">|</TD>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapInvalidate">
<INPUT type="submit" value="Inval">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapInvalidateNonBlocking">
<INPUT type="submit" value="Inval NB">
					</td>
                    </FORM>

		<TD bgcolor="#000000">|</TD>

                    <FORM method="GET" action="">
                    <td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableObjectKeyInvalidate">
<INPUT type="submit" value="Inval">
					</td>
                    </FORM>
                    
					<td>
					</td>

		<TD bgcolor="#000000">|</TD>

                    <FORM method="GET" action="">
                    <td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapSerializableMyObjectKeyInvalidate">
<INPUT type="submit" value="Inval">
					</td>
                    </FORM>

					<td>
					</td>
                    
					<TD bgcolor="#000000">|</TD>
                    
		<FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapInvalidateMyObjectDepKey">
<INPUT type="submit" value="Inval">
					</td>
                    </FORM>
                    
					<td>
					</td>
					
					<TD bgcolor="#000000">|</TD>
                    
					<td>
					</td>
                    
					<td>
					</td>
					
					<TD bgcolor="#000000">|</TD>
                    
		<FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapDataIds_Invalidate">
<INPUT type="submit" value="Inval">
					</td>
                    </FORM>
                    
					<td>
					</td>

					<TD bgcolor="#000000">|</TD>
                    
                    </TR>


</TABLE>

						<HR>
Change Listener Tests

<TABLE border="0">

					<tr>
                    
                    <FORM method="GET" action="">
                    <TD>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapChangeListenerRegister">
<INPUT type="submit" value="Register">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
                    <TD>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapChangeListenerUnRegister">
<INPUT type="submit" value="Unregister">
					</td>
                    </FORM>
					
                    <FORM method="GET" action="">
                    <td COLSPAN=2>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapChangeListenerChange">
<INPUT type="submit" value="Change">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapChangeListenerVerify">
<INPUT type="submit" value="Verify">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapChangeListenerVerifyNone">
<INPUT type="submit" value="Verify None">
					</td>
                    </FORM>
                    
                    <%String type = drsservlet.getChangeSource(drsservlet.changeSource)+"_"+drsservlet.getChangeCause(drsservlet.changeCause);%>

					<FORM method="GET" action="">
					<TD>
					<SELECT name="changeType">
      				<OPTION value="LOCAL_NEW_ENTRY_ADDED"         <%=type.equals("LOCAL_NEW_ENTRY_ADDED")?"selected":""%>>LOCAL_NEW_ENTRY_ADDED</OPTION>
	  				<OPTION value="LOCAL_EXISTING_VALUE_CHANGED"  <%=type.equals("LOCAL_EXISTING_VALUE_CHANGED")?"selected":""%>>LOCAL_EXISTING_VALUE_CHANGED</OPTION>
					<OPTION value="REMOTE_NEW_ENTRY_ADDED"        <%=type.equals("REMOTE_NEW_ENTRY_ADDED")?"selected":""%>>REMOTE_NEW_ENTRY_ADDED</OPTION>
					<OPTION value="REMOTE_EXISTING_VALUE_CHANGED" <%=type.equals("REMOTE_EXISTING_VALUE_CHANGED")?"selected":""%>>REMOTE_EXISTING_VALUE_CHANGED</OPTION>
					<OPTION></OPTION>
					</SELECT>
					<INPUT type="submit" value="Set">
					</TD>
					</FORM>
                    
					</tr>

</TABLE>

Invalidation Listener Tests

<TABLE border="0">

					<tr>
                    
                    <FORM method="GET" action="">
                    <TD>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapListenerRegister">
<INPUT type="submit" value="Register">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
                    <TD>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapListenerUnRegister">
<INPUT type="submit" value="Unregister">
					</td>
                    </FORM>
					
                    <FORM method="GET" action="">
                    <td COLSPAN=2>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapListenerInvalidate">
<INPUT type="submit" value="Invalidate">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapListenerVerify">
<INPUT type="submit" value="Verify">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapListenerVerifyNone">
<INPUT type="submit" value="Verify None">
					</td>
                    </FORM>
                    
                    <%type = drsservlet.getInvalidationSource(drsservlet.invalidationSource)+"_"+drsservlet.getInvalidationCause(drsservlet.invalidationCause);%>

					<FORM method="GET" action="">
					<TD>
					<SELECT name="invalidationType">
      				<OPTION value="LOCAL_EXPLICIT"   <%=type.equals("LOCAL_EXPLICIT")?"selected":""%>>LOCAL_EXPLICIT</OPTION>
	  				<OPTION value="REMOTE_EXPLICIT"  <%=type.equals("REMOTE_EXPLICIT")?"selected":""%>>REMOTE_EXPLICIT</OPTION>
					<OPTION value="LOCAL_TIMEOUT"    <%=type.equals("LOCAL_TIMEOUT")?"selected":""%>>LOCAL_TIMEOUT</OPTION>
					<OPTION value="REMOTE_TIMEOUT"   <%=type.equals("REMOTE_TIMEOUT")?"selected":""%>>REMOTE_TIMEOUT</OPTION>
					<OPTION value="LOCAL_CLEAR_ALL"  <%=type.equals("LOCAL_CLEAR_ALL")?"selected":""%>>LOCAL_CLEAR_ALL</OPTION>
					<OPTION value="REMOTE_CLEAR_ALL" <%=type.equals("REMOTE_CLEAR_ALL")?"selected":""%>>REMOTE_CLEAR_ALL</OPTION>
					<OPTION></OPTION>
					</SELECT>
					<INPUT type="submit" value="Set">
					</TD>
					</FORM>
                    
					</tr>

</TABLE>

<HR>
Miscellaneous Tests

<TABLE border="0">

					<tr>
                    
                    <FORM method="GET" action="">
                    <TD>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="resetCache">
<INPUT type="submit" value="Clear <%=drsservlet.getMapType(drsservlet.mapTypeCurrent)%>">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapLock">
<INPUT type="submit" value="Lock Test">
					</td>
                    </FORM>
					
                    <FORM method="GET" action="">
                    <td COLSPAN=2>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapFactory">
<INPUT type="submit" value="Legacy Factory Test">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
                    <td COLSPAN=2>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedObjectCacheFactory">
<INPUT type="submit" value="Object Factory Test">
					</td>
                    </FORM>
                    
                    <FORM method="GET" action="">
					<td>
<INPUT TYPE="HIDDEN" NAME="method" VALUE="testDistributedMapStress">
<INPUT type="submit" value="Stress Test">
					</td>
                    </FORM>
                    
                    
					</tr>

</TABLE>

						<BR>

						<A
				href="?method=testDistributedMapNonSerializableObjectKeyPut">testDistributedMapNonSerializableObjectKeyPut</A>
						<BR>
						<A
				href="?method=testDistributedMapNonSerializableObjectKeyGet">testDistributedMapNonSerializableObjectKeyGet</A>
						<BR>
						<A
				href="?method=testDistributedMapNonSerializableObjectKeyInvalidate">testDistributedMapNonSerializableObjectKeyInvalidate</A>
						<BR>
						<A
				href="?method=testDistributedMapNonSerializableObjectKeyGetNone">testDistributedMapNonSerializableObjectKeyGetNone</A>
						<BR>
						<A
				href="?method=testRenounceForMixedMode">testRenounceForMixedMode</A>
						<BR>
			<!--/TD>
		</TR>
	</TBODY>
</TABLE -->
Running...<BR>





