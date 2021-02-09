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
package com.ibm.ws.javaee.ddmodel.web;

import org.osgi.framework.ServiceReference;

import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.web.WebApp;
import com.ibm.ws.javaee.dd.web.WebFragment;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.ws.javaee.version.ServletVersion;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public final class WebFragmentAdapter implements DDAdapter, ContainerAdapter<WebFragment> {
    private static final int DEFAULT_MAX_VERSION = WebApp.VERSION_3_0;

    private ServiceReference<ServletVersion> maxVersionRef;
    private volatile int maxVersion = DEFAULT_MAX_VERSION;

    public synchronized void setVersion(ServiceReference<ServletVersion> versionRef) {
        maxVersionRef = versionRef;
        maxVersion = (Integer) versionRef.getProperty("version");
    }

    public synchronized void unsetVersion(ServiceReference<ServletVersion> versionRef) {
        if (versionRef == this.maxVersionRef) {
            maxVersionRef = null;
            maxVersion = DEFAULT_MAX_VERSION;
        }
    }

    @FFDCIgnore(ParseException.class)
    @Override
    public WebFragment adapt(Container root,
                             OverlayContainer rootOverlay,
                             ArtifactContainer artifactContainer,
                             Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);

        // The fragment descriptor is not cached!

        // No alternate descriptor is available for fragments.

        Entry ddEntry = containerToAdapt.getEntry(WebFragment.DD_NAME);
        if (ddEntry == null) {
            return null;
        }

        WebFragment webFragment;
        try {
            WebFragmentDDParser ddParser =
                new WebFragmentDDParser(containerToAdapt, ddEntry, maxVersion);
            webFragment = ddParser.parse();
        } catch (ParseException e) {
            throw new UnableToAdaptException(e);
        }
        return webFragment;
    }
}
