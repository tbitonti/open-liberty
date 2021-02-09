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

import com.ibm.ws.container.service.app.deploy.ContainerInfo;
import com.ibm.ws.container.service.app.deploy.WebModuleInfo;
import com.ibm.ws.javaee.dd.ejb.EJBJar;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.NonPersistentCache;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public final class EJBJarAdapter implements DDAdapter, ContainerAdapter<EJBJar> {
    @Override
    public EJBJar adapt(Container root,
                        OverlayContainer rootOverlay,
                        ArtifactContainer artifactContainer,
                        Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);

        // Caching is done by 'EJBJarEntryAdapter', using the container
        // which contains the resolved entry, and using the path of the
        // resolved entry.

        NonPersistentCache cache = containerToAdapt.adapt(NonPersistentCache.class);
        WebModuleInfo webModuleInfo = (WebModuleInfo) cache.getFromCache(WebModuleInfo.class);
        Entry ddEntry;
        if (webModuleInfo != null) {
            // No alternate EJB descriptor may be specified for EJB-IN-WAR.
            ddEntry = containerToAdapt.getEntry("WEB-INF/ejb-jar.xml");
        } else {
            ddEntry = DDAdapter.getAltEntry(cache, ContainerInfo.Type.EJB_MODULE);  
            if (ddEntry == null) {
                ddEntry = containerToAdapt.getEntry("META-INF/ejb-jar.xml");
            }
        }
        if (ddEntry == null) {
            return null;
        }
        
        return ddEntry.adapt(EJBJar.class);
    }
}
