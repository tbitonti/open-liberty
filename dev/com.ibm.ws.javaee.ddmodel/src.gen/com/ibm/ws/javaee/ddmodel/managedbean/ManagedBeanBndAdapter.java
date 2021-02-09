/*******************************************************************************
 * Copyright (c) 2017, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
// NOTE: This is a generated file. Do not edit it directly.
package com.ibm.ws.javaee.ddmodel.managedbean;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.dd.managedbean.ManagedBeanBnd;

import org.osgi.service.component.annotations.*;
import com.ibm.ws.container.service.app.deploy.ApplicationInfo;
import com.ibm.ws.container.service.app.deploy.ModuleInfo;
import com.ibm.ws.container.service.app.deploy.NestedConfigHelper;
import com.ibm.ws.container.service.app.deploy.WebModuleInfo;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.ddmodel.DDAdapter;
import com.ibm.ws.javaee.ddmodel.DDParser.ParseException;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;
import com.ibm.wsspi.adaptable.module.adapters.ContainerAdapter;
import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

@Component(configurationPolicy = ConfigurationPolicy.IGNORE,
    service = ContainerAdapter.class,
    property = { "service.vendor=IBM", "toType=com.ibm.ws.javaee.dd.managedbean.ManagedBeanBnd" })
public class ManagedBeanBndAdapter implements DDAdapter, ContainerAdapter<ManagedBeanBnd> {
    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               policyOption = ReferencePolicyOption.GREEDY)
    volatile List<ManagedBeanBnd> configurations;

    private ManagedBeanBndComponentImpl getConfigOverrides(
        OverlayContainer rootOverlay, ArtifactContainer artifactContainer)
        throws UnableToAdaptException {

        if ((configurations == null) || configurations.isEmpty()) {
             return null;
        }

        String appPath = artifactContainer.getPath();

        ApplicationInfo appInfo = (ApplicationInfo)
            rootOverlay.getFromNonPersistentCache(appPath, ApplicationInfo.class);

        ModuleInfo moduleInfo = null;
        if (appInfo == null) {
            if (rootOverlay.getParentOverlay() == null) {
                return null;
            }
            moduleInfo = (ModuleInfo)
                rootOverlay.getFromNonPersistentCache(appPath, ModuleInfo.class);
            if (moduleInfo == null) {
                return null;
            }
            appInfo = moduleInfo.getApplicationInfo();
            if (appInfo == null) {
                return null;
            }
        }

        NestedConfigHelper configHelper = appInfo.getConfigHelper();
        if ( configHelper == null ) {
            return null;
        }
        String servicePid = (String) configHelper.get("service.pid");
        String extendsPid = (String) configHelper.get("ibm.extends.source.pid");

        Set<String> overrideModuleNames = null;

        for (ManagedBeanBnd config : configurations) {
            ManagedBeanBndComponentImpl configImpl = (ManagedBeanBndComponentImpl) config;
            String parentPid = (String) configImpl.getConfigAdminProperties().get("config.parentPID");
            if ( servicePid.equals(parentPid) || parentPid.equals(extendsPid)) {
                if (moduleInfo == null) {
                    return configImpl;
                }

                String moduleName = (String) configImpl.getConfigAdminProperties().get("moduleName");
                if (moduleName == null) {
                    DDAdapter.unspecifiedModuleName(rootOverlay, getClass(), "managed-bean-bnd");
                    continue;
                }
                moduleName = DDAdapter.stripExtension(moduleName);

                if ( overrideModuleNames == null ) {
                    overrideModuleNames = new HashSet<String>();
                }                
                overrideModuleNames.add(moduleName);

                if (moduleInfo.getName().equals(moduleName)) {
                    return configImpl;
                }
            }
        }

        if (overrideModuleNames != null) {
            // Keep the UnableToAdaptException here.
            Application app = appInfo.getContainer().adapt(Application.class);
            DDAdapter.invalidModuleName(
                rootOverlay, getClass(),
                app, overrideModuleNames, "managed-bean-bnd"); 
        }

        return null;
    }
    
    public static final String XML_BND_IN_EJB_MOD_NAME = "META-INF/ibm-managed-bean-bnd.xml";
    public static final String XML_BND_IN_WEB_MOD_NAME = "WEB-INF/ibm-managed-bean-bnd.xml";

    @Override
    @FFDCIgnore(ParseException.class)
    public ManagedBeanBnd adapt(Container root, OverlayContainer rootOverlay, ArtifactContainer artifactContainer, Container containerToAdapt)
        throws UnableToAdaptException {

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);

        WebModuleInfo webInfo = (WebModuleInfo)
            rootOverlay.getFromNonPersistentCache(artifactContainer.getPath(), WebModuleInfo.class);
        String bndEntryName;
        if (webInfo == null) {
            bndEntryName = XML_BND_IN_EJB_MOD_NAME;
        } else {
            bndEntryName = XML_BND_IN_WEB_MOD_NAME;
        }

        ManagedBeanBndComponentImpl bndFromConfig =
            getConfigOverrides(rootOverlay, artifactContainer);

        Entry bndEntry = containerToAdapt.getEntry(bndEntryName);

        if (bndEntry == null) {
            return bndFromConfig;
        }

        ManagedBeanBnd bndFromEntry;        
        try {
            ManagedBeanBndDDParser bndParser =
                new ManagedBeanBndDDParser(containerToAdapt, bndEntry);
            bndFromEntry = bndParser.parse();
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
