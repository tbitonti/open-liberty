<% out.println("in parent"); %>

<% request.setAttribute("SAFTimeStamp", new Long(System.currentTimeMillis())); %>

<jsp:forward page="SaveAttributesChild.jsp" />
