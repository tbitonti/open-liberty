// 1.4, 10/9/07
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.htod;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.cache.CacheConfig;
import com.ibm.ws.cache.DCacheBase;
import com.ibm.ws.cache.TestConfig;
import com.ibm.ws.cache.pmi.DynaCachePMIClient;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class HTODTest3 extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("HTODTest3");

    String runTest = "all";
    //String runTest             = "testObjectsAsyncLruToDisk";
    //String runTest             = "testClearCache";
    int htodPerf = CacheConfig.HIGH;
    //int htodPerf               = CacheConfig.CUSTOM;
    //int htodPerf               = CacheConfig.LOW;

    public static String URI = "/dynacachetests/dmapstress2";

    String uri = "";
    String result = "";

    // Test ObjectsAsyncLruToDisk
    // It is required to have lruToDiskTriggerTime to be set to 100 before the server is started.
    //
    @Test
    public void testObjectsAsyncLruToDisk() throws Exception {

        if (runTest.equalsIgnoreCase("testObjectsAsyncLruToDisk")) {

            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("testObjectsAsyncLruToDisk...");

            String jndiServletCacheName = "services/cache/htodServletInstance_20";
            String jndiObjectCacheName = "services/cache/htodObjectInstance_21";

            // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D = 16  thread params
            String uri = URI + // Total items = 500
                         "?threadParms=" +
                         "1_5000_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // servlet (5000 put)
                         "1_5000_1_0_1_0_0_0_0_0_0_0_0_0_0_1;" + // dmap    (5000 put)
                         "1_5000_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // servlet (5000 put)
                         "1_5000_1_0_1_0_0_0_0_0_0_0_0_0_0_1;" + // dmap    (5000 put)
                         "1_5000_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // servlet (5000 put)
                         "1_5000_1_0_1_0_0_0_0_0_0_0_0_0_0_1;" + // dmap    (5000 put)
                         "&valueSize=100&depids=100" +
                         "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=6" +
                         "&win=100&delayAT=10000&exps=-1&expd=-1&clearBT=true" +
                         "&cacheServletName=" + jndiServletCacheName +
                         "&cacheDmapName=" + jndiObjectCacheName +
                         "&resetCount=true&savCount=true&idd=false&method=test1";
            resp = getWebResponse(wc, uri);

            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            //System.out.println(result);

            if (!SHARED_SERVER.isServerUp()) {
                throw new IllegalStateException("Junit setup for test HTODTest3 NOT correct");
            }

            // instantiate the JMX Client...Reads Dmgr properties from the TestConfig
            DynaCachePMIClient pmiDC = new DynaCachePMIClient(TestConfig.host, TestConfig.soapPort, TestConfig.serverRoot);

            String[] servletStatistics = pmiDC.getAllMbeanStat(jndiServletCacheName, TestConfig.getServer());
            int statisticCount = DynaCachePMIClient.getMbeanStat(servletStatistics, "ObjectsOnDisk");
            if (statisticCount != 10000) {
                fail("testObjectsAsyncLruToDisk.1 - received ObjectsOnDisk=" + statisticCount + " expected=10000");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(servletStatistics, "MemoryCacheEntries");
            if (statisticCount != 5000) {
                fail("testObjectsAsyncLruToDisk.2 - received MemoryCacheEntries=" + statisticCount + " expected=5000");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(servletStatistics, "ObjectsAsyncLruToDisk");
            if (statisticCount != 0) {
                fail("testObjectsAsyncLruToDisk.3 - received ObjectsAsyncLruToDisk=" + statisticCount + " expected=0");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(servletStatistics, "TemplatesBufferedForDisk");
            if (statisticCount != 3) {
                fail("testObjectsAsyncLruToDisk.4 - received TemplatesBufferedForDisk=" + statisticCount + " expected=3");
            }

            String[] objectStatistics = pmiDC.getAllMbeanStat(jndiObjectCacheName, TestConfig.getServer());
            statisticCount = DynaCachePMIClient.getMbeanStat(objectStatistics, "ObjectsOnDisk");
            if (statisticCount != 10000) {
                fail("testObjectsAsyncLruToDisk.11 - received ObjectsOnDisk=" + statisticCount + " expected=10000");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(objectStatistics, "MemoryCacheEntries");
            if (statisticCount != 5000) {
                fail("testObjectsAsyncLruToDisk.12 - received MemoryCacheEntries=" + statisticCount + " expected=5000");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(objectStatistics, "ObjectsAsyncLruToDisk");
            if (statisticCount < 1000) {
                fail("testObjectsAsyncLruToDisk.13 - received ObjectsAsyncLruToDisk=" + statisticCount + " expected > 1000");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(objectStatistics, "TemplatesBufferedForDisk");
            if (statisticCount != 0) {
                fail("testObjectsAsyncLruToDisk.14 - received TemplatesBufferedForDisk=" + statisticCount + " expected=0");
            }
        }
    }

    @Test
    public void testClearCache() throws Exception {

        if (runTest.equalsIgnoreCase("all") ||
            runTest.equalsIgnoreCase("testClearCache")) {

            String[] statistics = null;
            String[] instanceNames = null;

            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("testClearCache...");

            if (!SHARED_SERVER.isServerUp()) {
                throw new IllegalStateException("Junit setup for test HTODTest3 NOT correct");
            }

            // instantiate the JMX Client...Reads Dmgr properties from the TestConfig
            DynaCachePMIClient pmiDC = new DynaCachePMIClient(TestConfig.host, TestConfig.soapPort, TestConfig.serverRoot);

            instanceNames = pmiDC.getCacheInstanceNames(TestConfig.getServer());

            int exp_num_instances = instanceNames.length;
            if (DynaCachePMIClient.isCacheInstanceExist(instanceNames, DCacheBase.DEFAULT_CACHE_NAME) == false) {
                fail("testClearCache.1 - baseCache does not exist");
            }

            statistics = pmiDC.getAllMbeanStat(DCacheBase.DEFAULT_BASE_JNDI_NAME, TestConfig.getServer());
            if (statistics == null || (statistics != null && statistics.length == 0)) {
                fail("testClearCache.2 - statistics for " + DCacheBase.DEFAULT_BASE_JNDI_NAME + " are null or empty");
            }

            statistics = pmiDC.getAllMbeanStat(DCacheBase.DEFAULT_CACHE_NAME, TestConfig.getServer());
            if (statistics == null || (statistics != null && statistics.length == 0)) {
                fail("testClearCache.3 - statistics for " + DCacheBase.DEFAULT_CACHE_NAME + " are null or empty");
            }

            if (DynaCachePMIClient.isCacheInstanceExist(instanceNames, DCacheBase.DEFAULT_DISTRIBUTED_MAP_NAME) == true) {
                fail("testClearCache.4 - " + DCacheBase.DEFAULT_DISTRIBUTED_MAP_NAME + " exists");
            }

            instanceNames = pmiDC.getCacheInstanceNames(TestConfig.getServer());
            if (instanceNames.length != exp_num_instances) {
                fail("testClearCache.5 - numInstanceNames received=" + instanceNames.length + " expected=" + exp_num_instances);
            }

            if (DynaCachePMIClient.isCacheInstanceExist(instanceNames, DCacheBase.DEFAULT_BASE_JNDI_NAME) == true) {
                fail("testClearCache.6 - services/cache/basecache exist");
            }

            if (DynaCachePMIClient.isCacheInstanceExist(instanceNames, DCacheBase.DEFAULT_DMAP_JNDI_NAME) == true) {
                fail("testClearCache.7 - services/cache/distributedmap exist");
            }

            if (DynaCachePMIClient.isCacheInstanceExist(instanceNames, "services/cache/htodServletInstance_3") == true) {
                fail("testClearCache.8 - services/cache/htodServletInstance_3 exist");
            }

            // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D = 16  thread params
            String uri = URI + // Total items = 500
                         "?threadParms=" +
                         "1_500_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // servlet (500 put)
                         "1_100_1_0_1_0_0_0_0_0_0_0_0_0_0_1;" + // dmap    (100 put)
                         "&valueSize=100&depids=10" +
                         "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=2" +
                         "&win=100&delayAT=10000&exps=-1&expd=-1&clearBT=true" +
                         "&cacheServletName=" + "services/cache/htodServletInstance_3" +
                         "&cacheDmapName=" + DCacheBase.DEFAULT_DMAP_JNDI_NAME +
                         "&resetCount=true&savCount=true&idd=false&method=test1";
            resp = getWebResponse(wc, uri);

            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            //System.out.println(result);

            statistics = pmiDC.getAllMbeanStat("services/cache/htodServletInstance_3", TestConfig.getServer());
            int statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "ObjectsOnDisk");
            if (statisticCount != 400) {
                fail("testClearCache.10 - received ObjectsOnDisk=" + statisticCount + " expected=500");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "MemoryCacheEntries");
            if (statisticCount != 100) {
                fail("testClearCache.11 - received MemoryCacheEntries=" + statisticCount + " expected=100");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "DependencyIdsOnDisk");
            if (statisticCount < 30) {
                fail("testClearCache.12 - received depIds=" + statisticCount + " expected > 30");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "TemplatesOnDisk");
            if (statisticCount != 1) {
                fail("testClearCache.13 - received templates=" + statisticCount + " expected=1");
            }
            Log.info(getClass(), "1", "3");
            statistics = pmiDC.getAllMbeanStat(DCacheBase.DEFAULT_DMAP_JNDI_NAME, TestConfig.getServer());
            //pmiDC.displayMbeanStat(statistics, Cache.DEFAULT_DMAP_JNDI_NAME);
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "ObjectsOnDisk");
            if (statisticCount != 0) {
                fail("testClearCache.14 - received ObjectsOnDisk=" + statisticCount + " expected=0");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "MemoryCacheEntries");
            if (statisticCount != 100) {
                fail("testClearCache.15 - received MemoryCacheEntries=" + statisticCount + " expected=100");
            }
            Log.info(getClass(), "1", "4");
            statistics = pmiDC.getAllMbeanStat(DCacheBase.DEFAULT_DISTRIBUTED_MAP_NAME, TestConfig.getServer());
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "ObjectsOnDisk");
            if (statisticCount != 0) {
                fail("testClearCache.16 - received ObjectsOnDisk=" + statisticCount + " expected=0");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "MemoryCacheEntries");
            if (statisticCount != 100) {
                fail("testClearCache.17 - received MemoryCacheEntries=" + statisticCount + " expected=100");
            }

            // MBean's clearCache()
            pmiDC.clearCache("services/cache/htodServletInstance_3", TestConfig.getServer());
            pmiDC.clearCache(DCacheBase.DEFAULT_DMAP_JNDI_NAME, TestConfig.getServer());
            Log.info(getClass(), "1", "5");
            statistics = pmiDC.getAllMbeanStat("services/cache/htodServletInstance_3", TestConfig.getServer());
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "ObjectsOnDisk");
            if (statisticCount != 0) {
                fail("testClearCache.18 - received ObjectsOnDisk=" + statisticCount + " expected=0");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "MemoryCacheEntries");
            if (statisticCount != 0) {
                fail("testClearCache.19 - received MemoryCacheEntries=" + statisticCount + " expected=0");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "DependencyIdsOnDisk");
            if (statisticCount != 0) {
                fail("testClearCache.20 - received depIds=" + statisticCount + " expected=0");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "TemplatesOnDisk");
            if (statisticCount != 0) {
                fail("testClearCache.21 - received templates=" + statisticCount + " expected=0");
            }
            Log.info(getClass(), "1", "6");
            statistics = pmiDC.getAllMbeanStat(DCacheBase.DEFAULT_DMAP_JNDI_NAME, TestConfig.getServer());
            //pmiDC.displayMbeanStat(statistics, Cache.DEFAULT_DMAP_JNDI_NAME);
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "ObjectsOnDisk");
            if (statisticCount != 0) {
                fail("testClearCache.22 - received ObjectsOnDisk=" + statisticCount + " expected=0");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "MemoryCacheEntries");
            if (statisticCount != 0) {
                fail("testClearCache.23 - received MemoryCacheEntries=" + statisticCount + " expected=0");
            }

            instanceNames = pmiDC.getCacheInstanceNames(TestConfig.getServer());
            // baseCache, default & services/cache/htodServletInstance_3
            Assert.assertEquals("testClearCache.24 - numInstanceNames", 3, instanceNames.length);

            Log.info(getClass(), "1", "7");
            // Test for reset statistics counters
            // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D = 16  thread params
            uri = URI +
                  "?valueSize=100&depids=10" +
                  "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0" +
                  "&win=100&delayAT=10000&exps=-1&expd=-1&clearBT=false" +
                  "&cacheServletName=" + "services/cache/htodServletInstance_3" +
                  "&cacheDmapName=" + DCacheBase.DEFAULT_DMAP_JNDI_NAME +
                  "&resetCount=true&savCount=true&idd=false";
            resp = getWebResponse(wc, uri);

            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            //System.out.println(s);
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            //System.out.println(result);
            statistics = pmiDC.getAllMbeanStat("services/cache/htodServletInstance_3", TestConfig.getServer());
            //pmiDC.displayMbeanStat(statistics, "services/cache/htodServletInstance_3");
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "CacheRemoves");
            if (statisticCount != 0) {
                fail("testClearCache.25 - received CacheRemoves=" + statisticCount + " expected=0");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "ExplicitInvalidationsFromMemory");
            if (statisticCount != 0) {
                fail("testClearCache.26 - received ExplicitInvalidationsFromMemory=" + statisticCount + " expected=0");
            }
            Log.info(getClass(), "1", "8");
            statistics = pmiDC.getAllMbeanStat(DCacheBase.DEFAULT_DMAP_JNDI_NAME, TestConfig.getServer());
            //pmiDC.displayMbeanStat(statistics, Cache.DEFAULT_DMAP_JNDI_NAME);
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "CacheRemoves");
            if (statisticCount != 0) {
                fail("testClearCache.27 - received CacheRemoves=" + statisticCount + " expected=0");
            }
            statisticCount = DynaCachePMIClient.getMbeanStat(statistics, "ExplicitInvalidationsFromMemory");
            if (statisticCount != 0) {
                fail("testClearCache.28 - received ExplicitInvalidationsFromMemory=" + statisticCount + " expected=0");
            }
            Log.info(getClass(), "1", "10");
        }
    }
}
