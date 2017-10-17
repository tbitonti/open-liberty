package com.ibm.ws.artifact.test;

/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2011
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.common.SharedOutputManager;

/**
 *
 */
public class BvtClientSampleTest {
    private static SharedOutputManager outputMgr;

    /**
     * Capture stdout/stderr output to the manager.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // There are variations of this constructor:
        // e.g. to specify a log location or an enabled trace spec. Ctrl-Space
        // for suggestions
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

    private static final String urlPrefix = "http://localhost:" + System.getProperty("HTTP_default", "8000") + "/urltestapp";

    @Test
    public void testURLService() throws IOException {
        URL url = new URL(urlPrefix + "?testURLService");
        testServlet(url);
    }

    @Test
    public void testParser() throws IOException {
        URL url = new URL(urlPrefix + "?parseTest");
        testServlet(url);
    }

    @Test
    public void testConnection() throws IOException {
        URL url = new URL(urlPrefix + "?connectionTest");
        testServlet(url);
    }

    public void testServlet(URL url) throws IOException {
        //System.out.println("Testing to " + url);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestMethod("GET");
        InputStream is = con.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {
            // read the page contents
            String line = br.readLine();
            List<String> lines = new ArrayList<String>();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
            con.disconnect();

            // log the output lines so we can debug
            System.out.println("Output: ");
            for (String msg : lines) {
                System.out.println(msg);
            }

            // check the first line to be sure we at least got to the servlet
            assertEquals("Servlet content was incorrect", "This is WOPR. Welcome Dr Falken.", lines.get(0));

            boolean foundPass = false;

            //Pass criteria: 
            // - No FAIL: lines
            // - at least one PASS line

            for (String msg : lines) {
                if (msg.startsWith("FAIL: ")) {
                    // When there is a fail log the whole output
                    StringBuilder builder = new StringBuilder();
                    for (String lineForMessage : lines) {
                        builder.append(lineForMessage);
                        builder.append("\n");
                    }
                    fail(builder.toString());
                }
                if (msg.startsWith("PASS")) {
                    foundPass = true;
                }
            }
            if (!foundPass) {
                fail("Did not see PASS from servlet invocation at " + url);
            }

        } finally {
            br.close();
        }
    }
}
