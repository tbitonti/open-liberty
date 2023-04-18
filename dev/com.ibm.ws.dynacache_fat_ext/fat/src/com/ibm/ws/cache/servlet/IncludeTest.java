package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class IncludeTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("IncludeTest");

    @Test
    public void testBasicInclude() throws Exception {

        String URI = "/dynacachetests/includeservlet";
        int requests = 4;
        while (requests-- > 0) {
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            String title = resp.getTitle();
            assertTrue(msg("CacheTest was not found in the title", resp), title.indexOf("CacheTest") != -1);

            int space = title.indexOf(' ');
            assertTrue(msg("missing space in title", resp), space != -1);

            String respNum = title.substring(title.indexOf(' ')).trim();
            WebTable include1 = resp.getTableWithID("include1");
            assertNotNull(msg("missing include1 table", resp), include1);
            validateTable(include1, "include1", 1, 1, respNum);

            WebTable include2 = resp.getTableWithID("include2");
            assertNotNull(msg("missing include2 table", resp), include2);
            validateTable(include2, "include2", 2, 1, respNum);

            WebTable maintable = resp.getTableWithID("maintable");
            assertNotNull(msg("missing main table", resp), maintable);
            validateTable(maintable, "maintable", 12, 8, respNum);

        }
    }

    @Test
    public void testRequestForward() throws Exception {
        System.out.println("****************start of testRequestForward*********************");
        String URI = "/dynacachetests/includeservlet?type=requestForward";

        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("IncludeTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("IncludeTimeStamp was empty", resp1), ts1.length() != 0);

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("IncludeTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("IncludeTimeStamp was empty", resp2), ts2.length() != 0);

        assertEquals(msg("IncludeTimeStamps did not match, RequestDispacther().forward() failed", resp2), ts1, ts2);
        System.out.println("****************end of testRequestForward*********************");
    }

    @Test
    public void testRequestInclude() throws Exception {
        System.out.println("****************start of testRequestInclude*********************");
        String URI = "/dynacachetests/includeservlet?type=requestInclude";

        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("IncludeTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("IncludeTimeStamp was empty", resp1), ts1.length() != 0);

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("IncludeTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("IncludeTimeStamp was empty", resp2), ts2.length() != 0);

        assertEquals(msg("IncludeTimeStamps did not match, RequestDispacther().include() failed", resp2), ts1, ts2);
        System.out.println("****************end of testRequestInclude*********************");
    }

    @Test
    public void testNamedForward() throws Exception {
        System.out.println("****************start of testNamedForward*********************");
        String URI = "/dynacachetests/includeservlet?type=namedForward";

        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("IncludeTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("IncludeTimeStamp was empty", resp1), ts1.length() != 0);

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("IncludeTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("IncludeTimeStamp was empty", resp2), ts2.length() != 0);

        assertEquals(msg("IncludeTimeStamps did not match, NamedDispacther().forward() failed", resp2), ts1, ts2);
        System.out.println("****************end of testNamedForward*********************");
    }

    @Test
    public void testNamedInclude() throws Exception {
        System.out.println("****************start of testNamedInclude*********************");
        String URI = "/dynacachetests/includeservlet?type=namedInclude";

        WebConversation wc = startNewConversation();

        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("IncludeTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("IncludeTimeStamp was empty", resp1), ts1.length() != 0);

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("IncludeTimeStamp").getCellAsText(0, 0).trim();
        assertTrue(msg("IncludeTimeStamp was empty", resp2), ts2.length() != 0);

        assertEquals(msg("IncludeTimeStamps did not match, NamedDispacther().include() failed", resp2), ts1, ts2);
        System.out.println("****************end of testNamedInclude*********************");
    }

}
