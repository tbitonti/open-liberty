package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * This test employs a PreInvalidationListenerImpl, that returns false for every
 * shouldInvalidate Request. The Junit here is simply a response parser for
 * test success or failure strings. The actual test is done by the
 * PreInvalidationListenerServlet
 *
 */
public class PreInvalidationListenerTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("PreInvalidationListenerTest");

    private static final String TEST_URI = "/dynacachetests/PreInvalidationListenerServlet";
    private static final String SUCCESS = "Test successful";

    @Test
    public void testDisk() throws Exception {
        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        String testName = "disk";
        System.out.println("Running test type=" + testName + "...");
        resp = getWebResponse(wc, TEST_URI + "?testType=" + testName);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String body = resp.getText();
        int sindex = body.indexOf(SUCCESS);
        if (sindex < 0) {
            fail("PreInvalidationListener_disk - " + resp.getURL() + "\n" + body);
        }
    }

    @Test
    public void testMemory() throws Exception {
        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        String testName = "memory";
        System.out.println("Running test type=" + testName + "...");
        resp = getWebResponse(wc, TEST_URI + "?testType=" + testName);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String body = resp.getText();
        int sindex = body.indexOf(SUCCESS);
        if (sindex < 0) {
            fail("PreInvalidationListener_memory - " + resp.getURL() + "\n" + body);
        }
    }

    @Test
    public void testDiskTimeout() throws Exception {
        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        String testName = "diskTimeout";
        System.out.println("Running test type=" + testName + "...");
        resp = getWebResponse(wc, TEST_URI + "?testType=" + testName);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String body = resp.getText();
        int sindex = body.indexOf(SUCCESS);
        if (sindex < 0) {
            fail("PreInvalidationListener_diskTimeout - " + resp.getURL() + "\n" + body);
        }
    }

    @Test
    public void testDiskTimeoutSize() throws Exception {
        WebResponse resp = null;
        WebConversation wc = startNewConversation();
        String testName = "diskTimeoutSize";
        System.out.println("Running test type=" + testName + "...");
        resp = getWebResponse(wc, TEST_URI + "?testType=" + testName);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        String body = resp.getText();
        int sindex = body.indexOf(SUCCESS);
        if (sindex < 0) {
            fail("PreInvalidationListener_diskTimeoutSize - " + resp.getURL() + "\n" + body);
        }
    }
}
