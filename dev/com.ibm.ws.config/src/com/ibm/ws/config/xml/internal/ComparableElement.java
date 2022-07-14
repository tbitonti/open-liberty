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

import com.ibm.websphere.ras.annotation.Trivial;

public class ComparableElement extends SimpleElement {
    public ComparableElement(SimpleElement element, int index, String pid, String defaultId) {
        super(element);

        this.pid = pid;

        String useId = element.getId();
        if ((useId == null) || useId.startsWith("default")) {
            if (defaultId == null) {
                useId = (index < 1) ? "default-0" : "default-" + index;
                this.usingDefaultId = true;
            } else {
                useId = defaultId;
            }
        }
        this.id = useId;
    }

    //

    private final String id;

    @Override
    @Trivial
    public String getId() {
        return id;
    }

    //

    private final String pid;

    /**
     * Override: If a PID was provided, use that as the node name. Otherwise,
     * obtain the usual node name. See {@link ConfigElement#getNodeName()}.
     *
     * Child-first processing in ConfigEvaluator apparently needs to deal
     * with both the original XML element node name and the PID.
     *
     * @return A node name for this element. Either, the PID, or the
     *         usual node name.
     */
    @Override
    @Trivial
    public String getNodeName() {
        return ((pid == null) ? super.getNodeName() : pid);
    }
}
