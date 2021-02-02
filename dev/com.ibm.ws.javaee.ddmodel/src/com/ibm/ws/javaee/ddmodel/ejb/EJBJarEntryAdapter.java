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
package com.ibm.ws.javaee.ddmodel.ejb;

import org.osgi.framework.ServiceReference;

import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.ejb.EJBJar;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.EntryAdapter;
import com.ibm.wsspi.artifact.ArtifactEntry;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public class EJBJarEntryAdapter implements EntryAdapter<EJBJar> {
    private static final int DEFAULT_MAX_VERSION = EJBJar.VERSION_4_0;

    private ServiceReference<EJBJarDDParserVersion> maxVersionRef;
    private volatile int maxVersion = DEFAULT_MAX_VERSION;

    public synchronized void setVersion(ServiceReference<EJBJarDDParserVersion> versionRef) {
        maxVersionRef = versionRef;
        maxVersion = (Integer) versionRef.getProperty("version");
    }

    public synchronized void unsetVersion(ServiceReference<EJBJarDDParserVersion> versionRef) {
        if (versionRef == this.maxVersionRef) {
            maxVersionRef = null;
            maxVersion = DEFAULT_MAX_VERSION;
        }
    }

    @FFDCIgnore(ParseException.class)
    @Override
    public EJBJar adapt(
        Container root,
        OverlayContainer rootOverlay,
        ArtifactEntry artifactEntry,
        Entry entryToAdapt) throws UnableToAdaptException {

        // Cache using the descriptor path and the descriptor root container.

        String ddPath = artifactEntry.getPath();
        EJBJar ejbJar = (EJBJar) rootOverlay.getFromNonPersistentCache(ddPath, EJBJar.class);
        if (ejbJar == null) {
            try {
                EJBJarDDParser ddParser = new EJBJarDDParser(root, entryToAdapt, maxVersion);
                ejbJar = ddParser.parse();
            } catch (ParseException e) {
                throw new UnableToAdaptException(e);
            }
            rootOverlay.addToNonPersistentCache(ddPath, EJBJar.class, ejbJar);
        }
        return ejbJar;
    }
}
