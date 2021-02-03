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
package com.ibm.ws.javaee.ddmodel.permissions;

import com.ibm.ws.javaee.dd.permissions.PermissionsConfig;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

final class PermissionsConfigDDParser extends DDParser {
    public PermissionsConfigDDParser(Container ddRootContainer, Entry ddEntry)
        throws ParseException {
        super(ddRootContainer, ddEntry);
    }

    @Override
    public PermissionsConfig parse() throws ParseException {
        super.parseRootElement();
        return (PermissionsConfig) rootParsable;
    }

    // TODO: The permissions configuration perhaps should
    //       respect max version.
    
    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement("permissions");

        String vers = requireVersion();
        if ("7".equals(vers)) {        
            requireNamespace(vers, "http://xmlns.jcp.org/xml/ns/javaee");
            version = 70; /// JavaEE7 and JavaEE8
        } else if ("9".equals(vers)) {
            requireNamespace(vers, "https://jakarta.ee/xml/ns/jakartaee");
            version = 90; // Jakarta9
        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }

        return new PermissionsConfigType(getDeploymentDescriptorPath());
    }
}
