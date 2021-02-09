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
package com.ibm.ws.javaee.ddmodel.wsbnd.adapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.ibm.ws.container.service.app.deploy.ApplicationInfo;
import com.ibm.ws.container.service.app.deploy.EJBModuleInfo;
import com.ibm.ws.container.service.app.deploy.ModuleInfo;
import com.ibm.ws.container.service.app.deploy.NestedConfigHelper;
import com.ibm.ws.container.service.app.deploy.WebModuleInfo;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.ws.javaee.ddmodel.wsbnd.WebservicesBnd;
import com.ibm.ws.javaee.ddmodel.wsbnd.impl.WebservicesBndComponentImpl;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

@Component(configurationPolicy = ConfigurationPolicy.IGNORE,
           service = ContainerAdapter.class,
           property = { "service.vendor=IBM", "toType=com.ibm.ws.javaee.ddmodel.wsbnd.WebservicesBnd" })
public final class WebservicesBndAdapter implements DDAdapter, ContainerAdapter<WebservicesBnd> {
    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               policyOption = ReferencePolicyOption.GREEDY)
    volatile List<WebservicesBnd> configurations;

    private WebservicesBndComponentImpl getConfigOverrides(
        OverlayContainer overlay, ArtifactContainer artifactContainer)
        throws UnableToAdaptException {

        if ( (configurations == null) || configurations.isEmpty() ) {
            return null;
        }
        
        String containerPath = artifactContainer.getPath();

        ApplicationInfo appInfo = (ApplicationInfo)
            overlay.getFromNonPersistentCache(containerPath, ApplicationInfo.class);

        ModuleInfo moduleInfo = null;
        if (appInfo == null) {
            moduleInfo = (ModuleInfo)
                overlay.getFromNonPersistentCache(containerPath, ModuleInfo.class);
            if (moduleInfo == null) {
                return null;
            }
            appInfo = moduleInfo.getApplicationInfo();
            if (appInfo == null) {
                return null;
            }
        }

        NestedConfigHelper configHelper = appInfo.getConfigHelper();
        if (configHelper == null) {
            return null;
        }
        String servicePid = (String) configHelper.get("service.pid");
        String extendsPid = (String) configHelper.get("ibm.extends.source.pid");

        Set<String> configuredModuleNames = null;

        for (WebservicesBnd config : configurations) {
            WebservicesBndComponentImpl configImpl = (WebservicesBndComponentImpl) config;
            String parentPid = (String) configImpl.getConfigAdminProperties().get("config.parentPID");
            if (servicePid.equals(parentPid) || parentPid.equals(extendsPid)) {
                if (moduleInfo == null) {
                    return configImpl;
                }

                String moduleName = (String) configImpl.getConfigAdminProperties().get("moduleName");
                if (moduleName == null) {
                    DDAdapter.unspecifiedModuleName(overlay, getClass(),
                        WebServicesBndDDParser.WEBSERVICES_BND_ELEMENT_NAME);
                    continue;
                }
                moduleName = DDAdapter.stripExtension(moduleName);

                if (configuredModuleNames == null) {
                    configuredModuleNames = new HashSet<>();
                }
                configuredModuleNames.add(moduleName);

                if (moduleInfo.getName().equals(moduleName)) {
                    return configImpl;
                }
            }
        }

        if (configuredModuleNames != null) {
            Application app = appInfo.getContainer().adapt(Application.class);
            DDAdapter.invalidModuleName(
                overlay, getClass(),
                app, configuredModuleNames, WebServicesBndDDParser.WEBSERVICES_BND_ELEMENT_NAME); 
        }
        return null;
    }

    //

    private boolean isServer = true;

    @Activate
    protected void activate(ComponentContext cc) {
        isServer = "server".equals(cc.getBundleContext().getProperty("wlp.process.type"));
    }

    protected void deactivate(ComponentContext cc) {
        // TODO: Should 'isServer' be set back to true?
        // EMPTY
    }

    //

    @FFDCIgnore(ParseException.class)
    @Override
    public WebservicesBnd adapt(Container root, OverlayContainer rootOverlay, ArtifactContainer artifactContainer, Container containerToAdapt)
        throws UnableToAdaptException {

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);

        // The web services binding is not cached.

        Entry bndEntry = null;
        if (isServer) {
            String containerPath = artifactContainer.getPath();
            if (rootOverlay.getFromNonPersistentCache(containerPath, WebModuleInfo.class) != null) {
                bndEntry = containerToAdapt.getEntry(WebservicesBnd.WEB_XML_BND_URI);
            } else if (rootOverlay.getFromNonPersistentCache(containerPath, EJBModuleInfo.class) != null) {
                bndEntry = containerToAdapt.getEntry(WebservicesBnd.EJB_XML_BND_URI);
            }
        } else {
            bndEntry = containerToAdapt.getEntry(WebservicesBnd.EJB_XML_BND_URI);
        }
        WebservicesBndComponentImpl bndFromConfig =
            getConfigOverrides(rootOverlay, artifactContainer);

        if ( bndEntry == null ) {
            return bndFromConfig;
        }

        WebservicesBnd bndFromEntry;
        try {
            WebServicesBndDDParser parser =
                new WebServicesBndDDParser(containerToAdapt, bndEntry);
            bndFromEntry = parser.parse();
        } catch (ParseException e) {
            throw new UnableToAdaptException(e);
        }

        if (bndFromConfig == null) {
            return bndFromEntry;
        } else {
            bndFromConfig.setDelegate(bndFromEntry);
            return bndFromConfig;
        }
    }
}
