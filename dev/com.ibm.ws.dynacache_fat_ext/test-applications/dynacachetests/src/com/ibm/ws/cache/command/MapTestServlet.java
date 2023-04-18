// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.EntryInfo;


public class MapTestServlet extends HttpServlet
{
    private static CommandMap commandMap = new CommandMap();

    public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        PrintWriter out;

        res.setContentType("text/html");
        out = res.getWriter();
        out.println("<html><body topmargin=\"20\" leftmargin=\"20\" bgcolor=\"#0066cc\">");
        out.println("<font color=\"#ffffff\">");
        out.println("<h1>MapTestServlet</h1>");
        String command = req.getParameter("action");
        String key = req.getParameter("key");
        String value = req.getParameter("value");
        String sharing = req.getParameter("sharing");
        int sharingPolicy = EntryInfo.NOT_SHARED;
        if (sharing == null)
           sharingPolicy = EntryInfo.NOT_SHARED;
        else if (sharing.equals("push"))
           sharingPolicy = EntryInfo.SHARED_PUSH;
        else if (sharing.equals("pull"))
           sharingPolicy = EntryInfo.SHARED_PULL;
        if (command.equals("put")) {
           commandMap.put(key,value,sharingPolicy);
        } else if (command.equals("get")) {
           value = (String) commandMap.get(key,sharingPolicy);
        } else if (command.equals("clear")) {
          commandMap.clear();
        } else {
           res.sendError(500,"missing action");
           return;
        }
        out.println("<table id=\"results\">");
        out.println("<tr><td>"+key+"</td><td>"+value+"</td></tr>");
        out.println("</table>");
        out.println("</body></html>");
    }
}
