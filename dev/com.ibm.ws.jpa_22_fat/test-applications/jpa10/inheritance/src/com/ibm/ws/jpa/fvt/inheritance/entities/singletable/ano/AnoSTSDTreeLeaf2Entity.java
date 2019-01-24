/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.jpa.fvt.inheritance.entities.singletable.ano;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.ibm.ws.jpa.fvt.inheritance.entities.ITreeLeaf2;

@Entity
@DiscriminatorValue("AnoSTSDLeaf2")
public class AnoSTSDTreeLeaf2Entity extends AnoSTSDTreeRootEntity implements ITreeLeaf2 {
    public AnoSTSDTreeLeaf2Entity() {
        super();
    }

    private float floatVal;

    @Override
    public float getFloatVal() {
        return floatVal;
    }

    @Override
    public void setFloatVal(float floatVal) {
        this.floatVal = floatVal;
    }

    @Override
    public String toString() {
        return "AnoSTSDTreeLeaf2Entity [floatVal=" + floatVal + ", getId()=" + getId() + ", getName()=" + getName()
               + "]";
    }
}
