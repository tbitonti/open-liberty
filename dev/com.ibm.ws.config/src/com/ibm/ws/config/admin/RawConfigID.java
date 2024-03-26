/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.config.admin;

import com.ibm.websphere.ras.annotation.Trivial;

/**
 * Most primitive configuration ID.
 *
 * Used for configuration ID lookups: Encapsulates
 * the configuration ID state and provides the base
 * hash and equality implementations.
 */
@Trivial
public class RawConfigID {

    public RawConfigID(ConfigID parent, String pid, String id, String childAttribute) {
        this.parent = parent;

        this.pid = pid;
        this.childAttribute = childAttribute;
        this.id = id;

        this.hashCode = asHash();
    }

    private final ConfigID parent;

    private final String pid;
    private final String id;
    private final String childAttribute;

    public String getPid() {
        return pid;
    }

    public String getId() {
        return id;
    }

    public ConfigID getParent() {
        return parent;
    }

    public String getChildAttribute() {
        return childAttribute;
    }

    //

    protected final int hashCode;

    private int asHash() {
        int prime = 31;

        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((pid == null) ? 0 : pid.hashCode());
        result = prime * result + ((childAttribute == null) ? 0 : childAttribute.hashCode());
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object otherObj) {
        if (this == otherObj) {
            return true;
        } else if (otherObj == null) {
            return false;
        } else if (!(otherObj instanceof RawConfigID)) {
            return false;
        }

        RawConfigID other = (RawConfigID) otherObj;
        if (hashCode != other.hashCode) {
            return false;
        }

        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }

        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }

        if (pid == null) {
            if (other.pid != null) {
                return false;
            }
        } else if (!pid.equals(other.pid)) {
            return false;
        }

        if (childAttribute == null) {
            if (other.childAttribute != null) {
                return false;
            }
        } else if (!childAttribute.equals(other.childAttribute)) {
            return false;
        }

        return true;
    }

    public ConfigID asConfigID() {
        return new ConfigID(parent, pid, id, childAttribute);
    }
}