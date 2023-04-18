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

public class TilesTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("TilesTest");

    @Test
    public void testBasicTileInsertJsp() throws Exception {
        System.out.println("****************start of testBasicTileInsertJsp*********************");

        String URI = "/dynacachetests/tileParent.jsp?type=jsp";
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        long ts1, ts2, ts3, ts4;
        WebTable table1 = resp.getTableWithID("TimeStampParent");
        assertNotNull(msg("missing TimeStampParent table", resp), table1);
        TableCell cell1 = table1.getTableCell(0, 0);
        ts1 = 0;
        ts1 = Long.parseLong(cell1.asText());

        WebTable table2 = resp.getTableWithID("TimeStampChild");
        assertNotNull(msg("missing TimeStamp table", resp), table2);
        TableCell cell2 = table2.getTableCell(0, 0);
        ts2 = 0;
        ts2 = Long.parseLong(cell2.asText());

        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        //Parent timestamps should not match and the child timestamps should match
        table1 = resp.getTableWithID("TimeStampParent");
        assertNotNull(msg("missing TimeStampParent table", resp), table1);
        cell1 = table1.getTableCell(0, 0);
        ts3 = 0;
        ts3 = Long.parseLong(cell1.asText());
        assertTrue(msg("Parent TimeStamps matched but they should not since tileparent.jsp is not cacheable", resp), ts1 != ts3);

        table2 = resp.getTableWithID("TimeStampChild");
        assertNotNull(msg("missing TimeStamp table", resp), table2);
        cell2 = table2.getTableCell(0, 0);
        ts4 = 0;
        ts4 = Long.parseLong(cell2.asText());

        System.out.println("t1:" + ts1 + " t2:" + ts2 + " t3:" + ts3 + " t4:" + ts4);

        assertTrue(msg("Child TimeStamps did not match", resp), ts2 == ts4);
        System.out.println("****************end of testBasicTileInsertJsp*********************");

    }

    @Test
    public void testBasicTileInsertServlet() throws Exception {
        System.out.println("****************start of testBasicTileInsertServlet*********************");

        clearCache();
        String URI = "/dynacachetests/tileParent.jsp?type=servlet";
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        long ts1, ts2, ts3, ts4;
        WebTable table1 = resp.getTableWithID("TimeStampParent");
        assertNotNull(msg("missing TimeStampParent table", resp), table1);
        TableCell cell1 = table1.getTableCell(0, 0);
        ts1 = 0;
        ts1 = Long.parseLong(cell1.asText());

        WebTable table2 = resp.getTableWithID("TimeStampServlet");
        assertNotNull(msg("missing TimeStamp table", resp), table2);
        TableCell cell2 = table2.getTableCell(0, 0);
        ts2 = 0;
        ts2 = Long.parseLong(cell2.asText());

        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        //Parent timestamps should not match and the child timestamps should match
        WebTable table3 = resp2.getTableWithID("TimeStampParent");
        assertNotNull(msg("missing TimeStampParent table", resp), table3);
        TableCell cell3 = table3.getTableCell(0, 0);
        ts3 = 0;
        ts3 = Long.parseLong(cell3.asText());
        assertTrue(msg("Parent TimeStamps matched but they should not since tileparent.jsp is not cacheable", resp), ts1 != ts3);

        WebTable table4 = resp.getTableWithID("TimeStampServlet");
        assertNotNull(msg("missing TimeStamp table", resp2), table4);
        TableCell cell4 = table4.getTableCell(0, 0);
        ts4 = 0;
        ts4 = Long.parseLong(cell4.asText());
        System.out.println("t1:" + ts1 + " t2:" + ts2 + " t3:" + ts3 + " t4:" + ts4);

        assertTrue(msg("Child TimeStamps did not match", resp2), ts2 == ts4);
        System.out.println("****************end of testBasicTileInsertServlet*********************");

    }

    @Test
    public void testTileTemplate() throws Exception {
        System.out.println("****************start of testTileTemplate*********************");
        clearCache();
        String URI = "/dynacachetests/tileTemplate.jsp";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);

        String ts1 = resp1.getTableWithID("header").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);

        String ts2 = resp1.getTableWithID("body").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts2.length() != 0);

        String ts3 = resp1.getTableWithID("footer").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts3.length() != 0);

        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);

        String ts11 = resp2.getTableWithID("header").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts11.length() != 0);

        String ts22 = resp2.getTableWithID("body").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts22.length() != 0);

        String ts33 = resp2.getTableWithID("footer").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts33.length() != 0);

        System.out.println("t1:" + ts1 + " t2:" + ts2 + " t3:" + ts3);
        System.out.println("t1:" + ts11 + " t2:" + ts22 + " t3:" + ts33);
        assertEquals(msg("header time stamps did not match", resp2), ts1, ts11);
        assertEquals(msg("body time stamps did not match", resp2), ts2, ts22);
        assertEquals(msg("footer time stamps did not match", resp2), ts3, ts33);

        System.out.println("****************end of testTileTemplate*********************");

    }

//TODO: This test needs to be fixed.
/*
 * @Test
 * public void testTileDefinition() throws Exception {
 * System.out.println("****************start of testTileDefinition*********************");
 * clearCache();
 * String URI = "/dynacachetests/tileDefinition.jsp";
 * WebConversation wc = startNewConversation();
 * WebResponse resp1 = getWebResponse(wc, URI);
 * assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
 * 
 * String ts1 = resp1.getTableWithID("header").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);
 * 
 * String ts2 = resp1.getTableWithID("body").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts2.length() != 0);
 * 
 * String ts3 = resp1.getTableWithID("footer").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts3.length() != 0);
 * 
 * synchronized (this) {
 * wait(25);
 * }
 * WebResponse resp2 = getWebResponse(wc, URI);
 * assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
 * 
 * String ts11 = resp2.getTableWithID("header").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts11.length() != 0);
 * 
 * String ts22 = resp2.getTableWithID("body").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts22.length() != 0);
 * 
 * String ts33 = resp2.getTableWithID("footer").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts33.length() != 0);
 * 
 * System.out.println("t1:" + ts1 + " t2:" + ts2 + " t3:" + ts3);
 * System.out.println("t1:" + ts11 + " t2:" + ts22 + " t3:" + ts33);
 * assertEquals(msg("header time stamps did not match", resp2), ts1, ts11);
 * assertEquals(msg("body time stamps did not match", resp2), ts2, ts22);
 * assertEquals(msg("footer time stamps did not match", resp2), ts3, ts33);
 * 
 * System.out.println("****************end of testTileDefinition*********************");
 * 
 * }
 * 
 * @Test
 * public void testTileDefinition2() throws Exception {
 * System.out.println("****************start of testTileDefinition2*********************");
 * clearCache();
 * String URI = "/dynacachetests/testSTDefinition.jsp";
 * WebConversation wc = startNewConversation();
 * WebResponse resp1 = getWebResponse(wc, URI);
 * assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
 * 
 * String ts1 = resp1.getTableWithID("header").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);
 * 
 * String ts2 = resp1.getTableWithID("result").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts2.length() != 0);
 * 
 * String ts3 = resp1.getTableWithID("footer").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts3.length() != 0);
 * 
 * synchronized (this) {
 * wait(25);
 * }
 * WebResponse resp2 = getWebResponse(wc, URI);
 * assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
 * 
 * String ts11 = resp2.getTableWithID("header").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts11.length() != 0);
 * 
 * String ts22 = resp2.getTableWithID("result").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts22.length() != 0);
 * 
 * String ts33 = resp2.getTableWithID("footer").getCellAsText(0, 0).trim();
 * assertTrue(msg("time stamp was empty", resp1), ts33.length() != 0);
 * 
 * System.out.println("t1:" + ts1 + " t2:" + ts2 + " t3:" + ts3);
 * System.out.println("t1:" + ts11 + " t2:" + ts22 + " t3:" + ts33);
 * 
 * assertEquals(msg("header time stamps did not match", resp2), ts1, ts11);
 * assertEquals(msg("body time stamps did not match", resp2), ts2, ts22);
 * assertEquals(msg("footer time stamps did not match", resp2), ts3, ts33);
 * 
 * System.out.println("****************end of testTileDefinition2*********************");
 * 
 * }
 */
    @Test
    public void testTileInsertStrut() throws Exception {
        System.out.println("****************start of testTileInsertStrut*********************");

        clearCache();
        String URI = "/dynacachetests/tileParent.jsp?type=strut";
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        long ts1, ts2, ts3, ts4;
        WebTable table1 = resp.getTableWithID("TimeStampParent");
        assertNotNull(msg("missing TimeStampParent table", resp), table1);
        TableCell cell1 = table1.getTableCell(0, 0);
        ts1 = 0;
        ts1 = Long.parseLong(cell1.asText());

        WebTable table2 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing TimeStamp table", resp), table2);
        TableCell cell2 = table2.getTableCell(0, 0);
        ts2 = 0;
        ts2 = Long.parseLong(cell2.asText());

        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        //Timestamp should match...
        table1 = resp.getTableWithID("TimeStampParent");
        assertNotNull(msg("missing TimeStampParent table", resp), table1);
        cell1 = table1.getTableCell(0, 0);
        ts3 = 0;
        ts3 = Long.parseLong(cell1.asText());
        assertTrue(msg("Parent TimeStamps did not match", resp), ts1 == ts3);

        table2 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing TimeStamp table", resp), table2);
        cell2 = table2.getTableCell(0, 0);
        ts4 = 0;
        ts4 = Long.parseLong(cell2.asText());
        System.out.println("t1:" + ts1 + " t2:" + ts2 + " t3:" + ts3 + " t4:" + ts4);
        assertTrue(msg("Child TimeStamps did not match", resp), ts2 == ts4);

        System.out.println("****************end of testTileInsertStrut*********************");
    }

    @Test
    public void testStrutsForwardTile() throws Exception {
        System.out.println("****************start of testStrutsForwardTile*********************");
        clearCache();
        String URI = "/dynacachetests/forward.do?type=forwardtotile";

        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        long ts1, ts2, ts3, ts4;
        WebTable table1 = resp.getTableWithID("TimeStampParent");
        assertNotNull(msg("missing TimeStampParent table", resp), table1);
        TableCell cell1 = table1.getTableCell(0, 0);
        ts1 = 0;
        ts1 = Long.parseLong(cell1.asText());

        WebTable table2 = resp.getTableWithID("TimeStampChild");
        assertNotNull(msg("missing TimeStamp table", resp), table2);
        TableCell cell2 = table2.getTableCell(0, 0);
        ts2 = 0;
        ts2 = Long.parseLong(cell2.asText());

        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        //Parent timestamps should not match and the child timestamps should match
        table1 = resp.getTableWithID("TimeStampParent");
        assertNotNull(msg("missing TimeStampParent table", resp), table1);
        cell1 = table1.getTableCell(0, 0);
        ts3 = 0;
        ts3 = Long.parseLong(cell1.asText());
        assertTrue(msg("Parent TimeStamps matched but they should not since tileparent.jsp is not cacheable", resp), ts1 != ts3);

        table2 = resp.getTableWithID("TimeStampChild");
        assertNotNull(msg("missing TimeStamp table", resp), table2);
        cell2 = table2.getTableCell(0, 0);
        ts4 = 0;
        ts4 = Long.parseLong(cell2.asText());
        System.out.println("t1:" + ts1 + " t2:" + ts2 + " t3:" + ts3 + " t4:" + ts4);

        assertTrue(msg("Child TimeStamps did not match", resp), ts2 == ts4);

        System.out.println("****************end of testStrutsForwardTile*********************");

    }

    @Test
    public void testTileCSF() throws Exception {
        System.out.println("****************start of testTileCSF*********************");

        clearCache();
        String URI = "/dynacachetests/tileGrandParent.jsp?arg1=CSF";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("GP").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);

        String ts2 = resp1.getTableWithID("parent").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts2.length() != 0);

        String ts3 = resp1.getTableWithID("child1").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts3.length() != 0);

        String ts4 = resp1.getTableWithID("child2").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts4.length() != 0);

        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts11 = resp2.getTableWithID("GP").getCellAsText(0, 0).trim();

        String ts22 = resp2.getTableWithID("parent").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts22.length() != 0);

        String ts33 = resp2.getTableWithID("child1").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts33.length() != 0);

        String ts44 = resp2.getTableWithID("child2").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts44.length() != 0);

        System.out.println("t1:" + ts1 + " t2:" + ts2 + " t3:" + ts3 + " t4:" + ts4);
        System.out.println("t11:" + ts11 + " t22:" + ts22 + " t33:" + ts33 + " t44:" + ts44);

        assertEquals(msg("Grand parent time stamps did not match, consume subfragments failed", resp2), ts1, ts11);
        assertEquals(msg("Parent time stamps did not match, consume subfragments failed", resp2), ts2, ts22);
        assertEquals(msg("Grand child1 time stamps did not match, consume subfragments failed", resp2), ts3, ts33);
        assertEquals(msg("Grand child2 time stamps did not match, consume subfragments failed", resp2), ts4, ts44);
        System.out.println("****************end of testTileCSF*********************");

    }

    @Test
    public void testTileDNC() throws Exception {
        System.out.println("****************start of testTileDNC*********************");
        clearCache();
        String URI = "/dynacachetests/tileDNCGrandParent.jsp?arg1=DNC";
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);
        String ts1 = resp1.getTableWithID("GP").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts1.length() != 0);

        String ts2 = resp1.getTableWithID("parent").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts2.length() != 0);

        String ts3 = resp1.getTableWithID("child1").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts3.length() != 0);

        String ts4 = resp1.getTableWithID("child2").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp1), ts4.length() != 0);

        //invalidate child1
        getWebResponse(wc, "/dynacachetests/tileCSFChild1.jsp?inv=DNC");
        synchronized (this) {
            wait(25);
        }
        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts11 = resp2.getTableWithID("GP").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts11.length() != 0);

        String ts22 = resp2.getTableWithID("parent").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts22.length() != 0);

        String ts33 = resp2.getTableWithID("child1").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts33.length() != 0);

        String ts44 = resp2.getTableWithID("child2").getCellAsText(0, 0).trim();
        assertTrue(msg("time stamp was empty", resp2), ts44.length() != 0);

        System.out.println("t1:" + ts1 + " t2:" + ts2 + " t3:" + ts3 + " t4:" + ts4);
        System.out.println("t11:" + ts11 + " t22:" + ts22 + " t33:" + ts33 + " t44:" + ts44);

        assertEquals(msg("Grand parent time stamps did not match, consume subfragments failed", resp2), ts1, ts11);
        assertEquals(msg("Parent time stamps did not match, consume subfragments failed", resp2), ts2, ts22);
        assertTrue(msg("Grand child1 time stamps matched, do not consume subfragments failed", resp2), ts3 != ts33);
        assertEquals(msg("Grand child2 time stamps did not match, consume subfragments failed", resp2), ts4, ts44);
        System.out.println("****************end of testTileCSF*********************");

    }

    @Test
    public void testTileSaveAttributes() throws Exception {
        System.out.println("****************start of testStrutsSaveAttribute*********************");

        clearCache();
        //cache id based of request attribute
        String URI = "/dynacachetests/tileSaveAttrParent.jsp";
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
        if (ts2 != null) {
            ts2.trim();
        }
        assertNotNull(msg("SaveAttributes: missing TimeStamp in 1st invocation", resp1), ts1);
        assertTrue(msg("SaveAttributes: found TimeStamp in 2nd invocation", resp2), (ts2 != "null"));
        System.out.println("****************end of testStrutsSaveAttributes*********************");

    }

}
