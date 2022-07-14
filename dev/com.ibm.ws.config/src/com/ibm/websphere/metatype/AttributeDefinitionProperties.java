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
package com.ibm.websphere.metatype;

public class AttributeDefinitionProperties {

    public AttributeDefinitionProperties(String id) {
        this.id = id;
    }

    private final String id;

    public String getId() {
        return id;
    }

    private String referencePid;

    public String getReferencePid() {
        return referencePid;
    }

    public void setReferencePid(String referencePid) {
        this.referencePid = referencePid;
    }

    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private int type;
    private boolean isFinal;
    private boolean flat;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isFlat() {
        return flat;
    }

    public void setFlat(boolean flat) {
        this.flat = flat;
    }

    private int cardinality;

    public int getCardinality() {
        return cardinality;
    }

    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    private String[] defaultValue;

    public String[] getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String[] defaultValue) {
        this.defaultValue = defaultValue;
    }

    private String[] optionLabels;
    private String[] optionValues;

    public String[] getOptionLabels() {
        return optionLabels;
    }

    public void setOptionLabels(String[] optionLabels) {
        this.optionLabels = optionLabels;
    }

    public String[] getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(String[] optionValues) {
        this.optionValues = optionValues;
    }

    private String service;
    private String serviceFilter;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServiceFilter() {
        return serviceFilter;
    }

    public void setServiceFilter(String serviceFilter) {
        this.serviceFilter = serviceFilter;
    }

    private String variable;

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    private String uniqueCategory;

    public boolean isUnique() {
        return uniqueCategory != null;
    }

    public String getUnique() {
        return this.uniqueCategory;
    }

    public void setUnique(String unique) {
        this.uniqueCategory = unique;
    }

    private String copyOf;

    public String getCopyOf() {
        return copyOf;
    }

    public void setCopyOf(String copyOf) {
        this.copyOf = copyOf;
    }
}
