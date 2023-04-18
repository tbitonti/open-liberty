<%@ page import="com.ibm.websphere.cache.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<html>
<body topmargin="20" leftmargin="20" bgcolor="#0066cc" text="#ffffff">
<font color="#ffffff">
<h1>MapTest</h1>
<table>
<form name="myform" action="maptest.jsp" method="GET">
    <tr>
        <td>Action</td>
        <td><SELECT NAME="action" SIZE=1>
               <OPTION>get</OPTION>
               <OPTION SELECTED>put</OPTION>
               <OPTION>clear</OPTION>
               <OPTION>display</OPTION>
               <OPTION>invalidate</OPTION>
            </SELECT>
        </td>
    </tr>

    <tr>
       <td>Key</td>
       <td><INPUT NAME="key" TYPE="text" VALUE="testkey"></td>
    </tr>
    <tr>
       <td>Value</td>
       <td><INPUT NAME="value" TYPE="text" VALUE="testvalue"></td>
    </tr>
    <tr>
       <td>Replication</td>
       <td><SELECT NAME="sharing" SIZE=1>
               <OPTION SELECTED>none</OPTION>
               <OPTION>push</OPTION>
               <OPTION>pull</OPTION>
            </SELECT>
       </td>
    </tr>
    <tr>
       <td>Cache Instance</td>
       <td><SELECT NAME="instance" SIZE=1>
               <OPTION SELECTED>default</OPTION>
               <OPTION>cache one</OPTION>
               <OPTION>cache two</OPTION>
            </SELECT>
       </td>
    </tr>
    <tr><td><INPUT NAME="Submit" Value="Submit" TYPE="submit"></td></tr>
</form>
</table>
<%
    try {
      String action = request.getParameter("action");
      if (action != null) {
         out.println("<br>Action: "+action);
         String key = request.getParameter("key");
         String value = request.getParameter("value");
         String sharing = request.getParameter("sharing");
         String instance = request.getParameter("instance");
         int sharingPolicy = EntryInfo.NOT_SHARED;
         out.println("<br>Instance: "+instance);
         DistributedMap map = DistributedMapFactory.getMap(instance);
         if (sharing == null)
            sharingPolicy = EntryInfo.NOT_SHARED;
         else if (sharing.equals("push"))
            sharingPolicy = EntryInfo.SHARED_PUSH;
         else if (sharing.equals("pull"))
            sharingPolicy = EntryInfo.SHARED_PULL;
         if (action.equals("put")) {
            out.println("<br>Key: "+key);
            out.println("<br>Value: "+value);
            map.put(key,value,1,-1,sharingPolicy,null);
         } else if (action.equals("get")) {
            out.println("<br>Key: "+key);
//            value = (String) map.get(key,sharingPolicy);
            value = (String) map.get(key); //TODO
            out.println("<br>Get Value: "+value);
         } else if (action.equals("clear")) {
           map.clear();
         } else if (action.equals("invalidate")) {
           map.invalidate(key);
         }
         String maps[]= {"default","cache one","cache two"};
         for (int i=0;i<maps.length;i++) {
            out.println("<br><br>Map contents for "+maps[i]+":<br>");
            DistributedMap dmap = DistributedMapFactory.getMap(maps[i]);
            Iterator it = dmap.keySet().iterator();
            out.println("<table>");
            while(it.hasNext()) {
               out.println("<tr>");
               String key1 = (String) it.next();
               String value1 = (String) dmap.get(key1);
               out.println("<td>"+key1+"</td>");
               out.println("<td>"+value1+"</td>");
               out.println("</tr>");
            }
            out.println("</table>");
         }
      }
    } catch (Throwable t) {
       t.printStackTrace(new PrintWriter(out));
    }
%>

</html>
