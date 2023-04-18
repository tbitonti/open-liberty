<p>Begin Skip Cache Child 2</p>
<TABLE id="Child2TimeStamp"><TR><TD><%=System.currentTimeMillis()%></TD></TR></TABLE>
<%! String attrName; %>
<% java.util.Enumeration attrs = request.getAttributeNames(); %>
<h2>Child 2 Request attributes:</h2>
<ul>
<% while (attrs.hasMoreElements()) { 
   attrName = (String)attrs.nextElement(); %>
<li> <%= attrName %>: <%= request.getAttribute( attrName ) %> </li>
<% } %>
</ul>
<p>End Skip Cache Child 2</p>

