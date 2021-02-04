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

// ************************************************************
// THIS FILE HAS BEEN UPDATED SINCE IT WAS GENERATED.
// ANY NEWLY GENERATED CODE MUST BE CAREFULLY MERGED WITH
// THIS CODE.
// ************************************************************

package com.ibm.ws.javaee.ddmodel.webext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.ibm.ws.container.service.app.deploy.ApplicationInfo;
import com.ibm.ws.container.service.app.deploy.ModuleInfo;
import com.ibm.ws.container.service.app.deploy.NestedConfigHelper;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.dd.web.WebApp;
import com.ibm.ws.javaee.dd.webext.WebExt;
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
           property = { "service.vendor=IBM", "toType=com.ibm.ws.javaee.dd.webext.WebExt" })
public class WebExtAdapter implements DDAdapter, ContainerAdapter<WebExt> {
    /**
     * The list of web-extension configuration overrides.  These are keyed
     * by module name.
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               policyOption = ReferencePolicyOption.GREEDY)
    volatile List<WebExt> configurations;

    /**
     * Answer the web extension type configuration override of a module.
     *
     * @param overlay The overlay container of the module.
     * @param artifactContainer The raw container of the module.
     *
     * @return The web extension override of the module.
     *
     * @throws UnableToAdaptException Thrown if an error occured while
     *     accessing the non-persistent cache.
     */
    private WebExtComponentImpl getConfigOverrides(
        OverlayContainer overlay,
        ArtifactContainer artifactContainer) throws UnableToAdaptException {

        if ( (configurations == null) || configurations.isEmpty() ) {
          return null;
        }

        ApplicationInfo appInfo = (ApplicationInfo)
            overlay.getFromNonPersistentCache(artifactContainer.getPath(), ApplicationInfo.class);

        ModuleInfo moduleInfo = null;
        if ( appInfo == null ) {
            // TODO: Why no parent container check?            
            moduleInfo = (ModuleInfo)
                overlay.getFromNonPersistentCache(artifactContainer.getPath(), ModuleInfo.class);
            if ( moduleInfo == null ) {
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

        // The configurations were injected by the service machinery.
        // Loop through these to fine one which matches the target module name.
        // Generate errors along the way.
        //
        // What errors are generated depends on the order of processing.
        //
        // A web extension override which does not have a name is invalid, but
        // that won't be detected unless there is a module which matches an override
        // which follows the invalid override.
        //
        // Note that at most one error is generated, regardless of how many web
        // module overrides are missing module names.

        Set<String> overrideModuleNames = null;

        for ( WebExt config : configurations ) {
            WebExtComponentImpl configImpl = (WebExtComponentImpl) config;

            String parentPid = (String) configImpl.getConfigAdminProperties().get("config.parentPID");
            if ( !servicePid.equals(parentPid) && !parentPid.equals(extendsPid)) {
                continue;
            }

            if ( moduleInfo == null ) {
                return configImpl;
            }

            String moduleName = (String) configImpl.getConfigAdminProperties().get("moduleName");
            if ( moduleName == null ) {
                DDAdapter.unspecifiedModuleName(overlay, getClass(), "web-ext");
                continue;
            }
            moduleName = DDAdapter.stripExtension(moduleName);
            
            if (overrideModuleNames == null) {
                overrideModuleNames = new HashSet<String>();            
            }
            overrideModuleNames.add(moduleName);

            if ( moduleInfo.getName().equals(moduleName) ) {
                return configImpl;
            }
        }

        if ( overrideModuleNames != null ) {
            // Keep the UnableToAdaptException here.
            Application app = appInfo.getContainer().adapt(Application.class);
            
            DDAdapter.invalidModuleName(
                overlay, getClass(),
                app, overrideModuleNames, "web-ext"); 
        }

        return null;
    }

    /**
     * Obtain the web extension for a module container.
     *
     * There are four possibilities:
     *
     * (1) The container has neither a web extension nor a configuration override of its
     * web extension.  In this case, answer null: No web extension is available.
     *
     * (2) The container has a web extension and does not have a configuration override
     * (of the web extension).  Answer the web extension.
     *
     * (3) The container does not have a web extension, but does have a configuration
     * override.  Answer the configuration override.
     *
     * (4) The container has both a web extension and a configuration override.  Answer
     * the configuration override with the web extension set as a delegate.
     *
     * @param root The module container.
     * @param rootOverlay The root overlay container.
     * @param artifactContainer The raw module container.
     * @param containerToAdapt The module container.
     *
     * @return The effective web extension of the module.  Null if no extension is
     *     available.
     *
     * @throws UnableToAdaptException Thrown in case of an error obtaining the
     *     effective web extension.  This will usually be a parse error or a
     *     naming error.
     */
    @Override
    @FFDCIgnore(ParseException.class)
    public WebExt adapt(
        Container root,
        OverlayContainer rootOverlay,
        ArtifactContainer artifactContainer,
        Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, rootOverlay, artifactContainer.getPath());

        WebApp webApp = containerToAdapt.adapt(WebApp.class);
        String webAppVersion = ((webApp == null) ? null : webApp.getVersion());
        boolean xmi = ( "2.2".equals(webAppVersion) ||
                        "2.3".equals(webAppVersion) ||
                        "2.4".equals(webAppVersion) );
        String extEntryName = ( xmi ? WebExt.XMI_EXT_NAME : WebExt.XML_EXT_NAME );  
        Entry extEntry = containerToAdapt.getEntry(extEntryName);

        WebExtComponentImpl extFromConfig =
            getConfigOverrides(rootOverlay, artifactContainer);

        if ( extEntry == null ) {
            return extFromConfig;
        }

        WebExt extFromEntry;
        try {
            extFromEntry = new WebExtDDParser(containerToAdapt, extEntry, xmi).parse();
        } catch ( ParseException e ) {
            throw new UnableToAdaptException(e);
        }

        if ( extFromConfig == null ) {
            return extFromEntry;
        } else {  
            extFromConfig.setDelegate(extFromEntry);
            return extFromConfig;
        }
    }
}
