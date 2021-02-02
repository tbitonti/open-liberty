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
package com.ibm.ws.javaee.ddmodel.managedbean;

import com.ibm.ws.javaee.dd.managedbean.ManagedBeanBnd;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class ManagedBeanBndDDParser extends DDParser {
    public ManagedBeanBndDDParser(Container ddRootContainer, Entry ddEntry)
        throws ParseException {
        super(ddRootContainer, ddEntry);
    }

    @Override
    public ManagedBeanBnd parse() throws ParseException {
        super.parseRootElement();
        return (ManagedBeanBnd) rootParsable;
    }

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement("managed-bean-bnd");
        requireNamespace("http://websphere.ibm.com/xml/ns/javaee");
        
        String vers = requireVersion();
        if ("1.0".equals(vers)) {
            version = 10;
        } else if ("1.1".equals(vers)) {
            version = 11;
        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }

        return new ManagedBeanBndType(getDeploymentDescriptorPath());
    }
}
