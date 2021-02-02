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
package com.ibm.ws.javaee.ddmodel.jsf;

import com.ibm.ws.javaee.dd.jsf.FacesConfig;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

final class FacesConfigDDParser extends DDParser {
    public FacesConfigDDParser(Container ddRootContainer, Entry ddEntry)
        throws ParseException {
        this(ddRootContainer, ddEntry, FacesConfigAdapter.DEFAULT_MAX_VERSION);
    }

    public FacesConfigDDParser(Container ddRootContainer, Entry ddEntry, int maxVersion)
        throws ParseException {
        super(ddRootContainer, ddEntry, maxVersion);
    }

    public int getFacesBundleLoadedVersion() {
        return maxVersion;
    }

    @Override
    public FacesConfig parse() throws ParseException {
        super.parseRootElement();
        return (FacesConfig) rootParsable;
    }

    private static final String FACES_CONFIG_DTD_PUBLIC_ID_10 =
        "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN";
    private static final String FACES_CONFIG_DTD_PUBLIC_ID_11 =
        "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN";

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement("faces-config");

        String vers = requireVersionOrDTD();
        if (vers == null) {
            if (FACES_CONFIG_DTD_PUBLIC_ID_10.equals(dtdPublicId)) {
                version = 10;
            } else if (FACES_CONFIG_DTD_PUBLIC_ID_11.equals(dtdPublicId)) {
                version = 11;
            } else {
                throw new ParseException(errorInvalidPublicId());
            }

        } else if ("1.2".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = 12; // JavaEE5
        } else if ("2.0".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = 20; // JavaEE6
        } else if ("2.1".equals(vers)) {
            requireNamespace(vers, "http://java.sun.com/xml/ns/javaee");
            version = 21; // JavaEE6

        } else if ("2.2".equals(vers)) {
            requireProvisioning(22, vers);
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");
            version = 22; // JavaEE7            
        } else if ("2.3".equals(vers)) {
            requireProvisioning(23, vers);
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");            
            version = 23; // JavaEE8

        } else if ("3.0".contentEquals(vers)) {
            requireProvisioning(30, vers);
            requireNamespace(vers, "https://jakarta.ee/xml/ns/jakartaee");
            version = 30; // Jakarta 9

        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }

        return new FacesConfigType(getDeploymentDescriptorPath());
    }
}