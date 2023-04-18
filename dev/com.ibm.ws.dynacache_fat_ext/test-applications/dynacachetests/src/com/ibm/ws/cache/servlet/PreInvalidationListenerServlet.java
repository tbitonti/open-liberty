package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.CacheEntry;
import com.ibm.websphere.cache.DistributedNioMap;
import com.ibm.websphere.cache.EntryInfo;
import com.ibm.websphere.cache.PreInvalidationListener;
import com.ibm.wsspi.cache.DistributedObjectCacheFactory;

/**
 * Servlet Test class for the Servlet: PreInvalidationListenerTest
 */
public class PreInvalidationListenerServlet extends
                javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    private static final long serialVersionUID = 1L;

    public PreInvalidationListenerServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        if (request.getParameter("testType").equals("disk"))
            response.getWriter().write(runTestDisk());
        else if (request.getParameter("testType").equals("memory"))
            response.getWriter().write(runTestMemory());
        else if (request.getParameter("testType").equals("diskTimeout"))
            response.getWriter().write(runDiskTimeout());
        else if (request.getParameter("testType").equals("diskTimeoutSize")) {
            response.getWriter().write(getSizeForTimeout());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private String runTestMemory() {
        DistributedNioMap map;
        Properties p = new Properties();
        p.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);

        map = DistributedObjectCacheFactory.getMap("Proxy1", p);
        if (map == null || !(map instanceof DistributedNioMap)) {
            return "runTestMemory1.1: Can't get an instance of Proxy1 NIOMap";
        }
        map.clear();
        if (map.enableListener(true) == false) {
            String msg = "runTestMemory1.2: Cannot use the non-async event source";
            return msg;
        }

        map.put("key1", "red", null, 1, -1, EntryInfo.NOT_SHARED, null, null);
        map.put("key2", "blue", null, 1, -1, EntryInfo.NOT_SHARED, null, null);
        map.put("key3", "yellow", null, 1, -1, EntryInfo.NOT_SHARED, null, null);
        map.put("key4", "green", null, 1, -1, EntryInfo.NOT_SHARED, null, null);

        map.invalidate("key2");//invalidate before listener--should succeed.

        PreInvalidationListener listener = new PreInvalidationListenerImpl();
        map.addPreInvalidationListener(listener);

        map.invalidate("key1"); //should fail
        map.invalidate("key3"); //should fail
        map.invalidate("key4", true, false); //should bypass listener and succeed.

        // Now try to get key1, key2, and key3. All should be available except key2 & key4
        Object obj1 = map.getCacheEntry("key1");
        Object obj2 = map.getCacheEntry("key2");
        Object obj3 = map.getCacheEntry("key3");
        Object obj4 = map.getCacheEntry("key4");

        if (obj2 != null || obj4 != null)
            return "did not invalidate key2 or key4--failing";

        if (obj1 == null || obj3 == null) {
            return "invalidated obj1 or obj3--failing";
        }

        map.put("key2", "orange", null, 1, -1, EntryInfo.NOT_SHARED, null, null);

        map.removePreInvalidationListener(listener);

        obj2 = map.getCacheEntry("key2");
        String msg = "key1 is " + ((CacheEntry) obj1).getValue() + "   key2 is " + ((CacheEntry) obj2).getValue() + "  key3 is "
                     + ((CacheEntry) obj3).getValue();

        map.invalidate("key1");
        map.invalidate("key2");
        map.invalidate("key3");

        // Now keys should be null
        obj1 = map.getCacheEntry("key1");
        obj2 = map.getCacheEntry("key2");
        obj3 = map.getCacheEntry("key3");

        if (obj1 != null || obj2 != null || obj3 != null) {
            return "invalidates did not work after listnener removed--failed.";
        }
        else
            msg += ". Results were null after listener was turned off then they were invalidated.";

        return "Test successful: " + msg;
    }

    public String runTestDisk() {
        DistributedNioMap map;
        Properties p = new Properties();
        p.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
        p.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "1");
        p.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_TRUE);

        map = DistributedObjectCacheFactory.getMap("proxyDisk", p);
        if (map == null || !(map instanceof DistributedNioMap)) {
            return "runTestDisk2.1: Can't get an instance of Proxy1 NIOMap";
        }
        map.clear();
        if (map.enableListener(true) == false) {
            String msg = "runTestDisk2.2: Cannot use the non-async event source";
            return msg;
        }

        // Plug in listener. Listener will return false unless we send a CLEAR
        PreInvalidationListener listener = new PreInvalidationListenerImpl();
        map.addPreInvalidationListener(listener);

        // Put 10,000 entries on disk
        for (int i = 0; i < 10001; i++) {
            map.put("key" + i, "value" + i, null, 1, -1, EntryInfo.NOT_SHARED, null, null);
        }

        int sizeMem = map.size(false);
        int sizeDisk = map.size(true) - sizeMem;

        String msg = "Size in memory after first put: " + sizeMem + ", size on disk after first put: " + sizeDisk + "\n";

        // Now, try to invalidate half of the entries
        for (int i = 0; i < 5000; i++) {
            map.invalidate("key" + i);
        }

        // Size should remain the same, check
        if (map.size(true) != sizeMem + sizeDisk)
            return (msg + "Invalidates succeeded without bypassing listener--test case failed");
        else
            msg += "Could not invalidate entries without bypassing listener, test passed.\n";

        // Now, try to invalidate 2,000 entries by bypassing listener
        for (int i = 0; i < 2000; i++) {
            map.invalidate("key" + i, false, false);
        }

        // Give disk cache cleanup time to run before checking size
        allowTimeForDiskCacheCleanup();

        if (map.size(true) != sizeMem + sizeDisk - 2000)
            return (msg + "Invalidates which bypassed listener did not succeed--test case failed. Size of map was " + map.size(true));
        else
            msg += "Invalidates that bypassed listener succeeded, test passed\n";

        // Now, try a clear. This should succeed because listener should let it pass through
        map.clear();

        map.removePreInvalidationListener(listener);

        if (map.size(true) == 0)
            return (msg + "Clear attempt unsuccessful--test case failed");
        else
            msg += "Clear attempt successful, test passed. Test successful \n";

        return msg;
    }

    private void allowTimeForDiskCacheCleanup() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String runDiskTimeout() {
        DistributedNioMap map;
        Properties p = new Properties();
        p.put(DistributedObjectCacheFactory.KEY_ENABLE_NIO_SUPPORT, DistributedObjectCacheFactory.VALUE_TRUE);
        p.put(DistributedObjectCacheFactory.KEY_CACHE_SIZE, "1");
        p.put(DistributedObjectCacheFactory.KEY_ENABLE_DISK_OFFLOAD, DistributedObjectCacheFactory.VALUE_TRUE);

        map = DistributedObjectCacheFactory.getMap("proxyDiskTimeout", p);
        if (map == null || !(map instanceof DistributedNioMap)) {
            return "runTestDisk2.1: Can't get an instance of Proxy1 NIOMap";
        }
        map.clear();
        if (map.enableListener(true) == false) {
            String msg = "runTestDisk2.2: Cannot use the non-async event source";
            return msg;
        }

        // Plug in listener. Listener will return false unless we send a CLEAR
        PreInvalidationListener listener = new PreInvalidationListenerImpl();
        map.addPreInvalidationListener(listener);

        // Put 10,000 entries on disk
        for (int i = 0; i < 10001; i++) {
            map.put("key" + i, "value" + i, null, 1, 6000, EntryInfo.NOT_SHARED, null, null);
        }

        return "Attached Listener and populated cache. Test successful.";
    }

    private String getSizeForTimeout() {

        allowTimeForDiskCacheCleanup();

        DistributedNioMap map = DistributedObjectCacheFactory.getMap("proxyDiskTimeout");
        int cacheSize = map.size(true);
        String msg = Integer.toString(cacheSize);
        if (cacheSize >= 10000) {
            return "Test successful: " + msg;
        } else {
            return "Test Failed Expected: 10000 Actual: " + msg;
        }
    }

}