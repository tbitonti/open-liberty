// 1.27, 5/17/05m
// IBM Confidential OCO Source Material
// 5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.TestConfig;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class MiscTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("MiscTest");

    @Test
    public void testJapan() throws Exception {
        clearCache();
        String URI = "/dynacachetests/japan.jsp";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);

        //send 2 successive requests.  should get a miss and a hit
        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        String ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);
        assertNotNull(msg("japan: missing TimeStamp in 1st invocation", resp1), ts1);
        assertNotNull(msg("japan: missing TimeStamp in 2nd invocation", resp2), ts2);
        assertEquals(msg("japan test, stamps weren't equal", resp2), ts1, ts2);
    }

    @Test
    public void testTimeout() throws Exception {
        String URI = "/dynacachetests/TimeoutPos.jsp";
        WebConversation wc = startNewConversation();
        //depends on a 10 second timeout
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);

        //send 2 successive requests.  should get a miss and a hit
        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);
        synchronized (this) {
            wait(2000);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);

        assertEquals(msg("1st pass of TimeoutPos test, and the stamps weren't equal", resp2), ts1, ts2);

        synchronized (this) {
            wait(15000);
        }

        resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);
        if (ts1.equals(ts2)) {
            fail(msg("TimeStamps were equal, should have gotten a timeout -> regened stamp", resp2));
        }

        resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);
        assertEquals(msg("2nd pass of TimeoutPos test, and the stamps weren't equal", resp1), ts1, ts2);
    }

    @Test
    public void testCloseWriter() throws Exception {
        String URI = "/dynacachetests/CloseAndFlush?action=closewriter";
        WebConversation wc = startNewConversation();
        //send 2 successive requests.  should get a miss and a hit
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);

        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        String ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);

        assertEquals(msg("testCloseWriter: Timestamps weren't equal", resp2), ts1, ts2);
    }

    @Test
    public void testFlushWriter() throws Exception {
        String URI = "/dynacachetests/CloseAndFlush?action=flushwriter";
        WebConversation wc = startNewConversation();
        //send 2 successive requests.  should get a miss and a hit
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("TimeStamp1").getCellAsText(0, 0);
        String ts2 = resp1.getTableWithID("TimeStamp2").getCellAsText(0, 0);

        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);

        assertEquals(msg("testFlushWriter: Timestamps weren't equal", resp2), ts1, resp2.getTableWithID("TimeStamp1").getCellAsText(0, 0));
        assertEquals(msg("testFlushWriter: Timestamps weren't equal", resp2), ts2, resp2.getTableWithID("TimeStamp2").getCellAsText(0, 0));
    }

    @Test
    public void testCloseStream() throws Exception {
        String URI = "/dynacachetests/CloseAndFlush?action=closestream";
        WebConversation wc = startNewConversation();
        //send 2 successive requests.  should get a miss and a hit
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getText();

        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getText();

        assertEquals(msg("testCloseStream: bodies weren't equal", resp2), ts1, ts2);
    }

    @Test
    public void testFlushStream() throws Exception {
        String URI = "/dynacachetests/CloseAndFlush?action=flushstream";
        WebConversation wc = startNewConversation();
        //send 2 successive requests.  should get a miss and a hit
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getText();

        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getText();

        assertEquals(msg("testFlushStream: bodies weren't equal", resp2), ts1, ts2);
    }

    @Test
    public void testCrossWebAppInclude() throws Exception {
        String URI = "/dynacachetests/moduleA/CrossWebApp1";
        StringBuffer sb = new StringBuffer(URI);
        WebConversation wc = startNewConversation();

        //C->C->C, check miss & hit
        WebResponse resp1 = getWebResponse(wc, sb.append("?cache1=yes&cache2=yes").toString());
        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, sb.toString());
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);

        String[][] timestamps = new String[2][3];
        String[][] contexts = new String[2][3];
        int MISS = 0;
        int HIT = 1;

        for (int i = 0; i < 3; i++) {
            timestamps[MISS][i] = resp1.getTableWithID("timestamp" + (i + 1)).getCellAsText(0, 0);
            contexts[MISS][i] = resp1.getTableWithID("context_path" + (i + 1)).getCellAsText(0, 0);
            timestamps[HIT][i] = resp2.getTableWithID("timestamp" + (i + 1)).getCellAsText(0, 0);
            contexts[HIT][i] = resp2.getTableWithID("context_path" + (i + 1)).getCellAsText(0, 0);
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(msg("CrossWebAppInclude: C-C-C: timestamp" + (i + 1), resp2), timestamps[MISS][i], timestamps[HIT][i]);
            assertEquals(msg("CrossWebAppInclude: C-C-C: context_path" + (i + 1), resp2), contexts[MISS][i], contexts[HIT][i]);
        }
        assertEquals(msg("CrossWebAppInclude: C-C-C: context_path for 1 and 3", resp1), contexts[MISS][0], contexts[MISS][2]);
        assertTrue(msg("CrossWebAppInclude: C-C-C: context_path for 1 and 2", resp1), contexts[MISS][1] != contexts[MISS][2]);

        //U->C->C, miss & hit
        sb.setLength(URI.length());
        resp1 = getWebResponse(wc, sb.append("?cache1=no&cache2=yes").toString());
        synchronized (this) {
            wait(25);
        }
        resp2 = getWebResponse(wc, sb.toString());
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        for (int i = 0; i < 3; i++) {
            timestamps[MISS][i] = resp1.getTableWithID("timestamp" + (i + 1)).getCellAsText(0, 0);
            contexts[MISS][i] = resp1.getTableWithID("context_path" + (i + 1)).getCellAsText(0, 0);
            timestamps[HIT][i] = resp2.getTableWithID("timestamp" + (i + 1)).getCellAsText(0, 0);
            contexts[HIT][i] = resp2.getTableWithID("context_path" + (i + 1)).getCellAsText(0, 0);
        }
        for (int i = 0; i < 3; i++) {
            if (i != 0)
                assertEquals(msg("CrossWebAppInclude: U-C-C: timestamp" + (i + 1), resp2), timestamps[MISS][i], timestamps[HIT][i]);
            assertEquals(msg("CrossWebAppInclude: U-C-C: context_path" + (i + 1), resp2), contexts[MISS][i], contexts[HIT][i]);
        }
        assertEquals(msg("CrossWebAppInclude: U-C-C: context_path for 1 and 3", resp1), contexts[MISS][0], contexts[MISS][2]);
        assertTrue(msg("CrossWebAppInclude: U-C-C: context_path for 1 and 2", resp1), contexts[MISS][1] != contexts[MISS][2]);
        //C->U->C, miss & hit
        //send 2 successive requests.  should get a miss and a hit
        sb.setLength(URI.length());
        resp1 = getWebResponse(wc, sb.append("?cache1=yes&cache2=no").toString());
        synchronized (this) {
            wait(25);
        }
        resp2 = getWebResponse(wc, sb.toString());
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        for (int i = 0; i < 3; i++) {
            if (i != 0)
                assertEquals(msg("CrossWebAppInclude: C-U-C: timestamp" + (i + 1), resp2), timestamps[MISS][i], timestamps[HIT][i]);
            assertEquals(msg("CrossWebAppInclude: C-U-C: context_path" + (i + 1), resp2), contexts[MISS][i], contexts[HIT][i]);
        }
        assertEquals(msg("CrossWebAppInclude: C-U-C: context_path for 1 and 3", resp1), contexts[MISS][0], contexts[MISS][2]);
        assertTrue(msg("CrossWebAppInclude: C-U-C: context_path for 1 and 2", resp1), contexts[MISS][1] != contexts[MISS][2]);

    }

    //test side effects
    @Test
    public void testSendRedirect() throws Exception {
        String URI = "/dynacachetests/BasicServlet?action=sendRedirect";
        WebConversation wc = startNewConversation();
        //send 2 successive requests.  should get a miss and a hit
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);
        assertNotNull(msg("testSendRedirect: should have gotten back to TimeStamp", resp1), ts1);
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);
        assertNotNull(msg("testSendRedirect: should have gotten back to TimeStamp", resp2), ts2);
    }

    @Test
    public void testCookie() throws Exception {
        String URI = "/dynacachetests/BasicServlet?action=cookie";

        ASynchWebRequest as1 = new ASynchWebRequest(TestConfig.getBaseURL() + URI);
        ASynchWebRequest as2 = new ASynchWebRequest(TestConfig.getBaseURL() + URI);

        as1.start();
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        as2.start();

        WebResponse resp1 = as1.getWebResponse();
        WebResponse resp2 = as2.getWebResponse();
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);

        String head1 = as1.wc.getCookieValue("testCookie");
        String head2 = as2.wc.getCookieValue("testCookie");

        assertEquals(msg("testCookie: cookie values not present or not equal", resp2), head1, head2);
    }

    @Test
    public void testLocale() throws Exception {
        String URI = "/dynacachetests/BasicServlet?action=locale";

        ASynchWebRequest as1 = new ASynchWebRequest(TestConfig.getBaseURL() + URI);
        ASynchWebRequest as2 = new ASynchWebRequest(TestConfig.getBaseURL() + URI);

        as1.wc.setHeaderField("Accept-Language", "da");
        as2.wc.setHeaderField("Accept-Language", "en-gb");

        as1.start();
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        as2.start();

        WebResponse resp1 = as1.getWebResponse();
        WebResponse resp2 = as2.getWebResponse();
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);

        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);
        String ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);

        assertTrue(msg("testLocale: timestamps shouldn't be equal for different locales", resp2), ts1 != ts2);
        resp1 = getWebResponse(as1.wc, URI);
        resp2 = getWebResponse(as2.wc, URI);
        String ts3 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);
        String ts4 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);
        assertEquals(msg("testLocale: timestamps should be equal for same locale on cache hit", resp1), ts1, ts3);
        assertEquals(msg("testLocale: timestamps should be equal for same locale on cache hit", resp2), ts2, ts4);

    }

    @Test
    public void testFilter() throws Exception {
        String URI = "/dynacachetests/BasicServlet?action=filter";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);

        String resp = resp1.getText().trim();
        assertTrue(msg("filter did not preprocess", resp1), resp.startsWith("...preprocessing..."));
        assertTrue(msg("filter did not postprocess", resp1), resp.endsWith("...postprocessing..."));
        //send 2 successive requests.  should get a miss and a hit
        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);
        synchronized (this) {
            wait(25);
        }
        resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);

        resp = resp1.getText().trim();
        assertTrue(msg("filter did not preprocess on cache hit", resp1), resp.startsWith("...preprocessing..."));
        assertTrue(msg("filter did not postprocess on cache hit", resp1), resp.endsWith("...postprocessing..."));
        //send 2 successive requests.  should get a miss and a hit
        String ts2 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);

        assertEquals(msg("testFilter: stamps weren't equal", resp1), ts1, ts2);
    }

    //global consume-fragments property
    @Test
    public void testConsumeSubfragments() throws Exception {
        clearCache();
        String URI = "/dynacachetests/consumeParent.jsp";
        for (int i = 0; i < 100; i++) {
            WebConversation wc = startNewConversation();
            WebResponse resp1 = getWebResponse(wc, URI);
            assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
            String ts1 = resp1.getTableWithID("test").getCellAsText(1, 0).trim();
            assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);
            synchronized (this) {
                wait(25);
            }
            WebResponse resp2 = getWebResponse(wc, URI);
            assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
            String ts2 = resp2.getTableWithID("test").getCellAsText(1, 0).trim();
            assertTrue(msg("time stamp was empty", resp2), ts2.length() != 0);
            assertEquals(msg("time stamps did not match, consume subfragments failed", resp2), ts1, ts2);
        }
    }

    //cache-id consume-fragments property
    @Test
    public void testConsumeSubfragments2() throws Exception {
        String URI = "/dynacachetests/consumeParent2.jsp";
        clearCache();
        for (int i = 0; i < 100; i++) {
            WebConversation wc = startNewConversation();
            WebResponse resp1 = getWebResponse(wc, URI);
            assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
            String ts1 = resp1.getTableWithID("test").getCellAsText(1, 0).trim();
            assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);
            synchronized (this) {
                wait(25);
            }
            WebResponse resp2 = getWebResponse(wc, URI);
            assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
            String ts2 = resp2.getTableWithID("test").getCellAsText(1, 0).trim();
            assertTrue(msg("time stamp was empty", resp2), ts2.length() != 0);
            assertEquals(msg("time stamps did not match, consume subfragments failed", resp2), ts1, ts2);
        }
    }

    //global consume-fragments property wit
    @Test
    public void testConsumeSubfragmentForward() throws Exception {
        String URI = "/dynacachetests/consumeParentForward.jsp";
        clearCache();
        for (int i = 0; i < 100; i++) {
            WebConversation wc = startNewConversation();
            WebResponse resp1 = getWebResponse(wc, URI);
            assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
            String ts1 = resp1.getTableWithID("test").getCellAsText(0, 0).trim();
            assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);
            synchronized (this) {
                wait(25);
            }
            WebResponse resp2 = getWebResponse(wc, URI);
            assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
            String ts2 = resp2.getTableWithID("test").getCellAsText(0, 0).trim();
            assertTrue(msg("time stamp was empty", resp2), ts2.length() != 0);
            assertEquals(msg("time stamps did not match, consume subfragments failed", resp2), ts1, ts2);
        }
    }

    //save-attributes property
    @Test
    public void testSaveAttributes() throws Exception {
        clearCache();
        String URI = "/dynacachetests/SaveAttributesParent.jsp";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI + "?arg1=val1");

        //send 2 successive requests.  should get a miss and a hit
        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        WebResponse resp2 = getWebResponse(wc, URI + "?arg1=val1");
        String ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);
        if (ts2 != null) {
            ts2.trim();
        }
        assertNotNull(msg("SaveAttributes: missing TimeStamp in 1st invocation", resp1), ts1);
        assertTrue(msg("SaveAttributes: found TimeStamp in 2nd invocation", resp2), ts2.contains("null"));

        // cleaning up
        resp2 = getWebResponse(wc, URI + "?inv=val1");
    }

    @Test
    public void testSaveAttributesTrueExclude() throws Exception {
        clearCache();
        String URI = "/dynacachetests/SavAttrParent/TrueExclude";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);

        //send 2 successive requests.  should get a miss and a hit
        String ts1 = resp1.getTableWithID("att1").getCellAsText(0, 0);
        assertNotNull(msg("SaveAttributesTrueExclude: missing att1 in 1st invocation", resp1), ts1);

        String ts2 = resp1.getTableWithID("att2").getCellAsText(0, 0);
        assertNotNull(msg("SaveAttributesTrueExclude: missing att2 in 1st invocation", resp1), ts2);

        String ts3 = resp1.getTableWithID("att3").getCellAsText(0, 0);
        assertNotNull(msg("SaveAttributesTrueExclude: missing att3 in 1st invocation", resp1), ts3);

        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        String ts11 = resp2.getTableWithID("att1").getCellAsText(0, 0);
        assertTrue(msg("SaveAttributesTrueExclude: found att1 in 2nd invocation", resp2), ts11.equals("null"));

        String ts22 = resp2.getTableWithID("att2").getCellAsText(0, 0);
        assertNotNull(msg("SaveAttributesTrueExclude: missing att2 in 1st invocation", resp2), ts22);
        assertTrue(msg("SaveAttributesTrueExclude: found att2 in 2nd invocation", resp2), ts22.equals(ts2));

        String ts33 = resp2.getTableWithID("att3").getCellAsText(0, 0);
        assertNotNull(msg("SaveAttributesTrueExclude: missing att2 in 1st invocation", resp2), ts33);
        assertTrue(msg("SaveAttributesTrueExclude: found att3 in 2nd invocation", resp2), ts33.equals(ts3));

    }

    @Test
    public void testSaveAttributesFalseExclude() throws Exception {
        clearCache();
        String URI = "/dynacachetests/SavAttrParent/FalseExclude";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);

        //send 2 successive requests.  should get a miss and a hit
        String ts1 = resp1.getTableWithID("att1").getCellAsText(0, 0);
        assertNotNull(msg("SaveAttributesTrueExclude: missing att1 in 1st invocation", resp1), ts1);

        String ts2 = resp1.getTableWithID("att2").getCellAsText(0, 0);
        assertNotNull(msg("SaveAttributesTrueExclude: missing att2 in 1st invocation", resp1), ts2);

        String ts3 = resp1.getTableWithID("att3").getCellAsText(0, 0);
        assertNotNull(msg("SaveAttributesTrueExclude: missing att3 in 1st invocation", resp1), ts3);

        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        String ts11 = resp2.getTableWithID("att1").getCellAsText(0, 0);
        assertNotNull(msg("SaveAttributesTrueExclude: missing att2 in 1st invocation", resp2), ts11);
        assertTrue(msg("SaveAttributesTrueExclude: found att2 in 2nd invocation", resp2), ts11.equals(ts1));

        String ts22 = resp2.getTableWithID("att2").getCellAsText(0, 0);
        assertTrue(msg("SaveAttributesTrueExclude: found att1 in 2nd invocation", resp2), ts22.equals("null"));

        String ts33 = resp2.getTableWithID("att3").getCellAsText(0, 0);
        assertTrue(msg("SaveAttributesTrueExclude: found att1 in 2nd invocation", resp2), ts33.equals("null"));

    }

    @Test
    public void testCacheSpecsScope() throws Exception {
        clearCache();
        String URI = "/dynacachetests/moduleB/CrossWebApp4";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);

        //send 2 successive requests.  Should get 2 misses
        String ts1 = resp1.getTableWithID("timestamp4").getCellAsText(0, 0);
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        String ts2 = resp2.getTableWithID("timestamp4").getCellAsText(0, 0);
        assertNotNull(msg("testCacheSpecsScope: missing TimeStamp in 1st invocation", resp1), ts1);
        assertNotNull(msg("testCacheSpecsScope: missing TimeStamp in 2nd invocation", resp2), ts2);
        assertTrue(msg("testCacheSpecsScope: timestamps shouldn't be equal. Incorrect cache policy used. Cached using cachespec from a different web module.", resp2), ts1 != ts2);
    }

    @Test
    public void testReqAttIter() throws Exception {
        clearCache();
        String URI = "/dynacachetests/ReqAttA.jsp";

        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);

        for (int i = 0; i < 5; i++) {
            String ts1 = resp1.getTableWithID("ReqAttr").getCellAsText(i, 0).trim();
            assertTrue(msg("ReqAttr was empty", resp1), ts1.length() != 0);
            String ts2 = resp1.getTableWithID("ReqAttr").getCellAsText(i, 1).trim();
            assertTrue(msg("ReqAttr was empty", resp1), ts2.length() != 0);
            assertEquals(msg("ReqAttr did not match, testReqAttIter failed", resp1), ts1, ts2.trim());
        }
    }

    @Test
    public void testDynamicContentProvider() throws Exception {

        clearCache();
        String URI = "/dynacachetests/DCPGrandParent";

        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);

        String grandparent_1 = resp1.getTableWithID("DCPGrandParent1").getCellAsText(0, 0).trim();
        String parent_1 = resp1.getTableWithID("DCPParent1").getCellAsText(0, 0).trim();
        String child_1 = resp1.getTableWithID("DCP1").getCellAsText(0, 0).trim();
        String dynamic_1 = resp1.getTableWithID("DynamicContent").getCellAsText(0, 0).trim();

        assertTrue(msg("DCPGrandParent1 was empty", resp1), grandparent_1.length() != 0);
        assertTrue(msg("DCPParent1 was empty", resp1), parent_1.length() != 0);
        assertTrue(msg("DCP1 was empty", resp1), child_1.length() != 0);
        assertTrue(msg("DynamicContent was empty", resp1), child_1.length() != 0);

        synchronized (this) {
            wait(5);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String grandparent_2 = resp1.getTableWithID("DCPGrandParent1").getCellAsText(0, 0).trim();
        String parent_2 = resp1.getTableWithID("DCPParent1").getCellAsText(0, 0).trim();
        String child_2 = resp1.getTableWithID("DCP1").getCellAsText(0, 0).trim();
        String dynamic_2 = resp1.getTableWithID("DynamicContent").getCellAsText(0, 0).trim();

        assertTrue(msg("DCPGrandParent1 was empty", resp1), grandparent_1.length() != 0);
        assertTrue(msg("DCPParent1 was empty", resp1), parent_1.length() != 0);
        assertTrue(msg("DCP1 was empty", resp1), child_1.length() != 0);
        assertTrue(msg("DynamicContent was empty", resp1), child_1.length() != 0);

        assertEquals(msg("DNCGrandParentTimeStamps did not match, DynamicContentProvider failed", resp2), grandparent_1, grandparent_2);
        assertEquals(msg("DNCParentTimeStamps did not match, DynamicContentProvider failed", resp2), parent_1, parent_2);
        assertEquals(msg("DNCChild1TimeStamps did not match, DynamicContentProvider failed", resp2), child_1, child_2);
        assertNotSame(msg("DynamicContent matched, DynamicContentProvider failed", resp2), dynamic_1, dynamic_2);
    }

    @Test
    public void testPreviewRequest_null() throws Exception {
        clearCache();
        String URI = "/dynacachetests/PreviewServlet";
        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String sts1 = resp1.getTableWithID("ServletTimeStamp").getCellAsText(0, 0);
        String cts1 = resp1.getTableWithID("CommandTimeStamp").getCellAsText(0, 0);

        synchronized (this) {
            wait(2000);
        }

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String sts2 = resp2.getTableWithID("ServletTimeStamp").getCellAsText(0, 0);
        String cts2 = resp2.getTableWithID("CommandTimeStamp").getCellAsText(0, 0);

        assertEquals(msg("ServletTimeStamps weren't equal with PreviewRequest not set", resp2), sts1, sts2);
        assertEquals(msg("CommandTimeStamps weren't equal with PreviewRequest not set", resp2), cts1, cts2);
    }

    @Test
    public void testPreviewRequest_true() throws Exception {
        clearCache();
        String URI = "/dynacachetests/PreviewServlet?previewRequest=true";
        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String sts1 = resp1.getTableWithID("ServletTimeStamp").getCellAsText(0, 0);
        String cts1 = resp1.getTableWithID("CommandTimeStamp").getCellAsText(0, 0);

        synchronized (this) {
            wait(2000);
        }

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String sts2 = resp2.getTableWithID("ServletTimeStamp").getCellAsText(0, 0);
        String cts2 = resp2.getTableWithID("CommandTimeStamp").getCellAsText(0, 0);

        assertFalse(msg("ServletTimeStamps were equal with PreviewRequest set to true", resp2), sts1.equals(sts2));
        assertFalse(msg("CommandTimeStamps were equal with PreviewRequest set to true", resp2), cts1.equals(cts2));
    }

    @Test
    public void testPreviewRequest_false() throws Exception {
        clearCache();
        String URI = "/dynacachetests/PreviewServlet?previewRequest=false";
        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String sts1 = resp1.getTableWithID("ServletTimeStamp").getCellAsText(0, 0);
        String cts1 = resp1.getTableWithID("CommandTimeStamp").getCellAsText(0, 0);

        synchronized (this) {
            wait(2000);
        }

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String sts2 = resp2.getTableWithID("ServletTimeStamp").getCellAsText(0, 0);
        String cts2 = resp2.getTableWithID("CommandTimeStamp").getCellAsText(0, 0);

        assertEquals(msg("ServletTimeStamps weren't equal with PreviewRequest set to false", resp2), sts1, sts2);
        assertEquals(msg("CommandTimeStamps weren't equal with PreviewRequest set to false", resp2), cts1, cts2);
    }

    @Test
    public void testIsUncacheable_true() throws Exception {
        clearCache();
        String URI = "/dynacachetests/IsUncacheable.jsp?cacheable=false";
        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String timestamp1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 1);
        String cacheable1 = resp1.getTableWithID("TimeStamp").getCellAsText(1, 1);

        synchronized (this) {
            wait(2000);
        }

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String timestamp2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 1);
        String cacheable2 = resp2.getTableWithID("TimeStamp").getCellAsText(1, 1);

        assertTrue(msg("TimeStamps were equal, request was cacheable", ""), !timestamp1.equals(timestamp2));
        assertTrue(msg("isUncacheable returned false for a non-cacheable fragment", ""), cacheable1.contains("true"));
    }

    @Test
    public void testIsUncacheable_false() throws Exception {
        clearCache();
        String URI = "/dynacachetests/IsUncacheable.jsp?cacheable=true";
        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String timestamp1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 1);
        String cacheable1 = resp1.getTableWithID("TimeStamp").getCellAsText(1, 1);

        synchronized (this) {
            wait(2000);
        }

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String timestamp2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 1);
        String cacheable2 = resp2.getTableWithID("TimeStamp").getCellAsText(1, 1);

        assertTrue(msg("TimeStamps were not equal, request was not cacheable", ""), timestamp1.equals(timestamp2));
        assertTrue(msg("isUncacheable returned true for a cacheable fragment", ""), cacheable1.contains("false"));
    }

    @Test
    public void testIgnoreGetPost_false() throws Exception {
        clearCache();
        String URI = "/dynacachetests/TimeStamp";
        WebConversation wc = startNewConversation();

        //Using HeadMethodWebRequest requires httpunit 1.6 to run correctly.
        //WebRequest headRequest = new HeadMethodWebRequest(HOST+URI);
        //WebResponse headResponse = wc.getResource(headRequest);

        WebRequest getRequest = new GetMethodWebRequest(TestConfig.getBaseURL() + URI);
        getRequest.setParameter("ignore", "false");
        WebResponse getResponse = wc.getResponse(getRequest);
        assertEquals(msg("Response code was not OK", getResponse), getResponse.getResponseCode(), 200);
        String getTimestamp = getResponse.getTableWithID("TimeStamp").getCellAsText(0, 0);

        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }

        WebRequest postRequest = new PostMethodWebRequest(TestConfig.getBaseURL() + URI);
        postRequest.setParameter("ignore", "false");
        WebResponse postResponse = wc.getResponse(postRequest);
        assertEquals(msg("Response code was not OK", postResponse), postResponse.getResponseCode(), 200);
        String postTimestamp = postResponse.getTableWithID("TimeStamp").getCellAsText(0, 0);

        assertTrue(msg("TimeStamps were equal, requestType was not appended to the cache-id", ""), !getTimestamp.equals(postTimestamp));
    }

    @Test
    public void testIgnoreGetPost_true() throws Exception {
        clearCache();
        String URI = "/dynacachetests/TimeStamp";
        WebConversation wc = startNewConversation();

        //Using HeadMethodWebRequest requires httpunit 1.6 to run correctly.
        //WebRequest headRequest = new HeadMethodWebRequest(HOST+URI);
        //WebResponse headResponse = wc.getResource(headRequest);

        WebRequest getRequest = new GetMethodWebRequest(TestConfig.getBaseURL() + URI);
        getRequest.setParameter("ignore", "true");
        WebResponse getResponse = wc.getResponse(getRequest);
        assertEquals(msg("Response code was not OK", getResponse), getResponse.getResponseCode(), 200);
        String getTimestamp = getResponse.getTableWithID("TimeStamp").getCellAsText(0, 0);

        WebRequest postRequest = new PostMethodWebRequest(TestConfig.getBaseURL() + URI);
        postRequest.setParameter("ignore", "true");
        WebResponse postResponse = wc.getResponse(postRequest);
        assertEquals(msg("Response code was not OK", postResponse), postResponse.getResponseCode(), 200);
        String postTimestamp = postResponse.getTableWithID("TimeStamp").getCellAsText(0, 0);

        assertTrue(msg("TimeStamps were not equal, requestType was appended to the cache-id", ""), getTimestamp.equals(postTimestamp));
    }

    @Test
    public void testDmapTimeout() throws Exception {
        String URI = "/dynacachetests/dmaptest?action=timeout";

        WebConversation wc = startNewConversation();

        WebRequest getRequest = new GetMethodWebRequest(TestConfig.getBaseURL() + URI);
        WebResponse getResponse = wc.getResponse(getRequest);
        assertEquals(msg("Response code was not OK", getResponse), getResponse.getResponseCode(), 200);
    }

}
