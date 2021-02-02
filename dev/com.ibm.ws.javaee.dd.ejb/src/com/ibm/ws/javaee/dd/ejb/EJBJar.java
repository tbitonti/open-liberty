/*******************************************************************************
 * Copyright (c) 2012, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.dd.ejb;

import java.util.List;

import com.ibm.ws.javaee.dd.common.ModuleDeploymentDescriptor;

public interface EJBJar extends ModuleDeploymentDescriptor {
    int VERSION_1_1 = 11;
    int VERSION_2_0 = 20;
    int VERSION_2_1 = 21;
    int VERSION_3_0 = 30;
    int VERSION_3_1 = 31;
    int VERSION_3_2 = 32;
    int VERSION_4_0 = 40;

    int getVersionID();

    /**
     * @return &lt;metadata-complete> if specified, false if unspecified, or
     *         false if {@link #getVersionID} is less than {@link #VERSION_3_0} (though
     *         these module versions are semantically metadata-complete per the EJB 3.0
     *         specification)
     */
    boolean isMetadataComplete();

    /**
     * @return &lt;session>, &lt;entity>, and &lt;message-driven> as a read-only
     *         list
     */
    List<EnterpriseBean> getEnterpriseBeans();

    /**
     * @return &lt;interceptors>, or null if unspecified
     */
    Interceptors getInterceptors();

    /**
     * @return &lt;relationships>, or null if unspecified
     */
    Relationships getRelationshipList();

    /**
     * @return &lt;assembly-descriptor>, or null if unspecified
     */
    AssemblyDescriptor getAssemblyDescriptor();

    /**
     * @return &lt;ejb-client-jar>, or null if unspecified
     */
    String getEjbClientJar();
}
