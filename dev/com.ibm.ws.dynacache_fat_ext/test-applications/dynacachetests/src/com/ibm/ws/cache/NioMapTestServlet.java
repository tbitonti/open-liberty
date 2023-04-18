// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.ChangeEvent;
import com.ibm.websphere.cache.DistributedObjectCache;
import com.ibm.websphere.cache.InvalidationEvent;
import com.ibm.websphere.cache.exception.DiskCacheEntrySizeOverLimitException;
import com.ibm.websphere.cache.exception.DiskSizeInEntriesOverLimitException;
import com.ibm.ws.cache.intf.DCache;
import com.ibm.wsspi.cache.DistributedObjectCacheFactory;

public class NioMapTestServlet extends HttpServlet {
    private DCache cache;
    private PrintWriter out;

    private DistributedObjectCache nioMapBasic;
    private DistributedObjectCache nioMapHtod;
    private DistributedObjectCache nioMapPush;
    private DistributedObjectCache nioMapPushPull;

    private com.ibm.websphere.cache.CacheEntry ce;
    private com.ibm.websphere.cache.CacheEntry ce1;
    private com.ibm.websphere.cache.CacheEntry ce2;

    private String msg = "";
    private String v;

    // key as object
    private final MyNioMapObject niomk1 = new MyNioMapObject("key1");
    private final MyNioMapObject niomk2 = new MyNioMapObject("key2");

    // value as object for Cluster Member X
    private MyNioMapObject niomo1 = new MyNioMapObject("value1");
    private MyNioMapObject niomo2 = new MyNioMapObject("value2");
    private MyNioMapObject niomo3 = new MyNioMapObject("value3");
    private MyNioMapObject niomo4 = new MyNioMapObject("value4");
    private final MyNioMapObject niomo5 = new MyNioMapObject("value5");

    // alias as object
    private final MyNioMapObject nioma1_1 = new MyNioMapObject("key1-1");
    private final MyNioMapObject nioma1_2 = new MyNioMapObject("key1-2");
    private final MyNioMapObject nioma1_3 = new MyNioMapObject("key1-3");
    private final MyNioMapObject nioma2_1 = new MyNioMapObject("key2-1");
    private final MyNioMapObject nioma2_2 = new MyNioMapObject("key2-2");
    private final MyNioMapObject nioma2_3 = new MyNioMapObject("key2-3");

    // depId as object
    private final MyNioMapObject niomd1_1 = new MyNioMapObject("dependency id1-1");
    private final MyNioMapObject niomd1_2 = new MyNioMapObject("dependency id1-2");
    private final MyNioMapObject niomd2_1 = new MyNioMapObject("dependency id2-1");
    private final MyNioMapObject niomd2_2 = new MyNioMapObject("dependency id2-2");

    // meta data object
    private final MyNioMapObject niommdata1_1 = new MyNioMapObject("metaData1_1");
    private final MyNioMapObject niommdata1_2 = new MyNioMapObject("metaData1_2");

    private final MyNioMapObject niommdata2_1 = new MyNioMapObject("metaData2_1");
    private final MyNioMapObject niommdata2_2 = new MyNioMapObject("metaData2_2");

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
        //out.println("<font color=\"#ffffff\">");
        out.println("<center><h1>NioMapTestServlet</h1></center>");
        out.println("<br><b>Test selection:</b><br>");
        out.println("<br>");
        out.println("<b><a href=\"?method=basic\">Test basic (String)</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=basicObject\">Test basic (Object)</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=htodObject\">Test HTOD Object</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=htodSkipMemoryWriteToDisk\">Test HTOD SkipMemoryAndWriteToDisk</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=htodExceptionTestManually\">Test HTOD Exception Manually</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=putOnDiskExceptionTest\">Test PutOnDisk Exception</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=cacheEntryOverflow\">Test CacheEntry Overflow</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drs_Push_Setup\">Test DRS - Cluster Push Setup</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsX_Push_1\">Test DRS - Cluster MemberX_Push_1</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsY_Push_1\">Test DRS - Cluster MemberY_Push_1</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsX_Push_2\">Test DRS - Cluster MemberX_Push_2</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsY_Push_2\">Test DRS - Cluster MemberY_Push_2</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsX_Push_3\">Test DRS - Cluster MemberX_Push_3</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsY_Push_3\">Test DRS - Cluster MemberY_Push_3</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drs_PushPull_Setup\">Test DRS - Cluster Push-Pull Setup</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsX_PushPull_1\">Test DRS - Cluster MemberX_PushPull_1</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsY_PushPull_1\">Test DRS - Cluster MemberY_PushPull_1</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsX_PushPull_2\">Test DRS - Cluster MemberX_PushPull_2</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=drsY_PushPull_2\">Test DRS - Cluster MemberY_PushPull_2</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=basicObjectOG\">Test basic (Object) for ObjectGrid</a></b>");
        out.println("<br>");
        String method = req.getParameter("method");
        if (method != null) {
            if (method.equals("basic")) {
                runBasic();
            } else if (method.equals("basicObject")) {
                runBasicObject();
            } else if (method.equals("htodObject")) {
                runHtodObject();
            } else if (method.equals("htodSkipMemoryWriteToDisk")) {
                runSkipMemoryWriteToDisk();
            } else if (method.equals("htodExceptionTestManually")) {
                runExceptionTestManually();
            } else if (method.equals("putOnDiskExceptionTest")) {
                runPutOnDiskExceptionTest();
            } else if (method.equals("cacheEntryOverflow")) {
                runCacheEntryOverflow();
            } else if (method.equals("drs_Push_Setup")) {
                runDrs_Push_Setup();
            } else if (method.equals("drsX_Push_1")) {
                runDrsX_Push_1();
            } else if (method.equals("drsY_Push_1")) {
                runDrsY_Push_1();
            } else if (method.equals("drsX_Push_2")) {
                runDrsX_Push_2();
            } else if (method.equals("drsY_Push_2")) {
                runDrsY_Push_2();
            } else if (method.equals("drsX_Push_3")) {
                runDrsX_Push_3();
            } else if (method.equals("drsY_Push_3")) {
                runDrsY_Push_3();
            } else if (method.equals("drs_PushPull_Setup")) {
                runDrs_PushPull_Setup();
            } else if (method.equals("drsX_PushPull_1")) {
                runDrsX_PushPull_1();
            } else if (method.equals("drsY_PushPull_1")) {
                runDrsY_PushPull_1();
            } else if (method.equals("drsX_PushPull_2")) {
                runDrsX_PushPull_2();
            } else if (method.equals("drsY_PushPull_2")) {
                runDrsY_PushPull_2();
            } else if (method.equals("basicObjectOG")) {
                runBasicObjectObjectGrid();
            }
        }
        if (!msg.equals("")) {
            out.println("<br> Test failure: " + msg + "<br>");
        }
        out.println("</body></html>");
    }

    public void runBasic() {
        out.println("<br>Running basic test (Key as String)...<br>");
        msg = "";
        niomo1.clear();
        niomo2.clear();

        try {
            if (this.nioMapBasic == null) {
                Properties props = new Properties();
                props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
                this.nioMapBasic = DistributedObjectCacheFactory.getMap("NIO_BASIC", props);
            }
            if (this.nioMapBasic == null) {
                msg = "Error (1) NIO map is null";
                return;
            }
            if (!(this.nioMapBasic.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapBasic.getMapType();
                return;
            }
            this.nioMapBasic.clear();
            this.nioMapBasic.setSharingPolicy(EntryInfo.NOT_SHARED);

            out.println("<br>-- put(): key=\"key1\" value=\"value1\" sp=\"NOT_SHARED\" depId=\"dependency id1\" alias=\"key1-1\"");
            this.nioMapBasic.put("key1", niomo1, null, 1, 0, EntryInfo.NOT_SHARED, new Object[] { "dependency id1" }, new Object[] { "key1-1" });

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry("key1");
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (3) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-1\"");
            ce = this.nioMapBasic.getCacheEntry("key1-1");
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (4) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- put(): key=\"key1\" value=\"value2\" sp=\"NOT_SHARED\" depId=\"dependency id2\" alias=\"key1-2\"");
            this.nioMapBasic.put("key1", niomo2, null, 1, 0, EntryInfo.NOT_SHARED, new Object[] { "dependency id2" }, new Object[] { "key1-2" });

            if (niomo1.count != 0) {
                msg = "Error (5) Release count Expected=0 Received=" + niomo1.count;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            if (niomo1.count != 1) {
                msg = "Error (6) Release count Expected=1 Received=" + niomo1.count;
                return;
            }

            out.println("<br>-- addAlias(): key=\"key1\" alias=\"key1-3\"");
            this.nioMapBasic.addAlias("key1", new Object[] { "key1-3" });

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry("key1");
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (7) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-1\"");
            ce = this.nioMapBasic.getCacheEntry("key1-1");
            if (ce != null) {
                msg = "Error (8) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapBasic.getCacheEntry("key1-2");
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (9) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-3\"");
            ce = this.nioMapBasic.getCacheEntry("key1-3");
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (10) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- removeAlias(): alias=\"key1-2\"");
            this.nioMapBasic.removeAlias("key1-2");

            out.println("<br>-- removeAlias(): alias=\"key1-3\"");
            this.nioMapBasic.removeAlias("key1-3");

            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapBasic.getCacheEntry("key1-2");
            if (ce != null) {
                msg = "Error (11) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-3\"");
            ce = this.nioMapBasic.getCacheEntry("key1-3");
            if (ce != null) {
                msg = "Error (12) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry("key1");
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (13) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 2nd put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put");
            ce.finish();

            if (niomo2.count != 0) {
                msg = "Error (14) Release count Expected=0 Received=" + niomo2.count;
                return;
            }

            out.println("<br>-- addAlias(): key=\"key1\" alias=\"key1-1\"");
            this.nioMapBasic.addAlias("key1", new Object[] { "key1-1" });

            out.println("<br>-- invalidate(): alias=\"key1-1\"");
            this.nioMapBasic.invalidate("key1-1", true);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry("key1");
            if (ce != null) {
                msg = "Error (15) CacheEntry Expected=NULL";
                return;
            }

            if (niomo2.count != 1) {
                msg = "Error (16) Release count Expected=1 Received=" + niomo2.count;
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runBasicObject() {
        out.println("<br>Running basic test (Key as Object, DepId as Object)...<br>");
        msg = "";
        niomo1.clear();
        niomo2.clear();

        try {
            if (this.nioMapBasic == null) {
                Properties props = new Properties();
                props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
                this.nioMapBasic = DistributedObjectCacheFactory.getMap("NIO_BASIC", props);
            }
            if (this.nioMapBasic == null) {
                msg = "Error (1) NIO map is null";
                return;
            }
            if (!(this.nioMapBasic.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapBasic.getMapType();
                return;
            }
            this.nioMapBasic.clear();
            this.nioMapBasic.setSharingPolicy(EntryInfo.NOT_SHARED);

            out.println("<br>-- put(): key=\"key1\" value=\"value1\" sp=\"NOT_SHARED\" depId=\"dependency id1\" alias=\"key1-1\"");
            ce = this.nioMapBasic.putAndGet(niomk1, niomo1, null, 1, 0, EntryInfo.NOT_SHARED, new Object[] { niomd1_1 }, new Object[] { nioma1_1 });
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (3) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry(niomk1);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (4) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-1\"");
            ce = this.nioMapBasic.getCacheEntry(nioma1_1);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (5) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- put(): key=\"key1\" value=\"value2\" sp=\"NOT_SHARED\" depId=\"dependency id1_2\" alias=\"key1-2\"");
            ce1 = this.nioMapBasic.putAndGet(niomk1, niomo2, null, 1, 0, EntryInfo.NOT_SHARED, new Object[] { niomd1_2 }, new Object[] { nioma1_2 });
            v = ce1.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (6) Value Expected=value2 Received=" + v;
                return;
            }

            if (niomo1.count != 0) {
                msg = "Error (7) Release count Expected=0 Received=" + niomo1.count;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            if (niomo1.count != 1) {
                msg = "Error (8) Release count Expected=1 Received=" + niomo1.count;
                return;
            }

            out.println("<br>-- addAlias(): key=\"key1\" alias=\"key1-3\"");
            this.nioMapBasic.addAlias(niomk1, new Object[] { nioma1_3 });

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry(niomk1);
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (9) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-1\"");
            ce = this.nioMapBasic.getCacheEntry(nioma1_1);
            if (ce != null) {
                msg = "Error (11) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapBasic.getCacheEntry(nioma1_2);
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (11) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-3\"");
            ce = this.nioMapBasic.getCacheEntry(nioma1_3);
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (12) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- removeAlias(): alias=\"key1-2\"");
            this.nioMapBasic.removeAlias(nioma1_2);

            out.println("<br>-- removeAlias(): alias=\"key1-3\"");
            this.nioMapBasic.removeAlias(nioma1_3);

            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapBasic.getCacheEntry(nioma1_2);
            if (ce != null) {
                msg = "Error (13) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-3\"");
            ce = this.nioMapBasic.getCacheEntry(nioma1_3);
            if (ce != null) {
                msg = "Error (14) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry(niomk1);
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (15) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 2nd put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put");
            ce.finish();

            if (niomo2.count != 0) {
                msg = "Error (16) Release count Expected=0 Received=" + niomo2.count;
                return;
            }

            out.println("<br>-- addAlias(): key=\"key1\" alias=\"key1-1\"");
            this.nioMapBasic.addAlias(niomk1, new Object[] { nioma1_1 });

            out.println("<br>-- invalidate(): alias=\"key1-1\"");
            this.nioMapBasic.invalidate(nioma1_1, true);

            //try {
            //    Thread.sleep(1000);
            //} catch (Exception e) {
            //}

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry(niomk1);
            if (ce != null) {
                msg = "Error (17) CacheEntry Expected=NULL";
                return;
            }

            if (niomo2.count != 1) {
                msg = "Error (18) Release count Expected=1 Received=" + niomo2.count;
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runHtodObject() {
        out.println("<br>Running HTOD Object test...<br>");
        msg = "";
        niomo1.clear();
        niomo2.clear();

        try {
            Properties props = new Properties();
            props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_TRUE);
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
            this.nioMapHtod = DistributedObjectCacheFactory.getMap("NIO_HTOD", props);

            if (this.nioMapHtod == null) {
                msg = "Error (1) NIO map is null <br>";
                return;
            }
            if (!(this.nioMapHtod.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapHtod.getMapType();
                return;
            }
            this.nioMapHtod.clear();

            if (!this.nioMapHtod.isEmpty(true)) {
                msg = "Error (3) nioMap is not empty after clear";
                return;
            }
            this.cache = ((DistributedObjectCacheAdapter) this.nioMapHtod).getCache();
            int cacheSize = cache.getMaxNumberCacheEntries();
            out.println("<br>-- cache size=" + cacheSize);
            if (cacheSize != 100) {
                msg = "Error (4) cachesize != 100";
                return;
            }

            MyNioMapObject[] niomKeys = new MyNioMapObject[cacheSize];
            MyNioMapObject[] niomAlias = new MyNioMapObject[10];

            if (!(cache.getSwapToDisk())) {
                msg = "Error (5) disk offload is NOT enable.";
                return;
            }

            out.println("<br>-- Loop for put() - cache size" + cacheSize);
            for (int i = 0, j = 0; i < cacheSize; i++, j++) {
                if (j == 10) {
                    j = 0;
                }
                MyNioMapObject niomo = new MyNioMapObject("value" + i);
                Object[] aliasArray = null;
                if (i < 10) {
                    aliasArray = new Object[1];
                    niomAlias[i] = new MyNioMapObject("key" + i + "-1");
                    aliasArray[0] = niomAlias[i];
                }
                niomKeys[i] = new MyNioMapObject("key" + i);
                this.nioMapHtod.put(niomKeys[i], niomo, null, 1, 0, EntryInfo.NOT_SHARED, new String[] { "depId" + j }, aliasArray);
            }

            if (this.nioMapHtod.isEmpty(true)) {
                msg = "Error (6) NioMap is empty after put";
                return;
            }

            int count = this.nioMapHtod.size(true);
            if (count != (cacheSize + 10)) {
                msg = "Error (7) NioMap size expected=" + (cacheSize + 10) + " received=" + count;
                return;
            }

            count = this.nioMapHtod.size(false);
            if (count != cacheSize) {
                msg = "Error (8) NioMap size expected=" + cacheSize + " received=" + count;
                return;
            }

            if (!this.nioMapHtod.containsKey(niomKeys[0], true)) {
                msg = "Error (9) NioMap does not contain key0 in memory or disk map";
                return;
            }

            if (this.nioMapHtod.containsKey(niomKeys[0], false)) {
                msg = "Error (10) NioMap contains key0 in memory map";
                return;
            }

            if (!this.nioMapHtod.containsKey(niomAlias[0], true)) {
                msg = "Error (11) NioMap does not contain key0-1 alias in memory or disk map";
                return;
            }

            if (this.nioMapHtod.containsKey(niomAlias[0], false)) {
                msg = "Error (12) NioMap contains key0-1 alias in memory map";
                return;
            }

            java.util.Collection cacheIds = cache.getIdsByRangeDisk(0, -1);
            out.println("<br>-- CacheIds=" + cacheIds);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                if (cacheIds.size() != 10) {
                    msg = "Error (13) CacheIds count expected=10 received=" + cacheIds.size();
                    return;
                }
                Iterator it = cacheIds.iterator();
                ValueSet vs = new ValueSet(10);
                while (it.hasNext()) {
                    MyNioMapObject id = (MyNioMapObject) it.next();
                    vs.add(id.toString());
                }
                for (int i = 0; i < 5; i++) {
                    if (!(vs.contains("key" + i))) {
                        msg = "Error (14) Do not find cacheId - key" + i;
                        return;
                    }
                    if (!(vs.contains("key" + i + "-1"))) {
                        msg = "Error (15) Do not find cacheId - key" + i + "-1";
                        return;
                    }
                }
            } else {
                msg = "Error (16) No cacheIds error";
                return;
            }

            java.util.Collection depIds = cache.getDepIdsByRangeDisk(0, -1);
            out.println("<br>-- dep id: " + depIds);
            if (depIds != null && !depIds.isEmpty()) {
                if (depIds.size() != 5) {
                    msg = "Error (17) depIds count expected=5 received=" + depIds.size();
                    return;
                }
                Iterator it = depIds.iterator();
                int i = 0;
                while (it.hasNext()) {
                    String id = (String) it.next();
                    if (!(id.equals("depId0")) && !(id.equals("depId1")) && !(id.equals("depId2")) && !(id.equals("depId3")) && !(id.equals("depId4"))) {
                        msg = "Error (18) depId=" + id + " not match";
                        return;
                    }
                }
            } else {
                msg = "Error (19) No depIds error";
                return;
            }

            out.println("<br>-- getEntryDisk(): key=" + niomKeys[0]);
            ce = cache.getEntryDisk(niomKeys[0]);
            v = ce.getValue().toString();
            if (!v.equals("value0")) {
                msg = "Error (20) Value Expected=value0 Received=" + v;
                return;
            }

            out.println("<br>-- getEntryDisk(): alias=" + niomAlias[0]);
            ce = cache.getEntryDisk(niomAlias[0]);
            v = ce.getValue().toString();
            if (!v.equals("value0")) {
                msg = "Error (21) Value Expected=value0 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): key=" + niomKeys[0]);
            ce = this.nioMapHtod.getCacheEntry(niomKeys[0]);
            v = ce.getValue().toString();
            if (!v.equals("value0")) {
                msg = "Error (22) Value Expected=value0 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for " + ce.getId());
            ce.finish();

            out.println("<br>-- getCacheEntry(): alias=" + niomAlias[1]);
            ce = this.nioMapHtod.getCacheEntry(niomAlias[1]);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (23) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for " + ce.getId());
            ce.finish();

            MyNioMapObject aliasKey2 = new MyNioMapObject("key2-2");
            out.println("<br>-- addAlias(): key=" + niomKeys[2] + " alias=" + aliasKey2);
            this.nioMapHtod.addAlias(niomKeys[2], new Object[] { aliasKey2 });

            ce = this.nioMapHtod.getCacheEntry(aliasKey2);
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (24) Value Expected=value2 Received=" + v;
                return;
            }
            out.println("<br>-- finish(): CacheEntry for " + ce.getId());
            ce.finish();

            out.println("<br>-- removeAlias(): alias=" + niomAlias[3]);
            this.nioMapHtod.removeAlias(niomAlias[3]);

            out.println("<br>-- getCacheEntry(): alias=" + niomAlias[3]);
            ce = this.nioMapHtod.getCacheEntry(niomAlias[3]);
            if (ce != null) {
                msg = "Error (25) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- getCacheEntry(): key=" + niomKeys[3]);
            ce = this.nioMapHtod.getCacheEntry(niomKeys[3]);
            v = ce.getValue().toString();
            if (!v.equals("value3")) {
                msg = "Error (26) Value Expected=value3 Received=" + v;
                return;
            }
            out.println("<br>-- finish(): CacheEntry for " + ce.getId());
            ce.finish();

            Thread.sleep(20000);

            cacheIds = cache.getIdsByRangeDisk(0, -1);
            out.println("<br>-- cache Ids=" + cacheIds);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                if (cacheIds.size() != 14) {
                    msg = "Error (27) CacheIds count expected=10 received=" + cacheIds.size();
                    return;
                }
                Iterator it = cacheIds.iterator();
                ValueSet vs = new ValueSet(14);
                while (it.hasNext()) {
                    MyNioMapObject id = (MyNioMapObject) it.next();
                    vs.add(id.toString());
                }
                for (int i = 0; i < 9; i++) {
                    if (i == 2) {
                        i += 2;
                    }
                    if (!(vs.contains("key" + i))) {
                        msg = "Error (28) Do not find cacheId - key" + i;
                        return;
                    }
                    if (!(vs.contains("key" + i + "-1"))) {
                        msg = "Error (29) Do not find cacheId - key" + i + "-1";
                        return;
                    }
                }
            } else {
                msg = "Error (30) No cacheIds error";
                return;
            }

            out.println("<br><br>HTOD Object Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    /**
     * In order for this test to run one has to set the custom properties
     * 
     * com.ibm.ws.cache.CacheConfig.lruToDiskTriggerTime --> JVM custom property Frequency with which cache
     * entries in memory are asynchronously offloaded to disk when the disk offload feature is enabled
     * int Value Unit: Milliseconds Lower Bound: 0 Upper Bound: 5000
     * 
     * com.ibm.ws.cache.CacheConfig.lruToDiskTriggerPercent --> JVM custom property Percentage of the memory cache
     * size used as a overflow buffer when disk offload is enabled. Cache entries in the overflow buffer are purged
     * and asynchronously offloaded to disk at a frequency of lruToDiskTriggerTimemilliseconds. If the this memory
     * overflow buffer is full, cache entries are offloaded to disk synchronously on the callers thread.
     * int Value Unit: Percentage Lower Bound: 0 Upper Bound: 100
     */
    public void runSkipMemoryWriteToDisk() {
        out.println("<br>Running HTOD skip memory and write to disk test...<br>");
        msg = "";
        niomo1.clear();
        niomo2.clear();

        try {
            Properties props = new Properties();
            props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_TRUE);
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
            props.put(DistributedObjectCacheFactory.KEY_DISKCACHE_PERFORMANCE_LEVEL, "3");
            this.nioMapHtod = DistributedObjectCacheFactory.getMap("NIO_PUTONDISK", props);

            if (this.nioMapHtod == null) {
                msg = "Error (1) NIO map is null <br>";
                return;
            }
            if (!(this.nioMapHtod.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapHtod.getMapType();
                return;
            }
            this.nioMapHtod.clear();

            if (!this.nioMapHtod.isEmpty(true)) {
                msg = "Error (3) nioMap is not empty after clear";
                return;
            }
            this.cache = ((DistributedObjectCacheAdapter) this.nioMapHtod).getCache();
            int cacheSize = cache.getMaxNumberCacheEntries();
            out.println("<br>-- cache size=" + cacheSize);
            if (cacheSize != 100) {
                msg = "Error (4) cachesize != 100";
                return;
            }
            if (this.nioMapHtod.enableListener(true) == false) {
                msg = "Error (4a) Cannot use the non-async event source";
                return;
            }

            MyEventListenerImpl ilistener = new MyEventListenerImpl("InvalidationListener", 50, out);;
            this.nioMapHtod.addInvalidationListener(ilistener);
            MyEventListenerImpl clistener = new MyEventListenerImpl("ChangeListener", 100, out);
            this.nioMapHtod.addChangeListener(clistener);

            if (!(cache.getSwapToDisk())) {
                msg = "Error (5) disk offload is NOT enable.";
                return;
            }

            out.println("<br>-- Loop for put() - cache size" + cacheSize);
            MyNioMapObject[] niomObjects = new MyNioMapObject[cacheSize];
            // id: 0 - 4   have 300 sec timeout
            // id: 5 - 49  have no timeout
            // id: 50 - 99 have 20 sec timeout
            for (int i = 0, j = 0; i < cacheSize; i++, j++) {
                if (j == 10) {
                    j = 0;
                }
                niomObjects[i] = new MyNioMapObject("value:" + i);
                if (i < cacheSize / 2) {
                    if (i < 5) {
                        this.nioMapHtod.put("key:" + i, niomObjects[i], null, 1, 300, -1, EntryInfo.NOT_SHARED, new String[] { "depId:" + j }, null, true);
                    } else {
                        this.nioMapHtod.put("key:" + i, niomObjects[i], null, 1, 0, -1, EntryInfo.NOT_SHARED, new String[] { "depId:" + j }, null, true);
                    }
                } else {
                    this.nioMapHtod.put("key:" + i, niomObjects[i], null, 1, 30, -1, EntryInfo.NOT_SHARED, new String[] { "depId:" + j }, null, true);
                }
            }

            clistener.waitOnCompletion();
            out.println("<br>-- Wait completion and check result");
            for (int i = 0; i < 100; i++) {
                String rs = clistener.compare(new InvalidationListenerInfo("key:" + i, niomObjects[i], ChangeEvent.NEW_ENTRY_ADDED, ChangeEvent.LOCAL, "NIO_PUTONDISK"), 100);
                if (!rs.equals("")) {
                    msg = "Error (5a): " + rs;
                    return;
                }
            }

            int count = this.nioMapHtod.size(false);
            if (count != 0) {
                msg = "Error (6) disk cache size expected=0 received=" + count;
                return;
            }

            count = this.nioMapHtod.size(true);
            if (count != cacheSize) {
                msg = "Error (7) disk cache size expected=" + cacheSize + " received=" + count;
                return;
            }

            java.util.Collection cacheIds = cache.getIdsByRangeDisk(0, -1);
            out.println("<br>-- After put cache ids on disk are:" + cacheIds);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                if (cacheIds.size() != cacheSize) {
                    msg = "Error (8) CacheIds count expected=" + cacheSize + " received=" + cacheIds.size();
                    return;
                }
                Iterator it = cacheIds.iterator();
                ValueSet vs = new ValueSet(cacheSize);
                while (it.hasNext()) {
                    String id = (String) it.next();
                    vs.add(id);
                }
                for (int i = 0; i < cacheSize; i++) {
                    if (!(vs.contains("key:" + i))) {
                        msg = "Error (9) Do not find cacheId - key" + i;
                        return;
                    }
                }
            } else {
                msg = "Error (10) No cacheIds error";
                return;
            }

            java.util.Collection depIds = cache.getDepIdsByRangeDisk(0, -1);
            out.println("<br>-- After put dep ids on disk are:" + depIds);
            if (depIds != null && !depIds.isEmpty()) {
                if (depIds.size() != 10) {
                    msg = "Error (11) depIds count expected=10 received=" + depIds.size();
                    return;
                }
                Iterator it = depIds.iterator();
                ValueSet vs = new ValueSet(cacheSize);
                while (it.hasNext()) {
                    vs.add(it.next());
                }
                for (int i = 0; i < 10; i++) {
                    if (!(vs.contains("depId:" + i))) {
                        msg = "Error (12) Do not find depId:" + i;
                        return;
                    } else {
                        cacheIds = cache.getCacheIdsByDependencyDisk("depId:" + i);
                        if (cacheIds != null && !cacheIds.isEmpty()) {
                            if (cacheIds.size() != cacheSize / 10) {
                                msg = "Error (13) CacheIds count expected=" + (cacheSize / 10) + " received=" + cacheIds.size();
                                return;
                            }
                            out.println("<br>-- For depId:" + i + " -- cache ids are:" + cacheIds);
                            it = cacheIds.iterator();
                            while (it.hasNext()) {
                                String id = (String) it.next();
                                if (!(id.endsWith(String.valueOf(i)))) {
                                    msg = "Error (14) wrong cacheId: " + id + " for depid:" + i;
                                }
                            }
                        } else {
                            msg = "Error (15) No cacheIds error";
                            return;
                        }

                    }
                }
            } else {
                msg = "Error (16) No depIds error";
                return;
            }

            for (int i = 0; i < cacheSize; i++) {
                out.println("<br>-- getCacheEntry(): key:" + i);
                ce = this.nioMapHtod.getCacheEntry("key:" + i);
                v = ce.getValue().toString();
                if (!v.equals("value:" + i)) {
                    msg = "Error (17) Value Expected=value:" + i + " Received=" + v;
                    return;
                }

                out.println("<br>-- finish(): CacheEntry for " + ce.getId());
                ce.finish();
            }
            count = this.nioMapHtod.size(false);
            if (count != 0) {
                msg = "Error (18) disk cache size expected=0 received=" + count;
                return;
            }

            count = this.nioMapHtod.size(true);
            if (count != cacheSize) {
                msg = "Error (19) disk cache size expected=" + cacheSize + " received=" + count;
                return;
            }

            out.println("<br>-- sleep 40 secs for some entries timeout");
            try {
                Thread.sleep(40000);
            } catch (Exception e) {
            }

            count = this.nioMapHtod.size(true);
            if (count != cacheSize / 2) {
                msg = "Error (21) disk cache size expected=" + cacheSize / 2 + " received=" + count;
                return;
            }

            cacheIds = this.cache.getIdsByRangeDisk(0, -1);
            out.println("<br>-- After timeout cache ids on disk are: " + cacheIds);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                if (cacheIds.size() != cacheSize / 2) {
                    msg = "Error (22) CacheIds count expected=" + cacheSize + " received=" + cacheIds.size();
                    return;
                }
                Iterator it = cacheIds.iterator();
                ValueSet vs = new ValueSet(cacheSize);
                while (it.hasNext()) {
                    String id = (String) it.next();
                    vs.add(id);
                }
                for (int i = 0; i < cacheSize / 2; i++) {
                    if (!(vs.contains("key:" + i))) {
                        msg = "Error (23) Do not find cacheId - key:" + i;
                        return;
                    }
                }
            } else {
                msg = "Error (24) No cacheIds error";
                return;
            }

            depIds = this.cache.getDepIdsByRangeDisk(0, -1);
            out.println("<br>-- After timeout dep ids on disk are: " + depIds);
            if (depIds != null && !depIds.isEmpty()) {
                if (depIds.size() != 10) {
                    msg = "Error (25) depIds count expected=10 received=" + depIds.size();
                    return;
                }
                Iterator it = depIds.iterator();
                ValueSet vs = new ValueSet(cacheSize);
                while (it.hasNext()) {
                    vs.add(it.next());
                }
                for (int i = 0; i < 10; i++) {
                    if (!(vs.contains("depId:" + i))) {
                        msg = "Error (26) Do not find depId " + i;
                        return;
                    } else {
                        Thread.sleep(10000);
                        cacheIds = this.cache.getCacheIdsByDependencyDisk("depId:" + i);
                        if (cacheIds != null && !cacheIds.isEmpty()) {
                            if (cacheIds.size() != cacheSize / 20) {
                                msg = "Error (27) CacheIds count expected=" + (cacheSize / 20) + " received=" + cacheIds.size();
                                return;
                            }
                            out.println("<br>-- For depId" + i + " -- cache ids are:" + cacheIds);
                            it = cacheIds.iterator();
                            while (it.hasNext()) {
                                String id = (String) it.next();
                                if (!(id.endsWith(String.valueOf(i)))) {
                                    msg = "Error (28) wrong cacheId: " + id + " for depid:" + i;
                                }
                            }
                        } else {
                            msg = "Error (29) No cacheIds error";
                            return;
                        }
                    }
                }
                ilistener.waitOnCompletion();
                out.println("<br>-- Wait completion and check result");
                for (int i = cacheSize / 2; i < cacheSize; i++) {
                    String rs = ilistener.compare(new InvalidationListenerInfo("key:" + i, niomObjects[i], InvalidationEvent.DISK_TIMEOUT, ChangeEvent.LOCAL, "NIO_PUTONDISK"), 50);
                    if (!rs.equals("")) {
                        msg = "Error (30): " + rs;
                        return;
                    }
                }
            } else {
                msg = "Error (31) No depIds error";
                return;
            }

            // put overwrite id: 0 - 9 with different value
            clistener.restart(10);
            MyNioMapObject[] niomObjects2 = new MyNioMapObject[cacheSize];
            for (int i = 0; i < 10; i++) {
                niomObjects2[i] = new MyNioMapObject("new value:" + i);
                this.nioMapHtod.put("key:" + i, niomObjects2[i], null, 1, 0, -1, EntryInfo.NOT_SHARED, new String[] { "depId:0" }, null, true);
            }
            clistener.waitOnCompletion();
            out.println("<br>-- Wait completion and check result");
            for (int i = 0; i < 10; i++) {
                String rs = clistener.compare(new InvalidationListenerInfo("key:" + i, niomObjects2[i], ChangeEvent.EXISTING_VALUE_CHANGED, ChangeEvent.LOCAL, "NIO_PUTONDISK"), 10);
                if (!rs.equals("")) {
                    msg = "Error (32): " + rs;
                    return;
                }
            }
            // get id: 0 - 9 and verify the value
            for (int i = 0; i < 10; i++) {
                out.println("<br>-- getCacheEntry(): key:" + i);
                ce = this.nioMapHtod.getCacheEntry("key:" + i);
                v = ce.getValue().toString();
                if (!v.equals("new value:" + i)) {
                    msg = "Error (33) Value Expected=new value:" + i + " Received=" + v;
                    return;
                }

                out.println("<br>-- finish(): CacheEntry for " + ce.getId());
                ce.finish();
            }

            out.println("<br><br>HTOD skip memory and write to disk Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runExceptionTestManually() {
        out.println("<br>Running exception manually test...<br>");
        msg = "";
        niomo1.clear();
        niomo2.clear();

        try {
            Properties props = new Properties();
            props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_TRUE);
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
            props.put(DistributedObjectCacheFactory.KEY_DISKCACHE_PERFORMANCE_LEVEL, "3");
            this.nioMapHtod = DistributedObjectCacheFactory.getMap("NIO_HTOD", props);

            if (this.nioMapHtod == null) {
                msg = "Error (1) NIO map is null <br>";
                return;
            }
            if (!(this.nioMapHtod.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapHtod.getMapType();
                return;
            }
            this.nioMapHtod.clear();

            if (!this.nioMapHtod.isEmpty(true)) {
                msg = "Error (3) nioMap is not empty after clear";
                return;
            }
            this.cache = ((DistributedObjectCacheAdapter) this.nioMapHtod).getCache();
            int cacheSize = cache.getMaxNumberCacheEntries();
            out.println("<br>-- cache size=" + cacheSize);
            if (cacheSize != 100) {
                msg = "Error (4) cachesize != 100";
                return;
            }

            MyNioMapObject[] niomKeys = new MyNioMapObject[cacheSize];
            for (int i = 0; i < 11; i++) {
                niomKeys[i] = new MyNioMapObject("key" + i);
            }

            if (!(cache.getSwapToDisk())) {
                msg = "Error (5) disk offload is NOT enable.";
                return;
            }

            out.println("<br>-- Loop for put() - cache size" + cacheSize);
            for (int i = 0; i < 11; i++) {
                MyNioMapObject niomo = new MyNioMapObject("value" + i);
                Object[] aliasArray = null;
                if (i == 10) {
                    this.nioMapHtod.put(niomKeys[i], niomo, null, 1, 0, -1, EntryInfo.NOT_SHARED, new String[] { "depid-0" }, aliasArray, true);
                } else {
                    this.nioMapHtod.put(niomKeys[i], niomo, null, 1, 0, -1, EntryInfo.NOT_SHARED, new Object[] { niomKeys[10] }, aliasArray, false);
                }
            }

            out.println("<br><br>HTOD exception manually test completed successfully");
        } catch (Exception e) {

            msg = "*** Exception: <br>" + getStackTrace(e);
        } finally {
            try {
                Thread.sleep(15000);
            } catch (Exception e) {
            }
            Enumeration e = cache.getAllIds();
            ArrayList ids = new ArrayList();
            while (e.hasMoreElements()) {
                ids.add(e.nextElement());
            }
            out.println("<br>-- cache ids in memory are: " + ids);

            java.util.Collection depIds = this.cache.getAllDependencyIds();
            out.println("<br>-- dep ids in memory are: " + depIds);

            java.util.Collection cacheIds = this.cache.getIdsByRangeDisk(0, -1);
            out.println("<br>-- cache ids on disk are: " + cacheIds);

            depIds = this.cache.getDepIdsByRangeDisk(0, -1);;
            out.println("<br>-- dep ids on disk are: " + depIds);
        }
    }

    public void runPutOnDiskExceptionTest() {
        out.println("<br>Running PutOnDisk exception test...<br>");
        msg = "";
        niomo1.clear();
        niomo2.clear();

        int cacheSize = 100;
        try {
            Properties props = new Properties();
            props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_TRUE);
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
            props.put(DistributedObjectCacheFactory.KEY_DISKCACHE_PERFORMANCE_LEVEL, "3");
            props.put(DistributedObjectCacheFactory.KEY_DISKCACHE_SIZE, "100");
            props.put(DistributedObjectCacheFactory.KEY_DISKCACHE_ENTRY_SIZE_MB, "1");
            props.put(DistributedObjectCacheFactory.KEY_DISKCACHE_EVICTION_POLICY, "0");
            this.nioMapHtod = DistributedObjectCacheFactory.getMap("NIO_EXCEPTION", props);

            if (this.nioMapHtod == null) {
                msg = "Error (1) NIO map is null <br>";
                return;
            }
            if (!(this.nioMapHtod.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapHtod.getMapType();
                return;
            }
            this.nioMapHtod.clear();

            if (!this.nioMapHtod.isEmpty(true)) {
                msg = "Error (3) nioMap is not empty after clear";
                return;
            }
            this.cache = ((DistributedObjectCacheAdapter) this.nioMapHtod).getCache();
            cacheSize = cache.getMaxNumberCacheEntries();
            out.println("<br>-- cache size=" + cacheSize);
            if (cacheSize != 100) {
                msg = "Error (4) cachesize != 100";
                return;
            }

            if (!(cache.getSwapToDisk())) {
                msg = "Error (5) disk offload is NOT enable.";
                return;
            }

            MyNioMapObject[] niomKeys = new MyNioMapObject[cacheSize + 1];
            for (int i = 0; i < cacheSize + 1; i++) {
                niomKeys[i] = new MyNioMapObject("key" + i);
            }

            out.println("<br>Verify DiskCacheEntrySizeOverLimitException");
            boolean exceptionOccurred = false;
            try {
                long[] longValue = new long[1000000];
                MyNioMapLongObject longNioObject = new MyNioMapLongObject(longValue);
                this.nioMapHtod.put(niomKeys[0], longNioObject, null, 1, 0, -1, EntryInfo.NOT_SHARED, null, null, true);
            } catch (DiskCacheEntrySizeOverLimitException e) {
                exceptionOccurred = true;
            } catch (Exception e) {
                msg = "Error (6) expected=DiskCacheEntrySizeOverLimitException but received=" + e.getMessage();
                return;
            }
            if (exceptionOccurred == false) {
                msg = "Error (7) no exception occurred; expected=DiskCacheEntrySizeOverLimitException";
                return;
            }
            out.println("<br>Verify DiskSizeInEntriesOverLimitException");
            exceptionOccurred = false;
            try {
                for (int i = 0; i < 101; i++) {
                    MyNioMapObject niomo = new MyNioMapObject("value" + i);
                    this.nioMapHtod.put(niomKeys[i], niomo, null, 1, 0, -1, EntryInfo.NOT_SHARED, null, null, true);
                }
            } catch (DiskSizeInEntriesOverLimitException e) {
                exceptionOccurred = true;
            } catch (Exception e) {
                msg = "Error (8) expected=DiskSizeInEntriesOverLimitException but received=" + e.getMessage();
                return;
            }
            if (exceptionOccurred == false) {
                msg = "Error (9) no exception occurred; expected=DiskSizeInEntriesOverLimitException";
                return;
            }

            out.println("<br><br>PutOnDisk exception test completed successfully");

        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
            return;
        }

    }

    public void runCacheEntryOverflow() {
        out.println("<br>Running CacheEntry Overflow test...<br>");
        msg = "";
        niomo1.clear();
        niomo2.clear();

        try {
            Properties props = new Properties();
            props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_TRUE);
            props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
            props.put(DistributedObjectCacheFactory.KEY_DISKCACHE_PERFORMANCE_LEVEL, "3");
            this.nioMapHtod = DistributedObjectCacheFactory.getMap("NIO_CE_OVERFLOW", props);

            if (this.nioMapHtod == null) {
                msg = "Error (1) NIO map is null <br>";
                return;
            }
            if (!(this.nioMapHtod.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapHtod.getMapType();
                return;
            }
            this.nioMapHtod.clear();

            if (!this.nioMapHtod.isEmpty(true)) {
                msg = "Error (3) nioMap is not empty after clear";
                return;
            }
            this.cache = ((DistributedObjectCacheAdapter) this.nioMapHtod).getCache();
            int cacheSize = cache.getMaxNumberCacheEntries();
            out.println("<br>-- cache size=" + cacheSize);
            if (cacheSize != 100) {
                msg = "Error (4) cachesize != 100";
                return;
            }

            this.cache.getCacheStatisticsListener().reset(); // reset statistics

            MyNioMapObject[] niomKeys = new MyNioMapObject[cacheSize + 10];
            com.ibm.websphere.cache.CacheEntry[] cacheEntries = new com.ibm.websphere.cache.CacheEntry[cacheSize + 10];
            for (int i = 0; i < cacheSize + 10; i++) {
                niomKeys[i] = new MyNioMapObject("key" + i);
            }

            if (!(cache.getSwapToDisk())) {
                msg = "Error (5) disk offload is NOT enable.";
                return;
            }

            Map statistics = MBeans.getCacheStatisticsMap(cache.getCacheStatistics());
            //out.println("<br>" + statistics);
            Long count = (Long) statistics.get("MemoryCacheEntries");
            if (count != null) {
                if (count.intValue() != 0) {
                    msg = "Error (6) MemoryCacheEntries expected=0 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (7) MemoryCacheEntries not found.";
            }
            count = (Long) statistics.get("OverflowEntriesFromMemory");
            if (count != null) {
                if (count.intValue() != 0) {
                    msg = "Error (8) OverflowEntriesFromMemory expected=0 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (9) OverflowEntriesFromMemory not found.";
            }
            count = (Long) statistics.get("ObjectsAsyncLruToDisk");
            if (count != null) {
                if (count.intValue() != 0) {
                    msg = "Error (10) ObjectsAsyncLruToDisk expected=0 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (11) ObjectsAsyncLruToDisk not found.";
            }

            out.println("<br>-- Loop for put() - cache size" + cacheSize + " plus 10");
            for (int i = 0; i < cacheSize + 10; i++) {
                MyNioMapObject niomo = new MyNioMapObject("value" + i);
                cacheEntries[i] = this.nioMapHtod.putAndGet(niomKeys[i], niomo, null, 1, 0, -1, EntryInfo.NOT_SHARED, null, null);
            }

            statistics = MBeans.getCacheStatisticsMap(cache.getCacheStatistics());
            //out.println("<br>" + statistics);
            count = (Long) statistics.get("MemoryCacheEntries");
            if (count != null) {
                if (count.intValue() != 110) {
                    msg = "Error (12) MemoryCacheEntries expected=110 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (13) MemoryCacheEntries not found.";
            }
            count = (Long) statistics.get("OverflowEntriesFromMemory");
            if (count != null) {
                if (count.intValue() != 10) {
                    msg = "Error (14) OverflowEntriesFromMemory expected=10 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (15) OverflowEntriesFromMemory not found.";
            }
            count = (Long) statistics.get("ObjectsAsyncLruToDisk");
            if (count != null) {
                if (count.intValue() != 0) {
                    msg = "Error (16) ObjectsAsyncLruToDisk expected=0 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (17) ObjectsAsyncLruToDisk not found.";
            }

            for (int i = 0; i < cacheSize + 0; i++) {
                cacheEntries[i].finish();
            }

            out.println("<br> wait up to 20 sec");
            try {
                for (int i = 0; i < 20; i++) {
                    Thread.sleep(1000);
                    statistics = MBeans.getCacheStatisticsMap(cache.getCacheStatistics());
                    count = (Long) statistics.get("MemoryCacheEntries");
                    if (count != null) {
                        if (count.intValue() == cacheSize) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }

            statistics = MBeans.getCacheStatisticsMap(cache.getCacheStatistics());
            out.println("<br>" + statistics);
            count = (Long) statistics.get("MemoryCacheEntries");
            if (count != null) {
                if (count.intValue() != cacheSize) {
                    msg = "Error (18) MemoryCacheEntries expected=100 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (19) MemoryCacheEntries not found.";
            }
            count = (Long) statistics.get("OverflowEntriesFromMemory");
            if (count != null) {
                if (count.intValue() != 10) {
                    msg = "Error (20) OverflowEntriesFromMemory expected=10 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (21) OverflowEntriesFromMemory not found.";
            }
            count = (Long) statistics.get("ObjectsAsyncLruToDisk");
            if (count != null) {
                if (count.intValue() != 10) {
                    msg = "Error (22) ObjectsAsyncLruToDisk expected=10 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (23) ObjectsAsyncLruToDisk not found.";
            }
            count = (Long) statistics.get("ObjectsOnDisk");
            if (count != null) {
                if (count.intValue() != 10) {
                    msg = "Error (24) ObjectsOnDisk expected=10 but received=" + count.toString();
                    return;
                }
            } else {
                msg = "Error (25) ObjectsOnDisk not found.";
            }

            out.println("<br><br>CacheEntry Overflow test completed successfully");
        } catch (Exception e) {

            msg = "*** Exception: <br>" + getStackTrace(e);
            return;
        } finally {
            //try {
            //    Thread.sleep(15000);
            //} catch (Exception e) {
            //}
            Enumeration e = cache.getAllIds();
            ArrayList ids = new ArrayList();
            while (e.hasMoreElements()) {
                ids.add(e.nextElement());
            }
            out.println("<br>-- cache ids in memory are: " + ids);

            java.util.Collection depIds = this.cache.getAllDependencyIds();
            out.println("<br>-- dep ids in memory are: " + depIds);

            java.util.Collection cacheIds = this.cache.getIdsByRangeDisk(0, -1);
            out.println("<br>-- cache ids on disk are: " + cacheIds);

            depIds = this.cache.getDepIdsByRangeDisk(0, -1);;
            out.println("<br>-- dep ids on disk are: " + depIds);
        }
    }

    public void runDrs_Push_Setup() {
        out.println("<br>Running DRS Cluster Push Setup test...<br>");
        msg = "";

        try {
            if (this.nioMapPush == null) {
                Properties props = new Properties();
                props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_FALSE);
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_CACHE_REPLICATION, DistributedObjectCacheFactory.VALUE_TRUE);
                this.nioMapPush = DistributedObjectCacheFactory.getMap("NIO_PUSH", props);
            }
            if (this.nioMapPush == null) {
                msg = "Error (1) NIO map is null <br>";
                return;
            }
            if (!(this.nioMapPush.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapPush.getMapType();
                return;
            }
            this.nioMapPush.clear();
            this.cache = ((DistributedObjectCacheAdapter) this.nioMapPush).getCache();
            int cacheSize = cache.getMaxNumberCacheEntries();
            out.println("<br>-- cache size=" + cacheSize);
            if (cacheSize != 100) {
                msg = "Error (3) cachesize != 100";
                return;
            }
            this.nioMapPush.setSharingPolicy(EntryInfo.SHARED_PUSH);

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrsX_Push_1() {
        out.println("<br>Running DRS Cluster MemberX_1 Push test...<br>");
        msg = "";
        niomo1.clear();
        niomo2.clear();

        try {
            if (this.nioMapPush == null) {
                this.nioMapPush = DistributedObjectCacheFactory.getMap("NIO_PUSH");
            }

            out.println("<br>-- putAndGet(): key=\"key1\" value=\"value1\" sp=\"SHARED_PUSH\" depId=\"depId1_1\" alias=\"key1-1\"");
            ce = this.nioMapPush.putAndGet(niomk1, niomo1, niommdata1_1, 1, 0, EntryInfo.SHARED_PUSH, new Object[] { niomd1_1 }, new Object[] { nioma1_1 });
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (1) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- addAlias(): key=\"key1\" alias=\"key1-2\"");
            this.nioMapPush.addAlias(niomk1, new Object[] { nioma1_2 });

            out.println("<br>-- getCacheEntry(): alias=\"key1-1\"");
            ce = this.nioMapPush.getCacheEntry(nioma1_1);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (2) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapPush.getCacheEntry(nioma1_2);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (3) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put (key1)");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put (key1)");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put (key1)");
            ce.finish();

            if (niomo1.count != 0) {
                msg = "Error (4) Release count Expected=0 Received=" + niomo1.count;
                return;
            }
            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrsY_Push_1() {
        out.println("<br>Running DRS Cluster MemberY_1 Push test...<br>");
        msg = "";
        niomo3.clear();

        try {
            if (this.nioMapPush == null) {
                this.nioMapPush = DistributedObjectCacheFactory.getMap("NIO_PUSH");
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapPush.getCacheEntry(niomk1);
            niomo1 = (MyNioMapObject) ce.getValue();
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (1) Value Expected=value1 Received=" + v;
                return;
            }
            out.println("<br>-- getCacheEntry(): alias=\"key1-1\"");
            ce = this.nioMapPush.getCacheEntry(nioma1_1);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (2) Value Expected=value1 Received=" + v;
                return;
            }
            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapPush.getCacheEntry(nioma1_2);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (3) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put (key1)");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put (key1)");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put (key1)");
            ce.finish();

            if (niomo1.count != 0) {
                msg = "Error (4) Release count Expected=0 Received=" + niomo1.count;
                return;
            }

            out.println("<br>-- put(): key=\"key2\" value=\"value3\" sp=\"SHARED_PUSH\" depId=\"depId2_1\" alias=\"key2-1\"");
            this.nioMapPush.put(niomk2, niomo3, niommdata2_1, 1, 0, EntryInfo.SHARED_PUSH, new Object[] { niomd2_1 }, new Object[] { nioma2_1 });

            out.println("<br>-- addAlias(): key=\"key1\" alias=\"key2-2\"");
            this.nioMapPush.addAlias(niomk2, new Object[] { nioma2_2 });

            out.println("<br>-- getCacheEntry(): alias=\"key2-1\"");
            ce = this.nioMapPush.getCacheEntry(nioma2_1);
            v = ce.getValue().toString();
            if (!v.equals("value3")) {
                msg = "Error (5) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapPush.getCacheEntry(nioma2_2);
            v = ce.getValue().toString();
            if (!v.equals("value3")) {
                msg = "Error (6) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put (key2)");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put (key2)");
            ce.finish();

            if (niomo3.count != 0) {
                msg = "Error (7) Release count Expected=0 Received=" + niomo3.count;
                return;
            }
            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrsX_Push_2() {
        out.println("<br>Running DRS Cluster MemberX_2 Push test...<br>");
        msg = "";
        niomo2.clear();

        try {
            if (this.nioMapPush == null) {
                this.nioMapPush = DistributedObjectCacheFactory.getMap("NIO_PUSH");
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce1 = this.nioMapPush.getCacheEntry(niomk1);
            niomo1 = (MyNioMapObject) ce1.getValue();
            v = ce1.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (1) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- removeAlias(): alias=\"key1-1\"");
            this.nioMapPush.removeAlias(nioma1_1);

            out.println("<br>-- getCacheEntry(): alias=\"key1-1\"");
            ce = this.nioMapPush.getCacheEntry(nioma1_1);
            if (ce != null) {
                msg = "Error (2) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- putAndGet(): key=\"key1\" value=\"value2\" sp=\"SHARED_PUSH\" depId=\"depId2\" alias=\"key1-3\"");
            ce = this.nioMapPush.putAndGet(niomk1, niomo2, niommdata1_2, 1, 0, EntryInfo.SHARED_PUSH, new Object[] { niomd1_2 }, new Object[] { nioma1_3 });
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (3) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapPush.getCacheEntry(nioma1_2);
            if (ce != null) {
                msg = "Error (4) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-3\"");
            ce2 = this.nioMapPush.getCacheEntry(nioma1_3);
            v = ce2.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (5) Value Expected=value2 Received=" + v;
                return;
            }

            if (niomo1.count != 0) {
                msg = "Error (6) Release count Expected=0 Received=" + niomo1.count;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put (key1)");
            ce1.finish();

            if (niomo1.count != 1) {
                msg = "Error (7) Release count Expected=1 Received=" + niomo1.count;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 2nd put (key1)");
            ce2.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put (key1)");
            ce2.finish();

            if (niomo2.count != 0) {
                msg = "Error (8) Release count Expected=0 Received=" + niomo2.count;
                return;
            }

            out.println("<br>-- getCacheEntry(): key=\"key2\"");
            ce = this.nioMapPush.getCacheEntry(niomk2);
            niomo3 = (MyNioMapObject) ce.getValue();
            v = ce.getValue().toString();
            if (!v.equals("value3")) {
                msg = "Error (9) Value Expected=value3 Received=" + v;
                return;
            }
            out.println("<br>-- getCacheEntry(): alias=\"key2-1\"");
            ce = this.nioMapPush.getCacheEntry(nioma2_1);
            v = ce.getValue().toString();
            if (!v.equals("value3")) {
                msg = "Error (10) Value Expected=value3 Received=" + v;
                return;
            }
            out.println("<br>-- getCacheEntry(): alias=\"key2-2\"");
            ce = this.nioMapPush.getCacheEntry(nioma2_2);
            v = ce.getValue().toString();
            if (!v.equals("value3")) {
                msg = "Error (11) Value Expected=value3 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put (key2)");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put (key2)");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put (key2)");
            ce.finish();

            if (niomo3.count != 0) {
                msg = "Error (12) Release count Expected=0 Received=" + niomo3.count;
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }

    }

    public void runDrsY_Push_2() {
        out.println("<br>Running DRS Cluster MemberY_2 Push test...<br>");
        msg = "";
        niomo4.clear();

        try {
            if (this.nioMapPush == null) {
                this.nioMapPush = DistributedObjectCacheFactory.getMap("NIO_PUSH");
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapPush.getCacheEntry(niomk1);
            niomo2 = (MyNioMapObject) ce.getValue();
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (1) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-3\"");
            ce = this.nioMapPush.getCacheEntry(nioma1_3);
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (2) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 2nd put (key1)");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put (key1)");
            ce.finish();

            if (niomo2.count != 0) {
                msg = "Error (3) Release count Expected=0 Received=" + niomo2.count;
                return;
            }

            out.println("<br>-- getCacheEntry(): key=\"key2\"");
            ce1 = this.nioMapPush.getCacheEntry(niomk2);
            niomo3 = (MyNioMapObject) ce1.getValue();
            v = ce1.getValue().toString();
            if (!v.equals("value3")) {
                msg = "Error (4) Value Expected=value3 Received=" + v;
                return;
            }

            out.println("<br>-- removeAlias(): alias=\"key2-1\"");
            this.nioMapPush.removeAlias(nioma2_1);

            out.println("<br>-- getCacheEntry(): alias=\"key2-1\"");
            ce = this.nioMapPush.getCacheEntry(nioma2_1);
            if (ce != null) {
                msg = "Error (6) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- put(): key=\"key1\" value=\"value4\" sp=\"SHARED_PUSH\" depId=\"depId2_2\" alias=\"key2-3\"");
            this.nioMapPush.put(niomk2, niomo4, niommdata2_2, 1, 0, EntryInfo.SHARED_PUSH, new Object[] { niomd2_2 }, new Object[] { nioma2_3 });

            out.println("<br>-- getCacheEntry(): alias=\"key2-2\"");
            ce = this.nioMapPush.getCacheEntry(nioma2_2);
            if (ce != null) {
                msg = "Error (7) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key2-3\"");
            ce2 = this.nioMapPush.getCacheEntry(nioma2_3);
            v = ce2.getValue().toString();
            if (!v.equals("value4")) {
                msg = "Error (8) Value Expected=value2 Received=" + v;
                return;
            }

            if (niomo3.count != 0) {
                msg = "Error (9) Release count Expected=0 Received=" + niomo3.count;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put (key2)");
            ce1.finish();

            if (niomo3.count != 1) {
                msg = "Error (10) Release count Expected=1 Received=" + niomo3.count;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 2nd put (key2)");
            ce2.finish();

            if (niomo4.count != 0) {
                msg = "Error (11) Release count Expected=0 Received=" + niomo4.count;
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrsX_Push_3() {
        out.println("<br>Running DRS Cluster MemberX_3 Push test...<br>");
        msg = "";

        try {
            if (this.nioMapPush == null) {
                this.nioMapPush = DistributedObjectCacheFactory.getMap("NIO_PUSH");
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapPush.getCacheEntry(niomk1);
            niomo2 = (MyNioMapObject) ce.getValue();
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (1) Value Expected=value2 Received=" + v;
                return;
            }
            out.println("<br>-- finish(): CacheEntry for 2nd put (key1)");
            ce.finish();

            out.println("<br>-- invalidate(): alias=\"key1-3\"");
            this.nioMapPush.invalidate(nioma1_3, true);

            //try {
            //    Thread.sleep(1000);
            //} catch (Exception e) {
            //}

            out.println("<br>-- get(): getCacheEntry=\"key1\"");
            ce = this.nioMapPush.getCacheEntry(niomk1);
            if (ce != null) {
                msg = "Error (1) CacheEntry Expected=NULL";
                return;
            }

            if (niomo2.count != 1) {
                msg = "Error (2) Release count Expected=1 Received=" + niomo2.count;
                return;
            }

            out.println("<br>-- getCacheEntry(): key=\"key2\"");
            ce = this.nioMapPush.getCacheEntry(niomk2);
            niomo4 = (MyNioMapObject) ce.getValue();
            v = ce.getValue().toString();
            if (!v.equals("value4")) {
                msg = "Error (3) Value Expected=value4 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key2-3\"");
            ce = this.nioMapPush.getCacheEntry(nioma2_3);
            v = ce.getValue().toString();
            if (!v.equals("value4")) {
                msg = "Error (4) Value Expected=value4 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 2nd put (key2)");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 2nd put (key2)");
            ce.finish();

            if (niomo4.count != 0) {
                msg = "Error (5) Release count Expected=0 Received=" + niomo4.count;
                return;
            }

            out.println("<br>-- invalidate(): alias=\"key2-3\"");
            this.nioMapPush.invalidate(nioma2_3, true);

            //try {
            //    Thread.sleep(1000);
            //} catch (Exception e) {
            //}

            out.println("<br>-- get(): getCacheEntry=\"key2\"");
            ce = this.nioMapPush.getCacheEntry(niomk2);
            if (ce != null) {
                msg = "Error (6) CacheEntry Expected=NULL";
                return;
            }

            if (niomo4.count != 1) {
                msg = "Error (7) Release count Expected=1 Received=" + niomo4.count;
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrsY_Push_3() {
        out.println("<br>Running DRS Cluster MemberY_3 Push test...<br>");

        try {
            if (this.nioMapPush == null) {
                this.nioMapPush = DistributedObjectCacheFactory.getMap("NIO_PUSH");
            }

            out.println("<br>-- get(): getCacheEntry=\"key1\"");
            ce = this.nioMapPush.getCacheEntry(niomk1);
            if (ce != null) {
                msg = "Error (1) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br>-- get(): getCacheEntry=\"key2\"");
            ce = this.nioMapPush.getCacheEntry(niomk2);
            if (ce != null) {
                msg = "Error (2) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrs_PushPull_Setup() {
        out.println("<br>Running DRS Cluster Push-Pull Setup test...<br>");
        msg = "";

        try {
            if (this.nioMapPushPull == null) {
                Properties props = new Properties();
                props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_FALSE);
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_CACHE_REPLICATION, DistributedObjectCacheFactory.VALUE_TRUE);
                this.nioMapPushPull = DistributedObjectCacheFactory.getMap("NIO_PUSH_PULL", props);
            }
            if (this.nioMapPushPull == null) {
                msg = "Error (1) NIO map is null <br>";
                return;
            }
            if (!(this.nioMapPushPull.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapPushPull.getMapType();
                return;
            }
            this.nioMapPushPull.clear();
            this.cache = ((DistributedObjectCacheAdapter) this.nioMapPushPull).getCache();
            int cacheSize = cache.getMaxNumberCacheEntries();
            out.println("<br>-- cache size=" + cacheSize);
            if (cacheSize != 100) {
                msg = "Error (3) cachesize != 100";
                return;
            }
            this.nioMapPushPull.setSharingPolicy(EntryInfo.SHARED_PUSH_PULL);

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrsX_PushPull_1() {
        out.println("<br>Running DRS Cluster MemberX_1 PushPull test...<br>");
        msg = "";
        niomo1.clear();

        try {
            if (this.nioMapPushPull == null) {
                this.nioMapPushPull = DistributedObjectCacheFactory.getMap("NIO_PUSH_PULL");
            }

            out.println("<br>-- put(): key=\"key1\" value=\"value1\" sp=\"SHARED_PUSH_PULL\" depId=\"depId1_1\" alias=\"key1-1\"");
            this.nioMapPushPull.put(niomk1, niomo1, niommdata1_1, 1, 0, EntryInfo.SHARED_PUSH_PULL, new Object[] { niomd1_1 }, new Object[] { nioma1_1 });

            out.println("<br>-- addAlias(): key=\"key1\" alias=\"key1-2\"");
            this.nioMapPushPull.addAlias(niomk1, new Object[] { nioma1_2 });

            out.println("<br>-- getCacheEntry(): alias=\"key1-1\"");
            ce = this.nioMapPushPull.getCacheEntry(nioma1_1);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (1) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapPushPull.getCacheEntry(nioma1_2);
            if (!v.equals("value1")) {
                msg = "Error (2) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            if (niomo1.count != 0) {
                msg = "Error (3) Release count Expected=0 Received=" + niomo1.count;
                return;
            }
            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrsY_PushPull_1() {
        out.println("<br>Running DRS Cluster MemberY_1 PushPull test...<br>");
        msg = "";
        niomo2.clear();

        try {
            if (this.nioMapPushPull == null) {
                this.nioMapPushPull = DistributedObjectCacheFactory.getMap("NIO_PUSH_PULL");
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapPushPull.getCacheEntry(niomk1);
            niomo1 = (MyNioMapObject) ce.getValue();
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (1) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-1\"");
            ce = this.nioMapPushPull.getCacheEntry(nioma1_1);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (2) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-2\"");
            ce = this.nioMapPushPull.getCacheEntry(nioma1_2);
            if (!v.equals("value1")) {
                msg = "Error (3) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            if (niomo1.count != 0) {
                msg = "Error (4) Release count Expected=0 Received=" + niomo1.count;
                return;
            }
            out.println("<br>-- put(): key=\"key1\" value=\"value2\" sp=\"SHARED_PUSH_PULL\" depId=\"depId1_2\" alias=\"key1-3\"");
            this.nioMapPushPull.put(niomk1, niomo2, niommdata1_2, 1, 0, EntryInfo.SHARED_PUSH_PULL, new Object[] { niomd1_2 }, new Object[] { nioma1_3 });

            if (niomo1.count != 1) {
                msg = "Error (5) Release count Expected=1 Received=" + niomo1.count;
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrsX_PushPull_2() {
        out.println("<br>Running DRS Cluster MemberX_2 PushPull test...<br>");
        msg = "";

        try {
            if (this.nioMapPushPull == null) {
                this.nioMapPushPull = DistributedObjectCacheFactory.getMap("NIO_PUSH_PULL");
            }

            out.println("<br>-- getCacheEntry(): alias=\"key1-3\"");
            ce = this.nioMapPushPull.getCacheEntry(nioma1_3);
            niomo2 = (MyNioMapObject) ce.getValue();
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (1) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapPushPull.getCacheEntry(niomk1);
            v = ce.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (2) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            if (niomo2.count != 0) {
                msg = "Error (3) Release count Expected=0 Received=" + niomo2.count;
                return;
            }

            out.println("<br>-- invalidate(): alias=\"key1-3\"");
            this.nioMapPushPull.invalidate(nioma1_3, true);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapPushPull.getCacheEntry(niomk1);
            if (ce != null) {
                msg = "Error (4) CacheEntry Expected=NULL";
                return;
            }

            if (niomo2.count != 1) {
                msg = "Error (5) Release count Expected=1 Received=" + niomo2.count;
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runDrsY_PushPull_2() {
        out.println("<br>Running DRS Cluster MemberY_2 PushPull test...<br>");
        msg = "";

        try {
            if (this.nioMapPushPull == null) {
                this.nioMapPushPull = DistributedObjectCacheFactory.getMap("NIO_PUSH_PULL");
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapPushPull.getCacheEntry(niomk1);
            if (ce != null) {
                msg = "Error (1) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public void runBasicObjectObjectGrid() {
        out.println("<br>Running basic test (Key as Object, DepId as Object) for ObjectGrid...<br>");
        msg = "";
        niomo1.clear();
        niomo2.clear();

        try {
            if (this.nioMapBasic == null) {
                Properties props = new Properties();
                props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
                this.nioMapBasic = DistributedObjectCacheFactory.getMap("NIO_BASIC", props);
            }
            if (this.nioMapBasic == null) {
                msg = "Error (1) NIO map is null";
                return;
            }
            if (!(this.nioMapBasic.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_NIO_MAP)) {
                msg = "Error (2) it is NOT NIO map - got " + this.nioMapBasic.getMapType();
                return;
            }
            this.nioMapBasic.clear();
            this.nioMapBasic.setSharingPolicy(EntryInfo.NOT_SHARED);

            out.println("<br>-- put(): key=\"key1\" value=\"value1\" sp=\"NOT_SHARED\" depId=\"dependency id1\"");
            ce = this.nioMapBasic.putAndGet(niomk1, niomo1, null, 1, 0, EntryInfo.NOT_SHARED, new Object[] { niomd1_1 }, null);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (3) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry(niomk1);
            v = ce.getValue().toString();
            if (!v.equals("value1")) {
                msg = "Error (4) Value Expected=value1 Received=" + v;
                return;
            }

            out.println("<br>-- put(): key=\"key1\" value=\"value2\" sp=\"NOT_SHARED\" depId=\"dependency id1_2\"");
            ce1 = this.nioMapBasic.putAndGet(niomk1, niomo2, null, 1, 0, EntryInfo.NOT_SHARED, new Object[] { niomd1_2 }, null);
            v = ce1.getValue().toString();
            if (!v.equals("value2")) {
                msg = "Error (6) Value Expected=value2 Received=" + v;
                return;
            }

            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();
            out.println("<br>-- finish(): CacheEntry for 1st put");
            ce.finish();

            out.println("<br>-- invalidate(): key=\"key1\"");
            this.nioMapBasic.invalidate(niomk1, true);

            //try {
            //    Thread.sleep(1000);
            //} catch (Exception e) {
            //}

            out.println("<br>-- getCacheEntry(): key=\"key1\"");
            ce = this.nioMapBasic.getCacheEntry(niomk1);
            if (ce != null) {
                msg = "Error (7) CacheEntry Expected=NULL";
                return;
            }

            out.println("<br><br>Test completed successfully");
        } catch (Exception e) {
            msg = "*** Exception: <br>" + getStackTrace(e);
        }
    }

    public String getStackTrace(Throwable oThrowable) {
        if (oThrowable == null)
            return null;
        StringWriter oStringWriter = new StringWriter();
        PrintWriter oPrintWriter = new PrintWriter(oStringWriter);
        oThrowable.printStackTrace(oPrintWriter);

        return oStringWriter.toString();
    }
}
