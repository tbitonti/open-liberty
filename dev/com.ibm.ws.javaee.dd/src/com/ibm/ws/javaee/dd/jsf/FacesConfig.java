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
package com.ibm.ws.javaee.dd.jsf;

import java.util.List;

import com.ibm.ws.javaee.dd.DeploymentDescriptor;

public interface FacesConfig extends DeploymentDescriptor {
    String DD_NAME = "WEB-INF/faces-config.xml";

    int VERSION_2_0 = 20;
    int VERSION_2_2 = 22;
    int VERSION_2_3 = 23;
    int VERSION_3_0 = 30;

    String getVersion();

    List<FacesConfigManagedBean> getManagedBeans();
    List<String> getManagedObjects(); // Added for CDI 1.2
}
