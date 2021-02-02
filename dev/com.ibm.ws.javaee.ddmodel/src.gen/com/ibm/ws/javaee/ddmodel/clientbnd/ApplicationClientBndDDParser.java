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
package com.ibm.ws.javaee.ddmodel.clientbnd;

import com.ibm.ws.javaee.dd.client.ApplicationClient;
import com.ibm.ws.javaee.dd.clientbnd.ApplicationClientBnd;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class ApplicationClientBndDDParser extends DDParser {
    public ApplicationClientBndDDParser(Container ddRootContainer, Entry ddEntry, boolean xmi)
        throws ParseException {
        super(ddRootContainer, ddEntry, ApplicationClient.class, xmi);
    }

    @Override
    public ApplicationClientBnd parse() throws ParseException {
        super.parseRootElement();
        return (ApplicationClientBnd) rootParsable;
    }

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        if (!xmi) {
            return createXMLRootParsable();
        } else {
            ParsableElement rootParsableElement = createXMIRootParsable();
            namespace = null;
            return rootParsableElement;
        }
    }

    private ParsableElement createXMLRootParsable() throws ParseException {
        requireRootElement("application-client-bnd");
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

        return new ApplicationClientBndType(getDeploymentDescriptorPath());
    }
    
    private ParsableElement createXMIRootParsable() throws ParseException {
        requireRootElement("ApplicationClientBinding");
        requireNamespace("clientbnd.xmi");
        version = 9; // TODO: This is a strange version.
        return new ApplicationClientBndType(getDeploymentDescriptorPath(), true);
    }
}
