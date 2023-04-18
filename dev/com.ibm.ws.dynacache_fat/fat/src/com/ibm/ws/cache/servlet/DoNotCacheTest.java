// 8/29/2005
// IBM Confidential OCO Source Material
// 5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * Test the do-not-cache property.
 *
 * It defines a fragment not to be cached as well as not consumed
 * by its parent.
 *
 * @author poirier@us.ibm.com
 */
public class DoNotCacheTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("DoNotCacheTest");

    private static final String URI = "/dynacachetests/DoNotCacheParent.jsp";

    /**
     * test01: if do-not-cache is not present, parent and child
     * should both be cached.
     */
    @Test
    public void testNotPresent() throws Exception {
        clearCache();

        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI + "?test=test01");

        String ts1 = resp1.getTableWithID("ParentTimeStamp").getCellAsText(0, 0);
        String ts2 = resp1.getTableWithID("ChildTimeStamp").getCellAsText(0, 0);
        assertNotNull(msg("DoNotCacheTest01: missing TimeStamp in parent in 1st invocation", resp1), ts1);
        assertNotNull(msg("DoNotCacheTest01: missing TimeStamp in child in 1st invocation", resp1), ts2);

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        // 2nd invocation, both should be cached
        WebResponse resp2 = getWebResponse(wc, URI + "?test=test01");
        String ts11 = resp2.getTableWithID("ParentTimeStamp").getCellAsText(0, 0);
        String ts22 = resp2.getTableWithID("ChildTimeStamp").getCellAsText(0, 0);
        assertTrue(msg("DoNotCacheTest01: wrong TimeStamp in 2nd invocation - parent not cached", resp2), ts1.equals(ts11));
        assertTrue(msg("DoNotCacheTest01: wrong TimeStamp in 2nd invocation - child not cached", resp2), ts2.equals(ts22));
    }

    /**
     * test02: if do-not-cache is present, parent should be
     * cached but not child.
     */
    @Test
    public void testPresent() throws Exception {
        clearCache();

        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI + "?test=test02");

        String ts1 = resp1.getTableWithID("ParentTimeStamp").getCellAsText(0, 0);
        String ts2 = resp1.getTableWithID("ChildTimeStamp").getCellAsText(0, 0);
        assertNotNull(msg("DoNotCacheTest02: missing TimeStamp in parent in 1st invocation", resp1), ts1);
        assertNotNull(msg("DoNotCacheTest02: missing TimeStamp in child in 1st invocation", resp1), ts2);

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        // 2nd invocation, both should be cached
        WebResponse resp2 = getWebResponse(wc, URI + "?test=test02");
        String ts11 = resp2.getTableWithID("ParentTimeStamp").getCellAsText(0, 0);
        String ts22 = resp2.getTableWithID("ChildTimeStamp").getCellAsText(0, 0);
        assertTrue(msg("DoNotCacheTest02: wrong TimeStamp in 2nd invocation - parent not cached", resp2), ts1.equals(ts11));
        assertTrue(msg("DoNotCacheTest02: wrong TimeStamp in 2nd invocation - child cached", resp2), !ts2.equals(ts22));
    }

}
