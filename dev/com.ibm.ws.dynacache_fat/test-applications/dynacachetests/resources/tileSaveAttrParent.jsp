<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>


<% out.println("in parent"); %>

<% request.setAttribute("SAFTimeStamp", new Long(System.currentTimeMillis())); %>

<tiles:insert page="SaveAttributesChild.jsp" flush="true"/>
