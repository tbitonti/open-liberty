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
package com.ibm.ws.javaee.dd;

/**
 * Common type for all JavaEE / Jakarta deployment descriptors.
 */
public interface DeploymentDescriptor {
    /**
     * The path to the descriptor.  Relative to the top of the
     * enclosing archives.  Does not start with a '/'.
     * 
     * Usually within "META-INF".  For web module archives, within
     * "WEB-INF".
     *
     * @return The path to the descriptor.
     */
    String getDeploymentDescriptorPath();

    /**
     * Answer the component of the descriptor which contains a specified
     * "id" value.  Null if no element of the descriptor contains
     * a matching value.
     * 
     * @param id The ID value which is to be located.
     *
     * @return The component of the descriptor which contains a matching
     *     "id" value.
     */
    Object getComponentForId(String id);

    /**
     * Answer the value of the "id" attribute of a component of this
     * descriptor.  Answer null if the component does not have an "id"
     * attribute, or if that attribute is unset.
     *
     * @param A component of this descriptor from which to retrieve
     *     the "id" attribute value.
     *
     * @return The "id" attribute value of the component.
     */
    String getIdForComponent(Object ddComponent);
}
