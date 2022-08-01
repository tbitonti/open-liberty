/*******************************************************************************
 * Copyright (c) 2011, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.config.xml.internal.variables;

import com.ibm.websphere.ras.annotation.Sensitive;
import com.ibm.ws.config.xml.internal.XMLConfigParser.MergeBehavior;

public class ConfigVariable extends AbstractLibertyVariable {
    public ConfigVariable(String name, @Sensitive String value, @Sensitive String defaultValue, MergeBehavior mergeBehavior, String location, boolean sensitive) {
        this.name = name;
        this.sensitive = sensitive;
        this.defaultValue = defaultValue;

        this.location = location;
        this.mergeBehavior = mergeBehavior;

        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ConfigVariable[");
        builder.append("name=").append(name).append(", ");
        builder.append("value=").append(getObscuredValue()).append(", ");
        builder.append("defaultValue=").append(getObscuredDefaultValue()).append(", ");
        builder.append("source=").append(Source.XML_CONFIG);
        builder.append("]");
        return builder.toString();
    }

    private final String name;
    private final boolean sensitive;
    private final String defaultValue;

    private final String location;
    private final MergeBehavior mergeBehavior;

    private final String value;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isSensitive() {
        return sensitive;
    }

    @Sensitive
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Source getSource() {
        return Source.XML_CONFIG;
    }

    public String getDocumentLocation() {
        return location;
    }

    public MergeBehavior getMergeBehavior() {
        return mergeBehavior;
    }

    @Sensitive
    @Override
    public String getValue() {
        return value;
    }
}
