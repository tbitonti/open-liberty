// 1.11, 10/9/07
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.DistributedObjectCache;
import com.ibm.ws.cache.intf.DCache;
import com.ibm.wsspi.cache.DistributedObjectCacheFactory;

public class HTODTestServlet extends HttpServlet {
    private PrintWriter out;

    private DCache cache;
    private int cacheSize = 0;
    private String msg = "";
    private Collection cacheIds;
    private Collection depIds;
    private Collection templates;
    private String v;
    private int cachesize = 2000;
    private com.ibm.websphere.cache.CacheEntry ce = null;
    private int valueSize = 1250;
    private DistributedObjectCache dMap;

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
        out.println("<center><h1>HTODTestServlet</h1></center>");
        out.println("<br><b>Test selection:</b><br>");
        out.println("<br>");
        out.println("<b><a href=\"?method=htod1\">Test HTOD1</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=htod2\">Test HTOD2</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=htod3\">Test HTOD3</a></b>");
        out.println("<br>");
        out.println("<b><a href=\"?method=htod4\">Test HTOD4</a></b>");
        out.println("<br>");
        String tmp = req.getParameter("valueSize");
        if (tmp != null) {
            this.valueSize = new Integer(tmp).intValue();
        }
        String method = req.getParameter("method");
        if (method != null) {

            if (method.equals("htod1")) {
                runHtod1();
            } else if (method.equals("htod2")) {
                runHtod2();
            } else if (method.equals("htod3")) {
                out.println("<br> valueSize=" + valueSize);
                runHtod3();
            } else if (method.equals("htod3a")) {
                runHtod3a();
            } else if (method.equals("htod4")) {
                runHtod4();
            }
            if (!msg.equals("")) {
                out.println("<br> Test failure: " + msg);
            }
        }
        out.println("</body></html>");
    }

    public void runHtod1() {
        msg = "";
        out.println("<br>Running HTOD1 test...<br>");

        com.ibm.websphere.cache.Cache ca = com.ibm.websphere.cache.DynamicCacheAccessor.getCache();

        cache = (DCache) ca;

        if (cache == null) {
            msg = "runHtod1.1 Can't get an instance of cache";
            return;
        }

        if (!(cache.getSwapToDisk())) {
            msg = "runHtod1.2 Disk offLoad is not enabled";
            return;
        }

        cacheSize = cache.getMaxNumberCacheEntries();
        out.println("<br>-- Cache size=" + cacheSize);

        if (cacheSize < 30) {
            msg = "runHtod1.3 Cache size < 30";
            return;
        }

        try {

            cache.clear();
            Thread.sleep(20000); ///give the machine some breathing time

            out.println("<br>-- Loop for put() - " + (cacheSize + 20));
            for (int i = 0, j = 0; i < cacheSize + 20; i++, j++) {
                if (j == 10) {
                    j = 0;
                }
                if (i > 19) {
                    j = 20;
                }
                EntryInfo ei = new EntryInfo();
                ei.setId("key" + i);
                ei.addTemplate("htod1Template");
                ei.setTimeLimit(0);
                ei.setSharingPolicy(EntryInfo.NOT_SHARED);
                ei.addDataId("depid" + j);
                cache.setValue(ei, "value" + i, !Cache.COORDINATE);
            }

            int count = cache.getIdsSizeDisk();
            if (count != 20) {
                msg = "runHtod1.4 CacheIds count expected=20 received=" + count;
                return;
            }

            //cacheIds = cache.getIdsByRangeDisk(0, -1);
            cacheIds = cache.getIdsByRangeDisk(0, 2);
            if (!cacheIds.contains(HTODDynacache.DISKCACHE_MORE)) {
                msg = "runHtod1.4a collection does not contain " + HTODDynacache.DISKCACHE_MORE;
                return;
            }
            cacheIds.remove(HTODDynacache.DISKCACHE_MORE);
            boolean more = true;
            Collection c = null;
            do {
                c = cache.getIdsByRangeDisk(1, 2);
                if (c != null) {
                    if (c.contains(HTODDynacache.DISKCACHE_MORE)) {
                        c.remove(HTODDynacache.DISKCACHE_MORE);
                        cacheIds.addAll(c);
                    } else {
                        cacheIds.addAll(c);
                        more = false;
                    }
                } else {
                    more = false;
                }
            } while (more == true);

            if (cacheIds != null && !cacheIds.isEmpty()) {
                out.println("<br>-- CacheIds=" + cacheIds);
                if (cacheIds.size() != 20) {
                    msg = "runHtod1.5 CacheIds count expected=20 received=" + cacheIds.size();
                    return;
                }
                for (int i = 0; i < 20; i++) {
                    if (!cacheIds.contains("key" + i)) {
                        msg = "runHtod1.6 Do not find cacheId key" + i;
                        return;
                    }
                }
            } else {
                msg = "runHtod1.7 No cacheIds error";
                return;
            }

            cacheIds = cache.getIdsByRangeDisk(0, 3);
            cacheIds.remove(HTODDynacache.DISKCACHE_MORE);
            more = true;
            do {
                c = cache.getIdsByRangeDisk(1, 3);
                if (c != null) {
                    if (c.contains(HTODDynacache.DISKCACHE_MORE)) {
                        c.remove(HTODDynacache.DISKCACHE_MORE);
                        cacheIds.addAll(c);
                    } else {
                        cacheIds.addAll(c);
                        more = false;
                    }
                } else {
                    more = false;
                }
            } while (more == true);

            if (cacheIds != null && !cacheIds.isEmpty()) {
                out.println("<br>-- CacheIds=" + cacheIds);
                if (cacheIds.size() != 20) {
                    msg = "runHtod1.8 CacheIds count expected=20 received=" + cacheIds.size();
                    return;
                }
                for (int i = 0; i < 20; i++) {
                    if (!cacheIds.contains("key" + i)) {
                        msg = "runHtod1.9 Do not find cacheId key" + i;
                        return;
                    }
                }
            } else {
                msg = "runHtod1.10 No cacheIds error";
                return;
            }

            Collection c1 = cache.getIdsByRangeDisk(0, 5);
            c1.remove(HTODDynacache.DISKCACHE_MORE);
            Collection c2 = cache.getIdsByRangeDisk(1, 5);
            c2.remove(HTODDynacache.DISKCACHE_MORE);
            Collection c3 = cache.getIdsByRangeDisk(1, 5);
            c3.remove(HTODDynacache.DISKCACHE_MORE);
            Collection c4 = cache.getIdsByRangeDisk(1, 5);
            c4.remove(HTODDynacache.DISKCACHE_MORE);
            c = cache.getIdsByRangeDisk(-1, 5);
            c.remove(HTODDynacache.DISKCACHE_MORE);
            if (!c.containsAll(c3)) {
                msg = "runHtod1.11 Get previous expected=" + c3 + " received=" + c;
            }
            c = cache.getIdsByRangeDisk(-1, 5);
            c.remove(HTODDynacache.DISKCACHE_MORE);
            if (!c.containsAll(c2)) {
                msg = "runHtod1.12 Get previous expected=" + c2 + " received=" + c;
            }
            c = cache.getIdsByRangeDisk(-1, 5);
            c.remove(HTODDynacache.DISKCACHE_MORE);
            if (!c.containsAll(c1)) {
                msg = "runHtod1.13 Get previous expected=" + c1 + " received=" + c;
            }

            depIds = cache.getDepIdsByRangeDisk(0, 3);
            if (!depIds.contains(HTODDynacache.DISKCACHE_MORE)) {
                msg = "runHtod1.13a collection does not contain " + HTODDynacache.DISKCACHE_MORE;
                return;
            }
            depIds.remove(HTODDynacache.DISKCACHE_MORE);
            more = true;
            do {
                c = cache.getDepIdsByRangeDisk(1, 3);
                if (c != null) {
                    if (c.contains(HTODDynacache.DISKCACHE_MORE)) {
                        c.remove(HTODDynacache.DISKCACHE_MORE);
                        depIds.addAll(c);
                    } else {
                        depIds.addAll(c);
                        more = false;
                    }
                } else {
                    more = false;
                }
            } while (more == true);

            count = cache.getDepIdsSizeDisk();
            if (count != 10) {
                msg = "runHtod1.14 DepIds count expected=10 received=" + count;
                return;
            }

            depIds = cache.getDepIdsByRangeDisk(0, -1);
            if (depIds != null && !depIds.isEmpty()) {
                out.println("<br>-- DepIds=" + depIds);
                if (depIds.size() != 10) {
                    msg = "runHtod1.15 DepIds count expected=10 received=" + depIds.size();
                    return;
                }
                for (int i = 0, k = 0; i < 10; i++, k++) {
                    if (!depIds.contains("depid" + i)) {
                        msg = "runHtod1.16 Do not find depId - depid" + i;
                        return;
                    }
                    cacheIds = cache.getCacheIdsByDependencyDisk("depid" + i);
                    if (cacheIds != null && !cacheIds.isEmpty()) {
                        out.println("<br>-- CacheIds=" + cacheIds);
                        if (cacheIds.size() != 2) {
                            msg = "runHtod1.17 CacheIds count expected=2 received=" + cacheIds.size();
                            return;
                        }
                        for (int j = 0; j < 2; j++) {
                            if (!cacheIds.contains("key" + (k + 10))) {
                                msg = "runHtod1.18 Do not find cacheId key" + i;
                                return;
                            }
                        }
                    } else {
                        msg = "runHtod1.19 No cacheIds error";
                        return;
                    }
                }
            } else {
                msg = "runHtod1.20 No depIds error";
                return;
            }

            count = cache.getTemplatesSizeDisk();
            if (count != 1) {
                msg = "runHtod1.21 Templates count expected=1 received=" + count;
                return;
            }

            templates = cache.getTemplatesByRangeDisk(0, -1);
            if (templates != null && !templates.isEmpty()) {
                out.println("<br>-- Templates=" + templates);
                if (templates.size() != 1) {
                    msg = "runHtod1.22 Templates count expected=1 received=" + templates.size();
                    return;
                }
                if (!templates.contains("htod1Template")) {
                    msg = "runHtod1.23 Do not find template - htod1Template";
                    return;
                }
                cacheIds = cache.getCacheIdsByTemplateDisk("htod1Template");
                if (cacheIds != null && !cacheIds.isEmpty()) {
                    out.println("<br>-- CacheIds=" + cacheIds);
                    if (cacheIds.size() != 20) {
                        msg = "runHtod1.24 CacheIds count expected=20 received=" + cacheIds.size();
                        return;
                    }
                    for (int i = 0; i < 20; i++) {
                        if (!cacheIds.contains("key" + i)) {
                            msg = "runHtod1.25 Do not find cacheId key" + i;
                            return;
                        }
                    }
                } else {
                    msg = "runHtod1.26 No cacheIds error";
                    return;
                }
            } else {
                msg = "runHtod1.27 No templates error";
                return;
            }

            out.println("<br>-- Invalidate depid1, key0, key10");
            cache.invalidateById("key0", true);
            cache.invalidateById("key10", true);
            cache.invalidateById("depid1", true);

            Thread.sleep(20000);

            count = cache.getIdsSizeDisk();
            if (count != 16) {
                msg = "runHtod1.28 CacheIds count expected=16 received=" + count;
                return;
            }
            cacheIds = cache.getIdsByRangeDisk(0, -1);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                out.println("<br>-- CacheIds=" + cacheIds);
                if (cacheIds.size() != 16) {
                    msg = "runHtod1.29 CacheIds count expected=16 received=" + cacheIds.size();
                    return;
                }
                for (int i = 2; i < 20; i++) {
                    if (i != 10 && i != 11) {
                        if (!cacheIds.contains("key" + i)) {
                            msg = "runHtod1.30 Do not find cacheId key" + i;
                            return;
                        }
                    }
                }
            } else {
                msg = "runHtod1.31 No cacheIds error";
                return;
            }
            // skip the following test
            //count = cache.getDepIdsSizeDisk();
            //if (count != 8) {
            //    msg = "runHtod1.32 Depids count expected=8 received=" + count;
            //    return;
            //}

            depIds = cache.getDepIdsByRangeDisk(0, -1);
            if (depIds != null && !depIds.isEmpty()) {
                out.println("<br>-- DepIds=" + depIds);
                // skip the following test
                //if (depIds.size() != 8) {
                //    msg = "runHtod1.33 Depids count expected=8 received=" + depIds.size();
                //    return;
                //}
                for (int i = 2, k = 2; i < 10; i++, k++) {
                    if (!depIds.contains("depid" + i)) {
                        msg = "runHtod1.34 Do not find depId - depid" + i;
                        return;
                    }
                    cacheIds = cache.getCacheIdsByDependencyDisk("depid" + i);
                    if (cacheIds != null && !cacheIds.isEmpty()) {
                        out.println("<br>-- CacheIds=" + cacheIds);
                        if (cacheIds.size() != 2) {
                            msg = "runHtod1.35 CacheIds count expected=2 received=" + cacheIds.size();
                            return;
                        }
                        for (int j = 0; j < 2; j++) {
                            if (!cacheIds.contains("key" + (k + 10))) {
                                msg = "runHtod1.36 Do not find cacheId key" + i;
                                return;
                            }
                        }
                    } else {
                        msg = "runHtod1.37 No cacheIds error";
                        return;
                    }
                }
            } else {
                msg = "runHtod1.38 No depids error";
                return;
            }

            count = cache.getTemplatesSizeDisk();
            if (count != 1) {
                msg = "runHtod1.39 Templates count expected=1 received=" + count;
                return;
            }

            templates = cache.getTemplatesByRangeDisk(0, -1);
            if (templates != null && !templates.isEmpty()) {
                out.println("<br>-- Templates=" + templates);
                if (templates.size() != 1) {
                    msg = "runHtod1.40 Templates count expected=1 received=" + templates.size();
                    return;
                }
                if (!templates.contains("htod1Template")) {
                    msg = "runHtod1.41 Do not find template - htod1Template";
                    return;
                }
                cacheIds = cache.getCacheIdsByTemplateDisk("htod1Template");
                if (cacheIds != null && !cacheIds.isEmpty()) {
                    out.println("<br>-- CacheIds=" + cacheIds);
                    if (cacheIds.size() != 16) {
                        msg = "runHtod1.42 CacheIds count expected=16 received=" + cacheIds.size();
                        return;
                    }
                    for (int i = 2; i < 20; i++) {
                        if (i != 10 && i != 11) {
                            if (!cacheIds.contains("key" + i)) {
                                msg = "runHtod1.43 Do not find cacheId key" + i;
                                return;
                            }
                        }
                    }
                } else {
                    msg = "runHtod1.44 No cacheIds error";
                    return;
                }
            } else {
                msg = "runHtod1.45 No templates error";
                return;
            }

            out.println("<br>-- Invalidate depid2-depid8, key9, key19");
            cache.invalidateById("depid2", true);
            cache.invalidateById("depid3", true);
            cache.invalidateById("depid4", true);
            cache.invalidateById("depid5", true);
            cache.invalidateById("depid6", true);
            cache.invalidateById("depid7", true);
            cache.invalidateById("depid8", true);
            cache.invalidateById("key9", true);
            cache.invalidateById("key19", true);
            Thread.sleep(20000);
            count = cache.getIdsSizeDisk();
            if (count != 0) {
                msg = "runHtod1.46 CacheIds count expected=0 received=" + count;
                return;
            }
            cacheIds = cache.getIdsByRangeDisk(0, -1);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                msg = "runHtod1.47 CacheIds expected=empty but received=" + cacheIds;
                return;
            }
            // skip the following test
            //count = cache.getDepIdsSizeDisk();
            //if (count != 0) {
            //    msg = "runHtod1.48 Depids count expected=0 received=" + count;
            //    return;
            //}
            depIds = cache.getDepIdsByRangeDisk(0, -1);
            if (depIds != null && !depIds.isEmpty()) {
                Iterator it = depIds.iterator();
                while (it.hasNext()) {
                    String id = (String) it.next();
                    cacheIds = cache.getCacheIdsByDependencyDisk(id);
                    if (cacheIds != null && !cacheIds.isEmpty()) {
                        msg = "runHtod1.49 cacheIds is not empty for depId:" + id + " but received=" + cacheIds.toString();
                        return;
                    }
                }
            }
            // skip the following test
            //count = cache.getTemplatesSizeDisk();
            //if (count != 0) {
            //    msg = "runHtod1.50 Templates count expected=0 received=" + count;
            //    return;
            //}
            templates = cache.getTemplatesByRangeDisk(0, -1);
            if (templates != null && !templates.isEmpty()) {
                Iterator it = depIds.iterator();
                while (it.hasNext()) {
                    String id = (String) it.next();
                    cacheIds = cache.getCacheIdsByTemplateDisk(id);
                    if (cacheIds != null && !cacheIds.isEmpty()) {
                        msg = "runHtod1.51 cacheIds is not empty for template:" + id + " but received=" + cacheIds.toString();
                        return;
                    }
                }
            }

            out.println("<br>-- Loop for additional 10 put() - ");
            for (int i = cacheSize + 20; i < cacheSize + 30; i++) {
                EntryInfo ei = new EntryInfo();
                ei.setId("key" + i);
                ei.addTemplate("htod1Template");
                ei.setTimeLimit(0);
                ei.setSharingPolicy(EntryInfo.NOT_SHARED);
                ei.addDataId("depid");
                cache.setValue(ei, "value" + i, !Cache.COORDINATE);
            }

            cacheIds = cache.getIdsByRangeDisk(0, -1);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                out.println("<br>-- CacheIds=" + cacheIds);
                if (cacheIds.size() != 10) {
                    msg = "runHtod1.52 CacheIds count expected=10 received=" + cacheIds.size();
                    return;
                }
                for (int i = 20; i < 30; i++) {
                    if (!cacheIds.contains("key" + i)) {
                        msg = "runHtod1.53 Do not find cacheId key" + i;
                        return;
                    }
                }
            } else {
                msg = "runHtod1.54 No cacheIds error";
                return;
            }

            out.println("<br>-- Clear disk");
            cache.clear();
            Thread.sleep(20000); ///give the machine some breathing time

            count = cache.getIdsSizeDisk();
            if (count != 0) {
                msg = "runHtod1.55 CacheIds count expected=0 received=" + count;
                return;
            }
            cacheIds = cache.getIdsByRangeDisk(0, -1);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                msg = "runHtod1.56 CacheIds expected=empty but received=" + cacheIds;
                return;
            }
            count = cache.getDepIdsSizeDisk();
            if (count != 0) {
                msg = "runHtod1.57 Depids count expected=0 received=" + count;
                return;
            }
            depIds = cache.getDepIdsByRangeDisk(0, -1);
            if (depIds != null && !depIds.isEmpty()) {
                msg = "runHtod1.58 DepIds expected=empty but received=" + depIds;
                return;
            }
            count = cache.getTemplatesSizeDisk();
            if (count != 0) {
                msg = "runHtod1.59 Templates count expected=0 received=" + count;
                return;
            }
            templates = cache.getTemplatesByRangeDisk(0, -1);
            if (templates != null && !templates.isEmpty()) {
                msg = "runHtod1.60 Templates expected=empty but received=" + templates;
                return;
            }

            out.println("<br><br>HTOD1 Test completed successfully");

        } catch (Exception e) {
            msg = getStackTrace(e);
            out.println("<br> *** Exception: <br>" + msg);
        }
        return;
    }

    public void runHtod2() {
        msg = "";
        out.println("<br>Running HTOD2 test...<br>");

        try {
            cacheSize = 2000;
            cache = ServerCache.getCache("HTODTest2");
            if (cache == null) {
                CacheConfig cc = ServerCache.getCacheService().getCacheConfig();
                cc.setMaxCacheSize(cacheSize);
                cc.setEnableDiskOffload(true);
                cc.setDiskCacheEvictionPolicy(CacheConfig.EVICTION_NONE);
                cache = ServerCache.createCache("HTODTest2", cc);
            }
            if (cache == null) {
                msg = "runHtod2.1 Can't get an instance of cache";
                return;
            }
            cacheSize = cache.getMaxNumberCacheEntries();
            out.println("<br>-- Cache size=" + cacheSize);

            if (cacheSize != 2000) {
                msg = "runHtod2.2 Cache size expected=2000 but received=" + cacheSize;
                return;
            }
            if (!(cache.getSwapToDisk())) {
                msg = "runHtod2.3 Disk offLoad is not enabled";
                return;
            }
            cache.clear();
            Thread.sleep(20000); ///give the machine some breathing time

            out.println("<br>-- Loop for put() - " + (cacheSize + 5));
            long start = 0, current = 0;
            start = System.currentTimeMillis();
            for (int i = 0, j = 0; i < cacheSize + 5; i++, j++) {
                if (j == 10) {
                    j = 0;
                }
                EntryInfo ei = new EntryInfo();
                ei.setId("key" + i);
                ei.addTemplate("htod2Template");
                if (i == 0 && i == 1) {
                    ei.setTimeLimit(5);
                } else {
                    ei.setTimeLimit(0);
                }
                ei.setSharingPolicy(EntryInfo.NOT_SHARED);
                ei.addDataId("depid" + j);
                cache.setValue(ei, "value" + i, !Cache.COORDINATE);
            }

            int count = cache.getIdsSizeDisk();
            if (count != 5) {
                msg = "runHtod2.4 CacheIds count expected=5 received=" + count;
                return;
            }

            cacheIds = cache.getIdsByRangeDisk(0, -1);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                out.println("<br>-- CacheIds=" + cacheIds);
                if (cacheIds.size() != 5) {
                    msg = "runHtod2.5 CacheIds count expected=5 received=" + cacheIds.size();
                    return;
                }
                for (int i = 0; i < 5; i++) {
                    if (!cacheIds.contains("key" + i)) {
                        msg = "runHtod2.6 Do not find cacheId key" + i;
                        return;
                    }
                }
            } else {
                msg = "runHtod2.7 No cacheIds error";
                return;
            }
            current = System.currentTimeMillis();
            out.println("<br>-- remaining time= " + (current - start));
            do {
                Thread.sleep(100);
                current = System.currentTimeMillis();
            } while (current < (start + 5100));

            for (int i = 0; i < 5; i++) {
                EntryInfo ei = new EntryInfo();
                ei.setId("key" + i);
                v = (String) cache.getValue(ei, false);
                if (i == 0 && i == 1) {
                    if (v != null) {
                        msg = "runHtod2.8 For key" + i + " value expected=null but received=" + v;
                    }
                } else {
                    if (v == null) {
                        msg = "runHtod2.9 For key" + i + " value received=null";
                    } else if (!(v.equals("value" + i))) {
                        msg = "runHtod2.10 For key" + i + " value expected=value" + i + " received=" + v;
                    }
                }
            }
            cache.clear();
            Thread.sleep(20000); ///give the machine some breathing time

            out.println("<br><br>HTOD2 Test completed successfully");

        } catch (Exception e) {
            msg = getStackTrace(e);
            out.println("<br> *** Exception: <br>" + msg);
        }
        return;
    }

    public void runHtod3() {
        msg = "";
        out.println("<br>Running HTOD3 test...<br>");

        try {
            cacheSize = 220;
            cache = ServerCache.getCache("HTODTest3");
            if (cache == null) {
                CacheConfig cc = ServerCache.getCacheService().getCacheConfig();
                cc.setMaxCacheSize(cacheSize);
                cc.setEnableDiskOffload(true);
                cc.setDiskCacheEvictionPolicy(CacheConfig.EVICTION_NONE);
                cache = ServerCache.createCache("HTODTest3", cc);
            }
            if (cache == null) {
                msg = "runhtod3.1 Can't get an instance of cache";
                return;
            }
            cachesize = cache.getMaxNumberCacheEntries();
            out.println("<br> Cache size: " + cachesize);

            if (!(cache.getSwapToDisk())) {
                msg = "runHtod3.2 Disk offLoad is not enabled";
                return;
            }
            cache.clear();
            Thread.sleep(20000); ///give the machine some breathing time
            EntryInfo ei = new EntryInfo();
            String template = "";
            String depid = "";
            String temp = "";
            Collection c = null;
            int m = 0;
            long start, end;
            start = System.currentTimeMillis();
            for (int j = 0; j < cachesize * 10; j++) {
                String name = "test2";
                if (j % 2 == 0) {
                    name = "test1";
                }
                String id = name + ":data:" + j;
                long[] value = new long[valueSize];
                value[0] = j;
                if (j % 20 == 0) {
                    temp = ":dep-id:" + m;
                    m++;
                }
                depid = name + temp;
                template = name + ":template";
                ei.reset();
                ei.setId(id);
                ei.setTimeLimit(0);
                ei.setSharingPolicy(EntryInfo.NOT_SHARED);
                ei.addDataId(depid);
                ei.addTemplate(template);
                cache.invalidateAndSet(ei, value, true);
            }
            out.println("<br> Time to put the cache: " + (System.currentTimeMillis() - start) + " ms");
            int size = cache.getNumberCacheEntries();
            if (size != cachesize) {
                msg = "runHtod3.3  cache entries in memory not match: expected=" + cachesize + " received=" + size;
                return;
            }
            size = cache.getIdsSizeDisk();
            if (size != (cachesize * 9)) {
                msg = "runHtod3.4  cache entries in disk not match: expected=" + (cachesize * 9) + " received=" + size;
                return;
            }
            c = cache.getAllDependencyIds();
            size = c.size() + cache.getDepIdsSizeDisk();
            if (size != (cachesize * 10 / 20 * 2)) {
                msg = "runHtod3.5 dep ids in memory and disk not match: expected=" + (cachesize * 10 / 20 * 2) + " received=" + size;
                return;
            }
            c.clear();
            Thread.sleep(20000); ///give the machine some breathing time
            size = cache.getTemplatesSizeDisk();
            if (size != 2) {
                msg = "runHtod3.6 template size in disk not match: expected=2 received=" + size;
                return;
            }
            start = System.currentTimeMillis();
            // invalidateByTemplate
            cache.invalidateByTemplate("test1:template", true);
            end = System.currentTimeMillis();
            size = cache.getTemplatesSizeDisk();
            if (size != 1) {
                msg = "runHtod3.7 template size in disk not match: expected=1 received=" + size;
                return;
            }

            //ADD DELAY here to account for the PMI counters to increase
            Thread.sleep(20000);

            size = cache.getIdsSizeDisk();
            if (size != (cachesize * 9 / 2)) {
                msg = "runHtod3.8 cache entries in disk not match: expected=" + (cachesize * 9 / 2) + " received=" + size;
                return;
            }

            ce = cache.getEntryDisk("test1:data:0");
            if (ce != null) {
                msg = "runHtod3.9 cache test1:data:0 should not exist";
                return;
            }

            ce = cache.getEntryDisk("test1:data:" + (cachesize * 10 - 1));
            if (ce != null) {
                msg = "runHtod3.10 cache test1:data:" + (cachesize * 10 - 1) + " should not exist";
                return;
            }

            c = cache.getIdsByRangeDisk(0, -1);

            size = c.size();
            if (size != (cachesize * 9 / 2)) {
                msg = "runHtod3.11 cache ids count expected=" + (cachesize * 9 / 2) + " received=" + size;
                return;
            } else {
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    String id = (String) it.next();
                    if (id.startsWith("test1")) {
                        msg = "runHtod3.12 cache data not expected=" + c.toString();
                        return;
                    }
                }
            }

            for (int i = 0; i < (cachesize * 10 / 20); i++) {
                c = cache.getCacheIdsByDependencyDisk("test1:dep-id:" + i);
                if (c.size() != 0) {
                    msg = "runHtod3.13 dep id: test1:dep-id:" + i + " should be empty but received=" + c.toString();
                    return;
                }
            }

            c = cache.getCacheIdsByTemplateDisk("test1:template");
            if (c.size() != 0) {
                msg = "runHtod3.14 template: test1:template should be empty but received=" + c.toString();
                return;
            }
            out.println("<br> Time to invalidate the template: " + (end - start) + " ms");

            start = System.currentTimeMillis();
            // invalidateById
            cache.invalidateById("test2:dep-id:0", true);
            cache.invalidateById("test2:dep-id:1", true);
            end = System.currentTimeMillis();
            Thread.sleep(15000);

            size = cache.getIdsSizeDisk();
            if (size != (cachesize * 9 / 2 - 20)) {
                msg = "runHtod3.15 cache entries in disk not match: expected=" + (cachesize * 9 / 2 - 20) + " received=" + size;
                return;
            }

            for (int i = 1; i < 40; i++) {
                ce = cache.getEntryDisk("test2:data:" + i);
                if (ce != null) {
                    msg = "runHtod3.16 cache test2:data:" + i + " should not exist";
                    return;
                }
                i++;
            }

            for (int i = 0; i < 2; i++) {
                c = cache.getCacheIdsByDependencyDisk("test2:dep-id:" + i);
                if (c.size() != 0) {
                    msg = "runHtod3.17 dep id: test2:dep-id:" + i + " should be empty but received=" + c.toString();
                    return;
                }
            }

            Thread.sleep(10000);
            c = cache.getIdsByRangeDisk(0, -1);
            if (c.size() != (cachesize * 9 / 2 - 20)) {
                msg = "runHtod3.18 cache ids count expected=" + (cachesize * 9 / 2 - 20) + " received=" + c.size();
                return;
            } else {
                for (int i = 1; i < 40; i++) {
                    if (c.contains("test2:data:" + i)) {
                        msg = "runHtod3.19 cache data not expected=" + c.toString();
                        return;
                    }
                    i++;
                }
            }

            c = cache.getCacheIdsByTemplateDisk("test2:template");
            if (c.size() != (cachesize * 9 / 2 - 20)) {
                msg = "runHtod3.20 template: test2:template count expected=" + (cachesize * 9 / 2 - 20) + " received=" + size;
                return;
            } else {
                for (int i = 1; i < 40; i++) {
                    if (c.contains("test2:data:" + i)) {
                        msg = "runHtod3.21 cache data not expected=" + c.toString();
                        return;
                    }
                    i++;
                }
            }

            out.println("<br> Time to invalidate the dep ids: " + (end - start) + " ms");

        } catch (Exception e) {
            out.println("<br> *** Exception: <br>" + getStackTrace(e));
        }
    }

    public void runHtod3a() {
        msg = "";
        out.println("<br>Continuing HTOD3 test...<br>");

        try {

            cache = ServerCache.getCache("HTODTest3");

            if (cache == null) {
                msg = "runhtod3.1 Can't get an instance of cache";
                return;
            }

            int size = cache.getDepIdsSizeDisk();
            if (size != (cachesize * 9 / 20 - 2)) {
                msg = "runHtod3.22 dep ids size: expected=" + (cachesize * 9 / 20 - 2) + " received=" + size;
                return;
            }

        } catch (Exception e) {
            out.println("<br> *** Exception: <br>" + getStackTrace(e));
        }
    }

    public void runHtod4() {
        msg = "";
        out.println("<br>Running HTOD4 test...<br>");

        try {
            if (this.dMap == null) {
                Properties props = new Properties();
                props.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "100");
                props.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_TRUE);
                props.put(DistributedObjectCacheFactory.KEY_DISKCACHE_EVICTION_POLICY, String.valueOf(CacheConfig.EVICTION_NONE));
                this.dMap = DistributedObjectCacheFactory.getMap("HTODTest4", props);
            }
            if (this.dMap == null) {
                msg = "runHtod4.1 dmap is null";
                return;
            }
            if (!(this.dMap.getMapType() == DistributedObjectCache.TYPE_DISTRIBUTED_MAP)) {
                msg = "runHtod4.2 it is NOT distributed map type - got " + this.dMap.getMapType();
                return;
            }
            this.dMap.clear();
            Thread.sleep(20000); ///give the machine some breathing time

            if (!dMap.isEmpty(true)) {
                msg = "runHtod4.3 dmap is not empty after clear";
                return;
            }
            cacheSize = 100;

            out.println("<br>-- Loop for put() - " + (cacheSize + 101));
            for (int i = 0, j = 0; i < cacheSize + 101; i++) {
                this.dMap.put("key" + i, "value" + i);
            }

            if (dMap.isEmpty(true)) {
                msg = "runHtod4.4 dmap is empty after put";
                return;
            }
            int count = dMap.size(true);
            if (count != (cacheSize + 101)) {
                msg = "runHtod4.5 dmap size expected=201 received=" + count;
                return;
            }

            count = dMap.size(false);
            if (count != cacheSize) {
                msg = "runHtod4.6 CacheIds count expected=100 received=" + count;
                return;
            }

            if (!dMap.containsKey("key0", true)) {
                msg = "runHtod4.7 Dmap does not contain key0 in memory or disk map";
                return;
            }

            if (dMap.containsKey("key0", false)) {
                msg = "runHtod4.8 Dmap contains key0 in memeory dmap";
                return;
            }

            if (!dMap.containsKey("key101", true)) {
                msg = "runHtod4.9 Dmap does not contain key101 in memory or disk map";
                return;
            }

            if (!dMap.containsKey("key101", false)) {
                msg = "runHtod4.10 Dmap does not contain key101 in memeory dmap";
                return;
            }

            cacheIds = dMap.keySet(true);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                if (cacheIds.size() != (cacheSize + 101)) {
                    msg = "runHtod4.11 CacheIds count expected=201 received=" + cacheIds.size();
                    return;
                }
                for (int i = 0; i < (cacheSize + 101); i++) {
                    if (!cacheIds.contains("key" + i)) {
                        msg = "runHtod4.12 Do not find cacheId key" + i;
                        return;
                    }
                }
            } else {
                msg = "runHtod4.13 No cacheIds error";
                return;
            }

            cacheIds = dMap.keySet(false);
            if (cacheIds != null && !cacheIds.isEmpty()) {
                if (cacheIds.size() != cacheSize) {
                    msg = "runHtod4.14 CacheIds count expected=201 received=" + cacheIds.size();
                    return;
                }
                for (int i = 101; i < cacheSize + 101; i++) {
                    if (!cacheIds.contains("key" + i)) {
                        msg = "runHtod4.15 Do not find cacheId key" + i;
                        return;
                    }
                }
            } else {
                msg = "runHtod4.16 No cacheIds error";
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
