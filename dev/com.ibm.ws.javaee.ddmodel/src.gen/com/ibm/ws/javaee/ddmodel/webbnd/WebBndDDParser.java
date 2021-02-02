/*******************************************************************************
 * Copyright (c) 2017, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.ddmodel.webbnd;

import com.ibm.ws.javaee.dd.web.WebApp;
import com.ibm.ws.javaee.dd.webbnd.WebBnd;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class WebBndDDParser extends DDParser {
    public WebBndDDParser(Container ddRootContainer, Entry ddEntry, boolean xmi)
        throws ParseException {
        super(ddRootContainer, ddEntry, WebApp.class, xmi);
    }

    @Override
    public WebBnd parse() throws ParseException {
        super.parseRootElement();
        return (WebBnd) rootParsable;
    }

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        if (!xmi) {
            return createXMLRootParsable();
        } else {
            ParsableElement rootParsableElement = createXMIRootParsable();
            namespace = null; // TODO: Why clear this?
            return rootParsableElement;
        }
    }

    private ParsableElement createXMIRootParsable() throws ParseException {
        requireRootElement("WebAppBinding");
        requireNamespace("webappbnd.xmi");
        version = 9; // TODO: This is a strange version.
        return new WebBndType(getDeploymentDescriptorPath(), true);
    }

    private ParsableElement createXMLRootParsable() throws ParseException {
        requireRootElement("web-bnd");
        requireNamespace("http://websphere.ibm.com/xml/ns/javaee");

        String vers = requireVersion();
        if ("1.0".equals(vers)) {
            version = 10;
        } else if ("1.1".equals(vers)) {
            version = 11;
        } else if ("1.2".equals(vers)) {
            version = 12;
        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }
        
        return new WebBndType(getDeploymentDescriptorPath());
    }
}
