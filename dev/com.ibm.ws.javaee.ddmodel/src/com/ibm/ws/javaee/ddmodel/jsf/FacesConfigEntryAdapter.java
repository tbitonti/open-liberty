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
package com.ibm.ws.javaee.ddmodel.jsf;

import org.osgi.framework.ServiceReference;

import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.jsf.FacesConfig;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.ws.javaee.version.FacesVersion;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.EntryAdapter;
import com.ibm.wsspi.artifact.ArtifactEntry;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public class FacesConfigEntryAdapter implements EntryAdapter<FacesConfig> {
    private ServiceReference<FacesVersion> maxVersionRef;
    private volatile int maxVersion = FacesConfigAdapter.DEFAULT_MAX_VERSION;

    public synchronized void setVersion(ServiceReference<FacesVersion> versionRef) {
        maxVersionRef = versionRef;
        maxVersion = (Integer) versionRef.getProperty("version");
    }

    public synchronized void unsetVersion(ServiceReference<FacesVersion> versionRef) {
        if (versionRef == this.maxVersionRef) {
            maxVersionRef = null;
            maxVersion = FacesConfigAdapter.DEFAULT_MAX_VERSION;
        }
    }

    @FFDCIgnore(ParseException.class)
    @Override
    public FacesConfig adapt(Container root, OverlayContainer rootOverlay, ArtifactEntry artifactEntry, Entry entryToAdapt) throws UnableToAdaptException {
        if (entryToAdapt != null) {
            try {
                FacesConfigDDParser ddParser = new FacesConfigDDParser(root, entryToAdapt, maxVersion);
                FacesConfig facesConfig = ddParser.parse();
                return facesConfig;
            } catch (ParseException e) {
                throw new UnableToAdaptException(e);
            }
        }
        return null;
    }
}
