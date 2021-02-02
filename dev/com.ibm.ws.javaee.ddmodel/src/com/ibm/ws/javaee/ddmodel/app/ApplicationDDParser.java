/*******************************************************************************
 * Copyright (c) 2011, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.ddmodel.app;

import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.ws.javaee.version.JavaEEVersion;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public final class ApplicationDDParser extends DDParser {
    public ApplicationDDParser(Container ddRootContainer, Entry ddEntry, int maxVersion)
        throws ParseException {
        super(ddRootContainer, ddEntry, maxVersion);
    }

    @Override
    public Application parse() throws ParseException {
        super.parseRootElement();
        return (Application) rootParsable;
    }

    private static final String APPLICATION_DTD_PUBLIC_ID_12 =
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN";
    private static final String APPLICATION_DTD_PUBLIC_ID_13 =
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN";

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement("application");

        String vers = requireVersionOrDTD();

        if (vers == null) {
            if (APPLICATION_DTD_PUBLIC_ID_12.equals(dtdPublicId)) {
                version = Application.VERSION_1_2;
                eePlatformVersion = 12;
            } else if (APPLICATION_DTD_PUBLIC_ID_13.equals(dtdPublicId)) {
                version = Application.VERSION_1_3;
                eePlatformVersion = 13;
            } else {
                throw new ParseException(errorInvalidPublicId());
            }

        } else if ("1.4".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/j2ee");
            version = Application.VERSION_1_4;
            eePlatformVersion = 14;
        } else if ("5".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = Application.VERSION_5;
            eePlatformVersion = 50;
        } else if ("6".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = Application.VERSION_6;
            eePlatformVersion = 60;

        } else if ("7".equals(vers)) {
            requireProvisioning(JavaEEVersion.VERSION_70, vers);
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");
            version = Application.VERSION_7;
            eePlatformVersion = 70;
        } else if ("8".equals(vers)) {
            requireProvisioning(JavaEEVersion.VERSION_80, vers);
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");
            version = Application.VERSION_8;
            eePlatformVersion = 80;

        } else if ("9".equals(vers)) {
            requireProvisioning(JavaEEVersion.VERSION_90, vers);
            requireNamespace(vers, "https://jakarta.ee/xml/ns/jakartaee");
            version = Application.VERSION_9;
            eePlatformVersion = 90;

        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }
        
        return new ApplicationType(getDeploymentDescriptorPath());
    }
}
