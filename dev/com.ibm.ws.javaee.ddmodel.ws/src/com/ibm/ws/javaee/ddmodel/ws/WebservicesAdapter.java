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
package com.ibm.ws.javaee.ddmodel.ws;

import com.ibm.ws.container.service.app.deploy.EJBModuleInfo;
import com.ibm.ws.container.service.app.deploy.WebModuleInfo;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.ws.Webservices;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public final class WebservicesAdapter implements DDAdapter, ContainerAdapter<Webservices> {
    @FFDCIgnore(ParseException.class)
    @Override
    public Webservices adapt(Container root,
                             OverlayContainer rootOverlay,
                             ArtifactContainer artifactContainer,
                             Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, rootOverlay, artifactContainer.getPath());

        String containerPath = artifactContainer.getPath();

        Webservices webServices = (Webservices)
            rootOverlay.getFromNonPersistentCache(containerPath, Webservices.class);
        if (webServices != null) {
            return webServices;
        }

        Entry ddEntry;
        if (rootOverlay.getFromNonPersistentCache(containerPath, WebModuleInfo.class) != null) {
            ddEntry = containerToAdapt.getEntry(Webservices.WEB_DD_NAME);
        } else if (rootOverlay.getFromNonPersistentCache(containerPath, EJBModuleInfo.class) != null) {
            ddEntry = containerToAdapt.getEntry(Webservices.EJB_DD_NAME);
        } else {
            ddEntry = null;
        }
        if ( ddEntry == null ) {
            return null;
        }

        try {
            WebServicesDDParser ddParser =
                new WebServicesDDParser(containerToAdapt, ddEntry);
            webServices = ddParser.parse();
        } catch (ParseException e) {
            throw new UnableToAdaptException(e);
        }

        rootOverlay.addToNonPersistentCache(containerPath, Webservices.class, webServices);
        return webServices;
    }
}
