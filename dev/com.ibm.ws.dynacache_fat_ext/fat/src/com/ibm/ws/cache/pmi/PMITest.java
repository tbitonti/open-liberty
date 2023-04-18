// IBM Confidential OCO Source Material
// 5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business Machines Corp. 1997, 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.pmi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.TestConfig;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.ibm.ws.cache.stat.internal.WSDynamicCacheStats;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class PMITest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("PMITest");

    private final boolean debug = true;
    public static final boolean DISTRIB = true;
    public static final int WAIT_TIME = 2000;

    protected String pmiHost = "localhost";
    protected String pmiPort = "8880";
    protected String connector = TestConfig.getConnector();

    boolean remoteEnabled = false;

    String nodeName = null;
    String nodeName2 = null;
    String serverName = null;
    String serverName2 = null;

    String pmiTestURI = "/dynacachetests/pmitest";
    String timeStampURI = "/dynacachetests/TimeStamp";
    String timeStamp2URI = "/dynacachetests/TimeStamp2";
    String reqParm_sharePush_URI = "/dynacachetests/reqparmtest_sharePush";
    String reqParm_sharePull_URI = "/dynacachetests/reqparmtest";
    String dmapURI = "/dynacachetests/dmaptest";

    String dmap1 = "services/cache/distributedmap_1";
    String dmap2 = "services/cache/distributedmap_2";

    WebResponse resp = null;
    WebConversation wc = null;

    boolean zOSMode = false;
    int invDelay = 1;
    int remoteDelay = 4;

    TreeMap d1_e_cm_hmap = new TreeMap();
    TreeMap d1_e_cm_object_hmap1 = new TreeMap();
    TreeMap d1_e_cm_object_hmap2 = new TreeMap();
    TreeMap d1_e_cm_servlet_bc_hmap = new TreeMap();
    TreeMap d1_e_cm_servlet_bc_template_hmap = new TreeMap();
    TreeMap d1_e_cm_servlet_bc_template_t1_hmap = new TreeMap();
    TreeMap d1_e_cm_servlet_bc_template_t2_hmap = new TreeMap();
    TreeMap d1_e_cm_servlet_bc_template_t3_hmap = new TreeMap();
    TreeMap d1_e_cm_servlet_bc_template_t4_hmap = new TreeMap();
    TreeMap d1_e_cm_object_dmap1_hmap = new TreeMap();
    TreeMap d1_e_cm_object_dmap2_hmap = new TreeMap();

    TreeMap d2_e_cm_hmap = new TreeMap();
    TreeMap d2_e_cm_object_hmap = new TreeMap();
    TreeMap d2_e_cm_servlet_bc_hmap = new TreeMap();
    TreeMap d2_e_cm_servlet_bc_template_hmap = new TreeMap();
    TreeMap d2_e_cm_servlet_bc_template_t3_hmap = new TreeMap();
    TreeMap d2_e_cm_servlet_bc_template_t4_hmap = new TreeMap();
    TreeMap d2_e_cm_object_dmap3_hmap = new TreeMap();

    TreeMap r_hmap = new TreeMap();

    Map[] d1_servlet_1_maps = new Map[4];
    Map[] d1_servlet_2_maps = new Map[4];
    Map[] d1_servlet_3_maps = new Map[4];
    Map[] d1_servlet_4_maps = new Map[4];
    Map[] d1_object_1_maps = new Map[3];
    Map[] d1_object_2_maps = new Map[3];

    Map[] d2_servlet_3_maps = new Map[4];
    Map[] d2_servlet_4_maps = new Map[4];

    Map[] d2_object_3_maps = new Map[3];
    String msg = "";
    int count = 0;
    int bc_cachesize = 2000;
    int dmap1_cachesize = 100;
    int dmap2_cachesize = 100;
    private final boolean found_dmap1 = false;
    private final boolean found_dmap2 = false;

    @Before
    public void setUp() throws Exception {

        setupPMIConfig();

        //STANDALONE testing for base run        

        //localDefaultServlet()....testing
        PMIHelper.initializeExpectedCounters(d1_e_cm_hmap, 0, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d1_e_cm_servlet_bc_hmap, 2, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d1_e_cm_servlet_bc_template_hmap, 15, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d1_e_cm_servlet_bc_template_t1_hmap, 15, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d1_e_cm_servlet_bc_template_t2_hmap, 15, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d1_e_cm_servlet_bc_template_t3_hmap, 15, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d1_e_cm_servlet_bc_template_t4_hmap, 15, PMIHelper.CACHE_SERVLET);

        //localDMap()....testing
        PMIHelper.initializeExpectedCounters(d1_e_cm_object_hmap1, 2, PMIHelper.CACHE_OBJECT);
        PMIHelper.initializeExpectedCounters(d1_e_cm_object_hmap2, 2, PMIHelper.CACHE_OBJECT);
        PMIHelper.initializeExpectedCounters(d1_e_cm_object_dmap1_hmap, 15, PMIHelper.CACHE_OBJECT);
        PMIHelper.initializeExpectedCounters(d1_e_cm_object_dmap2_hmap, 15, PMIHelper.CACHE_OBJECT);

        //ND testing in a truly distrbuted environment
        PMIHelper.initializeExpectedCounters(d2_e_cm_hmap, 0, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d2_e_cm_servlet_bc_hmap, 2, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d2_e_cm_servlet_bc_template_hmap, 15, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d2_e_cm_servlet_bc_template_t3_hmap, 15, PMIHelper.CACHE_SERVLET);
        PMIHelper.initializeExpectedCounters(d2_e_cm_servlet_bc_template_t4_hmap, 15, PMIHelper.CACHE_SERVLET);

        PMIHelper.initializeExpectedCounters(d2_e_cm_object_hmap, 2, PMIHelper.CACHE_OBJECT);
        PMIHelper.initializeExpectedCounters(d2_e_cm_object_dmap3_hmap, 15, PMIHelper.CACHE_OBJECT);

        //Expected Map initialization
        d1_servlet_1_maps[0] = d1_e_cm_hmap;
        d1_servlet_1_maps[1] = d1_e_cm_servlet_bc_hmap;
        d1_servlet_1_maps[2] = d1_e_cm_servlet_bc_template_hmap;
        d1_servlet_1_maps[3] = d1_e_cm_servlet_bc_template_t1_hmap;

        d1_servlet_2_maps[0] = d1_e_cm_hmap;
        d1_servlet_2_maps[1] = d1_e_cm_servlet_bc_hmap;
        d1_servlet_2_maps[2] = d1_e_cm_servlet_bc_template_hmap;
        d1_servlet_2_maps[3] = d1_e_cm_servlet_bc_template_t2_hmap;

        d1_servlet_3_maps[0] = d1_e_cm_hmap;
        d1_servlet_3_maps[1] = d1_e_cm_servlet_bc_hmap;
        d1_servlet_3_maps[2] = d1_e_cm_servlet_bc_template_hmap;
        d1_servlet_3_maps[3] = d1_e_cm_servlet_bc_template_t3_hmap;

        d1_servlet_4_maps[0] = d1_e_cm_hmap;
        d1_servlet_4_maps[1] = d1_e_cm_servlet_bc_hmap;
        d1_servlet_4_maps[2] = d1_e_cm_servlet_bc_template_hmap;
        d1_servlet_4_maps[3] = d1_e_cm_servlet_bc_template_t4_hmap;

        d1_object_1_maps[0] = d1_e_cm_hmap;
        d1_object_1_maps[1] = d1_e_cm_object_hmap1;
        d1_object_1_maps[2] = d1_e_cm_object_dmap1_hmap;

        d1_object_2_maps[0] = d1_e_cm_hmap;
        d1_object_2_maps[1] = d1_e_cm_object_hmap2;
        d1_object_2_maps[2] = d1_e_cm_object_dmap2_hmap;

        d2_servlet_3_maps[0] = d2_e_cm_hmap;
        d2_servlet_3_maps[1] = d2_e_cm_servlet_bc_hmap;
        d2_servlet_3_maps[2] = d2_e_cm_servlet_bc_template_hmap;
        d2_servlet_3_maps[3] = d2_e_cm_servlet_bc_template_t3_hmap;

        d2_servlet_4_maps[0] = d2_e_cm_hmap;
        d2_servlet_4_maps[1] = d2_e_cm_servlet_bc_hmap;
        d2_servlet_4_maps[2] = d2_e_cm_servlet_bc_template_hmap;
        d2_servlet_4_maps[3] = d2_e_cm_servlet_bc_template_t4_hmap;

        d2_object_3_maps[0] = d2_e_cm_hmap;
        d2_object_3_maps[1] = d2_e_cm_object_hmap;
        d2_object_3_maps[2] = d2_e_cm_object_dmap3_hmap;

        bc_cachesize = TestConfig.getBaseCacheSize();

    }

    protected void setupPMIConfig() throws Exception {

        PMIHelper.initializeCacheNames();
        pmiHost = TestConfig.getHost();
        pmiPort = Integer.toString(TestConfig.getPmiPort());
        nodeName = TestConfig.getNode();
        serverName = TestConfig.getServer();
        wc = startNewConversation();
        enablePMICounters(!DISTRIB, nodeName, serverName);

    }

    @Test
    public void testPMI() throws Exception {
        if (!SHARED_SERVER.isServerUp()) {
            throw new IllegalStateException("Junit setup for test PMITest NOT correct");
        }
        DynaCachePMIClient dpmic = new DynaCachePMIClient(TestConfig.host, TestConfig.soapPort, TestConfig.serverRoot);
        dpmic.enableDynaCacheStats(); //enable all the PMI statistics
        dpmic.forceFullMBeanName();
        setupPMIConfig(); // Reinitialize the PMITestServlet
        localDefaultServlet();
        localDMap();
        localInval(); // depends on localDefaultServlet 
    }

    /*
     * hits an unshared entry
     * tests:
     * 
     * In the tests below the usage of assertEquals(msg, expected value, actual value)
     * is opposite in terms of expected and actual value
     * 
     * hitsInMemory
     * hitsOnDisk
     * misses
     * entries
     * lruInvalidations
     */
    public void localDefaultServlet() throws Exception {

        if (debug) {
            System.out.println("localDefaultServlet... 1 ");
        }

        //
        // For 1st template: /dynacachetests/TimeStamp
        // 1 fresh request, should get a miss and an entry out of it
        // 
        resp = getWebResponse(wc, timeStampURI + "?arg1=-1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        /*
         * U will see these sleeps scattered thru the tests. On fast systems these need to be increases
         * and on slow systems decreased. Tests sometimes fail if these sleeps arent working the correct
         * combination.
         */
        PMIHelper.sleep(3 * WAIT_TIME); // delay 6 sec for the PMI counter to update

        if (debug) {
            System.out.println("localDefaultServlet... 2 ");
        }

        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.MaxInMemoryCacheEntryCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.InMemoryCacheEntryCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.MissCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.ClientRequestCount, 1, PMIHelper.TYPE_ADD);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET, r_hmap);
        assertEquals("PMITest.local DefaultServlet.1 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET, d1_e_cm_servlet_bc_hmap, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("PMITest.localDefaultServlet.2 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", d1_e_cm_servlet_bc_template_t1_hmap, r_hmap);

        if (debug) {
            System.out.println("localDefaultServlet... 3 ");
        }
        //
        // For 1st template: /dynacachetests/TimeStamp
        // 1 existing request, should get a hitInMemory
        // 
        resp = getWebResponse(wc, timeStampURI + "?arg1=-1");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("PMITest.localDefaultServlet.3 - count not equal", count, 15);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.HitsInMemoryCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.ClientRequestCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", d1_e_cm_servlet_bc_template_t1_hmap, r_hmap);

        if (debug) {
            System.out.println("localDefaultServlet... 4 ");
        }

        //
        // For 1st template: /dynacachetests/TimeStamp
        // (bc_cachesize*2) fresh request, should get bc_cachesize*2 more misses and bc_cachesize+1 lruInvalidation
        // 
        StringBuffer tmp = new StringBuffer(timeStampURI);
        for (int i = 0; i < (bc_cachesize * 2); i++) {
            tmp.append("?arg1=").append(i);
            resp = getWebResponse(wc, tmp.toString());
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            tmp.setLength(timeStampURI.length());
        }

        PMIHelper.sleep(3 * WAIT_TIME);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("PMITest.localDefaultServlet.4 - count not equal", count, 15);

        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, bc_cachesize - 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.InMemoryCacheEntryCount, bc_cachesize - 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.MissCount, bc_cachesize * 2, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.ClientRequestCount, bc_cachesize * 2, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.LruInvalidationCount, bc_cachesize + 1, PMIHelper.TYPE_ADD);
        System.out.println("r_hmap \n" + r_hmap);
        System.out.println("d1_e_cm_servlet_bc_template_t1_hmap \n" + d1_e_cm_servlet_bc_template_t1_hmap);

        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", d1_e_cm_servlet_bc_template_t1_hmap, r_hmap);

        if (debug) {
            System.out.println("localDefaultServlet... 5 ");
        }
        //
        // For 1st template: /dynacachetests/TimeStamp
        // bc_cachesize fresh request, should get bc_cachesize more misses and bc_cachesize lruInvalidation
        // 
        for (int i = 0; i < bc_cachesize; i++) {
            tmp.append("?arg1=").append(i);
            resp = getWebResponse(wc, tmp.toString());
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            tmp.setLength(timeStampURI.length());
        }
        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("PMITest.localDefaultServlet.5 - count not equal", count, 15);

        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.MissCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.ClientRequestCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.LruInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", d1_e_cm_servlet_bc_template_t1_hmap, r_hmap);

        PMIHelper.sleep(WAIT_TIME * 4); //8 seconds 

        if (debug) {
            System.out.println("localDefaultServlet... 6 ");
        }

        //hit 1st bc_cachesize/2 entries again, should get bc_cachesize/2 hitsInMemory
        //should get 0 more lruInvals
        for (int i = 0; i < (bc_cachesize / 2); i++) {
            tmp.append("?arg1=").append(i);
            resp = getWebResponse(wc, tmp.toString());
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            tmp.setLength(timeStampURI.length());
        }

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("PMITest.localDefaultServlet.6 - count not equal", count, 15);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.HitsInMemoryCount, bc_cachesize / 2, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.ClientRequestCount, bc_cachesize / 2, PMIHelper.TYPE_ADD);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", d1_e_cm_servlet_bc_template_t1_hmap, r_hmap);

        PMIHelper.sleep(WAIT_TIME * 4); //4 seconds

        if (debug) {
            System.out.println("localDefaultServlet... 7 ");
        }
        //
        // For 2nd template: /dynacachetests/TimeStamp2
        // (bc_cachesize+1) fresh requests 
        // 
        tmp = new StringBuffer(timeStamp2URI);
        for (int i = 0; i < bc_cachesize + 1; i++) {
            tmp.append("?arg1=").append(i);
            resp = getWebResponse(wc, tmp.toString());
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            tmp.setLength(timeStamp2URI.length());
        }

        PMIHelper.sleep(WAIT_TIME * 10);

        PMIHelper.setExpectedCounter(d1_servlet_2_maps, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, bc_cachesize, PMIHelper.TYPE_SUBTRACT);
        PMIHelper.setExpectedCounter(d1_servlet_2_maps, WSDynamicCacheStats.MissCount, bc_cachesize + 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_2_maps, WSDynamicCacheStats.ClientRequestCount, bc_cachesize + 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_2_maps, WSDynamicCacheStats.LruInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.LruInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp2", r_hmap);
        assertEquals("PMITest.localDefaultServlet.7 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp2", d1_e_cm_servlet_bc_template_t2_hmap, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("PMITest.localDefaultServlet.8 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", d1_e_cm_servlet_bc_template_t1_hmap, r_hmap);

        //STAT aggregation does NOT work 

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE, r_hmap);
        assertEquals("PMITest.localDefaultServlet.9 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE, d1_e_cm_servlet_bc_template_hmap, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET, r_hmap);
        assertEquals("PMITest.localDefaultServlet.10 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET, d1_e_cm_servlet_bc_hmap, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, WSDynamicCacheStats.NAME, r_hmap);
        assertEquals("PMITest.localDefaultServlet.12 - count not equal", count, 17);
    }

    /*
     * Using DistributedMap to verify the following counters:
     * - hitsInMemory
     * - misses
     * - entries
     * - requestFromClient
     */

    public void localDMap() throws Exception {

        if (debug) {
            System.out.println("localDMap... 1");
        }

        resp = getWebResponse(wc, dmapURI + "?action=get&key=testkey&instance=" + dmap1 + "&Submit=Submit");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        PMIHelper.sleep(WAIT_TIME); // delay 2 sec

        if (debug) {
            System.out.println("localDMap... 2");
        }
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.MaxInMemoryCacheEntryCount, 1000, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.MissCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.ClientRequestCount, 1, PMIHelper.TYPE_ADD);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap1, r_hmap);
        assertEquals("PMITest.localDMap.2 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap1, d1_e_cm_object_hmap1, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap1 + ";" + PMIHelper.OBJECT_MODULE, r_hmap);
        assertEquals("PMITest.localDMap.2 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap1 + ";" + PMIHelper.OBJECT_MODULE, d1_e_cm_object_dmap1_hmap, r_hmap);

        if (debug) {
            System.out.println("localDMap... 3");
        }
        resp = getWebResponse(wc, dmapURI + "?action=put&key=testkey&value=testvalue&sharing=none&instance=" + dmap1 + "&timeLimit=0" + "&Submit=Submit");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.InMemoryCacheEntryCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.ClientRequestCount, 1, PMIHelper.TYPE_ADD);

        PMIHelper.sleep(3 * WAIT_TIME); // delay 6 seconds for the PMI counters to update

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap1, r_hmap);
        assertEquals("PMITest.localDMap.3 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap1, d1_e_cm_object_hmap1, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap1 + ";" + PMIHelper.OBJECT_MODULE, r_hmap);
        assertEquals("PMITest.localDMap.3 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap1 + ";" + PMIHelper.OBJECT_MODULE, d1_e_cm_object_dmap1_hmap, r_hmap);

        if (debug) {
            System.out.println("localDMap... 4");
        }
        resp = getWebResponse(wc, dmapURI + "?action=get&key=testkey&instance=" + dmap1 + "&Submit=Submit");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.HitsInMemoryCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.ClientRequestCount, 1, PMIHelper.TYPE_ADD);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap1 + ";" + PMIHelper.OBJECT_MODULE, r_hmap);
        assertEquals("PMITest.localDMap.4 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap1 + ";" + PMIHelper.OBJECT_MODULE, d1_e_cm_object_dmap1_hmap, r_hmap);

        if (debug) {
            System.out.println("localDMap... 5");
        }
        resp = getWebResponse(wc, dmapURI + "?action=get&key=testkey&instance=" + dmap2 + "&Submit=Submit");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.MaxInMemoryCacheEntryCount, 1000, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.ClientRequestCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.MissCount, 1, PMIHelper.TYPE_ADD);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap2, r_hmap);
        assertEquals("PMITest.localDMap.5 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap2, d1_e_cm_object_hmap2, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap2 + ";" + PMIHelper.OBJECT_MODULE, r_hmap);
        assertEquals("PMITest.localDMap.5 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap2 + ";" + PMIHelper.OBJECT_MODULE, d1_e_cm_object_dmap2_hmap, r_hmap);

        if (debug) {
            System.out.println("localDMap... 6");
        }
        resp = getWebResponse(wc, dmapURI + "?action=put&key=testkey&value=testvalue&sharing=none&instance=" + dmap2 + "&timeLimit=0" + "&Submit=Submit");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.InMemoryCacheEntryCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.ClientRequestCount, 1, PMIHelper.TYPE_ADD);

        PMIHelper.sleep(3 * WAIT_TIME);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap2, r_hmap);
        assertEquals("PMITest.localDMap.6 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap2, d1_e_cm_object_hmap2, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap2 + ";" + PMIHelper.OBJECT_MODULE, r_hmap);
        assertEquals("PMITest.localDMap.6 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap2 + ";" + PMIHelper.OBJECT_MODULE, d1_e_cm_object_dmap2_hmap, r_hmap);

        if (debug) {
            System.out.println("localDMap... 7");
        }
        resp = getWebResponse(wc, dmapURI + "?action=invalidate&key=testkey&instance=" + dmap1 + "&Submit=Submit");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        resp = getWebResponse(wc, dmapURI + "?action=invalidate&key=testkey&instance=" + dmap2 + "&Submit=Submit");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.ExplicitInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.ExplicitMemoryInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.LocalExplicitInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.InMemoryCacheEntryCount, 1, PMIHelper.TYPE_SUBTRACT);
        PMIHelper.setExpectedCounter(d1_object_1_maps, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, 1, PMIHelper.TYPE_SUBTRACT);

        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.ExplicitInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.ExplicitMemoryInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.LocalExplicitInvalidationCount, 1, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.InMemoryCacheEntryCount, 1, PMIHelper.TYPE_SUBTRACT);
        PMIHelper.setExpectedCounter(d1_object_2_maps, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, 1, PMIHelper.TYPE_SUBTRACT);

        PMIHelper.sleep(3 * WAIT_TIME);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap1, r_hmap);
        assertEquals("PMITest.localDMap.7 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap2, d1_e_cm_object_hmap1, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap2, r_hmap);
        assertEquals("PMITest.localDMap.7 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap1, d1_e_cm_object_hmap2, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap1 + ";" + PMIHelper.OBJECT_MODULE, r_hmap);
        assertEquals("PMITest.localDMap.7 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap1 + ";" + PMIHelper.OBJECT_MODULE, d1_e_cm_object_dmap1_hmap, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_OBJECT + dmap2 + ";" + PMIHelper.OBJECT_MODULE, r_hmap);
        assertEquals("PMITest.localDMap.7 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_OBJECT + dmap2 + ";" + PMIHelper.OBJECT_MODULE, d1_e_cm_object_dmap2_hmap, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, WSDynamicCacheStats.NAME, r_hmap);
        assertEquals("PMITest.localDMap.7 - count not equal", count, 17);

    }

    /**
     * hits an unshared entry
     * tests:
     * totalTimeoutInvalidations
     * timeoutInvalidations
     * explicitInvalidations
     * explicitInvalidationsFromMemory
     * explicitInvalidationsFromDisk
     * explicitInvalidationsLocal
     */

    public void localInval() throws Exception {

        if (debug) {
            System.out.println("localInval... 1 ");
        }

        //NOTE THIS HAS A DEPENDENCY ON localservlet()....
        // invalidate all the cache entries for TimeStamp2
        StringBuffer tmp = new StringBuffer(timeStamp2URI);
        for (int i = 0; i < (bc_cachesize + 1); i++) {
            tmp.append("?inv=").append(i);
            resp = getWebResponse(wc, tmp.toString());
            tmp.setLength(timeStamp2URI.length());
        }

        PMIHelper.setExpectedCounter(d1_servlet_2_maps, WSDynamicCacheStats.ExplicitInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_2_maps, WSDynamicCacheStats.ExplicitMemoryInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_2_maps, WSDynamicCacheStats.LocalExplicitInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_2_maps, WSDynamicCacheStats.InMemoryAndDiskCacheEntryCount, bc_cachesize, PMIHelper.TYPE_SUBTRACT);
        PMIHelper.setExpectedCounter(d1_servlet_2_maps, WSDynamicCacheStats.InMemoryCacheEntryCount, bc_cachesize, PMIHelper.TYPE_SUBTRACT);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp2", r_hmap);
        assertEquals("PMITest.localInval.1 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp2", d1_e_cm_servlet_bc_template_t2_hmap, r_hmap);
        //System.out.println("**** timestamp2=" + r_hmap);

        //do the timeouts
        //gen bc_cachesize hits, wait for timeouts
        //should have bc_cachesize timeouts

        if (debug) {
            System.out.println("localInval... 2 ");
        }

        tmp = new StringBuffer(timeStampURI);
        for (int i = 0; i < bc_cachesize; i++) {
            tmp.append("?timeout=").append(i);
            resp = getWebResponse(wc, tmp.toString());
            tmp.setLength(timeStampURI.length());
        }

        PMIHelper.sleep(50000 * invDelay); // delay 50 * invDelay sec

        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.MissCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.ClientRequestCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.TimeoutInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("PMITest.localInval.3 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", d1_e_cm_servlet_bc_template_t1_hmap, r_hmap);
        //System.out.println("**** timestamp=" + r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp2", r_hmap);
        assertEquals("PMITest.localInval.4 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp2", d1_e_cm_servlet_bc_template_t2_hmap, r_hmap);
        //System.out.println("**** timestamp2=" + r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE, r_hmap);
        assertEquals("PMITest.localInval.5 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE, d1_e_cm_servlet_bc_template_hmap, r_hmap);
        //System.out.println("**** template=" + r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET, r_hmap);
        assertEquals("PMITest.localInval.6 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET, d1_e_cm_servlet_bc_hmap, r_hmap);
        //System.out.println("**** baseCache=" + r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, WSDynamicCacheStats.NAME, r_hmap);
        assertEquals("PMITest.localInval.11 - count not equal", count, 17);

        if (debug) {
            System.out.println("localInval... 3 ");
        }

        //do the explicits
        //gen 2*bc_cachesize entries, then invalidate them
        //should get 2*bc_cachesize total
        //should get 2*bc_cachesize local
        //should get bc_cachesize in memory
        //should get bc_cachesize on disk
        for (int i = 0; i < (bc_cachesize * 2); i++) {
            tmp.append("?arg1=").append(i);
            resp = getWebResponse(wc, tmp.toString());
            tmp.setLength(timeStampURI.length());
        }
        for (int i = 0; i < (bc_cachesize * 2); i++) {
            tmp.append("?inv=").append(i);
            resp = getWebResponse(wc, tmp.toString());
            tmp.setLength(timeStampURI.length());
        }

        PMIHelper.sleep(WAIT_TIME * invDelay * 3); // delay 6 sec

        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.ExplicitInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.ExplicitMemoryInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.LocalExplicitInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.MissCount, bc_cachesize * 2, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.LruInvalidationCount, bc_cachesize, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(d1_servlet_1_maps, WSDynamicCacheStats.ClientRequestCount, bc_cachesize * 2, PMIHelper.TYPE_ADD);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", r_hmap);
        assertEquals("PMITest.localInval.7 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE + ";/dynacachetests/TimeStamp", d1_e_cm_servlet_bc_template_t1_hmap, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET_TEMPLATE, r_hmap);
        assertEquals("PMITest.localInval.8 - count not equal", count, 15);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET_TEMPLATE, d1_e_cm_servlet_bc_template_hmap, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, PMIHelper.CACHE_SERVLET, r_hmap);
        assertEquals("PMITest.localInval.9 - count not equal", count, 2);
        PMIHelper.compareCounters(PMIHelper.CACHE_SERVLET, d1_e_cm_servlet_bc_hmap, r_hmap);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, WSDynamicCacheStats.NAME, r_hmap);
        assertEquals("PMITest.localInval.11 - count not equal", count, 17);

    }

    public void enablePMICounters(boolean distrib, String node, String server) throws Exception {

        String url = "";
        if (!distrib) {
            url = pmiTestURI + "?method=enable&host=" + pmiHost + "&port=" + pmiPort + "&connector=" + connector +
                  "&node=" + node + "&server=" + server + "&pmitree=" + WSDynamicCacheStats.NAME;
            //System.out.println("*** URL=" + url);
            resp = getWebResponse(wc, url);
        } else {
            url = pmiTestURI + "?method=enable&host=" + pmiHost + "&port=" + pmiPort + "&connector=" + connector +
                  "&node=" + node + "&server=" + server + "&pmitree=" + WSDynamicCacheStats.NAME;
            //System.out.println("*** Distrib URL=" + url);
            resp = getCloneWebResponse(wc, url);
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
            //System.out.println("STANDALONE:"+ pmiTestURI + "?method=read&pmitree=" + cacheModules);
            resp = getWebResponse(wc, pmiTestURI + "?method=read&pmitree=" + cacheModules);
        } else {
            //System.out.println("DISTRIB:"+ pmiTestURI + "?method=read&pmitree=" + cacheModules);
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
                    //System.out.println(cacheModules+ ":" + 0);
                    return 0;
                }
                int eindex = s.indexOf("</body>", sindex);
                s = s.substring(sindex + "ReturnOutput: ".length(), eindex);
                if (debug) {
                    System.out.println("readCountersFromPMI-->" + cacheModules + ":[" + s + "]");
                }
                count = PMIHelper.parseCounters(s.trim(), hmap);
            }
        }
        return count;
    }

}