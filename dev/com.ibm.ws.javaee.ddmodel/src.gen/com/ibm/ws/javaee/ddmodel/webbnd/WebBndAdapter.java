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
package com.ibm.ws.javaee.ddmodel.webbnd;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.dd.web.WebApp;
import com.ibm.ws.javaee.dd.webbnd.WebBnd;

import org.osgi.service.component.annotations.*;
import com.ibm.ws.container.service.app.deploy.ApplicationInfo;
import com.ibm.ws.container.service.app.deploy.ModuleInfo;
import com.ibm.ws.container.service.app.deploy.NestedConfigHelper;
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
    property = { "service.vendor=IBM", "toType=com.ibm.ws.javaee.dd.webbnd.WebBnd" })
public class WebBndAdapter implements DDAdapter, ContainerAdapter<WebBnd> {
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    volatile List<WebBnd> configurations;

    private WebBndComponentImpl getConfigOverrides(
        OverlayContainer overlay, ArtifactContainer artifactContainer)
        throws UnableToAdaptException {

        if ( (configurations == null) || configurations.isEmpty() ) {
             return null;
        }

        ApplicationInfo appInfo = (ApplicationInfo)
            overlay.getFromNonPersistentCache(artifactContainer.getPath(), ApplicationInfo.class);

        ModuleInfo moduleInfo = null;
        if (appInfo == null) {
            moduleInfo = (ModuleInfo)
                overlay.getFromNonPersistentCache(artifactContainer.getPath(), ModuleInfo.class);
            if (moduleInfo == null) {
                return null;
            }
            appInfo = moduleInfo.getApplicationInfo();
            if ( appInfo == null ) {
                return null;
            }
        }

        NestedConfigHelper configHelper = appInfo.getConfigHelper();
        if ( configHelper == null ) {
            return null;
        }
        String servicePid = (String) configHelper.get("service.pid");
        String extendsPid = (String) configHelper.get("ibm.extends.source.pid");

        Set<String> configuredModuleNames = null;
        for (WebBnd config : configurations) {
             WebBndComponentImpl configImpl = (WebBndComponentImpl) config;
             String parentPid = (String) configImpl.getConfigAdminProperties().get("config.parentPID");
             if ( servicePid.equals(parentPid) || parentPid.equals(extendsPid)) {
                 if (moduleInfo == null) {
                     return configImpl;
                 }
                  
                 String moduleName = (String) configImpl.getConfigAdminProperties().get("moduleName");
                 if (moduleName == null) {
                     DDAdapter.unspecifiedModuleName(overlay, getClass(), "web-bnd");
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
        
        if ( configuredModuleNames != null ) {
            Application app = appInfo.getContainer().adapt(Application.class);
            DDAdapter.invalidModuleName(
                overlay, getClass(),
                app, configuredModuleNames, "web-bnd"); 
        }

        return null;
    }

    @Override
    @FFDCIgnore(ParseException.class)
    public WebBnd adapt(Container root,
                        OverlayContainer rootOverlay,
                        ArtifactContainer artifactContainer,
                        Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);

        WebApp webApp = containerToAdapt.adapt(WebApp.class);
        String webAppVersion = ( (webApp == null) ? null : webApp.getVersion() );
        boolean xmi = ( "2.2".equals(webAppVersion) ||
                        "2.3".equals(webAppVersion) ||
                        "2.4".equals(webAppVersion) );
        String bndEntryName = ( xmi ? WebBnd.XMI_BND_NAME : WebBnd.XML_BND_NAME ); 
        Entry bndEntry = containerToAdapt.getEntry(bndEntryName);

        WebBndComponentImpl bndFromConfig = getConfigOverrides(rootOverlay, artifactContainer);
        if ( bndEntry == null ) {
            return bndFromConfig;
        }

        WebBnd bndFromEntry;
        try {
            bndFromEntry = new WebBndDDParser(containerToAdapt, bndEntry, xmi).parse();
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
