// IBM Confidential OCO Source Material
// 5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * Test the consume-subfragments exclude element
 * Line item 3821.
 *
 * @date Aug 30, 2005
 * @author poirier@us.ibm.com
 */
public class ConsumeSubfragmentsExcludeTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("ConsumeSubfragmentsExcludeTest");

    private static final String URI = "/dynacachetests/ConsumeSubfragmentsExcludeParent.jsp";

    /**
     * Common implementation for tests.
     */
    private void common(String testname,
                        boolean shouldCacheParent,
                        boolean shouldCacheChild1,
                        boolean shouldCacheChild2) throws Exception {
        clearCache();

        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI + "?test=" + testname);

        String ts1 = resp1.getTableWithID("ParentTimeStamp").getCellAsText(0, 0);
        String ts2 = resp1.getTableWithID("Child1TimeStamp").getCellAsText(0, 0);
        String ts3 = resp1.getTableWithID("Child2TimeStamp").getCellAsText(0, 0);
        assertNotNull(msg("ConsumeSubfragmentsExclude" + testname + ": missing TimeStamp in parent in 1st invocation", resp1), ts1);
        assertNotNull(msg("ConsumeSubfragmentsExclude" + testname + ": missing TimeStamp in child 1 in 1st invocation", resp1), ts2);
        assertNotNull(msg("ConsumeSubfragmentsExclude" + testname + ": missing TimeStamp in child 2 in 1st invocation", resp1), ts3);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        // 2nd invocation,
        WebResponse resp2 = getWebResponse(wc, URI + "?test=" + testname);
        String ts11 = resp2.getTableWithID("ParentTimeStamp").getCellAsText(0, 0);
        String ts22 = resp2.getTableWithID("Child1TimeStamp").getCellAsText(0, 0);
        String ts33 = resp2.getTableWithID("Child2TimeStamp").getCellAsText(0, 0);

        boolean same;

        same = ts1.equals(ts11);
        if (shouldCacheParent && !same) {
            fail(msg("ConsumeSubfragmentsExclude" + testname + ": parent should have been cached but was not", resp2));
        }
        if (same && !shouldCacheParent) {
            fail(msg("ConsumeSubfragmentsExclude" + testname + ": parent should not have been cached but was", resp2));
        }

        same = ts2.equals(ts22);
        if (shouldCacheChild1 && !same) {
            fail(msg("ConsumeSubfragmentsExclude" + testname + ": child 1 should have been cached but was not", resp2));
        }
        if (same && !shouldCacheChild1) {
            fail(msg("ConsumeSubfragmentsExclude" + testname + ": child 1 should not have been cached but was", resp2));
        }

        same = ts3.equals(ts33);
        if (shouldCacheChild2 && !same) {
            fail(msg("ConsumeSubfragmentsExclude" + testname + ": child 2 should have been cached but was not", resp2));
        }
        if (same && !shouldCacheChild2) {
            fail(msg("ConsumeSubfragmentsExclude" + testname + ": child 2 should not have been cached but was", resp2));
        }
    }

    /**
     * test01: consume is true, exclude child 1.
     *
     * Should cache result of parent request including child 2, but
     * not child 1.
     */
    @Test
    public void testConsumeTrue() throws Exception {

        common("test01", true, false, true);
    }

    /**
     * Test 2 - consume not mentioned.
     *
     * Verify default behavior - parent cached but not children.
     */

    @Test
    public void testDefault() throws Exception {
        common("test02", true, false, false);
    }

    /**
     * Test 3 - consume is true, no excludes.
     *
     * Parent and children should all be cached.
     */
    @Test
    public void testConsume() throws Exception {
        common("test03", true, true, true);
    }
}
