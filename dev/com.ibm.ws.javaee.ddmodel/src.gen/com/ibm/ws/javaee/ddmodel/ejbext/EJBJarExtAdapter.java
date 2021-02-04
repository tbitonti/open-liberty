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
package com.ibm.ws.javaee.ddmodel.ejbext;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.dd.ejb.EJBJar;
import com.ibm.ws.javaee.dd.ejbext.EJBJarExt;

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
    property = { "service.vendor=IBM", "toType=com.ibm.ws.javaee.dd.ejbext.EJBJarExt" })
public class EJBJarExtAdapter implements DDAdapter, ContainerAdapter<EJBJarExt> {
    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               policyOption = ReferencePolicyOption.GREEDY)
    volatile List<EJBJarExt> configurations;

    private EJBJarExtComponentImpl getConfigOverrides(
        OverlayContainer rootOverlay, ArtifactContainer artifactContainer)
        throws UnableToAdaptException {

        if ((configurations == null) || configurations.isEmpty()) {
             return null;
        }

        String ejbJarPath = artifactContainer.getPath();

        ApplicationInfo appInfo = (ApplicationInfo)
            rootOverlay.getFromNonPersistentCache(ejbJarPath, ApplicationInfo.class);

        ModuleInfo moduleInfo = null;
        if (appInfo == null) {
            if (rootOverlay.getParentOverlay() == null) {
                return null;
            }
            moduleInfo = (ModuleInfo)
                rootOverlay.getFromNonPersistentCache(ejbJarPath, ModuleInfo.class);
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

        Set<String> overrideModuleNames = null;
        for (EJBJarExt config : configurations) {
            EJBJarExtComponentImpl configImpl = (EJBJarExtComponentImpl) config;
            String parentPid = (String) configImpl.getConfigAdminProperties().get("config.parentPID");
            if ( servicePid.equals(parentPid) || parentPid.equals(extendsPid)) {
                if (moduleInfo == null) {
                    return configImpl;
                }

                String moduleName = (String) configImpl.getConfigAdminProperties().get("moduleName");
                if (moduleName == null) {
                    DDAdapter.unspecifiedModuleName(rootOverlay, getClass(), "ejb-jar-ext");
                    continue;
                }
                moduleName = DDAdapter.stripExtension(moduleName);

                if (overrideModuleNames == null) {
                    overrideModuleNames = new HashSet<>();
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
                app, overrideModuleNames, "ejb-jar-ext");
        }
        return null;
    }
    
    public static final String XMI_EXT_IN_EJB_MOD_NAME = "META-INF/ibm-ejb-jar-ext.xmi";
    public static final String XML_EXT_IN_EJB_MOD_NAME = "META-INF/ibm-ejb-jar-ext.xml";
    public static final String XMI_EXT_IN_WEB_MOD_NAME = "WEB-INF/ibm-ejb-jar-ext.xmi";
    public static final String XML_EXT_IN_WEB_MOD_NAME = "WEB-INF/ibm-ejb-jar-ext.xml";

    @Override
    @FFDCIgnore(ParseException.class)
    public EJBJarExt adapt(
        Container root,
        OverlayContainer rootOverlay,
        ArtifactContainer artifactContainer,
        Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, rootOverlay, artifactContainer.getPath());

        // The EJB extension is not cached.

        String ejbJarPath = artifactContainer.getPath();

        EJBJar ejbJar = containerToAdapt.adapt(EJBJar.class);
        boolean xmi = ((ejbJar != null) && (ejbJar.getVersionID() < EJBJar.VERSION_3_0));
        WebModuleInfo webInfo = (WebModuleInfo)
            rootOverlay.getFromNonPersistentCache(ejbJarPath, WebModuleInfo.class);
        String extEntryName;
        if (webInfo == null) {
            extEntryName = (xmi ? XMI_EXT_IN_EJB_MOD_NAME : XML_EXT_IN_EJB_MOD_NAME);
        } else {
            extEntryName = (xmi ? XMI_EXT_IN_WEB_MOD_NAME : XML_EXT_IN_WEB_MOD_NAME);
        }
        Entry extEntry = containerToAdapt.getEntry(extEntryName);

        EJBJarExtComponentImpl extFromConfig =
            getConfigOverrides(rootOverlay, artifactContainer);

        if (extEntry == null) {
            return extFromConfig;
        }

        EJBJarExt extFromEntry;        
        try {
            EJBJarExtDDParser extParser =
                new EJBJarExtDDParser(containerToAdapt, extEntry, xmi);
            extFromEntry = extParser.parse();
        } catch (ParseException e) {
            throw new UnableToAdaptException(e);
        }

        if (extFromConfig == null) {
            return extFromEntry;
        } else {
            extFromConfig.setDelegate(extFromEntry);
            return extFromConfig;
        }
    }
}
