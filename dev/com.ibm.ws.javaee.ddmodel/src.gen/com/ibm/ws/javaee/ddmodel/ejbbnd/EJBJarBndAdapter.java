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
package com.ibm.ws.javaee.ddmodel.ejbbnd;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.dd.ejb.EJBJar;
import com.ibm.ws.javaee.dd.ejbbnd.EJBJarBnd;

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
    property = { "service.vendor=IBM", "toType=com.ibm.ws.javaee.dd.ejbbnd.EJBJarBnd" })
public class EJBJarBndAdapter implements DDAdapter, ContainerAdapter<EJBJarBnd> {
    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               policyOption = ReferencePolicyOption.GREEDY)
    volatile List<EJBJarBnd> configurations;

    private EJBJarBndComponentImpl getConfigOverrides(
        OverlayContainer rootOverlay, ArtifactContainer artifactContainer)
        throws UnableToAdaptException {

        if ((configurations == null) || configurations.isEmpty()) {
             return null;
        }

        String containerPath = artifactContainer.getPath();

        ApplicationInfo appInfo = (ApplicationInfo)
            rootOverlay.getFromNonPersistentCache(containerPath, ApplicationInfo.class);

        ModuleInfo moduleInfo = null;
        if (appInfo == null) {
            moduleInfo = (ModuleInfo)
                rootOverlay.getFromNonPersistentCache(containerPath, ModuleInfo.class);
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
        for (EJBJarBnd config : configurations) {
             EJBJarBndComponentImpl configImpl = (EJBJarBndComponentImpl) config;
             String parentPid = (String) configImpl.getConfigAdminProperties().get("config.parentPID");
             if ( !servicePid.equals(parentPid) && !parentPid.equals(extendsPid)) {
                 continue;
             }
             if (moduleInfo == null) {
                 return configImpl;
             }

             String moduleName = (String) configImpl.getConfigAdminProperties().get("moduleName");
             if (moduleName == null) {
                 DDAdapter.unspecifiedModuleName(rootOverlay, getClass(), "ejb-jar-bnd");
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

        if (overrideModuleNames != null) {
            Application app = appInfo.getContainer().adapt(Application.class);
            DDAdapter.invalidModuleName(
                rootOverlay, getClass(),
                app, overrideModuleNames, "ejb-jar-bnd"); 
        }

        return null;
    }
    
    public static final String XMI_BND_IN_EJB_MOD_NAME = "META-INF/ibm-ejb-jar-bnd.xmi";
    public static final String XML_BND_IN_EJB_MOD_NAME = "META-INF/ibm-ejb-jar-bnd.xml";
    public static final String XMI_BND_IN_WEB_MOD_NAME = "WEB-INF/ibm-ejb-jar-bnd.xmi";
    public static final String XML_BND_IN_WEB_MOD_NAME = "WEB-INF/ibm-ejb-jar-bnd.xml";
    
    @Override
    @FFDCIgnore(ParseException.class)
    public EJBJarBnd adapt(Container root, OverlayContainer rootOverlay, ArtifactContainer artifactContainer, Container containerToAdapt)
        throws UnableToAdaptException {

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);

        EJBJar ejbJar = containerToAdapt.adapt(EJBJar.class);
        boolean xmi = ((ejbJar != null) &&( ejbJar.getVersionID() < EJBJar.VERSION_3_0));

        String ejbJarPath = artifactContainer.getPath();

        WebModuleInfo webInfo = (WebModuleInfo)
            rootOverlay.getFromNonPersistentCache(ejbJarPath, WebModuleInfo.class);

        String extEntryName;
        if (webInfo == null) {
            extEntryName = xmi ? XMI_BND_IN_EJB_MOD_NAME : XML_BND_IN_EJB_MOD_NAME;
        } else {
            extEntryName = xmi ? XMI_BND_IN_WEB_MOD_NAME : XML_BND_IN_WEB_MOD_NAME;
        }
        Entry extEntry = containerToAdapt.getEntry(extEntryName);

        EJBJarBndComponentImpl extFromConfig = getConfigOverrides(rootOverlay, artifactContainer);

        if (extEntry == null) {
            return extFromConfig;
        }

        EJBJarBnd extFromApp;
        try {
            extFromApp = new EJBJarBndDDParser(containerToAdapt, extEntry, xmi).parse();
        } catch (ParseException e) {
            throw new UnableToAdaptException(e);
        }

        if (extFromConfig == null) {
            return extFromApp;
        } else {
            extFromConfig.setDelegate(extFromApp);
            return extFromConfig;
        }
    }
}
