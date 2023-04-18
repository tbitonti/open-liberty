// 8/29/2005
// IBM Confidential OCO Source Material
// 5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestSuite;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * Test attributes. These test cases were created from past high-profile APARs.
 * 
 * @author Peter Gibbons
 */
public class AttrTest extends ServletTestCase {
    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("AttrTest");

    private static final String URI = "/dynacachetests/AttrA";

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite()
    {
        return new TestSuite(AttrTest.class);
    }

    /**
     * test01: D1 is dnc, so cache everything but it. Attribute foo should be non-null on
     * D1 since it gets saved.
     */
    @Test
    public void testAttr1() throws Exception {
        clearCache();

        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI + "1.jsp");

        String tsA1 = resp1.getTableWithID("TimeStampA1").getCellAsText(0, 0);
        String tsB1 = resp1.getTableWithID("TimeStampB1").getCellAsText(0, 0);
        String tsC1 = resp1.getTableWithID("TimeStampC1").getCellAsText(0, 0);
        String tsD1 = resp1.getTableWithID("TimeStampD1").getCellAsText(0, 0);
        String tsFoo = resp1.getTableWithID("foo").getCellAsText(0, 0);
        assertNotNull(msg("AttrTest01: missing TimeStampA1 in 1st invocation", resp1), tsA1);
        assertNotNull(msg("AttrTest01: missing TimeStampB1 in 1st invocation", resp1), tsB1);
        assertNotNull(msg("AttrTest01: missing TimeStampC1 in 1st invocation", resp1), tsC1);
        assertNotNull(msg("AttrTest01: missing TimeStampD1 in 1st invocation", resp1), tsD1);
        assertNotNull(msg("AttrTest01: missing foo in 1st invocation", resp1), tsFoo);

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        // 2nd invocation, all except D1 should be cached
        WebResponse resp2 = getWebResponse(wc, URI + "1.jsp");
        String ts2A1 = resp2.getTableWithID("TimeStampA1").getCellAsText(0, 0);
        String ts2B1 = resp2.getTableWithID("TimeStampB1").getCellAsText(0, 0);
        String ts2C1 = resp2.getTableWithID("TimeStampC1").getCellAsText(0, 0);
        String ts2D1 = resp2.getTableWithID("TimeStampD1").getCellAsText(0, 0);
        String ts2Foo = resp2.getTableWithID("foo").getCellAsText(0, 0);
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - A1 not cached", resp2), tsA1.equals(ts2A1));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - B1 not cached", resp2), tsB1.equals(ts2B1));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - C1 not cached", resp2), tsC1.equals(ts2C1));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - D1 cached", resp2), !tsD1.equals(ts2D1));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - E1 not cached", resp2), tsFoo.equals(ts2Foo));
    }

    /**
     * test02: A2, B2, and D2 should be cached. C2, E2, and Foo should not (dnconsume)
     */
    @Test
    public void testAttr2() throws Exception {
        clearCache();

        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI + "2.jsp");

        String tsA2 = resp1.getTableWithID("TimeStampA2").getCellAsText(0, 0);
        String tsB2 = resp1.getTableWithID("TimeStampB2").getCellAsText(0, 0);
        String tsC2 = resp1.getTableWithID("TimeStampC2").getCellAsText(0, 0);
        String tsD2 = resp1.getTableWithID("TimeStampD2").getCellAsText(0, 0);
        String tsE2 = resp1.getTableWithID("TimeStampE2").getCellAsText(0, 0);
        String tsFoo = resp1.getTableWithID("foo").getCellAsText(0, 0);
        assertNotNull(msg("AttrTest01: missing TimeStampA1 in 1st invocation", resp1), tsA2);
        assertNotNull(msg("AttrTest01: missing TimeStampB1 in 1st invocation", resp1), tsB2);
        assertNotNull(msg("AttrTest01: missing TimeStampC1 in 1st invocation", resp1), tsC2);
        assertNotNull(msg("AttrTest01: missing TimeStampD1 in 1st invocation", resp1), tsD2);
        assertNotNull(msg("AttrTest01: missing TimeStampE1 in 1st invocation", resp1), tsE2);
        assertNotNull(msg("AttrTest01: missing foo in 1st invocation", resp1), tsFoo);

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        // 2nd invocation, all except D1 should be cached
        WebResponse resp2 = getWebResponse(wc, URI + "2.jsp");
        String ts2A2 = resp2.getTableWithID("TimeStampA2").getCellAsText(0, 0);
        String ts2B2 = resp2.getTableWithID("TimeStampB2").getCellAsText(0, 0);
        String ts2C2 = resp2.getTableWithID("TimeStampC2").getCellAsText(0, 0);
        String ts2D2 = resp2.getTableWithID("TimeStampD2").getCellAsText(0, 0);
        String ts2E2 = resp2.getTableWithID("TimeStampE2").getCellAsText(0, 0);
        String ts2Foo = resp2.getTableWithID("foo").getCellAsText(0, 0);
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - A2 not cached", resp2), ts2A2.equals(tsA2));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - B2 not cached", resp2), ts2B2.equals(tsB2));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - C2 cached", resp2), !ts2C2.equals(tsC2));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - D2 not cached", resp2), ts2D2.equals(tsD2));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - E2 cached", resp2), !ts2E2.equals(tsE2));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - foo cached", resp2), !ts2Foo.equals(tsFoo));
    }

    @Test
    public void testAttr3() throws Exception {
        clearCache();

        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI + "3.jsp");

        String tsA3 = resp1.getTableWithID("TimeStampA3").getCellAsText(0, 0);
        String tsB3 = resp1.getTableWithID("TimeStampB3").getCellAsText(0, 0);
        String tsC3 = resp1.getTableWithID("TimeStampC3").getCellAsText(0, 0);
        String tsSave = resp1.getTableWithID("saveThisAttr").getCellAsText(0, 0);
        String tsDontSave = resp1.getTableWithID("dontSaveThisAttr").getCellAsText(0, 0);
        assertNotNull(msg("AttrTest01: missing TimeStampA3 in 1st invocation", resp1), tsA3);
        assertNotNull(msg("AttrTest01: missing TimeStampB3 in 1st invocation", resp1), tsB3);
        assertNotNull(msg("AttrTest01: missing TimeStampC3 in 1st invocation", resp1), tsC3);
        assertNotNull(msg("AttrTest01: missing saveThisAttr in 1st invocation", resp1), tsSave);
        assertNotNull(msg("AttrTest01: missing dontSaveThisAttr in 1st invocation", resp1), tsDontSave);

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        // 2nd invocation, all except D1 should be cached
        WebResponse resp2 = getWebResponse(wc, URI + "3.jsp");
        String ts2A3 = resp2.getTableWithID("TimeStampA3").getCellAsText(0, 0);
        String ts2B3 = resp2.getTableWithID("TimeStampB3").getCellAsText(0, 0);
        String ts2C3 = resp2.getTableWithID("TimeStampC3").getCellAsText(0, 0);
        String ts2Save = resp2.getTableWithID("saveThisAttr").getCellAsText(0, 0);
        String ts2DontSave = resp2.getTableWithID("dontSaveThisAttr").getCellAsText(0, 0);
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - A3 not cached", resp2), tsA3.equals(ts2A3));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - B3 not cached", resp2), tsB3.equals(ts2B3));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - C3 cached", resp2), !tsC3.equals(ts2C3));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - saveThisAttr not cached", resp2), tsSave.equals(ts2Save));
        assertTrue(msg("AttrTest01: wrong TimeStamp in 2nd invocation - dontSaveThisAttr present", resp2), ts2DontSave.equals("dontSaveAttr null"));
    }

}
