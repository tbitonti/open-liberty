/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
// NOTE: This is a generated file. Do not edit it directly.
package com.ibm.ws.javaee.ddmodel.clientbnd;

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
import com.ibm.ws.javaee.dd.client.ApplicationClient;
import com.ibm.ws.javaee.dd.clientbnd.ApplicationClientBnd;
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
    property = { "service.vendor=IBM", "toType=com.ibm.ws.javaee.dd.clientbnd.ApplicationClientBnd" })
public class ApplicationClientBndAdapter implements DDAdapter, ContainerAdapter<ApplicationClientBnd> {
    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               policyOption = ReferencePolicyOption.GREEDY)
    volatile List<ApplicationClientBnd> configurations;

    private ApplicationClientBndComponentImpl getConfigOverrides(
        OverlayContainer rootOverlay, ArtifactContainer artifactContainer) {

        if ( (configurations == null) || configurations.isEmpty() ) {
             return null;
        }

        String containerPath = artifactContainer.getPath();

        ApplicationInfo appInfo = (ApplicationInfo)
            rootOverlay.getFromNonPersistentCache(containerPath, ApplicationInfo.class);
        if (appInfo == null) {
            return null;
        }

        NestedConfigHelper configHelper = appInfo.getConfigHelper();
        if (configHelper == null) {
            return null;
        }
        String servicePid = (String) configHelper.get("service.pid");
        String extendsPid = (String) configHelper.get("ibm.extends.source.pid");

        for (ApplicationClientBnd config : configurations) {
             ApplicationClientBndComponentImpl configImpl = (ApplicationClientBndComponentImpl) config;
             String parentPid = (String) configImpl.getConfigAdminProperties().get("config.parentPID");
             if (servicePid.equals(parentPid) || parentPid.equals(extendsPid)) {
                 return configImpl;
             }
             // TODO: The client BND processing doesn't verify module names for the
             //       the application client overrides.  Should this be done?
        }

        return null;
    }
    
    @Override
    @FFDCIgnore(ParseException.class)
    public ApplicationClientBnd adapt(
        Container root,
        OverlayContainer rootOverlay,
        ArtifactContainer artifactContainer,
        Container containerToAdapt) throws UnableToAdaptException {

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);

        // The application client binding is not cached.

        ApplicationClient appClient = containerToAdapt.adapt(ApplicationClient.class);
        String appClientVersion = ((appClient == null) ? null : appClient.getVersion());
        boolean xmi = ( "1.2".equals(appClientVersion) ||
                        "1.3".equals(appClientVersion) ||
                        "1.4".equals(appClientVersion) );
        String bndEntryName;
        if (xmi) {
            bndEntryName = ApplicationClientBnd.XMI_BND_NAME;
        } else {
            bndEntryName = ApplicationClientBnd.XML_BND_NAME;
        }
        Entry bndEntry = containerToAdapt.getEntry(bndEntryName);

        ApplicationClientBndComponentImpl bndFromConfig =
            getConfigOverrides(rootOverlay, artifactContainer);

        if (bndEntry == null) {
            return bndFromConfig;
        }

        ApplicationClientBnd bndFromEntry;     
        try {
            ApplicationClientBndDDParser bndParser =
                new ApplicationClientBndDDParser(containerToAdapt, bndEntry, xmi);
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
