// IBM Confidential OCO Source Material
// 5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

/**
 * Test the skip-cache-attribute property
 * Line item 3821.
 * 
 * @date Aug 30, 2005
 * @author poirier@us.ibm.com
 */
public class SkipCacheAttributeTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("SkipCacheAttributeTest");

    private static final String URIbase = "/dynacachetests/";

    private class timestamps {
        String parent;
        String child1;
        String child2;
        WebResponse resp;
    };

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

    private timestamps getPage(WebConversation wc,
                               String jspname,
                               String testname)
                    throws Exception
    {
        timestamps ts = new timestamps();

        WebResponse resp1 = getWebResponse(wc, URIbase + jspname + "?test=" + testname);
        ts.resp = resp1;

        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);

        WebTable parenttable = resp1.getTableWithID("ParentTimeStamp");
        assertNotNull(msg("SkipCacheAttribute " + testname + " : missing parent table", resp1), parenttable);
        ts.parent = parenttable.getCellAsText(0, 0);

        WebTable childtable1 = resp1.getTableWithID("Child1TimeStamp");
        assertNotNull(msg("SkipCacheAttribute " + testname + " : missing child 1 timestamp table", resp1), childtable1);
        ts.child1 = childtable1.getCellAsText(0, 0);
        assertNotNull(msg("SkipCacheAttribute" + testname + ": missing TimeStamp in 1st child", resp1), ts.child1);

        WebTable childtable2 = resp1.getTableWithID("Child2TimeStamp");
        assertNotNull(msg("SkipCacheAttribute " + testname + " : missing child 2 timestamp table", resp1), childtable1);
        ts.child2 = childtable2.getCellAsText(0, 0);
        assertNotNull(msg("SkipCacheAttribute" + testname + ": missing TimeStamp in 2nd child", resp1), ts.child2);

        return ts;
    }

    /**
     * Common implementation for tests.
     */
    private void common(String testname,
                        String jspname,
                        boolean shouldCacheParent,
                        boolean shouldCacheChild1,
                        boolean shouldCacheChild2
                    )
                                    throws Exception
    {
        clearCache();

        WebConversation wc = startNewConversation();

        timestamps ts1 = getPage(wc, jspname, testname);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        timestamps ts2 = getPage(wc, jspname, testname);

        boolean same;

        same = ts1.parent.equals(ts2.parent);
        if (shouldCacheParent && !same) {
            fail(msg("SkipCacheAttribute" + testname + ": parent should have been cached but was not", ts1.resp, ts2.resp));
        }
        if (same && !shouldCacheParent) {
            fail(msg("SkipCacheAttribute" + testname + ": parent should not have been cached but was", ts1.resp, ts2.resp));
        }

        same = ts1.child1.equals(ts2.child1);
        if (shouldCacheChild1 && !same) {
            fail(msg("SkipCacheAttribute" + testname + ": child 1 should have been cached but was not", ts1.resp, ts2.resp));
        }
        if (same && !shouldCacheChild1) {
            fail(msg("SkipCacheAttribute" + testname + ": child 1 should not have been cached but was", ts1.resp, ts2.resp));
        }

        same = ts1.child2.equals(ts2.child2);
        if (shouldCacheChild2 && !same) {
            fail(msg("SkipCacheAttribute" + testname + ": child 2 should have been cached but was not", ts1.resp, ts2.resp));
        }
        if (same && !shouldCacheChild2) {
            fail(msg("SkipCacheAttribute" + testname + ": child 2 should not have been cached but was", ts1.resp, ts2.resp));
        }
    }

    /**
     * The cachespec specifies that the following jsps are cached:
     * SkipCache1.jsp
     * SkipCache2.jsp
     * SkipCacheChild.jsp
     */

    /**
     * test01:
     * 
     * SkipCache1:
     * calls child 1
     * sets the skip-cache attribute
     * calls child 2
     * the parent should not be cached
     * the child 1 should be cached.
     * the child 2 should not be cached.
     */
    @Test
    public void test01() throws Exception {
        common("test01", "SkipCache1.jsp", false, true, false);
    }

    /**
     * test02:
     * 
     * SkipCache2:
     * calls child 1
     * sets a different attribute
     * calls child 2
     * the parent should be cached
     * the child 1 should be cached.
     * the child 2 should be cached.
     * 
     */
    @Test
    public void test02() throws Exception {
        common("test02", "SkipCache2.jsp", true, true, true);
    }
}
