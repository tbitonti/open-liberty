package com.ibm.ws.cache.bvtclient;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.common.SharedOutputManager;

public class DistributedMapTest {
    private static SharedOutputManager outputMgr;

    /**
     * Capture stdout/stderr output to the manager.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        outputMgr = SharedOutputManager.getInstance();
        outputMgr.captureStreams();
    }

    /**
     * Final teardown work when class is exiting.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Make stdout and stderr "normal"
        outputMgr.restoreStreams();
    }

    /**
     * Individual teardown after each test.
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        // Clear the output generated after each method invocation
        outputMgr.resetStreams();
    }

    /**
     * Constructor.
     */
    public DistributedMapTest() {
        // nothing
    }

    private String getPort() {
        return System.getProperty("HTTP_default", "8000");
    }

    @Test
    public void callTestServlet() {
        try {
            // Call cache servlet and ensure data is returned from cache
            URL url = new URL("http://localhost:" + getPort() + "/CacheApp/MyCacheServlet");
            System.out.println("callTestServlet URL=" + url.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("GET");
            InputStream is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            line = br.readLine();
            assertEquals("Test app returned unexpected string", "data", line);
            System.out.println("Output: " + line);
            con.disconnect();
        } catch (Throwable t) {
            outputMgr.failWithThrowable("callTestServlet", t);
        }
    }

}
