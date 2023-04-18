// 1.5, 2/11/08
// IBM Confidential OCO Source Material
// 5724-I63, 5724-H88, 5655-N01, 5733-W60 (C) COPYRIGHT International Business Machines Corp. 1997, 2005
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.htod;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.CacheConfig;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class HTODTest2 extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("HTODTest2");

    /*
     * 
     * PMI can be enable or disabled
     * cachesize = 100
     * 
     * High Performance:
     * com.ibm.ws.cache.CacheConfig.diskCachePerformanceLevel = 3
     * com.ibm.ws.cache.CacheConfig.ignoreValueInInvalidationEvent = true
     * com.ibm.ws.cache.CacheConfig.flushToDiskOnStop = true
     * com.ibm.ws.cache.flushToDiskOnStop = true
     * 
     * Custom Performance:
     * com.ibm.ws.cache.CacheConfig.diskCachePerformanceLevel = 2
     * com.ibm.ws.cache.CacheConfig.htodCleanupFrequency = 2
     * com.ibm.ws.cache.CacheConfig.htodDelayOffloadEntriesLimit = 10000
     * com.ibm.ws.cache.CacheConfig.ignoreValueInInvalidationEvent = true
     * com.ibm.ws.cache.CacheConfig.flushToDiskOnStop = true
     * com.ibm.ws.cache.flushToDiskOnStop = true
     */

    //String runTest             = "all";
    //String runTest             = "testIListenerTest1";
    //String runTest             = "testIListenerTest2";
    //String runTest             = "testTimeoutInvalidation";
    //String runTest             = "testClearTest1";
    //String runTest             = "testCacheIdAsDepIdTest1";
    //String runTest             = "testCacheIdAsDepIdTest2";
    //String runTest             = "testCacheIdAsDepIdTest3";
    //String runTest             = "testFlushToDiskOnStop_1";
    //String runTest             = "testFlushToDiskOnStop_2";
    //String runTest             = "testAfterFlushToDiskOnStop";
    //String runTest             = "testDiskCacheSize";
    //String runTest             = "testPushPullInvalidation1";
    //String runTest             = "testPushPullInvalidation2";
    //String runTest               = "testExplicitMbeanCounters";
    String runTest = "testCacheMbeanCounters";
    //String runTest               = "testResetPMI";
    int htodPerf = CacheConfig.HIGH;
    //int htodPerf               = CacheConfig.CUSTOM; 
    //int htodPerf               = CacheConfig.LOW; 

    public static String URI = "/dynacachetests/dmapstress2";
    public final static int MIN_DISK_DATA_SIZE = (477551 + 47743 + 1031) * 8;

    String uri = "";
    String result = "";

    // for servlet cache
    long sMemEntriesSize = 0;
    long sDiskEntriesSize = 0;
    long sObjectsOnDisk = 0;
    long sDependencyIdsInMemoryAndDisk = 0;
    long sDependencyIdsOnDisk = 0;
    long sTemplatesOnDisk = 0;
    long sTotalCacheDataDiskSize = 0;
    long sCacheHits = 0;
    long sCacheMisses = 0;
    long sCacheRemoves = 0;
    long sCacheLruRemoves = 0;
    long sExplicitInvalidationsFromMemory = 0;
    long sExplicitInvalidationsFromDisk = 0;
    long sExplicitInvalidationsLocal = 0;
    long sExplicitInvalidationsRemote = 0;
    long sTimeoutInvalidationsFromMemory = 0;
    long sTimeoutInvalidationsFromDisk = 0;
    long sPendingRemovalFromDisk = 0;
    long sDependencyIdsBufferedForDisk = 0;
    long sDependencyIdsOffloadedToDisk = 0;
    long sDependencyIdBasedInvalidationsFromDisk = 0;
    long sTemplatesBufferedForDisk = 0;
    long sTemplatesOffloadedToDisk = 0;
    long sTemplateBasedInvalidationsFromDisk = 0;
    long sObjectsReadFromDisk = 0;
    long sObjectsReadFromDisk4K = 0;
    long sObjectsReadFromDisk40K = 0;
    long sObjectsReadFromDisk400K = 0;
    long sObjectsReadFromDisk4000K = 0;
    long sObjectsReadFromDiskSize = 0;
    long sObjectsWriteToDisk = 0;
    long sObjectsWriteToDisk4K = 0;
    long sObjectsWriteToDisk40K = 0;
    long sObjectsWriteToDisk400K = 0;
    long sObjectsWriteToDisk4000K = 0;
    long sObjectsWriteToDiskSize = 0;
    long sObjectsDeleteFromDisk = 0;
    long sObjectsDeleteFromDisk4K = 0;
    long sObjectsDeleteFromDisk40K = 0;
    long sObjectsDeleteFromDisk400K = 0;
    long sObjectsDeleteFromDisk4000K = 0;
    long sObjectsDeleteFromDiskSize = 0;
    long sPushPullTableSize = 0;
    long sRemoteInvalidationNotifications = 0;
    long sRemoteUpdateNotifications = 0;
    long sRemoteObjectUpdates = 0;
    long sRemoteObjectUpdateSize = 0;
    long sRemoteObjectHits = 0;
    long sRemoteObjectFetchSize = 0;
    long sRemoteObjectMisses = 0;
    long sInvalidationLocal = 0;
    long sInvalidationLocalExplicit = 0;
    long sInvalidationLocalLRU = 0;
    long sInvalidationLocalTimeout = 0;
    long sInvalidationLocalDiskTimeout = 0;
    long sInvalidationLocalClearAll = 0;
    long sInvalidationRemote = 0;
    long sInvalidationRemoteExplicit = 0;
    long sInvalidationRemoteLRU = 0;
    long sInvalidationRemoteTimeout = 0;
    long sInvalidationRemoteDiskTimeout = 0;
    long sInvalidationRemoteClearAll = 0;

    // for dmap cache
    long dMemEntriesSize = 0;
    long dDiskEntriesSize = 0;
    long dObjectsOnDisk = 0;
    long dDependencyIdsInMemoryAndDisk = 0;
    long dDependencyIdsOnDisk = 0;
    long dTemplatesOnDisk = 0;
    long dTotalCacheDataDiskSize = 0;
    long dInvalidationLocal = 0;
    long dInvalidationLocalExplicit = 0;
    long dInvalidationLocalLRU = 0;
    long dInvalidationLocalTimeout = 0;
    long dInvalidationLocalDiskTimeout = 0;
    long dInvalidationLocalClearAll = 0;
    long dInvalidationRemote = 0;
    long dInvalidationRemoteExplicit = 0;
    long dInvalidationRemoteLRU = 0;
    long dInvalidationRemoteTimeout = 0;
    long dInvalidationRemoteDiskTimeout = 0;
    long dInvalidationRemoteClearAll = 0;

    boolean ok = true;

    // Use cache id as dep id (idd=true)
    // No timeout
    // 10 threads
    // 5 threads dep id invalidation (put)
    // 4 threads (no timeout) (put)
    // 1 thread template invalidation (put)
    @Test
    public void testCacheIdAsDepIdTest1() throws Exception {
        if (ok && (runTest.equalsIgnoreCase("all") || runTest.equalsIgnoreCase("testCacheIdAsDepIdTest1"))) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("CacheIdAsDepIdTest1...");
            uri = URI
                  + "?threadParms=1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&idd=true&clearBT=true&delayAT=30000&exps=-1&expd=-1&savCount=true&method=test1";
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                ok = false;
                fail("CacheIdsAsDepIdTest1 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize + this.sObjectsOnDisk != 4000) {
                ok = false;
                fail("CacheIdsAsDepIdTest1 - sMemEntriesSize=" + this.sMemEntriesSize + " sObjectsOnDisk=" + this.sObjectsOnDisk + " expected total= 4000");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 40) {
                ok = false;
                fail("CacheIdsAsDepIdTest1 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=40");
            }
            if (this.sTemplatesOnDisk != 4) {
                ok = false;
                fail("CacheIdsAsDepIdTest1 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=4");
            }
        }
    }

    // Use cache id as dep id (idd=true)
    // 10 threads
    // 8 threads with timeout (put)
    // 1 threads dep id invalidation (put)
    // 1 thread template invalidation (put)
    @Test
    public void testCacheIdAsDepIdTest2() throws Exception {
        if (ok && (runTest.equalsIgnoreCase("all") || runTest.equalsIgnoreCase("testCacheIdAsDepIdTest2"))) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("CacheIdAsDepIdTest2...");
            if (htodPerf == CacheConfig.HIGH) {
                uri = URI
                      + "?threadParms=1_1000_1_15_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_35_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_40_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_45_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_50_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&idd=true&delayAT=100000&clearBT=true&exps=0&expd=-1&savCount=true&method=test1";
            } else if (htodPerf == CacheConfig.CUSTOM) {
                uri = URI
                      + "?threadParms=1_1000_1_15_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_35_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_40_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_45_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_50_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&idd=true&delayAT=400000&clearBT=true&exps=0&expd=-1&savCount=true&method=test1";
            }
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("CacheIdsAsDepIdTest2 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest2 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=0");
            }
            if (this.sDiskEntriesSize != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest2 - received sDiskEntriesSize=" + this.sDiskEntriesSize + " expected=0");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest2 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=0");
            }
            if (this.sTemplatesOnDisk != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest2 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=0");
            }
            if (this.sTotalCacheDataDiskSize != MIN_DISK_DATA_SIZE) {
                ok = false;
                fail("CacheIdsAsDepIdTest2 - received sTotalCacheDataDiskSize=" + this.sTotalCacheDataDiskSize + " expected=" + MIN_DISK_DATA_SIZE);
            }
        }
    }

    // Use cache id as dep id (idd=true)
    // 10 threads
    // 4 threads with timeout (put) - servlet cache
    // 4 threads with timeout (put) - dmap cache
    // 1 threads dep id invalidation (put) - dmap cache
    // 1 thread template invalidation (put) - servlet cache
    @Test
    public void testCacheIdAsDepIdTest3() throws Exception {
        if (ok && (runTest.equalsIgnoreCase("all") || runTest.equalsIgnoreCase("testCacheIdAsDepIdTest3"))) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("CacheIdAsDepIdTest3...");
            if (htodPerf == CacheConfig.HIGH) {
                uri = URI
                      + "?threadParms=1_1000_1_15_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_1;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_1;1_1000_1_35_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_40_1_0_0_0_0_0_0_0_0_0_0_1;1_1000_1_45_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_50_1_0_0_0_0_0_0_0_0_0_0_1;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_1;1_1000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&idd=true&delayAT=125000&clearBT=true&exps=0&expd=0&savCount=true&method=test1";
            } else if (htodPerf == CacheConfig.CUSTOM) {
                uri = URI
                      + "?threadParms=1_1000_1_15_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_1;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_1;1_1000_1_35_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_40_1_0_0_0_0_0_0_0_0_0_0_1;1_1000_1_45_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_50_1_0_0_0_0_0_0_0_0_0_0_1;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_1;1_1000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&idd=true&delayAT=500000&clearBT=true&exps=0&expd=0&savCount=true&method=test1";
            }
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                ok = false;
                fail("CacheIdsAsDepIdTest3 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=0");
            }
            if (this.sDiskEntriesSize != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received sDiskEntriesSize=" + this.sDiskEntriesSize + " expected=0");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=0");
            }
            if (this.sTemplatesOnDisk != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=0");
            }
            if (this.sTotalCacheDataDiskSize != MIN_DISK_DATA_SIZE) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received sTotalCacheDataDiskSize=" + this.sTotalCacheDataDiskSize + " expected=" + MIN_DISK_DATA_SIZE);
            }
            if (this.dMemEntriesSize != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received dMemEntriesSize=" + this.dMemEntriesSize + " expected=0");
            }
            if (this.dDiskEntriesSize != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received dDiskEntriesSize=" + this.dDiskEntriesSize + " expected=0");
            }
            if (this.dDependencyIdsInMemoryAndDisk != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received dDependencyIdsInMemoryAndDisk=" + this.dDependencyIdsInMemoryAndDisk + " expected=0");
            }
            if (this.dTemplatesOnDisk != 0) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received dTemplatesOnDisk=" + this.dTemplatesOnDisk + " expected=0");
            }
            if (this.dTotalCacheDataDiskSize != MIN_DISK_DATA_SIZE) {
                ok = false;
                fail("CacheIdsAsDepIdTest3 - received dTotalCacheDataDiskSize=" + this.dTotalCacheDataDiskSize + " expected=" + MIN_DISK_DATA_SIZE);
            }
        }
    }

    // 10 threads
    // 4 threads with timeout
    // 3 threads dep id invalidation (put, put, get)
    // 3 thread template invalidation (put, put, get)
    @Test
    public void testTimeoutInvalidation() throws Exception {
        if (ok && (runTest.equalsIgnoreCase("all") || runTest.equalsIgnoreCase("testTimeoutInvalidation"))) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("TimeoutInvalidation...");
            if (htodPerf == CacheConfig.HIGH) {
                uri = URI
                      + "?threadParms=1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_40_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_50_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_60_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_1_1_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&delayAT=80000&clearBT=true&exps=6000&expd=-1&savCount=true&method=test1";
                //uri = URI + "?threadParms=1_120_1_30_1_0_0_0_0_0_0_0_0_0_0_0;1_120_1_40_1_0_0_0_0_0_0_0_0_0_0_0;1_120_1_50_1_0_0_0_0_0_0_0_0_0_0_0;1_120_1_60_1_0_0_0_0_0_0_0_0_0_0_0;1_120_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_120_1_0_1_0_0_1_0_1_1_0_0_0_0_0;1_120_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_120_1_0_1_0_0_1_0_1_1_0_0_0_0_0;1_120_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_120_1_0_1_0_0_1_0_1_1_0_0_0_0_0;&valueSize=1000&depids=30&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&delayAT=70000&method=test1";
            } else if (htodPerf == CacheConfig.CUSTOM) {
                uri = URI
                      + "?threadParms=1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_40_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_50_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_60_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_1_1_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&delayAT=500000&exps=6000&clearBT=true&savCount=true&method=test1";
            }
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                ok = false;
                fail("TimeoutInvalidation - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sObjectsOnDisk != 6000) {
                ok = false;
                fail("TimeoutInvalidation -  sObjectsOnDisk=" + this.sObjectsOnDisk + " expected total= 6000");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 60) {
                ok = false;
                fail("TimeoutInvalidation - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=60");
            }
            if (this.sTemplatesOnDisk != 6) {
                ok = false;
                fail("TimeoutInvalidation - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=6");
            }
            if (this.sPendingRemovalFromDisk != 0) {
                ok = false;
                fail("TimeoutInvalidation - received sPendingRemovalFromDisk=" + this.sPendingRemovalFromDisk + " expected=0");
            }
            if (this.sDependencyIdsBufferedForDisk != 60) {
                ok = false;
                fail("TimeoutInvalidation - received sDependencyIdsBufferedForDisk=" + this.sDependencyIdsBufferedForDisk + " expected=60");
            }
            if (this.sDependencyIdsOffloadedToDisk != 0) {
                ok = false;
                fail("TimeoutInvalidation - received sDependencyIdsOffloadedToDisk=" + this.sDependencyIdsOffloadedToDisk + " expected=0");
            }
            if (this.sDependencyIdBasedInvalidationsFromDisk != 30) {
                ok = false;
                fail("TimeoutInvalidation - received  sDependencyIdBasedInvalidationsFromDisk=" + this.sDependencyIdBasedInvalidationsFromDisk + " expected=30");
            }
            if (htodPerf == CacheConfig.HIGH) {
                if (this.sTemplatesBufferedForDisk != 6) {
                    ok = false;
                    fail("TimeoutInvalidation - received sTemplatesBufferedForDisk=" + this.sTemplatesBufferedForDisk + " expected=6");
                }
                if (this.sTemplatesOffloadedToDisk != 0) {
                    ok = false;
                    fail("TimeoutInvalidation - received sTemplatesOffloadedToDisk=" + this.sTemplatesBufferedForDisk + " expected=0");
                }
            } else {
                if (this.sTemplatesBufferedForDisk != 3) {
                    ok = false;
                    fail("TimeoutInvalidation - received sTemplatesBufferedForDisk=" + this.sTemplatesBufferedForDisk + " expected=3");
                }
                if (this.sTemplatesOffloadedToDisk != 3) {
                    ok = false;
                    fail("TimeoutInvalidation - received sTemplatesOffloadedToDisk=" + this.sTemplatesBufferedForDisk + " expected=3");
                }
            }
            if (this.sTemplateBasedInvalidationsFromDisk != 3) {
                ok = false;
                fail("TimeoutInvalidation - received sTemplateBasedInvalidationsFromDisk=" + this.sTemplateBasedInvalidationsFromDisk + " expected=3");
            }
            if (this.sObjectsReadFromDisk != 6000) {
                ok = false;
                fail("TimeoutInvalidation - received sObjectsReadFromDisk=" + this.sObjectsReadFromDisk + " expected=6000");
            }
            if (this.sObjectsReadFromDisk40K != 6000) {
                ok = false;
                fail("TimeoutInvalidation - received sObjectsReadFromDisk40K=" + this.sObjectsReadFromDisk40K + " expected=6000");
            }
            /*
             * if (this.sObjectsWriteToDisk > 15000) {
             * ok = false;
             * fail("TimeoutInvalidation - received sObjectsWriteToDisk=" + this.sObjectsWriteToDisk + " expected > 15000");
             * }
             * if (this.sObjectsWriteToDisk40K > 15000) {
             * ok = false;
             * fail("TimeoutInvalidation - received sObjectsWriteToDisk40K=" + this.sObjectsWriteToDisk40K + " expected > 15000");
             * }
             * if (this.sObjectsDeleteFromDisk > 9900) {
             * ok = false;
             * fail("TimeoutInvalidation - received sObjectsDeleteFromDisk=" + this.sObjectsDeleteFromDisk + " expected > 9900");
             * }
             * if (this.sObjectsDeleteFromDisk40K > 9900) {
             * ok = false;
             * fail("TimeoutInvalidation - received sObjectsDeleteFromDisk40K=" + this.sObjectsDeleteFromDisk40K + " expected > 9900");
             * }
             */
        }
    }

    // 10 threads
    // 8 threads with timeout (put)
    // 1 threads dep id invalidation (put)
    // 1 thread template invalidation and clear (put)
    @Test
    public void testClearTest1() throws Exception {
        if (ok && (runTest.equalsIgnoreCase("all") || runTest.equalsIgnoreCase("testClearTest1"))) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("ClearTest1...");
            uri = URI
                  + "?threadParms=1_1000_1_15_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_35_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_40_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_45_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_50_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_1_1_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=1000&sdelay=0&adelay=0&threads=10&win=100&delayAT=0&clearBT=true&savCount=true&method=test1";
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                ok = false;
                fail("ClearTest1 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 0) {
                ok = false;
                fail("ClearTest1 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=0");
            }
            if (this.sDiskEntriesSize != 0) {
                ok = false;
                fail("ClearTest1 - received sDiskEntriesSize=" + this.sDiskEntriesSize + " expected=0");
            }
            if (this.sObjectsOnDisk != 0) {
                ok = false;
                fail("ClearTest1 -  sObjectsOnDisk=" + this.sObjectsOnDisk + " expected total= 0");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 0) {
                ok = false;
                fail("ClearTest1 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=0");
            }
            if (this.sTemplatesOnDisk != 0) {
                ok = false;
                fail("ClearTest1 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=0");
            }
            if (this.sTotalCacheDataDiskSize != MIN_DISK_DATA_SIZE) {
                ok = false;
                fail("ClearTest1 - received sTotalCacheDataDiskSize=" + this.sTotalCacheDataDiskSize + " expected=" + MIN_DISK_DATA_SIZE);
            }
        }
    }

    // Invalidation listener is enabled (idd=false)
    // 6 threads
    // 3 threads with timeout (put)
    // 1 threads dep id invalidation (put, inv, put, get)
    // 1 thread template invalidation (put, inv, put, get)
    // 1 thread (put, clear)
    public void testIListenerTest1() throws Exception {
        if (ok && (runTest.equalsIgnoreCase("all") || runTest.equalsIgnoreCase("testIListenerTest1"))) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("IListenerTest1...");
            if (htodPerf == CacheConfig.HIGH) {
                //uri = URI + "?threadParms=1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_0_1_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=80000&sdelay=0&adelay=0&threads=6&win=100&ilistener=true&delayAT=2000&exps=0&exps=0&expd=-1&savCount=true&method=test1";
                uri = URI
                      + "?threadParms=1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_0_1_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=80000&sdelay=0&adelay=0&threads=6&win=100&ilistener=true&delayAT=2000&exps=0&exps=0&expd=-1&savCount=true&method=test1";
            } else if (htodPerf == CacheConfig.CUSTOM) {
                uri = URI
                      + "?threadParms=1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_1_1_0_0_0_0_0;1_1000_1_0_1_0_0_0_1_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=180000&sdelay=0&adelay=0&threads=6&win=100&ilistener=true&delayAT=2000&exps=0&expd=-1&savCount=true&method=test1";
            }
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("IListenerTest1 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 0) {
                ok = false;
                fail("IListenerTest1 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=0");
            }
            if (this.sDiskEntriesSize != 0) {
                ok = false;
                fail("IListenerTest1 - received sDiskEntriesSize=" + this.sDiskEntriesSize + " expected=0");
            }
            if (this.sObjectsOnDisk != 0) {
                ok = false;
                fail("IListenerTest1 -  sObjectsOnDisk=" + this.sObjectsOnDisk + " expected total= 0");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 0) {
                ok = false;
                fail("IListenerTest1 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=0");
            }
            if (this.sTemplatesOnDisk != 0) {
                ok = false;
                fail("IListenerTest1 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=0");
            }
            if (this.sTotalCacheDataDiskSize != MIN_DISK_DATA_SIZE) {
                ok = false;
                fail("IListenerTest1 - received sTotalCacheDataDiskSize=" + this.sTotalCacheDataDiskSize + " expected=" + MIN_DISK_DATA_SIZE);
            }
            if (this.sDependencyIdsBufferedForDisk != 0) {
                ok = false;
                fail("IListenerTest1 - received sDependencyIdsBufferedForDisk=" + this.sDependencyIdsBufferedForDisk + " expected=0");
            }
            if (this.sDependencyIdsOffloadedToDisk != 0) {
                ok = false;
                fail("IListenerTest1 - received sDependencyIdsOffloadedToDisk=" + this.sDependencyIdsOffloadedToDisk + " expected=0");
            }
            if (this.sDependencyIdBasedInvalidationsFromDisk != 10) {
                ok = false;
                fail("IListenerTest1 - received  sDependencyIdBasedInvalidationsFromDisk=" + this.sDependencyIdBasedInvalidationsFromDisk + " expected=10");
            }
            if (this.sTemplatesBufferedForDisk != 0) {
                ok = false;
                fail("IListenerTest1 - received sTemplatesBufferedForDisk=" + this.sTemplatesBufferedForDisk + " expected=0");
            }
            if (htodPerf == CacheConfig.HIGH) {
                if (this.sTemplatesOffloadedToDisk != 0) {
                    ok = false;
                    fail("IListenerTest1 - received sTemplatesOffloadedToDisk=" + this.sTemplatesBufferedForDisk + " expected=0");
                }
            } else {
                if (this.sTemplatesOffloadedToDisk != 1) {
                    ok = false;
                    fail("IListenerTest1 - received sTemplatesOffloadedToDisk=" + this.sTemplatesBufferedForDisk + " expected=1");
                }
            }
            if (this.sTemplateBasedInvalidationsFromDisk != 1) {
                ok = false;
                fail("IListenerTest1 - received sTemplateBasedInvalidationsFromDisk=" + this.sTemplateBasedInvalidationsFromDisk + " expected=1");
            }
            if (this.sObjectsReadFromDisk != 2000) {
                ok = false;
                fail("IListenerTest1 - received sObjectsReadFromDisk=" + this.sObjectsReadFromDisk + " expected=2000");
            }
            if (this.sObjectsReadFromDisk40K != 2000) {
                ok = false;
                fail("IListenerTest1 - received sObjectsReadFromDisk40K=" + this.sObjectsReadFromDisk40K + " expected=2000");
            }
            if (this.sObjectsWriteToDisk < 7900 || this.sObjectsWriteToDisk > 8000) {
                ok = false;
                fail("IListenerTest1 - received sObjectsWriteToDisk=" + this.sObjectsWriteToDisk + " expected=7900-8000");
            }
            if (this.sObjectsWriteToDisk40K < 7900 || this.sObjectsWriteToDisk40K > 8000) {
                ok = false;
                fail("IListenerTest1 - received sObjectsWriteToDisk40K=" + this.sObjectsWriteToDisk40K + " expected=7900-8000");
            }
            if (this.sInvalidationLocal != 5001) {
                ok = false;
                fail("IListenerTest1 - received sInvalidationLocal=" + this.sInvalidationLocal + " expected=5001");
            }
            if (this.sInvalidationLocalExplicit != 2000) {
                ok = false;
                fail("IListenerTest1 - received sInvalidationLocalExplicit=" + this.sInvalidationLocalExplicit + " expected=2000");
            }
            if (this.sInvalidationLocalLRU != 0) {
                ok = false;
                fail("IListenerTest1 - received sInvalidationLocalLRU=" + this.sInvalidationLocalLRU + " expected=0");
            }
            if (this.sInvalidationLocalTimeout != 0) {
                ok = false;
                fail("IListenerTest1 - received sInvalidationLocalTimeout=" + this.sInvalidationLocalTimeout + " expected=0");
            }
            if (this.sInvalidationLocalDiskTimeout != 3000) {
                ok = false;
                fail("IListenerTest1 - received sInvalidationLocalDiskTimeout=" + this.sInvalidationLocalDiskTimeout + " expected=3000");
            }
            if (this.sInvalidationLocalClearAll != 1) {
                ok = false;
                fail("IListenerTest1 - received sInvalidationLocalClearAll=" + this.sInvalidationLocalClearAll + " expected=1");
            }
        }
    }

    // Invalidation listener is enabled (idd=true)
    // 5 threads
    // 3 threads with timeout (put)
    // 1 threads dep id invalidation (put, inv)
    // 1 thread template invalidation (put, inv)
    @Test
    public void testIListenerTest2() throws Exception {
        if (ok && (runTest.equalsIgnoreCase("all") || runTest.equalsIgnoreCase("testIListenerTest2"))) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("IListenerTest2...");
            if (htodPerf == CacheConfig.HIGH) {
                uri = URI
                      + "?threadParms=1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=5&win=100&ilistener=true&delayAT=70000&exps=0&exps=0&expd=-1&idd=true&savCount=true&method=test1";
            } else if (htodPerf == CacheConfig.CUSTOM) {
                uri = URI
                      + "?threadParms=1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_25_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_1000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;1_1000_1_30_1_0_0_0_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=5&win=100&ilistener=true&delayAT=600000&exps=0&expd=-1&idd=true&savCount=true&method=test1";
            }
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                ok = false;
                fail("IListenerTest2 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 0) {
                ok = false;
                fail("IListenerTest2 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=0");
            }
            if (this.sDiskEntriesSize != 0) {
                ok = false;
                fail("IListenerTest2 - received sDiskEntriesSize=" + this.sDiskEntriesSize + " expected=0");
            }
            if (this.sObjectsOnDisk != 0) {
                ok = false;
                fail("IListenerTest2 -  sObjectsOnDisk=" + this.sObjectsOnDisk + " expected total= 0");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 0) {
                ok = false;
                fail("IListenerTest2 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=0");
            }
            if (this.sTemplatesOnDisk != 0) {
                ok = false;
                fail("IListenerTest2 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=0");
            }
            if (this.sTotalCacheDataDiskSize != MIN_DISK_DATA_SIZE) {
                ok = false;
                fail("IListenerTest2 - received sTotalCacheDataDiskSize=" + this.sTotalCacheDataDiskSize + " expected=" + MIN_DISK_DATA_SIZE);
            }
            if (this.sDependencyIdsBufferedForDisk != 0) {
                ok = false;
                fail("IListenerTest2 - received sDependencyIdsBufferedForDisk=" + this.sDependencyIdsBufferedForDisk + " expected=0");
            }
            if (this.sDependencyIdsOffloadedToDisk != 0) {
                ok = false;
                fail("IListenerTest2 - received sDependencyIdsOffloadedToDisk=" + this.sDependencyIdsOffloadedToDisk + " expected=0");
            }
            if (this.sDependencyIdBasedInvalidationsFromDisk < 39 || this.sDependencyIdBasedInvalidationsFromDisk > 40) {
                ok = false;
                fail("IListenerTest2 - received  sDependencyIdBasedInvalidationsFromDisk=" + this.sDependencyIdBasedInvalidationsFromDisk + " expected=39-40");
            }
            if (this.sTemplatesBufferedForDisk != 0) {
                ok = false;
                fail("IListenerTest2 - received sTemplatesBufferedForDisk=" + this.sTemplatesBufferedForDisk + " expected=0");
            }
            if (this.sTemplatesOffloadedToDisk != 0) {
                ok = false;
                fail("IListenerTest2 - received sTemplatesOffloadedToDisk=" + this.sTemplatesBufferedForDisk + " expected=0");
            }
            if (this.sTemplateBasedInvalidationsFromDisk != 1) {
                ok = false;
                fail("IListenerTest2 - received sTemplateBasedInvalidationsFromDisk=" + this.sTemplateBasedInvalidationsFromDisk + " expected=1");
            }
            if (this.sObjectsReadFromDisk != 0) {
                ok = false;
                fail("IListenerTest2 - received sObjectsReadFromDisk=" + this.sObjectsReadFromDisk + " expected=0");
            }
            if (this.sObjectsWriteToDisk < 4900 || this.sObjectsWriteToDisk > 5000) {
                ok = false;
                fail("IListenerTest2 - received sObjectsWriteToDisk=" + this.sObjectsWriteToDisk + " expected=4900-5000");
            }
            if (this.sObjectsWriteToDisk40K < 4900 || this.sObjectsWriteToDisk40K > 5000) {
                ok = false;
                fail("IListenerTest2 - received sObjectsWriteToDisk40K=" + this.sObjectsWriteToDisk40K + " expected=4900-5000");
            }
            if (this.sInvalidationLocal != 5000) {
                ok = false;
                fail("IListenerTest2 - received sInvalidationLocal=" + this.sInvalidationLocal + " expected=5000");
            }
            if (this.sInvalidationLocalExplicit != 2000) {
                ok = false;
                fail("IListenerTest2 - received sInvalidationLocalExplicit=" + this.sInvalidationLocalExplicit + " expected=2000");
            }
            if (this.sInvalidationLocalLRU != 0) {
                ok = false;
                fail("IListenerTest2 - received sInvalidationLocalLRU=" + this.sInvalidationLocalLRU + " expected=0");
            }
            if (sInvalidationLocalTimeout + this.sInvalidationLocalDiskTimeout != 3000) {
                ok = false;
                fail("IListenerTest2 - received sInvalidationLocalTimeout + sInvalidationLocalDiskTimeout=" + (this.sInvalidationLocalTimeout + this.sInvalidationLocalDiskTimeout)
                     + " expected=3000");
            }
            if (this.sInvalidationLocalClearAll != 0) {
                ok = false;
                fail("IListenerTest2 - received sInvalidationLocalClearAll=" + this.sInvalidationLocalClearAll + " expected=0");
            }
        }
    }

    // FlushToDiskOnStop
    // 10 threads
    // 8 threads with timeout (put)
    // 1 threads dep id invalidation (put, inv)
    // 1 thread template invalidation (put, inv)
    @Test
    public void testFlushToDiskOnStop_1() throws Exception {
        if (runTest.equalsIgnoreCase("testFlushToDiskOnStop_1")) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("FlushToDiskOnStop_1...");
            //uri = URI + "?threadParms=1_2000_1_60_1_0_0_0_0_0_0_1_0_0_0_0;1_2000_1_70_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_80_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_90_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_100_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_110_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_120_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_130_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_2000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=200&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=2000&adelay=0&threads=10&win=100&delayAT=0&savCount=true&method=test1";
            //uri = URI + "?threadParms=1_1000_1_60_1_0_0_0_0_0_0_1_0_0_0_0;1_1000_1_70_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_80_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_90_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_100_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_110_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_120_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_130_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_2000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=200&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=2000&adelay=0&threads=10&win=100&delayAT=0&savCount=true&method=test1";
            //uri = URI + "?threadParms=1_1000_1_100_1_0_0_0_0_0_0_1_0_0_0_0;1_1000_1_110_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_300_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_310_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_320_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_330_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_340_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_350_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_2000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=200&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=1000&adelay=0&threads=10&win=100&delayAT=0&clearBT=true&savCount=true&method=test1";
            uri = URI
                  + "?threadParms=1_1000_1_100_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_120_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_300_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_310_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_320_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_330_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_340_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_350_1_0_0_0_0_0_0_0_0_0_0_0;1_3000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_3000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=200&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=1000&adelay=0&threads=10&win=100&delayAT=0&clearBT=true&savCount=true&method=test1";
            //uri = URI + "?threadParms=1_500_1_120_1_0_0_0_0_0_0_1_0_0_0_0;1_500_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_500_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=1000&adelay=0&threads=3&win=100&delayAT=0&savCount=true&method=test1";
            //uri = URI + "?threadParms=1_100_1_120_1_0_0_0_0_0_0_1_0_0_0_0;1_100_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_100_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=1000&adelay=0&threads=3&win=100&delayAT=0&savCount=true&method=test1";
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("FlushToDiskOnStop_1 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
        }
    }

    // FlushToDiskOnStop
    // idd= true
    // 10 threads
    // 8 threads with timeout (put)
    // 1 threads dep id invalidation (put, inv)
    // 1 thread template invalidation (put, inv)
    @Test
    public void testFlushToDiskOnStop_2() throws Exception {
        if (runTest.equalsIgnoreCase("testFlushToDiskOnStop_2")) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("FlushToDiskOnStop_2...");
            uri = URI
                  + "?threadParms=1_1000_1_100_1_0_0_0_0_0_0_1_0_0_0_0;1_1000_1_110_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_300_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_310_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_320_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_330_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_340_1_0_0_0_0_0_0_0_0_0_0_0;1_1000_1_350_1_0_0_0_0_0_0_0_0_0_0_0;1_2000_1_0_1_0_1_0_0_0_0_0_0_0_0_0;1_2000_1_0_1_0_0_1_0_0_0_0_0_0_0_0;&valueSize=1000&depids=200&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=1000&adelay=0&threads=10&win=100&delayAT=0&idd=true&clearBT=true&savCount=true&method=test1";
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("FlushToDiskOnStop_2 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
        }
    }

    public void testAfterFlushToDiskOnStop() throws Exception {
        if (runTest.equalsIgnoreCase("testAfterFlushToDiskOnStop")) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("AfterFlushToDiskOnStop...");
            uri = URI + "?&clearBT=false&delayAT=0&threads=0&savCount=false&method=test1";
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("AfterFlushToDiskOnStop - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
        }
    }

    // set com.ibm.ws.cache.CacheConfig.diskCacheSize = 300
    @Test
    public void testDiskCacheSize() throws Exception {
        if (runTest.equalsIgnoreCase("testDiskCacheSize")) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("DiskCacheSize...");
            uri = URI
                  + "?threadParms=1_1500_1_0_1_0_0_0_0_0_0_0_0_0_0_0;&valueSize=1000&depids=100&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&idd=false&clearBT=true&delayAT=0&exps=-1&expd=-1&savCount=true&method=test1";
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                ok = false;
                fail("DiskCacheSizeTest - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 100) {
                ok = false;
                fail("DiskCacheSizeTest - sMemEntriesSize=" + this.sMemEntriesSize + " expected total= 100");
            }
            if (this.sObjectsOnDisk != 300) {
                ok = false;
                fail("DiskCacheSizeTest -  sObjectsOnDisk=" + this.sObjectsOnDisk + " expected= 300");
            }
        }
    }

    // PushPull Mode Testing
    // 10 threads
    // 1 thread (1000 put) no timeout 
    // 4 threads with timeout (500 put)
    // 3 threads dep id invalidation (500 put, inv)
    // 2 thread template invalidation (500 put, inv)
    @Test
    public void testPushPullInvalidation1() throws Exception {
        if (runTest.equalsIgnoreCase("testPushPullInvalidation1")) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();

            System.out.println("PushPullInvalidation1.1...");
            uri = URI + "?&clearBT=false&threads=0&resetCount=true&savCount=false&delayAT=0&method=test1";
            resp = getCloneWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("PushPullInvalidation1.1 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            System.out.println("PushPullInvalidation1.2...");
            uri = URI + "?&clearBT=true&threads=0&resetCount=true&savCount=false&delayAT=0&method=test1";
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            //System.out.println(s);
            sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("PushPullInvalidation1.2 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 0) {
                ok = false;
                fail("PushPullInvalidation1.2 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=0");
            }
            if (this.sObjectsOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation1.2 - received sObjectsOnDisk=" + this.sObjectsOnDisk + " expected=0");
            }
            if (this.sDependencyIdsOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation1.2 - received sDependencyIdsOnDisk=" + this.sDependencyIdsOnDisk + " expected=0");
            }
            if (this.sTemplatesOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation1.2 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=0");
            }
            if (this.sPushPullTableSize != 0) {
                ok = false;
                fail("PushPullInvalidation1.2 - received sPushPullTableSize=" + this.sPushPullTableSize + " expected=0");
            }
            System.out.println("PushPullInvalidation1.3...");
            if (htodPerf == CacheConfig.HIGH) {
                uri = URI
                      + "?threadParms=1_1000_4_0_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_120_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_130_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_140_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_150_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;&valueSize=500&depids=100&delay=80&ddelay=5000&tdelay=2000&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&delayAT=120000&exps=900&expd=-1&clearBT=false&idName=Cache1:9081&savCount=true&method=test1";
            } else if (htodPerf == CacheConfig.CUSTOM) {
                uri = URI
                      + "?threadParms=1_1000_4_0_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_120_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_130_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_140_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_150_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;&valueSize=500&depids=100&delay=80&ddelay=5000&tdelay=2000&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&delayAT=240000&exps=900&expd=-1&clearBT=false&idName=Cache1:9081&savCount=true&method=test1";
            }
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            //System.out.println(s);
            sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                ok = false;
                fail("PushPullInvalidation1.3 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 100) {
                ok = false;
                fail("PushPullInvalidation1.3 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=100");
            }
            if (this.sObjectsOnDisk != 900) {
                ok = false;
                fail("PushPullInvalidation1.3 - received sObjectsOnDisk=" + this.sObjectsOnDisk + " expected=900");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 10) {
                ok = false;
                fail("PushPullInvalidation1.3 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=10");
            }
            if (this.sTemplatesOnDisk != 1) {
                ok = false;
                fail("PushPullInvalidation1.3 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=1");
            }
            if (this.sPushPullTableSize != 0) {
                ok = false;
                fail("PushPullInvalidation1.3 - received sPushPullTableSize=" + this.sPushPullTableSize + " expected=0");
            }
            if ((sTimeoutInvalidationsFromMemory + this.sTimeoutInvalidationsFromDisk) != (500 * 4)) {
                ok = false;
                fail("PushPullInvalidation1.3 - received sTimeoutInvalidationsFromMemory + sTimeoutInvalidationsFromDisk="
                     + (this.sTimeoutInvalidationsFromMemory + this.sTimeoutInvalidationsFromDisk) + " expected=" + (500 * 4));
            }
            if ((sExplicitInvalidationsFromMemory + this.sExplicitInvalidationsFromDisk) != (500 * 5)) {
                ok = false;
                fail("PushPullInvalidation1.3 - received sExplicitInvalidationsFromMemory + sExplicitInvalidationsFromDisk="
                     + (this.sExplicitInvalidationsFromMemory + this.sExplicitInvalidationsFromDisk) + " expected=" + (500 * 5));
            }
            if (this.sDependencyIdBasedInvalidationsFromDisk != (5 * 3)) {
                ok = false;
                fail("PushPullInvalidation1.3 - received sDependencyIdBasedInvalidationsFromDisk=" + this.sDependencyIdBasedInvalidationsFromDisk + " expected=" + (5 * 3));
            }
            if (this.sTemplateBasedInvalidationsFromDisk != 2) {
                ok = false;
                fail("PushPullInvalidation1.3 - received sTemplateBasedInvalidationsFromDisk=" + this.sTemplateBasedInvalidationsFromDisk + " expected=2");
            }

            System.out.println("PushPullInvalidation1.4...");
            uri = URI + "?&clearBT=false&threads=0&savCount=false&delayAT=30000&expps=1000&method=test1";
            resp = getCloneWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            //System.out.println(s);
            sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("PushPullInvalidation1.4 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 0) {
                ok = false;
                fail("PushPullInvalidation1.4 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=0");
            }
            if (this.sObjectsOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation1.4 - received sObjectsOnDisk=" + this.sObjectsOnDisk + " expected=0");
            }
            if (this.sDependencyIdsOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation1.4 - received sDependencyIdsOnDisk=" + this.sDependencyIdsOnDisk + " expected=0");
            }
            if (this.sTemplatesOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation1.4 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=0");
            }
            if (this.sPushPullTableSize != 1000) {
                ok = false;
                fail("PushPullInvalidation1.4 - received sPushPullTableSize=" + this.sPushPullTableSize + " expected=1000");
            }
            if (this.sRemoteObjectHits != 0) {
                ok = false;
                fail("PushPullInvalidation1.4 - received sRemoteObjectHits=" + this.sRemoteObjectHits + " expected=0");
            }
            if (this.sRemoteUpdateNotifications != (1000 + 500 * 9)) {
                ok = false;
                fail("PushPullInvalidation1.4 - received sRemoteUpdateNotifications=" + this.sRemoteUpdateNotifications + " expected=" + (1000 + 500 * 9));
            }
            // 2 (clear) + 1000 + 5000*9 + 5 (depId/thread)*3 + 1 (template/thread) * 2 + 5000*9
            if (this.sRemoteInvalidationNotifications != (2 + 1000 + 500 * 9 + 5 * 3 + 2 + 500 * 9)) {
                ok = false;
                fail("PushPullInvalidation1.4 - received sRemoteInvalidationNotifications=" + this.sRemoteInvalidationNotifications + " expected="
                     + (2 + 1000 + 500 * 9 + 5 * 3 + 2 + 500 * 9));
            }
            System.out.println("PushPullInvalidation1.5...");
            uri = URI
                  + "?threadParms=1_1000_4_0_0_1_0_0_0_0_0_0_0_0_0_0;&valueSize=500&depids=100&delay=80&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&clearBT=false&delayAT=1000&threads=1&idName=Cache1:9081&savCount=true&method=test1";
            resp = getCloneWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            //System.out.println(s);
            sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("PushPullInvalidation1.5 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 100) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=100");
            }
            if (this.sObjectsOnDisk != 900) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sObjectsOnDisk=" + this.sObjectsOnDisk + " expected=900");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 10) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=10");
            }
            if (this.sTemplatesOnDisk != 1) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=1");
            }
            if (this.sCacheHits != 1000) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sCacheHits=" + this.sCacheHits + " expected=1000");
            }
            if (this.sCacheMisses != 0) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sCacheMisses=" + this.sCacheMisses + " expected=0");
            }
            if (this.sObjectsWriteToDisk != 900) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sObjectsWriteToDisk=" + this.sObjectsWriteToDisk + " expected=900");
            }
            if (this.sObjectsWriteToDiskSize != (900 * 4027)) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sObjectsWriteToDiskSize=" + this.sObjectsWriteToDiskSize + " expected=" + (900 * 4027));
            }
            if (this.sRemoteObjectMisses != 0) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sRemoteObjectMisses=" + this.sRemoteObjectMisses + " expected=0");
            }
            if (this.sRemoteObjectHits != 1000) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sRemoteObjectHits=" + this.sRemoteObjectHits + " expected=1000");
            }
            if (this.sRemoteObjectFetchSize != (1000 * 4027)) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sRemoteObjectFetchSize=" + this.sRemoteObjectFetchSize + " expected=" + (1000 * 4027));
            }
            if (this.sPushPullTableSize != 0) {
                ok = false;
                fail("PushPullInvalidation1.5 - received sPushPullTableSize=" + this.sPushPullTableSize + " expected=0");
            }
        }
    }

    // PushPull Mode Testing
    // idd= true
    // 10 threads
    // 1 thread (1000 put) no timeout 
    // 4 threads with timeout (500 put)
    // 3 threads dep id invalidation (500 put, inv)
    // 2 thread template invalidation (500 put, inv)
    @Test
    public void testPushPullInvalidation2() throws Exception {
        if (runTest.equalsIgnoreCase("testPushPullInvalidation2")) {
            WebResponse resp = null;
            WebConversation wc = startNewConversation();

            System.out.println("PushPullInvalidation2.1...");
            uri = URI + "?&clearBT=false&threads=0&resetCount=true&savCount=false&delayAT=0&method=test1";
            resp = getCloneWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("PushPullInvalidation2.1 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            System.out.println("PushPullInvalidation2.2...");
            uri = URI + "?&clearBT=true&threads=0&resetCount=true&savCount=false&delayAT=0&method=test1";
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            //System.out.println(s);
            sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("PushPullInvalidation2.2 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();
            if (this.sMemEntriesSize != 0) {
                ok = false;
                fail("PushPullInvalidation2.2 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=0");
            }
            if (this.sObjectsOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation2.2 - received sObjectsOnDisk=" + this.sObjectsOnDisk + " expected=0");
            }
            if (this.sDependencyIdsOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation2.2 - received sDependencyIdsOnDisk=" + this.sDependencyIdsOnDisk + " expected=0");
            }
            if (this.sTemplatesOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation2.2 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=0");
            }
            if (this.sPushPullTableSize != 0) {
                ok = false;
                fail("PushPullInvalidation2.2 - received sPushPullTableSize=" + this.sPushPullTableSize + " expected=0");
            }
            System.out.println("PushPullInvalidation2.3...");
            if (htodPerf == CacheConfig.HIGH) {
                //uri = URI + "?threadParms=1_300_4_0_1_0_0_0_0_0_0_0_0_0_0_0;1_100_4_120_1_0_0_0_0_0_0_0_0_0_0_0;1_100_4_130_1_0_0_0_0_0_0_0_0_0_0_0;1_100_4_140_1_0_0_0_0_0_0_0_0_0_0_0;1_100_4_150_1_0_0_0_0_0_0_0_0_0_0_0;1_100_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_100_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_100_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_100_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_100_4_0_1_0_1_0_0_0_0_0_0_0_0_0;&valueSize=500&depids=10&delay=80&ddelay=5000&tdelay=2000&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&delayAT=100000&exps=200&expd=-1&clearBT=false&idName=Cache1:9081&savCount=true&idd=true&method=test1";
                uri = URI
                      + "?threadParms=1_1000_4_0_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_120_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_130_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_140_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_150_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;&valueSize=500&depids=100&delay=80&ddelay=5000&tdelay=2000&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&delayAT=120000&exps=900&expd=-1&clearBT=false&idName=Cache1:9081&savCount=true&idd=true&method=test1";
            } else if (htodPerf == CacheConfig.CUSTOM) {
                uri = URI
                      + "?threadParms=1_1000_4_0_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_120_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_130_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_140_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_150_1_0_0_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;1_500_4_0_1_0_0_1_0_0_0_0_0_0_0_0;1_500_4_0_1_0_1_0_0_0_0_0_0_0_0_0;&valueSize=500&depids=100&delay=80&ddelay=5000&tdelay=2000&cdelay=0&sdelay=0&adelay=0&threads=10&win=100&delayAT=240000&exps=900&expd=-1&clearBT=false&idName=Cache1:9081&savCount=true&idd=true&method=test1";
            }
            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            //System.out.println(s);
            sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                ok = false;
                fail("PushPullInvalidation2.3 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();

            if (this.sMemEntriesSize != 100) {
                ok = false;
                fail("PushPullInvalidation2.3 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=100");
            }
            if (this.sObjectsOnDisk != 900) {
                ok = false;
                fail("PushPullInvalidation2.3 - received sObjectsOnDisk=" + this.sObjectsOnDisk + " expected=900");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 10) {
                ok = false;
                fail("PushPullInvalidation2.3 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=10");
            }
            if (this.sTemplatesOnDisk != 1) {
                ok = false;
                fail("PushPullInvalidation2.3 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=1");
            }
            if (this.sPushPullTableSize != 0) {
                ok = false;
                fail("PushPullInvalidation2.3 - received sPushPullTableSize=" + this.sPushPullTableSize + " expected=0");
            }
            if ((sTimeoutInvalidationsFromMemory + this.sTimeoutInvalidationsFromDisk) != (500 * 4)) {
                ok = false;
                fail("PushPullInvalidation2.3 - received sTimeoutInvalidationsFromMemory + sTimeoutInvalidationsFromDisk="
                     + (this.sTimeoutInvalidationsFromMemory + this.sTimeoutInvalidationsFromDisk) + " expected=" + (500 * 4));
            }
            if ((sExplicitInvalidationsFromMemory + this.sExplicitInvalidationsFromDisk) != (500 * 5)) {
                ok = false;
                fail("PushPullInvalidation2.3 - received sExplicitInvalidationsFromMemory + sExplicitInvalidationsFromDisk="
                     + (this.sExplicitInvalidationsFromMemory + this.sExplicitInvalidationsFromDisk) + " expected=" + (500 * 5));
            }
            if (this.sDependencyIdBasedInvalidationsFromDisk != (5 * 7)) {
                ok = false;
                fail("PushPullInvalidation2.3 - received sDependencyIdBasedInvalidationsFromDisk=" + this.sDependencyIdBasedInvalidationsFromDisk + " expected=" + (5 * 7));
            }
            if (this.sTemplateBasedInvalidationsFromDisk != 2) {
                ok = false;
                fail("PushPullInvalidation2.3 - received sTemplateBasedInvalidationsFromDisk=" + this.sTemplateBasedInvalidationsFromDisk + " expected=2");
            }

            System.out.println("PushPullInvalidation2.4...");
            uri = URI + "?&clearBT=false&threads=0&savCount=false&delayAT=30000&expps=1000&method=test1";
            resp = getCloneWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            //System.out.println(s);
            sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("PushPullInvalidation2.4 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();

            if (this.sMemEntriesSize != 0) {
                ok = false;
                fail("PushPullInvalidation2.4 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=0");
            }
            if (this.sObjectsOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation2.4 - received sObjectsOnDisk=" + this.sObjectsOnDisk + " expected=0");
            }
            if (this.sDependencyIdsOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation2.4 - received sDependencyIdsOnDisk=" + this.sDependencyIdsOnDisk + " expected=0");
            }
            if (this.sTemplatesOnDisk != 0) {
                ok = false;
                fail("PushPullInvalidation2.4 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=0");
            }

            if (this.sPushPullTableSize != 1000) {
                ok = false;
                fail("PushPullInvalidation2.4 - received sPushPullTableSize=" + this.sPushPullTableSize + " expected=1000");
            }
            if (this.sRemoteObjectHits != 0) {
                ok = false;
                fail("PushPullInvalidation2.4 - received sRemoteObjectHits=" + this.sRemoteObjectHits + " expected=0");
            }
            if (this.sRemoteUpdateNotifications != (1000 + 500 * 9)) {
                ok = false;
                fail("PushPullInvalidation2.4 - received sRemoteUpdateNotifications=" + this.sRemoteUpdateNotifications + " expected=" + (1000 + 500 * 9));
            }
            // 2 (clear) + 1000 + 5000*9 + 500*9 + 2 (template)
            if (this.sRemoteInvalidationNotifications != (2 + 1000 + 500 * 9 + 2 + 500 * 9)) {
                ok = false;
                fail("PushPullInvalidation2.4 - received sRemoteInvalidationNotifications=" + this.sRemoteInvalidationNotifications + " expected="
                     + (2 + 1000 + 500 * 9 + 2 + 500 * 9));
            }
            System.out.println("PushPullInvalidation2.5...");
            uri = URI
                  + "?threadParms=1_1000_4_0_0_1_0_0_0_0_0_0_0_0_0_0;&valueSize=500&depids=100&delay=80&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&clearBT=false&delayAT=1000&threads=1&idName=Cache1:9081&savCount=true&method=test1";
            //uri = URI + "?threadParms=1_300_4_0_0_1_0_0_0_0_0_0_0_0_0_0;&valueSize=500&depids=100&delay=80&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&clearBT=false&delayAT=1000&threads=1&idName=Cache1:9081&savCount=true&method=test1";
            resp = getCloneWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            s = resp.getText();
            //System.out.println(s);
            sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                fail("PushPullInvalidation2.5 - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }
            parse();

            if (this.sMemEntriesSize != 100) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=100");
            }
            if (this.sObjectsOnDisk != 900) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sObjectsOnDisk=" + this.sObjectsOnDisk + " expected=900");
            }
            if (this.sDependencyIdsInMemoryAndDisk != 10) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk + " expected=10");
            }
            if (this.sTemplatesOnDisk != 1) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=1");
            }
            if (this.sCacheHits != 1000) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sCacheHits=" + this.sCacheHits + " expected=1000");
            }
            if (this.sCacheMisses != 0) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sCacheMisses=" + this.sCacheMisses + " expected=0");
            }
            if (this.sObjectsWriteToDisk != 900) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sObjectsWriteToDisk=" + this.sObjectsWriteToDisk + " expected=900");
            }
            if (this.sObjectsWriteToDiskSize != (900 * 4027)) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sObjectsWriteToDiskSize=" + this.sObjectsWriteToDiskSize + " expected=" + (900 * 4027));
            }
            if (this.sRemoteObjectMisses != 0) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sRemoteObjectMisses=" + this.sRemoteObjectMisses + " expected=0");
            }
            if (this.sRemoteObjectHits != 1000) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sRemoteObjectHits=" + this.sRemoteObjectHits + " expected=1000");
            }
            if (this.sRemoteObjectFetchSize != (1000 * 4027)) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sRemoteObjectFetchSize=" + this.sRemoteObjectFetchSize + " expected=" + (1000 * 4027));
            }
            if (this.sPushPullTableSize != 0) {
                ok = false;
                fail("PushPullInvalidation2.5 - received sPushPullTableSize=" + this.sPushPullTableSize + " expected=0");
            }
        }
    }

    // Test MBean statistics
    // Testing ExplicitInvalidationsFromMemory,  ExplicitInvalidationsFromDisk, ExplicitInvalidationsLocal
    // idd= true
    // valuesize = 20
    // Request1= 2 threads  thread-1 with timeout  (200 no timeout put)  thread-2 with timeout (300 put timeout)
    // Request2= 1 threads  thread-1   (300 get)   
    @Test
    public void testCacheMbeanCounters() throws Exception {

        if (ok && (runTest.equalsIgnoreCase("all") ||
            runTest.equalsIgnoreCase("testCacheMbeanCounters"))) {

            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("testCacheMbeanCounters...");

            // cachesize = 100
            // no limit on disksize
            // all operations on the services/cache/htodInstance_25
            // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D = 16  thread params
            String uri = URI + // Total items = 800
                         "?threadParms=1_500_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // no timeout (500 put)  
                         "1_300_1_30_1_0_0_0_0_0_0_0_0_0_0_0;" + // timeout (300 put) 
                         "&valueSize=20&depids=10" +
                         "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&threads=2" +
                         "&win=100&delayAT=50000&exps=-1&expd=-1&clearBT=true&resetPMI=true" +
                         "&savCount=false&idd=false" +
                         "&cacheServletName=services/cache/htodInstance_25&method=test1";
            resp = getWebResponse(wc, uri);

            //Do a get of 500 cache ids on both the threads
            uri = URI +
                  "?threadParms=1_500_1_0_0_1_0_0_0_0_0_0_0_0_0_0;" + // get 500 
                  "1_500_1_0_0_1_0_0_0_0_0_0_0_0_0_0;" + // get 500 
                  "&threads=2" +
                  "&win=100&delayAT=30000&exps=500&expd=-1&clearBT=false" +
                  "&savCount=false&idd=false" +
                  "&cacheServletName=services/cache/htodInstance_25&method=test1";
            resp = getWebResponse(wc, uri);

            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }

            parse();

            if (this.sMemEntriesSize != 100) {
                ok = false;
                fail("testCacheMbeanCounters - received sMemEntriesSize=" + this.sMemEntriesSize + " expected=100");
            }

            if (this.sDiskEntriesSize != 500) {
                ok = false;
                fail("testCacheMbeanCounters - received sDiskEntriesSize=" + this.sDiskEntriesSize + " expected=500");
            }
            if (this.sObjectsOnDisk != 500) {
                ok = false;
                fail("testCacheMbeanCounters - received sObjectsOnDisk=" + this.sObjectsOnDisk + " expected=500");
            }
            if (this.sDependencyIdsOnDisk != 50) {
                ok = false;
                fail("testCacheMbeanCounters - received sDependencyIdsOnDisk=" + this.sDependencyIdsOnDisk + " expected=50");
            }

            if (this.sTemplatesOnDisk != 1) {
                ok = false;
                fail("testCacheMbeanCounters - received sTemplatesOnDisk=" + this.sTemplatesOnDisk + " expected=1");
            }
            if (this.sCacheHits != 500) {
                ok = false;
                fail("testCacheMbeanCounters - received sCacheHits=" + this.sCacheHits + " expected=500");
            }
            if (this.sCacheMisses != 500) {
                ok = false;
                fail("testCacheMbeanCounters - received sCacheMisses=" + this.sCacheMisses + " expected=500");
            }

            //why are there 200 items on the disk ...?

            //all the nos. beyond this points seem to be inflated by 100            
            //What is sCacheRemoves and sCacheLruRemoves
            if (this.sCacheRemoves != 300) {
                ok = false;
                fail("testCacheMbeanCounters - received sCacheRemoves=" + this.sCacheRemoves + " expected=300");
            }
            long expCacheLruRemoves = 1200 - this.sTimeoutInvalidationsFromMemory;
            if (this.sCacheLruRemoves != expCacheLruRemoves) {
                ok = false;
                fail("testCacheMbeanCounters - received sCacheLruRemoves=" + this.sCacheLruRemoves + " expected=" + expCacheLruRemoves);
            }
            long expTimeoutInvalidationsFromDisk = 300 - this.sTimeoutInvalidationsFromMemory;
            if (this.sTimeoutInvalidationsFromDisk != expTimeoutInvalidationsFromDisk) {
                ok = false;
                fail("testCacheMbeanCounters - received sTimeoutInvalidationsFromDisk=" + this.sTimeoutInvalidationsFromDisk + " expected=" + expTimeoutInvalidationsFromDisk);
            }
            if (this.sDependencyIdsBufferedForDisk != 50) {
                ok = false;
                fail("testCacheMbeanCounters - received sDependencyIdsBufferedForDisk=" + this.sDependencyIdsBufferedForDisk + " expected=50");
            }
            if (this.sDependencyIdBasedInvalidationsFromDisk != 0) {
                ok = false;
                fail("testCacheMbeanCounters - received sDependencyIdBasedInvalidationsFromDisk=" + this.sDependencyIdBasedInvalidationsFromDisk + " expected=0");
            }
            if (this.sTemplatesBufferedForDisk != 1) {
                ok = false;
                fail("testCacheMbeanCounters - received sTemplatesBufferedForDisk=" + this.sTemplatesBufferedForDisk + " expected=1");
            }
            if (this.sObjectsReadFromDisk != 500) {
                ok = false;
                fail("testCacheMbeanCounters - received sObjectsReadFromDisk=" + this.sObjectsReadFromDisk + " expected=500");
            }
            if (this.sObjectsReadFromDisk4K != 500) {
                ok = false;
                fail("testCacheMbeanCounters - received sObjectsReadFromDisk4K=" + this.sObjectsReadFromDisk4K + " expected=500");
            }
            long expObjectsWriteToDisk = 800 - this.sTimeoutInvalidationsFromMemory;
            if (this.sObjectsWriteToDisk != expObjectsWriteToDisk) {
                ok = false;
                fail("testCacheMbeanCounters - received sObjectsWriteToDisk=" + this.sObjectsWriteToDisk + " expected=" + expObjectsWriteToDisk);
            }

            long expObjectsDeleteFromDisk = 300 - this.sTimeoutInvalidationsFromMemory;
            if (this.sObjectsDeleteFromDisk != expObjectsDeleteFromDisk) {
                ok = false;
                fail("testCacheMbeanCounters - received sObjectsDeleteFromDisk=" + this.sObjectsDeleteFromDisk + " expected=" + expObjectsDeleteFromDisk);
            }

        }

/*
 * Snapshot of PMI Counters after this test
 * 
 * stats name=cacheModule.template
 * HitsInMemoryCount[21]=0
 * HitsOnDiskCount[22]=500 //SOMEONE NEEDS TO EXPLAIN THIS ...THIS VALUE ACCORDING TO LOGIC = (100 mem + 300 disk)
 * ExplicitInvalidationCount[23]=0
 * LruInvalidationCount[24]=900
 * TimeoutInvalidationCount[25]=400
 * InMemoryAndDiskCacheEntryCount[26]=100 //SOMEHOW THIS IS CORRECT :-)
 * RemoteHitCount[27]=0
 * MissCount[28]=0
 * ClientRequestCount[29]=500
 * DistributedRequestCount[30]=0
 * ExplicitMemoryInvalidationCount[31]=0
 * ExplicitDiskInvalidationCount[32]=0
 * LocalExplicitInvalidationCount[34]=0
 * RemoteExplicitInvalidationCount[35]=0
 * RemoteCreationCount[36]=0
 * 
 * 
 * 
 * stats name=localhost:9081:thread:0:template
 * HitsInMemoryCount[21]=0
 * HitsOnDiskCount[22]=200
 * ExplicitInvalidationCount[23]=0
 * LruInvalidationCount[24]=400
 * TimeoutInvalidationCount[25]=0
 * InMemoryAndDiskCacheEntryCount[26]=200
 * RemoteHitCount[27]=0
 * MissCount[28]=0
 * ClientRequestCount[29]=200
 * DistributedRequestCount[30]=0
 * ExplicitMemoryInvalidationCount[31]=0
 * ExplicitDiskInvalidationCount[32]=0
 * LocalExplicitInvalidationCount[34]=0
 * RemoteExplicitInvalidationCount[35]=0
 * RemoteCreationCount[36]=0
 * 
 * 
 * 
 * stats name=localhost:9081:thread:1:template
 * HitsInMemoryCount[21]=0
 * HitsOnDiskCount[22]=300
 * ExplicitInvalidationCount[23]=0
 * LruInvalidationCount[24]=500 //WHAT THE HECK DOES THIS MEAN
 * TimeoutInvalidationCount[25]=400 //DONT UNDERSTAND
 * InMemoryAndDiskCacheEntryCount[26]=-100 //THIS SURELY IS WRONG. CAN THIS BE NEGATIVE
 * RemoteHitCount[27]=0
 * MissCount[28]=0
 * ClientRequestCount[29]=300
 * DistributedRequestCount[30]=0
 * ExplicitMemoryInvalidationCount[31]=0
 * ExplicitDiskInvalidationCount[32]=0
 * LocalExplicitInvalidationCount[34]=0
 * RemoteExplicitInvalidationCount[35]=0
 * RemoteCreationCount[36]=0
 */

    }

    // Test MBean statistics
    // ExplicitInvalidationsFromMemory,  ExplicitInvalidationsFromDisk, ExplicitInvalidationsLocal TimeoutInvalidationsFromMemory, 
    // idd= true
    // 10 threads
    // 1 thread with timeout  (1000 put)  
    // 4 threads with timeout (500 put)
    // 3 threads dep id invalidation (500 put, inv)
    // 2 thread template invalidation (500 put, inv)
    @Test
    public void testExplicitMbeanCounters() throws Exception {

        if (ok && (runTest.equalsIgnoreCase("all") ||
            runTest.equalsIgnoreCase("testExplicitMbeanCounters"))) {

            WebResponse resp = null;
            WebConversation wc = startNewConversation();
            System.out.println("testExplicitMbeanCounters...");

            // cachesize = 100
            // no limit on disksize
            // all operations on the base cache
            // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D = 16  thread params
            String uri = URI + // Total items = 5500
                         "?threadParms=1_1000_1_20_1_0_0_0_0_0_0_0_0_0_0_0;" + // timeout (1000 put) 
                         "1_500_1_20_1_0_0_0_0_0_0_0_0_0_0_0;" + // timeout (500 put) 
                         "1_500_1_30_1_0_0_0_0_0_0_0_0_0_0_0;" + // timeout (500 put)
                         "1_500_1_40_1_0_0_0_0_0_0_0_0_0_0_0;" + // timeout (500 put)
                         "1_500_1_50_1_0_0_0_0_0_0_0_0_0_0_0;" + // timeout (500 put)
                         "1_500_1_0_1_0_1_0_0_0_0_0_0_0_0_0;" + // (500 no timeout put) dep-id invalidate
                         "1_500_1_0_1_0_0_1_0_0_0_0_0_0_0_0;" + // (500 no timeout put) template invalidate
                         "1_500_1_0_1_0_1_0_0_0_0_0_0_0_0_0;" + // (500 no timeout put) dep-id invalidate
                         "1_500_1_0_1_0_0_1_0_0_0_0_0_0_0_0;" + // (500 no timeout put) template invalidate
                         "1_500_1_0_1_0_1_0_0_0_0_0_0_0_0_0;" + // (500 no timeout put) dep-id invalidate
                         "&valueSize=500&depids=100" +
                         "&delay=80&ddelay=5000&tdelay=2000&cdelay=0&sdelay=0&adelay=0&threads=10" +
                         "&win=100&delayAT=120000&exps=-1&expd=-1&clearBT=true" +
                         "&savCount=true&idd=true&method=test1";

            resp = getWebResponse(wc, uri);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
            String s = resp.getText();
            //System.out.println(s);
            int sindex = s.indexOf("error");
            if (sindex > 0) {
                System.out.println(s);
                ok = false;
                fail("testExplicitMbeanCounters - " + resp.getURL() + "\n Error occurred during the test");
            }
            sindex = s.indexOf("Result: ");
            if (sindex > 0) {
                int eindex = s.indexOf("</body>", sindex);
                result = s.substring(sindex, eindex);
            }

            parse();

            if (this.sCacheRemoves != 5500) {
                ok = false;
                fail("testExplicitMbeanCounters - received sCacheRemoves=" + this.sCacheRemoves + " expected=5500");
            }

            //Issue: What does this counter sCacheLruRemoves mean. I see 5400 here
            if (this.sCacheLruRemoves != 5400) {
                ok = false;
                fail("testExplicitMbeanCounters - received sCacheLruRemoves=" + this.sCacheLruRemoves + " expected=5400");
            }
            if (this.sExplicitInvalidationsFromDisk != 2500) {
                ok = false;
                fail("testExplicitMbeanCounters - received sExplicitInvalidationsFromDisk=" + this.sExplicitInvalidationsFromDisk + " expected=2500");
            }
            if (this.sExplicitInvalidationsLocal != 2500) {
                ok = false;
                fail("testExplicitMbeanCounters - received sExplicitInvalidationsLocal=" + this.sExplicitInvalidationsLocal + " expected=2500");
            }
            if (this.sTimeoutInvalidationsFromMemory != 100) {
                ok = false;
                fail("testExplicitMbeanCounters - received sTimeoutInvalidationsFromMemory=" + this.sTimeoutInvalidationsFromMemory + " expected=100");
            }
            if (this.sTimeoutInvalidationsFromDisk != 2900) {
                ok = false;
                fail("testExplicitMbeanCounters - received sTimeoutInvalidationsFromDisk=" + this.sTimeoutInvalidationsFromDisk + " expected=2900");
            }

            // Issue: This valus should be 15, why is this value 44 ...?
            if (10 > this.sDependencyIdBasedInvalidationsFromDisk || this.sDependencyIdBasedInvalidationsFromDisk > 20) {
                ok = false;
                fail("testExplicitMbeanCounters - received sDependencyIdBasedInvalidationsFromDisk=" + this.sDependencyIdBasedInvalidationsFromDisk + " expected=[10,20]");
            }
            if (this.sTemplateBasedInvalidationsFromDisk != 2) {
                ok = false;
                fail("testExplicitMbeanCounters - received sTemplateBasedInvalidationsFromDisk=" + this.sTemplateBasedInvalidationsFromDisk + " expected=2");
            }
            if (this.sObjectsWriteToDisk != 5400) { //for now this looks ok
                ok = false;
                fail("testExplicitMbeanCounters - received sObjectsWriteToDisk=" + this.sObjectsWriteToDisk + " expected=5400");
            }
            if (this.sObjectsDeleteFromDisk40K != 5400) { //for now this looks ok
                ok = false;
                fail("testExplicitMbeanCounters - received sObjectsDeleteFromDisk40K=" + this.sObjectsDeleteFromDisk40K + " expected=5400");
            }
        }

    }

    public void parse() {
        int sindex, eindex;
        String temp;

        this.sMemEntriesSize = 0;
        this.sDiskEntriesSize = 0;
        this.sObjectsOnDisk = 0;
        this.sDependencyIdsInMemoryAndDisk = 0;
        this.sDependencyIdsOnDisk = 0;
        this.sTemplatesOnDisk = 0;
        this.sTotalCacheDataDiskSize = 0;
        this.sCacheHits = 0;
        this.sCacheMisses = 0;
        this.sCacheRemoves = 0;
        this.sCacheLruRemoves = 0;
        this.sExplicitInvalidationsFromMemory = 0;
        this.sExplicitInvalidationsFromDisk = 0;
        this.sExplicitInvalidationsLocal = 0;
        this.sExplicitInvalidationsRemote = 0;
        this.sTimeoutInvalidationsFromMemory = 0;
        this.sTimeoutInvalidationsFromDisk = 0;
        this.sPendingRemovalFromDisk = 0;
        this.sDependencyIdsBufferedForDisk = 0;
        this.sDependencyIdsOffloadedToDisk = 0;
        this.sDependencyIdBasedInvalidationsFromDisk = 0;
        this.sTemplatesBufferedForDisk = 0;
        this.sTemplatesOffloadedToDisk = 0;
        this.sTemplateBasedInvalidationsFromDisk = 0;
        this.sObjectsReadFromDisk = 0;
        this.sObjectsReadFromDisk4K = 0;
        this.sObjectsReadFromDisk40K = 0;
        this.sObjectsReadFromDisk400K = 0;
        this.sObjectsReadFromDisk4000K = 0;
        this.sObjectsReadFromDiskSize = 0;
        this.sObjectsWriteToDisk = 0;
        this.sObjectsWriteToDisk4K = 0;
        this.sObjectsWriteToDisk40K = 0;
        this.sObjectsWriteToDisk400K = 0;
        this.sObjectsWriteToDisk4000K = 0;
        this.sObjectsWriteToDiskSize = 0;
        this.sObjectsDeleteFromDisk = 0;
        this.sObjectsDeleteFromDisk4K = 0;
        this.sObjectsDeleteFromDisk40K = 0;
        this.sObjectsDeleteFromDisk400K = 0;
        this.sObjectsDeleteFromDisk4000K = 0;
        this.sObjectsDeleteFromDiskSize = 0;
        this.sPushPullTableSize = 0;
        this.sRemoteInvalidationNotifications = 0;
        this.sRemoteUpdateNotifications = 0;
        this.sRemoteObjectUpdates = 0;
        this.sRemoteObjectUpdateSize = 0;
        this.sRemoteObjectHits = 0;
        this.sRemoteObjectFetchSize = 0;
        this.sRemoteObjectMisses = 0;
        this.sInvalidationLocal = 0;
        this.sInvalidationLocalExplicit = 0;
        this.sInvalidationLocalLRU = 0;
        this.sInvalidationLocalTimeout = 0;
        this.sInvalidationLocalDiskTimeout = 0;
        this.sInvalidationLocalClearAll = 0;
        this.sInvalidationRemote = 0;
        this.sInvalidationRemoteExplicit = 0;
        this.sInvalidationRemoteLRU = 0;
        this.sInvalidationRemoteTimeout = 0;
        this.sInvalidationRemoteDiskTimeout = 0;
        this.sInvalidationRemoteClearAll = 0;

        this.dMemEntriesSize = 0;
        this.dDiskEntriesSize = 0;
        this.dObjectsOnDisk = 0;
        this.dDependencyIdsInMemoryAndDisk = 0;
        this.dDependencyIdsOnDisk = 0;
        this.dTemplatesOnDisk = 0;
        this.dTotalCacheDataDiskSize = 0;
        this.dInvalidationLocal = 0;
        this.dInvalidationLocalExplicit = 0;
        this.dInvalidationLocalLRU = 0;
        this.dInvalidationLocalTimeout = 0;
        this.dInvalidationLocalDiskTimeout = 0;
        this.dInvalidationLocalClearAll = 0;
        this.dInvalidationRemote = 0;
        this.dInvalidationRemoteExplicit = 0;
        this.dInvalidationRemoteLRU = 0;
        this.dInvalidationRemoteTimeout = 0;
        this.dInvalidationRemoteDiskTimeout = 0;
        this.dInvalidationRemoteClearAll = 0;

        System.out.println("***************************************************");
        sindex = result.indexOf("serverName=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "serverName=".length(), eindex);
            System.out.println("serverName=" + temp);
        }

        sindex = result.indexOf("serverPort=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "serverPort=".length(), eindex);
            System.out.println("serverPort=" + temp);

        }
        System.out.println("***************************************************");
        sindex = result.indexOf("sMemEntriesSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sMemEntriesSize=".length(), eindex);
            try {
                this.sMemEntriesSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sMemEntriesSize=" + this.sMemEntriesSize);

        }
        sindex = result.indexOf("sDiskEntriesSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sDiskEntriesSize=".length(), eindex);
            try {
                this.sDiskEntriesSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sDiskEntriesSize=" + this.sDiskEntriesSize);
        }
        sindex = result.indexOf("sObjectsOnDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsOnDisk=".length(), eindex);
            try {
                this.sObjectsOnDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsOnDisk=" + this.sObjectsOnDisk);
        }
        sindex = result.indexOf("sDependencyIdsInMemoryAndDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sDependencyIdsInMemoryAndDisk=".length(), eindex);
            try {
                this.sDependencyIdsInMemoryAndDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sDependencyIdsInMemoryAndDisk=" + this.sDependencyIdsInMemoryAndDisk);
        }
        sindex = result.indexOf("sDependencyIdsOnDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sDependencyIdsOnDisk=".length(), eindex);
            try {
                this.sDependencyIdsOnDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sDependencyIdsOnDisk=" + this.sDependencyIdsOnDisk);
        }
        sindex = result.indexOf("sTemplatesOnDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sTemplatesOnDisk=".length(), eindex);
            try {
                this.sTemplatesOnDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sTemplatesOnDisk=" + this.sTemplatesOnDisk);
        }
        sindex = result.indexOf("sTotalCacheDataDiskSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sTotalCacheDataDiskSize=".length(), eindex);
            try {
                this.sTotalCacheDataDiskSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sTotalCacheDataDiskSize=" + this.sTotalCacheDataDiskSize);
        }
        sindex = result.indexOf("sCacheHits=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sCacheHits=".length(), eindex);
            try {
                this.sCacheHits = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sCacheHits=" + this.sCacheHits);
        }
        sindex = result.indexOf("sCacheMisses=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sCacheMisses=".length(), eindex);
            try {
                this.sCacheMisses = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sCacheMisses=" + this.sCacheMisses);
        }
        sindex = result.indexOf("sCacheRemoves=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sCacheRemoves=".length(), eindex);
            try {
                this.sCacheRemoves = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sCacheRemoves=" + this.sCacheRemoves);
        }
        sindex = result.indexOf("sCacheLruRemoves=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sCacheLruRemoves=".length(), eindex);
            try {
                this.sCacheLruRemoves = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sCacheLruRemoves=" + this.sCacheLruRemoves);
        }
        sindex = result.indexOf("sExplicitInvalidationsFromMemory=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sExplicitInvalidationsFromMemory=".length(), eindex);
            try {
                this.sExplicitInvalidationsFromMemory = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sExplicitInvalidationsFromMemory=" + this.sExplicitInvalidationsFromMemory);
        }
        sindex = result.indexOf("sExplicitInvalidationsFromDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sExplicitInvalidationsFromDisk=".length(), eindex);
            try {
                this.sExplicitInvalidationsFromDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sExplicitInvalidationsFromDisk=" + this.sExplicitInvalidationsFromDisk);
        }
        sindex = result.indexOf("sExplicitInvalidationsLocal=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sExplicitInvalidationsLocal=".length(), eindex);
            try {
                this.sExplicitInvalidationsLocal = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sExplicitInvalidationsLocal=" + this.sExplicitInvalidationsLocal);
        }
        sindex = result.indexOf("sExplicitInvalidationsRemote=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sExplicitInvalidationsRemote=".length(), eindex);
            try {
                this.sExplicitInvalidationsRemote = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sExplicitInvalidationsRemote=" + this.sExplicitInvalidationsRemote);
        }
        sindex = result.indexOf("sTimeoutInvalidationsFromMemory=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sTimeoutInvalidationsFromMemory=".length(), eindex);
            try {
                this.sTimeoutInvalidationsFromMemory = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sTimeoutInvalidationsFromMemory=" + this.sTimeoutInvalidationsFromMemory);
        }
        sindex = result.indexOf("sTimeoutInvalidationsFromDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sTimeoutInvalidationsFromDisk=".length(), eindex);
            try {
                this.sTimeoutInvalidationsFromDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sTimeoutInvalidationsFromDisk=" + this.sTimeoutInvalidationsFromDisk);
        }
        sindex = result.indexOf("sPendingRemovalFromDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sPendingRemovalFromDisk=".length(), eindex);
            try {
                this.sPendingRemovalFromDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sPendingRemovalFromDisk=" + this.sPendingRemovalFromDisk);
        }
        sindex = result.indexOf("sDependencyIdsBufferedForDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sDependencyIdsBufferedForDisk=".length(), eindex);
            try {
                this.sDependencyIdsBufferedForDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sDependencyIdsBufferedForDisk=" + this.sDependencyIdsBufferedForDisk);
        }
        sindex = result.indexOf("sDependencyIdsOffloadedToDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sDependencyIdsOffloadedToDisk=".length(), eindex);
            try {
                this.sDependencyIdsOffloadedToDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sDependencyIdsOffloadedToDisk=" + this.sDependencyIdsOffloadedToDisk);
        }
        sindex = result.indexOf("sDependencyIdBasedInvalidationsFromDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sDependencyIdBasedInvalidationsFromDisk=".length(), eindex);
            try {
                this.sDependencyIdBasedInvalidationsFromDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sDependencyIdBasedInvalidationsFromDisk=" + this.sDependencyIdBasedInvalidationsFromDisk);
        }
        sindex = result.indexOf("sTemplatesBufferedForDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sTemplatesBufferedForDisk=".length(), eindex);
            try {
                this.sTemplatesBufferedForDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sTemplatesBufferedForDisk=" + this.sTemplatesBufferedForDisk);
        }
        sindex = result.indexOf("sTemplatesOffloadedToDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sTemplatesOffloadedToDisk=".length(), eindex);
            try {
                this.sTemplatesOffloadedToDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sTemplatesOffloadedToDisk=" + this.sTemplatesOffloadedToDisk);
        }
        sindex = result.indexOf("sTemplateBasedInvalidationsFromDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sTemplateBasedInvalidationsFromDisk=".length(), eindex);
            try {
                this.sTemplateBasedInvalidationsFromDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sTemplateBasedInvalidationsFromDisk=" + this.sTemplateBasedInvalidationsFromDisk);
        }
        sindex = result.indexOf("sObjectsReadFromDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsReadFromDisk=".length(), eindex);
            try {
                this.sObjectsReadFromDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsReadFromDisk=" + this.sObjectsReadFromDisk);
        }
        sindex = result.indexOf("sObjectsReadFromDisk4K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsReadFromDisk4K=".length(), eindex);
            try {
                this.sObjectsReadFromDisk4K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsReadFromDisk4K=" + this.sObjectsReadFromDisk4K);
        }
        sindex = result.indexOf("sObjectsReadFromDisk40K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsReadFromDisk40K=".length(), eindex);
            try {
                this.sObjectsReadFromDisk40K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsReadFromDisk40K=" + this.sObjectsReadFromDisk40K);
        }
        sindex = result.indexOf("sObjectsReadFromDisk400K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsReadFromDisk400K=".length(), eindex);
            try {
                this.sObjectsReadFromDisk400K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsReadFromDisk400K=" + this.sObjectsReadFromDisk400K);
        }
        sindex = result.indexOf("sObjectsReadFromDisk4000K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsReadFromDisk4000K=".length(), eindex);
            try {
                this.sObjectsReadFromDisk4000K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsReadFromDisk4000K=" + this.sObjectsReadFromDisk4000K);
        }
        sindex = result.indexOf("sObjectsReadFromDiskSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsReadFromDiskSize=".length(), eindex);
            try {
                this.sObjectsReadFromDiskSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsReadFromDiskSize=" + this.sObjectsReadFromDiskSize);
        }
        sindex = result.indexOf("sObjectsWriteToDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsWriteToDisk=".length(), eindex);
            try {
                this.sObjectsWriteToDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsWriteToDisk=" + this.sObjectsWriteToDisk);
        }
        sindex = result.indexOf("sObjectsWriteToDisk4K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsWriteToDisk4K=".length(), eindex);
            try {
                this.sObjectsWriteToDisk4K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsWriteToDisk4K=" + this.sObjectsWriteToDisk4K);
        }
        sindex = result.indexOf("sObjectsWriteToDisk40K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsWriteToDisk40K=".length(), eindex);
            try {
                this.sObjectsWriteToDisk40K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsWriteToDisk40K=" + this.sObjectsWriteToDisk40K);
        }
        sindex = result.indexOf("sObjectsWriteToDisk400K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsWriteToDisk400K=".length(), eindex);
            try {
                this.sObjectsWriteToDisk400K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsWriteToDisk400K=" + this.sObjectsWriteToDisk400K);
        }
        sindex = result.indexOf("sObjectsWriteToDisk4000K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsWriteToDisk4000K=".length(), eindex);
            try {
                this.sObjectsWriteToDisk4000K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsWriteToDisk4000K=" + this.sObjectsWriteToDisk4000K);
        }
        sindex = result.indexOf("sObjectsWriteToDiskSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsWriteToDiskSize=".length(), eindex);
            try {
                this.sObjectsWriteToDiskSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsWriteToDiskSize=" + this.sObjectsWriteToDiskSize);
        }
        sindex = result.indexOf("sObjectsDeleteFromDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsDeleteFromDisk=".length(), eindex);
            try {
                this.sObjectsDeleteFromDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsDeleteFromDisk=" + this.sObjectsDeleteFromDisk);
        }
        sindex = result.indexOf("sObjectsDeleteFromDisk4K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsDeleteFromDisk4K=".length(), eindex);
            try {
                this.sObjectsDeleteFromDisk4K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsDeleteFromDisk4K=" + this.sObjectsDeleteFromDisk4K);
        }
        sindex = result.indexOf("sObjectsDeleteFromDisk40K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsDeleteFromDisk40K=".length(), eindex);
            try {
                this.sObjectsDeleteFromDisk40K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsDeleteFromDisk40K=" + this.sObjectsDeleteFromDisk40K);
        }
        sindex = result.indexOf("sObjectsDeleteFromDisk400K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsDeleteFromDisk400K=".length(), eindex);
            try {
                this.sObjectsDeleteFromDisk400K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsDeleteFromDisk400K=" + this.sObjectsDeleteFromDisk400K);
        }
        sindex = result.indexOf("sObjectsDeleteFromDisk4000K=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsDeleteFromDisk4000K=".length(), eindex);
            try {
                this.sObjectsDeleteFromDisk4000K = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsDeleteFromDisk4000K=" + this.sObjectsDeleteFromDisk4000K);
        }
        sindex = result.indexOf("sObjectsDeleteFromDiskSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sObjectsDeleteFromDiskSize=".length(), eindex);
            try {
                this.sObjectsDeleteFromDiskSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sObjectsDeleteFromDiskSize=" + this.sObjectsDeleteFromDiskSize);
        }
        sindex = result.indexOf("sPushPullTableSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sPushPullTableSize=".length(), eindex);
            try {
                this.sPushPullTableSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sPushPullTableSize=" + this.sPushPullTableSize);
        }
        sindex = result.indexOf("sRemoteInvalidationNotifications=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sRemoteInvalidationNotifications=".length(), eindex);
            try {
                this.sRemoteInvalidationNotifications = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sRemoteInvalidationNotifications=" + this.sRemoteInvalidationNotifications);
        }
        sindex = result.indexOf("sRemoteUpdateNotifications=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sRemoteUpdateNotifications=".length(), eindex);
            try {
                this.sRemoteUpdateNotifications = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sRemoteUpdateNotifications=" + this.sRemoteUpdateNotifications);
        }
        sindex = result.indexOf("sRemoteObjectUpdates=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sRemoteObjectUpdates=".length(), eindex);
            try {
                this.sRemoteObjectUpdates = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sRemoteObjectUpdates=" + this.sRemoteObjectUpdates);
        }
        sindex = result.indexOf("sRemoteObjectUpdateSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sRemoteObjectUpdateSize=".length(), eindex);
            try {
                this.sRemoteObjectUpdateSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sRemoteObjectUpdateSize=" + this.sRemoteObjectUpdateSize);
        }
        sindex = result.indexOf("sRemoteObjectHits=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sRemoteObjectHits=".length(), eindex);
            try {
                this.sRemoteObjectHits = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sRemoteObjectHits=" + this.sRemoteObjectHits);
        }
        sindex = result.indexOf("sRemoteObjectFetchSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sRemoteObjectFetchSize=".length(), eindex);
            try {
                this.sRemoteObjectFetchSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sRemoteObjectFetchSize=" + this.sRemoteObjectFetchSize);
        }
        sindex = result.indexOf("sRemoteObjectMisses=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sRemoteObjectMisses=".length(), eindex);
            try {
                this.sRemoteObjectMisses = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sRemoteObjectMisses=" + this.sRemoteObjectMisses);
        }
        sindex = result.indexOf("sInvalidationLocal=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationLocal=".length(), eindex);
            try {
                this.sInvalidationLocal = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationLocal=" + this.sInvalidationLocal);
        }
        sindex = result.indexOf("sInvalidationLocalExplicit=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationLocalExplicit=".length(), eindex);
            try {
                this.sInvalidationLocalExplicit = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationLocalExplicit=" + this.sInvalidationLocalExplicit);
        }
        sindex = result.indexOf("sInvalidationLocalLRU=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationLocalLRU=".length(), eindex);
            try {
                this.sInvalidationLocalLRU = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationLocalLRU=" + this.sInvalidationLocalLRU);
        }
        sindex = result.indexOf("sInvalidationLocalTimeout=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationLocalTimeout=".length(), eindex);
            try {
                this.sInvalidationLocalTimeout = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationLocalTimeout=" + this.sInvalidationLocalTimeout);
        }
        sindex = result.indexOf("sInvalidationLocalDiskTimeout=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationLocalDiskTimeout=".length(), eindex);
            try {
                this.sInvalidationLocalDiskTimeout = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationLocalDiskTimeout=" + this.sInvalidationLocalDiskTimeout);
        }
        sindex = result.indexOf("sInvalidationLocalClearAll=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationLocalClearAll=".length(), eindex);
            try {
                this.sInvalidationLocalClearAll = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationLocalClearAll=" + this.sInvalidationLocalClearAll);
        }
        sindex = result.indexOf("sInvalidationRemote=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationRemote=".length(), eindex);
            try {
                this.sInvalidationRemote = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationRemote=" + this.sInvalidationRemote);
        }
        sindex = result.indexOf("sInvalidationRemoteExplicit=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationRemoteExplicit=".length(), eindex);
            try {
                this.sInvalidationRemoteExplicit = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationRemoteExplicit=" + this.sInvalidationRemoteExplicit);
        }
        sindex = result.indexOf("sInvalidationRemoteLRU=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationRemoteLRU=".length(), eindex);
            try {
                this.sInvalidationRemoteLRU = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationRemoteLRU=" + this.sInvalidationRemoteLRU);
        }
        sindex = result.indexOf("sInvalidationRemoteTimeout=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationRemoteTimeout=".length(), eindex);
            try {
                this.sInvalidationRemoteTimeout = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationRemoteTimeout=" + this.sInvalidationRemoteTimeout);
        }
        sindex = result.indexOf("sInvalidationRemoteDiskTimeout=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationRemoteDiskTimeout=".length(), eindex);
            try {
                this.sInvalidationRemoteDiskTimeout = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationRemoteDiskTimeout=" + this.sInvalidationRemoteDiskTimeout);
        }
        sindex = result.indexOf("sInvalidationRemoteClearAll=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "sInvalidationRemoteClearAll=".length(), eindex);
            try {
                this.sInvalidationRemoteClearAll = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("sInvalidationRemoteClearAll=" + this.sInvalidationRemoteClearAll);
        }
        //
        // dmap 
        //
        sindex = result.indexOf("dMemEntriesSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dMemEntriesSize=".length(), eindex);
            try {
                this.dMemEntriesSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dMemEntriesSize=" + this.dMemEntriesSize);

        }
        sindex = result.indexOf("dDiskEntriesSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dDiskEntriesSize=".length(), eindex);
            try {
                this.dDiskEntriesSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dDiskEntriesSize=" + this.dDiskEntriesSize);
        }
        sindex = result.indexOf("dObjectsOnDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dObjectsOnDisk=".length(), eindex);
            try {
                this.dObjectsOnDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dObjectsOnDisk=" + this.dObjectsOnDisk);
        }
        sindex = result.indexOf("dDependencyIdsInMemoryAndDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dDependencyIdsInMemoryAndDisk=".length(), eindex);
            try {
                this.dDependencyIdsInMemoryAndDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dDependencyIdsInMemoryAndDisk=" + this.dDependencyIdsInMemoryAndDisk);
        }
        sindex = result.indexOf("dDependencyIdsOnDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dDependencyIdsOnDisk=".length(), eindex);
            try {
                this.dDependencyIdsOnDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dDependencyIdsOnDisk=" + this.dDependencyIdsOnDisk);
        }
        sindex = result.indexOf("dTemplatesOnDisk=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dTemplatesOnDisk=".length(), eindex);
            try {
                this.dTemplatesOnDisk = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dTemplatesOnDisk=" + this.dTemplatesOnDisk);
        }
        sindex = result.indexOf("dTotalCacheDataDiskSize=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dTotalCacheDataDiskSize=".length(), eindex);
            try {
                this.dTotalCacheDataDiskSize = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dTotalCacheDataDiskSize=" + this.dTotalCacheDataDiskSize);
        }
        sindex = result.indexOf("dInvalidationLocal=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationLocal=".length(), eindex);
            try {
                this.dInvalidationLocal = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationLocal=" + this.dInvalidationLocal);
        }
        sindex = result.indexOf("dInvalidationLocalExplicit=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationLocalExplicit=".length(), eindex);
            try {
                this.dInvalidationLocalExplicit = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationLocalExplicit=" + this.dInvalidationLocalExplicit);
        }
        sindex = result.indexOf("dInvalidationLocalLRU=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationLocalLRU=".length(), eindex);
            try {
                this.dInvalidationLocalLRU = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationLocalLRU=" + this.dInvalidationLocalLRU);
        }
        sindex = result.indexOf("dInvalidationLocalTimeout=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationLocalTimeout=".length(), eindex);
            try {
                this.dInvalidationLocalTimeout = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationLocalTimeout=" + this.dInvalidationLocalTimeout);
        }
        sindex = result.indexOf("dInvalidationLocalDiskTimeout=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationLocalDiskTimeout=".length(), eindex);
            try {
                this.dInvalidationLocalDiskTimeout = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationLocalDiskTimeout=" + this.dInvalidationLocalDiskTimeout);
        }
        sindex = result.indexOf("dInvalidationLocalClearAll=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationLocalClearAll=".length(), eindex);
            try {
                this.dInvalidationLocalClearAll = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationLocalClearAll=" + this.dInvalidationLocalClearAll);
        }
        sindex = result.indexOf("dInvalidationRemote=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationRemote=".length(), eindex);
            try {
                this.dInvalidationRemote = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationRemote=" + this.dInvalidationRemote);
        }
        sindex = result.indexOf("dInvalidationRemoteExplicit=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationRemoteExplicit=".length(), eindex);
            try {
                this.dInvalidationRemoteExplicit = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationRemoteExplicit=" + this.dInvalidationRemoteExplicit);
        }
        sindex = result.indexOf("dInvalidationRemoteLRU=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationRemoteLRU=".length(), eindex);
            try {
                this.dInvalidationRemoteLRU = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationRemoteLRU=" + this.dInvalidationRemoteLRU);
        }
        sindex = result.indexOf("dInvalidationRemoteTimeout=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationRemoteTimeout=".length(), eindex);
            try {
                this.dInvalidationRemoteTimeout = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationRemoteTimeout=" + this.dInvalidationRemoteTimeout);
        }
        sindex = result.indexOf("dInvalidationRemoteDiskTimeout=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationRemoteDiskTimeout=".length(), eindex);
            try {
                this.dInvalidationRemoteDiskTimeout = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationRemoteDiskTimeout=" + this.dInvalidationRemoteDiskTimeout);
        }
        sindex = result.indexOf("dInvalidationRemoteClearAll=");
        if (sindex > 0) {
            eindex = result.indexOf(" ", sindex);
            temp = result.substring(sindex + "dInvalidationRemoteClearAll=".length(), eindex);
            try {
                this.dInvalidationRemoteClearAll = new Long(temp).longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("dInvalidationRemoteClearAll=" + this.dInvalidationRemoteClearAll);
        }
    }

    public String getResult() {
        return result;
    }

    public void setResult(String string) {
        result = string;
    }

}
