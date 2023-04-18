package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.websphere.simplicity.log.Log;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class DiscardTest extends ServletTestCase {

    private static final Class<?> c = DiscardTest.class;

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("DiscardTest");

    /**
     * This will only work if the jstl and standard.jar in the WAS runtime have the
     * prereq.jstl from defect PK47988
     */
    @Test
    public void testIKEA() throws Exception {

        String URI = "/dynacachetests/JSTLIKEA.jsp?test=test1";

        //ensure that the parent is cached
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, URI);
        Log.info(c, "testIKEA", msg("CacheMiss response", resp));
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String ts1 = resp.getTableWithID("JSTL Parent Begin").getCellAsText(0, 0); //cache miss

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        resp = getWebResponse(wc, URI);
        Log.info(c, "testIKEA", msg("CacheHit response", resp));
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String ts2 = resp.getTableWithID("JSTL Parent Begin").getCellAsText(0, 0);
        assertEquals(msg("testIKEA: Timestamps for Parent not equal", resp), ts1, ts2);

        //ensure that duplicate content is not seen on a cache hit
        String dnc = resp.getTableWithID("DNC").getCellAsText(0, 0);
        String testcache = resp.getTableWithID("testcache").getCellAsText(0, 0);
        String all = resp.getTableWithID("ALL").getCellAsText(0, 0);

        assertEquals(msg("testIKEA: Timestamps for Parent not equal", resp), ts1, ts2);
        assertEquals("testIKEA: Duplicate content", all.indexOf(testcache), all.lastIndexOf(testcache));
        assertEquals("testIKEA: Duplicate content", all.indexOf(dnc), all.lastIndexOf(dnc));
    }

    // Make sure content is discarded
    @Test
    public void testDiscard() throws Exception {

        String URI = "/dynacachetests/DiscardParent.jsp";

        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);

        System.out.println("In discard case");
        /*
         * Cache Miss case: All content should be there and be consumed/cached
         */
        String ts1 = resp1.getTableWithID("Parent").getCellAsText(0, 0);
        String ts2 = resp1.getTableWithID("A1").getCellAsText(0, 0);
        String ts3 = resp1.getTableWithID("A2").getCellAsText(0, 0);
        String ts4 = resp1.getTableWithID("B1").getCellAsText(0, 0);
        String ts5 = resp1.getTableWithID("B2").getCellAsText(0, 0);
        String ts6 = resp1.getTableWithID("C1").getCellAsText(0, 0);
        assertNotNull(msg("DiscardTest: missing TimeStamp in parent in 1st invocation", resp1), ts1);
        assertNotNull(msg("DiscardTest: missing TimeStamp in A1 in 1st invocation", resp1), ts2);
        assertNotNull(msg("DiscardTest: missing TimeStamp in A2 in 1st invocation", resp1), ts3);
        assertNotNull(msg("DiscardTest: missing TimeStamp in B1 in 1st invocation", resp1), ts4);
        assertNotNull(msg("DiscardTest: missing TimeStamp in B2 in 1st invocation", resp1), ts5);
        assertNotNull(msg("DiscardTest: missing TimeStamp in C1 in 1st invocation", resp1), ts6);

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        /*
         * Cache Hit case: Only A1, A2, and B2 should be present. Others (B1, C1) are discarded
         */
        WebResponse resp2 = getWebResponse(wc, URI);
        String ts21 = resp2.getTableWithID("Parent").getCellAsText(0, 0);
        String ts22 = resp2.getTableWithID("A1").getCellAsText(0, 0);
        String ts23 = resp2.getTableWithID("A2").getCellAsText(0, 0);
        String ts25 = resp2.getTableWithID("B2").getCellAsText(0, 0);
        String ts24, ts26;
        ts24 = ts26 = null;

        try {
            ts24 = resp2.getTableWithID("B1").getCellAsText(0, 0);
            ts26 = resp2.getTableWithID("C1").getCellAsText(0, 0);
        } catch (NullPointerException npe) {
        }

        // Make sure content that should be there is
        assertNotNull(msg("DiscardTest: missing TimeStamp in parent in 2nd invocation", resp2), ts21);
        assertNotNull(msg("DiscardTest: missing TimeStamp in A1 in 2nd invocation", resp2), ts22);
        assertNotNull(msg("DiscardTest: missing TimeStamp in A2 in 2nd invocation", resp2), ts23);
        assertNotNull(msg("DiscardTest: missing TimeStamp in B2 in 2nd invocation", resp2), ts25);

        // Make sure content that should not be present is not
        assertNull(msg("DiscardTest: Present TimeStamp in B1 in 2nd invocation", resp2), ts24);
        assertNull(msg("DiscardTest: Present TimeStamp in C1 in 2nd invocation", resp2), ts26);

        // Verify timestamps:
        assertEquals(msg("DiscardTest: Timestamps for Parent not equal", resp2), ts1, ts21);
        assertEquals(msg("DiscardTest: Timestamps for A1 not equal", resp2), ts2, ts22);
        assertEquals(msg("DiscardTest: Timestamps for A2 not equal", resp2), ts3, ts23);
        assertEquals(msg("DiscardTest: Timestamps for B2 not equal", resp2), ts5, ts25);
    }
}
