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
package com.ibm.ws.javaee.ddmodel.bval;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.javaee.dd.bval.ValidationConfig;
import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class ValidationConfigDDParser extends DDParser {
    private static final TraceComponent tc = Tr.register(ValidationConfigDDParser.class);

    public ValidationConfigDDParser(Container ddRootContainer, Entry ddEntry)
        throws ParseException {
        super(ddRootContainer, ddEntry);
    }

    @Override
    public ValidationConfig parse() throws ParseException {
        super.parseRootElement();
        return (ValidationConfig) rootParsable;
    }

    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        // Alone among the several descriptor types, validation configuration
        // descriptors may be missing their root element.
        // See com.ibm.ws.javaee.ddmodel.DDParser.parseRootElement().
        if (!"validation-config".equals(rootElementLocalName)) {
            Tr.warning(tc, "BVKEY_NOT_A_BEAN_VALIDATION_XML", getModuleName());
            return null;
        }

        // Null version:
        //   From javaee 6; a specific namespace value is required.
        // "1.0" version:
        //   Not allowed
        // "1.1":
        //   From javaee 7: a specific namespace value is required

        String vers = getAttributeValue("", "version");
        if (vers == null) {
            requireNamespace("http://jboss.org/xml/ns/javax/validation/configuration");
            version = ValidationConfig.VERSION_1_0;
        } else if ("1.1".equals(vers)) {
            requireNamespace(vers, "http://jboss.org/xml/ns/javax/validation/configuration");
            version = ValidationConfig.VERSION_1_1;
        } else {
            // Covers the case when the version is "1.0"
            throw new ParseException(errorInvalidVersion(vers));
        }
        
        return new ValidationConfigType(getDeploymentDescriptorPath());
    }
}
