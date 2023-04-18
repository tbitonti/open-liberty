// 1.8, 10/15/07
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.DistributedMap;
import com.ibm.ws.cache.DCacheBase;
import com.ibm.ws.cache.EntryInfo;
import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.cache.intf.CacheStatisticsListener;
import com.ibm.ws.cache.spi.DistributedMapFactory;


public class MBeanCounterTestServlet extends HttpServlet {

    private static final long serialVersionUID = 9037800534391313307L;
    private com.ibm.ws.cache.intf.DCache  cacheServlet = null;
    private com.ibm.ws.cache.intf.DCacheConfig cacheServletConfig = null;
    private com.ibm.ws.cache.intf.DCache  cacheDmap = null;
    private com.ibm.ws.cache.intf.DCacheConfig  cacheDmapConfig = null;
    private DistributedMap dmap = null;
    private boolean debug = true;

    private String msg = "";
    private PrintWriter out;

    private EntryInfo ei            = new EntryInfo();
    
    /* Cache parameters */
    private String   cacheName        = null;   // name of cache instance to use
                                                 // -1 means not set by caller
/* Test parameters */
    private int      valueSize        = 1250;   // ??
    private int      threads          = 1;
    private int      delay            = 0;      // put delay
    private int      ddelay           = 0;      // dep id invalidate delay
    private int      tdelay           = 0;      // template invalidate delay
    private int      cdelay           = 0;      // clear delay
    private int      sdelay           = 0;      // stop delay
    private int      adelay           = 0;      // alias delay
    private int      delayAfterPut    = 0;      // delay (sec.)
    private int      delayAfterGet    = 0;      // delay (sec.)
    private int      delayAfterInvalidate    = 0; // delay (sec.)
    private int      ldelay           = 10000;  // listener delay
    private int      delayAT          = 10000;  // delay after test
    private int      num_depids       = 100;    // no. of dep ids
    private int      win              = 100;    // display window
    private String   idName           = "";     // use the idaname for DRS testcases
    private String   serverName       = "";
    private int      serverPort       = 9080;
    private boolean  waitInv          = true;   // sync ...async for invalidations
    private boolean  oneDepid         = false;  // one dep id for all cache-ids 
    private boolean  useTemplate      = false;  // add template to cached objects, use it for invalidating
    private boolean  invOneDepid      = false;  // invalidate one dep id ..always set to false

    private boolean  same             = false;  // use put1 and put2 use the same cache id
    private boolean  idd              = false;  // use cache id as depid
    private String   xdepid           = "";     // invalidate by this dep id for all ids
    private boolean  clearBT          = true;   // clear before test
    private boolean  invokeDC         = true;   // invoke disk cleanup 
    private int      expectedServletDiskCacheSize             = -1;     // server ??
    private int      expectedDmapDiskCacheSize             = -1;     // dmap -1 dont care abt expect value, else check for the expected val
    private int numputs               = 0;
    private int numgets               = 0;
    private int numinvalidates        = 0;
    private int timeToLive            = 0;
    private int sharingPolicy         = 1;
    private boolean usedmap           = false;

    private long sSavCacheHits                        = 0;
    private long sSavCacheMisses                      = 0;
    private long sSavCacheRemoves                     = 0;
    private long sSavCacheLruRemoves                  = 0;
    private long sSavExplicitInvalidationsFromMemory  = 0;
    private long sSavExplicitInvalidationsFromDisk    = 0;
    private long sSavExplicitInvalidationsLocal       = 0;
    private long sSavExplicitInvalidationsRemote      = 0;
    private long sSavTimeoutInvalidationsFromMemory   = 0;
    private long sSavTimeoutInvalidationsFromDisk     = 0;
    private long sSavGarbageCollectorInvalidationsFromDisk = 0;
    private long sSavOverflowInvalidationsFromDisk    = 0;
    private long sSavDepIdsOffloadedToDisk            = 0;
    private long sSavDepIdBasedInvalidationsFromDisk  = 0;
    private long sSavTemplatesOffloadedToDisk         = 0;
    private long sSavTemplateBasedInvalidationsFromDisk = 0;
    private long sSavObjectsReadFromDisk              = 0;
    private long sSavObjectsReadFromDisk4K            = 0;
    private long sSavObjectsReadFromDisk40K           = 0;
    private long sSavObjectsReadFromDisk400K          = 0;
    private long sSavObjectsReadFromDisk4000K         = 0;
    private long sSavObjectsReadFromDiskSize          = 0;
    private long sSavObjectsWriteToDisk               = 0;
    private long sSavObjectsWriteToDisk4K             = 0;
    private long sSavObjectsWriteToDisk40K            = 0;
    private long sSavObjectsWriteToDisk400K           = 0;
    private long sSavObjectsWriteToDisk4000K          = 0;
    private long sSavObjectsWriteToDiskSize           = 0;
    private long sSavObjectsDeleteFromDisk            = 0;
    private long sSavObjectsDeleteFromDisk4K          = 0;
    private long sSavObjectsDeleteFromDisk40K         = 0;
    private long sSavObjectsDeleteFromDisk400K        = 0;
    private long sSavObjectsDeleteFromDisk4000K       = 0;
    private long sSavObjectsDeleteFromDiskSize        = 0;
    private long sSavRemoteInvalidationNotifications  = 0;
    private long sSavRemoteUpdateNotifications        = 0;
    private long sSavRemoteObjectUpdates              = 0;
    private long sSavRemoteObjectUpdateSize           = 0;
    private long sSavRemoteObjectHits                 = 0;
    private long sSavRemoteObjectFetchSize            = 0;
    private long sSavRemoteObjectMisses               = 0;

    private  boolean  dmapUsed         = false;

    public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        this.dmapUsed = false;
        out = response.getWriter();
        response.setContentType("text/html");
        out.println("<html><body topmargin=\"20\" leftmargin=\"20\" text =\"#000000\" bgcolor=\"#FFFFFF\">");
        //out.println("<font color=\"#ffffff\">");
        out.println("<center><h1>MBeanCounterTestServlet</h1></center>");
        out.println("<br><b>Test selection:</b><br>" );
        out.println("<br>" );
        out.println("<b><a href=\"?method=restart\">Restart</a></b>" );
        out.println("<br>" );
        out.println("<b><a href=\"?method=emptytest\">Empty Test</a></b>" );
        out.println("<br>" );

        resetParameters();

        String tmp = request.getParameter("valueSize");
        if (tmp != null ) {
            this.valueSize = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("threads");
        if (tmp != null ) {
            this.threads = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("win");
        if (tmp != null ) {
            this.win = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("waitInv");
        if (tmp != null ) {
            if (tmp.equalsIgnoreCase("true")) {
                this.waitInv = true;
            } else {
                this.waitInv = false;
            }
        }
        tmp = request.getParameter("same");
        if (tmp != null ) {
            if (tmp.equalsIgnoreCase("true")) {
                this.same = true;
            } else {
                this.same = false;
            }
        }
        tmp = request.getParameter("idd");
        if (tmp != null ) {
            if (tmp.equalsIgnoreCase("true")) {
                this.idd = true;
            } else {
                this.idd = false;
            }
        }
        tmp = request.getParameter("clearBT");
        if (tmp != null ) {
            if (tmp.equalsIgnoreCase("true")) {
                this.clearBT = true;
            } else {
                this.clearBT = false;
            }
        }
        tmp = request.getParameter("usedmap");
        if (tmp != null ) {
            if (tmp.equalsIgnoreCase("true")) {
                this.usedmap = true;
            } else {
                this.usedmap = false;
            }
        }
        tmp = request.getParameter("invokeDC");
        if (tmp != null ) {
            if (tmp.equalsIgnoreCase("true")) {
                this.invokeDC = true;
            } else {
                this.invokeDC = false;
            }
        }
        tmp = request.getParameter("idName");
        if (tmp != null ) {
            this.idName = tmp;
        }
        if (idName.equals("")) {
            idName = this.serverName + ":" + this.serverPort;
        }
        tmp = request.getParameter("cacheName");
        if (tmp != null) {
            this.cacheName = tmp;
        }
        tmp = request.getParameter("oneDepid");
        if (tmp != null ) {
            if (tmp.equalsIgnoreCase("true")) {
                this.oneDepid = true;
                this.xdepid = idName + ":dep-id";
            } else {
                this.oneDepid = false;
            }
        }
        tmp = request.getParameter("useTemplate");
        if (tmp != null ) {
            this.useTemplate = tmp.equalsIgnoreCase("true");
        } else {
        	this.useTemplate = false;
        }
        tmp = request.getParameter("timeToLive");
        if (tmp != null ) {
            this.timeToLive = Integer.parseInt(tmp);
        } else {
        	this.timeToLive = -1;
        }
        tmp = request.getParameter("delayAfterPut");
        if (tmp != null) { this.delayAfterPut = Integer.parseInt(tmp); }
        tmp = request.getParameter("delayAfterGet");
        if (tmp != null) { this.delayAfterGet = Integer.parseInt(tmp); }
        tmp = request.getParameter("delayAfterInvalidate");
        if (tmp != null) { this.delayAfterInvalidate = Integer.parseInt(tmp); }
        tmp = request.getParameter("sharingPolicy");
        if (tmp != null ) {
            this.sharingPolicy = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("numputs");
        if (tmp != null ) {
            this.numputs = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("numgets");
        if (tmp != null ) { 
            this.numgets = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("numinvalidates");
        if (tmp != null ) {
            this.numinvalidates = Integer.parseInt(tmp);
        } else {
        	this.numinvalidates = 0;
        }
        tmp = request.getParameter("delay");
        if (tmp != null ) {
            this.delay = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("ddelay");
        if (tmp != null ) {
            this.ddelay = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("tdelay");
        if (tmp != null ) {
            this.tdelay = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("cdelay");
        if (tmp != null ) {
            this.cdelay = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("sdelay");
        if (tmp != null ) {
            this.sdelay = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("adelay");
        if (tmp != null ) {
            this.adelay = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("ldelay");
        if (tmp != null ) {
            this.ldelay = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("delayAT");
        if (tmp != null ) {
            this.delayAT = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("depids");
        if (tmp != null ) {
            this.num_depids = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("exps");
        if (tmp != null ) {
            this.expectedServletDiskCacheSize = Integer.parseInt(tmp);
        }
        tmp = request.getParameter("expd");
        if (tmp != null ) {
            this.expectedDmapDiskCacheSize = Integer.parseInt(tmp);
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
        out.println("<br> useTemplate=" + this.useTemplate);
        out.println("<br> same=" + this.same);
        out.println("<br> idd=" + this.idd);
        out.println("<br> clearBT=" + this.clearBT);
        out.println("<br> invokeDC=" + this.invokeDC);
        out.println("<br> exps=" + this.expectedServletDiskCacheSize);
        out.println("<br> expd=" + this.expectedDmapDiskCacheSize);
        out.println("<hr>");
        
        /*
         * If cacheName has been set, we fetch the named cache instance;
         * otherwise we fetch the base cache.
         */

        if (cacheName == null) {
            cacheName = DCacheBase.DEFAULT_BASE_JNDI_NAME;
        }
        out.println("<br> cacheName=" + cacheName);
        out.println("<hr>");
         
        //Force a look up everytime        
        System.out.println("Looking up:" + cacheName);
        cacheServlet = (com.ibm.ws.cache.intf.DCache) ServerCache.getConfiguredCache(cacheName);
        if (cacheServlet == null) {
            System.out.println("Could not find:" + cacheName);
            com.ibm.websphere.cache.Cache ca = com.ibm.websphere.cache.DynamicCacheAccessor.getCache();
            cacheServlet = (com.ibm.ws.cache.intf.DCache)ca;
            System.out.println("Instantiated cacheServlet:"+cacheServlet.getCacheName());
            out.println("Instantiated cacheServlet:"+cacheServlet.getCacheName());
        }else{
            System.out.println("Retrieved cacheServlet:"+cacheServlet.getCacheName());
            out.println("Retrieved cacheServlet:"+cacheServlet.getCacheName());       
        }
                        
        if (cacheServlet != null) {            
        	cacheServletConfig = cacheServlet.getCacheConfig(); // test, test
            if (debug) System.out.println("cacheServletConfig: " + cacheServletConfig);
        }
        
        if (dmap == null) {
            dmap = DistributedMapFactory.getMap(DCacheBase.DEFAULT_DISTRIBUTED_MAP_NAME);
            cacheDmap = (com.ibm.ws.cache.intf.DCache)ServerCache.getCache(DCacheBase.DEFAULT_DISTRIBUTED_MAP_NAME);
             if (cacheDmap != null) {
                 cacheDmapConfig = cacheDmap.getCacheConfig(); // test, test
             }
        }

        String method = request.getParameter("method");
        if (method != null) {

            if (method.equals("emptytest")) {
                runEmptyTest();
            } else if (method.equals("putandget")) {
                runPutAndGet();
            }
            
            if (!msg.equals("")) {
                out.println("<br> error: Test failure: " + msg);
            }
        }
        out.println("</body></html>");
    }

    public void runEmptyTest() {
    
        msg = "";
        out.println("<br>Running EmptyTest...<br>" );
        
        if ( cacheServlet == null) {
            msg = "runTest1.1 Can't get an instance of cache";
            return;
        }
        
        if ( dmap == null) {
            msg = "runTest1.2 Can't get an instance of dmap";
            return;
        }        
 
        //set the local count instance variables
        saveCountersBeforeTest();
        waitAndGetResult();
    }

    /**
     * Do some puts and gets.
     *
     * Will put 'numputs' items of the specified size, each with a
     * unique cache id.
     *
     * Will then do 'numgets' gets of those items, rotating through the
     * cache ids that were put, starting with the first.
     *
     * Parameters expected: <ul>
     * <li>valueSize - size of items' value in bytes
     * <li>numputs - number of items to put.
     * <li>numgets - number of items to get.
     * <li>numinvalidates - number of items to invalidate after the put and gets - if 0, no invalidates will be done.
     * <li>onedepid - if true, add a common depid to all cached objects, and use it for any invalidations
     * <li>usetemplate - if true, add a common template to all cached objects, and use it for invalidation
     * <li>clearBT - whether to clear before testing
     * <li>usedmap - whether to use dmap
     * <li>timeToLive - lifetime of entries (optional, default 0 = infinite)
     * <li>delayAfterPut - sec. to delay after puts (optional, default=0)
     * <li>delayAfterGet - sec. to delay after gets (optional, default=0)
     * </ul>
     */
    public void runPutAndGet() {
        String m = "runPutAndGet";
        msg = "";
        out.println("<br>Running " + m + "...<br>" );
        if ( cacheServlet == null) {
            msg = m + ".1 Can't get an instance of cache";
            return;
        }

        if ( dmap == null) {
            msg = m + ".2 Can't get an instance of dmap";
            return;
        }

        if (this.clearBT) {
            cacheServlet.clear();
            cacheServlet.clearDisk();      
            cacheServlet.resetPMICounters();
            dmap.clear();
            if (null != cacheDmap){
                cacheDmap.clearDisk();                
            }            
        }

        saveCountersBeforeTest();
        
        Object[] depids = null;
        if (oneDepid) {
            depids = new Object[1];
            depids[0] = "common dep id";
        }
        
        String template = null;
        if (useTemplate) {
            template = m + ":template";
        }

        /* do puts */        
        for (int putnum = 0; putnum < this.numputs; putnum++) {
            String cacheid = m + ":" + putnum;
            
            byte[] value = new byte[this.valueSize];
            
            // just putting the bytes of the iteration into the value to have something there.
            if (valueSize > 0) {
                value[0] = (byte) (putnum & 0xff);
                if (debug) System.out.println(" value[0]:"+ value[0]);
            }
            
            if (valueSize > 1) {
                value[1] = (byte) ((putnum >> 8) & 0xff);
                if (debug)  System.out.println(" value[1]:"+ value[1]);

            }
            if (valueSize > 2){
                value[2] = (byte) ((putnum >> 16) & 0xff);
                if (debug) System.out.println(" value[2]:"+ value[2]);                
            }
            
            
            put(cacheid, value, 1, this.timeToLive, this.sharingPolicy, depids, template);
        }
        
        if (this.delayAfterPut != 0) {
            try {
                Thread.sleep(1000 * this.delayAfterPut);
            } catch (InterruptedException e) {
                msg = m + "delayAfterPut: interrupted" + e;
                return;
            }
        }

        /* do gets */
        for (int getnum = 0; getnum < this.numgets; getnum++) {
            int idnum = getnum % this.numputs;
            String cacheid = m + ":" + idnum;
            byte[] value = (byte[]) get(cacheid);
        }

        if (this.delayAfterGet != 0) {
            try {
                Thread.sleep(1000 * this.delayAfterGet);
            } catch (InterruptedException e) {
                msg = m + "delayAfterGet: interrupted" + e;
                return;
            }
        }
        
        if (debug){
            System.out.println("numinvalidates:"+numinvalidates);
            System.out.println("oneDepid:"+oneDepid);
            System.out.println("useTemplate:"+useTemplate);
        }
        
        /* do invalidates */
        if (numinvalidates > 0) {
            if (oneDepid) {
                invalidateById((String)depids[0], true);
            } else if (useTemplate) {
                invalidateByTemplate(template,true);
            } else {
                for (int invalidatenum = 0; invalidatenum < this.numinvalidates; invalidatenum++) {
                    int idnum = invalidatenum % this.numputs;
                    String cacheid = m + ":" + idnum;
                    invalidateById(cacheid, true);
                }
            }
        }

        if (this.delayAfterInvalidate != 0) {
            try {
                Thread.sleep(1000 * this.delayAfterInvalidate);
            } catch (InterruptedException e) {
                msg = m + "delayAfterInvalidate: interrupted" + e;
                return;
            }
        }
        

        /* report results */
        waitAndGetResult();
        /* Done */
    }

    void put(Object id, Object value, int priority, int timeToLive, int sharingPolicy, Object[] depIds, String template) {                
        if (this.usedmap) {
            if (debug) System.out.println("putting in dmap: "+dmap+" id: "+id);            
            dmap.put(id, value, priority, timeToLive, sharingPolicy, depIds);
        } else {
            if (debug) System.out.println("putting in servlet cache: "+cacheServlet.getCacheName()+ " id: "+id);            
            ei.reset();
            ei.setId(id);
            ei.setTimeLimit(timeToLive);       // seconds
            ei.setSharingPolicy(sharingPolicy);
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
            cacheServlet.invalidateAndSet(ei, value, true);
        }
    }

    Object get(Object key) {
        if (this.usedmap) {
            if (debug)  System.out.println("getting from dmap:"+dmap+" id: "+key+" value: "+dmap.get(key));
            return dmap.get(key);
        } else {
            Object value = cacheServlet.getValue(key, cacheServlet.shouldPull(this.sharingPolicy, key));
            if (debug) {
                System.out.println("getting from servlet cache:"+cacheServlet.getCacheName()+ " id: "+key+" value: "+value);            }
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


    private void saveCountersBeforeTest() {
        
        CacheStatisticsListener csl = cacheServlet.getCacheStatisticsListener();

        this.sSavCacheHits                        = csl.getCacheHitsCount();
        this.sSavCacheMisses                      = csl.getCacheMissesCount();
        this.sSavCacheRemoves                     = csl.getCacheRemovesCount();
        this.sSavCacheLruRemoves                  = csl.getCacheLruRemovesCount();
        this.sSavExplicitInvalidationsFromMemory  = csl.getExplicitInvalidationsFromMemoryCount();
        this.sSavExplicitInvalidationsFromDisk    = csl.getExplicitInvalidationsFromDiskCount();
        this.sSavExplicitInvalidationsLocal       = csl.getExplicitInvalidationsLocalCount();
        this.sSavExplicitInvalidationsRemote      = csl.getExplicitInvalidationsRemoteCount();
        this.sSavTimeoutInvalidationsFromMemory   = csl.getTimeoutInvalidationsFromMemoryCount();
        this.sSavTimeoutInvalidationsFromDisk     = csl.getTimeoutInvalidationsFromDiskCount();
        this.sSavGarbageCollectorInvalidationsFromDisk = csl.getGarbageCollectorInvalidationsFromDiskCount();
        this.sSavOverflowInvalidationsFromDisk    = csl.getOverflowInvalidationsFromDiskCount();
        this.sSavDepIdsOffloadedToDisk     = csl.getDepIdsOffloadedToDiskCount();
        this.sSavDepIdBasedInvalidationsFromDisk  = csl.getDepIdBasedInvalidationsFromDiskCount();
        this.sSavTemplatesOffloadedToDisk         = csl.getTemplatesOffloadedToDiskCount();
        this.sSavTemplateBasedInvalidationsFromDisk = csl.getTemplateBasedInvalidationsFromDiskCount();
        this.sSavObjectsReadFromDisk              = csl.getObjectsReadFromDiskCount();
        this.sSavObjectsReadFromDisk4K            = csl.getObjectsReadFromDisk4KCount();
        this.sSavObjectsReadFromDisk40K           = csl.getObjectsReadFromDisk40KCount();
        this.sSavObjectsReadFromDisk400K          = csl.getObjectsReadFromDisk400KCount();
        this.sSavObjectsReadFromDisk4000K         = csl.getObjectsReadFromDisk4000KCount();
        this.sSavObjectsReadFromDiskSize          = csl.getObjectsReadFromDiskSizeCount();
        this.sSavObjectsWriteToDisk               = csl.getObjectsWriteToDiskCount();
        this.sSavObjectsWriteToDisk4K             = csl.getObjectsWriteToDisk4KCount();
        this.sSavObjectsWriteToDisk40K            = csl.getObjectsWriteToDisk40KCount();
        this.sSavObjectsWriteToDisk400K           = csl.getObjectsWriteToDisk400KCount();
        this.sSavObjectsWriteToDisk4000K          = csl.getObjectsWriteToDisk4000KCount();
        this.sSavObjectsWriteToDiskSize           = csl.getObjectsWriteToDiskSizeCount();
        this.sSavObjectsDeleteFromDisk            = csl.getObjectsDeleteFromDiskCount();
        this.sSavObjectsDeleteFromDisk4K          = csl.getObjectsDeleteFromDisk4KCount();
        this.sSavObjectsDeleteFromDisk40K         = csl.getObjectsDeleteFromDisk40KCount();
        this.sSavObjectsDeleteFromDisk400K        = csl.getObjectsDeleteFromDisk400KCount();
        this.sSavObjectsDeleteFromDisk4000K       = csl.getObjectsDeleteFromDisk4000KCount();
        this.sSavObjectsDeleteFromDiskSize        = csl.getObjectsDeleteFromDiskSizeCount();
        this.sSavRemoteInvalidationNotifications  = csl.getRemoteInvalidationNotificationsCount();
        this.sSavRemoteUpdateNotifications        = csl.getRemoteUpdateNotificationsCount();
        this.sSavRemoteObjectUpdates              = csl.getRemoteObjectUpdatesCount();
        this.sSavRemoteObjectUpdateSize           = csl.getRemoteObjectUpdateSizeCount();
        this.sSavRemoteObjectHits                 = csl.getRemoteObjectHitsCount();
        this.sSavRemoteObjectFetchSize            = csl.getRemoteObjectFetchSizeCount();
        this.sSavRemoteObjectMisses               = csl.getRemoteObjectMissesCount();
    }

    private void waitAndGetResult() {

        if (this.delayAT > 0) {
            System.out.println("*** delayAT start delayAT=" + delayAT/1000 + " sec");
            if (expectedServletDiskCacheSize != -1 || expectedDmapDiskCacheSize != -1 ) {
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
                    if (expectedServletDiskCacheSize != -1 && expectedDmapDiskCacheSize != -1) {
                        boolean bs = false;
                        boolean bd = false;
                        if (cacheServlet.getActualIdsSizeDisk() == expectedServletDiskCacheSize) {
                            if (cacheServlet.getIdsSizeDisk() == cacheServlet.getActualIdsSizeDisk()) {
                                bs = true;
                            }
                        }
                        if (cacheDmap.getActualIdsSizeDisk() == expectedDmapDiskCacheSize) {
                            if (cacheDmap.getIdsSizeDisk() == cacheDmap.getActualIdsSizeDisk()) {
                                bd = true;
                            }
                        }
                        if (bs && bd) {
                            break;
                        }
                    } else if (expectedServletDiskCacheSize != -1) {
                        if (cacheServlet.getActualIdsSizeDisk() == expectedServletDiskCacheSize) {
                            if (cacheServlet.getIdsSizeDisk() == cacheServlet.getActualIdsSizeDisk()) {
                                break;
                            }
                        }
                    } else if (expectedDmapDiskCacheSize != -1) {
                        if (cacheDmap.getActualIdsSizeDisk() == expectedDmapDiskCacheSize) {
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
        
        
        HashSet hs = new HashSet(10);
        Collection c = cacheServlet.getAllDependencyIds();
        if (c != null && c.size() > 0) {
            hs.addAll(c);
        }
        c = cacheServlet.getDepIdsByRangeDisk(0, -1);
        if (c != null && c.size() > 0) {
            hs.addAll(c);
        }
        
        int depIdSize = hs.size();
        CacheStatisticsListener csl = cacheServlet.getCacheStatisticsListener();
        StringBuffer resultBuffer = new StringBuffer();
        resultBuffer.append("Result: sMemEntriesSize=");
        resultBuffer.append(cacheServlet.getNumberCacheEntries());
        resultBuffer.append(" sDiskEntriesSize=");
        resultBuffer.append(cacheServlet.getIdsSizeDisk());
        resultBuffer.append(" sActualDiskEntriesSize=");
        resultBuffer.append(cacheServlet.getActualIdsSizeDisk());
        resultBuffer.append(" sDiskDepIdSize=");
        resultBuffer.append(depIdSize);
        resultBuffer.append(cacheServlet.getDepIdsSizeDisk());
        resultBuffer.append(" sDiskTemplateSize=");
        resultBuffer.append(cacheServlet.getTemplatesSizeDisk());
        resultBuffer.append(" sTotalCacheDataDiskSize=");
        resultBuffer.append(cacheServlet.getCacheSizeInBytesDisk());
        resultBuffer.append(" sCacheHits=");
        resultBuffer.append(csl.getCacheHitsCount() - sSavCacheHits);
        resultBuffer.append(" sCacheMisses=");
        resultBuffer.append(csl.getCacheMissesCount() - sSavCacheMisses);
        resultBuffer.append(" sRemoves=");
        resultBuffer.append(csl.getCacheRemovesCount() - sSavCacheRemoves);
        resultBuffer.append(" sLruRemoves=");
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
        resultBuffer.append(" sPendingRemovalSizeDisk=");
        resultBuffer.append(cacheServlet.getPendingRemovalSizeDisk());
        resultBuffer.append(" sDepIdsBufferedSizeDisk=");
        resultBuffer.append(cacheServlet.getDepIdsBufferedSizeDisk());
        resultBuffer.append(" sDepIdsOffloadedToDisk=");
        resultBuffer.append(csl.getDepIdsOffloadedToDiskCount() - sSavDepIdsOffloadedToDisk);
        resultBuffer.append(" sDepIdBasedInvalidationsFromDisk=");
        resultBuffer.append(csl.getDepIdBasedInvalidationsFromDiskCount() - sSavDepIdBasedInvalidationsFromDisk);
        resultBuffer.append(" sTemplatesBufferedSizeDisk=");
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
        resultBuffer.append(csl.getObjectsWriteToDiskSizeCount()- sSavObjectsWriteToDiskSize);
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
        resultBuffer.append(csl.getRemoteInvalidationNotificationsCount()- sSavRemoteInvalidationNotifications);
        resultBuffer.append(" sRemoteUpdateNotifications=");
        resultBuffer.append(csl.getRemoteUpdateNotificationsCount() - sSavRemoteUpdateNotifications);
        resultBuffer.append(" sRemoteObjectUpdates=");
        resultBuffer.append(csl.getRemoteObjectUpdatesCount() - sSavRemoteObjectUpdates);
        resultBuffer.append(" sRemoteObjectUpdateSize=");
        resultBuffer.append(csl.getRemoteObjectUpdateSizeCount() - sSavRemoteObjectUpdateSize);
        resultBuffer.append(" sRemoteObjectHits=");
        resultBuffer.append(csl.getRemoteObjectHitsCount()- sSavRemoteObjectHits);
        resultBuffer.append(" sRemoteObjectFetchSize=");
        resultBuffer.append(csl.getRemoteObjectFetchSizeCount() - sSavRemoteObjectFetchSize);
        resultBuffer.append(" sRemoteObjectMisses=");
        resultBuffer.append(csl.getRemoteObjectMissesCount() - sSavRemoteObjectMisses);
        resultBuffer.append(" ");
        if (this.dmapUsed) {
            resultBuffer.append("****");
            hs.clear();
            c = cacheDmap.getAllDependencyIds();
            if (c != null && c.size() > 0) {
                hs.addAll(c);
            }
            c = cacheDmap.getDepIdsByRangeDisk(0, -1);
            if (c != null && c.size() > 0) {
                hs.addAll(c);
            }
            depIdSize = hs.size();
            resultBuffer.append(" dMemEntriesSize=");
            resultBuffer.append(cacheDmap.getNumberCacheEntries());
            resultBuffer.append(" dDiskEntriesSize=");
            resultBuffer.append(cacheDmap.getIdsSizeDisk());
            resultBuffer.append(" dActualDiskEntriesSize=");
            resultBuffer.append(cacheDmap.getActualIdsSizeDisk());
            resultBuffer.append(" dDiskDepIdSize=");
            resultBuffer.append(depIdSize);
            resultBuffer.append(cacheDmap.getDepIdsSizeDisk());
            resultBuffer.append(" dDiskTemplateSize=");
            resultBuffer.append(cacheDmap.getTemplatesSizeDisk());
            resultBuffer.append(" dTotalCacheDataDiskSize=");
            resultBuffer.append(cacheDmap.getCacheSizeInBytesDisk());
        }
        
        resultBuffer.append(" ");
        String result = resultBuffer.toString();
        System.out.println("*** " + result);
        out.println("<br> " + result);
        
    }

    void resetParameters() {
    	this.cacheServletConfig = null;
        this.cacheServlet     = null;
        this.cacheDmap        = null;
        this.cacheDmapConfig  = null;
        this.cacheName        = null;
        this.ei               = new EntryInfo();
        this.valueSize        = 1250;
        this.threads          = 1;
        this.delay            = 0;      // put delay
        this.ddelay           = 0;      // dep id invalidate delay
        this.tdelay           = 0;      // template invalidate delay
        this.cdelay           = 0;      // clear delay
        this.sdelay           = 0;      // stop delay
        this.adelay           = 0;      // alias delay
        this.delayAfterPut    = 0;
        this.delayAfterGet    = 0;
        this.ldelay           = 10000;  // listener delay
        this.delayAT          = 10000;  // delay after test
        this.num_depids       = 100;
        this.win              = 100;
        this.idName           = "";
        this.waitInv          = true;
        this.oneDepid         = false;
        this.invOneDepid      = false;
        this.same             = false;  // use put1 and put2 use the same cache id
        this.idd              = false;  // use cache id as depid
        this.xdepid           = "";
        this.clearBT          = true;
        this.invokeDC         = true;
        this.expectedServletDiskCacheSize             = -1;
        this.expectedDmapDiskCacheSize             = -1;
        this.timeToLive       = 0;
        this.numinvalidates   = 0;
    }

    static String getStackTrace(Throwable e) {

        if (e == null ) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw  = new PrintWriter( sw );
        e.printStackTrace( pw );
        return sw.toString();
    }

}
