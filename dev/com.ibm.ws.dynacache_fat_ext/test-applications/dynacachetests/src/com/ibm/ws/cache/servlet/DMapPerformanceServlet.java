// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.EntryInfo;
import com.ibm.ws.cache.spi.DistributedMapFactory;

//import com.ibm.ws.cache.spi.DistributedMapFactory;
//import java.util.Properties;

//------------------------------------------------------------
//
//------------------------------------------------------------ 
public class DMapPerformanceServlet extends HttpServlet {
    private PrintWriter out = null;
    private final Random rand = new Random();
    private boolean interactiveMode = false;
    private int shareType = EntryInfo.NOT_SHARED;
    private int globalShareType = EntryInfo.NOT_SHARED;
    private int shareTypeLast = -99;
    private int ratio = 8; // 6 for 60/40, 8 for 80/20, 9 for 90/10
    private boolean getShareType = false;
    private boolean getMapType = false;
    private boolean getGlobalShareType = false;

    private DistributedMap[] distributedMaps = null;
    private final Thread[] threads = null;
    private int mapCount = 1;

    //------------------------------------------------------------
    public void testThroughputDistributedMap() //throws Exception
    //------------------------------------------------------------
    {
        distributedMaps = new DistributedMap[mapCount];
        for (int i = 0; i < mapCount; i++) {
            try {
                distributedMaps[i] = DistributedMapFactory.getMap("services/cache/distributedmap_" + i);
            } catch (Exception e) {
                out.println(getStackTrace(e));
            }

            distributedMaps[i].setSharingPolicy(globalShareType);
            distributedMaps[i].clear();
        }

        PerformanceThread[] threads = new PerformanceThread[mapCount];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new PerformanceThread(distributedMaps[i], "thread:" + i, 5000, ratio, 10 - ratio);
        }

        long start1 = System.currentTimeMillis();
        try {
            for (int i = 0; i < threads.length; i++) {
                threads[i].start();
            }
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
        } catch (Exception e) {
            out.println(getStackTrace(e));
        }

        long end1 = System.currentTimeMillis();

        double throughput = ((50000)) / ((double) ((end1 - start1)) / 1000);
        double totalThroughput = ((mapCount * 50000)) / ((double) ((end1 - start1)) / 1000);

        if (interactiveMode) {
            out.println("Time to complete test : " + (end1 - start1) + " ms");
            out.println("<br>");
            out.println("Throughput per DistributedMap : " + throughput + "transactions/sec");
            out.println("<br>");
            out.println("Total Throughput for " + mapCount + " DistributedMaps : " + totalThroughput + "transactions/sec");
        } else {
            out.println(throughput);
            out.println(totalThroughput);
        }

        for (int i = 0; i < mapCount; i++) {
            distributedMaps[i].clear();
        }

        threads = null;
        distributedMaps = null;

    }

    //------------------------------------------------------------
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    //------------------------------------------------------------
    {
        boolean error = false;

        if (!error) {
            super.service(request, response);
        }

    }

    //------------------------------------------------------------
    private void sendException(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException
    //------------------------------------------------------------
    {
        out = response.getWriter();
        if (interactiveMode) {
            out.println("<br>");
            out.println(getStackTrace(e));
            out.println("<br>");
        } else {
            out.print("\n\n\nDRSServlet exception\n\n\n" + getStackTrace(e) + "\n\n\n");
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    //------------------------------------------------------------
    {
        //---------------------------------------------
        // Check for required parms
        //---------------------------------------------
        String method = request.getParameter("method");
        if (method == null) {
            method = "defaultAction";
        }
        //---------------------------------------------

        interactiveMode = request.getParameter("quietMode") == null ? true : false;
        getShareType = request.getParameter("shareType") != null ? true : false;
        getMapType = request.getParameter("mapType") != null ? true : false;
        getGlobalShareType = request.getParameter("globalShareType") != null ? true : false;

        response.setContentType("text/html");

        if (interactiveMode)
            out = response.getWriter();

        //---------------------------------------------
        // Sharing type?
        //---------------------------------------------
        if (getShareType) { // DMAP put share type
            String tmp = request.getParameter("shareType");
            if (tmp.equalsIgnoreCase("NONE")) {
                shareType = EntryInfo.NOT_SHARED;
            } else if (tmp.equalsIgnoreCase("PUSH")) {
                shareType = EntryInfo.SHARED_PUSH;
            } else if (tmp.equalsIgnoreCase("PULL")) {
                shareType = EntryInfo.SHARED_PULL;
            } else if (tmp.equalsIgnoreCase("PUSH-PULL")) {
                shareType = EntryInfo.SHARED_PUSH_PULL;
            }
            if (shareTypeLast != shareType) {
                System.out.println("SHARE TYPE CHANGED FROM " + shareTypeLast + " to " + shareType);
                shareTypeLast = shareType;
            }
        }
        if (getGlobalShareType) { // DMAP global share type
            String tmp = request.getParameter("globalShareType");
            if (tmp.equalsIgnoreCase("NONE")) {
                globalShareType = EntryInfo.NOT_SHARED;
            } else if (tmp.equalsIgnoreCase("PUSH")) {
                globalShareType = EntryInfo.SHARED_PUSH;
            } else if (tmp.equalsIgnoreCase("PULL")) {
                globalShareType = EntryInfo.SHARED_PULL;
            } else if (tmp.equalsIgnoreCase("PUSH-PULL")) {
                globalShareType = EntryInfo.SHARED_PUSH_PULL;
            }
        }
        //---------------------------------------------

        //---------------------------------------------
        // Ping request?
        //---------------------------------------------
        if (method.equalsIgnoreCase("ping")) {
            if (!interactiveMode)
                out = response.getWriter();
            out.print("pong");
            return;
        }
        //---------------------------------------------

        //---------------------------------------------
        // Check for parm sets
        //---------------------------------------------

        String tmp = request.getParameter("mapCount");
        if (null != tmp) {
            mapCount = new Integer(tmp).intValue();
        }

        tmp = request.getParameter("ratio");
        if (null != tmp) {
            ratio = new Integer(tmp).intValue();
        }

        //---------------------------------------------

        try {
            //-------------------------------------------
            // Helper URLs
            //-------------------------------------------
            if (interactiveMode) {
                out.println("PMServlet - REMEMBER TO CHECK WEBSPHERE LOGS FOR ERRORS!!!");
                out.println("<br>");
                out.println(rand.nextInt() + " " + System.currentTimeMillis() + " ");
                out.println("<br>");

                out.println("<a href=\"?method=testThroughputDistributedMap&&ttl=30\">testThroughputDistributedMap</a>");
                out.println("<br>");

                out.println("<hr>");

                out.println("Set MapCount (current=<b>"
                            + mapCount
                            + "</b>) <nobr><a href=\"?mapCount=1\">&nbsp;1&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?mapCount=100\">&nbsp;100&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?mapCount=200\">&nbsp;200&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?mapCount=400\">&nbsp;400&nbsp;</a></nobr> ");
                out.println("<br>");

                out.println("Set Hit/Miss ratio (current=<b>"
                            + getRatio(ratio)
                            + "</b>) <nobr><a href=\"?ratio=6\">&nbsp;60/40&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?ratio=8\">&nbsp;80/20&nbsp;</a>&nbsp;&nbsp;&nbsp;<a href=\"?ratio=9\">&nbsp;90/10&nbsp;</a></nobr>");
                out.println("<br>");

                out.println("Set DMap Global share type (current=<b>"
                            + getShareType(globalShareType)
                            + "</b>) <nobr><a href=\"?globalShareType=NONE\">NONE</a>&nbsp;&nbsp;&nbsp;<a href=\"?globalShareType=PUSH\">PUSH</a>&nbsp;&nbsp;&nbsp;<a href=\"?globalShareType=PUSH-PULL\">PUSH-PULL</a>&nbsp;&nbsp;&nbsp;<a href=\"?globalShareType=PULL\">PULL</a></nobr>");
                out.println("<br>");

                out.println("Set DMap Put share type (current=<b>"
                            + getShareType(shareType)
                            + ")</b> <nobr><a href=\"?shareType=NONE\">NONE</a>&nbsp;&nbsp;&nbsp;<a href=\"?shareType=PUSH\">PUSH</a>&nbsp;&nbsp;&nbsp;<a href=\"?shareType=PUSH-PULL\">PUSH-PULL</a>&nbsp;&nbsp;&nbsp;<a href=\"?shareType=PULL\">PULL</a></nobr>");
                out.println("<br>");

                out.println("<br>");

            }
            //-------------------------------------------

            //---------------------------------------------
            // Execute the request
            //---------------------------------------------
            this.getClass().getMethod(method, null).invoke(this, null);
            //---------------------------------------------

            if (!interactiveMode)
                out = response.getWriter();
            out.print("done");

        } catch (Exception e) {
            if (interactiveMode) {
                out.println("<br>");
                out.println(getStackTrace(e));
                out.println("<br>");
            } else {
                out = response.getWriter();
                out.print("\n\n\nDMapPerformanceServlet exception\n\n\n" + getStackTrace(e) + "\n\n\n");
            }
        }
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void ping() throws Exception
    //------------------------------------------------------------
    {
        out.println("pong");
    }

    //------------------------------------------------------------

    //------------------------------------------------------------
    public void defaultAction()
    //------------------------------------------------------------
    {}

    //------------------------------------------------------------

    //-------------------------------------------------------------------------------------
    static String getStackTrace(Throwable e)
    //-------------------------------------------------------------------------------------
    {
        if (null == e) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    //-------------------------------------------------------------------------------------

    //-------------------------------------------------------------------------------------
    static String getShareType(int shareType)
    //-------------------------------------------------------------------------------------
    {
        String s = "unknown";
        switch (shareType) {
            case EntryInfo.NOT_SHARED: {
                s = "NONE";
                break;
            }
            case EntryInfo.SHARED_PUSH: {
                s = "PUSH";
                break;
            }
            case EntryInfo.SHARED_PUSH_PULL: {
                s = "PUSH-PULL";
                break;
            }
            case EntryInfo.SHARED_PULL: {
                s = "PULL";
                break;
            }
        }
        return s;
    }

    //-------------------------------------------------------------------------------------

    //-------------------------------------------------------------------------------------
    static String getRatio(int ratio)
    //-------------------------------------------------------------------------------------
    {
        String s = "Unknown";

        if (ratio == 6) {
            s = "60/40";
        } else if (ratio == 8) {
            s = "80/20";
        } else if (ratio == 9) {
            s = "90/10";
        }

        return s;
    }
    //-------------------------------------------------------------------------------------

}

//-------------------------------------------------------------------------------------

class PerformanceThread extends Thread {
    String name;
    Map map;
    int loops;
    int puts;
    int gets;

    PerformanceThread(Map map, String name, int loops, int gets, int puts) {
        this.map = map;
        this.name = name;
        this.loops = loops;
        this.puts = puts;
        this.gets = gets;

    }

    @Override
    public void run() {
        for (int k = 0; k < loops; k++) {
            for (int i = 0; i < 1; i++) {
                String id = name;
                String data = name + ": this is a test value:" + i;
                map.put(id, data);
            }

            for (int i = 0; i < gets; i++) {
                String id = name;
                String data = name + ": this is a test value:" + i;
                String newData = (String) map.get(id);
            }

            for (int i = 0; i < puts - 1; i++) {
                String id = name;
                String data = name + ": this is a test value:" + i;
                map.put(id, data);
            }

        }
    }
}
