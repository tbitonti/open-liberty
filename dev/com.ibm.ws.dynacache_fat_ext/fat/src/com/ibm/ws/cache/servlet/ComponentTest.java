package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class ComponentTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("ComponentTest");

    @Test
    public void testParameter() throws Exception {
        String URI = "/dynacachetests/reqparmtest";
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        //miss 1
        WebForm form = resp.getFormWithID("submit");
        WebRequest req = form.getRequest("arg1", "1");
        resp = wc.getResponse(req);
        WebTable include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, miss 1", resp), include1);
        TableCell cell = include1.getTableCell(0, 0);
        long timeStamp1 = Long.parseLong(cell.asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        //hit 1
        form = resp.getFormWithID("submit");
        req = form.getRequest("arg1", "1");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, hit 1", resp), include1);
        cell = include1.getTableCell(0, 0);
        long timeStamp2 = Long.parseLong(cell.asText());
        assertEquals(msg("timestamps are not equal", resp), timeStamp1, timeStamp2);
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        //miss 2, different from 1
        form = resp.getFormWithID("submit");
        req = form.getRequest("arg1", "2");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, miss 2", resp), include1);
        cell = include1.getTableCell(0, 0);
        timeStamp1 = Long.parseLong(cell.asText());
        assertTrue(msg("timestamps are equal for different paramaters", resp), timeStamp1 != timeStamp2);
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        //hit 2
        form = resp.getFormWithID("submit");
        req = form.getRequest("arg1", "2");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, hit 2", resp), include1);
        cell = include1.getTableCell(0, 0);
        timeStamp2 = Long.parseLong(cell.asText());
        assertTrue(msg("timestamps are not equal for hit 2", resp), timeStamp1 == timeStamp2);
    }

    @Test
    public void testAttribute() throws Exception {
        String URI = "/dynacachetests/reqattrtest";
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        //miss 1
        WebForm form = resp.getFormWithID("submit");
        WebRequest req = form.getRequest("arg", "1");
        resp = wc.getResponse(req);
        WebTable include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, miss 1", resp), include1);
        TableCell cell = include1.getTableCell(0, 0);
        long timeStamp1 = Long.parseLong(cell.asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        //hit 1
        form = resp.getFormWithID("submit");
        req = form.getRequest("arg", "1");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, hit 1", resp), include1);
        cell = include1.getTableCell(0, 0);
        long timeStamp2 = Long.parseLong(cell.asText());
        assertEquals(msg("timestamps are not equal", resp), timeStamp1, timeStamp2);
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        //miss 2, different from 1

        form = resp.getFormWithID("submit");
        req = form.getRequest("arg", "2");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, miss 2", resp), include1);
        cell = include1.getTableCell(0, 0);
        timeStamp1 = Long.parseLong(cell.asText());
        assertTrue(msg("timestamps are equal for different paramaters", resp), timeStamp1 != timeStamp2);
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        //hit 2
        form = resp.getFormWithID("submit");
        req = form.getRequest("arg", "2");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, hit 2", resp), include1);
        cell = include1.getTableCell(0, 0);
        timeStamp2 = Long.parseLong(cell.asText());
        assertTrue(msg("timestamps are not equal for hit 2", resp), timeStamp1 == timeStamp2);

        //miss 1
        form = resp.getFormWithID("submit");
        req = form.getRequest("method", "3");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, miss 1", resp), include1);
        cell = include1.getTableCell(0, 0);
        timeStamp1 = Long.parseLong(cell.asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        //hit 1
        form = resp.getFormWithID("submit");
        req = form.getRequest("method", "3");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, hit 1", resp), include1);
        cell = include1.getTableCell(0, 0);
        timeStamp2 = Long.parseLong(cell.asText());
        assertEquals(msg("timestamps are not equal", resp), timeStamp1, timeStamp2);
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        //miss 2, different from 1

        form = resp.getFormWithID("submit");
        req = form.getRequest("method", "4");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, miss 2", resp), include1);
        cell = include1.getTableCell(0, 0);
        timeStamp1 = Long.parseLong(cell.asText());
        assertTrue(msg("timestamps are equal for different paramaters", resp), timeStamp1 != timeStamp2);
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }
        //hit 2
        form = resp.getFormWithID("submit");
        req = form.getRequest("method", "4");
        resp = wc.getResponse(req);
        include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, hit 2", resp), include1);
        cell = include1.getTableCell(0, 0);
        timeStamp2 = Long.parseLong(cell.asText());
        assertTrue(msg("timestamps are not equal for hit 2", resp), timeStamp1 == timeStamp2);
    }

    @Test
    public void testPathInfo() throws Exception {
        String URI = "/dynacachetests/pathinfo1/blahblahblah";
        WebConversation wc = startNewConversation();
        WebResponse resp = null;
        /*
         * pathinfo1 has pathInfo, and includes pathinfo2....
         * 
         * A w/o pathInfo -- 1 cached (different from A), 2 not cached
         * 
         * B with pathInfo -- 1 and 2 are cached
         */

        //miss A
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        WebTable wt = resp.getTableWithID("TimeStamp1");
        assertNotNull(msg("missing timestamp table, miss A 1", resp), wt);
        long timeStampAMiss1 = Long.parseLong(wt.getTableCell(0, 0).asText());
        wt = resp.getTableWithID("TimeStamp2");
        assertNotNull(msg("missing timestamp table, miss A 2", resp), wt);
        long timeStampAMiss2 = Long.parseLong(wt.getTableCell(0, 0).asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        //Hit A
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        wt = resp.getTableWithID("TimeStamp1");
        assertNotNull(msg("missing timestamp table, hit A 1", resp), wt);
        long timeStampAHit1 = Long.parseLong(wt.getTableCell(0, 0).asText());
        wt = resp.getTableWithID("TimeStamp2");
        assertNotNull(msg("missing timestamp table, hit A 2", resp), wt);
        long timeStampAHit2 = Long.parseLong(wt.getTableCell(0, 0).asText());

        assertTrue(msg("A: timestamps different for hit and miss in cacheable parent", resp), timeStampAHit1 == timeStampAMiss1);
        assertTrue(msg("A: timestamps equal for hit and miss in uncacheable child", resp), timeStampAHit2 != timeStampAHit1);
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        URI += "?genPathInfo=true";
        //miss B
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        wt = resp.getTableWithID("TimeStamp1");
        assertNotNull(msg("missing timestamp table, miss B 1", resp), wt);
        long timeStampBMiss1 = Long.parseLong(wt.getTableCell(0, 0).asText());
        wt = resp.getTableWithID("TimeStamp2");
        assertNotNull(msg("missing timestamp table, miss B 2", resp), wt);
        long timeStampBMiss2 = Long.parseLong(wt.getTableCell(0, 0).asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        //Hit A
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        wt = resp.getTableWithID("TimeStamp1");
        assertNotNull(msg("missing timestamp table, hit B 1", resp), wt);
        long timeStampBHit1 = Long.parseLong(wt.getTableCell(0, 0).asText());
        wt = resp.getTableWithID("TimeStamp2");
        assertNotNull(msg("missing timestamp table, hit B 2", resp), wt);
        long timeStampBHit2 = Long.parseLong(wt.getTableCell(0, 0).asText());

        assertTrue(msg("timestamps equal for parent in A and B", resp), timeStampBHit1 != timeStampAHit1);

        assertTrue(msg("B: timestamps different for hit and miss in cacheable parent", resp), timeStampBHit1 == timeStampBMiss1);
        assertTrue(msg("B: timestamps different for hit and miss in cacheable child", resp), timeStampBHit2 == timeStampBMiss2);

    }

    @Test
    public void testMappingMultipleCacheEntriesToSameSW() throws Exception {

        //this test case is to test mapping multiple config entries to same servlet wrapper

        String URI1 = "/dynacachetests/pathinfo1/xxxx";
        WebConversation wc = startNewConversation();
        WebResponse resp = null;
        WebResponse resp2 = null;

        //miss A
        resp = getWebResponse(wc, URI1);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        WebTable wt = resp.getTableWithID("TimeStamp1");
        assertNotNull(msg("missing timestamp table, miss A 1", resp), wt);
        long timeStampMiss1 = Long.parseLong(wt.getTableCell(0, 0).asText());

        //Hit A
        resp = getWebResponse(wc, URI1);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        wt = resp.getTableWithID("TimeStamp1");
        assertNotNull(msg("missing timestamp table, miss A 1", resp), wt);
        long timeStampHit1 = Long.parseLong(wt.getTableCell(0, 0).asText());
        assertTrue(msg("timestamps are different for hit and miss in cacheable parent", resp), timeStampHit1 == timeStampMiss1);

        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        String URI2 = "/dynacachetests/pathinfo1/yyyy";
        //miss B
        resp2 = getWebResponse(wc, URI2);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        WebTable wt2 = resp2.getTableWithID("TimeStamp1");
        assertNotNull(msg("missing timestamp table, miss A 1", resp2), wt2);
        long timeStampMiss2 = Long.parseLong(wt2.getTableCell(0, 0).asText());

        //HitB
        resp2 = getWebResponse(wc, URI2);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        wt2 = resp2.getTableWithID("TimeStamp1");
        assertNotNull(msg("missing timestamp table, miss A 1", resp2), wt2);
        long timeStampHit2 = Long.parseLong(wt2.getTableCell(0, 0).asText());

        assertTrue(msg("timestamps are different for hit and miss in cacheable parent", resp2), timeStampHit2 == timeStampMiss2);
        assertTrue(msg("timestamps are same for different pathinfo, timeStampHit1=" + timeStampHit1 + " timeStampHit2=" + timeStampHit2, resp2), timeStampHit1 != timeStampHit2);

    }

    @Test
    public void testParameterList() throws Exception {
        String URI = "/dynacachetests/reqparmlisttest";
        WebConversation wc = startNewConversation();

        //miss 1
        WebResponse resp = getWebResponse(wc, URI + "?parm=one");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        WebTable include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, miss 1", resp), include1);
        TableCell cell = include1.getTableCell(0, 0);
        long timeStamp1 = Long.parseLong(cell.asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        //miss 2
        resp = getWebResponse(wc, URI + "?parm=one&parm=four&parm=three&parm=two");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        WebTable include2 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, miss 2", resp), include2);
        TableCell cell2 = include2.getTableCell(0, 0);
        long timeStamp2 = Long.parseLong(cell2.asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        //hit 1
        resp = getWebResponse(wc, URI + "?parm=one&parm=four&parm=three&parm=two");
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        WebTable include3 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing timestamp table, miss 2", resp), include3);
        TableCell cell3 = include3.getTableCell(0, 0);
        long timeStamp3 = Long.parseLong(cell3.asText());
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        assertTrue(msg("timestamps are equal for miss1 and miss2", resp), timeStamp1 != timeStamp2);
        assertTrue(msg("timestamps are not equal for miss2 and hit 1", resp), timeStamp2 == timeStamp3);
    }

    @Test
    public void testServletPath() throws Exception {
        String URI = "/dynacachetests/ServletPathTest";
        WebConversation wc = startNewConversation();
        WebResponse resp = null;

        //miss			 
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        WebTable wt = resp.getTableWithID("TimeStamp_ServletPath");
        assertNotNull(msg("missing timestamp table, miss A 1", resp), wt);
        long timeStampAMiss1 = Long.parseLong(wt.getTableCell(0, 0).asText());

        //Hit A
        resp = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        wt = resp.getTableWithID("TimeStamp_ServletPath");
        assertNotNull(msg("missing timestamp table, hit A 1", resp), wt);
        long timeStampAHit1 = Long.parseLong(wt.getTableCell(0, 0).asText());

        assertTrue(msg("ServletPathTest, timeStamps are not equal", resp), timeStampAHit1 == timeStampAMiss1);

    }

}
