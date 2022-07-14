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

import java.util.List;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.config.admin.ConfigID;
import com.ibm.ws.config.xml.internal.MetaTypeRegistry.RegistryEntry;

public class FactoryElement extends MetaTypeElement {

    /**
     * Assign an ID for a new factory element.
     *
     * If the element is using a non-default ID (which means the element
     * has an ID obtained from a server configuration file), use that
     * assigned ID as the factory element ID.
     *
     * Otherwise, if the entry has a default ID, use that. If the entry
     * does not have a default ID, assign a default ID using the supplied
     * index.
     *
     * @param element An element conditionally used to obtain the ID.
     * @param entry   An entry conditionally used to obtain the ID.
     * @param index   The index of a new default ID, if one is generated.
     *
     * @return An ID for a new factory element.
     */
    private String assignId(SimpleElement element, RegistryEntry entry, int index) {
        if (element.isUsingNonDefaultId()) {
            return element.getId();
        } else {
            String id = entry.getDefaultId();
            if (id == null) {
                id = (index < 1) ? "default-0" : "default-" + index;
            }
            return id;
        }
    }

    public FactoryElement(SimpleElement element, int index, RegistryEntry entry) {
        super(element, entry.getPid());

        this.id = assignId(element, entry, index);
    }

    public FactoryElement(List<SimpleElement> elements, String pid, String id) throws ConfigMergeException {
        this(elements.get(0), elements, pid, id);
    }

    public FactoryElement(SimpleElement firstElement, List<SimpleElement> elements, String pid, String id) throws ConfigMergeException {
        super(firstElement.getNodeName(), pid);
        this.setMergeBehavior(firstElement.mergeBehavior);
        this.setDocumentLocation(firstElement.getDocumentLocation());

        this.id = id;

        merge(elements);
    }

    @Override
    @Trivial
    public boolean isFactory() {
        return true;
    }

    //

    @Override
    @Trivial
    public String getNodeName() {
        return pid;
    }

    //

    private final String id;

    @Override
    @Trivial
    public String getId() {
        return id;
    }

    // Attribute name is unique under a parent, pid may not be, so distinguish using the attr name
    // For example, <AD id="fooRef" ibm:reference="com.ibm.ws.foobar"/>
    //              <AD id="barRef" ibm:reference="com.ibm.ws.foobar"/>

    @Override
    @Trivial
    public ConfigID getConfigID() {
        ConfigElement useParent = getParent();

        if (useParent == null) {
            return new ConfigID(this.pid, getId());

        } else {
            return new ConfigID(useParent.getConfigID(), this.pid, getId(), childAttributeName);
        }
    }
}
