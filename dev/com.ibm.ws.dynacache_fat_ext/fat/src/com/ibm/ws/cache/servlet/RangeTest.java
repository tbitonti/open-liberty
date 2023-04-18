package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.TestConfig;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

/**
 * Test the <range> element inside <value> and <not-value>.
 * 
 * From line item 3821, WAS v7.
 * 
 * @author Dan Poirier
 * @date August 26, 2005
 */
public class RangeTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("RangeTest");

    public static String URI = "/dynacachetests/RangeTimeStamp.jsp";

    // includeservlet == com.ibm.ws.cache.servlet.IncludeTestServlet

    public String msg(String errorMessage, WebResponse resp, WebResponse resp2) {
        String content = "<no content>";
        if (resp != null) {
            try {
                content = "\nResponse Headers:\n" + resp.toString();
                content += "\nURL:\n" + resp.getURL().toString();
                content += "\nBody:\n" + resp.getText();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        if (resp2 != null) {
            try {
                content += "\nResponse2 Headers:\n" + resp2.toString();
                content += "\nURL2:\n" + resp2.getURL().toString();
                content += "\nBody2:\n" + resp2.getText();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return errorMessage + ": " + content + "\n";
    }

    /**
     * Submit one URI and see if it is cached or not cached properly.
     * 
     * @parm testname Value for "test" paramter on request
     * @parm value value to pass for "inputvalue" paramter
     * @parm shouldcache Whether the response should be cached
     */
    public void runonetest(String testname,
                           String value,
                           boolean shouldcache)
                    throws Exception
    {
        /* clearCache(); */

        WebConversation wc = startNewConversation();

        WebRequest req = new GetMethodWebRequest(TestConfig.getBaseURL() + URI);
        req.setParameter("test", testname);
        req.setParameter("inputvalue", value);

        WebResponse resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

        WebTable include1 = resp.getTableWithID("TimeStamp");
        assertNotNull(msg("missing include1 table", resp), include1);
        TableCell cell = include1.getTableCell(0, 0);

        long timeStamp = 0;
        timeStamp = Long.parseLong(cell.asText());

        // slight delay to make sure the time has ticked
        try {
            Thread.sleep(25);
        } catch (Exception e) {
        }

        // Try again
        WebResponse resp2;
        resp2 = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);

        WebTable include2;
        include2 = resp2.getTableWithID("TimeStamp");
        assertNotNull(msg("missing include2 table", resp2), include2);
        TableCell cell2;
        cell2 = include2.getTableCell(0, 0);

        boolean matched = (timeStamp == Long.parseLong(cell2.asText()));

        if (shouldcache && !matched) {
            fail(msg("Supposed to cache but didn't - testname=" + testname + ", value=" + value, resp, resp2));
        } else if (!shouldcache && matched) {
            fail(msg("Not supposed to cache but did - testname=" + testname + ", value=" + value, resp, resp2));
        }

        // Invalidate the cache entry, if any
        /*
         * req.setParameter("inv", "1");
         * resp = wc.getResponse(req);
         */
    }

    /**
     * Invalid cache specs. Should not be cached, regardless of input.
     * 
     * TODO: Should report an error somewhere, look for it.
     */

    // RangeTest01:
    // Low is higher than high: low=20, high=10

    @Test
    public void test01() throws Exception {
        runonetest("RangeTest01", "5", false);
    }

    @Test
    public void test02() throws Exception {
        runonetest("RangeTest01", "10", false);
    }

    @Test
    public void test03() throws Exception {
        runonetest("RangeTest01", "15", false);
    }

    @Test
    public void test04() throws Exception {
        runonetest("RangeTest01", "20", false);
    }

    @Test
    public void test05() throws Exception {
        runonetest("RangeTest01", "25", false);
    }

    // RangeTest02: low isn't an integer, it's a string.
    // low="toolow" high="10"
    @Test
    public void test10() throws Exception {
        runonetest("RangeTest02", "x", false);
    }

    @Test
    public void test11() throws Exception {
        runonetest("RangeTest02", "9", false);
    }

    @Test
    public void test12() throws Exception {
        runonetest("RangeTest02", "10", false);
    }

    @Test
    public void test13() throws Exception {
        runonetest("RangeTest02", "11", false);
    }

    // RangeTest03: high isn't an integer, it's a string
    //  low="5" high="toohigh"
    @Test
    public void test20() throws Exception {
        runonetest("RangeTest03", "x", false);
    }

    @Test
    public void test21() throws Exception {
        runonetest("RangeTest03", "4", false);
    }

    @Test
    public void test22() throws Exception {
        runonetest("RangeTest03", "5", false);
    }

    @Test
    public void test23() throws Exception {
        runonetest("RangeTest03", "6", false);
    }

    // RangeTest04: low isn't an integer, it's a decimal
    // low="1.5" high="10"
    @Test
    public void test30() throws Exception {
        runonetest("RangeTest04", "x", false);
    }

    @Test
    public void test31() throws Exception {
        runonetest("RangeTest04", "1", false);
    }

    @Test
    public void test32() throws Exception {
        runonetest("RangeTest04", "1.5", false);
    }

    @Test
    public void test33() throws Exception {
        runonetest("RangeTest04", "2", false);
    }

    @Test
    public void test34() throws Exception {
        runonetest("RangeTest04", "10", false);
    }

    @Test
    public void test35() throws Exception {
        runonetest("RangeTest04", "11", false);
    }

    // RangeTest05: high isn't an integer, it's a decimal
    // low="1" high="10.001"
    @Test
    public void test40() throws Exception {
        runonetest("RangeTest05", "0", false);
    }

    @Test
    public void test41() throws Exception {
        runonetest("RangeTest05", "1", false);
    }

    @Test
    public void test42() throws Exception {
        runonetest("RangeTest05", "2", false);
    }

    @Test
    public void test43() throws Exception {
        runonetest("RangeTest05", "10", false);
    }

    @Test
    public void test44() throws Exception {
        runonetest("RangeTest05", "10.001", false);
    }

    @Test
    public void test45() throws Exception {
        runonetest("RangeTest05", "11", false);
    }

    // RangeTest06: low and high both missing
    @Test
    public void test50() throws Exception {
        runonetest("RangeTest06", "x", false);
    }

    @Test
    public void test51() throws Exception {
        runonetest("RangeTest06", "0", false);
    }

    /**
     * Begin testing on valid cache specs.
     */

    /**
     * RangeTest10
     * 
     * <value>9</value>
     * <value><range low="-20" high="-10"/> </value>
     */

    @Test
    public void test100() throws Exception {
        runonetest("RangeTest10", "-21", false);
    }

    @Test
    public void test101() throws Exception {
        runonetest("RangeTest10", "-20", true);
    }

    @Test
    public void test102() throws Exception {
        runonetest("RangeTest10", "-10", true);
    }

    @Test
    public void test103() throws Exception {
        runonetest("RangeTest10", "-9", false);
    }

    @Test
    public void test104() throws Exception {
        runonetest("RangeTest10", "8", false);
    }

    @Test
    public void test105() throws Exception {
        runonetest("RangeTest10", "9", true);
    }

    @Test
    public void test106() throws Exception {
        runonetest("RangeTest10", "10", false);
    }

    @Test
    public void test107() throws Exception {
        runonetest("RangeTest10", "x", false);
    }

    /**
     * RangeTest11
     * 
     * <value>0</value>
     */
    @Test
    public void test110() throws Exception {
        runonetest("RangeTest11", "x", false);
    }

    @Test
    public void test111() throws Exception {
        runonetest("RangeTest11", "-1", false);
    }

    @Test
    public void test112() throws Exception {
        runonetest("RangeTest11", "0", true);
    }

    @Test
    public void test113() throws Exception {
        runonetest("RangeTest11", "1", false);
    }

    @Test
    public void test114() throws Exception {
        runonetest("RangeTest11", "false", false);
    }

    /**
     * RangeTest20
     * 
     * No <value>. Match as long as no not-value's match.
     * 
     * <not-value><range low="-20" high="-10"/> </not-value>
     */

    @Test
    public void test200() throws Exception {
        runonetest("RangeTest20", "x", true);
    }

    @Test
    public void test201() throws Exception {
        runonetest("RangeTest20", "10", true);
    }

    @Test
    public void test202() throws Exception {
        runonetest("RangeTest20", "-15", false);
    }

    /**
     * RangeTest21
     * 
     * value range is a superset of not-value range.
     * should give us two ranges where we get cached with a hole in the middle.
     * 
     * <value><range low="-50" high="200"/> </value>
     * <not-value><range low="-20" high="-10"/> </not-value>
     */

    @Test
    public void test205() throws Exception {
        runonetest("RangeTest21", "-51", false);
    }

    @Test
    public void test206() throws Exception {
        runonetest("RangeTest21", "-50", true);
    }

    @Test
    public void test207() throws Exception {
        runonetest("RangeTest21", "-30", true);
    }

    @Test
    public void test208() throws Exception {
        runonetest("RangeTest21", "-21", true);
    }

    @Test
    public void test209() throws Exception {
        runonetest("RangeTest21", "-20", false);
    }

    @Test
    public void test210() throws Exception {
        runonetest("RangeTest21", "-19", false);
    }

    @Test
    public void test211() throws Exception {
        runonetest("RangeTest21", "-10", false);
    }

    @Test
    public void test212() throws Exception {
        runonetest("RangeTest21", "-9", true);
    }

    @Test
    public void test213() throws Exception {
        runonetest("RangeTest21", "50", true);
    }

    @Test
    public void test214() throws Exception {
        runonetest("RangeTest21", "200", true);
    }

    @Test
    public void test215() throws Exception {
        runonetest("RangeTest21", "201", false);
    }

    /**
     * RangeTest22
     * 
     * not-value range is a superset of value range.
     * basically nothing should get cached.
     * 
     * <value><range low="20" high="30"/> </value>
     * <not-value><range low="0" high="50"/> </not-value>
     */
    @Test
    public void test220() throws Exception {
        runonetest("RangeTest22", "-1", false);
    }

    @Test
    public void test221() throws Exception {
        runonetest("RangeTest22", "0", false);
    }

    @Test
    public void test222() throws Exception {
        runonetest("RangeTest22", "1", false);
    }

    @Test
    public void test223() throws Exception {
        runonetest("RangeTest22", "20", false);
    }

    @Test
    public void test224() throws Exception {
        runonetest("RangeTest22", "25", false);
    }

    @Test
    public void test225() throws Exception {
        runonetest("RangeTest22", "30", false);
    }

    @Test
    public void test226() throws Exception {
        runonetest("RangeTest22", "40", false);
    }

    @Test
    public void test227() throws Exception {
        runonetest("RangeTest22", "50", false);
    }

    @Test
    public void test228() throws Exception {
        runonetest("RangeTest22", "60", false);
    }

    /**
     * RangeTest23
     * 
     * value and not-value ranges overlap
     * only the part of value not overlapped by not-value should get cached
     * 
     * <value><range low="0" high="30"/> </value>
     * <not-value><range low="20" high="50"/> </not-value>
     */
    @Test
    public void test230() throws Exception {
        runonetest("RangeTest23", "-1", false);
    }

    @Test
    public void test231() throws Exception {
        runonetest("RangeTest23", "0", true);
    }

    @Test
    public void test232() throws Exception {
        runonetest("RangeTest23", "1", true);
    }

    @Test
    public void test233() throws Exception {
        runonetest("RangeTest23", "19", true);
    }

    @Test
    public void test234() throws Exception {
        runonetest("RangeTest23", "20", false);
    }

    @Test
    public void test235() throws Exception {
        runonetest("RangeTest23", "30", false);
    }

    @Test
    public void test236() throws Exception {
        runonetest("RangeTest23", "40", false);
    }

    @Test
    public void test237() throws Exception {
        runonetest("RangeTest23", "50", false);
    }

    @Test
    public void test238() throws Exception {
        runonetest("RangeTest23", "60", false);
    }

    /**
     * RangeTest24
     * 
     * Two disjoint <value> ranges.
     * Anything in either range should get cached, but not in between.
     * 
     * <value><range low="0" high="10"/> </value>
     * <value><range low="20" high="30"/> </value>
     */
    @Test
    public void test240() throws Exception {
        runonetest("RangeTest24", "-1", false);
    }

    @Test
    public void test241() throws Exception {
        runonetest("RangeTest24", "0", true);
    }

    @Test
    public void test242() throws Exception {
        runonetest("RangeTest24", "5", true);
    }

    @Test
    public void test243() throws Exception {
        runonetest("RangeTest24", "10", true);
    }

    @Test
    public void test244() throws Exception {
        runonetest("RangeTest24", "11", false);
    }

    @Test
    public void test245() throws Exception {
        runonetest("RangeTest24", "19", false);
    }

    @Test
    public void test246() throws Exception {
        runonetest("RangeTest24", "20", true);
    }

    @Test
    public void test247() throws Exception {
        runonetest("RangeTest24", "25", true);
    }

    @Test
    public void test248() throws Exception {
        runonetest("RangeTest24", "30", true);
    }

    @Test
    public void test249() throws Exception {
        runonetest("RangeTest24", "31", false);
    }

    /**
     * RangeTest30
     * 
     * Overlapping ranges. Should cache anywhere in the whole area.
     * 
     * <value>
     * <range low="12" high="50"/>
     * <range low="25" high="100"/>
     * </value>
     */
    @Test
    public void test400() throws Exception {
        runonetest("RangeTest30", "11", false);
    }

    @Test
    public void test401() throws Exception {
        runonetest("RangeTest30", "12", true);
    }

    @Test
    public void test402() throws Exception {
        runonetest("RangeTest30", "24", true);
    }

    @Test
    public void test403() throws Exception {
        runonetest("RangeTest30", "25", true);
    }

    @Test
    public void test404() throws Exception {
        runonetest("RangeTest30", "26", true);
    }

    @Test
    public void test405() throws Exception {
        runonetest("RangeTest30", "49", true);
    }

    @Test
    public void test406() throws Exception {
        runonetest("RangeTest30", "50", true);
    }

    @Test
    public void test407() throws Exception {
        runonetest("RangeTest30", "51", true);
    }

    @Test
    public void test408() throws Exception {
        runonetest("RangeTest30", "100", true);
    }

    @Test
    public void test409() throws Exception {
        runonetest("RangeTest30", "101", false);
    }

    /**
     * RangeTest31
     * 
     * Same as RangeTest30, except in separate value elements.
     * Should get same results.
     * 
     * <value><range low="12" high="50"/></value>
     * <value><range low="25" high="100"/></value>
     */
    @Test
    public void test500() throws Exception {
        runonetest("RangeTest31", "11", false);
    }

    @Test
    public void test501() throws Exception {
        runonetest("RangeTest31", "12", true);
    }

    @Test
    public void test502() throws Exception {
        runonetest("RangeTest31", "24", true);
    }

    @Test
    public void test503() throws Exception {
        runonetest("RangeTest31", "25", true);
    }

    @Test
    public void test504() throws Exception {
        runonetest("RangeTest31", "26", true);
    }

    @Test
    public void test505() throws Exception {
        runonetest("RangeTest31", "49", true);
    }

    @Test
    public void test506() throws Exception {
        runonetest("RangeTest31", "50", true);
    }

    @Test
    public void test507() throws Exception {
        runonetest("RangeTest31", "51", true);
    }

    @Test
    public void test508() throws Exception {
        runonetest("RangeTest31", "100", true);
    }

    @Test
    public void test509() throws Exception {
        runonetest("RangeTest31", "101", false);
    }

    /**
     * RangeTest32
     * 
     * Calls for a non-existent parameter to be in a range,
     * but there's no <required>true</required> on it, so it
     * should be cached.
     */
    @Test
    public void test520() throws Exception {
        runonetest("RangeTest32", "0", true);
    }

    /**
     * RangeTest33
     * RangeTest34
     * 
     * Calls for a non-existent parameter to be in a range,
     * but there is a <required>true</required> on it, so it
     * should NOT be cached.
     */
    @Test
    public void test521() throws Exception {
        runonetest("RangeTest33", "0", false);
    }

    @Test
    public void test522() throws Exception {
        runonetest("RangeTest34", "0", false);
    }

}
