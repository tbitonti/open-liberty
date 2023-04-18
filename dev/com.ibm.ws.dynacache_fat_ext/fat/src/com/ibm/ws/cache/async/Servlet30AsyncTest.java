package com.ibm.ws.cache.async;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.MalformedObjectNameException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.ibm.ws.cache.DCacheBase;
import com.ibm.ws.cache.TestConfig;
import com.ibm.ws.cache.pmi.DynaCachePMIClient;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class Servlet30AsyncTest extends ServletTestCase {

    private final static String contextPath = "/dynacachetests";
    private static String url;
    private static DynaCachePMIClient mbeanClient;
    private static WebConversation wc;

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("Servlet30AsyncTest");

    @BeforeClass
    public static void setup() throws MalformedObjectNameException, IOException, Exception {

        if (SHARED_SERVER.isServerUp()) {
            mbeanClient = new DynaCachePMIClient(TestConfig.host, TestConfig.soapPort, TestConfig.serverRoot);
            url = TestConfig.getBaseURL() + contextPath + "/StartAsyncDispatchComplete";
            wc = new WebConversation();
        } else {
            throw new IllegalStateException("Junit setup for test Servlet30AsyncTest NOT correct");
        }
    }

    @Before
    public void setUp() throws Exception {
        clearCache();
    }

    @Test
    public void testDoNotConsume() throws MalformedURLException, IOException, SAXException {

        WebRequest request = new GetMethodWebRequest(url);
        request.setParameter("test", "testDoNotConsume");
        request.setParameter("threadNum", "1");
        request.setParameter("numChars", "10");
        request.setParameter("path", "/StartAsyncDispatchCompleteAltPath");
        request.setParameter("flush", "true");

        System.out.println("testDoNotConsume ... Sending request 1");
        WebResponse response = wc.getResponse(request);
        String ts1 = response.getElementWithID("Timestamp_before_dispatch").getText();
        String ts2 = response.getElementWithID("Timestamp_after_dispatch").getText();

        System.out.println("testDoNotConsume ... Sending request 2");
        response = wc.getResponse(request);
        String ts3 = response.getElementWithID("Timestamp_before_dispatch").getText();
        String ts4 = response.getElementWithID("Timestamp_after_dispatch").getText();

        assertEquals(2, getCacheSize());
        assertEquals(ts1, ts3);
        assertEquals(ts2, ts4);
        System.out.println();
    }

    @Test
    public void testCacheChildONLY() throws MalformedURLException, IOException, SAXException {

        WebRequest request = new GetMethodWebRequest(url);
        request.setParameter("test", "testCacheChildONLY");
        request.setParameter("threadNum", "1");
        request.setParameter("numChars", "10");
        request.setParameter("path", "/StartAsyncDispatchCompleteAltPath");
        request.setParameter("flush", "true");

        System.out.println("testCacheChildONLY ... Sending request 1");
        WebResponse response = wc.getResponse(request);
        String ts1 = response.getElementWithID("Timestamp_after_dispatch").getText();
        String ts3 = response.getElementWithID("Timestamp_before_dispatch").getText();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }

        System.out.println("testCacheChildONLY ... Sending request 2");
        response = wc.getResponse(request);
        String ts2 = response.getElementWithID("Timestamp_after_dispatch").getText();
        String ts4 = response.getElementWithID("Timestamp_before_dispatch").getText();

        assertEquals(1, getCacheSize());
        assertEquals(ts1, ts2);
        assertFalse(ts3.equals(ts4));
        System.out.println();

    }

    @Test
    public void testConsumeSubFragments() throws MalformedURLException, IOException, SAXException {

        WebRequest request = new GetMethodWebRequest(url);
        request.setParameter("test", "testConsumeSubFragments");
        request.setParameter("threadNum", "1");
        request.setParameter("numChars", "10");
        request.setParameter("path", "/StartAsyncDispatchCompleteAltPath");
        request.setParameter("flush", "true");

        System.out.println("testConsumeSubFragments ... Sending request 1");
        WebResponse response = wc.getResponse(request);
        String ts1 = response.getElementWithID("Timestamp_before_dispatch").getText();
        String ts2 = response.getElementWithID("Timestamp_after_dispatch").getText();

        System.out.println("testConsumeSubFragments ... Sending request 2");
        response = wc.getResponse(request);
        String ts3 = response.getElementWithID("Timestamp_before_dispatch").getText();
        String ts4 = response.getElementWithID("Timestamp_after_dispatch").getText();

        assertEquals(1, getCacheSize());
        assertEquals(ts1, ts3);
        assertEquals(ts2, ts4);
        System.out.println();
    }

    @Test
    public void testTimeout() throws MalformedURLException, IOException, SAXException {

        WebRequest request = new GetMethodWebRequest(url);
        request.setParameter("test", "testTimeout");
        request.setParameter("threadNum", "1");
        request.setParameter("numChars", "10");
        request.setParameter("sleepTime", "6000"); //millis
        request.setParameter("path", "/StartAsyncDispatchCompleteAltPath");
        request.setParameter("flush", "true");

        WebResponse response = wc.getResponse(request);
        assertEquals(0, getCacheSize());
        System.out.println();

    }

    @Test
    public void testError() throws MalformedURLException, IOException, SAXException {

        WebRequest request = new GetMethodWebRequest(url);
        request.setParameter("test", "testError");
        request.setParameter("threadNum", "1");
        request.setParameter("numChars", "10");
        request.setParameter("exception", "true");
        request.setParameter("path", "/StartAsyncDispatchCompleteAltPath");
        request.setParameter("flush", "true");

        WebResponse response = wc.getResponse(request);
        assertEquals(0, getCacheSize());
        System.out.println();

    }

    private int getCacheSize() {
        try {
            return mbeanClient.getPK13460MbeanStat(DCacheBase.DEFAULT_BASE_JNDI_NAME, "MemoryCacheEntries");
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
