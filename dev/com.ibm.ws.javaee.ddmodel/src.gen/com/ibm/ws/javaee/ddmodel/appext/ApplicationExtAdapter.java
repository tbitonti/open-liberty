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
package com.ibm.ws.javaee.ddmodel.appext;

import java.util.List;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import com.ibm.ws.container.service.app.deploy.ApplicationInfo;
import com.ibm.ws.container.service.app.deploy.NestedConfigHelper;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.dd.appext.ApplicationExt;
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
    property = { "service.vendor=IBM", "toType=com.ibm.ws.javaee.dd.appext.ApplicationExt" })
public class ApplicationExtAdapter implements DDAdapter, ContainerAdapter<ApplicationExt> {
    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               policyOption = ReferencePolicyOption.GREEDY)
    volatile List<ApplicationExt> configurations;

    private ApplicationExtComponentImpl getConfigOverrides(
        OverlayContainer rootOverlay, ArtifactContainer artifactContainer) {

        if ((configurations == null) || configurations.isEmpty()) {
          return null;
        }

        String appPath = artifactContainer.getPath();

        ApplicationInfo appInfo = (ApplicationInfo)
            rootOverlay.getFromNonPersistentCache(appPath, ApplicationInfo.class);
        if ( appInfo == null ) {
            return null;
        }

        NestedConfigHelper configHelper = appInfo.getConfigHelper();
        if (configHelper == null) {
          return null;
        }
        String servicePid = (String) configHelper.get("service.pid");
        String extendsPid = (String) configHelper.get("ibm.extends.source.pid");

        for (ApplicationExt config : configurations) {
            ApplicationExtComponentImpl configImpl = (ApplicationExtComponentImpl) config;
            String parentPid = (String) configImpl.getConfigAdminProperties().get("config.parentPID");
            if ( servicePid.equals(parentPid) || parentPid.equals(extendsPid)) {
                return configImpl;
            }
        }
        return null;
    }
    
    @Override
    @FFDCIgnore(ParseException.class)
    public ApplicationExt adapt(
        Container root,
        OverlayContainer rootOverlay,
        ArtifactContainer artifactContainer,
        Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, rootOverlay, artifactContainer.getPath());

        // The application extension is not cached.

        Application application = containerToAdapt.adapt(Application.class);
        String appVersion = ((application == null) ? null : application.getVersion());
        String extEntryName;
        boolean xmi = ( "1.2".equals(appVersion) ||
                        "1.3".equals(appVersion) ||
                        "1.4".equals(appVersion) );
        if (xmi) {
            extEntryName = ApplicationExt.XMI_EXT_NAME;
        } else {
            extEntryName = ApplicationExt.XML_EXT_NAME;
        }
        Entry extEntry = containerToAdapt.getEntry(extEntryName);
        
        ApplicationExtComponentImpl extFromConfig =
            getConfigOverrides(rootOverlay, artifactContainer);

        if (extEntry == null) {
            return extFromConfig;
        }

        ApplicationExt extFromEntry;         
        try {
            ApplicationExtDDParser extParser =
                new ApplicationExtDDParser(containerToAdapt, extEntry, xmi);
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
