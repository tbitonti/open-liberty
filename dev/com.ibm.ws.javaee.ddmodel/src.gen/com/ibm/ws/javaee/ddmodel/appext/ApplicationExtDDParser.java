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
package com.ibm.ws.javaee.ddmodel.appext;

import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.dd.appext.ApplicationExt;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class ApplicationExtDDParser extends DDParser {
    public ApplicationExtDDParser(Container ddRootContainer, Entry ddEntry, boolean xmi)
        throws ParseException {
        super(ddRootContainer, ddEntry, Application.class, xmi);
    }

    @Override
    public ApplicationExt parse() throws ParseException {
        super.parseRootElement();
        return (ApplicationExt) rootParsable;
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

    private ParsableElement createXMLRootParsable() throws ParseException {
        requireRootElement("application-ext");
        requireNamespace("http://websphere.ibm.com/xml/ns/javaee");

        String vers = requireVersion();
        if ("1.0".equals(vers)) {
            version = 10;
        } else if ("1.1".equals(vers)) {
            version = 11;
        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }

        return new ApplicationExtType(getDeploymentDescriptorPath());
    }
    

    private ParsableElement createXMIRootParsable() throws ParseException {
        requireRootElement("ApplicationExtension");
        requireNamespace("applicationext.xmi");
        version = 9; // TODO: This is a strange version.
        return new ApplicationExtType(getDeploymentDescriptorPath(), true);
    }    
}
