<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>


<BODY>
	<TABLE border="0">
	<TBODY>
	<TR>
	<TD colspan="2"><tiles:insert attribute="header"/></TD>
	</TR>
	<TR>	
	<TD width="70%" valign="top"><tiles:insert attribute="body"/></TD>
	</TR>
	<TR>
	<TD colspan="2"><tiles:insert attribute="footer"/></TD>
	</TR>
	</TBODY>
	</TABLE>
</BODY>
</HTML>
