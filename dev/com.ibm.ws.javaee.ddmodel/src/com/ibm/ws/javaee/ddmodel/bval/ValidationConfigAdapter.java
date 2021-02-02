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
package com.ibm.ws.javaee.ddmodel.bval;

import com.ibm.ws.container.service.app.deploy.WebModuleInfo;
import com.ibm.ws.javaee.dd.bval.ValidationConfig;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.NonPersistentCache;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

public final class ValidationConfigAdapter implements DDAdapter, ContainerAdapter<ValidationConfig> {

    @Override
    public ValidationConfig adapt(Container root,
                                  OverlayContainer rootOverlay,
                                  ArtifactContainer artifactContainer,
                                  Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, rootOverlay, artifactContainer.getPath());

        // The validation configuration is not cached.

        NonPersistentCache cache = containerToAdapt.adapt(NonPersistentCache.class);
        WebModuleInfo webModuleInfo = (WebModuleInfo) cache.getFromCache(WebModuleInfo.class);

        Entry ddEntry;
        if (webModuleInfo != null) {
            // This path provides visibility to the validation descriptor
            // from the web container's class loader.
        	ddEntry = containerToAdapt.getEntry("WEB-INF/classes/META-INF/validation.xml");
            if (ddEntry == null) {
                ddEntry = containerToAdapt.getEntry("WEB-INF/validation.xml");
            }
        } else {
            ddEntry = containerToAdapt.getEntry("META-INF/validation.xml");
        }

        return ((ddEntry == null) ? null : ddEntry.adapt(ValidationConfig.class));
    }

}
