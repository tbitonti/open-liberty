/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.config.xml.internal.metatype;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.ObjectClassDefinition;

import com.ibm.websphere.metatype.ObjectClassDefinitionProperties;

//@formatter:off
public class WSObjectClassDefinitionImpl implements ObjectClassDefinition {
    public WSObjectClassDefinitionImpl(
        ObjectClassDefinitionProperties properties,
        List<AttributeDefinition> required,
        List<AttributeDefinition> optional) {

        this.required = new ArrayList<>(required);
        this.optional = new ArrayList<>(optional);
        this.properties = properties;
    }

    @Override
    public InputStream getIcon(int arg0) throws IOException {
        return null;
    }

    public String getChildAlias() {
        return null;
    }

    //

    private final ArrayList<AttributeDefinition> required;

    public void addAttributeDefinition(AttributeDefinition definition) {
        required.add(definition);
    }

    private final ArrayList<AttributeDefinition> optional;

    @Override
    public AttributeDefinition[] getAttributeDefinitions(int filter) {
        int numRequired;
        int numOptional;

        if ( filter == ObjectClassDefinition.ALL ) {
            numRequired = required.size();
            numOptional = optional.size();
        } else if (filter == ObjectClassDefinition.OPTIONAL) {
            numRequired = 0;
            numOptional = optional.size();

        } else if (filter == ObjectClassDefinition.REQUIRED) {
            numRequired = required.size();
            numOptional = 0;

        } else {
            throw new IllegalArgumentException("Unexpected filter value: " + filter);
        }

        AttributeDefinition[] filtered = new AttributeDefinition[ numRequired + numOptional ];
        for ( int adNo = 0; adNo < numRequired; adNo++ ) {
            filtered[adNo] = required.get(adNo);
        }
        for ( int adNo = 0; adNo < numOptional; adNo++ ) {
            filtered[adNo + numRequired] = optional.get(adNo);
        }

        return filtered;
    }

    //

    private final ObjectClassDefinitionProperties properties;

    @Override
    public String getID() {
        return properties.getId();
    }

    public List<String> getObjectClass() {
        return properties.getObjectClass();
    }

    @Override
    public String getName() {
        return properties.getName();
    }

    public String getAlias() {
        return properties.getAlias();
    }

    @Override
    public String getDescription() {
        return properties.getDescription();
    }

    public String getParentPID() {
        return properties.getParentPID();
    }

    public String getExtendsAlias() {
        return properties.getExtendsAlias();
    }

    public String getExtends() {
        return properties.getExtends();
    }

    public boolean supportsExtensions() {
        return properties.supportsExtensions();
    }

    public boolean supportsHiddenExtensions() {
        return properties.supportsHiddenExtensions();
    }
}
//@formatter:on