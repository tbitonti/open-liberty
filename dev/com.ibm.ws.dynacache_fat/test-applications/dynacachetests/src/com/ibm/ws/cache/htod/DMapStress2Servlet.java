// 1.8, 2/5/08
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.htod;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.DistributedMap;
import com.ibm.ws.cache.DCacheBase;
import com.ibm.ws.cache.EntryInfo;
import com.ibm.ws.cache.MyObjectSizer;
import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.cache.intf.CacheStatisticsListener;
import com.ibm.ws.cache.intf.DCache;
import com.ibm.wsspi.cache.DistributedObjectCacheFactory;

public class DMapStress2Servlet extends HttpServlet {

    private DCache cacheServlet = null;
    private DCache cacheDmap = null;
    private DistributedMap dmap = null;
    private MyEventListener2Impl listenerServlet = null;
    private MyEventListener2Impl listenerDmap = null;

    private String msg = "";
    private PrintWriter out;

    private int valueSize = 1250;
    private int threads = 1;
    private int delay = 0; // put delay
    private int ddelay = 0; // dep id invalidate delay
    private int tdelay = 0; // template invalidate delay
    private int cdelay = 0; // clear delay
    private int sdelay = 0; // stop delay
    private int adelay = 0; // alias delay
    private int ldelay = 10000; // listener delay
    private int delayAT = 10000; // delay after test
    private int num_depids = 100;
    private int win = 100;
    private String idName = "";
    private String serverName = "";
    private int serverPort = 9080;
    private boolean waitInv = true;
    private boolean oneDepid = false;
    private boolean invOneDepid = false;
    private boolean ilistener = false;
    private boolean clistener = false;
    private boolean same = false; // use put1 and put2 use the same cache id
    private boolean idd = false; // use cache id as depid
    private String xdepid = "";
    private boolean clearBT = true;
    private boolean savCount = true;
    private boolean resetCount = false;
    private boolean resetPMI = false;
    private int exps = -1;
    private int expps = -1;
    private int expd = -1;
    private int exppd = -1;
    private boolean useSizer = false;
    private String cacheServletName = DCacheBase.DEFAULT_BASE_JNDI_NAME;
    private String cacheDmapName = DCacheBase.DEFAULT_DMAP_JNDI_NAME;

    private long sSavCacheHits = 0;
    private long sSavCacheMisses = 0;
    private long sSavCacheRemoves = 0;
    private long sSavCacheLruRemoves = 0;
    private long sSavExplicitInvalidationsFromMemory = 0;
    private long sSavExplicitInvalidationsFromDisk = 0;
    private long sSavExplicitInvalidationsLocal = 0;
    private long sSavExplicitInvalidationsRemote = 0;
    private long sSavTimeoutInvalidationsFromMemory = 0;
    private long sSavTimeoutInvalidationsFromDisk = 0;
    private long sSavGarbageCollectorInvalidationsFromDisk = 0;
    private long sSavOverflowInvalidationsFromDisk = 0;
    private long sSavDepIdsOffloadedToDisk = 0;
    private long sSavDepIdBasedInvalidationsFromDisk = 0;
    private long sSavTemplatesOffloadedToDisk = 0;
    private long sSavTemplateBasedInvalidationsFromDisk = 0;
    private long sSavObjectsReadFromDisk = 0;
    private long sSavObjectsReadFromDisk4K = 0;
    private long sSavObjectsReadFromDisk40K = 0;
    private long sSavObjectsReadFromDisk400K = 0;
    private long sSavObjectsReadFromDisk4000K = 0;
    private long sSavObjectsReadFromDiskSize = 0;
    private long sSavObjectsWriteToDisk = 0;
    private long sSavObjectsWriteToDisk4K = 0;
    private long sSavObjectsWriteToDisk40K = 0;
    private long sSavObjectsWriteToDisk400K = 0;
    private long sSavObjectsWriteToDisk4000K = 0;
    private long sSavObjectsWriteToDiskSize = 0;
    private long sSavObjectsDeleteFromDisk = 0;
    private long sSavObjectsDeleteFromDisk4K = 0;
    private long sSavObjectsDeleteFromDisk40K = 0;
    private long sSavObjectsDeleteFromDisk400K = 0;
    private long sSavObjectsDeleteFromDisk4000K = 0;
    private long sSavObjectsDeleteFromDiskSize = 0;
    private long sSavRemoteInvalidationNotifications = 0;
    private long sSavRemoteUpdateNotifications = 0;
    private long sSavRemoteObjectUpdates = 0;
    private long sSavRemoteObjectUpdateSize = 0;
    private long sSavRemoteObjectHits = 0;
    private long sSavRemoteObjectFetchSize = 0;
    private long sSavRemoteObjectMisses = 0;

    private boolean dmapUsed = false;
    // 1st digit: loop
    // 2nd digit: items
    // 3rd digit: sharingPolicy
    // 4th digit: timeToTife
    // 5th digit: put (1)
    // 6th digit: get (1)
    // 7th digit: invalidate dep ids
    // 8th digit: invalidate template
    // 9th digit: clear
    // 10th digit: put (2)
    // 11th digit: get (2)
    // 12th digit: stop server
    // 13th digit: addAlias
    // 14th digit: removeAlias
    // 15th digit: random - used by Test3
    // 16th digit; 0=servlet cache or 1=dmap
    private String threadParms = "1_5000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;" + //thread0
                                 "5_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;" + //thread1
                                 "5_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;" + //thread2
                                 "5_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;" + //thread3
                                 "5_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;" + //thread4
                                 "5_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;" + //thread5
                                 "5_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;" + //thread6
                                 "5_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;" + //thread7
                                 "5_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;" + //thread8
                                 "5_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;"; //thread9
    private int[][] threadParmArray = null;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        boolean resetAlready = false;
        this.dmapUsed = false;
        out = response.getWriter();
        response.setContentType("text/html");
        out.println("<html><body topmargin=\"20\" leftmargin=\"20\" text =\"#000000\" bgcolor=\"#FFFFFF\">");
        //out.println("<font color=\"#ffffff\">");
        out.println("<center><h1>DMapServlet</h1></center>");
        out.println("<br><b>Test selection:</b><br>");
        out.println("<br>");
        out.println("<b><a href=\"?method=test1\">Test1</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=test2\">Test2</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=test3\">Test3 - random</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=test4\">Test4 - random (all threads using same id)</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=restart\">Restart</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=init\">Initiate the cache instance</a></b>");
        out.println("<br>");

        this.serverName = request.getServerName();
        this.serverPort = request.getServerPort();

        String tmp = request.getParameter("cacheServletName");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.cacheServletName = tmp;
        }
        tmp = request.getParameter("cacheDmapName");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.cacheDmapName = tmp;
        }
        tmp = request.getParameter("valueSize");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.valueSize = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("threads");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.threads = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("win");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.win = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("waitInv");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.waitInv = true;
            } else {
                this.waitInv = false;
            }
        }
        tmp = request.getParameter("ilistener");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.ilistener = true;
            } else {
                this.ilistener = false;
            }
        }
        tmp = request.getParameter("clistener");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.clistener = true;
            } else {
                this.clistener = false;
            }
        }
        tmp = request.getParameter("same");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.same = true;
            } else {
                this.same = false;
            }
        }
        tmp = request.getParameter("idd");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.idd = true;
            } else {
                this.idd = false;
            }
        }
        tmp = request.getParameter("clearBT");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.clearBT = true;
            } else {
                this.clearBT = false;
            }
        }
        tmp = request.getParameter("savCount");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.savCount = true;
            } else {
                this.savCount = false;
            }
        }
        tmp = request.getParameter("resetCount");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.resetCount = true;
            } else {
                this.resetCount = false;
            }
        }
        tmp = request.getParameter("resetPMI");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.resetPMI = true;
            } else {
                this.resetPMI = false;
            }
        }
        tmp = request.getParameter("idName");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.idName = tmp;
        }
        if (idName.equals("")) {
            idName = this.serverName + ":" + this.serverPort;
        }
        tmp = request.getParameter("oneDepid");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.oneDepid = true;
                this.xdepid = idName + ":dep-id";
            } else {
                this.oneDepid = false;
            }
        }
        tmp = request.getParameter("useSizer");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            if (tmp.equalsIgnoreCase("true")) {
                this.useSizer = true;
            } else {
                this.useSizer = false;
            }
        }
        threadParmArray = new int[threads][16];
        tmp = request.getParameter("threadParms");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.threadParms = tmp;
        }
        if (this.threads > 0) {
            if (parse(this.threadParms) == false) {
                out.println("<br> error: " + msg);
                out.println("</body></html>");
                return;
            }

        }
        tmp = request.getParameter("delay");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.delay = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("ddelay");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.ddelay = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("tdelay");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.tdelay = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("cdelay");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.cdelay = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("sdelay");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.sdelay = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("adelay");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.adelay = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("ldelay");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.ldelay = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("delayAT");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.delayAT = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("depids");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.num_depids = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("exps");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.exps = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("expps");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.expps = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("expd");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.expd = new Integer(tmp).intValue();
        }
        tmp = request.getParameter("exppd");
        if (tmp != null) {
            if (!resetAlready) {
                resetParameters();
                resetAlready = true;
            }
            this.exppd = new Integer(tmp).intValue();
        }

        out.println("<br> idName=" + this.idName);
        out.println("<br> valueSize=" + this.valueSize);
        out.println("<br> threads=" + this.threads);
        out.println("<br> delay=" + this.delay);
        out.println("<br> ddelay=" + this.ddelay);
        out.println("<br> tdelay=" + this.tdelay);
        out.println("<br> cdelay=" + this.cdelay);
        out.println("<br> sdelay=" + this.sdelay);
        out.println("<br> adelay=" + this.adelay);
        out.println("<br> ldelay=" + this.ldelay);
        out.println("<br> delayAT=" + this.delayAT);
        out.println("<br> depids=" + this.num_depids);
        out.println("<br> win=" + this.win);
        out.println("<br> waitInv=" + this.waitInv);
        out.println("<br> oneDepid=" + this.oneDepid);
        out.println("<br> ilistener=" + this.ilistener);
        out.println("<br> clistener=" + this.clistener);
        out.println("<br> same=" + this.same);
        out.println("<br> idd=" + this.idd);
        out.println("<br> clearBT=" + this.clearBT);
        out.println("<br> savCount=" + this.savCount);
        out.println("<br> resetCount=" + this.resetCount);
        out.println("<br> resetPMI=" + this.resetPMI);
        out.println("<br> useSizer=" + this.useSizer);
        out.println("<br> exps=" + this.exps);
        out.println("<br> expps=" + this.expps);
        out.println("<br> expd=" + this.expd);
        out.println("<br> exppd=" + this.exppd);

        if (ilistener || clistener) {
            if (listenerServlet == null) {
                listenerServlet = new MyEventListener2Impl("ListenerServlet", this.win);
            } else {
                listenerServlet.reset();
            }
            if (listenerDmap == null) {
                listenerDmap = new MyEventListener2Impl("ListenerDmap", this.win);
            } else {
                listenerDmap.reset();
            }
        }

        //set the caches
        if (!cacheServletName.equalsIgnoreCase("none")) {
            setCacheServlet();
        }
        if (!cacheDmapName.equalsIgnoreCase("none")) {
            setDmap();
            setCacheDmap();
        }

        if (cacheServlet == null && dmap == null) {
            out.println("<br> Test failure: cacheServlet and dmap = null");
            out.println("</body></html>");
            return;
        }

        //NEED TO CLEAR CACHE BEFORE CALLING resetPMI()
        if (this.clearBT) {
            if (cacheServlet != null) {
                cacheServlet.clear();
            }
            if (dmap != null) {
                dmap.clear();
            }
        }
        if (this.resetPMI) {
            if (cacheServlet != null) {
                cacheServlet.resetPMICounters();
            }
            if (cacheDmap != null) {
                cacheDmap.resetPMICounters();
            }
        }

        saveCountersBeforeTest();

        if (this.ilistener || this.clistener) {
            if (cacheServlet != null) {
                cacheServlet.enableListener(true);
            }
            if (dmap != null) {
                dmap.enableListener(true);
            }
        }
        if (this.ilistener) {
            if (cacheServlet != null) {
                cacheServlet.addInvalidationListener(listenerServlet);
            }
            if (dmap != null) {
                dmap.addInvalidationListener(listenerDmap);
            }
        }
        if (this.clistener) {
            if (cacheServlet != null) {
                cacheServlet.addChangeListener(listenerServlet);
            }
            if (dmap != null) {
                dmap.addChangeListener(listenerDmap);
            }
        }

        String method = request.getParameter("method");
        if (method != null) {

            if (method.equals("test1")) {
                runTest1();
            } else if (method.equals("test2")) {
                runTest2();
            } else if (method.equals("test3")) {
                runTest3();
            } else if (method.equals("test4")) {
                runTest4();
            } else if (method.equals("init")) {
            }
            if (!msg.equals("")) {
                out.println("<br> Test failure: " + msg);
            }
        }
        out.println("</body></html>");
    }

    private void setCacheServlet() {
        //Force a look up everytime        
        System.out.println("Looking up:" + cacheServletName);
        cacheServlet = ServerCache.getConfiguredCache(cacheServletName);
        if (cacheServlet == null) {
            System.out.println("Could not find:" + cacheServletName);
            com.ibm.websphere.cache.Cache ca = com.ibm.websphere.cache.DynamicCacheAccessor.getCache();
            cacheServlet = (com.ibm.ws.cache.intf.DCache) ca;
            System.out.println("Instantiated cacheServlet:" + cacheServlet.getCacheName());
        } else {
            System.out.println("Retrieved cacheServlet:" + cacheServlet.getCacheName());
        }
    }

    private void setDmap() {

        try {
            System.out.println("Looking up:" + cacheDmapName);
            Context ctx = new InitialContext();
            dmap = (DistributedMap) ctx.lookup(cacheDmapName);

            if (dmap == null) {
                System.out.println("Lookup failed, cannot get a DistributedMap instance");
                out.println("Lookup failed, cannot get a DistributedMap instance");
            }
        } catch (NamingException ne) {
            out.println("Lookup resulted in an Exception  for DistributedMap instance." + ne.getMessage());
            System.out.println("Lookup resulted in an Exception for DistributedMap instance." + ne.toString());
        }

        if (dmap == null) {
            System.out.println("Could not find:" + cacheDmapName);
            dmap = DistributedObjectCacheFactory.getMap(DCacheBase.DEFAULT_DISTRIBUTED_MAP_NAME);
        }
        System.out.println("Retrieved dmap:" + dmap);
    }

    private void setCacheDmap() {
        cacheDmap = ServerCache.getCache(cacheDmapName);
        if (cacheDmap == null) {
            cacheDmap = ServerCache.getCache(DCacheBase.DEFAULT_DISTRIBUTED_MAP_NAME);
        }
        System.out.println("Retrieved cacheDmap:" + cacheDmap);
    }

    public void runTest1() {
        msg = "";
        out.println("<br>Running Test1...<br>");
        System.out.println("Running Test1...");

        if (!cacheServletName.equalsIgnoreCase("none") && cacheServlet == null) {
            msg = "runTest1.1 Can't get an instance of cache";
            return;
        }

        if (!cacheDmapName.equalsIgnoreCase("none") && dmap == null) {
            msg = "runTest1.2 Can't get an instance of dmap";
            return;
        }

        if (this.threads > 0) {
            TesterThread[] tthreads = new TesterThread[this.threads];

            int j = 0;
            for (int i = 0; i < tthreads.length; i++) {
                boolean put1 = false;
                boolean get1 = false;
                boolean dinv = false;
                boolean tinv = false;
                boolean clear = false;
                boolean put2 = false;
                boolean get2 = false;
                boolean stop = false;
                boolean addAlias = false;
                boolean removeAlias = false;
                boolean usedmap = false;
                if (j == 10) {
                    j = 0;
                }
                if (this.threadParmArray[j][4] == 1) {
                    put1 = true;
                }
                if (this.threadParmArray[j][5] == 1) {
                    get1 = true;
                }
                if (this.threadParmArray[j][12] == 1) {
                    addAlias = true;
                }
                if (this.threadParmArray[j][13] == 1) {
                    removeAlias = true;
                }
                if (this.threadParmArray[j][15] == 1) {
                    usedmap = true;
                }
                tthreads[i] = new TesterThread(this.idName + ":thread:" + i, this.threadParmArray[j][0], this.threadParmArray[j][1], this.threadParmArray[j][2], this.threadParmArray[j][3], put1, get1, dinv, tinv, clear, put2, get2, stop, addAlias, removeAlias, usedmap);
                j++;
            }

            long start = System.currentTimeMillis();
            try {
                for (int i = 0; i < tthreads.length; i++) {
                    tthreads[i].start();
                }
                for (int i = 0; i < tthreads.length; i++) {
                    tthreads[i].join();
                }
            } catch (Exception e) {
                out.println("<br> error: " + getDCStackTrace(e));
            }
            long end = System.currentTimeMillis();
            long totalTimePut1 = 0;
            long totalTimeGet1 = 0;
            long totalTimePut2 = 0;
            long totalTimeGet2 = 0;
            for (int i = 0; i < tthreads.length; i++) {
                totalTimePut1 += tthreads[i].timePut1;
                totalTimeGet1 += tthreads[i].timeGet1;
                totalTimePut2 += tthreads[i].timePut2;
                totalTimeGet1 += tthreads[i].timeGet2;
            }
            System.out.println("*** Time to propagate the cache : " + (end - start) + " ms" + " totalTimePut1=" + totalTimePut1 + " totalTimeGet1=" + totalTimeGet1
                               + " totalTimePut2=" + totalTimePut2 + "  totalTimeGet2=" + totalTimeGet2);
            out.println("<br> Time to propagate the cache : " + (end - start) + " ms" + " totalTimePut1=" + totalTimePut1 + " totalTimeGet1=" + totalTimeGet1 + " totalTimePut2="
                        + totalTimePut2 + "  totalTimeGet2=" + totalTimeGet2);

            j = 0;
            for (int i = 0; i < tthreads.length; i++) {
                boolean dinv = false;
                boolean tinv = false;
                boolean clear = false;
                boolean put2 = false;
                boolean get2 = false;
                boolean stop = false;
                boolean addAlias = false;
                boolean removeAlias = false;
                boolean usedmap = false;
                if (j == 10) {
                    j = 0;
                }
                if (this.threadParmArray[j][6] == 1) {
                    dinv = true;
                }
                if (this.threadParmArray[j][7] == 1) {
                    tinv = true;
                }
                if (this.threadParmArray[j][8] == 1) {
                    clear = true;
                }
                if (this.threadParmArray[j][9] == 1) {
                    put2 = true;
                }
                if (this.threadParmArray[j][10] == 1) {
                    get2 = true;
                }
                if (this.threadParmArray[j][11] == 1) {
                    stop = true;
                }
                if (this.threadParmArray[j][12] == 1) {
                    addAlias = true;
                }
                if (this.threadParmArray[j][13] == 1) {
                    removeAlias = true;
                }
                if (this.threadParmArray[j][15] == 1) {
                    usedmap = true;
                }
                tthreads[i] = new TesterThread(this.idName + ":thread:" + i, this.threadParmArray[j][0], this.threadParmArray[j][1], this.threadParmArray[j][2], this.threadParmArray[j][3], false, false, dinv, tinv, clear, put2, get2, stop, addAlias, removeAlias, usedmap);
                j++;
            }

            try {
                for (int i = 0; i < tthreads.length; i++) {
                    tthreads[i].start();
                }
                for (int i = 0; i < tthreads.length; i++) {
                    tthreads[i].join();
                }
            } catch (Exception e) {
                out.println("<br> error: " + getDCStackTrace(e));
            }
            end = System.currentTimeMillis();
            System.out.println("*** Time to complete test : " + (end - start) + " ms");
            out.println("<br> Time to complete test : " + (end - start) + " ms");
            tthreads = null;
        }
        waitAndGetResult();
    }

    public void runTest2() {
        msg = "";
        out.println("<br>Running Test2...<br>");
        System.out.println("Running Test2...");

        if (cacheServlet == null) {
            msg = "runTest2.1 Can't get an instance of cache or dmap";
            return;
        }

        TesterThread[] tthreads = new TesterThread[this.threads];

        int j = 0;
        for (int i = 0; i < tthreads.length; i++) {
            boolean put1 = false;
            boolean get1 = false;
            boolean dinv = false;
            boolean tinv = false;
            boolean clear = false;
            boolean put2 = false;
            boolean get2 = false;
            boolean stop = false;
            boolean addAlias = false;
            boolean removeAlias = false;
            boolean usedmap = false;
            if (j == 10) {
                j = 0;
            }
            if (this.threadParmArray[j][4] == 1) {
                put1 = true;
            }
            if (this.threadParmArray[j][5] == 1) {
                get1 = true;
            }
            if (this.threadParmArray[j][6] == 1) {
                dinv = true;
            }
            if (this.threadParmArray[j][7] == 1) {
                tinv = true;
            }
            if (this.threadParmArray[j][8] == 1) {
                clear = true;
            }
            if (this.threadParmArray[j][9] == 1) {
                put2 = true;
            }
            if (this.threadParmArray[j][10] == 1) {
                get2 = true;
            }
            if (this.threadParmArray[j][11] == 1) {
                stop = true;
            }
            if (this.threadParmArray[j][12] == 1) {
                addAlias = true;
            }
            if (this.threadParmArray[j][13] == 1) {
                removeAlias = true;
            }
            if (this.threadParmArray[j][15] == 1) {
                usedmap = true;
            }
            tthreads[i] = new TesterThread(this.idName + ":thread:" + i, this.threadParmArray[j][0], this.threadParmArray[j][1], this.threadParmArray[j][2], this.threadParmArray[j][3], put1, get1, dinv, tinv, clear, put2, get2, stop, addAlias, removeAlias, usedmap);
            j++;
        }

        long start = System.currentTimeMillis();
        try {
            for (int i = 0; i < tthreads.length; i++) {
                tthreads[i].start();
            }
            for (int i = 0; i < tthreads.length; i++) {
                tthreads[i].join();
            }
        } catch (Exception e) {
            out.println("<br> error: " + getDCStackTrace(e));
        }
        long end = System.currentTimeMillis();
        System.out.println("*** Time to complete test : " + (end - start) + " ms");
        out.println("<br> Time to complete test : " + (end - start) + " ms");

        tthreads = null;
    }

    public void runTest3() {
        msg = "";
        out.println("<br>Running Test3...<br>");
        System.out.println("Running Test3...");

        if (cacheServlet == null) {
            msg = "runTest3.1 Can't get an instance of cache or dmap";
            return;
        }

        TesterThread2[] tthreads = new TesterThread2[this.threads];

        int j = 0;
        for (int i = 0; i < tthreads.length; i++) {
            boolean get1 = false;
            boolean dinv = false;
            boolean tinv = false;
            boolean clear = false;
            boolean stop = false;
            boolean rand = false;
            boolean usedmap = false;
            if (j == 10) {
                j = 0;
            }
            if (this.threadParmArray[j][5] == 1) {
                get1 = true;
            }
            if (this.threadParmArray[j][6] == 1) {
                dinv = true;
            }
            if (this.threadParmArray[j][7] == 1) {
                tinv = true;
            }
            if (this.threadParmArray[j][8] == 1) {
                clear = true;
            }
            if (this.threadParmArray[j][11] == 1) {
                stop = true;
            }
            if (this.threadParmArray[j][14] == 1) {
                rand = true;
            }
            if (this.threadParmArray[j][15] == 1) {
                usedmap = true;
            }
            tthreads[i] = new TesterThread2(i, this.idName + ":thread:" + i, this.threadParmArray[j][0], this.threadParmArray[j][1], this.threadParmArray[j][2], this.threadParmArray[j][3], rand, get1, dinv, tinv, clear, stop, usedmap);
            j++;
        }

        long start = System.currentTimeMillis();
        try {
            for (int i = 0; i < tthreads.length; i++) {
                tthreads[i].start();
            }
            for (int i = 0; i < tthreads.length; i++) {
                tthreads[i].join();
            }
        } catch (Exception e) {
            out.println("<br> error: " + getDCStackTrace(e));
        }
        long end = System.currentTimeMillis();
        System.out.println("*** Time to complete test : " + (end - start) + " ms");
        out.println("<br> Time to complete test : " + (end - start) + " ms");

        tthreads = null;
    }

    public void runTest4() {
        msg = "";
        out.println("<br>Running Test4...<br>");
        System.out.println("Running Test4...");

        if (cacheServlet == null) {
            msg = "runTest4.1 Can't get an instance of cache or dmap";
            return;
        }

        TesterThread2[] tthreads = new TesterThread2[this.threads];

        int j = 0;
        for (int i = 0; i < tthreads.length; i++) {
            boolean get1 = false;
            boolean dinv = false;
            boolean tinv = false;
            boolean clear = false;
            boolean stop = false;
            boolean rand = false;
            boolean usedmap = false;
            if (j == 10) {
                j = 0;
            }
            if (this.threadParmArray[j][5] == 1) {
                get1 = true;
            }
            if (this.threadParmArray[j][6] == 1) {
                dinv = true;
            }
            if (this.threadParmArray[j][7] == 1) {
                tinv = true;
            }
            if (this.threadParmArray[j][8] == 1) {
                clear = true;
            }
            if (this.threadParmArray[j][11] == 1) {
                stop = true;
            }
            if (this.threadParmArray[j][14] == 1) {
                rand = true;
            }
            if (this.threadParmArray[j][15] == 1) {
                usedmap = true;
            }
            tthreads[i] = new TesterThread2(i, this.idName, this.threadParmArray[j][0], this.threadParmArray[j][1], this.threadParmArray[j][2], this.threadParmArray[j][3], rand, get1, dinv, tinv, clear, stop, usedmap);
            j++;
        }

        long start = System.currentTimeMillis();
        try {
            for (int i = 0; i < tthreads.length; i++) {
                tthreads[i].start();
            }
            for (int i = 0; i < tthreads.length; i++) {
                tthreads[i].join();
            }
        } catch (Exception e) {
            out.println("<br> error: " + getDCStackTrace(e));
        }
        long end = System.currentTimeMillis();
        long totalTimePut1 = 0;
        long totalTimeGet1 = 0;
        long totalPutCount = 0;
        long totalGetCount = 0;
        for (int i = 0; i < tthreads.length; i++) {
            totalTimePut1 += tthreads[i].timePut1;
            totalTimeGet1 += tthreads[i].timeGet1;
            totalPutCount += tthreads[i].putCount;
            totalGetCount += tthreads[i].getCount;
        }
        System.out.println("*** Time to complete test : " + (end - start) + " ms" + " totalTimePut1=" + totalTimePut1 + " totalTimeGet1=" + totalTimeGet1 + " totalPutCount="
                           + totalPutCount + "  totalGetCount=" + totalGetCount);
        out.println("<br> Time to complete test : " + (end - start) + " ms" + " totalTimePut1=" + totalTimePut1 + " totalTimeGet1=" + totalTimeGet1 + " totalPutCount="
                    + totalPutCount + "  totalGetCount=" + totalGetCount);

        tthreads = null;
    }

    private void saveCountersBeforeTest() {
        CacheStatisticsListener csl = null;
        if (this.resetCount) {
            if (cacheServlet != null) {
                csl = cacheServlet.getCacheStatisticsListener();
                csl.reset();
            }
            if (cacheDmap != null) {
                csl = cacheDmap.getCacheStatisticsListener();
                csl.reset();
            }
        }
        if (this.savCount) {
            if (cacheServlet != null) {
                csl = cacheServlet.getCacheStatisticsListener();
                this.sSavCacheHits = csl.getCacheHitsCount();
                this.sSavCacheMisses = csl.getCacheMissesCount();
                this.sSavCacheRemoves = csl.getCacheRemovesCount();
                this.sSavCacheLruRemoves = csl.getCacheLruRemovesCount();
                this.sSavExplicitInvalidationsFromMemory = csl.getExplicitInvalidationsFromMemoryCount();
                this.sSavExplicitInvalidationsFromDisk = csl.getExplicitInvalidationsFromDiskCount();
                this.sSavExplicitInvalidationsLocal = csl.getExplicitInvalidationsLocalCount();
                this.sSavExplicitInvalidationsRemote = csl.getExplicitInvalidationsRemoteCount();
                this.sSavTimeoutInvalidationsFromMemory = csl.getTimeoutInvalidationsFromMemoryCount();
                this.sSavTimeoutInvalidationsFromDisk = csl.getTimeoutInvalidationsFromDiskCount();
                this.sSavGarbageCollectorInvalidationsFromDisk = csl.getGarbageCollectorInvalidationsFromDiskCount();
                this.sSavOverflowInvalidationsFromDisk = csl.getOverflowInvalidationsFromDiskCount();
                this.sSavDepIdsOffloadedToDisk = csl.getDepIdsOffloadedToDiskCount();
                this.sSavDepIdBasedInvalidationsFromDisk = csl.getDepIdBasedInvalidationsFromDiskCount();
                this.sSavTemplatesOffloadedToDisk = csl.getTemplatesOffloadedToDiskCount();
                this.sSavTemplateBasedInvalidationsFromDisk = csl.getTemplateBasedInvalidationsFromDiskCount();
                this.sSavObjectsReadFromDisk = csl.getObjectsReadFromDiskCount();
                this.sSavObjectsReadFromDisk4K = csl.getObjectsReadFromDisk4KCount();
                this.sSavObjectsReadFromDisk40K = csl.getObjectsReadFromDisk40KCount();
                this.sSavObjectsReadFromDisk400K = csl.getObjectsReadFromDisk400KCount();
                this.sSavObjectsReadFromDisk4000K = csl.getObjectsReadFromDisk4000KCount();
                this.sSavObjectsReadFromDiskSize = csl.getObjectsReadFromDiskSizeCount();
                this.sSavObjectsWriteToDisk = csl.getObjectsWriteToDiskCount();
                this.sSavObjectsWriteToDisk4K = csl.getObjectsWriteToDisk4KCount();
                this.sSavObjectsWriteToDisk40K = csl.getObjectsWriteToDisk40KCount();
                this.sSavObjectsWriteToDisk400K = csl.getObjectsWriteToDisk400KCount();
                this.sSavObjectsWriteToDisk4000K = csl.getObjectsWriteToDisk4000KCount();
                this.sSavObjectsWriteToDiskSize = csl.getObjectsWriteToDiskSizeCount();
                this.sSavObjectsDeleteFromDisk = csl.getObjectsDeleteFromDiskCount();
                this.sSavObjectsDeleteFromDisk4K = csl.getObjectsDeleteFromDisk4KCount();
                this.sSavObjectsDeleteFromDisk40K = csl.getObjectsDeleteFromDisk40KCount();
                this.sSavObjectsDeleteFromDisk400K = csl.getObjectsDeleteFromDisk400KCount();
                this.sSavObjectsDeleteFromDisk4000K = csl.getObjectsDeleteFromDisk4000KCount();
                this.sSavObjectsDeleteFromDiskSize = csl.getObjectsDeleteFromDiskSizeCount();
                this.sSavRemoteInvalidationNotifications = csl.getRemoteInvalidationNotificationsCount();
                this.sSavRemoteUpdateNotifications = csl.getRemoteUpdateNotificationsCount();
                this.sSavRemoteObjectUpdates = csl.getRemoteObjectUpdatesCount();
                this.sSavRemoteObjectUpdateSize = csl.getRemoteObjectUpdateSizeCount();
                this.sSavRemoteObjectHits = csl.getRemoteObjectHitsCount();
                this.sSavRemoteObjectFetchSize = csl.getRemoteObjectFetchSizeCount();
                this.sSavRemoteObjectMisses = csl.getRemoteObjectMissesCount();
            }
        } else {
            this.sSavCacheHits = 0;
            this.sSavCacheMisses = 0;
            this.sSavCacheRemoves = 0;
            this.sSavCacheLruRemoves = 0;
            this.sSavExplicitInvalidationsFromMemory = 0;
            this.sSavExplicitInvalidationsFromDisk = 0;
            this.sSavExplicitInvalidationsLocal = 0;
            this.sSavExplicitInvalidationsRemote = 0;
            this.sSavTimeoutInvalidationsFromMemory = 0;
            this.sSavTimeoutInvalidationsFromDisk = 0;
            this.sSavGarbageCollectorInvalidationsFromDisk = 0;
            this.sSavOverflowInvalidationsFromDisk = 0;
            this.sSavDepIdsOffloadedToDisk = 0;
            this.sSavDepIdBasedInvalidationsFromDisk = 0;
            this.sSavTemplatesOffloadedToDisk = 0;
            this.sSavTemplateBasedInvalidationsFromDisk = 0;
            this.sSavObjectsReadFromDisk = 0;
            this.sSavObjectsReadFromDisk4K = 0;
            this.sSavObjectsReadFromDisk40K = 0;
            this.sSavObjectsReadFromDisk400K = 0;
            this.sSavObjectsReadFromDisk4000K = 0;
            this.sSavObjectsReadFromDiskSize = 0;
            this.sSavObjectsWriteToDisk = 0;
            this.sSavObjectsWriteToDisk4K = 0;
            this.sSavObjectsWriteToDisk40K = 0;
            this.sSavObjectsWriteToDisk400K = 0;
            this.sSavObjectsWriteToDisk4000K = 0;
            this.sSavObjectsWriteToDiskSize = 0;
            this.sSavObjectsDeleteFromDisk = 0;
            this.sSavObjectsDeleteFromDisk4K = 0;
            this.sSavObjectsDeleteFromDisk40K = 0;
            this.sSavObjectsDeleteFromDisk400K = 0;
            this.sSavObjectsDeleteFromDisk4000K = 0;
            this.sSavObjectsDeleteFromDiskSize = 0;
            this.sSavRemoteInvalidationNotifications = 0;
            this.sSavRemoteUpdateNotifications = 0;
            this.sSavRemoteObjectUpdates = 0;
            this.sSavRemoteObjectUpdateSize = 0;
            this.sSavRemoteObjectHits = 0;
            this.sSavRemoteObjectFetchSize = 0;
            this.sSavRemoteObjectMisses = 0;
        }
    }

    private void waitAndGetResult() {
        if (this.delayAT > 0) {
            System.out.println("*** delayAT start delayAT=" + delayAT / 1000 + " sec");
            if (exps != -1 || expd != -1 || expps != -1 || exppd != -1) {
                int currentWait = 0;
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    currentWait += 1000;
                    if (currentWait >= this.delayAT) {
                        System.out.println("**** Warning - delayAT timeout");
                        break;
                    }
                    if (exps != -1 && expd != -1) {
                        boolean bs = false;
                        boolean bd = false;
                        if (cacheServlet.getActualIdsSizeDisk() == exps) {
                            if (cacheServlet.getIdsSizeDisk() == cacheServlet.getActualIdsSizeDisk()) {
                                bs = true;
                            }
                        }
                        if (cacheDmap.getActualIdsSizeDisk() == expd) {
                            if (cacheDmap.getIdsSizeDisk() == cacheDmap.getActualIdsSizeDisk()) {
                                bd = true;
                            }
                        }
                        if (bs && bd) {
                            break;
                        }
                    } else if (exps != -1) {
                        if (cacheServlet.getActualIdsSizeDisk() == exps) {
                            if (cacheServlet.getIdsSizeDisk() == cacheServlet.getActualIdsSizeDisk()) {
                                break;
                            }
                        }
                    } else if (expps != -1) {
                        if (cacheServlet.getPushPullTableSize() == expps) {
                            if (cacheServlet.getIdsSizeDisk() == cacheServlet.getActualIdsSizeDisk()) {
                                break;
                            }
                        }
                    } else if (expd != -1) {
                        if (cacheDmap.getActualIdsSizeDisk() == expd) {
                            if (cacheDmap.getIdsSizeDisk() == cacheDmap.getActualIdsSizeDisk()) {
                                break;
                            }
                        }
                    } else if (exppd != -1) {
                        if (cacheDmap.getPushPullTableSize() == exppd) {
                            if (cacheDmap.getIdsSizeDisk() == cacheDmap.getActualIdsSizeDisk()) {
                                break;
                            }
                        }
                    }
                } while (currentWait > 0);
            } else {
                try {
                    Thread.sleep(delayAT);
                } catch (Exception e) {
                }
            }
            System.out.println("*** delayAT end");
        }
        if (this.ilistener) {
            //if (ldelay > 0) {
            //System.out.println("After test listener delay=" + ldelay/1000 + " sec");
            //    try {
            //        Thread.sleep(ldelay);
            //    } catch (Exception e) {
            //    }
            //}
            String output = listenerServlet.getInvalidationLocalInfo();
            System.out.println(output);
            out.println("<br> " + output);
            output = listenerServlet.getInvalidationRemoteInfo();
            System.out.println(output);
            out.println("<br> " + output);
            if (this.dmapUsed) {
                output = listenerDmap.getInvalidationLocalInfo();
                System.out.println(output);
                out.println("<br> " + output);
                output = listenerDmap.getInvalidationRemoteInfo();
                System.out.println(output);
                out.println("<br> " + output);
            }
        }

        if (this.ilistener) {
            cacheServlet.removeInvalidationListener(listenerServlet);
            dmap.removeInvalidationListener(listenerDmap);
        }
        if (this.clistener) {
            cacheServlet.removeChangeListener(listenerServlet);
            dmap.removeChangeListener(listenerDmap);
        }
        if (this.ilistener || this.clistener) {
            cacheServlet.enableListener(false);
            dmap.enableListener(false);
        }

        TreeSet ts = new TreeSet();
        HashSet hs = new HashSet(10);
        Collection c = cacheServlet.getAllDependencyIds();
        if (c != null && c.size() > 0) {
            ts.addAll(c);
            hs.addAll(c);
        }
        if (ts.size() < 1000) {
            //out.println("<br> depIds in memory: " + ts);
            System.out.println("*** depIds for cacheName=" + cacheServletName + " in memory: " + ts);
        }

        ts.clear();
        c = cacheServlet.getDepIdsByRangeDisk(0, -1);
        if (c != null && c.size() > 0) {
            ts.addAll(c);
            hs.addAll(c);
        }
        if (ts.size() < 1000) {
            //out.println("<br> depIds on disk: " + ts);
            System.out.println("*** depIds for cacheName=" + cacheServletName + " on disk: " + ts);
        }

        int depIdSize = hs.size();
        CacheStatisticsListener csl = cacheServlet.getCacheStatisticsListener();
        StringBuffer resultBuffer = new StringBuffer();

        resultBuffer.append("***");
        resultBuffer.append(" serverName=");
        resultBuffer.append(this.serverName);
        resultBuffer.append(" serverPort=");
        resultBuffer.append(this.serverPort);
        resultBuffer.append(" Result: sMemEntriesSize=");
        resultBuffer.append(cacheServlet.getNumberCacheEntries());
        resultBuffer.append(" sDiskEntriesSize=");
        resultBuffer.append(cacheServlet.getIdsSizeDisk());
        resultBuffer.append(" sObjectsOnDisk=");
        resultBuffer.append(cacheServlet.getActualIdsSizeDisk());
        resultBuffer.append(" sDependencyIdsInMemoryAndDisk=");
        resultBuffer.append(depIdSize);
        resultBuffer.append(" sDependencyIdsOnDisk=");
        resultBuffer.append(cacheServlet.getDepIdsSizeDisk());
        resultBuffer.append(" sTemplatesOnDisk=");
        resultBuffer.append(cacheServlet.getTemplatesSizeDisk());
        resultBuffer.append(" sTotalCacheDataDiskSize=");
        resultBuffer.append(cacheServlet.getCacheSizeInBytesDisk());
        resultBuffer.append(" sCacheHits=");
        resultBuffer.append(csl.getCacheHitsCount() - sSavCacheHits);
        resultBuffer.append(" sCacheMisses=");
        resultBuffer.append(csl.getCacheMissesCount() - sSavCacheMisses);
        resultBuffer.append(" sCacheRemoves=");
        resultBuffer.append(csl.getCacheRemovesCount() - sSavCacheRemoves);
        resultBuffer.append(" sCacheLruRemoves=");
        resultBuffer.append(csl.getCacheLruRemovesCount() - sSavCacheLruRemoves);
        resultBuffer.append(" sExplicitInvalidationsFromMemory=");
        resultBuffer.append(csl.getExplicitInvalidationsFromMemoryCount() - sSavExplicitInvalidationsFromMemory);
        resultBuffer.append(" sExplicitInvalidationsFromDisk=");
        resultBuffer.append(csl.getExplicitInvalidationsFromDiskCount() - sSavExplicitInvalidationsFromDisk);
        resultBuffer.append(" sExplicitInvalidationsLocal=");
        resultBuffer.append(csl.getExplicitInvalidationsLocalCount() - sSavExplicitInvalidationsLocal);
        resultBuffer.append(" sExplicitInvalidationsRemote=");
        resultBuffer.append(csl.getExplicitInvalidationsRemoteCount() - sSavExplicitInvalidationsRemote);
        resultBuffer.append(" sTimeoutInvalidationsFromMemory=");
        resultBuffer.append(csl.getTimeoutInvalidationsFromMemoryCount() - sSavTimeoutInvalidationsFromMemory);
        resultBuffer.append(" sTimeoutInvalidationsFromDisk=");
        resultBuffer.append(csl.getTimeoutInvalidationsFromDiskCount() - sSavTimeoutInvalidationsFromDisk);
        resultBuffer.append(" sGarbageCollectorInvalidationsFromDisk=");
        resultBuffer.append(csl.getGarbageCollectorInvalidationsFromDiskCount() - sSavGarbageCollectorInvalidationsFromDisk);
        resultBuffer.append(" sOverflowInvalidationsFromDisk=");
        resultBuffer.append(csl.getOverflowInvalidationsFromDiskCount() - sSavOverflowInvalidationsFromDisk);
        resultBuffer.append(" sPendingRemovalFromDisk=");
        resultBuffer.append(cacheServlet.getPendingRemovalSizeDisk());
        resultBuffer.append(" sDependencyIdsBufferedForDisk=");
        resultBuffer.append(cacheServlet.getDepIdsBufferedSizeDisk());
        resultBuffer.append(" sDependencyIdsOffloadedToDisk=");
        resultBuffer.append(csl.getDepIdsOffloadedToDiskCount() - sSavDepIdsOffloadedToDisk);
        resultBuffer.append(" sDependencyIdBasedInvalidationsFromDisk=");
        resultBuffer.append(csl.getDepIdBasedInvalidationsFromDiskCount() - sSavDepIdBasedInvalidationsFromDisk);
        resultBuffer.append(" sTemplatesBufferedForDisk=");
        resultBuffer.append(cacheServlet.getTemplatesBufferedSizeDisk());
        resultBuffer.append(" sTemplatesOffloadedToDisk=");
        resultBuffer.append(csl.getTemplatesOffloadedToDiskCount() - sSavTemplatesOffloadedToDisk);
        resultBuffer.append(" sTemplateBasedInvalidationsFromDisk=");
        resultBuffer.append(csl.getTemplateBasedInvalidationsFromDiskCount() - sSavTemplateBasedInvalidationsFromDisk);
        resultBuffer.append(" sObjectsReadFromDisk=");
        resultBuffer.append(csl.getObjectsReadFromDiskCount() - sSavObjectsReadFromDisk);
        resultBuffer.append(" sObjectsReadFromDisk4K=");
        resultBuffer.append(csl.getObjectsReadFromDisk4KCount() - sSavObjectsReadFromDisk4K);
        resultBuffer.append(" sObjectsReadFromDisk40K=");
        resultBuffer.append(csl.getObjectsReadFromDisk40KCount() - sSavObjectsReadFromDisk40K);
        resultBuffer.append(" sObjectsReadFromDisk400K=");
        resultBuffer.append(csl.getObjectsReadFromDisk400KCount() - sSavObjectsReadFromDisk400K);
        resultBuffer.append(" sObjectsReadFromDisk4000K=");
        resultBuffer.append(csl.getObjectsReadFromDisk4000KCount() - sSavObjectsReadFromDisk4000K);
        resultBuffer.append(" sObjectsReadFromDiskSize=");
        resultBuffer.append(csl.getObjectsReadFromDiskSizeCount() - sSavObjectsReadFromDiskSize);
        resultBuffer.append(" sObjectsWriteToDisk=");
        resultBuffer.append(csl.getObjectsWriteToDiskCount() - sSavObjectsWriteToDisk);
        resultBuffer.append(" sObjectsWriteToDisk4K=");
        resultBuffer.append(csl.getObjectsWriteToDisk4KCount() - sSavObjectsWriteToDisk4K);
        resultBuffer.append(" sObjectsWriteToDisk40K=");
        resultBuffer.append(csl.getObjectsWriteToDisk40KCount() - sSavObjectsWriteToDisk40K);
        resultBuffer.append(" sObjectsWriteToDisk400K=");
        resultBuffer.append(csl.getObjectsWriteToDisk400KCount() - sSavObjectsWriteToDisk400K);
        resultBuffer.append(" sObjectsWriteToDisk4000K=");
        resultBuffer.append(csl.getObjectsWriteToDisk4000KCount() - sSavObjectsWriteToDisk4000K);
        resultBuffer.append(" sObjectsWriteToDiskSize=");
        resultBuffer.append(csl.getObjectsWriteToDiskSizeCount() - sSavObjectsWriteToDiskSize);
        resultBuffer.append(" sObjectsDeleteFromDisk=");
        resultBuffer.append(csl.getObjectsDeleteFromDiskCount() - sSavObjectsDeleteFromDisk);
        resultBuffer.append(" sObjectsDeleteFromDisk4K=");
        resultBuffer.append(csl.getObjectsDeleteFromDisk4KCount() - sSavObjectsDeleteFromDisk4K);
        resultBuffer.append(" sObjectsDeleteFromDisk40K=");
        resultBuffer.append(csl.getObjectsDeleteFromDisk40KCount() - sSavObjectsDeleteFromDisk40K);
        resultBuffer.append(" sObjectsDeleteFromDisk400K=");
        resultBuffer.append(csl.getObjectsDeleteFromDisk400KCount() - sSavObjectsDeleteFromDisk400K);
        resultBuffer.append(" sObjectsDeleteFromDisk4000K=");
        resultBuffer.append(csl.getObjectsDeleteFromDisk4000KCount() - sSavObjectsDeleteFromDisk4000K);
        resultBuffer.append(" sObjectsDeleteFromDiskSize=");
        resultBuffer.append(csl.getObjectsDeleteFromDiskSizeCount() - sSavObjectsDeleteFromDiskSize);
        resultBuffer.append(" sPushPullTableSize=");
        resultBuffer.append(cacheServlet.getPushPullTableSize());
        resultBuffer.append(" sRemoteInvalidationNotifications=");
        resultBuffer.append(csl.getRemoteInvalidationNotificationsCount() - sSavRemoteInvalidationNotifications);
        resultBuffer.append(" sRemoteUpdateNotifications=");
        resultBuffer.append(csl.getRemoteUpdateNotificationsCount() - sSavRemoteUpdateNotifications);
        resultBuffer.append(" sRemoteObjectUpdates=");
        resultBuffer.append(csl.getRemoteObjectUpdatesCount() - sSavRemoteObjectUpdates);
        resultBuffer.append(" sRemoteObjectUpdateSize=");
        resultBuffer.append(csl.getRemoteObjectUpdateSizeCount() - sSavRemoteObjectUpdateSize);
        resultBuffer.append(" sRemoteObjectHits=");
        resultBuffer.append(csl.getRemoteObjectHitsCount() - sSavRemoteObjectHits);
        resultBuffer.append(" sRemoteObjectFetchSize=");
        resultBuffer.append(csl.getRemoteObjectFetchSizeCount() - sSavRemoteObjectFetchSize);
        resultBuffer.append(" sRemoteObjectMisses=");
        resultBuffer.append(csl.getRemoteObjectMissesCount() - sSavRemoteObjectMisses);
        if (this.ilistener) {
            if (listenerServlet.invalidationLocal > 0) {
                resultBuffer.append(" sInvalidationLocal=");
                resultBuffer.append(listenerServlet.invalidationLocal);
                resultBuffer.append(" sInvalidationLocalExplicit=");
                resultBuffer.append(listenerServlet.invalidationLocalExplicit);
                resultBuffer.append(" sInvalidationLocalLRU=");
                resultBuffer.append(listenerServlet.invalidationLocalLRU);
                resultBuffer.append(" sInvalidationLocalTimeout=");
                resultBuffer.append(listenerServlet.invalidationLocalTimeout);
                resultBuffer.append(" sInvalidationLocalDiskTimeout=");
                resultBuffer.append(listenerServlet.invalidationLocalDiskTimeout);
                resultBuffer.append(" sInvalidationLocalClearAll=");
                resultBuffer.append(listenerServlet.invalidationLocalClearAll);
                resultBuffer.append(" sInvalidationLocalDiskGC=");
                resultBuffer.append(listenerServlet.invalidationLocalDiskGC);
                resultBuffer.append(" sInvalidationLocalDiskOverflow=");
                resultBuffer.append(listenerServlet.invalidationLocalDiskOverflow);
            }
            if (listenerServlet.invalidationRemote > 0) {
                resultBuffer.append(" sInvalidationRemote=");
                resultBuffer.append(listenerServlet.invalidationRemote);
                resultBuffer.append(" sInvalidationRemoteExplicit=");
                resultBuffer.append(listenerServlet.invalidationRemoteExplicit);
                resultBuffer.append(" sInvalidationRemoteLRU=");
                resultBuffer.append(listenerServlet.invalidationRemoteLRU);
                resultBuffer.append(" sInvalidationRemoteTimeout=");
                resultBuffer.append(listenerServlet.invalidationRemoteTimeout);
                resultBuffer.append(" sInvalidationRemoteDiskTimeout=");
                resultBuffer.append(listenerServlet.invalidationRemoteDiskTimeout);
                resultBuffer.append(" sInvalidationRemoteClearAll=");
                resultBuffer.append(listenerServlet.invalidationRemoteClearAll);
                resultBuffer.append(" sInvalidationRemoteDiskGC=");
                resultBuffer.append(listenerServlet.invalidationRemoteDiskGC);
                resultBuffer.append(" sInvalidationRemoteDiskOverflow=");
                resultBuffer.append(listenerServlet.invalidationRemoteDiskOverflow);
            }
        }
        resultBuffer.append(" ");
        if (this.dmapUsed) {
            hs.clear();
            ts.clear();
            c = cacheDmap.getAllDependencyIds();
            if (c != null && c.size() > 0) {
                ts.addAll(c);
                hs.addAll(c);
            }
            if (ts.size() < 1000) {
                //    out.println("<br> depIds in memory: " + ts);
                System.out.println("*** depIds for cacheName=" + cacheDmapName + " in memory: " + ts);
            }

            ts.clear();
            c = cacheDmap.getDepIdsByRangeDisk(0, -1);
            if (c != null && c.size() > 0) {
                ts.addAll(c);
                hs.addAll(c);
            }
            if (ts.size() < 1000) {
                //    out.println("<br> depIds on disk: " + ts);
                System.out.println("*** depIds for cacheName=" + cacheDmapName + " on disk: " + ts);
            }
            depIdSize = hs.size();
            resultBuffer.append("***");
            resultBuffer.append(" dMemEntriesSize=");
            resultBuffer.append(cacheDmap.getNumberCacheEntries());
            resultBuffer.append(" dDiskEntriesSize=");
            resultBuffer.append(cacheDmap.getIdsSizeDisk());
            resultBuffer.append(" dObjectsOnDisk=");
            resultBuffer.append(cacheDmap.getActualIdsSizeDisk());
            resultBuffer.append(" dDependencyIdsInMemoryAndDisk=");
            resultBuffer.append(depIdSize);
            resultBuffer.append(" dDependencyIdsOnDisk=");
            resultBuffer.append(cacheDmap.getDepIdsSizeDisk());
            resultBuffer.append(" dTemplatesOnDisk=");
            resultBuffer.append(cacheDmap.getTemplatesSizeDisk());
            resultBuffer.append(" dTotalCacheDataDiskSize=");
            resultBuffer.append(cacheDmap.getCacheSizeInBytesDisk());
            if (this.ilistener) {
                if (listenerDmap.invalidationLocal > 0) {
                    resultBuffer.append(" dInvalidationLocal=");
                    resultBuffer.append(listenerDmap.invalidationLocal);
                    resultBuffer.append(" dInvalidationLocalExplicit=");
                    resultBuffer.append(listenerDmap.invalidationLocalExplicit);
                    resultBuffer.append(" dInvalidationLocalLRU=");
                    resultBuffer.append(listenerDmap.invalidationLocalLRU);
                    resultBuffer.append(" dInvalidationLocalTimeout=");
                    resultBuffer.append(listenerDmap.invalidationLocalTimeout);
                    resultBuffer.append(" dInvalidationLocalDiskTimeout=");
                    resultBuffer.append(listenerDmap.invalidationLocalDiskTimeout);
                    resultBuffer.append(" dInvalidationLocalClearAll=");
                    resultBuffer.append(listenerDmap.invalidationLocalClearAll);
                    resultBuffer.append(" dInvalidationLocalDiskGC=");
                    resultBuffer.append(listenerDmap.invalidationLocalDiskGC);
                    resultBuffer.append(" dInvalidationLocalDiskOverflow=");
                    resultBuffer.append(listenerDmap.invalidationLocalDiskOverflow);
                }
                if (listenerDmap.invalidationRemote > 0) {
                    resultBuffer.append(" dInvalidationRemote=");
                    resultBuffer.append(listenerDmap.invalidationRemote);
                    resultBuffer.append(" dInvalidationRemoteExplicit=");
                    resultBuffer.append(listenerDmap.invalidationRemoteExplicit);
                    resultBuffer.append(" dInvalidationRemoteLRU=");
                    resultBuffer.append(listenerDmap.invalidationRemoteLRU);
                    resultBuffer.append(" dInvalidationRemoteTimeout=");
                    resultBuffer.append(listenerDmap.invalidationRemoteTimeout);
                    resultBuffer.append(" dInvalidationRemoteDiskTimeout=");
                    resultBuffer.append(listenerDmap.invalidationRemoteDiskTimeout);
                    resultBuffer.append(" dInvalidationRemoteClearAll=");
                    resultBuffer.append(listenerDmap.invalidationRemoteClearAll);
                    resultBuffer.append(" dInvalidationRemoteDiskGC=");
                    resultBuffer.append(listenerDmap.invalidationRemoteDiskGC);
                    resultBuffer.append(" dInvalidationRemoteDiskOverflow=");
                    resultBuffer.append(listenerDmap.invalidationRemoteDiskOverflow);
                }
            }
        }
        resultBuffer.append(" ");
        String result = resultBuffer.toString();
        System.out.println(result);
        out.println("<br> " + result);
    }

    class TesterThread extends Thread {

        String name = "";
        String sSharingPolicy = "";
        EntryInfo ei = new EntryInfo();
        int loop = 1;
        int items = 1000;
        int sharingPolicy = 1;
        int timeToLive = 0;
        boolean put1 = true;
        boolean get1 = true;
        boolean dinv = false;
        boolean tinv = false;
        boolean clear = false;
        boolean put2 = false;
        boolean get2 = false;
        boolean stop = false;
        boolean addAlias = false;
        boolean removeAlias = false;
        boolean usedmap = false;
        public long timePut1 = 0;
        public long timeGet1 = 0;
        public long timePut2 = 0;
        public long timeGet2 = 0;

        TesterThread(String name, int loop, int items, int sharingPolicy, int timeToLive, boolean put1, boolean get1, boolean dinv, boolean tinv, boolean clear, boolean put2,
                     boolean get2, boolean stop, boolean addAlias, boolean removeAlias, boolean usedmap) {
            this.name = name;
            this.loop = loop;
            this.items = items;
            this.sharingPolicy = sharingPolicy;
            this.timeToLive = timeToLive;
            if (sharingPolicy == EntryInfo.NOT_SHARED) {
                sSharingPolicy = "Not-Sharing";
            } else if (sharingPolicy == EntryInfo.SHARED_PUSH) {
                sSharingPolicy = "Push";
            } else if (sharingPolicy == EntryInfo.SHARED_PUSH_PULL) {
                sSharingPolicy = "Push-Pull";
            }
            this.put1 = put1;
            this.get1 = get1;
            this.dinv = dinv;
            this.tinv = tinv;
            this.clear = clear;
            this.put2 = put2;
            this.get2 = get2;
            this.stop = stop;
            this.addAlias = addAlias;
            this.removeAlias = removeAlias;
            this.usedmap = usedmap;
            if (this.usedmap) {
                dmapUsed = true;
            }
        }

        @Override
        public void run() {
            long start = 0, delta = 0;
            String depid = "";
            String[] depids = null;
            int ttl = 0;
            String template = this.name + ":template";

            out.println("<br> Thread id=" + this.name + " loop=" + this.loop + " items=" + this.items + " sharingPolicy=" + this.sSharingPolicy + " timeToLive= " + this.timeToLive
                        + " put1=" + this.put1 + " get1=" + this.get1 + " dinv=" + this.dinv + " tinv=" + this.tinv + " clear=" + this.clear + " put2=" + this.put2 + " get2="
                        + this.get2 + " stop=" + this.stop + " addAlias=" + this.addAlias + " removeAlias=" + this.removeAlias);
            try {
                for (int i = 0; i < this.loop; i++) {
                    if (put1) {
                        start = System.currentTimeMillis();
                        int k = 0;
                        for (int j = 0; j < this.items; j++) {
                            String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                            Object value = null;
                            if (useSizer) {
                                value = new MyObjectSizer(id, valueSize);
                            } else {
                                value = new long[valueSize];
                                long[] v = (long[]) value;
                                v[0] = j;
                                v[1] = i;
                            }
                            ttl = this.timeToLive;
                            if (j % num_depids == 0) {
                                if (idd) {
                                    depid = id;
                                } else {
                                    depid = this.name + ":dep-id:" + k;
                                    k++;
                                }
                            } else {
                                if (idd) {
                                    ttl = 0;
                                }
                            }

                            if (oneDepid == true) {
                                depids[0] = xdepid;
                                if (depid.equals(id)) {
                                    depids = new String[1];
                                    depids[0] = xdepid;
                                } else {
                                    depids = new String[2];
                                    depids[0] = xdepid;
                                    depids[1] = depid;
                                }
                            } else {
                                if (depid.equals(id)) {
                                    depids = null;
                                } else {
                                    depids = new String[1];
                                    depids[0] = depid;
                                }
                            }
                            if (delay > 0) {
                                sleep(delay);
                            }
                            put(id, value, 1, ttl, this.sharingPolicy, depids, template);

                            if (addAlias && (j % 100 == 0)) {
                                String[] aliasIds = { id + ":alias:0", id + ":alias:1" };
                                if (adelay > 0) {
                                    sleep(adelay);
                                }
                                addAlias(id, aliasIds);
                            }
                            if (j > 0 && j % win == 0) {
                                delta = System.currentTimeMillis() - start;
                                timePut1 += delta;
                                out.println("<br> loop=" + i + " Putting cache index=" + id + " average elapsed= " + (delta / win));
                                System.out.println("*** loop=" + i + " Putting cache index=" + id + " average elapsed= " + (delta / win));
                                start = System.currentTimeMillis();
                            }
                        }
                        timePut1 += System.currentTimeMillis() - start;
                    }
                    if (get1) {
                        start = System.currentTimeMillis();
                        for (int j = 0; j < this.items; j++) {
                            String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                            if (delay > 0) {
                                sleep(delay);
                            }
                            Object value = get(id);
                            if (useSizer) {
                                MyObjectSizer os = (MyObjectSizer) value;
                                if (os == null) {
                                    if (this.timeToLive <= 0) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error value=null");
                                    }
                                } else {
                                    if (!(os.toString().equals(id))) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error data mismatch value=" + os.toString());
                                    }
                                }
                            } else {
                                long[] v = (long[]) value;
                                if (v == null) {
                                    if (this.timeToLive <= 0) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error value=null");
                                    }
                                } else {
                                    if (v[0] != j) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error data mismatch value=" + v[0]);
                                    }
                                    if (v[1] != i) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error data mismatch value=" + v[1]);
                                    }
                                }
                            }
                            if (addAlias && (j % 100 == 0)) {
                                if (adelay > 0) {
                                    sleep(adelay);
                                }
                                value = get(id + ":alias:0");
                                if (useSizer) {
                                    MyObjectSizer os = (MyObjectSizer) value;
                                    if (os == null) {
                                        if (this.timeToLive <= 0) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error value=null");
                                        }
                                    } else {
                                        if (!(os.toString().equals(id))) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error data mismatch value=" + os.toString());
                                        }
                                    }
                                } else {
                                    long[] v = (long[]) value;
                                    if (v == null) {
                                        if (this.timeToLive <= 0) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error value=null");
                                        }
                                    } else {
                                        if (v[0] != j) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error data mismatch value=" + v[0]);
                                        }
                                        if (v[1] != i) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error data mismatch value=" + v[1]);
                                        }
                                    }
                                }
                                value = get(id + ":alias:1");
                                if (useSizer) {
                                    MyObjectSizer os = (MyObjectSizer) value;
                                    if (os == null) {
                                        if (this.timeToLive <= 0) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error value=null");
                                        }
                                    } else {
                                        if (!(os.toString().equals(id))) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error data mismatch value=" + os.toString());
                                        }
                                    }
                                } else {
                                    long[] v = (long[]) value;
                                    if (v == null) {
                                        if (this.timeToLive <= 0) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error value=null");
                                        }
                                    } else {
                                        if (v[0] != j) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error data mismatch value=" + v[0]);
                                        }
                                        if (v[1] != i) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error data mismatch value=" + v[1]);
                                        }
                                    }
                                }
                            }
                            if (j > 0 && j % win == 0) {
                                delta = System.currentTimeMillis() - start;
                                timeGet1 += delta;
                                System.out.println("*** loop=" + i + " Getting cache index=" + id + " average elapsed= " + (delta / win));
                                out.println("<br> loop=" + i + " Getting cache index=" + id + " average elapsed= " + (delta / win));
                                start = System.currentTimeMillis();
                            }
                        }
                        timeGet1 += System.currentTimeMillis() - start;
                    }
                    if (removeAlias) {
                        if (adelay > 0) {
                            Thread.sleep(adelay);
                        }
                        start = System.currentTimeMillis();
                        for (int j = 0; j < this.items; j++) {
                            String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                            if (adelay > 0) {
                                sleep(adelay);
                            }
                            removeAlias(id + ":alias:0");
                            removeAlias(id + ":alias:1");
                            j = j + 99;
                        }
                        System.out.println("*** loop=" + i + " removing aliases elapsed= " + (System.currentTimeMillis() - start));
                        out.println("<br> loop=" + i + " removing aliases elapsed= " + (System.currentTimeMillis() - start));
                    }
                    if (dinv) {
                        if (idd) {
                            for (int j = 0; j < this.items; j++) {
                                if (j % num_depids == 0) {
                                    depid = this.name + ":" + this.sSharingPolicy + ":" + j;
                                } else {
                                    continue;
                                }
                                if (ddelay > 0) {
                                    Thread.sleep(ddelay);
                                }
                                System.out.println("*** loop=" + i + " Starting Invalidate depid=" + depid + " waitInv=" + waitInv);
                                out.println("<br> loop=" + i + " Starting Invalidate depid=" + depid + " waitInv=" + waitInv);
                                start = System.currentTimeMillis();
                                invalidateById(depid, waitInv);

                                System.out.println("*** loop=" + i + " Invalidate depid=" + depid + " elapsed= " + (System.currentTimeMillis() - start));
                                out.println("<br> loop=" + i + " Invalidate depid=" + depid + " elapsed= " + (System.currentTimeMillis() - start));
                            }
                        } else {
                            for (int j = 0; j < this.items / num_depids; j++) {
                                if (ddelay > 0) {
                                    Thread.sleep(ddelay);
                                }
                                depid = this.name + ":dep-id:" + j;
                                System.out.println("*** loop=" + i + " Starting Invalidate depid=" + depid + " waitInv=" + waitInv);
                                out.println("<br> loop=" + i + " Starting Invalidate depid=" + depid + " waitInv=" + waitInv);
                                start = System.currentTimeMillis();
                                invalidateById(depid, waitInv);
                                System.out.println("*** loop=" + i + " Invalidate depid=" + depid + " elapsed= " + (System.currentTimeMillis() - start));
                                out.println("<br> loop=" + i + " Invalidate depid=" + depid + " elapsed= " + (System.currentTimeMillis() - start));
                            }
                        }
                        if (oneDepid == true && invOneDepid == false) {
                            invOneDepid = true;
                            System.out.println("*** loop=" + i + " Starting Invalidate depid=" + xdepid + " waitInv=" + waitInv);
                            out.println("<br> loop=" + i + " Starting Invalidate depid=" + xdepid + " waitInv=" + waitInv);
                            invalidateById(xdepid, waitInv);
                            System.out.println("*** loop=" + i + " Invalidate depid=" + xdepid + " elapsed= " + (System.currentTimeMillis() - start));
                            out.println("<br> loop=" + i + " Invalidate depid=" + xdepid + " elapsed= " + (System.currentTimeMillis() - start));
                        }

                        System.out.println("*** loop=" + i + " in Thread:" + this.name + " PendingRemovalFromDisk=" + cacheServlet.getPendingRemovalSizeDisk() + " ");
                        out.println("*** loop=" + i + " in Thread:" + this.name + " PendingRemovalFromDisk=" + cacheServlet.getPendingRemovalSizeDisk() + " ");

                    }
                    if (tinv && !usedmap) {
                        if (tdelay > 0) {
                            Thread.sleep(tdelay);
                        }
                        System.out.println("*** loop=" + i + " Starting Invalidate template=" + template + " waitInv=" + waitInv);
                        out.println("<br> loop=" + i + " Starting Invalidate template=" + template + " waitInv=" + waitInv);
                        start = System.currentTimeMillis();
                        invalidateByTemplate(template, waitInv);
                        System.out.println("*** loop=" + i + " Invalidate template=" + template + " elapsed= " + (System.currentTimeMillis() - start));
                        out.println("<br> loop=" + i + " Invalidate template=" + template + " elapsed= " + (System.currentTimeMillis() - start));
                    }
                    if (clear) {
                        if (cdelay > 0) {
                            Thread.sleep(cdelay);
                        }
                        start = System.currentTimeMillis();
                        clear();
                        System.out.println("*** loop=" + i + " Clear elapsed= " + (System.currentTimeMillis() - start));
                        out.println("<br> loop=" + i + " Clear elapsed= " + (System.currentTimeMillis() - start));
                    }
                    if (put2) {
                        start = System.currentTimeMillis();
                        int k = 0;
                        for (int j = 0; j < this.items; j++) {
                            String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                            if (!same) {
                                id = id + "-2";
                            }
                            Object value = null;
                            if (useSizer) {
                                value = new MyObjectSizer(id, valueSize + 100);
                            } else {
                                value = new long[valueSize];
                                long[] v = (long[]) value;
                                v[0] = j * j;
                                v[1] = i;
                            }
                            ttl = this.timeToLive;
                            if (j % num_depids == 0) {
                                if (idd) {
                                    depid = id;
                                } else {
                                    depid = this.name + ":dep-id:" + k;
                                    k++;
                                }
                            } else {
                                if (idd) {
                                    ttl = 0;
                                }
                            }
                            if (oneDepid == true) {
                                depids[0] = xdepid;
                                if (depid.equals(id)) {
                                    depids = new String[1];
                                    depids[0] = xdepid;
                                } else {
                                    depids = new String[2];
                                    depids[0] = xdepid;
                                    depids[1] = depid;
                                }
                            } else {
                                if (depid.equals(id)) {
                                    depids = null;
                                } else {
                                    depids = new String[1];
                                    depids[0] = depid;
                                }
                            }
                            if (delay > 0) {
                                sleep(delay);
                            }
                            put(id, value, 1, ttl, this.sharingPolicy, depids, template);
                            if (addAlias && (j % 100 == 0)) {
                                String[] aliasIds = { id + ":alias:0", id + ":alias:1" };
                                if (!same) {
                                    aliasIds[0] = aliasIds[0] + "-2";
                                    aliasIds[1] = aliasIds[1] + "-2";
                                }
                                if (adelay > 0) {
                                    sleep(adelay);
                                }
                                addAlias(id, aliasIds);
                            }
                            if (j > 0 && j % win == 0) {
                                delta = System.currentTimeMillis() - start;
                                timePut2 += delta;
                                System.out.println("*** loop=" + i + " Putting cache index=" + id + " average elapsed= " + (delta / win));
                                out.println("<br> loop=" + i + " Putting cache index=" + id + " average elapsed= " + (delta / win));
                                start = System.currentTimeMillis();
                            }
                        }
                        timePut2 += System.currentTimeMillis() - start;
                    }
                    if (get2) {
                        start = System.currentTimeMillis();
                        for (int j = 0; j < this.items; j++) {
                            String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                            if (!same) {
                                id = id + "-2";
                            }
                            if (delay > 0) {
                                sleep(delay);
                            }
                            Object value = get(id);
                            if (useSizer) {
                                MyObjectSizer os = (MyObjectSizer) value;
                                if (os == null) {
                                    if (this.timeToLive <= 0) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error value=null");
                                    }
                                } else {
                                    if (!(os.toString().equals(id))) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error data mismatch value=" + os.toString());
                                    }
                                }
                            } else {
                                long[] v = (long[]) value;
                                if (v == null) {
                                    if (this.timeToLive <= 0) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error value=null");
                                    }
                                } else {
                                    if (v[0] != j * j) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error data mismatch value=" + v[0]);
                                    }
                                    if (v[1] != i) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error data mismatch value=" + v[1]);
                                    }
                                }
                            }
                            if (addAlias && (j % 100 == 0)) {
                                if (adelay > 0) {
                                    sleep(adelay);
                                }
                                if (same) {
                                    value = get(id + ":alias:0");
                                } else {
                                    value = get(id + ":alias:0-2");
                                }
                                if (useSizer) {
                                    MyObjectSizer os = (MyObjectSizer) value;
                                    if (os == null) {
                                        if (this.timeToLive <= 0) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error value=null");
                                        }
                                    } else {
                                        if (!(os.toString().equals(id))) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error data mismatch value=" + os.toString());
                                        }
                                    }
                                } else {
                                    long[] v = (long[]) value;
                                    if (v == null) {
                                        if (this.timeToLive <= 0) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error value=null");
                                        }
                                    } else {
                                        if (v[0] != j) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error data mismatch value=" + v[0]);
                                        }
                                        if (v[1] != i) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:0 *** error data mismatch value=" + v[1]);
                                        }
                                    }
                                }
                                if (same) {
                                    value = get(id + ":alias:1");
                                } else {
                                    value = get(id + ":alias:1-2");
                                }
                                if (useSizer) {
                                    MyObjectSizer os = (MyObjectSizer) value;
                                    if (os == null) {
                                        if (this.timeToLive <= 0) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error value=null");
                                        }
                                    } else {
                                        if (!(os.toString().equals(id))) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error data mismatch value=" + os.toString());
                                        }
                                    }
                                } else {
                                    long[] v = (long[]) value;
                                    if (v == null) {
                                        if (this.timeToLive <= 0) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error value=null");
                                        }
                                    } else {
                                        if (v[0] != j * j) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error data mismatch value=" + v[0]);
                                        }
                                        if (v[1] != i) {
                                            out.println("<br> loop=" + i + " cache index=" + id + ":alias:1 *** error data mismatch value=" + v[1]);
                                        }
                                    }
                                }
                            }
                            if (j > 0 && j % win == 0) {
                                delta = System.currentTimeMillis() - start;
                                timeGet2 += delta;
                                System.out.println("*** loop=" + i + " Getting cache index=" + id + " average elapsed= " + (delta / win));
                                out.println("<br> loop=" + i + " Getting cache index=" + id + " average elapsed= " + (delta / win));
                                start = System.currentTimeMillis();
                            }
                        }
                        timeGet2 += System.currentTimeMillis() - start;
                    }
                    if (removeAlias) {
                        if (adelay > 0) {
                            Thread.sleep(adelay);
                        }
                        start = System.currentTimeMillis();
                        for (int j = 0; j < this.items; j++) {
                            String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                            if (adelay > 0) {
                                sleep(adelay);
                            }
                            if (same) {
                                removeAlias(id + ":alias:0");
                                removeAlias(id + ":alias:1");
                            } else {
                                removeAlias(id + ":alias:0-2");
                                removeAlias(id + ":alias:1-2");
                            }
                            j = j + 99;
                        }
                        System.out.println("*** loop=" + i + " removing aliases elapsed= " + (System.currentTimeMillis() - start));
                        out.println("<br> loop=" + i + " removing aliases elapsed= " + (System.currentTimeMillis() - start));
                    }
                    if (stop) {
                        if (sdelay > 0) {
                            Thread.sleep(sdelay);
                        }
                        start = System.currentTimeMillis();
                        cacheStop();
                        System.out.println("*** loop=" + i + " Stop elapsed= " + (System.currentTimeMillis() - start));
                        out.println("<br> loop=" + i + " Stop elapsed= " + (System.currentTimeMillis() - start));
                    }
                }
            } catch (Exception e) {
                out.println("<br> error: " + getDCStackTrace(e));
            }
        }

        void put(Object id, Object value, int priority, int timeToLive, int sharingPolicy, Object[] depIds, String template) {

            if (this.usedmap) {
                //System.out.println("putting in dmap:"+dmap+" id:"+id);
                dmap.put(id, value, priority, timeToLive, sharingPolicy, depIds);
            } else {
                //System.out.println("putting in servlet cache:"+cacheServlet.getCacheName()+ " id:"+id);
                ei.reset();
                ei.setId(id);
                ei.setTimeLimit(timeToLive);
                ei.setSharingPolicy(sharingPolicy);
                //if (depId != null) {
                //    ei.addDataId(depId);
                //}
                if (template != null) {
                    ei.addTemplate(template);
                }
                if (depIds != null) {
                    for (int i = 0; i < depIds.length; i++) {
                        if (depIds[i] != null) {
                            ei.addDataId(depIds[i]);
                        }
                    }
                }
                //if (depIds != null) {
                //    for (int i = 0; i < depIds.length; i++)
                //       ei.addTemplate(templateIds[i]);
                //}
                cacheServlet.invalidateAndSet(ei, value, true);
            }
        }

        Object get(Object key) {

            if (this.usedmap) {
                //System.out.println("getting from dmap:"+dmap+" id:"+key+" value:"+dmap.get(key));
                return dmap.get(key);

            } else {

                Object value = cacheServlet.getValue(key, cacheServlet.shouldPull(this.sharingPolicy, key));
                //System.out.println("getting from servlet cache:"+cacheServlet.getCacheName()+ " id:"+key+" value:"+value);
                return value;
            }
        }

        void invalidateById(Object id, boolean waitOnInvalidation) {

            if (this.usedmap) {
                dmap.invalidate(id, waitOnInvalidation);
            } else {
                cacheServlet.invalidateById(id, waitOnInvalidation);
            }
        }

        void invalidateByTemplate(String id, boolean waitOnInvalidation) {
            if (!this.usedmap) {
                cacheServlet.invalidateByTemplate(id, waitOnInvalidation);
            }
        }

        void clear() {
            if (this.usedmap) {
                dmap.clear();
            } else {
                cacheServlet.clear();
            }
        }

        void cacheStop() {
            if (this.usedmap) {
                cacheDmap.stop();
            } else {
                cacheServlet.stop();
            }
        }

        void addAlias(Object key, Object[] aliasArray) {
            if (this.usedmap) {
                dmap.addAlias(key, aliasArray);
            } else {
                cacheServlet.addAlias(
                                      key,
                                      aliasArray,
                                      cacheServlet.shouldPull(this.sharingPolicy, key),
                                      true);
            }

        }

        void removeAlias(Object aliasId) {
            if (this.usedmap) {
                dmap.removeAlias(aliasId);
            } else {
                cacheServlet.removeAlias(
                                         aliasId,
                                         cacheServlet.shouldPull(this.sharingPolicy, aliasId),
                                         true);
            }

        }
    }

    class TesterThread2 extends Thread {

        private final Random random;
        String name = "";
        String sSharingPolicy = "";
        EntryInfo ei = new EntryInfo();
        int loop = 1;
        int items = 1000;
        int sharingPolicy = 1;
        int timeToLive = 0;
        boolean rand = false;
        boolean get1 = true;
        boolean dinv = false;
        boolean tinv = false;
        boolean clear = false;
        boolean stop = false;
        boolean usedmap = false;
        public long timePut1 = 0;
        public long timeGet1 = 0;
        public long timeGetMiss1 = 0;
        public int getCount = 0;
        public int putCount = 0;

        TesterThread2(int tid, String name, int loop, int items, int sharingPolicy, int timeToLive, boolean rand, boolean get1, boolean dinv, boolean tinv, boolean clear,
                      boolean stop, boolean usedmap) {
            random = new Random((tid + 1) * 89);
            this.name = name;
            this.loop = loop;
            this.items = items;
            this.sharingPolicy = sharingPolicy;
            this.timeToLive = timeToLive;
            if (sharingPolicy == EntryInfo.NOT_SHARED) {
                sSharingPolicy = "Not-Sharing";
            } else if (sharingPolicy == EntryInfo.SHARED_PUSH) {
                sSharingPolicy = "Push";
            } else if (sharingPolicy == EntryInfo.SHARED_PUSH_PULL) {
                sSharingPolicy = "Push-Pull";
            }
            this.rand = rand;
            this.get1 = get1;
            this.dinv = dinv;
            this.tinv = tinv;
            this.clear = clear;
            this.stop = stop;
            this.usedmap = usedmap;
            if (this.usedmap) {
                dmapUsed = true;
            }
        }

        @Override
        public void run() {
            long start = 0;
            long start1 = 0;
            String depid = "";
            String[] depids = null;
            if (oneDepid == true) {
                depids = new String[2];
            } else {
                depids = new String[1];
            }
            String template = this.name + ":template";

            out.println("<br> Thread id=" + this.name + " loop=" + this.loop + " items=" + this.items + " sharingPolicy=" + this.sSharingPolicy + " timeToLive= " + this.timeToLive
                        + " random=" + this.rand + " get1=" + this.get1 + " dinv=" + this.dinv + " tinv=" + this.tinv + " clear=" + this.clear + " stop=" + this.stop);
            try {
                for (int i = 0; i < this.loop; i++) {
                    if (rand) {
                        start = System.currentTimeMillis();
                        for (int k = 0; k < this.items; k++) {
                            int j = random.nextInt(items);
                            String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                            if (delay > 0) {
                                sleep(delay);
                            }
                            start1 = System.currentTimeMillis();
                            long[] value = (long[]) get(id);
                            timeGet1 += System.currentTimeMillis() - start1;
                            if (value == null) {
                                value = new long[valueSize];
                                value[0] = j;
                                if (num_depids < 2) {
                                    depid = this.name + ":dep-id";
                                } else {
                                    depid = this.name + random.nextInt(items / num_depids);
                                }
                                if (oneDepid == true) {
                                    depids[0] = xdepid;
                                    depids[1] = depid;
                                } else {
                                    depids[0] = depid;
                                }
                                if (delay > 0) {
                                    sleep(delay);
                                }
                                start1 = System.currentTimeMillis();
                                put(id, value, 1, this.timeToLive, this.sharingPolicy, depids, template);
                                timePut1 += System.currentTimeMillis() - start1;
                                putCount++;
                            } else {
                                getCount++;
                                if (value[0] != j) {
                                    out.println("<br> loop=" + i + " cache index=" + id + " *** data mismatch value[0]=" + value[0]);
                                }
                            }
                            if (k > 0 && k % win == 0) {
                                id = this.name + ":" + this.sSharingPolicy + ":" + k;
                                out.println("<br> loop=" + i + " Putting cache index=" + id + " average elapsed= " + ((System.currentTimeMillis() - start) / win) + " getCount="
                                            + getCount + " putCount=" + putCount);
                                System.out.println("*** loop=" + i + " Putting cache index=" + id + " average elapsed= " + ((System.currentTimeMillis() - start) / win)
                                                   + " getCount=" + getCount + " putCount=" + putCount);
                                start = System.currentTimeMillis();
                            }
                        }
                    } else {
                        start = System.currentTimeMillis();
                        int k = 0;
                        for (int j = 0; j < this.items; j++) {
                            String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                            long[] value = new long[valueSize];
                            value[0] = j;
                            value[1] = i;
                            if (j % num_depids == 0) {
                                depid = this.name + ":dep-id:" + k;
                                k++;
                            }
                            if (oneDepid == true) {
                                depids[0] = xdepid;
                                depids[1] = depid;
                            } else {
                                depids[0] = depid;
                            }
                            if (delay > 0) {
                                sleep(delay);
                            }
                            put(id, value, 1, this.timeToLive, this.sharingPolicy, depids, template);
                            if (j > 0 && j % win == 0) {
                                out.println("<br> loop=" + i + " Putting cache index=" + id + " average elapsed= " + ((System.currentTimeMillis() - start) / win));
                                System.out.println("*** loop=" + i + " Putting cache index=" + id + " average elapsed= " + ((System.currentTimeMillis() - start) / win));
                                start = System.currentTimeMillis();
                            }
                        }
                    }
                    if (rand) {
                        if (get1) {
                            start = System.currentTimeMillis();
                            for (int k = 0; k < this.items; k++) {
                                int j = random.nextInt(items);
                                String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                                if (delay > 0) {
                                    sleep(delay);
                                }
                                long[] value = (long[]) get(id);
                                if (value == null) {
                                    value = new long[valueSize];
                                    value[0] = j;
                                    if (num_depids < 2) {
                                        depid = this.name + ":dep-id";
                                    } else {
                                        depid = this.name + random.nextInt(items / num_depids);
                                    }
                                    if (oneDepid == true) {
                                        depids[0] = xdepid;
                                        depids[1] = depid;
                                    } else {
                                        depids[0] = depid;
                                    }
                                    if (delay > 0) {
                                        sleep(delay);
                                    }
                                    put(id, value, 1, this.timeToLive, this.sharingPolicy, depids, template);
                                    putCount++;
                                } else {
                                    if (value[0] != j) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** data mismatch value[0]=" + value[0]);
                                    }
                                    getCount++;
                                }
                                if (k > 0 && k % win == 0) {
                                    id = this.name + ":" + this.sSharingPolicy + ":" + k;
                                    out.println("<br> loop=" + i + " Getting cache index=" + id + " average elapsed= " + ((System.currentTimeMillis() - start) / win)
                                                + " getCount=" + getCount + " putCount=" + putCount);
                                    System.out.println("*** loop=" + i + " Getting cache index=" + id + " average elapsed= " + ((System.currentTimeMillis() - start) / win)
                                                       + " getCount=" + getCount + " putCount=" + putCount);
                                    start = System.currentTimeMillis();
                                }
                            }
                        }
                    } else {
                        if (get1) {
                            start = System.currentTimeMillis();
                            for (int j = 0; j < this.items; j++) {
                                String id = this.name + ":" + this.sSharingPolicy + ":" + j;
                                if (delay > 0) {
                                    sleep(delay);
                                }
                                long[] value = (long[]) get(id);
                                if (value == null) {
                                    out.println("<br> loop=" + i + " cache index=" + id + " *** error value=null");
                                } else {
                                    if (value[0] != j) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error data mismatch value=" + value[0]);
                                    }
                                    if (value[1] != i) {
                                        out.println("<br> loop=" + i + " cache index=" + id + " *** error data mismatch value=" + value[1]);
                                    }
                                }
                                if (j > 0 && j % win == 0) {
                                    System.out.println("*** loop=" + i + " Getting cache index=" + id + " average elapsed= " + ((System.currentTimeMillis() - start) / win));
                                    out.println("<br> loop=" + i + " Getting cache index=" + id + " average elapsed= " + ((System.currentTimeMillis() - start) / win));
                                    start = System.currentTimeMillis();
                                }
                            }
                        }
                    }
                    if (dinv) {
                        for (int j = 0; j < this.items / num_depids; j++) {
                            if (ddelay > 0) {
                                Thread.sleep(ddelay);
                            }
                            depid = this.name + ":dep-id:" + j;
                            System.out.println("*** loop=" + i + " Starting Invalidate depid=" + depid + " waitInv=" + waitInv);
                            out.println("<br> loop=" + i + " Starting Invalidate depid=" + depid + " waitInv=" + waitInv);
                            start = System.currentTimeMillis();
                            invalidateById(depid, waitInv);
                            System.out.println("*** loop=" + i + " Invalidate depid=" + depid + " elapsed= " + (System.currentTimeMillis() - start));
                            out.println("<br> loop=" + i + " Invalidate depid=" + depid + " elapsed= " + (System.currentTimeMillis() - start));
                        }
                        if (oneDepid == true && invOneDepid == false) {
                            invOneDepid = true;
                            System.out.println("*** loop=" + i + " Starting Invalidate depid=" + xdepid + " waitInv=" + waitInv);
                            out.println("<br> loop=" + i + " Starting Invalidate depid=" + xdepid + " waitInv=" + waitInv);
                            invalidateById(xdepid, waitInv);
                            System.out.println("*** loop=" + i + " Invalidate depid=" + xdepid + " elapsed= " + (System.currentTimeMillis() - start));
                            out.println("<br> loop=" + i + " Invalidate depid=" + xdepid + " elapsed= " + (System.currentTimeMillis() - start));
                        }

                        System.out.println("*** loop=" + i + " in Thread:" + this.name + " PendingRemovalFromDisk=" + cacheServlet.getPendingRemovalSizeDisk() + " ");
                        out.println("*** loop=" + i + " in Thread:" + this.name + " PendingRemovalFromDisk=" + cacheServlet.getPendingRemovalSizeDisk() + " ");

                    }
                    if (tinv && !usedmap) {
                        if (tdelay > 0) {
                            Thread.sleep(tdelay);
                        }
                        System.out.println("*** loop=" + i + " Starting Invalidate template=" + template + " waitInv=" + waitInv);
                        out.println("<br> loop=" + i + " Starting Invalidate template=" + template + " waitInv=" + waitInv);
                        start = System.currentTimeMillis();
                        invalidateByTemplate(template, waitInv);
                        System.out.println("*** loop=" + i + " Invalidate template=" + template + " elapsed= " + (System.currentTimeMillis() - start));
                        out.println("<br> loop=" + i + " Invalidate template=" + template + " elapsed= " + (System.currentTimeMillis() - start));
                    }
                    if (clear) {
                        if (cdelay > 0) {
                            Thread.sleep(cdelay);
                        }
                        start = System.currentTimeMillis();
                        clear();
                        System.out.println("*** loop=" + i + " Clear elapsed= " + (System.currentTimeMillis() - start));
                        out.println("<br> loop=" + i + " Clear elapsed= " + (System.currentTimeMillis() - start));
                    }
                    if (stop) {
                        if (sdelay > 0) {
                            Thread.sleep(sdelay);
                        }
                        start = System.currentTimeMillis();
                        cacheStop();
                        System.out.println("*** loop=" + i + " Stop elapsed= " + (System.currentTimeMillis() - start));
                        out.println("<br> loop=" + i + " Stop elapsed= " + (System.currentTimeMillis() - start));
                    }
                }
            } catch (Exception e) {
                out.println("<br> error: " + getDCStackTrace(e));
            }
        }

        void put(Object id, Object value, int priority, int timeToLive, int sharingPolicy, Object[] depIds, String template) {

            if (this.usedmap) {
                dmap.put(id, value, priority, timeToLive, sharingPolicy, depIds);
            } else {
                ei.reset();
                ei.setId(id);
                ei.setTimeLimit(timeToLive);
                ei.setSharingPolicy(sharingPolicy);
                //if (depId != null) {
                //    ei.addDataId(depId);
                //}
                if (template != null) {
                    ei.addTemplate(template);
                }
                if (depIds != null) {
                    for (int i = 0; i < depIds.length; i++)
                        ei.addDataId(depIds[i]);
                }
                //if (depIds != null) {
                //    for (int i = 0; i < depIds.length; i++)
                //       ei.addTemplate(templateIds[i]);
                //}
                cacheServlet.invalidateAndSet(ei, value, true);
            }
        }

        Object get(Object key) {
            if (this.usedmap) {
                return dmap.get(key);
            } else {
                Object value = cacheServlet.getValue(key, cacheServlet.shouldPull(this.sharingPolicy, key));
                return value;
            }
        }

        void invalidateById(Object id, boolean waitOnInvalidation) {
            if (this.usedmap) {
                dmap.invalidate(id, waitOnInvalidation);
            } else {
                cacheServlet.invalidateById(id, waitOnInvalidation);
            }
        }

        void invalidateByTemplate(String id, boolean waitOnInvalidation) {
            if (!this.usedmap) {
                cacheServlet.invalidateByTemplate(id, waitOnInvalidation);
            }
        }

        void clear() {
            if (this.usedmap) {
                dmap.clear();
            } else {
                cacheServlet.clear();
            }
        }

        void cacheStop() {
            if (this.usedmap) {
                cacheDmap.stop();
            } else {
                cacheServlet.stop();
            }
        }

    }

    boolean parse(String input) {

        boolean finish = false;
        String parm1, parm2, parm3, parm4, parm5, parm6, parm7, parm8, parm9, parm10, parm11, parm12, parm13, parm14, parm15, parm16;
        int index1, index2;
        int i = 0;
        do {
            index1 = input.indexOf("_");
            if (index1 > 0) {
                parm1 = input.substring(0, index1);
                index1++;

                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm2 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm3 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm4 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm5 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm6 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm7 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm8 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm9 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm10 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm11 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm12 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm13 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm14 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf("_", index1);
                if (index2 > 0) {
                    parm15 = input.substring(index1, index2);
                } else {
                    msg = "Format error e.g. 1_1000_1_0_1_1_0_0_0_0_0_0_0_0_0_0;";
                    return false;
                }
                index1 = index2 + 1;
                index2 = input.indexOf(";");
                if (index2 > 0) {
                    parm16 = input.substring(index1, index2);
                    input = input.substring(index2 + 1);
                } else {
                    parm16 = input.substring(index1);
                    finish = true;
                }
                out.println("<br> loop=" + parm1 + " items=" + parm2 + " sharingPolicy=" + parm3 + " timeToLive=" + parm4 + " put1=" + parm5 + " get1=" + parm6 + " invDepId="
                            + parm7 + " InvTemplate=" + parm8 + " clearCache=" + parm9 + " put2=" + parm10 + " get2=" + parm11 + " StopServer=" + parm12 + " addAlias=" + parm13
                            + " removeAlias=" + parm14 + " random=" + parm15 + " dmap=" + parm16);
                try {
                    this.threadParmArray[i][0] = new Integer(parm1).intValue();
                    this.threadParmArray[i][1] = new Integer(parm2).intValue();
                    this.threadParmArray[i][2] = new Integer(parm3).intValue();
                    this.threadParmArray[i][3] = new Integer(parm4).intValue();
                    this.threadParmArray[i][4] = new Integer(parm5).intValue();
                    this.threadParmArray[i][5] = new Integer(parm6).intValue();
                    this.threadParmArray[i][6] = new Integer(parm7).intValue();
                    this.threadParmArray[i][7] = new Integer(parm8).intValue();
                    this.threadParmArray[i][8] = new Integer(parm9).intValue();
                    this.threadParmArray[i][9] = new Integer(parm10).intValue();
                    this.threadParmArray[i][10] = new Integer(parm11).intValue();
                    this.threadParmArray[i][11] = new Integer(parm12).intValue();
                    this.threadParmArray[i][12] = new Integer(parm13).intValue();
                    this.threadParmArray[i][13] = new Integer(parm14).intValue();
                    this.threadParmArray[i][14] = new Integer(parm15).intValue();
                    this.threadParmArray[i][15] = new Integer(parm16).intValue();
                } catch (Exception e) {
                    msg = getDCStackTrace(e);
                    return false;
                }
                i++;
                if (i == threads) {
                    finish = true;
                }
            } else {
                finish = true;
            }

        } while (finish == false);
        return true;
    }

    void resetParameters() {
        this.valueSize = 1250;
        this.threads = 1;
        this.delay = 0; // put delay
        this.ddelay = 0; // dep id invalidate delay
        this.tdelay = 0; // template invalidate delay
        this.cdelay = 0; // clear delay
        this.sdelay = 0; // stop delay
        this.adelay = 0; // alias delay
        this.ldelay = 10000; // listener delay
        this.delayAT = 10000; // delay after test
        this.num_depids = 100;
        this.win = 100;
        this.idName = "";
        this.waitInv = true;
        this.oneDepid = false;
        this.invOneDepid = false;
        this.ilistener = false;
        this.clistener = false;
        this.same = false; // use put1 and put2 use the same cache id
        this.idd = false; // use cache id as depid
        this.xdepid = "";
        this.clearBT = true;
        this.savCount = true;
        this.resetCount = false;
        this.resetPMI = false;
        this.exps = -1;
        this.expps = -1;
        this.expd = -1;
        this.exppd = -1;
    }

    static String getDCStackTrace(Throwable e) {

        if (e == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}
