/*******************************************************************************
 * Copyright (c) 1997,2023 IBM Corporation and others.
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
package com.ibm.ws.cache.test.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CacheKey implements Externalizable {

    private String key;

    public CacheKey() {
        this.key = null;
    }

    public CacheKey(String key) {
        this.key = key;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(key);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        try {
            key = (String) in.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException("Read failure", ex);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (this == other) {
            return true;
        } else if (!(other instanceof CacheKey)) {
            return false;
        }

        String thisKey = this.key;
        String otherKey = ((CacheKey) other).key;
        if (thisKey == null) {
            return (otherKey == null);
        } else {
            return thisKey.equals(otherKey);
        }
    }

    @Override
    public int hashCode() {
        return ((key == null) ? 0 : key.hashCode());
    }

    @Override
    public String toString() {
        return key;
    }
}
