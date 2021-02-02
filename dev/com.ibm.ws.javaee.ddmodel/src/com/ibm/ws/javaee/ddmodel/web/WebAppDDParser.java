/*******************************************************************************
 * Copyright (c) 2014, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.ddmodel.web;

import com.ibm.ws.javaee.dd.web.WebApp;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.ws.javaee.ddmodel.web.common.WebAppType;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class WebAppDDParser extends DDParser {
    /**
     * Answer the minimum JavaEE version which is required
     * to support a provisioned servlet specification version.
     *
     * @param provisionedServletVersion A provisioned servlet
     *     specification version.
     *
     * @return The minimum JavaEE version which is required
     *     to support the specified specification version.
     */
    protected static int getEEVersion(int provisionedServletVersion) {
        int minEEVersion;
        if (provisionedServletVersion == 50) { // TODO: Make this '>='?
            minEEVersion = 90;
        } else if (provisionedServletVersion == 40) {
            minEEVersion = 80;
        } else if (provisionedServletVersion == 31) {
            minEEVersion = 70;
        } else {
            minEEVersion = 60; // The lowest that liberty can support.
        }
        return minEEVersion;
    }
    
    public WebAppDDParser(Container ddRootContainer, Entry ddEntry, int maxVersion)
        throws ParseException {

        super( ddRootContainer, ddEntry,
               TRIM_SIMPLE_CONTENT,
               maxVersion, getEEVersion(maxVersion) );
    }

    @Override
    public WebApp parse() throws ParseException {
        super.parseRootElement();
        return (WebApp) rootParsable;
    }

    private static final String WEBAPP_DTD_PUBLIC_ID_22 =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    private static final String WEBAPP_DTD_PUBLIC_ID_23 =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement("web-app");

        String vers = requireVersionOrDTD();

        if (vers == null) {
            if (WEBAPP_DTD_PUBLIC_ID_22.equals(dtdPublicId)) {
                version = 22;
                eePlatformVersion = 12;
            } else if (WEBAPP_DTD_PUBLIC_ID_23.equals(dtdPublicId)) {
                version = 23;
                eePlatformVersion = 13;
            } else {
                throw new ParseException(errorInvalidPublicId());                
            }

        } else if ("2.4".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/j2ee");
            version = 24;
            eePlatformVersion = 14;

        } else if ("2.5".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = 25;
            eePlatformVersion = 50;
        } else if ("3.0".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = WebApp.VERSION_3_0;
            eePlatformVersion = 60;

        } else if ("3.1".equals(vers)) {
            requireProvisioning(31, vers);
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");
            version = WebApp.VERSION_3_1;
            eePlatformVersion = 70;
        } else if ("4.0".equals(vers)) {
            requireProvisioning(40, vers);
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");
            version = WebApp.VERSION_4_0;
            eePlatformVersion = 80;

        } else if ("5.0".equals(vers)) {
            requireProvisioning(50, vers);
            requireNamespace(vers, "https://jakarta.ee/xml/ns/jakartaee");
            version = WebApp.VERSION_5_0;
            eePlatformVersion = 90;

        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }

        return new WebAppType(getDeploymentDescriptorPath());
    }
}
