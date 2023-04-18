/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2012, 2021
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.ibm.ws.cache.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.rules.ExternalResource;

import com.ibm.websphere.simplicity.config.ServerConfiguration;
import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.cache.TestConfig;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;
import componenttest.topology.impl.LibertyServerWrapper;

@LibertyServerWrapper
public class SharedServer extends ExternalResource {

    private LibertyServer liberty;
    public String testName;
    private final String wab;
    protected static String CACHEMONITOR_FEATURE_BUNDLE = "com.ibm.ws.dynacache.cachemonitor_1.0";
    protected static String CACHEMONITOR_FEATURE = "com.ibm.websphere.appserver.webCacheMonitor-1.0";

    public SharedServer(String tEsT) {
        this(tEsT, null);
    }

    public SharedServer(String tEsT, String wab) {
        testName = tEsT;
        this.wab = wab;
    }

    @Override
    public void before() throws Exception {

        liberty = LibertyServerFactory.getLibertyServer(testName, null, true);

        try {
            liberty.stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File f = new File("lib/LibertyFATTestFiles/" + testName + "/server.xml");
        if (f.exists()) {
            liberty.copyFileToLibertyServerRoot("", testName + "/server.xml");
        } else {
            liberty.copyFileToLibertyServerRoot("", "server.xml");
        }

        Log.info(SharedServer.class, "before", "\n Running Test [" + testName + "] with the following server.xml \n" + getLibertyConfig());
        List<String> jvmOptions = new ArrayList<String>();

        liberty.addInstalledAppForValidation("dynacachetests");
        liberty.addInstalledAppForValidation("modules");

        jvmOptions.add("-Dcom.ibm.ws.logging.trace.file.name=" + testName + "Trace.log");
        int ttl = 3600000;
        if (testName.equals("HTODTest1"))
            ttl = 7200000;
        jvmOptions.add("-Dcom.ibm.ws.timedexit.timetolive=" + ttl); // some of our tests take in excess of 20 minutes
        //jvmOptions.add("-Dglobal.trace.spec=*=info=enabled:com.ibm.ws.cache.*=all=enabled:com.ibm.wsspi.cache.*=all=enabled");
        liberty.setJvmOptions(jvmOptions);
        liberty.installBundle(CACHEMONITOR_FEATURE_BUNDLE + liberty.getMicroSuffix());
        liberty.installFeature(CACHEMONITOR_FEATURE);

        liberty.startServer(testName + "Console.log", true);

        if (wab != null) {
            String message = "CWWKT0016I:.*/" + wab;
            Log.info(SharedServer.class, "before", "Waiting for message to appear in log: " + message);
            liberty.waitForStringInLog(message, 30 * 1000);

            if (!testName.equals("DiskContents"))
                Assert.assertNotNull("CWWKF0011I not recieved", liberty.waitForStringInLog("CWWKF0011I"));

        }

        TestConfig.host = liberty.getHostname();
        TestConfig.port = liberty.getHttpDefaultPort();
        TestConfig.soapPort = liberty.getHttpDefaultSecurePort();
        TestConfig.serverRoot = liberty.getServerRoot();

        clearCache();
    }

    public boolean isServerUp() {
        if (null != liberty && liberty.isStarted()) {
            return liberty.waitForStringInLog("CWWKF0011I") != null;
        } else {
            return false;
        }
    }

    @Override
    public void after() {
        Log.info(SharedServer.class, "after", "Finished Test [" + testName + "]");
        if (null != liberty) {
            try {
                liberty.stopServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clearCache() throws Exception {
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest(TestConfig.getBaseURL() + "/dynacachetests/clearcacheservlet");
        WebResponse resp = wc.getResponse(req);
        Assert.assertEquals("cache clear failed", resp.getResponseCode(), 200);
    }

    private String getLibertyConfig() throws Exception, IOException {
        try (InputStream is = liberty.getServerConfigurationFile().openForReading()) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer);
            return writer.toString();
        }
    }

    public void restartServer() throws Exception {

        ServerConfiguration config = liberty.getServerConfiguration();

        liberty.stopServer();

        liberty.updateServerConfiguration(config);
        File f = new File("lib/LibertyFATTestFiles/" + testName + "/server.xml");
        if (f.exists()) {
            liberty.copyFileToLibertyServerRoot("", testName + "/server.xml");
        } else {
            liberty.copyFileToLibertyServerRoot("", "server.xml");
        }
        liberty.startServerAndValidate(false, false, true);
    }

    public LibertyServer getServer() {
        return liberty;
    }
}
