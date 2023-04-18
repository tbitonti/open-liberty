package com.ibm.ws.cache.strutstiles;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class StrutsTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("StrutsTest");

    @Test
    public void testBasicStruts() throws Exception {
        System.out.println("****************start of testBasicStruts*********************");

        clearCache();
        String URI = "/dynacachetests/basic.do?arg1=xxx";
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        WebTable include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing include1 table", resp), include1);
        TableCell cell = include1.getTableCell(0, 0);

        long timeStamp = 0;
        timeStamp = Long.parseLong(cell.asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        //Timestamp should match...
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing include1 table", resp), include1);
        cell = include1.getTableCell(0, 0);
        assertEquals(msg("TimeStamps did not match", resp), timeStamp, Long.parseLong(cell.asText()));
        System.out.println("****************end of testBasicStruts*********************");

    }

    @Test
    public void testStrutsForwardToJsp() throws Exception {
        System.out.println("****************start of testStrutsForwardToJsp*********************");
        clearCache();
        String URI = "/dynacachetests/forward.do?type=forwardtojsp";

        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("StrutsJspTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("StrutsJspTimeStamp was empty", resp1), ts1.length() != 0);

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("StrutsJspTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("StrutsJspTimeStamp was empty", resp2), ts2.length() != 0);

        assertEquals(msg("TimeStamps did not match, struts forward failed", resp2), ts1, ts2);
        System.out.println("****************end of testRequestForwardToJsp*********************");

    }

    @Test
    public void testStrutsForwardToStruts() throws Exception {
        System.out.println("****************start of testStrutsForwardToStruts*********************");
        clearCache();
        String URI = "/dynacachetests/forward.do?type=forwardtostruts";

        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("StrutsTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("StrutsTimeStamp was empty", resp1), ts1.length() != 0);

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("StrutsTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("StrutsTimeStamp was empty", resp2), ts2.length() != 0);

        assertEquals(msg("TimeStamps did not match, struts forward failed", resp2), ts1, ts2);
        System.out.println("****************end of testRequestForwardToStruts*********************");

    }

    @Test
    public void testStrutsForwardToServlet() throws Exception {
        System.out.println("****************start of testStrutsForwardToServlet*********************");
        clearCache();
        String URI = "/dynacachetests/forward.do?type=forwardtoservlet";

        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        System.out.println("respone:" + resp1.getText());

        String ts1 = resp1.getTableWithID("TimeStampChild").getCellAsText(0, 0).trim();
        System.out.println("respone:" + resp1.getText());
        assertTrue(msg("STTimeStampServlet was empty", resp1), ts1.length() != 0);

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("TimeStampChild").getCellAsText(0, 0).trim();
        assertTrue(msg("STTimeStampServlet was empty", resp2), ts2.length() != 0);

        assertEquals(msg("TimeStamps did not match, struts forward failed", resp2), ts1, ts2);
        System.out.println("****************end of testRequestForwardToServlet*********************");

    }

    @Test
    public void testStrutsInvalidation() throws Exception {
        System.out.println("****************start of testStrutsInvalidation*********************");
        clearCache();
        String URI = "/dynacachetests/basic.do";
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, URI + "?arg1=aaa");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        WebTable include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing include1 table", resp), include1);
        TableCell cell = include1.getTableCell(0, 0);

        long timeStamp = 0;
        timeStamp = Long.parseLong(cell.asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        resp = getWebResponse(wc, URI + "?inv=aaa");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        //Timestamp should match...
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing include1 table", resp), include1);
        cell = include1.getTableCell(0, 0);
        assertTrue(msg("TimeStamps match.. Invalidation failed", resp), timeStamp != Long.parseLong(cell.asText()));
        System.out.println("****************end of testStrutsInvalidation*********************");

    }

    //struts action includes parent.jsp which includes grandchild.jsp
    // CSF is set on struts action...
    @Test
    public void testStrutsCSF() throws Exception {
        System.out.println("****************start of testStrutsCSF*********************");

        clearCache();
        String URI = "/dynacachetests/stConsumeFragment.do?whoami=grandparent";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("CSF1").getCellAsText(1, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);
        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("CSF1").getCellAsText(1, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts2.length() != 0);
        assertEquals(msg("time stamps did not match, consume subfragments failed", resp2), ts1, ts2);
        System.out.println("****************end of testStrutsCSF*********************");

    }

    @Test
    public void testStrutsCSF2() throws Exception {
        System.out.println("****************start of testStrutsCSF2*********************");

        clearCache();
        String URI = "/dynacachetests/stConsumeParent.jsp";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("CSF2").getCellAsText(1, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);
        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("CSF2").getCellAsText(1, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts2.length() != 0);
        assertEquals(msg("time stamps did not match, consume subfragments failed", resp2), ts1, ts2);
        System.out.println("****************end of testStrutsCSF2*********************");

    }

    @Test
    public void testStrutsDNC() throws Exception {
        System.out.println("****************start of testStrutsDNC*********************");
        clearCache();
        String URI = "/dynacachetests/stDNCFragment.do?whoami=grandparent";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("DNC1").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);
        String ts2 = resp1.getTableWithID("DNC2").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts2.length() != 0);

        //invalidate just the grandparent...
        WebResponse inv1 = getWebResponse(wc, "/dynacachetests/stDNCFragment.do?inv=grandparent");

        //on 2'nd request the grandparent will be a miss, but grandchild should be a hit since
        //since it is marked as do-not-consume and is not consumed by grandparent..
        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts3 = resp2.getTableWithID("DNC1").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts3.length() != 0);
        String ts4 = resp2.getTableWithID("DNC2").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts4.length() != 0);
        assertEquals(msg("time stamps did not match, do-not-consume subfragments failed", resp2), ts2, ts4);
        System.out.println("****************end of testStrutsDNC*********************");

    }

    @Test
    public void testStrutsSaveAttribute() throws Exception {
        System.out.println("****************start of testStrutsSaveAttribute*********************");

        clearCache();
        //cache id based of request attribute
        String URI = "/dynacachetests/stSaveAttrParent.do";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);

        //send 2 successive requests.  should get a miss and a hit
        String ts1 = resp1.getTableWithID("att1").getCellAsText(0, 0);
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        String ts2 = resp2.getTableWithID("att1").getCellAsText(0, 0);
        if (ts2 != null) {
            ts2.trim();
        }
        assertNotNull(msg("SaveAttributes: missing TimeStamp in 1st invocation", resp1), ts1);
        assertTrue(msg("SaveAttributes: found TimeStamp in 2nd invocation", resp2), (ts2 != "null"));
        System.out.println("****************end of testStrutsSaveAttributes*********************");

    }

}
