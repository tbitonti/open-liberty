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
package com.ibm.ws.fat.utils;

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

/**
 * FAT server utilities.
 */
public class FATServerUtils {
    private static final Class<?> TEST_CLASS = FATServerUtils.class;
    private static final String CLASS_NAME = TEST_CLASS.getSimpleName();

    public static void info(String methodName, String text) {
        FATLogging.info(TEST_CLASS, methodName, text);
    }

    public static void info(String methodName, String text, String value) {
        FATLogging.info(TEST_CLASS, methodName, text, value);
    }
    
    //

    public LibertyServer getServer(String serverName) throws Exception {
        return LibertyServerFactory.getLibertyServer(serverName);
    }

    public URL getRequestUrl(
        LibertyServer server,
        String contextRoot,
        String suffix) throws MalformedURLException {

        return new URL(
            "http://" + server.getHostname() +
            ":" + server.getHttpDefaultPort() +
            suffix);
    }

    public List<String> getResponse(LibertyServer server, URL requestUrl) throws Exception {
        String methodName = "getResponse";
        info(methodName, "ENTER [ " + requestUrl + " ]");

        HttpURLConnection urlConnection =
            HttpUtils.getHttpConnection(requestUrl, HttpURLConnection.HTTP_OK, CONN_TIMEOUT );

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

    public void installWarToServer(LibertyServer server, WebArchive webArchive) throws Exception {
        String methodName = "installWarToServer";
        info(methodName, "ENTER [ " + server.getServerName() + " ] [ " + webArchive.getName() + " ]");

        ShrinkHelper.exportAppToServer(server, webArchive);

        info(methodName, "RETURN [ " + server.getServerName() + " ] [ " + webArchive.getName() + " ]");
    }

    public LibertyServer startServer(LibertyServer server) throws Exception {
        String methodName = "startServer";
        info(methodName, "ENTER [ " + server.getServerName() + " ]");

        server.startServer();
        info(methodName, "  HostName [ " + server.getHostname() + " ]");
        info(methodName, "  HttpPort [ " + server.getHttpDefaultPort() + " ]");

        info(methodName, "RETURN [ " + server.getServerName() + " ]");
        return server;
    }

    public void stopServer(LibertyServer server) throws Exception {
        String methodName = "stopServer";
        info(methodName, "ENTER [ " + server.getServerName() + " ]");

        server.stopServer();

        info(methodName, "RETURN [ " + server.getServerName() + " ]");
    }

    //

    public LibertyServer prepareServerAndWar(String serverName, FATWebArchiveDef webArchiveParams)
        throws Exception {

        String methodName = "prepareServerAndWar";
        info(methodName, "ENTER [ " + serverName + " ]");

        WebArchive webArchive = webArchiveParams.asWebArchive();

        LibertyServer server = getServer(serverName);
        installWarToServer(server, webArchive);

        info(methodName, "RETURN  [ " + serverName + " ]");
        return server;
    }
}
