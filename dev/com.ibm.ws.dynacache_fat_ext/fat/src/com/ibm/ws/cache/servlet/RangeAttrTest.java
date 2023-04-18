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
public class RangeAttrTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("RangeAttrTest");

    public static String URI = "/dynacachetests/RangeAttrParent.jsp";

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
     * @parm testname Value for "test" parameter on request
     * @parm value - value to pass for "setAttributeValue" parameter
     * @parm shouldcache Whether the response should be cached
     */
    public void runonetest(String testname,
                           String value,
                           boolean shouldcache)
                    throws Exception
    {
        clearCache();

        WebConversation wc = startNewConversation();

        WebRequest req = new GetMethodWebRequest(TestConfig.getBaseURL() + URI);
        req.setParameter("test", testname);
        req.setParameter("setAttributeValue", value);

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

        // Try again, same request
        WebResponse resp2 = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp2.getResponseCode(), 200);

        WebTable include2 = resp2.getTableWithID("TimeStamp");
        assertNotNull(msg("missing include1 table", resp2), include2);
        TableCell cell2 = include2.getTableCell(0, 0);

        boolean matched = (timeStamp == Long.parseLong(cell2.asText()));

        if (shouldcache && !matched) {
            fail(msg("Request should have been cached but was not", resp, resp2));
        } else if (!shouldcache && matched) {
            fail(msg("Request was cached but should not have been", resp, resp2));
        }

        // Invalidate the cache entry, if any
        req.setParameter("inv", "1");
        resp = wc.getResponse(req);
    }

    /**
     * Invalid cache specs. Should not be cached, regardless of input.
     * 
     * TODO: Should report an error somewhere, look for it.
     */

    // RangeAttrTest01:
    // Low is higher than high: low=20, high=10
    @Test
    public void test01() throws Exception {
        runonetest("RangeAttrTest01", "5", false);
    }

    @Test
    public void test02() throws Exception {
        runonetest("RangeAttrTest01", "10", false);
    }

    @Test
    public void test03() throws Exception {
        runonetest("RangeAttrTest01", "15", false);
    }

    @Test
    public void test04() throws Exception {
        runonetest("RangeAttrTest01", "20", false);
    }

    @Test
    public void test05() throws Exception {
        runonetest("RangeAttrTest01", "25", false);
    }

    // RangeAttrTest02: low isn't an integer, it's a string.
    // low="toolow" high="10"
    @Test
    public void test10() throws Exception {
        runonetest("RangeAttrTest02", "x", false);
    }

    @Test
    public void test11() throws Exception {
        runonetest("RangeAttrTest02", "9", false);
    }

    @Test
    public void test12() throws Exception {
        runonetest("RangeAttrTest02", "10", false);
    }

    @Test
    public void test13() throws Exception {
        runonetest("RangeAttrTest02", "11", false);
    }

    // RangeAttrTest03: high isn't an integer, it's a string
    //  low="5" high="toohigh"
    @Test
    public void test20() throws Exception {
        runonetest("RangeAttrTest03", "x", false);
    }

    @Test
    public void test21() throws Exception {
        runonetest("RangeAttrTest03", "4", false);
    }

    @Test
    public void test22() throws Exception {
        runonetest("RangeAttrTest03", "5", false);
    }

    @Test
    public void test23() throws Exception {
        runonetest("RangeAttrTest03", "6", false);
    }

    // RangeAttrTest04: low isn't an integer, it's a decimal
    // low="1.5" high="10"
    @Test
    public void test30() throws Exception {
        runonetest("RangeAttrTest04", "x", false);
    }

    @Test
    public void test31() throws Exception {
        runonetest("RangeAttrTest04", "1", false);
    }

    @Test
    public void test32() throws Exception {
        runonetest("RangeAttrTest04", "1.5", false);
    }

    @Test
    public void test33() throws Exception {
        runonetest("RangeAttrTest04", "2", false);
    }

    @Test
    public void test34() throws Exception {
        runonetest("RangeAttrTest04", "10", false);
    }

    @Test
    public void test35() throws Exception {
        runonetest("RangeAttrTest04", "11", false);
    }

    // RangeAttrTest05: high isn't an integer, it's a decimal
    // low="1" high="10.001"
    @Test
    public void test40() throws Exception {
        runonetest("RangeAttrTest05", "0", false);
    }

    @Test
    public void test41() throws Exception {
        runonetest("RangeAttrTest05", "1", false);
    }

    @Test
    public void test42() throws Exception {
        runonetest("RangeAttrTest05", "2", false);
    }

    @Test
    public void test43() throws Exception {
        runonetest("RangeAttrTest05", "10", false);
    }

    @Test
    public void test44() throws Exception {
        runonetest("RangeAttrTest05", "10.001", false);
    }

    @Test
    public void test45() throws Exception {
        runonetest("RangeAttrTest05", "11", false);
    }

    // RangeAttrTest06: low and high both missing

    @Test
    public void test50() throws Exception {
        runonetest("RangeAttrTest06", "x", false);
    }

    @Test
    public void test51() throws Exception {
        runonetest("RangeAttrTest06", "0", false);
    }

    /**
     * Begin testing on valid cache specs.
     */

    /**
     * RangeAttrTest10
     * 
     * <value>9</value>
     * <value><range low="-20" high="-10"/> </value>
     */

    @Test
    public void test100() throws Exception {
        runonetest("RangeAttrTest10", "-21", false);
    }

    @Test
    public void test101() throws Exception {
        runonetest("RangeAttrTest10", "-20", true);
    }

    @Test
    public void test102() throws Exception {
        runonetest("RangeAttrTest10", "-10", true);
    }

    @Test
    public void test103() throws Exception {
        runonetest("RangeAttrTest10", "-9", false);
    }

    @Test
    public void test104() throws Exception {
        runonetest("RangeAttrTest10", "8", false);
    }

    @Test
    public void test105() throws Exception {
        runonetest("RangeAttrTest10", "9", true);
    }

    @Test
    public void test106() throws Exception {
        runonetest("RangeAttrTest10", "10", false);
    }

    @Test
    public void test107() throws Exception {
        runonetest("RangeAttrTest10", "x", false);
    }

    /**
     * RangeAttrTest11
     * 
     * <value>0</value>
     */
    @Test
    public void test110() throws Exception {
        runonetest("RangeAttrTest11", "x", false);
    }

    @Test
    public void test111() throws Exception {
        runonetest("RangeAttrTest11", "-1", false);
    }

    @Test
    public void test112() throws Exception {
        runonetest("RangeAttrTest11", "0", true);
    }

    @Test
    public void test113() throws Exception {
        runonetest("RangeAttrTest11", "1", false);
    }

    @Test
    public void test114() throws Exception {
        runonetest("RangeAttrTest11", "false", false);
    }

    /**
     * RangeAttrTest20
     * 
     * No <value>. Match as long as no not-value's match.
     * 
     * <not-value><range low="-20" high="-10"/> </not-value>
     */

    @Test
    public void test200() throws Exception {
        runonetest("RangeAttrTest20", "x", true);
    }

    @Test
    public void test201() throws Exception {
        runonetest("RangeAttrTest20", "10", true);
    }

    @Test
    public void test202() throws Exception {
        runonetest("RangeAttrTest20", "-15", false);
    }

    /**
     * RangeAttrTest21
     * 
     * value range is a superset of not-value range.
     * should give us two ranges where we get cached with a hole in the middle.
     * 
     * <value><range low="-50" high="200"/> </value>
     * <not-value><range low="-20" high="-10"/> </not-value>
     */

    @Test
    public void test205() throws Exception {
        runonetest("RangeAttrTest21", "-51", false);
    }

    @Test
    public void test206() throws Exception {
        runonetest("RangeAttrTest21", "-50", true);
    }

    @Test
    public void test207() throws Exception {
        runonetest("RangeAttrTest21", "-30", true);
    }

    @Test
    public void test208() throws Exception {
        runonetest("RangeAttrTest21", "-21", true);
    }

    @Test
    public void test209() throws Exception {
        runonetest("RangeAttrTest21", "-20", false);
    }

    @Test
    public void test210() throws Exception {
        runonetest("RangeAttrTest21", "-19", false);
    }

    @Test
    public void test211() throws Exception {
        runonetest("RangeAttrTest21", "-10", false);
    }

    @Test
    public void test212() throws Exception {
        runonetest("RangeAttrTest21", "-9", true);
    }

    @Test
    public void test213() throws Exception {
        runonetest("RangeAttrTest21", "50", true);
    }

    @Test
    public void test214() throws Exception {
        runonetest("RangeAttrTest21", "200", true);
    }

    @Test
    public void test215() throws Exception {
        runonetest("RangeAttrTest21", "201", false);
    }

    /**
     * RangeAttrTest22
     * 
     * not-value range is a superset of value range.
     * basically nothing should get cached.
     * 
     * <value><range low="20" high="30"/> </value>
     * <not-value><range low="0" high="50"/> </not-value>
     */
    @Test
    public void test220() throws Exception {
        runonetest("RangeAttrTest22", "-1", false);
    }

    @Test
    public void test221() throws Exception {
        runonetest("RangeAttrTest22", "0", false);
    }

    @Test
    public void test222() throws Exception {
        runonetest("RangeAttrTest22", "1", false);
    }

    @Test
    public void test223() throws Exception {
        runonetest("RangeAttrTest22", "20", false);
    }

    @Test
    public void test224() throws Exception {
        runonetest("RangeAttrTest22", "25", false);
    }

    @Test
    public void test225() throws Exception {
        runonetest("RangeAttrTest22", "30", false);
    }

    @Test
    public void test226() throws Exception {
        runonetest("RangeAttrTest22", "40", false);
    }

    @Test
    public void test227() throws Exception {
        runonetest("RangeAttrTest22", "50", false);
    }

    @Test
    public void test228() throws Exception {
        runonetest("RangeAttrTest22", "60", false);
    }

    /**
     * RangeAttrTest23
     * 
     * value and not-value ranges overlap
     * only the part of value not overlapped by not-value should get cached
     * 
     * <value><range low="0" high="30"/> </value>
     * <not-value><range low="20" high="50"/> </not-value>
     */
    @Test
    public void test230() throws Exception {
        runonetest("RangeAttrTest23", "-1", false);
    }

    @Test
    public void test231() throws Exception {
        runonetest("RangeAttrTest23", "0", true);
    }

    @Test
    public void test232() throws Exception {
        runonetest("RangeAttrTest23", "1", true);
    }

    @Test
    public void test233() throws Exception {
        runonetest("RangeAttrTest23", "19", true);
    }

    @Test
    public void test234() throws Exception {
        runonetest("RangeAttrTest23", "20", false);
    }

    @Test
    public void test235() throws Exception {
        runonetest("RangeAttrTest23", "30", false);
    }

    @Test
    public void test236() throws Exception {
        runonetest("RangeAttrTest23", "40", false);
    }

    @Test
    public void test237() throws Exception {
        runonetest("RangeAttrTest23", "50", false);
    }

    @Test
    public void test238() throws Exception {
        runonetest("RangeAttrTest23", "60", false);
    }

    /**
     * RangeAttrTest24
     * 
     * Two disjoint <value> ranges.
     * Anything in either range should get cached, but not in between.
     * 
     * <value><range low="0" high="10"/> </value>
     * <value><range low="20" high="30"/> </value>
     */
    @Test
    public void test240() throws Exception {
        runonetest("RangeAttrTest24", "-1", false);
    }

    @Test
    public void test241() throws Exception {
        runonetest("RangeAttrTest24", "0", true);
    }

    @Test
    public void test242() throws Exception {
        runonetest("RangeAttrTest24", "5", true);
    }

    @Test
    public void test243() throws Exception {
        runonetest("RangeAttrTest24", "10", true);
    }

    @Test
    public void test244() throws Exception {
        runonetest("RangeAttrTest24", "11", false);
    }

    @Test
    public void test245() throws Exception {
        runonetest("RangeAttrTest24", "19", false);
    }

    @Test
    public void test246() throws Exception {
        runonetest("RangeAttrTest24", "20", true);
    }

    @Test
    public void test247() throws Exception {
        runonetest("RangeAttrTest24", "25", true);
    }

    @Test
    public void test248() throws Exception {
        runonetest("RangeAttrTest24", "30", true);
    }

    @Test
    public void test249() throws Exception {
        runonetest("RangeAttrTest24", "31", false);
    }

    /**
     * RangeAttrTest30
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
        runonetest("RangeAttrTest30", "11", false);
    }

    @Test
    public void test401() throws Exception {
        runonetest("RangeAttrTest30", "12", true);
    }

    @Test
    public void test402() throws Exception {
        runonetest("RangeAttrTest30", "24", true);
    }

    @Test
    public void test403() throws Exception {
        runonetest("RangeAttrTest30", "25", true);
    }

    @Test
    public void test404() throws Exception {
        runonetest("RangeAttrTest30", "26", true);
    }

    @Test
    public void test405() throws Exception {
        runonetest("RangeAttrTest30", "49", true);
    }

    @Test
    public void test406() throws Exception {
        runonetest("RangeAttrTest30", "50", true);
    }

    @Test
    public void test407() throws Exception {
        runonetest("RangeAttrTest30", "51", true);
    }

    @Test
    public void test408() throws Exception {
        runonetest("RangeAttrTest30", "100", true);
    }

    @Test
    public void test409() throws Exception {
        runonetest("RangeAttrTest30", "101", false);
    }

    /**
     * RangeAttrTest31
     * 
     * Same as RangeAttrTest30, except in separate value elements.
     * Should get same results.
     * 
     * <value><range low="12" high="50"/></value>
     * <value><range low="25" high="100"/></value>
     */
    @Test
    public void test500() throws Exception {
        runonetest("RangeAttrTest31", "11", false);
    }

    @Test
    public void test501() throws Exception {
        runonetest("RangeAttrTest31", "12", true);
    }

    @Test
    public void test502() throws Exception {
        runonetest("RangeAttrTest31", "24", true);
    }

    @Test
    public void test503() throws Exception {
        runonetest("RangeAttrTest31", "25", true);
    }

    @Test
    public void test504() throws Exception {
        runonetest("RangeAttrTest31", "26", true);
    }

    @Test
    public void test505() throws Exception {
        runonetest("RangeAttrTest31", "49", true);
    }

    @Test
    public void test506() throws Exception {
        runonetest("RangeAttrTest31", "50", true);
    }

    @Test
    public void test507() throws Exception {
        runonetest("RangeAttrTest31", "51", true);
    }

    @Test
    public void test508() throws Exception {
        runonetest("RangeAttrTest31", "100", true);
    }

    @Test
    public void test509() throws Exception {
        runonetest("RangeAttrTest31", "101", false);
    }

    /**
     * RangeTest32
     * 
     * Calls for a non-existent attribute to be in a range,
     * but it's not <required>, so it should be cached.
     */
    @Test
    public void test520() throws Exception {
        runonetest("RangeAttrTest32", "0", true);
    }

    /**
     * RangeTest33
     * 
     * Calls for a non-existent attribute to be in a range,
     * and it's <required>, so it should NOT be cached.
     */
    @Test
    public void test521() throws Exception {
        runonetest("RangeAttrTest33", "0", false);
    }

    /**
     * RangeTest35
     * 
     * Added by AKS to support case where object should NOT be cached based on invalid value
     * of a component with required = false
     */
    @Test
    public void test522() throws Exception {
        runonetest("RangeAttrTest35", "donotcache", false);
    }

    /**
     * RangeTest36
     * 
     * Added by AKS to support case where object should NOT be cached based on invalid value
     * of a component with required = false
     */
    @Test
    public void test523() throws Exception {
        runonetest("RangeAttrTest36", "notThis", false);
    }
}
