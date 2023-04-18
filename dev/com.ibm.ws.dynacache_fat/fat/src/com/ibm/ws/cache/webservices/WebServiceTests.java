// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.cache.cachemonitor.CacheMonitorTest;
import com.ibm.ws.cache.servlet.SharedServer;

import junit.framework.Assert;

public class WebServiceTests extends CacheMonitorTest {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("WebServices", "dynacachetests");

    @Test
    public void testWebServicesCachingSimple() throws Exception {

        final String methodName = "testWebServices1";

        Log.info(WebServiceTests.class, methodName, "Starting");

        checkSimpleWebServiceCaching("key1", "key1-1", false);

        Log.info(WebServiceTests.class, methodName, "Testing Transfer-Encoding: chunked");

        checkSimpleWebServiceCaching("key1", "key1-1", true);

        Log.info(WebServiceTests.class, methodName, "Test different key");

        checkSimpleWebServiceCaching("key2", "key2-1", true);

        Log.info(WebServiceTests.class, methodName, "Finished");
    }

    private void checkSimpleWebServiceCaching(String key, String expectedResponse, boolean chunked) throws MalformedURLException, IOException, ProtocolException {
        final String methodName = "checkSimpleWebServiceCaching";

        String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservices.cache.ws.ibm.com\"><soapenv:Header/><soapenv:Body><web:counter1><web:in>"
                         + key + "</web:in></web:counter1></soapenv:Body></soapenv:Envelope>";

        // Make the first web service call:
        String content = executeWebService("/dynacachetests/CounterService1?test=ServiceOperationTest", request, chunked);

        Log.info(WebServiceTests.class, methodName, "Raw response: " + content);

        Assert.assertTrue("Expecting response of " + expectedResponse, content.contains("<counter1Return>" + expectedResponse + "</counter1Return>"));

        // Make the second web service call which we expect to return a cached response of 1
        content = executeWebService("/dynacachetests/CounterService1?test=ServiceOperationTest", request, chunked);

        Log.info(WebServiceTests.class, methodName, "Raw response: " + content);

        Assert.assertTrue("Expecting response of " + expectedResponse, content.contains("<counter1Return>" + expectedResponse + "</counter1Return>"));
    }

    /**
     * @param relativeUrl
     * @param request
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ProtocolException
     */
    private String executeWebService(String relativeUrl, String request, boolean chunked) throws MalformedURLException, IOException, ProtocolException {
        final String methodName = "testWebServices1";
        URL url = new URL("http://" +
                          SHARED_SERVER.getServer().getHostname() + ":" +
                          SHARED_SERVER.getServer().getHttpDefaultPort() + relativeUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "text/xml");
        con.setRequestProperty("SOAPAction", "");

        if (chunked) {
            con.setChunkedStreamingMode(100);
        }

        con.setDoOutput(true);
        OutputStream out = con.getOutputStream();
        out.write(request.getBytes());
        out.flush();
        out.close();

        int status = con.getResponseCode();

        Log.info(WebServiceTests.class, methodName, "Response code: " + status);

        Assert.assertEquals(200, status);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }
}
