// 1.5, 9/9/04
// IBM Confidential OCO Source Material
// 5724-i63, 5724-H88 (C) COPYRIGHT International Business Machines Corp. 1997, 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.EntryInfo;
import com.ibm.ws.cache.DCacheBase;

public class DMapTestServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out;
        System.out.println("DMapTestServlet.doGet() - ENTRY");
        res.setContentType("text/html");
        out = res.getWriter();
        out.println("<html><body topmargin=\"20\" leftmargin=\"20\" bgcolor=\"#0066cc\">");
        out.println("<font color=\"#ffffff\">");
        out.println("<h1>DMapTestServlet</h1>");
        String action = req.getParameter("action");
        System.out.println("DMapTestServlet.doGet() - action-" + action);
        if (action != null) {
            out.println("<br>Action: " + action);
            String key = req.getParameter("key");
            String value = req.getParameter("value");
            String sharing = req.getParameter("sharing");
            String dSharing = req.getParameter("dSharing"); //TD- Dist. Map Global sharing policy
            String instance = req.getParameter("instance");
            String sTimeLimit = req.getParameter("timeLimit");

            if (instance == null)
                instance = DCacheBase.DEFAULT_DMAP_JNDI_NAME;
            out.println("<br>DMAP Instance: " + instance);
            int sharingPolicy = EntryInfo.NOT_SHARED;
            int dSharingPolicy = EntryInfo.NOT_SHARED; //TD- Dist. Map Global sharing policy
            int timeLimit = 300;
            if (sTimeLimit != null) {
                try {
                    timeLimit = Integer.parseInt(sTimeLimit);
                } catch (Exception e) {
                    timeLimit = 300;
                }
            }

//         DistributedMap map = DistributedMapFactory.getMap(instance);
            InitialContext context;
            DistributedMap map = null;
            System.out.println("DMapTestServlet.doGet() - instance-" + instance);
            try {
                context = new InitialContext();
                map = (DistributedMap) context.lookup(instance);
            } catch (Exception ex) {
                ex.printStackTrace(out);
            }
            if (map == null) {
                res.sendError(500, "*** Error: Cannot get a DistributedMap instance");
                return;
            }
            if (sharing == null) {
                sharingPolicy = EntryInfo.NOT_SHARED;
                sharing = "Not shared";
            } else if (sharing.equals("push"))
                sharingPolicy = EntryInfo.SHARED_PUSH;
            else if (sharing.equals("pull"))
                sharingPolicy = EntryInfo.SHARED_PULL;

            //TD - Distributed Map Global Sharing Policy
            if (dSharing == null) {
                dSharingPolicy = EntryInfo.NOT_SHARED;
                dSharing = "Not shared";
            } else if (dSharing.equals("push"))
                dSharingPolicy = EntryInfo.SHARED_PUSH;
            else if (dSharing.equals("pull"))
                dSharingPolicy = EntryInfo.SHARED_PULL;

            if (action.equals("put")) {
                if (key == null || value == null) {
                    res.sendError(500, "*** Error: Missing key or value parameter");
                    return;
                }
                out.println("<br>Key: " + key);
                out.println("<br>Value: " + value);
                out.println("<br>Sharing: " + sharing);
                out.println("<br>Distributed Map Global Sharing: " + map.getSharingPolicy());
                map.setSharingPolicy(dSharingPolicy); //TD- Dist. Map Global sharing policy
                map.put(key, value, 1, timeLimit, sharingPolicy, null);
                System.out.println(instance + "(" + key + "," + value + ") sharing " + sharing + " dsharing " + dSharing + " TTL " + sTimeLimit);
            } else if (action.equals("get")) {
                if (key == null) {
                    res.sendError(500, "*** Error: Missing key parameter");
                    return;
                }

                map.setSharingPolicy(dSharingPolicy); //TD
                out.println("Distributed Map Global Sharing Policy = " + map.getSharingPolicy());//TD

                out.println("<br>Key: " + key);
                value = (String) map.get(key);
                out.println("<br>Get Value: " + value);

            } else if (action.equals("clear")) {
                map.clear();
            } else if (action.equals("invalidate")) {
                map.invalidate(key);
            } else if (action.equals("timeout")) {
                try {
                    map.put("timeoutKey", "timeoutValue", -1, 30, EntryInfo.NOT_SHARED, null);

                    Thread.sleep(5000);

                    String timeOutValue = (String) map.get("timeoutKey");
                    if (timeOutValue == null) {
                        res.sendError(500, "timeoutValue not in cache");
                        return;
                    }

                    map.put("timeoutKey", timeOutValue, -1, 60, EntryInfo.NOT_SHARED, null);
                    Thread.sleep(30000);
                    timeOutValue = (String) map.get("timeoutKey");
                    if (timeOutValue == null) {
                        res.sendError(500, "timeOutValue does not exist in cache.");
                        return;
                    }
                    Thread.sleep(60000);
                    timeOutValue = (String) map.get("timeoutKey");
                    if (timeOutValue != null) {
                        res.sendError(500, "timeOutValue exists over its timeToLive");
                        return;
                    }
                } catch (Exception ex) {

                }

            }

            out.println("</body></html>");
        } else {
            res.sendError(500, "*** Error: Missing action parameter");
            return;
        }
    }
}
