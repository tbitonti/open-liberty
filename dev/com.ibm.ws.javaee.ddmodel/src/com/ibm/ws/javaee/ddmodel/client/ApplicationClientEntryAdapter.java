/*******************************************************************************
 * Copyright (c) 2014, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.ddmodel.client;

import org.osgi.framework.ServiceReference;

import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.client.ApplicationClient;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.EntryAdapter;
import com.ibm.wsspi.artifact.ArtifactEntry;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public final class ApplicationClientEntryAdapter implements EntryAdapter<ApplicationClient> {
    private static final int DEFAULT_MAX_VERSION = ApplicationClient.VERSION_9;

    private ServiceReference<ApplicationClientDDParserVersion> maxVersionRef;
    private volatile int maxVersion = DEFAULT_MAX_VERSION;

    public synchronized void setVersion(ServiceReference<ApplicationClientDDParserVersion> versionRef) {
        maxVersionRef = versionRef;
        maxVersion = (Integer) versionRef.getProperty(ApplicationClientDDParserVersion.VERSION);
    }

    public synchronized void unsetVersion(ServiceReference<ApplicationClientDDParserVersion> versionRef) {
        if (versionRef == this.maxVersionRef) {
            maxVersionRef = null;
            maxVersion = DEFAULT_MAX_VERSION;
        }
    }

    @FFDCIgnore(ParseException.class)
    @Override
    public ApplicationClient adapt(
        Container root,
        OverlayContainer rootOverlay,
        ArtifactEntry artifactEntry,
        Entry entryToAdapt) throws UnableToAdaptException {

        // Cache using the descriptor path and the descriptor root container.

        String ddPath = artifactEntry.getPath();
        ApplicationClient appClient = (ApplicationClient)
            rootOverlay.getFromNonPersistentCache(ddPath, ApplicationClient.class);
        if (appClient != null) {
            return appClient;
        }

        try {
            ApplicationClientDDParser ddParser =
                new ApplicationClientDDParser(root, entryToAdapt, maxVersion);
            appClient = ddParser.parse();
        } catch (ParseException e) {
            throw new UnableToAdaptException(e);
        }

        rootOverlay.addToNonPersistentCache(ddPath, ApplicationClient.class, appClient);

        return appClient;
    }
}
