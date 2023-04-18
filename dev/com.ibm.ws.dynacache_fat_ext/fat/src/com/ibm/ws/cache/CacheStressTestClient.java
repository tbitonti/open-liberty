// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;

public class CacheStressTestClient extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("CacheStressTestClient");

    String servlet = "/dynacachetests/drs?quietMode=1&";
    final String servletOrg = servlet;
    String className = "CacheStressTestClient";
    String uri = "";
    String body = "";
    String ttl = "30";
    int delay1 = 100;
    int delayDRS = 1500;
    boolean verbose = false;

    @Test
    public void testCacheStressTest() throws Exception {
        final String methodName = className + ".testCacheStressTest()";
        System.out.println("\n" + methodName + " begin");
        checkConnection();
        String mapType = "mapType=DMap_1&";
        servlet = servletOrg + "shareType=NONE&globalShareType=NONE&" + mapType;
        executeServerTestCase(TestConfig.getBaseURL(), "method=testDistributedMapStress");
        System.out.println(methodName + " end");
    }

    public void checkConnection() throws Exception {
        uri = "method=ping";
        HttpURLConnection connection = getResponse(TestConfig.getBaseURL(), uri);
        String body = getBody(connection);
        assertEquals("DRSServlet 1 did not respond - " + TestConfig.getBaseURL() + servlet + uri, "pong", body);
    }

    public HttpURLConnection getResponse(String host, String uri) throws Exception {
        URL u = new URL(host + servlet + uri);
        HttpURLConnection huc = null;
        try {
            huc = (HttpURLConnection) u.openConnection();
            huc.connect();
        } catch (Exception e) {
            System.out.println("Failed to connect to: " + host + servlet + uri);
            throw e;
        }
        assertTrue("Response code was not OK " + host + servlet + uri + " rc:" + huc.getResponseCode(), huc.getResponseCode() == 200);
        return huc;
    }

    public String getBody(HttpURLConnection huc) throws Exception {
        InputStreamReader isr = null;
        if (huc.getContentEncoding() != null) {
            isr = new InputStreamReader(huc.getInputStream(), huc.getContentEncoding());
        } else {
            isr = new InputStreamReader(huc.getInputStream());
        }
        if (huc.getContentLength() != -1) {
            char[] c = new char[huc.getContentLength()];
            isr.read(c, 0, c.length);
            huc.disconnect();
            return new String(c);
        } else {
            int size = 256;
            char[] buffer = new char[size];
            StringBuffer stringBuffer = new StringBuffer();
            int length = 0;
            while ((length = isr.read(buffer, 0, size)) > -1) {
                stringBuffer.append(buffer, 0, length);
            }
            huc.disconnect();
            return stringBuffer.toString();
        }
    }

    void executeServerTestCase(String URL, String uri) throws Exception {
        if (verbose)
            System.out.println("executeServerTestCase - " + URL);
        HttpURLConnection connection;
        connection = getResponse(URL, uri);
        body = getBody(connection);
        assertTrue("ERROR - " + body, body.equalsIgnoreCase("done"));
    }

}
