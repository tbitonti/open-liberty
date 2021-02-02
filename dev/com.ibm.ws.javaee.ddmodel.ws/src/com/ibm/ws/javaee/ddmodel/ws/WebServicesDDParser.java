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
package com.ibm.ws.javaee.ddmodel.ws;

import com.ibm.ws.javaee.dd.ws.Webservices;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public final class WebServicesDDParser extends DDParser {
    public WebServicesDDParser(Container ddRootContainer, Entry ddEntry)
        throws ParseException {
        super(ddRootContainer, ddEntry);
    }

    @Override
    public Webservices parse() throws ParseException {
        super.parseRootElement();
        return (Webservices) rootParsable;
    }

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement("webservices");

        String vers = requireVersion();
        if ( "2.0".equals(vers) ||
             "1.4".equals(vers) || "1.3".equals(vers) ||
             "1.2".equals(vers) || "1.1".equals(vers) ) {
            return new WebservicesType(getDeploymentDescriptorPath());
        } else {
            throw new ParseException(errorInvalidVersion(vers));
        }
    }
}
