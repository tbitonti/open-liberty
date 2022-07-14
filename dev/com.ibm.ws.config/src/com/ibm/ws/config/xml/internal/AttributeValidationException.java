/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.config.xml.internal;

import com.ibm.websphere.config.ConfigEvaluatorException;
import com.ibm.ws.config.xml.internal.metatype.ExtendedAttributeDefinition;

class AttributeValidationException extends ConfigEvaluatorException {
    private static final long serialVersionUID = -8873485148740653410L;

    public AttributeValidationException(ExtendedAttributeDefinition definition, String value, String result) {
        super(result);

        this.definition = definition;
        this.value = value;
        this.result = result;
    }

    private final ExtendedAttributeDefinition definition;
    private final String value;
    private final String result;

    public ExtendedAttributeDefinition getAttributeDefintion() {
        return definition;
    }

    public Object getValue() {
        return this.value;
    }

    public String getValidateResult() {
        return result;
    }
}
