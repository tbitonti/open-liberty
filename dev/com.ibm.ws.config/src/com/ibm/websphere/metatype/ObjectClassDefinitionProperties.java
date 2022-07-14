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

import java.util.List;

public class ObjectClassDefinitionProperties {

    public ObjectClassDefinitionProperties(String id) {
        this.id = id;
    }

    private String extend;
    private String extendsAlias;

    public String getExtends() {
        return extend;
    }

    public void setExtends(String extend) {
        this.extend = extend;
    }

    public String getExtendsAlias() {
        return extendsAlias;
    }

    public void setExtendsAlias(String extendsAlias) {
        this.extendsAlias = extendsAlias;
    }

    private final String id;

    public String getId() {
        return id;
    }

    private String name;
    private String description;
    private String alias;

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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    //

    private List<String> objectClass;

    public List<String> getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(List<String> objectClass) {
        this.objectClass = objectClass;
    }

    //

    private String parentPid;
    private String childalias;

    public String getParentPID() {
        return parentPid;
    }

    public void setParentPID(String parentPid) {
        this.parentPid = parentPid;
    }

    public String getChildalias() {
        return childalias;
    }

    public void setChildalias(String childalias) {
        this.childalias = childalias;
    }

    //

    private boolean supportsExtensions;
    private boolean supportsHiddenExtensions;

    public boolean supportsExtensions() {
        return supportsExtensions;
    }

    public void setSupportsExtensions(boolean supportsExtensions) {
        this.supportsExtensions = supportsExtensions;
    }

    public boolean supportsHiddenExtensions() {
        return supportsHiddenExtensions;
    }

    public void setSupportsHiddenExtensions(boolean supportsHiddenExtensions) {
        this.supportsHiddenExtensions = supportsHiddenExtensions;
    }
}
