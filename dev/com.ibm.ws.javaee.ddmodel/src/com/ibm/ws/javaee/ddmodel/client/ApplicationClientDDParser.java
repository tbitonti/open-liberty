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
package com.ibm.ws.javaee.ddmodel.client;

import com.ibm.ws.javaee.dd.client.ApplicationClient;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class ApplicationClientDDParser extends DDParser {
    public ApplicationClientDDParser(Container ddRootContainer, Entry ddEntry, int maxVersion)
        throws ParseException {
        super(ddRootContainer, ddEntry, maxVersion);
    }

    @Override
    public ApplicationClient parse() throws ParseException {
        super.parseRootElement();
        return (ApplicationClient) rootParsable;
    }

    private static final String APPCLIENT_DTD_PUBLIC_ID_12 =
        "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.2//EN";
    private static final String APPCLIENT_DTD_PUBLIC_ID_13 =
        "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.3//EN";

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement("application-client");

        String vers = requireVersionOrDTD();

        if (vers == null) {
            if (APPCLIENT_DTD_PUBLIC_ID_12.equals(dtdPublicId)) {
                version = ApplicationClient.VERSION_1_2;
                eePlatformVersion = 12;
            } else if (APPCLIENT_DTD_PUBLIC_ID_13.equals(dtdPublicId)) {
                version = ApplicationClient.VERSION_1_3;
                eePlatformVersion = 13;
            } else {
                throw new ParseException(errorInvalidPublicId());
            }

        } else if ("1.4".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/j2ee");
            version = ApplicationClient.VERSION_1_4;
            eePlatformVersion = 14;

        } else if ("5".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = ApplicationClient.VERSION_5;
            eePlatformVersion = 50;
        } else if ("6".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = ApplicationClient.VERSION_6;
            eePlatformVersion = 60;

        } else if ("7".equals(vers)) {
            requireProvisioning(ApplicationClient.VERSION_7, vers);
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");
            version = ApplicationClient.VERSION_7;
            eePlatformVersion = 70;
        } else if ("8".equals(vers)) {
            requireProvisioning(ApplicationClient.VERSION_8, vers);
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");
            version = ApplicationClient.VERSION_8;
            eePlatformVersion = 80;

        } else if ("9".equals(vers)) {
            requireProvisioning(ApplicationClient.VERSION_9, vers);
            requireNamespace(vers, "https://jakarta.ee/xml/ns/jakartaee");
            version = ApplicationClient.VERSION_9;
            eePlatformVersion = 90;

        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }
        
        return new ApplicationClientType(getDeploymentDescriptorPath());
    }
}
