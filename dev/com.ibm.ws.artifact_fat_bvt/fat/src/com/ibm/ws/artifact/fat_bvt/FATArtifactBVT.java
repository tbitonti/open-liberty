/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2011, 2017
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.fat_bvt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.ws.artifact.fat_bvt.utils.FATLogging;
import com.ibm.ws.artifact.fat_bvt.utils.FATServerUtils;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import componenttest.topology.impl.LibertyServer;
import componenttest.custom.junit.runner.FATRunner;

/**
 * Low level artifact FATs.
 */
@RunWith(FATRunner.class)
public class FATArtifactBVT {
    private static final Class<?> TEST_CLASS = FATArtifactBVT.class;
    private static final String CLASS_NAME = TEST_CLASS.getSimpleName();

    public static void info(String methodName, String text) {
        FATLogging.info(TEST_CLASS, methodName, text);
    }

    public static void info(String methodName, String text, String value) {
        FATLogging.info(TEST_CLASS, methodName, text, value);
    }

    // Artifact FAT BVT server ...

    private static LibertyServer server;

    public static void setUpServer() throws Exception {
        server = FATServerUtils.prepareServerAndWar(
            FATArtifactBVTServer.ARTIFACT_BVT_SERVER_NAME,
            FATArtifactBVTServer.ARTIFACT_BVT_WAR_DEF,
            FATArtifactBVTServer.ARTIFACT_BVT_FEATURE_DEF);
    }

    public static void tearDownServer() throws Exception {
        server = null;
    }

    public static LibertyServer getServer() {
        return server;
    }

    public static String getHostName() {
        return server.getHostname();
    }

    public static int getPortNumber() {
        return server.getHttpDefaultPort();
    }

    public static void startServer() throws Exception {
        getServer().startServer(); // 'startServer' throws Exception
    }

    public static void stopServer() throws Exception {
        getServer().stopServer(); // 'stopServer' throws Exception
    }

    public static URL getTestUrl(String suffix) throws MalformedURLException {
        return FATServerUtils.getRequestUrl(
            getServer(),
            FATArtifactBVTServer.ARTIFACT_BVT_CONTEXT_ROOT,
            suffix );
        // 'getRequestUrl' throws MalformedURLException
    }

    public static List<String> getResponse(URL url) throws Exception {
        return FATServerUtils.getResponse( getServer(), url );
        // 'getResponse' throws Exception
    }
    
    // Test setup ...

    @BeforeClass
    public static void setUp() throws Exception {
        setUpServer(); // throws Exception
        startServer(); // throws Exception
    }

    @AfterClass
    public static void tearDown() throws Exception {
        stopServer(); // throws Exception
        tearDownServer(); // throws Exception
    }

    //

    @Test
    public void basicTestDir() throws Exception {
        validateResponse( "basicTestDir", getTestUrl("?a") );
    }

    @Test
    public void basicTestJar() throws Exception {
        validateResponse( "basicTestJar", getTestUrl("?b") );
    }

    @Test
    public void basicTestRar() throws Exception {
        validateResponse( "basicTestRar", getTestUrl("?b_rar") );
    }

    @Test
    public void mediumTestDir() throws Exception {
        validateResponse( "mediumTestDir", getTestUrl("?c") );
    }

    @Test
    public void mediumTestJar() throws Exception {
        validateResponse( "mediumTestJar", getTestUrl("?d") );
    }

    @Test
    public void nestedJarTest() throws Exception {
        validateResponse( "nestedJarTest", getTestUrl("?e") );
    }

    @Test
    public void navigationTest() throws Exception {
        validateResponse( "navigationTest", getTestUrl("?f") );
    }

    @Test
    public void interpretedAdaptableTest() throws Exception {
        validateResponse( "interpretedAdaptableTest", getTestUrl("?interpretedAdaptable") );
    }

    @Test
    public void interpretedAdaptableTestRoots() throws Exception {
        validateResponse( "interpretedAdaptableTestRoots", getTestUrl("?interpretedAdaptableRoots") );
    }

    @Test
    public void testAddEntryToOverlay() throws Exception {
        validateResponse( "testAddEntryToOverlay", getTestUrl("?testAddEntryToOverlay") );
    }

    @Test
    public void testUnableToAdapt() throws Exception {
        validateResponse( "testUnableToAdapt", getTestUrl("?unableToAdapt") );
    }

    @Test
    public void dirOverlayTest() throws Exception {
        validateResponse( "dirOverlayTest", getTestUrl("?h") );
    }

    @Test
    public void adaptableTest() throws Exception {
        validateResponse( "adaptableTest", getTestUrl("?i") );
    }

    @Test
    public void testPhysicalPath() throws Exception {
        validateResponse( "testPhysicalPath", getTestUrl("?testPhysicalPath") );
    }

    @Test
    public void xmlTest() throws Exception {
        validateResponse( "xmlTest", getTestUrl("?looseRead") );
    }

    @Test
    public void notifyTest() throws Exception {
        validateResponse( "notifyTest", getTestUrl("?notify") );
    }

    @Test
    public void testCaseSensitivity_loose() throws Exception {
        validateResponse( "testCaseSensitivity_loose", getTestUrl("?testCaseSensitivity_loose") );
    }

    @Test
    public void testCaseSensitivity_file() throws Exception {
        validateResponse( "testCaseSensitivity_file", getTestUrl("?testCaseSensitivity_file") );
    }

    @Test
    public void testArtifactAPISuperTest() throws Exception {
        validateResponse( "testArtifactAPISuperTest", getTestUrl("?fs") );
    }

    @Test
    public void testDotDotPath() throws Exception {
        validateResponse( "testDotDotPath", getTestUrl("?testDotDotPath") );
    }

    @Test
    public void testImpliedZipDir() throws Exception {
        validateResponse( "testImpliedZipDir", getTestUrl("?testImpliedZipDir") );
    }

    @Test
    public void simpleBundleArtifactApiTest() throws Exception {
        validateResponse( "simpleBundleArtifactApiTest", getTestUrl("?simpleBundleArtifactApiTest") );
    }

    @Test
    public void zipCachingServiceTest() throws Exception {
        validateResponse( "zipCachingServiceTest", getTestUrl("?zipCachingServiceTest") );
    }

    // Test for 54588.
    @Test
    public void testGetEnclosingContainerOnBundle() throws Exception {
        validateResponse( "testGetEnclosingContainerOnBundle", getTestUrl("?testGetEnclosingContainerOnBundle") );
    }

    // Test for 100419
    @Test
    public void zipmultithreadedZipArtifactionInitialisationTest() throws Exception {
        validateResponse( "zipmultithreadedZipArtifactionInitialisationTest", getTestUrl("?zipMultiTest") );
    }

    // Test for 160622 / 153698
    @Test
    public void customContainerTest() throws Exception {
        validateResponse( "customContainerTest", getTestUrl("?customContainer") );
    }

    // Test for 842145
    @Test
    public void testBadBundlePathiteration() throws Exception {
        validateResponse( "testBadBundlePathiteration", getTestUrl("?testBadBundlePathIteration") );
    }

    //

    public static final String STANDARD_RESPONSE = "This is WOPR. Welcome Dr Falken.";

    public void validateResponse(String testName, URL url) throws Exception {
        String methodName = "validateResponse";
        info(methodName, "Test [ " + testName + " ]");

        List<String> responseLines = getResponse(url); // throws Exception

        // *** There must be at least one response line. ***

        Assert.assertTrue("Empty response", !responseLines.isEmpty());

        // *** And the first response line must be correct. ***

        Assert.assertEquals("Incorrest responst", STANDARD_RESPONSE, responseLines.get(0));

        int failCount = 0;
        int passCount = 0;

        for ( String responseLine : responseLines ) {
            if ( responseLine.startsWith("FAIL: ") ) {
                info(methodName, "Failure", responseLine);
                failCount++;
            } else if ( responseLine.startsWith("PASS") ) {
                info(methodName, "Success", responseLine);
                passCount++;
            }
        }

        // *** There must be no failure messages, and at least one pass message. ***

        String failureMessage;
        if ( failCount > 0 ) {
            failureMessage = "[ FAIL ] detected";
        } else if ( passCount == 0 ) {
            failureMessage = "No [ PASS ] detected";
        } else {
            failureMessage = null;
        }

        if ( failureMessage != null ) {
            Assert.fail( failureMessage + " [ " + FATLogging.asText(responseLines) + " ]" );
        }
    }
}
