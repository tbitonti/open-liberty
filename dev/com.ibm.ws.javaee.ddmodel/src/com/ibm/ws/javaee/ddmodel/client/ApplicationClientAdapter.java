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

import com.ibm.ws.container.service.app.deploy.ContainerInfo;
import com.ibm.ws.javaee.dd.client.ApplicationClient;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.NonPersistentCache;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public final class ApplicationClientAdapter implements DDAdapter, ContainerAdapter<ApplicationClient> {
    @Override
    public ApplicationClient adapt(Container root,
                                   OverlayContainer rootOverlay,
                                   ArtifactContainer artifactContainer,
                                   Container containerToAdapt) throws UnableToAdaptException {

        // TODO: The caching which is performed here, together with caching
        //       performed by 'ApplicationClientEntryAdapter.adapt', seem
        //       to be miss-aligned.  This get is at the root of the overlay
        //       cache, while the entry adapter put/get use the entry path.
        //       Further, the put/get performed by the entry adapter use
        //       the overlay container of the entry, which may be an alternate
        //       entry, which may be in a different container than the
        //       application client container.

        DDAdapter.logInfo(this, rootOverlay, artifactContainer.getPath());

        NonPersistentCache cache = containerToAdapt.adapt(NonPersistentCache.class);

        ApplicationClient appClient = (ApplicationClient)
            cache.getFromCache(ApplicationClient.class);
        if (appClient != null) {
            return appClient;
        }

        Entry ddEntry = DDAdapter.getAltEntry(cache, ContainerInfo.Type.CLIENT_MODULE);
        if (ddEntry == null) {
            ddEntry = containerToAdapt.getEntry(ApplicationClient.DD_NAME);
        }
        if (ddEntry == null) {
            return null;
        }

        return ddEntry.adapt(ApplicationClient.class);
    }
}
