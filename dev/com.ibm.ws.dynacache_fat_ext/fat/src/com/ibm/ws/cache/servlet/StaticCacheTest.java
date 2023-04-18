package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class StaticCacheTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("StaticCacheTest");

    // Add for defect 309152 to test c:import from JSTL tag lib on static content
    @Test
    public void testCImportStatic() throws Exception {
        String URI = "/dynacachetests/static.jsp";
        String cacheID = "/dynacachetests/static.html:requestType=GET";
        testCachedURI(URI, new String[] { cacheID });
    }

    @Test
    public void testStaticCachePolicy() throws Exception {
        String URI = "/dynacachetests/static2.html";
        String cacheID = "/dynacachetests/static2.html:requestType=GET";
        testCachedURI(URI, new String[] { cacheID });
    }

    @Test
    public void testStaticCachePolicySFServlet() throws Exception {
        String URI = "/dynacachetests/static3.html?useSFServlet=true";
        String cacheID = "/dynacachetests/com.ibm.ws.webcontainer.servlet.SimpleFileServlet.class:pathinfo=/static3.html:useSFServlet=true:requestType=GET";
        testCachedURI(URI, new String[] { cacheID });
    }

    public void testCachedURI(String uri, String[] cacheIDList) throws Exception {
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, uri);
        assertEquals(msg("Response code was not OK", resp), resp
                        .getResponseCode(), 200);

        checkCacheContents(cacheIDList, resp);
    }

    public void checkCacheContents(String[] cacheIDList, WebResponse resp) throws Exception {
        for (int i = 0; i < cacheIDList.length; i++) {
            if (!inCache(cacheIDList[i]))
                fail(msg(cacheIDList[i] + " not in cache but should be", resp));
        }

    }
}
