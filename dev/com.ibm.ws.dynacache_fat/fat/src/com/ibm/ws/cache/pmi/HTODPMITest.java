// 1.5,7/1/08
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2008
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.pmi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.CacheConfig;
import com.ibm.ws.cache.htod.HTODTest2;
import com.ibm.ws.cache.servlet.SharedServer;
import com.ibm.ws.cache.stat.internal.WSDynamicCacheStats;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

import componenttest.custom.junit.runner.Mode;
import componenttest.custom.junit.runner.Mode.TestMode;

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
/*
 * [kelapure@art7 dynacache.fvt]$ /home/kelapure/autowas/configs/regression/tools/java/bin/java
 * -Dcom.ibm.CORBA.ConfigURL=file:/home/kelapure/autowas/configs/regression/tools/sharedLibraries/simplicity/properties/sas.client.props
 * -Dcom.ibm.SSL.ConfigURL=file:/home/kelapure/autowas/configs/regression/tools/sharedLibraries/simplicity/properties/ssl.client.props
 * -Djava.ext.dirs=/home/kelapure/autowas/configs/regression/tools/sharedLibraries/simplicity/lib:/home/kelapure/autowas/configs/regression/tools/java/jre/lib:/home/kelapure/autowas/configs/regression/tools/java/jre/lib/ext:/home/kelapure/autowas/configs/tools/wasClient/lib
 * -cp ./build/lib/dynacachetests.jar com.ibm.ws.cache.pmi.HTODPMITest
 */
public class HTODPMITest extends PMITest {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("HTODPMITest");

    boolean debug = true;

    //needed for resetPMI
    HTODTest2 ht = new HTODTest2();

    //used by all tests
    TreeMap servlet_disk_hmap = new TreeMap(); //servlet cache DISK GROUP counters
    TreeMap object_disk_hmap = new TreeMap(); //object cache DISK GROUP counters

    public static final String objectCache = "services/cache/htodObjectInstance";
    public static final String servletCache = "services/cache/htodServletInstance";

    public static String URI = "/dynacachetests/dmapstress2";

    Map[] servlet_maps = new Map[5];
    Map[] object_maps = new Map[3];

    /**
     * IN LIBERTY ONLY HIGH PERFORMANCE MODE IS SUPPORTED
     * NO TESTING FOR CUSTOM AND LOW MODES
     */
    /* Mother Load of tests. This will torcher many a DynaCache tester for years to come... */
    @Override
    @Test
    @Mode(TestMode.QUARANTINE)
    public void testPMI() throws Exception {

        // All the 6 cache instances are defined in cacheinstances.properties
        // Cache Instance 3 : HIGH PERFORMANCE :   services/cache/htodServletInstance_3 services/cache/htodObjectInstance_3
        // Cache Instence 2 : CUSTOM PERFORMANCE: services/cache/htodServletInstance_2 services/cache/htodObjectInstance_2
        // Cache Instance 0 : LOW PERFORMANCE :    services/cache/htodServletInstance_0 services/cache/htodObjectInstance_0

        // cacheSize = 100
        // diskCacheSize = 500

        //High mode tests
        resetCountersAndMaps("services/cache/htodServletInstance_3", "services/cache/htodObjectInstance_3");
        System.out.println("HIGH mode START");
        tObjectsOnDisk(CacheConfig.HIGH);
        tHitsOnDisk(CacheConfig.HIGH);
        tExplicitInvalidationsFromDisk(CacheConfig.HIGH);
        tTimeoutInvalidationsFromDisk(CacheConfig.HIGH);
        tTemplatesAndDependencyIds(CacheConfig.HIGH);
        tCommonCounters(CacheConfig.HIGH);
        tPendingRemovalFromDisk(CacheConfig.HIGH);
        System.out.println("HIGH mode END");

        //custom mode tests
//        resetCountersAndMaps("services/cache/htodServletInstance_2", "services/cache/htodObjectInstance_2");
//        System.out.println("CUSTOM mode START");
//        tObjectsOnDisk(CacheConfig.CUSTOM);
//        tHitsOnDisk(CacheConfig.CUSTOM);
//        tExplicitInvalidationsFromDisk(CacheConfig.CUSTOM);
//        tTimeoutInvalidationsFromDisk(CacheConfig.CUSTOM);
//        tTemplatesAndDependencyIds(CacheConfig.CUSTOM);
//        tCommonCounters(CacheConfig.CUSTOM);
//        tPendingRemovalFromDisk(CacheConfig.CUSTOM);
//        System.out.println("CUSTOM mode END");

        //low mode tests
//        resetCountersAndMaps("services/cache/htodServletInstance_0", "services/cache/htodObjectInstance_0");
//        System.out.println("LOW mode START");
//        tObjectsOnDisk(CacheConfig.LOW);
//        tHitsOnDisk(CacheConfig.LOW);
//        tExplicitInvalidationsFromDisk(CacheConfig.LOW);
//        tTimeoutInvalidationsFromDisk(CacheConfig.LOW);
//        tTemplatesAndDependencyIds(CacheConfig.LOW);
//        tCommonCounters(CacheConfig.LOW);
//        tPendingRemovalFromDisk(CacheConfig.LOW);
//        System.out.println("LOW mode END");

    }

    private void tPendingRemovalFromDisk(int pLevel) throws Exception {

        System.out.println("Running tPendingRemovalFromDisk in performance mode:" + pLevel);

        //parse PendingRemovalFromDisk= from the servlet

        // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D
        String uri = URI + "?threadParms=" + // 500 entries in disk
                     "1_600_1_0_1_1_1_0_0_0_0_0_0_0_0_0;" + // servlet (get, put, dep inv)
                     "&valueSize=1000&depids=10&clearBT=false" +
                     "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&delayAT=50000" +
                     "&threads=8&win=50" +
                     "&exps=-1&expd=-1" +
                     "&method=test1" +
                     "&cacheServletName=" + servletCache + "_" + pLevel + "&cacheDmapName=" + objectCache + "_" + pLevel;

        String response = getAndCheckResponse(uri, "tPendingRemovalFromDisk");
        long pendingRemovalFromDisk = 0;

        //parse out the pending removal from disk.
        //This counter will never show up in PMI metrics that is why we have to get it in this backdoor fashion.
        //this counter only gets a value in the middle of the test run
        String temp = parseFromServletOutput(response, "PendingRemovalFromDisk=");
        pendingRemovalFromDisk = new Long(temp).longValue();

        // if ctr. value is less than 400 or more than 500 then we are in trouble
        if (350 > pendingRemovalFromDisk || pendingRemovalFromDisk > 500) {
            fail("Expected value of pendingRemovalFromDisk is between 400 and 500"
                 + " Received:"
                 + pendingRemovalFromDisk);
        }
    }

    private void tCommonCounters(int pLevel) throws Exception {

        System.out.println("Running tCommonCounters in performance mode:" + pLevel);
        if (debug) {
            r_hmap.clear();
            System.out.println("Counters before running tCommonCounters:" + pLevel);
            count = readCountersFromPMI(!DISTRIB, getServletCacheModule(pLevel), r_hmap);
        }

        // HitsOnDisk and ExplicitInvalidationsFromDisk are the common counters across groups

        // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D
        String uri = URI + "?threadParms=" + // 400 entries in both servlet and dmap
                     "1_100_1_0_0_1_1_0_0_0_0_0_0_0_0_0;" + // (get, dep inv)
                     "1_100_1_0_0_1_1_0_0_0_0_0_0_0_0_0;" + // (get, dep inv)
                     "1_100_1_0_0_1_0_1_0_0_0_0_0_0_0_0;" + // (get, template inv)
                     "1_100_1_0_0_1_0_1_0_0_0_0_0_0_0_0;" + // (get, template inv)

                     "1_100_1_0_0_1_1_0_0_0_0_0_0_0_0_1;" + // (get, dep inv)
                     "1_100_1_0_0_1_1_0_0_0_0_0_0_0_0_1;" + // (get, dep inv)
                     "1_100_1_0_0_1_1_0_0_0_0_0_0_0_0_1;" + // (get, dep inv)
                     "1_100_1_0_0_1_1_0_0_0_0_0_0_0_0_1;" + // (get, dep inv)

                     "&valueSize=1000&depids=10&clearBT=false" +
                     "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&delayAT=50000" +
                     "&threads=8&win=50" +
                     "&exps=-1&expd=-1" +
                     "&method=test1" +
                     "&cacheServletName=" + servletCache + "_" + pLevel + "&cacheDmapName=" + objectCache + "_" + pLevel;

        String response = getAndCheckResponse(uri, "tCommonCounters");

        //NOTE The value of DependencyIdsOnDisk and DependencyIdsBufferedForDisk can range from 30 - 35
        Set approxCounters = new HashSet();
        approxCounters.add("DependencyIdBasedInvalidationsFromDisk");
        approxCounters.add("HitsOnDisk");
        approxCounters.add("TemplateBasedInvalidationsFromDisk");

        //Set all the expected counters here
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.ObjectsOnDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.HitsOnDisk, 400, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.ExplicitInvalidationsFromDisk, 400, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsOnDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsOffloadedToDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdBasedInvalidationsFromDisk, 20, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesOnDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesBufferedForDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplateBasedInvalidationsFromDisk, 2, PMIHelper.TYPE_SET);

        //OBJECT disk group
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.ObjectsOnDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.HitsOnDisk, 400, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.ExplicitInvalidationsFromDisk, 400, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsOnDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsOffloadedToDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdBasedInvalidationsFromDisk, 32, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.TemplatesOnDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.TemplatesBufferedForDisk, 0, PMIHelper.TYPE_SET);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.TemplateBasedInvalidationsFromDisk, 0, PMIHelper.TYPE_SET);
        //only PendingRemovalFromDisk and TimeoutInvalidationsFromDisk is missing from this list

        r_hmap.clear();

        //Sleep a little bit for changes to kick-in
        Thread.sleep(120000); //RJMP:  Changed from 60 seconds to 120 seconds

        count = readCountersFromPMI(!DISTRIB, getServletCacheModule(pLevel), r_hmap);
        PMIHelper.compareCountersInRange(getServletCacheModule(pLevel), servlet_disk_hmap, r_hmap, 6, approxCounters);

        r_hmap.clear();
        count = readCountersFromPMI(!DISTRIB, getObjectCacheModule(pLevel), r_hmap);
        PMIHelper.compareCountersInRange(getObjectCacheModule(pLevel), object_disk_hmap, r_hmap, 100, approxCounters);

        /* Servlet cache HitsOnDisk and ExplicitInvalidationsFromDisk counter comparison */
        //collect values from the templates and make sure the values total up correctly for all the template instances
        String serverName = parseFromServletOutput(response, "serverName=");
        String serverPort = parseFromServletOutput(response, "serverPort=");
        String templateCacheModule = PMIHelper.CACHE_SERVLET_CUSTOM + pLevel + ";cacheModule.template";

        long cumulativeHitsOnDisk = 0;
        long cumulativeExplicitInvalidationsFromDisk = 0;
        for (int i = 0; i < 4; i++) {
            String templateInstanceCacheModule = templateCacheModule + ";" + serverName + ":" + serverPort + ":thread:" + i + ":template";
            cumulativeHitsOnDisk += getCounterValue("HitsOnDiskCount", templateInstanceCacheModule);
            cumulativeExplicitInvalidationsFromDisk += getCounterValue("ExplicitDiskInvalidationCount", templateInstanceCacheModule);
        }

        //template.value = template1.value + template2.value + template3.value + template4.value
        long templateHitsOnDisk = getCounterValue("HitsOnDiskCount", templateCacheModule);
        long templateExplicitInvalidationsFromDisk = getCounterValue("ExplicitDiskInvalidationCount", templateCacheModule);

        if (templateHitsOnDisk != cumulativeHitsOnDisk) {
            fail("Template instance HitsOnDisk values don't add up to the template value");
        }
        if (templateExplicitInvalidationsFromDisk != cumulativeExplicitInvalidationsFromDisk) {
            fail("Template instance ExplicitInvalidationsFromDisk values don't add up to the template value");
        }

        //template values are equal to the disk group statistic value
        long diskGroupStatHits = getCounterValue("HitsOnDisk", getServletCacheModule(pLevel));
        long diskGroupStatExplicitInvalidations = getCounterValue("ExplicitInvalidationsFromDisk", getServletCacheModule(pLevel));
        if (templateHitsOnDisk != diskGroupStatHits) {
            fail("HitsOnDisk value in " + templateCacheModule + " doesnt match up with value in" + getServletCacheModule(pLevel));
        }
        if (templateExplicitInvalidationsFromDisk != diskGroupStatExplicitInvalidations) {
            fail("ExplicitInvalidationsFromDisk value in " + templateCacheModule + " doesnt match up with value in" + getServletCacheModule(pLevel));
        }

        /* Object cache HitsOnDisk and ExplicitInvalidationsFromDisk counter comparison */
        diskGroupStatHits = getCounterValue("HitsOnDisk", getObjectCacheModule(pLevel));
        diskGroupStatExplicitInvalidations = getCounterValue("ExplicitInvalidationsFromDisk", getObjectCacheModule(pLevel));
        String customObjectCacheModule = PMIHelper.CACHE_OBJECT_CUSTOM + pLevel + ";cacheModule.objectCache";
        if (diskGroupStatHits != getCounterValue("HitsOnDiskCount", customObjectCacheModule)) {
            fail("HitsOnDisk value in " + customObjectCacheModule + " doesnt match up with value in" + getObjectCacheModule(pLevel));
        }
        if (diskGroupStatExplicitInvalidations != getCounterValue("ExplicitDiskInvalidationCount", customObjectCacheModule)) {
            fail("ExplicitInvalidationsFromDisk value in " + customObjectCacheModule + " doesnt match up with value in" + getObjectCacheModule(pLevel));
        }
    }

    private long getCounterValue(String counterName, String cacheModule) throws Exception {

        long value = 0;
        if (debug) {
            System.out.println("CM:" + cacheModule);
        }

        r_hmap.clear();
        readCountersFromPMI(!DISTRIB, cacheModule, r_hmap);
        if (debug) {
            System.out.println("r_hmap:" + r_hmap);
        }
        String temp = (String) r_hmap.get(counterName);
        long rc = new Long(temp).longValue();

        return rc;
    }

    private String parseFromServletOutput(String body, String find) {

        int sindex = body.indexOf(find);
        String found = null;
        if (sindex > 0) {
            int eindex = body.indexOf(" ", sindex);
            found = body.substring(sindex + find.length(), eindex);
        }

        return found;

    }

    private void tTemplatesAndDependencyIds(int pLevel) throws Exception {

        System.out.println("tTemplatesAndDependencyIds in performance mode:" + pLevel);
        if (debug) {
            r_hmap.clear();
            System.out.println("Counters before running tTemplatesAndDependencyIds:" + pLevel);
            count = readCountersFromPMI(!DISTRIB, getServletCacheModule(pLevel), r_hmap);
        }

        // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D
        String uri = URI + "?threadParms=" + // 400 entries in both servlet and dmap
                     "1_100_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // (servlet no timeout put) template 1
                     "1_100_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // (servlet no timeout put) template 2
                     "1_100_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // (servlet no timeout put) template 3
                     "1_100_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // (servlet no timeout put) template 4

                     "1_100_1_0_1_0_0_0_0_0_0_0_0_0_0_1;" + // (dmap no timeout put) template 1
                     "1_100_1_0_1_0_0_0_0_0_0_0_0_0_0_1;" + // (dmap no timeout put) template 2
                     "1_100_1_0_1_0_0_0_0_0_0_0_0_0_0_1;" + // (dmap no timeout put) template 3
                     "1_100_1_0_1_0_0_0_0_0_0_0_0_0_0_1;" + // (dmap no timeout put) template 4

                     "&valueSize=1000&depids=10&clearBT=false" +
                     "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&delayAT=50000" +
                     "&threads=8&win=50" +
                     "&exps=-1&expd=-1" +
                     "&method=test1" +
                     "&cacheServletName=" + servletCache + "_" + pLevel + "&cacheDmapName=" + objectCache + "_" + pLevel;

        getAndCheckResponse(uri, "tTemplates");

        //NOTE The value of DependencyIdsOnDisk and DependencyIdsBufferedForDisk can range from 30 - 35

        //SERVLET disk group
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.ObjectsOnDisk, 300, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsOnDisk, 32, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesOnDisk, 4, PMIHelper.TYPE_ADD);

        //OBJECT disk group
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.ObjectsOnDisk, 300, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsOnDisk, 31, PMIHelper.TYPE_ADD);

        if (pLevel == CacheConfig.HIGH || pLevel == CacheConfig.CUSTOM) {
            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 31, PMIHelper.TYPE_ADD);
            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesBufferedForDisk, 4, PMIHelper.TYPE_ADD);

            PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 31, PMIHelper.TYPE_ADD);
        } else if (pLevel == CacheConfig.LOW) {
            //NOTE: buffered and offloaded template and dependency counters are 0 here
        }

        Map<String, Long> approxCounters = new HashMap<String, Long>();
        approxCounters.put("DependencyIdsOnDisk", 3l);
        approxCounters.put("DependencyIdsBufferedForDisk", 3l);
        approxCounters.put("TemplatesBufferedForDisk", 1l);
        approxCounters.put("TemplatesOnDisk", 1l);

        r_hmap.clear();
        Thread.sleep(10000);
        count = readCountersFromPMI(!DISTRIB, getServletCacheModule(pLevel), r_hmap);
        PMIHelper.compareCountersInRangeMap(getServletCacheModule(pLevel), servlet_disk_hmap, r_hmap, approxCounters);

        r_hmap.clear();
        Thread.sleep(10000);
        count = readCountersFromPMI(!DISTRIB, getObjectCacheModule(pLevel), r_hmap);
        PMIHelper.compareCountersInRangeMap(getObjectCacheModule(pLevel), object_disk_hmap, r_hmap, approxCounters);
    }

    private void tTimeoutInvalidationsFromDisk(int pLevel) throws Exception {

        System.out.println("tTimeoutInvalidationsFromDisk in performance mode:" + pLevel);
        if (debug) {
            r_hmap.clear();
            System.out.println("Counters before running tTimeoutInvalidationsFromDisk:" + pLevel);
            count = readCountersFromPMI(!DISTRIB, getServletCacheModule(pLevel), r_hmap);
        }

        //ORIGINAL INTENTION
        // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D
        String uri = URI + "?threadParms=" +
                     "1_300_1_10_1_0_0_0_0_0_0_0_0_0_0_0;" + // (timeout put)
                     "1_300_1_10_1_0_0_0_0_0_0_0_0_0_0_1;" + // (timeout put)
                     "&valueSize=1000&depids=100&clearBT=false" +
                     "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&delayAT=50000" + //wait for 50 seconds for the entries to timeout
                     "&threads=2&win=200" +
                     "&exps=-1&expd=-1" +
                     "&method=test1" +
                     "&cacheServletName=" + servletCache + "_" + pLevel + "&cacheDmapName=" + objectCache + "_" + pLevel;

        getAndCheckResponse(uri, "tTimeoutInvalidationsFromDisk");

        // RESOLVE ISSUE
        //Issue: why have do I have to issue a get for the items to expire
        //Issue: why does a get increase the TemplatesOffloadedToDisk counter to 2 ...?
        uri = URI + "?threadParms=" +
              "1_300_1_0_0_1_0_0_0_0_0_0_0_0_0_0;" + // (get)
              "1_300_1_0_0_1_0_0_0_0_0_0_0_0_0_1;" + // (get)
              "&valueSize=1000&depids=100&clearBT=false" +
              "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&delayAT=50000" + //wait for 50 seconds for the entries to timeout
              "&threads=2&win=200" +
              "&exps=-1&expd=-1" +
              "&method=test1" +
              "&cacheServletName=" + servletCache + "_" + pLevel + "&cacheDmapName=" + objectCache + "_" + pLevel;
        getAndCheckResponse(uri, "tTimeoutInvalidationsFromDisk");

        //SERVLET disk group
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TimeoutInvalidationsFromDisk, 200, PMIHelper.TYPE_ADD);

        //Object cache disk group
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.TimeoutInvalidationsFromDisk, 200, PMIHelper.TYPE_ADD);

        if (pLevel == CacheConfig.CUSTOM) {

            //SERVLET disk group
            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesOffloadedToDisk, 1, PMIHelper.TYPE_ADD);

            //Object cache disk group No offload for object cache
            //PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.TemplatesOffloadedToDisk, 1, PMIHelper.TYPE_ADD);

        }

        r_hmap.clear();
        Thread.sleep(10000);
        count = readCountersFromPMI(!DISTRIB, getServletCacheModule(pLevel), r_hmap);
        PMIHelper.compareCounters(getServletCacheModule(pLevel), servlet_disk_hmap, r_hmap);

        r_hmap.clear();
        Thread.sleep(10000);
        count = readCountersFromPMI(!DISTRIB, getObjectCacheModule(pLevel), r_hmap);
        PMIHelper.compareCounters(getObjectCacheModule(pLevel), object_disk_hmap, r_hmap);
    }

    private void tExplicitInvalidationsFromDisk(int pLevel) throws Exception {

        System.out.println("tExplicitInvalidationsFromDisk in performance mode:" + pLevel);

        // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D
        String uri = URI + "?threadParms=" +
                     "1_500_1_0_0_0_1_0_0_0_0_0_0_0_0_0;" + // (dep inv)
                     "1_600_1_0_0_0_1_0_0_0_0_0_0_0_0_1;" + // (dep inv)
                     "&valueSize=1000&depids=100" +
                     "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&delayAT=80000" +
                     "&threads=2&win=200" +
                     "&clearBT=false&exps=-1&expd=-1" +
                     "&method=test1" +
                     "&cacheServletName=" + servletCache + "_" + pLevel + "&cacheDmapName=" + objectCache + "_" + pLevel;

        getAndCheckResponse(uri, "tExplicitInvalidationsFromDisk");

        //SERVLET disk group
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.ObjectsOnDisk, 500, PMIHelper.TYPE_SUBTRACT);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.ExplicitInvalidationsFromDisk, 500, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdBasedInvalidationsFromDisk, 4, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsOnDisk, 5, PMIHelper.TYPE_SUBTRACT);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesOnDisk, 1, PMIHelper.TYPE_SUBTRACT);

        //Object cache disk group
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.ObjectsOnDisk, 500, PMIHelper.TYPE_SUBTRACT);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.ExplicitInvalidationsFromDisk, 500, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdBasedInvalidationsFromDisk, 4, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsOnDisk, 5, PMIHelper.TYPE_SUBTRACT);

        if (pLevel == CacheConfig.HIGH) {
            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 5, PMIHelper.TYPE_SUBTRACT);
            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesBufferedForDisk, 1, PMIHelper.TYPE_SUBTRACT);

            PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 5, PMIHelper.TYPE_SUBTRACT);

        } else if (pLevel == CacheConfig.CUSTOM) {
            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 5, PMIHelper.TYPE_SUBTRACT);

            PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 5, PMIHelper.TYPE_SUBTRACT);
        }

        else if (pLevel == CacheConfig.LOW) {
            //NOTE: buffered and offloaded template and dependency counters are 0 here
        }

        r_hmap.clear();
        Thread.sleep(10000);
        count = readCountersFromPMI(!DISTRIB, getServletCacheModule(pLevel), r_hmap);
        PMIHelper.compareCounters(getServletCacheModule(pLevel), servlet_disk_hmap, r_hmap);

        r_hmap.clear();
        Thread.sleep(10000);
        count = readCountersFromPMI(!DISTRIB, getObjectCacheModule(pLevel), r_hmap);
        PMIHelper.compareCounters(getObjectCacheModule(pLevel), object_disk_hmap, r_hmap);
    }

    private void tHitsOnDisk(int pLevel) throws Exception {

        System.out.println("tHitsOnDisk in performance mode:" + pLevel);

        // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D
        String uri = URI + "?threadParms=" +
                     "1_500_1_0_0_1_0_0_0_0_0_0_0_0_0_0;" + // (get)
                     "1_600_1_0_0_1_0_0_0_0_0_0_0_0_0_1;" + // (get)
                     "&valueSize=1000&depids=100" +
                     "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&delayAT=20000" +
                     "&threads=2&win=200" +
                     "&clearBT=false&exps=-1&expd=-1" +
                     "&method=test1" +
                     "&cacheServletName=" + servletCache + "_" + pLevel + "&cacheDmapName=" + objectCache + "_" + pLevel;

        getAndCheckResponse(uri, "tHitsOnDisk");

        //SERVLET disk group
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.HitsOnDisk, 500, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.ObjectsOnDisk, 100, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsOnDisk, 1, PMIHelper.TYPE_ADD);

        if (pLevel == CacheConfig.HIGH || pLevel == CacheConfig.CUSTOM) {
            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 1, PMIHelper.TYPE_ADD);

        } else if (pLevel == CacheConfig.LOW) {
            //NOTE: buffered and offloaded template and dependency counters are 0 here
        }

        //OBJECT disk group
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.HitsOnDisk, 500, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.OverflowInvalidationsFromDisk, 100, PMIHelper.TYPE_ADD);

        r_hmap.clear();
        Thread.sleep(10000);
        count = readCountersFromPMI(!DISTRIB, getServletCacheModule(pLevel), r_hmap);
        PMIHelper.compareCounters(getServletCacheModule(pLevel), servlet_disk_hmap, r_hmap);

        r_hmap.clear();
        Thread.sleep(30000);
        count = readCountersFromPMI(!DISTRIB, getObjectCacheModule(pLevel), r_hmap);
        try {
            PMIHelper.compareCounters(getObjectCacheModule(pLevel), object_disk_hmap, r_hmap);
        } catch (Exception ex) {
            Thread.sleep(60000);
            count = readCountersFromPMI(!DISTRIB, getObjectCacheModule(pLevel), r_hmap);
            PMIHelper.compareCounters(getObjectCacheModule(pLevel), object_disk_hmap, r_hmap);
        }

    }

    public void tObjectsOnDisk(int pLevel) throws Exception {

        // THREAD PARAMS TO TRIGGER the Dmap stress2  servlet
        // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D

        System.out.println("tObjectsOnDisk in performance mode:" + pLevel);

        // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D
        String uri = URI + "?threadParms=" +
                     "1_500_1_0_1_0_0_0_0_0_0_0_0_0_0_0;" + // servlet no timeout put
                     "1_700_1_0_1_0_0_0_0_0_0_0_0_0_0_1;" + // dmap no timeout put
                     "&valueSize=1000&depids=100" +
                     "&delay=0&ddelay=0&tdelay=0&cdelay=0&sdelay=0&adelay=0&delayAT=80000" +
                     "&threads=2&win=200" +
                     "&clearBT=false&exps=-1&expd=-1" +
                     "&method=test1" +
                     "&cacheServletName=" + servletCache + "_" + pLevel + "&cacheDmapName=" + objectCache + "_" + pLevel;

        getAndCheckResponse(uri, "tObjectsOnDisk");

        //SERVLET disk group
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.ObjectsOnDisk, 400, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsOnDisk, 4, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesOnDisk, 1, PMIHelper.TYPE_ADD);

        //OBJECT disk group
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.ObjectsOnDisk, 500, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsOnDisk, 5, PMIHelper.TYPE_ADD);
        PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.OverflowInvalidationsFromDisk, 100, PMIHelper.TYPE_ADD);

        if (pLevel == CacheConfig.HIGH) {

            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 4, PMIHelper.TYPE_ADD);
            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesBufferedForDisk, 1, PMIHelper.TYPE_ADD);

            PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 5, PMIHelper.TYPE_ADD);

        } else if (pLevel == CacheConfig.CUSTOM) {

            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 4, PMIHelper.TYPE_ADD);
            PMIHelper.setExpectedCounter(servlet_disk_hmap, WSDynamicCacheStats.TemplatesOffloadedToDisk, 1, PMIHelper.TYPE_ADD);

            PMIHelper.setExpectedCounter(object_disk_hmap, WSDynamicCacheStats.DependencyIdsBufferedForDisk, 5, PMIHelper.TYPE_ADD);

        } else if (pLevel == CacheConfig.LOW) {
            // NOTE in CacheConfig.LOW buffered and offloaded template & dependency counters are 0
        }

        //series of checks is to verify that we are seeing the correct no. of counters
        //at each level. This check will be performed in the first and the last test
        r_hmap.clear();
        Thread.sleep(10000);
        count = readCountersFromPMI(!DISTRIB, "cacheModule", r_hmap);
        assertEquals("testObjectsOnDisk...1 count not equal", 17, count);

        r_hmap.clear();
        Thread.sleep(10000);
        count = readCountersFromPMI(!DISTRIB, getServletCacheModule(pLevel), r_hmap);
        assertEquals("testObjectsOnDisk...2 - count not equal", 15, count);
        PMIHelper.compareCounters(getServletCacheModule(pLevel), servlet_disk_hmap, r_hmap);

        /*
         * r_hmap.clear();
         * count = readCountersFromPMI(!DISTRIB, getObjectCacheModule(pLevel), r_hmap);
         * assertEquals("testObjectsOnDisk...3 - count not equal", 15, count);
         * PMIHelper.compareCounters(getObjectCacheModule(pLevel), object_disk_hmap, r_hmap);
         */

    }

    private String getObjectCacheModule(int performanceLevel) {
        String objectCacheModule = PMIHelper.CACHE_OBJECT_CUSTOM
                                   + performanceLevel
                                   + ";cacheModule.disk;cacheModule.diskOffloadEnabled";
        return objectCacheModule;
    }

    private String getServletCacheModule(int performanceLevel) {
        String servletCacheModule = PMIHelper.CACHE_SERVLET_CUSTOM
                                    + performanceLevel
                                    + ";cacheModule.disk;cacheModule.diskOffloadEnabled";
        return servletCacheModule;
    }

    public void resetCountersAndMaps(String sCacheName, String oCacheName) throws Exception {

        //servlet cache counters
        servlet_disk_hmap.clear();

        //object cache counters
        object_disk_hmap.clear();

        //reset cache and PMI Counters
        resetCacheAndPMICounters(sCacheName, oCacheName);

        //initialize the counters
        initializeCounterMaps();

        setupPMIConfig();

    }

    private void initializeCounterMaps() throws Exception {

        //INSTANCE NO. does not matter for initialization.

        PMIHelper.initializeExpectedCounters(servlet_disk_hmap,
                                             15, PMIHelper.CACHE_SERVLET_CUSTOM + "0;cacheModule.disk");
        PMIHelper.initializeExpectedCounters(object_disk_hmap,
                                             15, PMIHelper.CACHE_OBJECT_CUSTOM + "0;cacheModule.disk");
    }

    private String getAndCheckResponse(String uri, String testname) throws Exception {

        WebResponse resp = null;
        String result = null;
        WebConversation wc = startNewConversation();

        resp = getWebResponse(wc, uri);

        assertEquals(
                     msg("Response code was not OK", resp),
                     resp.getResponseCode(),
                     200);
        String s = resp.getText();
        int sindex = s.indexOf("error");
        int fIndex = s.indexOf("failure");

        if (sindex > 0 || fIndex > 0) {
            System.out.println(
                               testname
                               + "\n"
                               + resp.getURL()
                               + "\n error/failure messages seen");
        }

        sindex = s.indexOf("Result: ");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            result = s.substring(sindex, eindex);
        }

        if (debug) {
            HTODTest2 ht = new HTODTest2();
            ht.setResult(result);
            if (null != result) {
                ht.parse();
            }
        }

        return s;
    }

    private void resetCacheAndPMICounters(String sCacheName, String oCacheName) throws Exception {
        System.out.println("resetCacheAndPMICounters: servletCache=" + sCacheName + " objectCache=" + oCacheName);

        // Lo_It_Sp_TL_P1_G1_Id_It_CL_P2_G2_Ss_Aa_Ra_Rand_S/D
        String uri = URI + "??&clearBT=true&threads=0&resetPMI=true&savCount=false&delayAT=0&method=test1" +
                     "&cacheServletName=" + sCacheName +
                     "&cacheDmapName=" + oCacheName;

        getAndCheckResponse(uri, "resetCacheAndPMICounters");

    }

}
