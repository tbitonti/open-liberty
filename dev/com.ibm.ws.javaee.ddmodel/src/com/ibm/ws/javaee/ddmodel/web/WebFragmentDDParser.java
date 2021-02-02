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
import com.ibm.ws.javaee.dd.web.WebFragment;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.ws.javaee.ddmodel.web.common.WebFragmentType;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class WebFragmentDDParser extends DDParser {
    public WebFragmentDDParser(Container ddRootContainer, Entry ddEntry, int provisionedServletVersion)
        throws ParseException {

        super( ddRootContainer, ddEntry,
               TRIM_SIMPLE_CONTENT,
               provisionedServletVersion,
               WebAppDDParser.getEEVersion(provisionedServletVersion) );
    }

    @Override
    public WebFragment parse() throws ParseException {
        super.parseRootElement();
        return (WebFragment) rootParsable;
    }

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement("web-fragment");

        String vers = requireVersion();

        if ("3.0".equals(vers)) {
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
            eePlatformVersion = 80; // TODO: Should this be '90'?

        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }
        
        return new WebFragmentType(getDeploymentDescriptorPath());
    }
}
