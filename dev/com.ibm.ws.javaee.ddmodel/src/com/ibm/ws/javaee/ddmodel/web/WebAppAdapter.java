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

import com.ibm.ws.container.service.app.deploy.ContainerInfo;
import com.ibm.ws.javaee.dd.web.WebApp;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.NonPersistentCache;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public final class WebAppAdapter implements DDAdapter, ContainerAdapter<WebApp> {
    @Override
    public WebApp adapt(Container root,
                        OverlayContainer rootOverlay,
                        ArtifactContainer artifactContainer,
                        Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);

        // The web application is cached at the root of the web module's container.
        //
        // Contrast this with the caching which is done for application
        // descriptors (com.ibm.ws.javaee.ddmodel.app.ApplicationAdapter)
        // and EJBJar descriptors (com.ibm.ws.javaee.ddmodel.ejb.EJBJarAdapter).

        // This cache get never succeeds when an alternate descriptor
        // is used and the alternate descriptor is not in the web application
        // container.

        NonPersistentCache cache = containerToAdapt.adapt(NonPersistentCache.class);
        WebApp webApp = (WebApp) cache.getFromCache(WebApp.class);
        if (webApp != null) {
            return webApp;
        }

        Entry ddEntry = DDAdapter.getAltEntry(cache, ContainerInfo.Type.WEB_MODULE);
        if ( ddEntry == null ) {
            ddEntry = containerToAdapt.getEntry(WebApp.DD_NAME);
            if ( ddEntry == null ) {
                return null;
            }
        }

        return ddEntry.adapt(WebApp.class);
    }
}
