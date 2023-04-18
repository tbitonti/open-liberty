package com.ibm.ws.cache.async;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.ibm.ws.cache.TestConfig;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class ProgrammaticServletCachingTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("ProgrammaticServletCachingTest");

    String common_url = TestConfig.getBaseURL() + "/dynacachetests";

    @Test
    public void testCachingWithProgrammaticServlets() throws IOException, SAXException {

        WebConversation wc = new WebConversation();
        String servlet = "/MyDefinedServlet";
        assertEquals(getCounter(wc, servlet), getCounter(wc, servlet));

        servlet = "/MyAnnotatedServlet";
        assertEquals(getCounter(wc, servlet), getCounter(wc, servlet));

        servlet = "/MyProgrammaticServlet";
        assertEquals(getCounter(wc, servlet), getCounter(wc, servlet));

        servlet = "/MyProgrammaticServlet2";
        assertEquals(getCounter(wc, servlet), getCounter(wc, servlet));

        servlet = "/MyProgrammaticServlet3";
        assertEquals(getCounter(wc, servlet), getCounter(wc, servlet));

        servlet = "/MyDefinedServlet2";
        int ctr1 = getCounter(wc, servlet);
        int ctr2 = getCounter(wc, servlet);
        assertFalse(ctr1 + " Counters should not have been cached " + ctr2, ctr1 == ctr2);

    }

    private int getCounter(WebConversation wc, String servlet) throws IOException, SAXException {
        WebRequest request = new GetMethodWebRequest(common_url + servlet);
        WebResponse response = wc.getResponse(request);
        String counter_text = response.getElementWithID("COUNTER_VALUE").getText();
        return Integer.parseInt(counter_text);
    }
}
