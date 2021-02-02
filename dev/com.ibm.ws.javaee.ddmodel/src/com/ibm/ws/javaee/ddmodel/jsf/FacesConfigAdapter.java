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
package com.ibm.ws.javaee.ddmodel.jsf;

import org.osgi.framework.ServiceReference;

import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.jsf.FacesConfig;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.ws.javaee.version.FacesVersion;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public final class FacesConfigAdapter implements DDAdapter, ContainerAdapter<FacesConfig> {
    protected static final int DEFAULT_MAX_VERSION = FacesConfig.VERSION_2_0;

    private ServiceReference<FacesVersion> maxVersionRef;
    private volatile int maxVersion = DEFAULT_MAX_VERSION;

    public synchronized void setVersion(ServiceReference<FacesVersion> versionRef) {
        maxVersionRef = versionRef;
        maxVersion = (Integer) versionRef.getProperty("version");
    }

    public synchronized void unsetVersion(ServiceReference<FacesVersion> versionRef) {
        if (versionRef == this.maxVersionRef) {
            maxVersionRef = null;
            maxVersion = DEFAULT_MAX_VERSION;
        }
    }

    @FFDCIgnore(ParseException.class)
    @Override
    public FacesConfig adapt(Container root,
                             OverlayContainer rootOverlay,
                             ArtifactContainer artifactContainer,
                             Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, rootOverlay, artifactContainer.getPath());

        // The faces configuration is not cached.

        Entry ddEntry = containerToAdapt.getEntry(FacesConfig.DD_NAME);
        if (ddEntry == null) {
            return null;
        }

        FacesConfig facesConfig;
        try {
            FacesConfigDDParser ddParser =
                new FacesConfigDDParser(containerToAdapt, ddEntry, maxVersion);
            facesConfig = ddParser.parse();
        } catch (ParseException e) {
            throw new UnableToAdaptException(e);
        }
        return facesConfig;
    }
}
