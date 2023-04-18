<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
<HEAD>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ page 
language="java"
contentType="text/html; charset=ISO-8859-1"
pageEncoding="ISO-8859-1"
%>
<TITLE>stTestForm.jsp</TITLE>
</HEAD>
<BODY>
<P><BR>
<BR>
<BR>
<BR>
<BR>
</P>
<P> This is a test form</P>
<BR>

<html:form action="/login">
	<DIV align="center">
	<TABLE border="0">
		<TBODY align="center">

			<TR>
				<TH>userid</TH>
				<TD><html:text property="userid" /></TD>
			</TR>

			<TR>
				<TH>password</TH>
				<TD><html:password property="password" /></TD>
			</TR>
			<TR>
				<TD><html:submit property="submit" value="Submit" /></TD>
				<TD><html:reset /></TD>
			</TR>
		</TBODY>
	</TABLE>
	</DIV>
</html:form>
</BODY>
</HTML>

