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
package com.ibm.ws.javaee.ddmodel.app;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.ws.javaee.version.JavaEEVersion;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

// Caching notes:
//
// Primary descriptors:
//     com.ibm.ws.javaee.ddmodel.app.ApplicationAdapter
//     -- cached at the root of the application container
//     com.ibm.ws.javaee.ddmodel.client.ApplicationClientAdapter
//     -- cached at the descriptor path in the descriptor container;
//     -- has a mis-aligned cache get
//     -- uses entry adapter com.ibm.ws.javaee.ddmodel.client.ApplicationClientEntryAdapter
//     com.ibm.ws.javaee.ddmodel.ejb.EJBJarAdapter
//     -- cached at the descriptor path in the descriptor container;
//     -- does a get after doing alternate entry resolution
//     -- uses entry adapter com.ibm.ws.javaee.ddmodel.ejb.EJBJarEntryAdapter
//     com.ibm.ws.javaee.ddmodel.web.WebAppAdapter
//     -- cached at the descriptor path in the descriptor container;
//     -- has a mis-aligned cache get
//     -- uses entry adapter com.ibm.ws.javaee.ddmodel.web.WebAppEntryAdapter
//     com.ibm.ws.javaee.ddmodel.web.WebFragmentAdapter
//     -- not cached
//
// Secondary descriptors:
//     com.ibm.ws.javaee.ddmodel.jsf.FacesConfigAdapter
//     -- not cached
//     -- uses entry adapter com.ibm.ws.javaee.ddmodel.jsf.FacesConfigEntryAdapter
//     com.ibm.ws.javaee.ddmodel.permissions.PermissionsAdapter
//     -- not cached
//     com.ibm.ws.javaee.ddmodel.ws.WebservicesAdapter
//     -- cached at the root of the web services container
//     com.ibm.ws.javaee.ddmodel.bval.ValidationConfigAdapter
//     -- cached at the descriptor path in the descriptor container;
//     -- uses entry adapter com.ibm.ws.javaee.ddmodel.bval.ValidationConfigEntryAdapter
//
// Tertiary descriptors:
// -- none of the tertiary descriptors is cached
//     com.ibm.ws.javaee.ddmodel.appbnd.ApplicationBndAdapter
//     com.ibm.ws.javaee.ddmodel.appext.ApplicationExtAdapter
//     com.ibm.ws.javaee.ddmodel.clientbnd.ApplicationClientBndAdapter
//     com.ibm.ws.javaee.ddmodel.ejbbnd.EJBJarBndAdapter
//     com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtAdapter
//     com.ibm.ws.javaee.ddmodel.managedbean.ManagedBeanBndAdapter
//     com.ibm.ws.javaee.ddmodel.webbnd.WebBndAdapter
//     com.ibm.ws.javaee.ddmodel.webext.WebExtAdapter
//     com.ibm.ws.javaee.ddmodel.wsbnd.adapter.WebservicesBndAdapter

public final class ApplicationAdapter implements DDAdapter, ContainerAdapter<Application> {
    private ServiceReference<JavaEEVersion> maxVersionRef;
    private Version maxVersion = JavaEEVersion.DEFAULT_VERSION;

    public synchronized void setVersion(ServiceReference<JavaEEVersion> referenceRef) {
        this.maxVersionRef = referenceRef;
        this.maxVersion = Version.parseVersion((String) referenceRef.getProperty("version"));
    }

    public synchronized void unsetVersion(ServiceReference<JavaEEVersion> versionRef) {
        if (versionRef == this.maxVersionRef) {
            this.maxVersionRef = null;
            this.maxVersion = JavaEEVersion.DEFAULT_VERSION;
        }
    }
   
    @FFDCIgnore(ParseException.class)
    @Override
    public Application adapt(Container root,
                             OverlayContainer rootOverlay,
                             ArtifactContainer artifactContainer,
                             Container containerToAdapt) throws UnableToAdaptException {

        // Cache the application DD at the root of the application container.

        // TODO: Is application information available when this adapt is performed?

        String appPath = artifactContainer.getPath();

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);
        
        Application application = (Application)
            rootOverlay.getFromNonPersistentCache(appPath, Application.class);
        if (application != null) {
            return application;
        }

        Entry ddEntry = containerToAdapt.getEntry(Application.DD_NAME);
        if (ddEntry == null) {
            return null;
        }

        try {
            ApplicationDDParser ddParser =
                new ApplicationDDParser(containerToAdapt, ddEntry, JavaEEVersion.asInt(maxVersion));
            application = ddParser.parse();
        } catch (ParseException e) {
            throw new UnableToAdaptException(e);
        }

        rootOverlay.addToNonPersistentCache(appPath, Application.class, application);
        return application;
    }
}
