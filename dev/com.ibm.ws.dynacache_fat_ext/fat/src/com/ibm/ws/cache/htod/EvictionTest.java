// 1.8, 3/13/06
// IBM Confidential OCO Source Material
// 5724-I63, 5724-H88, 5655-N01, 5733-W60 (C) COPYRIGHT International Business Machines Corp. 1997, 2005
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
//
package com.ibm.ws.cache.htod;

import static junit.framework.Assert.fail;

import java.io.InputStream;
import java.util.Properties;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.cache.CacheConfig;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/** 
 * THESE 6 TESTS  RUN IN EXCESS OF 20 MINUTES.
 * For Liberty we will only test RANDOM AND SIZE FOR AN OBJECT CACHE
 */

/**
 * Info:
 * - This class is a junit test driver program.
 * - It tests Dynacache eviction policies for servlet and object caches.
 * - It communicates with the MBeanCounterTestServlet running in an AppServer.
 * MBeanCounterTestServlet is installed as part of dynacachetest.ear.
 * - This class is intended to be run using 'mantis EvictionTest'.
 * 
 * Setup:
 * - Specify the AppServer-under-test hostname and port in test.properties.
 * - Install dynacachetests.ear
 * - Define six cache instances in cacheinstances.properties:
 * servlet cache eviction_none
 * servlet cache eviction_random
 * servlet cache eviction_size
 * object cache eviction_none
 * object cache eviction_random
 * object cache eviction_size
 * - Specify the corresponding cache instance numbers in initSelf() below.
 * - Specify the directory location of cacheinstance.properties in constant
 * CACHE_INSTANCES_PROPS_FILENAME below. This is hard-coded below.
 * Please improve this in the future. (If you are running the tests
 * from the same machine as the appserver, then this may reference the
 * cacheinstance.properties file in the appserver install directory.
 * If you are running the tests on another machine, you must copy the
 * file to the testcase machine and keep it in sync with the server.)
 */
public class EvictionTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("EvictionTest");

    //-------------------------------------------------------------------------
    // Constants.
    //-------------------------------------------------------------------------

    /**
     * Indicates the Dynacache eviction thread cycle time.
     */
    private static final int EVICTION_CYCLE_TIME_MS = 15000;
    private static final String filesep = System.getProperty("file.separator");
    private static final int NUM_DEPENDENCY_FILES = 1;
    private static final int NUM_TEMPLATE_FILES = 1;
    private static final int NUM_BYTES_IN_A_GB = 1000000000;

    //-------------------------------------------------------------------------
    // Member variables.
    //-------------------------------------------------------------------------

    /**
     * Houses values read from the cacheinstances.properties file.
     */
    private Properties _cacheInstanceProperties;

    /**
     * Represents properties of servlet caches with various eviction policies.
     */
    private CacheInstanceProps _servletCacheNone;
    private CacheInstanceProps _servletCacheRandom;
    private CacheInstanceProps _servletCacheSize;

    /**
     * Represents properties of object caches with various eviction policies.
     */
    private CacheInstanceProps _objectCacheNone;
    private CacheInstanceProps _objectCacheRandom;
    private CacheInstanceProps _objectCacheSize;

    public static String URI = "/dynacachetests/MBeanCounterTestServlet";
    public final static int RESTART_WAIT_TIME = 180000;

    String uri = "";
    String result = "";

    /**
     * Represents statistics from dynacache.
     */
    ServletCacheCounters servletCounters = new ServletCacheCounters();

    //-------------------------------------------------------------------------
    // Public methods.  (mostly, anyway)
    //-------------------------------------------------------------------------

    public EvictionTest() {
        initSelf();
    }

    public static boolean stringin(String substring, String string) {
        return string.indexOf(substring) > 0;
    }

    /**
     * Tests a Servlet Cache with eviction policy NONE.
     */
//    @Test
//    public void testServletCacheNone() throws Exception {
//        runTestsEvictionBase(_servletCacheNone);
//    }

    /**
     * Tests an Object Cache with eviction policy NONE.
     */
//    @Test
//    public void testObjectCacheNone() throws Exception {
//        runTestsEvictionBase(_objectCacheNone);
//    }

    /**
     * Tests a Servlet Cache with eviction policy RANDOM.
     */
    @Test
    public void testServletCacheRandom() throws Exception {
        runTestsEvictionBase(_servletCacheRandom);
    }

    /**
     * Tests an Object Cache with eviction policy RANDOM.
     */
//    @Test
//    public void testObjectCacheRandom() throws Exception {
//        runTestsEvictionBase(_objectCacheRandom);
//    }

    /**
     * Tests a Servlet Cache with eviction policy SIZE_BASED.
     */
//  @Test
//    public void testServletCacheSize() throws Exception {
//        runTestsEvictionSize(_servletCacheSize);
//    }

    /**
     * Tests an Object Cache with eviction policy SIZE_BASED.
     */
//    @Test
//    public void testObjectCacheSize() throws Exception {
//        runTestsEvictionSize(_objectCacheSize);
//    }
    //-------------------------------------------------------------------------
    // Private methods.  (or some of them, anyway!)
    //-------------------------------------------------------------------------
    /**
     * Helper method to write to the console.
     */
    private void sop(String m, String msg) {
        Log.info(this.getClass(), "EvictionTest/", "m " + msg);
    }

    /**
     * Initializes member variables for this object.
     */
    private void initSelf() {
        String m = "initSelf: ";
        sop(m, "Entry.");

        // Read values from the cacheinstances.properties file.
        //
        initCacheInstanceProps();

        // Initialize variables to represent each cache instance.
        // Note: Cache numbers must correspond to values in cacheinstances.properties
        //
        _servletCacheNone = getCacheInstance(14);
        _servletCacheRandom = getCacheInstance(15);
        _servletCacheSize = getCacheInstance(16);

        _objectCacheNone = getCacheInstance(17);
        _objectCacheRandom = getCacheInstance(18);
        _objectCacheSize = getCacheInstance(19);

        sop(m, "Exit.");
    }

    /**
     * Reads values from the cacheinstance.properties file.
     */
    private void initCacheInstanceProps() {
        String m = "initCacheInstanceProps: ";

        Properties properties = new Properties();
        try {

            /**
             * Defines the installed location of the cacheinstances.properties file
             * to be read by this EvictionTest class on the tester machine.
             */

            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI + "?method=getServerRoot");

            String CACHE_INSTANCES_PROPS_FILENAME =
                            getClass()
                                            .getClassLoader()
                                            .getResource("cacheinstances.properties")
                                            .toString();
            sop(m, "CACHE_INSTANCES_PROPS_FILENAME: " + CACHE_INSTANCES_PROPS_FILENAME);

            InputStream inputStream =
                            getClass().getClassLoader().getResourceAsStream("cacheinstances.properties");

            properties.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            sop(m, "Exit. Error. Caught exception reading properties file. e=" + e);
            e.printStackTrace();
            return;
        }

        _cacheInstanceProperties = properties;
        // sop(m,"props=" + _cacheInstanceProperties);
        sop(m, "Exit. Success. Read properties from file.");
    }

    /**
     * Gets the specified cache instance with values from a properties file.
     */
    private CacheInstanceProps getCacheInstance(int cacheNumber) {
        return CacheInstanceProps.getInstance(cacheNumber, _cacheInstanceProperties);
    }

    /**
     * Tests a cache with eviction policy none.
     */
    private void runTestsEvictionBase(CacheInstanceProps props) throws Exception {
        String m = "runTestsEvictionBase: ";
        sop(m, "Entry.");

        setupTests();

        // Write one element to mem cache.
        //
        writeAndEvaluate(props, 1, 1);

        // Write elements to the mem cache, from 10% to 100% of max cache size.
        // 354206 - This test takes too long.  Do fewer loops, in 20% increments.
        //
        int tenPercentMem = props.cacheSize / 10;
        for (int i = 2; i <= 10; i = i + 2) {
            sop(m, "LOOP_CNT i=" + i);
            writeAndEvaluate(props, 1, (i * tenPercentMem));
        }

        // Fill the mem cache and write one element to the disk cache.
        //
        writeAndEvaluate(props, 1, (1 + props.cacheSize));

        // Fill the mem cache and write elements to the disk cache, 10% to 100%.
        // 354206 - This test takes too long.  Do fewer loops, in 20% increments.
        //
        int tenPercentDisk = props.diskCacheSize / 10;
        for (int i = 2; i <= 10; i = i + 2) {
            sop(m, "LOOP_COUNT i=" + i);
            writeAndEvaluate(props, 1, (props.cacheSize + (i * tenPercentDisk)));
        }

        // Overflow both caches.
        //
        writeAndEvaluate(props, 1, (1 + props.cacheSize + props.diskCacheSize));

        sop(m, "Exit.");
    }

    /**
     * Tests a cache with eviction policy size.
     */
    private void runTestsEvictionSize(CacheInstanceProps props) throws Exception {
        String m = "runTestsEvictionSize: ";
        sop(m, "Entry.");

        setupTests();

        // Fill the mem cache and write elements to the disk cache.
        // 354206 This test takes too long to run with 1GB cache size, so
        // just do it at the boundary values of 100k and 300K bytes.
        //
        for (int valueSizeBytes = 100000; valueSizeBytes <= 300000; valueSizeBytes += 200000) {

            //The following formula describes how to calculate the actual cache data size limit:
            //cache data size limit = disk cache size (in GB) - number of dependency files per GB - number of template files
            //ASSUMPTION: there is only *one* dependency and template file for each cache instance under test
            // 354206 New info from Andy Chow:  When testing an object cache, there is no template file.
            // Therefore the spaca available for objects is bigger than when testing a servlet cache.
            int realDiskCacheSizeInGB;
            if (props.enableServletSupport) {
                realDiskCacheSizeInGB = props.diskCacheSizeInGB - NUM_DEPENDENCY_FILES - NUM_TEMPLATE_FILES;
            } else {
                realDiskCacheSizeInGB = props.diskCacheSizeInGB - NUM_DEPENDENCY_FILES;
            }

            float maxElements = (realDiskCacheSizeInGB * NUM_BYTES_IN_A_GB) / valueSizeBytes;

            // 354206 These tests take way too much time.  Just run one test below eviction, and one above.
            //int tenPercentMaxElements = Math.round(maxElements / 10);
            //sop(m,"maxElements=" + maxElements + " tenPercentMaxElements=" + tenPercentMaxElements);
            //for (int i=1; i<=10; i++) {
            //    sop(m,"LOOP_COUNTS valueSizeBytes=" + valueSizeBytes + " i=" + i);
            //    writeAndEvaluate(props, valueSizeBytes, ((i * tenPercentMaxElements) + props.cacheSize));
            //}

            // Find the number of elements which corresponds to the low threshold.
            // Fill the disk cache with this many elements, and eviction should not happen.
            long numElementsLowThresh = Math.round(maxElements / 100.0) * props.diskCacheLowThreshold;
            sop(m, "BELOW_EVICTION valueSizeBytes=" + valueSizeBytes + " numElementsLowThresh=" + numElementsLowThresh);
            writeAndEvaluate(props, valueSizeBytes, (numElementsLowThresh + props.cacheSize));

            // Find the number of elements corresponding to halfway between the high threshold and a full cache.
            // Fill the disk cache with this many elements, and eviction should happen.
            long numElementsHighThreshPlus =
                            Math.round(maxElements / 100.0) *
                                            (props.diskCacheHighThreshold + ((100 - props.diskCacheHighThreshold) / 2));
            sop(m, "ABOVE_EVICTION valueSizeBytes=" + valueSizeBytes + " numElementsHighThreshPlus=" + numElementsHighThreshPlus);
            writeAndEvaluate(props, valueSizeBytes, (numElementsHighThreshPlus + props.cacheSize));
        }

        sop(m, "Exit.");
    }

    /**
     * Just make sure this is working - the emptytest
     * should do nothing, counters should all be zero.
     */
    private void setupTests() throws Exception {
        String m = "EvictionTest.setupTests: ";

        long sleepTime = 3456; // RESTART_WAIT_TIME;
        System.out.println(m + "Entry ... Not restarting server; just sleeping");
        System.out.println(m + "... Sleeping for " + (sleepTime / 1000) + " seconds.");
        Thread.sleep(sleepTime); //REMOVE this sleep when the notifications work in restartServer
        //Increase timeout here if u start seeing weird exceptions before the test begins
        System.out.println(m + "...Server restarted");

        String uri = URI + "?method=emptytest";
        System.out.println("uri=" + uri);
        servletCounters.getresponse(m, uri);

        /* check counters */
        servletCounters.check(
                              m, 0,
                              0, 0, 0,
                              0, 0, 0);

        System.out.println(m + "Exit ...done");
    }

    /**
     * Test writes into cache.
     * Verifies that the memory cache is filled first, and then the disk.
     */
    private void writeAndEvaluate(CacheInstanceProps cacheProps,
                                  int valueSizeBytes,
                                  long numputs) throws Exception {
        String m = "writeAndEvaluate: ";
        sop(m, "Entry. valueSizeBytes=" + valueSizeBytes +
               " numputs=" + numputs +
               " cacheName=" + cacheProps.name);

        //more diagnostics       
        long totalBytes = -1;
        if (numputs <= cacheProps.cacheSize) {
            totalBytes = valueSizeBytes * numputs;
            sop(m, " Writing " + totalBytes + " bytes to memory");
        } else {
            totalBytes = valueSizeBytes * (numputs - cacheProps.cacheSize);
            sop(m, " Writing " + (valueSizeBytes * cacheProps.cacheSize) + " bytes to memory");
            sop(m, " Writing " + totalBytes + " bytes to disk");
        }

        // Always wait to verify eviction is or is_not deleting objects from cache.
        //
        int delayAT = 2 * EVICTION_CYCLE_TIME_MS;

        // Send request to servlet and get response.
        String uri = URI + "?method=putandget" +
                     "&valueSize=" + valueSizeBytes +
                     "&numputs=" + numputs +
                     "&numgets=0" +
                     "&delayAT=" + delayAT +
                     "&clearBT=true" +
                     "&cacheName=" + cacheProps.name;
        WebResponse resp = servletCounters.getresponse(m, uri);

        //------------------------------------
        // Calculate expected values.
        //------------------------------------

        boolean exactMemMatchExpected = true;
        long expectedMemSize = -555;
        int minMemSize = -555;
        int maxMemSize = -555;

        boolean exactDiskMatchExpected = true;
        long expectedDiskSize = -555;
        double minDiskSize = -555;
        double maxDiskSize = -555;

        if (CacheConfig.EVICTION_NONE == cacheProps.diskCacheEvictionPolicy) {
            sop(m, "EVICTION_NONE. all exact.");
            exactMemMatchExpected = true;
            exactDiskMatchExpected = true;

            // Calculate expected values with no eviction.
            if (numputs <= cacheProps.cacheSize) {
                expectedMemSize = numputs;
                expectedDiskSize = 0;
            } else {
                expectedMemSize = cacheProps.cacheSize;
                long diskPuts = numputs - cacheProps.cacheSize;
                if (diskPuts <= cacheProps.diskCacheSize) {
                    expectedDiskSize = diskPuts;
                } else {
                    expectedDiskSize = cacheProps.diskCacheSize;
                }
            }
        } else if (CacheConfig.EVICTION_RANDOM == cacheProps.diskCacheEvictionPolicy) {
            sop(m, "EVICTION_RANDOM.");
            // Calculate expected values after eviction has been run.
            if (numputs <= cacheProps.cacheSize) {
                sop(m, "Fills mem cache only. Disk cache empty.");
                exactMemMatchExpected = true;
                expectedMemSize = numputs;
                exactDiskMatchExpected = true;
                expectedDiskSize = 0;
            } else {
                sop(m, "Fills mem cache and some disk cache.");
                exactMemMatchExpected = true;
                expectedMemSize = cacheProps.cacheSize;
                long diskPuts = numputs - cacheProps.cacheSize;
                // Convert the high threshold from percent to number of cache entries.
                int highPuts = (cacheProps.diskCacheHighThreshold * cacheProps.diskCacheSize) / 100;
                if (diskPuts <= highPuts) {
                    exactDiskMatchExpected = true;
                    expectedDiskSize = diskPuts;
                    sop(m, "exact=true. diskPuts=" + diskPuts);
                } else {
                    exactDiskMatchExpected = false;
                    // Convert the low threshold from percent to number of cache entries.
                    int lowPuts = (cacheProps.diskCacheLowThreshold * cacheProps.diskCacheSize) / 100;
                    minDiskSize = lowPuts - (lowPuts / 10);
                    maxDiskSize = highPuts + (highPuts / 10);
                    sop(m, "exact=false. diskPuts=" + diskPuts + " minDiskSize=" + minDiskSize + " maxDiskSize=" + maxDiskSize);
                }
            }
        } else if (CacheConfig.EVICTION_SIZE_BASED == cacheProps.diskCacheEvictionPolicy) {
            sop(m, "EVICTION_SIZE_BASED.");
            // Calculate expected values after eviction has been run.
            if (numputs <= cacheProps.cacheSize) {
                sop(m, "Fills mem cache only. Disk cache empty.");
                exactMemMatchExpected = true;
                expectedMemSize = numputs;
                exactDiskMatchExpected = true;
                expectedDiskSize = 0;
            } else {
                sop(m, "Fills mem cache and some disk cache.");
                exactMemMatchExpected = true;
                expectedMemSize = cacheProps.cacheSize;
                long diskPuts = numputs - cacheProps.cacheSize;

                //The following formula describes how to calculate the actual cache data size limit:
                //cache data size limit = disk cache size (in GB) - number of dependency files per GB - number of template files
                //ASSUMPTION : there is only *one* dependency and template file for each cache instance under test
                // 354206 New info from Andy Chow:  When testing an object cache, there is no template file.
                // Therefore the spaca available for objects is bigger than when testing a servlet cache.
                int realDiskCacheSizeInGB;
                if (cacheProps.enableServletSupport) {
                    realDiskCacheSizeInGB = cacheProps.diskCacheSizeInGB - NUM_DEPENDENCY_FILES - NUM_TEMPLATE_FILES;
                } else {
                    realDiskCacheSizeInGB = cacheProps.diskCacheSizeInGB - NUM_DEPENDENCY_FILES;
                }

                // Convert the high threshold from percent to cache size in GB.
                double highGB = (cacheProps.diskCacheHighThreshold * realDiskCacheSizeInGB) / 100.0;
                double lowGB = (cacheProps.diskCacheLowThreshold * realDiskCacheSizeInGB) / 100.0;

                // Calculate the space consumed by the specified number of elements.
                long totBytes = valueSizeBytes * numputs;
                // Check if they will fit in the disk cache.
                sop(m, "diskPuts=" + diskPuts + " highGB=" + highGB + " lowGB=" + lowGB + " totBytes=" + totBytes);

                if (totBytes < (highGB * NUM_BYTES_IN_A_GB)) {
                    exactDiskMatchExpected = true;
                    expectedDiskSize = diskPuts;
                    sop(m, "exact=true.");
                } else {
                    exactDiskMatchExpected = false;
                    // Convert the low threshold from percent to cache size in GB.

                    minDiskSize = (lowGB * NUM_BYTES_IN_A_GB) / valueSizeBytes;
                    maxDiskSize = (highGB * NUM_BYTES_IN_A_GB) / valueSizeBytes;
                    minDiskSize = minDiskSize - (minDiskSize / 10);
                    maxDiskSize = maxDiskSize + (maxDiskSize / 10);
                    sop(m, "exact=false. lowGB=" + lowGB + " minDiskSize=" + minDiskSize + " maxDiskSize=" + maxDiskSize);
                }
            }
        } else {
            fail(m + "Unrecognized policy value. " +
                 " policy=" + cacheProps.diskCacheEvictionPolicy +
                 " cacheName=" + cacheProps.name);
        }

        //------------------------------------
        // Compare expected vs actual values.
        //------------------------------------

        // Memory cache
        //
        if (exactMemMatchExpected) {
            // Exact check
            servletCounters.check(m, "sMemEntriesSize", expectedMemSize);
        } else {
            // Range check
            servletCounters.check(m, "sMemEntriesSize", minMemSize, maxMemSize);
        }

        // Disk cache
        //
        if (exactDiskMatchExpected) {
            // Exact check
            servletCounters.check(m, "sDiskEntriesSize", expectedDiskSize);
        } else {
            // Range check
            servletCounters.check(m, "sDiskEntriesSize", minDiskSize, maxDiskSize);
        }

        sop(m, "Exit.");
    }

}
