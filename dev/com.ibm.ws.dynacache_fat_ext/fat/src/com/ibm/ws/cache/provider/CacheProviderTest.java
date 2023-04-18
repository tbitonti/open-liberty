/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2012
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.cache.provider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.websphere.simplicity.log.Log;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;

public class CacheProviderTest {

    private static final Class<?> c = CacheProviderTest.class;
    private static LibertyServer server = LibertyServerFactory.getLibertyServer("cacheProviderServer");

    private static final String USER_FEATURE_USERTEST_MF = "CacheProvider/lib/features/dummyCache.mf";

    private static final String USER_FEATURE_USERTEST_JAR = "CacheProvider/lib/cache.dummy_1.0.0.jar";

    private static final String USER_FEATURE_PATH = "usr/extension/lib/features/";
    private static final String USER_BUNDLE_PATH = "usr/extension/lib/";

    /**
     * Copy the necessary features and bundles to the liberty server directories
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        final String METHOD_NAME = "classSetUp";

        Log.entering(c, METHOD_NAME);

        server.copyFileToLibertyInstallRoot(USER_FEATURE_PATH, USER_FEATURE_USERTEST_MF);

        server.copyFileToLibertyInstallRoot(USER_BUNDLE_PATH, USER_FEATURE_USERTEST_JAR);

        Log.exiting(c, METHOD_NAME);
    }

    /**
     * This method removes all the testing artifacts from the server directories.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void cleanup() throws Exception {
        final String METHOD_NAME = "classTearDown";

        Log.entering(c, METHOD_NAME);

        server.stopServer();

        //   server.deleteFileFromLibertyInstallRoot(USER_FEATURE_PATH + "dummyCache.mf");

        //    server.deleteFileFromLibertyInstallRoot(USER_BUNDLE_PATH + "com.alex.cache_1.0.0.jar");

        Log.exiting(c, METHOD_NAME);
    }

    /**
     * TestDescription:
     * This test ensures that an alternate cache provider can be used with static config.
     * 
     * @throws Exception
     */
    @Test
    public void testAlternateCacheProvider() throws Exception {
        final String METHOD_NAME = "testAlternateCacheProvider";

        Log.entering(c, METHOD_NAME);
        Log.info(c, METHOD_NAME, "*********** Starting testAlternateCacheProvider *******************");

        try {
            server.setServerConfigurationFile("CacheProvider/server-dummycache.xml");
            server.startServer("testAlternateCacheProvider.log");

            // use servlet to prod the cache into life
            String response = callTestServlet(null);

            Log.info(c, METHOD_NAME, response);
            assertNotNull("response from servlet should not have been null", response);
            assertTrue("response should have contained 'Dummy cache was used'", response.contains("Dummy cache was used"));

            /*
             * services/cache/testCache is defined in WEB-INF/cacheinstances.properties. This test is to verify a cache defined
             * in a properties file can be configured to use an alternate cache provider.
             */
            response = callTestServlet("jndiName=services/cache/testCache");

            Log.info(c, METHOD_NAME, response);
            assertNotNull("response from servlet should not have been null", response);
            assertTrue("response should have contained 'Dummy cache was used'", response.contains("Dummy cache was used"));

        } finally {
            server.stopServer();
        }

        Log.info(c, METHOD_NAME, "*********** Ending testAlternateCacheProvider *******************");
        Log.exiting(c, METHOD_NAME);
    }

    /**
     * TestDescription:
     * This test ensures that an alternate cache provider can be used with static config.
     * 
     * @throws Exception
     */
    @Test
    public void testAlternateCacheProviderDynamicFeature() throws Exception {
        final String METHOD_NAME = "testAlternateCacheProviderDynamicFeature";

        Log.entering(c, METHOD_NAME);
        Log.info(c, METHOD_NAME, "*********** Starting testAlternateCacheProviderDynamicFeature *******************");
        server.setServerConfigurationFile("CacheProvider/server-base.xml");
        try {
            server.startServer("testAlternateCacheProviderDynamicFeature.log");

            // use servlet to prod the cache into life
            String response = callTestServlet(null);

            Log.info(c, METHOD_NAME, response);
            assertNotNull("response from servlet should not have been null", response);
            assertTrue("response should have contained 'Dummy cache was not used'", response.contains("Dummy cache was not used"));

            server.setMarkToEndOfLog();
            server.setServerConfigurationFile("CacheProvider/server-dummycache.xml");
            // wait for config update to complete
            server.waitForMultipleStringsInLog(2, "CWWKF0008I");

            // try the cache again
            response = callTestServlet(null);

            Log.info(c, METHOD_NAME, response);
            assertNotNull("response from servlet should not have been null", response);
            assertTrue("response should have contained 'Dummy cache was used'", response.contains("Dummy cache was used"));
        } finally {
            server.stopServer();
        }
        Log.info(c, METHOD_NAME, "*********** Ending testAlternateCacheProviderDynamicFeature *******************");
        Log.exiting(c, METHOD_NAME);
    }

    /**
     * TestDescription:
     * This test ensures that an alternate cache provider can be used with static config.
     * 
     * @throws Exception
     */
    @Test
    public void testAltBaseCacheProvider() throws Exception {
        final String METHOD_NAME = "testAlternateCacheProvider";

        Log.entering(c, METHOD_NAME);
        Log.info(c, METHOD_NAME, "*********** Starting testAlternateCacheProvider *******************");

        try {
            server.setServerConfigurationFile("CacheProvider/server-altbasecache.xml");
            server.startServer("testAltBaseCacheProvider.log");

            // use servlet to prod the cache into life
            String response = callTestServlet("jndiName=services/cache/basecache");

            Log.info(c, METHOD_NAME, response);
            assertNotNull("response from servlet should not have been null", response);
            assertTrue("response should have contained 'Dummy cache was used' but was " + response, response.contains("Dummy cache was used"));

        } finally {
            server.stopServer();
        }

        Log.info(c, METHOD_NAME, "*********** Ending testAlternateCacheProvider *******************");
        Log.exiting(c, METHOD_NAME);
    }

    /**
     * @param object
     * @return
     */
    private String callTestServlet(String queryString) {
        HttpURLConnection con = null;
        String line = null;
        try {
            if (queryString == null) {
                queryString = "";
            } else {
                queryString = "?" + queryString;
            }
            URL url = new URL("http://" + server.getHostname() + ":"
                              + server.getHttpDefaultPort()
                              + "/cacheprovidertests/cacheprovider" + queryString);
            con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("GET");
            InputStream is = con.getInputStream();
            assertNotNull(is);
            InputStreamReader isr = new InputStreamReader(is);
            assertNotNull(isr);
            BufferedReader br = new BufferedReader(isr);
            assertNotNull(br);
            line = br.readLine();
            Log.info(c, "callTestServlet", line);
        } catch (Exception e) {
            Log.info(c, "callTestServlet", "exception caught" + e);
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return line;
    }
}
