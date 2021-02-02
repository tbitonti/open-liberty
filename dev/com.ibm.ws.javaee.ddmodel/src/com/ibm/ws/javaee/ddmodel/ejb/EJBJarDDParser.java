/*******************************************************************************
 * Copyright (c) 2012, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.ddmodel.ejb;

import com.ibm.ws.javaee.dd.ejb.EJBJar;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class EJBJarDDParser extends DDParser {
    public EJBJarDDParser(Container ddRootContainer, Entry ddEntry, int maxVersion)
        throws ParseException {
        super(ddRootContainer, ddEntry, maxVersion);
    }

    @Override
    public EJBJar parse() throws ParseException {
        super.parseRootElement();
        return (EJBJar) rootParsable;
    }

    private static final String EJBJAR_DTD_PUBLIC_ID_11 =
        "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN";
    private static final String EJBJAR_DTD_PUBLIC_ID_20 =
        "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN";

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement("ejb-jar");

        String vers = requireVersionOrDTD();
        if (vers == null) {
            if (EJBJAR_DTD_PUBLIC_ID_11.equals(dtdPublicId)) {
                version = EJBJar.VERSION_1_1;
                eePlatformVersion = 12;
            } else if (EJBJAR_DTD_PUBLIC_ID_20.equals(dtdPublicId)) {
                version = EJBJar.VERSION_2_0;
                eePlatformVersion = 13;
            } else {
                throw new ParseException(errorInvalidPublicId());
            }

        } else if ("2.1".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/j2ee");
            version = EJBJar.VERSION_2_1;
            eePlatformVersion = 14;

        } else if ("3.0".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = EJBJar.VERSION_3_0;
            eePlatformVersion = 50;
        } else if ("3.1".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = EJBJar.VERSION_3_1;
            eePlatformVersion = 60;

        } else if ("3.2".equals(vers)) {
            requireProvisioning(EJBJar.VERSION_3_2, vers);
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");
            version = EJBJar.VERSION_3_2;
            eePlatformVersion = 70;

        } else if ("4.0".equals(vers)) {
            requireProvisioning(EJBJar.VERSION_4_0, vers); 
            requireNamespace(vers, "https://jakarta.ee/xml/ns/jakartaee");
            version = EJBJar.VERSION_4_0;
            eePlatformVersion = 90;

        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }

        return new EJBJarType(getDeploymentDescriptorPath());
    }
}
