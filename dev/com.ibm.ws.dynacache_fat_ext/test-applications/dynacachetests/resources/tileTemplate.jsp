
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>

<html:html>
<HEAD>

<TITLE>tileTemplate.jsp</TITLE>
</HEAD>
<BODY>
<tiles:insert page="/layout.jsp" flush="true">
	<tiles:put name="header" value="/header.jsp" />
	<tiles:put name="body" value="/body.jsp" />
	<tiles:put name="footer" value="/footer.jsp" />
</tiles:insert>
</BODY>
</html:html>
