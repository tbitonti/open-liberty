/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.restfulWS30.fat;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.ShrinkHelper;
import com.ibm.websphere.simplicity.ShrinkHelper.DeployOptions;

import componenttest.annotation.Server;
import componenttest.annotation.TestServlet;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.utils.FATServletClient;
import io.openliberty.restfulWS30.fat.misc.DefaultApp;
import io.openliberty.restfulWS30.fat.webXml.WebXmlAppTestServlet;

/**
 * Tests whether a class can be both an <code>Application</code> subclass
 * <em>and<em> a resource class.
 */
@RunWith(FATRunner.class)
public class WebXmlAppTest extends FATServletClient {

    public static final String APP_NAME = "webXmlApp";
    public static final String SERVER_NAME = APP_NAME;
    private static final String WEB_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<web-app version=\"5.0\" xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n"
                    + "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "   xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee \n"
                    + "   https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd\">\n"
                    + "\n"
                    + "    <servlet>\n"
                    + "        <servlet-name>io.openliberty.restfulWS30.fat.misc.DefaultApp</servlet-name>\n"
                    + "    </servlet>\n"
                    + "    <servlet-mapping>\n"
                    + "        <servlet-name>io.openliberty.restfulWS30.fat.misc.DefaultApp</servlet-name>\n"
                    + "        <url-pattern>/pathFromWebXml/*</url-pattern>\n"
                    + "    </servlet-mapping>\n"
                    + "</web-app>" ;

    @Server(SERVER_NAME)
    @TestServlet(servlet = WebXmlAppTestServlet.class, contextRoot = APP_NAME)
    public static LibertyServer server;

    @BeforeClass
    public static void setUp() throws Exception {
        WebArchive war = ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
                        .addAsWebInfResource(new StringAsset(WEB_XML), "web.xml")
                        .addClass(DefaultApp.class)
                        .addPackages(true, WebXmlAppTestServlet.class.getPackage());

        ShrinkHelper.exportDropinAppToServer(server, war, DeployOptions.SERVER_ONLY);

        server.startServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stopServer();
    }
}