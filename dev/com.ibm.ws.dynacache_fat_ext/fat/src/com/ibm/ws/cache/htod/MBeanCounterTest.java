// IBM Confidential OCO Source Material
// 5724-I63, 5724-H88, 5655-N01, 5733-W60 (C) COPYRIGHT International Business Machines Corp. 1997, 2005
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.htod;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.WebResponse;

public class MBeanCounterTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("MBeanCounterTest");

    /*
     * cache.instance.13=services/cache/servletInstance_13
     * cache.instance.13.cacheSize = 3
     * cache.instance.13.enableDiskOffload = true
     * cache.instance.13.enableServletSupport = true
     * cache.instance.13.diskCacheSize = 20
     * cache.instance.13.diskCachePerformanceLevel = 3
     */

    public static String URI = "/dynacachetests/MBeanCounterTestServlet";

    ServletCacheCounters counters = null;
    String uri = "";
    String result = "";
    boolean ok = true;

    /**
     * Test reads and writes
     */
    public void commonCodeTestReadWrite(int valuesize, int numputs, int numgets) throws Exception {
        String m = "MBeanCounterTest.testReadWrite";
        System.out.println(m + "...");
        String uri =
                        URI
                                        + "?method=putandget&valueSize="
                                        + valuesize
                                        + "&numputs="
                                        + numputs
                                        + "&numgets="
                                        + numgets
                                        + "&clearBT=true&cacheName=services/cache/servletInstance_13";

        WebResponse resp = counters.getresponse(m, uri);

        int cachesize = 3;

        // We could handle these cases correctly, but we don't right now
        assertTrue(m + ": ERROR - not enough puts for test", numputs >= cachesize);
        assertTrue(m + ": ERROR - not enough gets for test", numgets >= cachesize);
        assertTrue(m + ": ERROR - puts and gets are different", numgets == numputs);

        /*
         * Reasoning:
         * 
         * With a cache size of 3 and disk offload on, the first
         * 3 puts will go into memory cache, and every one after that
         * will result in cache entries being written to disk. (probably
         * the older ones, but it doesn't matter which ones).
         * 
         * Then when we get them back again, 3 will come from memory
         * and the rest from disk. -- assuming we get the same number that
         * we put (see assertion above).
         * 
         * Therefore we should see (numputs - 3) reads and (numgets - 3)
         * writes.*
         * 
         * *DISCLAIMER:
         * When a read is issued for a cache id on the disk, it is swapped to memory and an item
         * from memory is moved to disk. Therefore if items on the disk are read before items in
         * memory, the count = numgets/puts and NOT numgets/puts -3
         * 
         * Nothing is deleted in this test.
         */

        int expectedwrites = numputs; // this will range from [numgets - cachesize, numgets]
        int expectedreads = numgets; //this will range from [numgets - cachesize, numgets]

        /* check counters */
        checkCounters(m, valuesize, expectedreads, expectedwrites, 0);

        System.out.println(m + "...done");
    }

    /**
     * Test delete counters due to having to remove items
     * to make room for new ones.
     * 
     * @parm valuesize - size of objects in bytes
     * @throws Exception
     */
    public void commonCodeTestDeleteOverflow(int valuesize, int numputs) throws Exception {
        String m = "MBeanCounterTest.testDeleteOverflow";
        System.out.println(m + "...");
        String uri = URI + "?method=putandget&valueSize="
                     + valuesize
                     + "&numputs=" + numputs
                     + "&numgets=0"
                     + "&clearBT=true&cacheName=services/cache/servletInstance_13";
        WebResponse resp = counters.getresponse(m, uri);

        int cachesize = 3;
        int diskcachesize = 20;

        // We could handle these cases correctly, but we don't right now
        assertTrue(m + ": ERROR - not enough puts for test", numputs >= cachesize);

        System.out.println("numputs:" + numputs);
        System.out.println("cachesize:" + cachesize);
        System.out.println("diskcachesize:" + diskcachesize);

        int expectedwrites =
                        (diskcachesize < (numputs - cachesize)
                                        ? diskcachesize
                                        : (numputs - cachesize));
        int expecteddeletes = 0; //nothing has been invalidated or timedout from the disk

        /* check counters */
        checkCounters(m, valuesize, 0, expectedwrites, expecteddeletes);

        System.out.println(m + "...done");
    }

    public void commonCodeTestDeleteTimeout(int valuesize) throws Exception {
        String m = "MBeanCounterTest.commonCodeTestDeleteTimeout";
        System.out.println(m + "...");
        int numputs = 9;
        int numgets = 9;
        int ttl = 5; // seconds
        int delayAfterPut = 10; // seconds
        int delayAfterGet = 20; // seconds

        WebResponse resp = counters.getresponse(m, URI + "?method=putandget"
                                                   + "&valueSize=" + valuesize
                                                   + "&numputs=" + numputs
                                                   + "&numgets=" + numgets
                                                   + "&delayAfterPut=" + delayAfterPut
                                                   + "&delayAfterGet=" + delayAfterGet
                                                   + "&timeToLive=" + ttl
                                                   + "&clearBT=true"
                                                   + "&cacheName=services/cache/servletInstance_13");

        int cachesize = 3;
        int diskCacheSize = 20;

        assertTrue(m + ": ERROR - puts and gets are different", numgets == numputs);

        /*
         * Reasoning:
         * 
         * With a timetolive of 5 seconds, if we try a get on them after
         * 5 seconds (or make it 10 just to be safe), they'll get invalidated
         * and added to the invalidation buffer.
         * 
         * The invalidation buffer is checked every 10 seconds to see if anything
         * needs deleting.
         * 
         * So if we wait another 20 seconds, they should have been deleted
         * from disk, so they should all have updated the DeletedFromDisk counters.
         * 
         * Note that any of this might happen faster, but all we care about here
         * is that the delete has definitely happened before we check the counter
         * at the end of the test.
         */

        int expectedwrites =
                        (diskCacheSize < (numputs - cachesize)
                                        ? diskCacheSize
                                        : (numputs - cachesize));
        int expectedreads = 0;
        int expecteddeletes = expectedwrites; //we timeout everything we have written to the disk

        /* check counters */
        checkCounters(m, valuesize, expectedreads, expectedwrites, expecteddeletes);

        System.out.println(m + "...done");
    }

    /**
     * Test deleting by invalidating by cache ID.
     * 
     * @param valuesize
     * @throws Exception
     */
    public void commonCodeTestDeleteInvalidate(int valuesize, boolean bydepid, boolean usetemplate) throws Exception {
        String m = "MBeanCounterTest.commonCodeTestDeleteInvalidate";
        System.out.println(m + "...");
        int numputs = 10;
        int numgets = 10;
        int numinvalidates = 10;
        boolean onedepid = bydepid;

        String uri = URI + "?method=putandget"
                     + "&valueSize=" + valuesize
                     + "&numputs=" + numputs
                     + "&numgets=" + numgets
                     + "&numinvalidates=" + numinvalidates
                     + "&clearBT=true"
                     + "&oneDepid=" + onedepid
                     + "&useTemplate" + usetemplate
                     + "&cacheName=services/cache/servletInstance_13"
                     + "&delayAfterInvalidate=45";

        WebResponse resp = counters.getresponse(m, uri);

        int cachesize = 3;
        int diskCacheSize = 20;

        assertTrue(m + ": ERROR - puts and gets are different", numgets == numputs);
        if (bydepid && usetemplate) {
            fail(m + ": ERROR - Cannot use both one dep id and template in same test");
        }

        /*
         * Reasoning:
         * 
         * Most of the puts will go into the disk.
         * When we invalidate them, they'll
         * all get deleted.
         */

        // since items get swapped from disk to memory 
        // no. of writes on disk = cachesize + no. of writes to disk
        // no. of reads  from disk = cachesize + no. of writed to disk 

        int expectedwrites = numputs;
        int expectedreads = numgets;
        int expecteddeletes = numputs;

        /* check counters */
        checkCounters(m, valuesize, expectedreads, expectedwrites, expecteddeletes);

        System.out.println(m + "...done");
    }

    /**
     * Test controller. All the tests call this method with varying data
     * sizes
     * 
     * @param size
     * @throws Exception
     */
    public void runTestsForSize(int size) throws Exception {
        setupTest();
        commonCodeTestReadWrite(size, 20, 20);
        commonCodeTestDeleteOverflow(size, 20);
        commonCodeTestDeleteTimeout(size);
        commonCodeTestDeleteInvalidate(size, false, false); // invalidate by cache id
        commonCodeTestDeleteInvalidate(size, true, false); // invalidate by dep id
        commonCodeTestDeleteInvalidate(size, false, true); // invalidate by template

    }

    public void setupTest() throws Exception {

        //check and enable PMI counters and if possible reset them to zero

        String m = "\n\n MBeanCounterTest.setupTest";
        counters = new ServletCacheCounters();

        //DynaCachePMIClient dpmic = new DynaCachePMIClient(TestConfig.host, TestConfig.soapPort, TestConfig.serverRoot);
        //SHARED_SERVER.restartServer();

        /* check counters */
        checkCounters(m, 0, 0, 0, 0);

        String uri = URI + "?method=emptytest&cacheName=services/cache/servletInstance_13";
        counters.getresponse(m, uri);

        System.out.println(m + "...done");
    }

    /*
     * Run tests with objects of sizes that fall into each category --
     * be sure to test the boundaries too.
     * 
     * Boundary checking which was employed in the earlier versions of
     * this test was later because of no sure way of figuring out the
     * overhead imposed by Dynacache during serialization
     * 
     * 
     * ALL BOUNDARY checking and SIZE checking is removed. Until DC
     * exposes a method which tells us what the size of the serialized
     * object is there is no way to guess the size occupied and hence
     * we cannot figure out totalread, totalwrite and totaldelete sizes
     */

    // < 4K
    public void tReadWriteDelete1() throws Exception {
        System.out.println("runTestsForSize(512)");
        runTestsForSize(512);
    }

    // >= 4K, < 40K
    public void tReadWriteDelete4000() throws Exception {
        System.out.println("runTestsForSize((5120))");
        runTestsForSize(5120);
    }

    // >= 40K, < 400K
    public void tReadWriteDelete40000() throws Exception {
        System.out.println("runTestsForSize((51200))");
        runTestsForSize(51200);
    }

    // >= 400K
    public void tReadWriteDelete400000() throws Exception {
        System.out.println("runTestsForSize((512000))");
        runTestsForSize(512000);
    }

    /**
     * This method checks the counters for all the testcases
     */
    private void checkCounters(
                               String m,
                               int value,
                               int expectedreads,
                               int expectedwrites,
                               int expecteddeletes) {

        //HTOD adds atleast 512 bytes overhead and serializes on a 512 byte boundary
        double FiveHundredAndTwelveByteQuotient = Math.ceil((value + 512) / 512.00);
        double valuesize = FiveHundredAndTwelveByteQuotient * 512;

        // expected read values
        int read4K = 0, read40K = 0, read400K = 0, read4000K = 0;

        if (valuesize <= 4000)
            read4K = expectedreads;
        if (valuesize > 4000 && valuesize <= 40000)
            read40K = expectedreads;
        if (valuesize > 40000 && valuesize <= 400000)
            read400K = expectedreads;
        if (valuesize > 400000 && valuesize <= 4000000)
            read4000K = expectedreads;

        // expected write values
        int write4K = 0, write40K = 0, write400K = 0, write4000K = 0;

        if (valuesize <= 4000)
            write4K = expectedwrites;
        if (valuesize > 4000 && valuesize <= 40000)
            write40K = expectedwrites;
        if (valuesize > 40000 && valuesize <= 400000)
            write400K = expectedwrites;
        if (valuesize > 400000 && valuesize <= 4000000)
            write4000K = expectedwrites;

        // expected delete values
        int delete4K = 0, delete40K = 0, delete400K = 0, delete4000K = 0;

        if (valuesize <= 4000)
            delete4K = expecteddeletes;
        if (valuesize > 4000 && valuesize <= 40000)
            delete40K = expecteddeletes;
        if (valuesize > 40000 && valuesize <= 400000)
            delete400K = expecteddeletes;
        if (valuesize > 400000 && valuesize <= 4000000)
            delete4000K = expecteddeletes;

        counters.check(m, "sObjectsReadFromDisk", expectedreads);
        counters.check(m, "sObjectsReadFromDisk4K", read4K);
        counters.check(m, "sObjectsReadFromDisk40K", read40K);
        counters.check(m, "sObjectsReadFromDisk400K", read400K);
        counters.check(m, "sObjectsReadFromDisk4000K", read4000K);
        long expectedReadSize = (long) (expectedreads * valuesize);
        //40% variation permitted since there is no way for us ot know for sure
        counters.check(m, "sObjectsReadFromDiskSize", expectedReadSize, 1.4 * expectedReadSize);

        counters.check(m, "sObjectsWriteToDisk", expectedwrites);
        counters.check(m, "sObjectsWriteToDisk4K", write4K);
        counters.check(m, "sObjectsWriteToDisk40K", write40K);
        counters.check(m, "sObjectsWriteToDisk400K", write400K);
        counters.check(m, "sObjectsWriteToDisk4000K", write4000K);
        long expectedWriteSize = (long) (expectedwrites * valuesize);
        counters.check(m, "sObjectsWriteToDiskSize", expectedWriteSize, 1.4 * expectedWriteSize);

        counters.check(m, "sObjectsDeleteFromDisk", expecteddeletes);
        counters.check(m, "sObjectsDeleteFromDisk4K", delete4K);
        counters.check(m, "sObjectsDeleteFromDisk40K", delete40K);
        counters.check(m, "sObjectsDeleteFromDisk400K", delete400K);
        counters.check(m, "sObjectsDeleteFromDisk4000K", delete4000K);
        long expectedDeleteSize = (long) (expecteddeletes * valuesize);
        counters.check(m, "sObjectsDeleteFromDiskSize", expectedDeleteSize, 1.4 * expectedDeleteSize);

    }

    @Test
    public void testAll() throws Exception {
        //tReadWriteDelete1();
        tReadWriteDelete4000();
        //tReadWriteDelete40000();
        //tReadWriteDelete400000();
    }

}