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

public class SaveCookieTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("SaveCookieTest");

    private static final String URI1 = "/dynacachetests/CacheCookie1.jsp";
    private static final String URI2 = "/dynacachetests/CacheCookie2.jsp";
    private static final String URI3 = "/dynacachetests/CacheCookie3.jsp";

    //save-cookies property 

    /**
     * make sure in absense of save-cookies, the response itself gets cached as expected
     */
    @Test
    public void testSaveCookies() throws Exception {
        clearCache();
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI1 + "?test=test01");

        //send 2 successive requests.  should get a miss and a hit
        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        WebResponse resp2 = getWebResponse(wc, URI1 + "?test=test01");
        String ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);
        if (ts2 != null) {
            ts2.trim();
        }
        assertNotNull(msg("SaveCookies: missing TimeStamp in 1st invocation", resp1), ts1);
        assertNotNull(msg("SaveCookies: missing TimeStamp in 2nd invocation", resp1), ts2);
        assertTrue(msg("SaveCookies: wrong TimeStamp in 2nd invocation - not cached", resp2), ts2.equals(ts1));

        // cleaning up
        resp2 = getWebResponse(wc, URI1 + "?inv=val1");
    }

    /**
     * with save-cookies true and exclude cookie1, make sure cookies 2 and 3 are cached
     * but not cookie 1
     */
    @Test
    public void testSaveCookiesTrueExclude() throws Exception {
        clearCache();
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI2 + "?test=test02");

        String cookie1 = resp1.getNewCookieValue("cookie1");
        String cookie2 = resp1.getNewCookieValue("cookie2");
        String cookie3 = resp1.getNewCookieValue("cookie3");

        assertNotNull(msg("SaveCookiesTrueExclude: missing cookie1 in 1st invocation", resp1), cookie1);

        assertNotNull(msg("SaveCookiesTrueExclude: missing cookie2 in 1st invocation", resp1), cookie2);

        assertNotNull(msg("SaveCookiesTrueExclude: missing cookie3 in 1st invocation", resp1), cookie3);

        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
        WebResponse resp2 = getWebResponse(wc, URI2 + "?test=test02");

        String cookie11 = resp2.getNewCookieValue("cookie1"); // expect null
        String cookie22 = resp2.getNewCookieValue("cookie2"); // should be there
        String cookie33 = resp2.getNewCookieValue("cookie3"); // should be there

        assertTrue(msg("SaveCookiesTrueExclude: found cookie1 in 2nd invocation", resp2), cookie11 == null);

        assertNotNull(msg("SaveCookiesTrueExclude: missing cookie2 in 1st invocation", resp2), cookie22);
        assertTrue(msg("SaveCookiesTrueExclude: cookie2 found in 2nd invocation but wrong value - not cached", resp2), cookie22.equals(cookie2));

        assertNotNull(msg("SaveCookiesTrueExclude: missing cookie2 in 1st invocation", resp2), cookie33);
        assertTrue(msg("SaveCookiesTrueExclude: found cookie3 in 2nd invocation but wrong value - not cached", resp2), cookie33.equals(cookie3));

    }

    /**
     * with save-cookies false and exclude cookie1, make sure cookie 1 is cached
     * but not cookies 2 or 3
     */
    @Test
    public void testSaveCookiesFalseExclude() throws Exception {
        clearCache();
        WebConversation wc = startNewConversation();
        WebResponse resp1 = getWebResponse(wc, URI3 + "?test=test03");

        String cookie1 = resp1.getNewCookieValue("cookie1");
        String cookie2 = resp1.getNewCookieValue("cookie2");
        String cookie3 = resp1.getNewCookieValue("cookie3");

        assertNotNull(msg("SaveCookiesTrueExclude: missing cookie1 in 1st invocation", resp1), cookie1);
        assertNotNull(msg("SaveCookiesTrueExclude: missing cookie2 in 1st invocation", resp1), cookie2);
        assertNotNull(msg("SaveCookiesTrueExclude: missing cookie3 in 1st invocation", resp1), cookie3);

        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }

        WebResponse resp2 = getWebResponse(wc, URI3 + "?test=test03");
        String cookie11 = resp2.getNewCookieValue("cookie1"); // should be there
        String cookie22 = resp2.getNewCookieValue("cookie2"); // expect null
        String cookie33 = resp2.getNewCookieValue("cookie3"); // expect null

        assertNotNull(msg("SaveCookiesFalseExclude: missing cookie2 in 1st invocation", resp2), cookie11);
        assertTrue(msg("SaveCookiesFalseExclude: found cookie2 in 2nd invocation", resp2), cookie11.equals(cookie1));

        assertTrue(msg("SaveCookiesFalseExclude: found cookie2 in 2nd invocation", resp2), cookie22 == null);

        assertTrue(msg("SaveCookiesFalseExclude: found cookie3 in 2nd invocation", resp2), cookie33 == null);

    }

}
