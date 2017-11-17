/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.artifact.fat_bvt.test.utils;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import com.ibm.websphere.simplicity.ShrinkHelper;

import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;
import componenttest.topology.utils.HttpUtils;

/**
 * FAT server utilities.
 */
public class FATServerUtils {
    private static final Class<?> TEST_CLASS = FATServerUtils.class;

    public static void info(String methodName, String text) {
        FATLogging.info(TEST_CLASS, methodName, text);
    }

    public static void info(String methodName, String text, String value) {
        FATLogging.info(TEST_CLASS, methodName, text, value);
    }

    //

    public static LibertyServer getServer(String serverName) throws Exception {
        return LibertyServerFactory.getLibertyServer(serverName);
    }

    public static URL getRequestUrl(
        LibertyServer server,
        String contextRoot,
        String suffix) throws MalformedURLException {

        return new URL(
            "http://" + server.getHostname() +
            ":" + server.getHttpDefaultPort() +
            contextRoot +
            suffix);
    }

    public static final int CONN_TIMEOUT = 10;

    public static List<String> getResponse(LibertyServer server, URL requestUrl) throws Exception {
        String methodName = "getResponse";
        info(methodName, "ENTER [ " + requestUrl + " ]");

        HttpURLConnection urlConnection =
            HttpUtils.getHttpConnection(
                requestUrl,
                HttpURLConnection.HTTP_OK, CONN_TIMEOUT );

        List<String> responseLines = new ArrayList<String>();

        try {
            BufferedReader responseReader = HttpUtils.getConnectionStream(urlConnection);

            String line;
            while ( (line = responseReader.readLine()) != null ) {
                responseLines.add(line);
            }

        } finally {
            urlConnection.disconnect();
        }

        info(methodName, "Response:");
        int numLines = responseLines.size();
        for ( int lineNo = 0; lineNo < numLines; lineNo++ ) {
            info(methodName, "  [ " + Integer.toString(lineNo) + " ] [ " + responseLines.get(lineNo) + " ]");
        }

        info(methodName, "RETURN");
        return responseLines;
    }

    public static void installWarToServer(LibertyServer server, WebArchive webArchive) throws Exception {
        String methodName = "installWarToServer";
        info(methodName, "ENTER [ " + server.getServerName() + " ] [ " + webArchive.getName() + " ]");

        ShrinkHelper.exportAppToServer(server, webArchive);

        info(methodName, "RETURN [ " + server.getServerName() + " ] [ " + webArchive.getName() + " ]");
    }

    public static LibertyServer startServer(LibertyServer server) throws Exception {
        String methodName = "startServer";
        info(methodName, "ENTER [ " + server.getServerName() + " ]");

        server.startServer();
        info(methodName, "  HostName [ " + server.getHostname() + " ]");
        info(methodName, "  HttpPort [ " + server.getHttpDefaultPort() + " ]");

        info(methodName, "RETURN [ " + server.getServerName() + " ]");
        return server;
    }

    public static void stopServer(LibertyServer server) throws Exception {
        String methodName = "stopServer";
        info(methodName, "ENTER [ " + server.getServerName() + " ]");

        server.stopServer();

        info(methodName, "RETURN [ " + server.getServerName() + " ]");
    }

    //

    public static LibertyServer prepareServerAndWar(
        String serverName,
        FATWebArchiveDef webArchiveDef,
        FATFeatureDef featureDef)
        throws Exception {

        String methodName = "prepareServerAndWar";
        info(methodName, "ENTER [ " + serverName + " ]");

        WebArchive webArchive = webArchiveDef.asWebArchive();

        LibertyServer server = getServer(serverName);

        installFeatureToServer(server, featureDef);

        installWarToServer(server, webArchive);

        info(methodName, "RETURN  [ " + serverName + " ]");
        return server;
    }

    public static final String SERVER_FEATURES_PATH = "usr/extension/lib/features/";

    public static final String SERVER_USER_LIB_PATH = "usr/extension/lib/";

    public static void installFeatureToServer(
        LibertyServer server,
        FATFeatureDef featureDef)
        throws Exception {

        String methodName = "installFeaturesToServer";
        info(methodName, "Server [ " + server.getServerName() + " ] ...");

        info(methodName, "  Feature Manifest [ " + featureDef.featureManifestPath + " ]");
        info(methodName, "  Feature JAR [ " + featureDef.featureJarPath + " ]");

        server.copyFileToLibertyInstallRoot(SERVER_FEATURES_PATH, featureDef.featureManifestPath);
        // throws Exception
        server.copyFileToLibertyInstallRoot(SERVER_USER_LIB_PATH, featureDef.featureJarPath);
        // throws Exception

        info(methodName, "Server [ " + server.getServerName() + " ] ... done");
    }
}
