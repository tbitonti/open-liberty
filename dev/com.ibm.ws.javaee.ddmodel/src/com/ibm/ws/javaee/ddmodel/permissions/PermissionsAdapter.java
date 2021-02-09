/*******************************************************************************
 * Copyright (c) 2015, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.ddmodel.permissions;

import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.permissions.PermissionsConfig;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public final class PermissionsAdapter implements DDAdapter, ContainerAdapter<PermissionsConfig> {

    @Override
    @FFDCIgnore(ParseException.class)
    public PermissionsConfig adapt(Container root,
                                   OverlayContainer rootOverlay,
                                   ArtifactContainer artifactContainer,
                                   Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);

        // The permissions configuration is not cached.

        Entry ddEntry = containerToAdapt.getEntry(PermissionsConfig.DD_NAME);
        if (ddEntry == null) {
            return null;
        }

        PermissionsConfig permissionsConfig;
        try {
            PermissionsConfigDDParser ddParser =
                new PermissionsConfigDDParser(containerToAdapt, ddEntry);
            permissionsConfig = ddParser.parse();
        } catch (ParseException e) {
            throw new UnableToAdaptException(e);
        }
        return permissionsConfig;
    }
}
