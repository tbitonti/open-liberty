// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5655-N01, 5733-W61 (C) COPYRIGHT International Business Machines Corp. 1997, 2006
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class NioMapTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("NioMapTest");

    private final boolean debug = true;
    public static String URI = "/dynacachetests/niomaptest";

    @Test
    public void testNioMap() throws Exception {

        final String methodName = "testNioMap";

        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        if (debug) {
            Log.info(this.getClass(), methodName, "DistributedNioMap basic test");
        }
        resp = getWebResponse(wc, URI + "?method=basic");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String s = resp.getText();
        Log.info(this.getClass(), methodName, s);
        int sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testNioMap.1 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }

        if (debug) {
            Log.info(this.getClass(), methodName, "DistributedNioMap basic object test");
        }
        resp = getWebResponse(wc, URI + "?method=basicObject");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        Thread.sleep(10000);
        s = resp.getText();
        Log.info(this.getClass(), methodName, s);
        sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testNioMap.2 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }
    }

    @Test
    public void testNioMapHTOD() throws Exception {
        final String methodName = "testNioMapHTOD";

        if (debug) {
            Log.info(this.getClass(), methodName, "DistributedNioMap HTOD object test");
        }
        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        resp = getWebResponse(wc, URI + "?method=htodObject");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        Thread.sleep(10000);
        String s = resp.getText();
        Log.info(this.getClass(), methodName, s);
        int sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testNioMapHTOD.1 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }
    }

    @Test
    public void testNioMapSkipMemoryWriteToDisk() throws Exception {

        final String methodName = "testNioMapSkipMemoryWriteToDisk";

        if (debug) {
            Log.info(this.getClass(), methodName, "DistributedNioMap Skip Memory and Write to Disk test");
        }
        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        resp = getWebResponse(wc, URI + "?method=htodSkipMemoryWriteToDisk");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        Thread.sleep(10000);
        String s = resp.getText();
        Log.info(this.getClass(), methodName, s);
        int sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testNioMapSMWTD.1 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }
    }

    @Test
    public void testCacheEntryOverflow() throws Exception {

        final String methodName = "testCacheEntryOverflow";
        if (debug) {
            Log.info(this.getClass(), methodName, "DistributedNioMap CacheEntry Overflow test");
        }
        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        resp = getWebResponse(wc, URI + "?method=cacheEntryOverflow");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        Thread.sleep(10000);
        String s = resp.getText();
        Log.info(this.getClass(), methodName, s);
        int sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testCacheEntryOverflow.1 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }
    }

    @Test
    public void testPutOnDiskException() throws Exception {
        final String methodName = "testPutOnDiskException";
        if (debug) {
            Log.info(this.getClass(), methodName, "DistributedNioMap PutOnDisk Exception test");
        }
        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        resp = getWebResponse(wc, URI + "?method=putOnDiskExceptionTest");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        Thread.sleep(10000);
        String s = resp.getText();
        Log.info(this.getClass(), methodName, s);
        int sindex = s.indexOf("Test failure:");
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            fail("testPutOnDiskException.1 - " + resp.getURL() + "\n" + s.substring(sindex, eindex));
        }
    }
}
