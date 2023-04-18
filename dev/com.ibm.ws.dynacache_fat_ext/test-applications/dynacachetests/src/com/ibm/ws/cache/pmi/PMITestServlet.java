// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.pmi;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.pmi.stat.StatDescriptor;
import com.ibm.websphere.pmi.stat.StatLevelSpec;
import com.ibm.websphere.pmi.stat.WSCountStatistic;
import com.ibm.websphere.pmi.stat.WSStatistic;
import com.ibm.websphere.pmi.stat.WSStats;
import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.cache.intf.DCache;
import com.ibm.ws.cache.stat.internal.WSDynamicCacheStats;

public class PMITestServlet extends HttpServlet {

    public static MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

    private PrintWriter out;
    private boolean recursive = false;
    private final Hashtable hashtable = new Hashtable();
    private String[] cacheModules = { WSDynamicCacheStats.NAME };
    private ObjectName perfON = null;
    private String msg = "";

    //**
    //** init( ServletConfig config )
    //**
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    //**
    //** destroy()
    //**
    @Override
    public void destroy() {
        super.destroy();
    }

    //**
    //** doGet( HttpServletRequest req, HttpServletResponse resp )
    //**
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {
        processRequest(req, resp);
    }

    //**
    //** doPost( HttpServletRequest req, HttpServletResponse resp )
    //**
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {
        processRequest(req, resp);
    }

    //**
    //** processRequest( HttpServletRequest req, HttpServletResponse resp )
    //**
    public void processRequest(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {

        out = resp.getWriter();
        resp.setContentType("text/html");
        out.println("<html><body topmargin=\"20\" leftmargin=\"20\" text =\"#000000\" bgcolor=\"#FFFFFF\">");
        out.println("<center><h1>PMITestServlet</h1></center>");
        out.println("<br>CacheModules: " + getCacheModules());
        out.println("<br><b>Test selection:</b><br>");
        out.println("<br>");
        out.println("<b><a href=\"?method=none&host=localhost&port=8879&node=cachowdNode02&server=Member_1\">Setup variable for ND</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=none&host=localhost&port=8880&node=NONE&server=server1\">Setup variable for single server</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=enable\">Enable cache module PMI counters</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=verify\">Verify PMI counters</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=read\">Read PMI counters</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=getInstance&pmitree=cacheModule;cacheModule.servlet\">Get Servlet Instances</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=getInstance&pmitree=cacheModule;cacheModule.object\">Get Object Instances</a></b>");
        out.println("<br>");

        parseMBeanReqParmeters(req);
        final HttpServletRequest request = req;

        initialize();

        executeMethodParameter(request);

        out.println("</body></html>");
    }

    private void executeMethodParameter(HttpServletRequest req) {
        String method = req.getParameter("method");
        if (method != null) {

            if (method.equals("verify")) {
                loadCounterParm(req);
                if (!msg.equals("")) {
                    out.println("<br>" + msg);
                }
                doVerify();
                if (!msg.equals("")) {
                    out.println("<br>" + msg);
                }
            } else if (method.equals("read")) {
                doRead();
                if (!msg.equals("")) {
                    out.println("<br>" + msg);
                }
            } else if (method.equals("getInstances")) {
                doGetInstances();
                if (!msg.equals("")) {
                    out.println("<br>" + msg);
                }
            } else if (method.equals("enable")) {
                doEnable();
                if (!msg.equals("")) {
                    out.println("<br>" + msg);
                }
            }
        }
    }

    private void parseMBeanReqParmeters(HttpServletRequest req) {

        String tree = req.getParameter("pmitree");
        if (tree != null) {
            findCacheModules(tree);
            out.println("<br>New cacheModules: " + getCacheModules());
            System.out.println("New cacheModules: " + getCacheModules());
        }

        String nrecursive = req.getParameter("recursive");
        if (nrecursive != null) {
            if (nrecursive.equals("true")) {
                recursive = true;
            } else {
                recursive = false;
            }
            out.println("<br>New recursive: " + recursive);
            System.out.println("New recursive: " + recursive);
        }
    }

    public void doVerify() {

        msg = "";
        try {
            //StatDescriptor is used to identify a particular PMI module/statistics in PMI tree
            StatDescriptor[] sd = new StatDescriptor[1];

            // msd that represents dynamic cache module in PMI tree
            sd[0] = new StatDescriptor(cacheModules);

            // prepare Perf MBean query parameters
            Object[] params;
            String[] sig;

            //sig = new String[] {"[Ljavax.management.ObjectName;","java.lang.Boolean"};
            //params = new Object[] {targetMBean, new Boolean(true)}; // true => recursive; false => non-recursive

            sig = new String[] { "[Lcom.ibm.websphere.pmi.stat.StatDescriptor;", "java.lang.Boolean" };
            params = new Object[] { sd, new Boolean(true) }; // true => recursive; false => non-recursive

            // Invoke on Perf MBean and get statistics
            WSStats[] stat = (com.ibm.websphere.pmi.stat.WSStats[]) mbeanServer.invoke(perfON, "getStatsArray", params, sig);
            /*
             * // List all statistic names
             * if (stat != null && stat.length > 0) {
             * out.println ("<br> List of statistics available in parent:");
             * String[] statName = stat[0].listStatisticNames();
             * for (int i = 0; i < statName.length; i++)
             * out.println ("<br>" + statName[i]);
             * }
             * 
             * // Print all statistics
             * for (int i = 0; i < stat.length; i++)
             * out.println("<br>" + stat[i].toString() + "<br>");
             */

            Enumeration renum = hashtable.keys();
            while (renum.hasMoreElements()) {
                Integer key = (Integer) renum.nextElement();
                String value = (String) hashtable.get(key);
                int cid = key.intValue();
                long expCount;
                try {
                    expCount = Long.parseLong(value);
                } catch (Exception e) {
                    msg = "Error: doVerify (" + value + ")" + getStackTrace(e);
                    return;
                }
                WSCountStatistic wscs = (WSCountStatistic) stat[0].getStatistic(cid);
                if (wscs != null) {
                    long count = wscs.getCount();
                    if (count != expCount) {
                        msg = "Error: doVerify - expected count=" + expCount + " *** " + wscs.toString();
                        return;
                    }
                } else {
                    msg = "Error: doVerify - no count(" + cid + ")";
                    return;
                }
            }
            msg = "ReturnOutput: OK";
        } catch (Exception e) {
            msg = "Error: doVerify - " + getStackTrace(e);
            return;
        }
    }

    public void doRead() {

        msg = "ReturnOutput: ";

        //StatDescriptor is used to identify a particular PMI module/statistics in PMI tree
        StatDescriptor[] sd = new StatDescriptor[1];

        // msd that represents dynamic cache module in PMI tree
        sd[0] = new StatDescriptor(cacheModules);

        // prepare Perf MBean query parameters

        String[] sig = new String[] { "[Lcom.ibm.websphere.pmi.stat.StatDescriptor;", "java.lang.Boolean" };
        Object[] params = new Object[] { sd, Boolean.FALSE }; // true => recursive; false => non-recursive
        // try up to 3 times to get the stats
        for (int i = 0; i < 3; i++) {
            getStats(params, sig);
            if (!msg.contains("None"))
                break;
            try {
                Thread.sleep(20000);
            } catch (Exception e) {
                msg = "Error: doRead - " + getStackTrace(e);
            }
        }

    }

    private void getStats(Object[] params, String[] sig) {
        try {
            // Invoke on Perf MBean and get statistics
            WSStats[] stat = (WSStats[]) mbeanServer.invoke(perfON, "getStatsArray", params, sig);

            if (stat != null && stat.length > 0) {
                if (stat[0] != null) {
                    WSStatistic[] wss = stat[0].getStatistics();
                    if (wss != null && wss.length > 0) {
                        for (int i = 0; i < wss.length; i++) {
                            int id = wss[i].getId();
                            WSCountStatistic wscs = (WSCountStatistic) stat[0].getStatistic(id);
                            if (wscs != null) {
                                long count = wscs.getCount();
                                msg = msg + id + "=" + count + ";";
                            }
                        }
                        return;
                    } else {
                        msg = msg + "None-WSStatistic array is null or length is 0";
                        return;
                    }
                } else {
                    msg = msg + "None-WSStat array is null or length is 0";
                }
            } else {
                msg = msg + "None-stat[0] is null is 0";
            }

        } catch (Exception e) {
            msg = "Error: doRead - " + getStackTrace(e);
            return;
        }
    }

    public void doGetInstances() {

        msg = "ReturnOutput: ";
        try {
            String[] sig = new String[] { "com.ibm.websphere.pmi.stat.StatDescriptor", "java.lang.Boolean" };
            Object[] params = new Object[] { new StatDescriptor(cacheModules), new Boolean(recursive) };

            // Invoke on Perf MBean and get servlet instances
            StatDescriptor[] stat = (StatDescriptor[]) mbeanServer.invoke(perfON, "listStatMembers", params, sig);
            if (stat != null) {
                for (int i = 0; i < stat.length; i++) {
                    String ci = stat[i].toString();
                    int index = ci.indexOf(cacheModules[1]);
                    if (index > 0) {
                        ci = ci.substring(index + cacheModules[1].length() + 1);
                        if (recursive) {
                            msg = msg + ci + ";";
                        } else {
                            DCache cache = ServerCache.getCache(ci);
                            if (cache != null) {
                                int cs = cache.getMaxNumberCacheEntries();
                                msg = msg + ci + "=" + cs + ";";
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            msg = "Error: doGetInstances - " + getStackTrace(e);
            return;
        }
    }

    public void doEnable() {

        msg = "";
        try {
            //TODO: Use the DynacachePMIClient

            /*
             * enable all PMI statistics. This is a bit heavy weight. In the future consider
             * using setCustomString method on the perf MBean
             */

            StatLevelSpec[] spec = new StatLevelSpec[1];
            spec[0] = new StatLevelSpec(cacheModules, new int[] { StatLevelSpec.ALL_STATISTICS });
            String[] signature = new String[] { "[Lcom.ibm.websphere.pmi.stat.StatLevelSpec;", "java.lang.Boolean" };
            Object[] params = new Object[] { spec, new Boolean(true) };
            mbeanServer.invoke(perfON, "setInstrumentationLevel", params, signature);

            msg = "ReturnOutput: OK";

        } catch (Exception e) {
            msg = "Error: doEnable - " + getStackTrace(e);
            return;
        }
    }

    public void initialize() {
        msg = "";

        System.out.println("Entering initialize...");

        try {
            if (perfON == null) {
                out.println("<br> Querying Perf MBean...");
                ObjectName onQuery = new ObjectName("WebSphere:type=Perf,*");
                Set<ObjectName> mxBeanSet = mbeanServer.queryNames(onQuery, null);
                if (mxBeanSet != null && mxBeanSet.size() > 0) {
                    Iterator<ObjectName> it = mxBeanSet.iterator();
                    perfON = it.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "Error: initialize - " + getStackTrace(e);
            return;

        } finally {
            System.out.println(msg);
        }

        System.out.println("Exiting initialize...");

    }

    public void loadCounterParm(HttpServletRequest req) {

        hashtable.clear();

        msg = "loadCounterParm entry";

        //iterate though the parameters
        Enumeration paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {

            String paramName = (String) paramNames.nextElement(); //something like ServletMaxInMemoryCacheEntryCount
            Object paramValue = req.getParameterValues(paramName); //value is 2000

            //we will be using binary search to get to the WSDynaCacheStat ID            
            Arrays.sort(PMIHelper.cacheCountersName);

            if (null != paramName) {
                int cacheStatID = Arrays.binarySearch(PMIHelper.cacheCountersName, paramName);
                if (-1 != cacheStatID) {
                    hashtable.put(new Integer(cacheStatID), paramValue);
                    msg = "loaded PMI counter: " + paramName;
                } else {
                    msg = "loadCounterParm >" + paramName + " not found in PMIHelper.cacheCountersName";
                }
            } else {
                msg = "loadCounterParm > parameter name was null";
            }
        }

        msg = "loadCounterParm exited";

    }

    // Helper method to list all mbeans in the appserver
    private void listMBeans() throws Exception {

        ObjectName o = new ObjectName("WebSphere:*");

        out.println("<br> querying" + o.toString() + mbeanServer.getMBeanCount());
        Set s = mbeanServer.queryNames(o, null);

        Object[] on = s.toArray();
        for (int i = 0; i < on.length; i++) {
            out.println("<br>" + ((ObjectName) on[i]).getKeyProperty("type"));
        }
    }

    private void findCacheModules(String tree) {
        boolean finish = false;
        ArrayList array = new ArrayList();
        do {
            int index = tree.indexOf(";");
            if (index > 0) {
                String s = tree.substring(0, index);
                tree = tree.substring(index + 1);
                array.add(s);
            } else if (index == 0) {
                tree = tree.substring(1);
            } else {
                array.add(tree);
                finish = true;
            }
        } while (finish == false);

        if (array.size() > 0) {
            Object[] oa = array.toArray();
            cacheModules = new String[oa.length];
            for (int i = 0; i < oa.length; i++) {
                cacheModules[i] = (String) oa[i];
            }
        }

    }

    private String getCacheModules() {
        String ret = "";
        for (int i = 0; i < cacheModules.length; i++) {
            if (i == (cacheModules.length - 1)) {
                ret = ret + cacheModules[i];
            } else {
                ret = ret + cacheModules[i] + ";";
            }
        }
        return ret;
    }

    private String getStackTrace(Throwable oThrowable) {
        if (oThrowable == null)
            return null;
        StringWriter oStringWriter = new StringWriter();
        PrintWriter oPrintWriter = new PrintWriter(oStringWriter);
        oThrowable.printStackTrace(oPrintWriter);

        return oStringWriter.toString();
    }
}
