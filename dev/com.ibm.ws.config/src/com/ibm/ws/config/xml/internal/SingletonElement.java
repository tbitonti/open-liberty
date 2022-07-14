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

public class SingletonElement extends MetaTypeElement {

    public SingletonElement(String nodeName, String pid) {
        super(nodeName, pid);
    }

    public SingletonElement(SimpleElement configElement, String pid) {
        super(configElement, pid);
    }

    public SingletonElement(List<SimpleElement> elements, String pid) throws ConfigMergeException {
        this(elements.get(0), elements, pid);
    }

    protected SingletonElement(SimpleElement firstElement, List<SimpleElement> elements, String pid) throws ConfigMergeException {
        super(firstElement.getNodeName(), pid);

        this.setMergeBehavior(firstElement.mergeBehavior);
        this.setDocumentLocation(firstElement.getDocumentLocation());

        this.merge(elements);
    }

    @Override
    @Trivial
    public boolean isSingleton() {
        return true;
    }

    @Override
    @Trivial
    public String getId() {
        return null;
    }

    @Override
    public ConfigID getConfigID() {
        return new ConfigID(pid);
    }
}
