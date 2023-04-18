// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.htod;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Map;
import java.util.TreeMap;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.DCacheBase;
import com.ibm.ws.cache.TestConfig;
import com.ibm.ws.cache.pmi.DynaCachePMIClient;
import com.ibm.ws.cache.pmi.PMIHelper;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.ibm.ws.cache.stat.internal.WSDynamicCacheStats;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class HTODTest1 extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("HTODTest1");

    private final boolean debug = true;
    public static final boolean DISTRIB = true;
    public static final int WAIT_TIME = 2000;

    private String pmiHost = "localhost";
    private String pmiPort = "8880";
    private final String connector = TestConfig.getConnector();

    boolean remoteEnabled = false;

    String nodeName = null;
    String nodeName2 = null;
    String serverName = null;
    String serverName2 = null;

    String pmiTestURI = "/dynacachetests/pmitest";
    String timeStampURI = "/dynacachetests/TimeStamp";
    String htodTestURI = "/dynacachetests/htodtest";

    WebResponse resp = null;
    WebConversation wc = null;

    boolean diskOffload = false;

    TreeMap e_cm_hmap = new TreeMap();
    TreeMap e_cm_servlet_hmap = new TreeMap();
    TreeMap e_cm_object_hmap = new TreeMap();
    TreeMap e_cm_servlet_bc_hmap = new TreeMap();
    TreeMap e_cm_servlet_bc_template_hmap = new TreeMap();
    TreeMap e_cm_servlet_bc_template_t1_hmap = new TreeMap();
    TreeMap e_cm_servlet_bc_template_t2_hmap = new TreeMap();
    TreeMap e_cm_object_dmap1_hmap = new TreeMap();
    TreeMap e_cm_object_dmap2_hmap = new TreeMap();
    TreeMap r_hmap = new TreeMap();

    String msg = "";
    int count = 0;
    int cacheSize = 0;

    //fill in all applicable perfdescriptors and names above
    //must determine whether we're in a cooperating JVM setup, and hence whether to do remote tests.
    //could be on separate nodes or same node, separate servers
    public void setUp() throws Exception {

        PMIHelper.initializeCacheNames();

        pmiHost = TestConfig.getHost();
        pmiPort = Integer.toString(TestConfig.getPmiPort());
        nodeName = TestConfig.getNode();
        serverName = TestConfig.getServer();

        wc = startNewConversation();
        enablePMICounters(!DISTRIB, nodeName, serverName);

        PMIHelper.initializeExpectedCounters(e_cm_servlet_bc_hmap, 2, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(e_cm_servlet_bc_template_hmap, 15, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(e_cm_servlet_bc_template_t1_hmap, 15, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(e_cm_servlet_bc_template_t2_hmap, 15, PMIHelper.CACHE_SERVLET);

        PMIHelper.initializeExpectedCounters(e_cm_object_hmap, 2, PMIHelper.CACHE_OBJECT);
        PMIHelper.initializeExpectedCounters(e_cm_object_dmap1_hmap, 15, PMIHelper.CACHE_OBJECT);
        PMIHelper.initializeExpectedCounters(e_cm_object_dmap2_hmap, 15, PMIHelper.CACHE_OBJECT);
    }

    @Test
    public void testLocal() throws Exception {
        if (!SHARED_SERVER.isServerUp()) {
            throw new IllegalStateException("Junit setup for test HTODTest1 NOT correct");
        }
        DynaCachePMIClient dpmic = new DynaCachePMIClient(TestConfig.host, TestConfig.soapPort, TestConfig.serverRoot);
        dpmic.enableDynaCacheStats();
        setUp();
        clearCache();
        localDefaultServletPmi();
        persistToDisk();
        htodCache();
        flushToDisk();
    }

    public void localDefaultServletPmi() throws Exception {

        if (debug) {
            System.out.println("LocalDefaultServletPmi... 1 ");
        }

        long[] timeStamps = { 0, 0 };
        StringBuffer tmp = new StringBuffer(timeStampURI);
        for (int i = 0; i < 2; i++) {
            tmp.append("?arg1=test").append(i);
            resp = getWebResponse(wc, tmp.toString());
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            tmp.setLength(timeStampURI.length());
            WebTable include1 = resp.getTableWithID("TimeStamp");
            assertNotNull(msg("missing include1 table", resp), include1);
            TableCell cell = include1.getTableCell(0, 0);
            timeStamps[i] = Long.parseLong(cell.asText());
        }

        PMIHelper.sleep(6000); // delay 2 sec

        if (debug) {
            System.out.println("LocalDefaultServletPmi... 2 ");
        }

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET, r_hmap);
        assertEquals("PMITest.localDefaultServlet.1 - count not equal", count, 2);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_hmap, WSDynamicCacheStats.MaxInMemoryCacheEntryCount, DCacheBase.DEFAULT_CACHE_SIZE, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_hmap, WSDynamicCacheStats.InMemoryCacheEntryCount, 2, PMIHelper.TYPE_ADD);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET, e_cm_servlet_bc_hmap, r_hmap);

        cacheSize = DCacheBase.DEFAULT_CACHE_SIZE;

        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, 2, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.MissCount, 2, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.ClientRequestCount, 2, PMIHelper.TYPE_ADD);

        if (debug) {
            System.out.println("LocalDefaultServletPmi... 3 ");
        }

        for (int i = 2; i < cacheSize + 2; i++) {
            tmp.append("?arg1=test").append(i);
            resp = getWebResponse(wc, tmp.toString());
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            tmp.setLength(timeStampURI.length());
        }
        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("HTODTest.LocalDefaultServletPmi.3 - count not equal", count, 15);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, cacheSize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.MissCount, cacheSize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.ClientRequestCount, cacheSize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.LruInvalidationCount, 2, PMIHelper.TYPE_ADD);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", e_cm_servlet_bc_template_t1_hmap, r_hmap);

        if (debug) {
            System.out.println("LocalDefaultServletPmi... 3 ");
        }
        resp = getWebResponse(wc, timeStampURI + "?inv=test0");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        PMIHelper.sleep(24000);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("HTODTest.LocalDefaultServletPmi.4 - count not equal", count, 15);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, 1, PMIHelper.TYPE_SUBTRACT);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.ExplicitDiskInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.LocalExplicitInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.ExplicitInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", e_cm_servlet_bc_template_t1_hmap, r_hmap);

        if (debug) {
            System.out.println("LocalDefaultServletPmi... 4 ");
        }
        resp = getWebResponse(wc, timeStampURI + "?arg1=test0");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        WebTable include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing include1 table", resp), include1);
        TableCell cell = include1.getTableCell(0, 0);
        if (Long.parseLong(cell.asText()) == timeStamps[0])
            fail("HTODTest.LocalDefaultServletPmi.5 - TimeStamps should not match");
        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("HTODTest.LocalDefaultServletPmi.5 - count not equal", count, 15);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.MissCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.ClientRequestCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.LruInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", e_cm_servlet_bc_template_t1_hmap, r_hmap);

        if (debug) {
            System.out.println("LocalDefaultServletPmi... 5 ");
        }
        resp = getWebResponse(wc, timeStampURI + "?arg1=test1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing include1 table", resp), include1);
        cell = include1.getTableCell(0, 0);
        assertEquals(msg("TimeStamps did not match", resp), timeStamps[1], Long.parseLong(cell.asText()));
        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("HTODTest.LocalDefaultServletPmi.6 - count not equal", count, 15);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.HitsOnDiskCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.ClientRequestCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(e_cm_servlet_bc_template_t1_hmap, WSDynamicCacheStats.LruInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", e_cm_servlet_bc_template_t1_hmap, r_hmap);
    }

    public void persistToDisk() throws Exception {
        clearCache();

        if (debug) {
            System.out.println("persistToDisk... 1 ");
        }
        String URI = "/dynacachetests/TimeStamp1";
        wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI + "?arg1=val0");
        String ts1 = resp1.getTableWithID("TimeStamp1").getCellAsText(0, 0);
        // fill the cache
        for (int i = 1; i < cacheSize + 2; i++) {
            resp1 = getWebResponse(wc, URI + "?arg1=val" + i);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        }

        //send second  request. It should find diffrent time stamp
        WebResponse resp2 = getWebResponse(wc, URI + "?arg1=val0");
        String ts2 = resp2.getTableWithID("TimeStamp1").getCellAsText(0, 0);
        assertTrue(msg("Persist-to-disk test, timestamps were equal", resp2), !ts1.equals(ts2));
    }

    public void htodCache() throws Exception {
        if (debug) {
            System.out.println("htodCache... 1 ");
        }
        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        resp = getWebResponse(wc, htodTestURI + "?method=htod1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String s = resp.getText();
        //System.out.println(s);
        int sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testHtodCache.1 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }

        if (debug) {
            System.out.println("htodCache... 2 ");
        }
        resp = getWebResponse(wc, htodTestURI + "?method=htod2");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        s = resp.getText();
        //System.out.println(s);
        sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testHtodCache.2 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }

        if (debug) {
            System.out.println("htodCache... 3 ");
        }
        resp = getWebResponse(wc, htodTestURI + "?method=htod3");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        s = resp.getText();
        //System.out.println(s);
        sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testHtodCache.3 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }
        if (debug) {
            System.out.println("htodCache... 3a ");
        }
        Thread.sleep(20000);
        resp = getWebResponse(wc, htodTestURI + "?method=htod3a");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        s = resp.getText();
        //System.out.println(s);
        sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            Thread.sleep(20000);
            resp = getWebResponse(wc, htodTestURI + "?method=htod3a");
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            sindex = s.indexOf("Test failure:");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                fail("testHtodCache.3a - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
            }
        }
        if (debug) {
            System.out.println("htodCache... 4 ");
        }
        resp = getWebResponse(wc, htodTestURI + "?method=htod4");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        s = resp.getText();
        //System.out.println(s);
        sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testHtodCache.4 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }
    }

    public void enablePMICounters(boolean distrib, String node, String server) throws Exception {

        String url = "";
        if (!distrib) {
            url = pmiTestURI + "?method=enable&host=" + pmiHost + "&port=" + pmiPort + "&connector=" + connector + "&node=" + node + "&server=" + server + "&pmitree="
                  + WSDynamicCacheStats.NAME;
            //System.out.println("*** URL=" + url);
            resp = getWebResponse(wc, pmiTestURI + "?method=enable&host=" + pmiHost + "&port=" + pmiPort + "&connector=" + connector + "&node=" + node + "&server=" + server
                                      + "&pmitree=" + WSDynamicCacheStats.NAME);
        } else {
            url = pmiTestURI + "?method=enable&host=" + pmiHost + "&port=" + pmiPort + "&connector=" + connector + "&node=" + node + "&server=" + server + "&pmitree="
                  + WSDynamicCacheStats.NAME;
            //System.out.println("*** Distrib URL=" + url);
            resp = getCloneWebResponse(wc, pmiTestURI + "?method=enable&host=" + pmiHost + "&port=" + pmiPort + "&connector=" + connector + "&node=" + node + "&server=" + server
                                           + "&pmitree=" + WSDynamicCacheStats.NAME);
        }
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String s = resp.getText();
        //System.out.println(s);
        int sindex = s.indexOf("Error:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("enablePMI.1 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }
    }

    public int readCountersFromPMI(boolean distrib, String cacheModules, Map hmap) throws Exception {

        int count = 0;
        if (!distrib) {
            resp = getWebResponse(wc, pmiTestURI + "?method=read&pmitree=" + cacheModules);
        } else {
            resp = getCloneWebResponse(wc, pmiTestURI + "?method=read&pmitree=" + cacheModules);
        }
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String s = resp.getText();
        //System.out.println(s);
        int sindex = s.indexOf("Error:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("PMITest.readCountersFromPMI.1 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        } else {
            sindex = s.indexOf("ReturnOutput: ");
            if (sindex <= 0) {
                fail("PMITest.readCountersFromPMI.2 - " + resp.getURL() + "\n" + s);
            } else {
                if (s.indexOf("ReturnOutput: None") > 0) {
                    return 0;
                }
                int eindex = s.indexOf("</body>", sindex);
                s = s.substring(sindex + "ReturnOutput: ".length(), eindex);
                count = PMIHelper.parseCounters(s, hmap);
            }
        }
        return count;
    }

    public void flushToDisk() throws Exception {
        clearCache();

        if (debug) {
            System.out.println("Flush to Disk...");
        }

        String URI = "/dynacachetests/TimeStamp";
        wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI + "?arg1=1");
        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);

        //restart the server and make sure we still get the same timestamp on the next hit
        //DynaCachePMIClient dpmic = new DynaCachePMIClient(TestConfig.host, TestConfig.soapPort, TestConfig.serverRoot);
        SHARED_SERVER.restartServer();

        //send second  request. It should find diffrent time stamp
        WebResponse resp2 = getWebResponse(wc, URI + "?arg1=1");
        String ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);
        assertTrue(msg("Flush to disk test. Timestamps were not equal", resp2), ts1.equals(ts2));
    }

}
