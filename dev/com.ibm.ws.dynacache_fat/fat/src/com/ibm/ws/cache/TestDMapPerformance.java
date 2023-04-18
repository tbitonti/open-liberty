// Created by Andrew J Ivory, 2/13/2003

package com.ibm.ws.cache;

import static junit.framework.Assert.assertTrue;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.servlet.SharedServer;

public class TestDMapPerformance {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("TestDMapPerformance");

    String URL_1 = TestConfig.getBaseURL();
    String uri = "";
    String body = "";
    String servlet = "/dynacachetests/";

    boolean verbose = true;

    //Test 100 DMap Instances
    //Set the DMap Count to 100 then test the throughput
    //by calling testDistributedMapPerformance
    @Test
    public void test100DMapInstances() {
        try {
            Thread.sleep(2000);

        } catch (Exception e) {
            System.out.println("Caught an exception while sleeping! e=" + e);
        }

        try {

            System.out.println("Test100DMapInstances");
            uri = "dmapperformance?mapCount=100&ratio=8&globalShareType=NONE&shareType=NONE";
            callServlet(URL_1, uri);

            uri = "dmapperformance?method=testThroughputDistributedMap&&ttl=30";
            body = callServlet(URL_1, uri);
            //System.out.println(body);

            int index1 = body.indexOf("DistributedMap : ") + 17;
            int index2 = body.indexOf(".");

            String temp1 = body.substring(index1, index2);
            int throughput = Integer.parseInt(temp1);
            //System.out.println(temp1);
            //System.out.println(throughput);

            int index3 = body.indexOf("DistributedMaps : ") + 18;
            int index4 = body.lastIndexOf(".");

            String temp2 = body.substring(index3, index4);
            int totalThroughput = Integer.parseInt(temp2);
            //System.out.println(temp2);
            //System.out.println(totalThroughput);

            assertTrue("Throughput per map for 100 maps above tolerance (" + throughput + ")", throughput > 100);

        } catch (Exception e) {
            System.out.println("Caught exception! e= " + e);
        }

    }

    //end of test100DMapInstances    

    //Test 200 DMap Instances
    //Set the DMap Count to 200 then test the throughput
    //by calling testDistributedMapPerformance
    @Test
    public void test200DMapInstances() {
        try {
            Thread.sleep(2000);

        } catch (Exception e) {
            System.out.println("Caught an exception while sleeping! e=" + e);
        }

        try {
            System.out.println("Test200DMapInstances");
            uri = "dmapperformance?mapCount=200&ratio=8&globalShareType=NONE&shareType=NONE";
            callServlet(URL_1, uri);

            uri = "dmapperformance?method=testThroughputDistributedMap&&ttl=30";
            body = callServlet(URL_1, uri);
            //System.out.println(body);

            System.out.println("\n");
            int index1 = body.indexOf("DistributedMap : ") + 17;
            int index2 = body.indexOf(".");

            String temp1 = body.substring(index1, index2);
            int throughput = Integer.parseInt(temp1);
            //System.out.println(temp1);
            //System.out.println(throughput);

            //int index3 = body.indexOf("DistributedMaps : ") + 18;
            //int index4 = body.lastIndexOf(".");

            //String temp2 = body.substring(index3,index4);
            //int totalThroughput = Integer.parseInt(temp2);
            //System.out.println(temp2);
            //System.out.println(totalThroughput);

            assertTrue("Throughput per map  for 200 maps above tolerance (" + throughput + ")", throughput > 50);

        } catch (Exception e) {
            System.out.println("Caught exception! e= " + e);
        }

    }

    //end of test100DMapInstances    

    public String callServlet(String URL, String uri) {
        String body = null;

        try {

            HttpURLConnection connection = getResponse(URL, uri);
            body = getBody(connection);
        } catch (Exception e) {
            System.out.println(" Caught exception! e= " + e);

        }

        return body;

    }

    public HttpURLConnection getResponse(String host, String uri) throws Exception {

        URL u = new URL(host + servlet + uri);
        HttpURLConnection huc = (HttpURLConnection) u.openConnection();
        huc.connect();
        assertTrue("Response code was not OK " + host + uri + huc.getResponseCode(), huc.getResponseCode() == 200);
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
}
