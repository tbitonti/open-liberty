<% String setAttributeValue = request.getParameter("setAttributeValue");
   if (setAttributeValue != null) {
      request.setAttribute("RangeTestAttribute", setAttributeValue);
   }
%>
<jsp:forward page="RangeAttrChild.jsp" />
