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

import com.ibm.ws.config.xml.internal.XMLConfigParser.MergeBehavior;

/**
 * This is a ConfigElement that corresponds to an XML element from the configuration. It has not
 * been processed with metatype information.
 */
public class SimpleElement extends ConfigElement {

    public SimpleElement(String nodeName) {
        super(nodeName);
    }

    public SimpleElement(String nodeName, int sequenceId,
                         List<String> docLocationStack,
                         List<MergeBehavior> behaviorStack) {
        super(nodeName, sequenceId, docLocationStack, behaviorStack);
    }

    public SimpleElement(ConfigElement configElement) {
        super(configElement);

        this.setId(configElement.getId());
    }

    public SimpleElement(ConfigElement configElement, String id, boolean isDefault) {
        super(configElement);

        this.id = id;
        this.usingDefaultId = isDefault;
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    private String id;
    protected boolean usingDefaultId;

    protected void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setDefaultId(int index) {
        if ((getId() == null) && (index > -1)) {
            setId("default-" + index);
            usingDefaultId = true;
        }
    }

    public boolean isUsingNonDefaultId() {
        if (getId() == null) {
            return false;
        }
        return !usingDefaultId;
    }
}
