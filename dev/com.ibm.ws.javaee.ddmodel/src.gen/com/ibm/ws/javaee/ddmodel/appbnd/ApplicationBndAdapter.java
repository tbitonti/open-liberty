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
package com.ibm.ws.javaee.ddmodel.appbnd;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.ibm.ws.javaee.dd.app.Application;
import com.ibm.ws.javaee.dd.appbnd.ApplicationBnd;

import com.ibm.ws.container.service.app.deploy.ApplicationInfo;
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
    property = { "service.vendor=IBM", "toType=com.ibm.ws.javaee.dd.appbnd.ApplicationBnd" })
public class ApplicationBndAdapter implements DDAdapter, ContainerAdapter<ApplicationBnd> {
    /** Configuration overrides of application binding information. */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               policyOption = ReferencePolicyOption.GREEDY)
    volatile List<ApplicationBnd> configurations;

    /**
     * Lookup application binding overrides for the application which is
     * associated with a specified container.
     * 
     * Match the stored {@link ApplicationInfo} to the {@link ApplicationBnd}
     * overrides.  Answer the one which matches 'service.pid' and
     * 'ibm.extends.source.pid'.  Answer null if no match is found.
     *
     * @param rootOverlay The overlay container from which to retrieve application
     *     information.
     * @param artifactContainer The artifact container associated with the overlay
     *     container.
     *
     * @return The application binding configuration override which matches the
     *     stored application information.  Null if no match is found.
     */
    private ApplicationBndComponentImpl getConfigOverrides(
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

        for (ApplicationBnd config : configurations) {
            ApplicationBndComponentImpl configImpl = (ApplicationBndComponentImpl) config;
            String parentPid = (String) configImpl.getConfigAdminProperties().get("config.parentPID");
            if (servicePid.equals(parentPid) || parentPid.equals(extendsPid)) {
                return configImpl;
            }
        }
        return null;
    }

    /**
     * Answer application binding information for an application container.
     * 
     * Obtain the binding information from configuration overrides and from the
     * usual container entry.  If both sources are available, set the information
     * which was obtained from the container entry as the delegate of the override
     * information and answer the override information.
     * 
     * @param root A container which has application information.
     * @param rootOverlay An overlay container associated with the container.
     * @param artifactContainer An artifact container associated with the container.
     * 
     * @return The application binding information of the container.  Null if none
     *     is available.
     *     
     * @throws UnableToAdaptException Thrown if an entry which contains application
     *     information is available but cannot be parsed.  Also thrown if the
     *     the application itself cannot be parsed.
     */
    @Override
    @FFDCIgnore(ParseException.class)
    public ApplicationBnd adapt(
        Container root,
        OverlayContainer rootOverlay,
        ArtifactContainer artifactContainer,
        Container containerToAdapt) throws UnableToAdaptException {

        // The application binding is NOT cached.

        DDAdapter.logInfo(this, root, rootOverlay, artifactContainer, containerToAdapt);
        
        Application application = containerToAdapt.adapt(Application.class);
        String appVersion = ( (application == null) ? null : application.getVersion() );
        boolean xmi = ( "1.2".equals(appVersion) ||
                        "1.3".equals(appVersion) ||
                        "1.4".equals(appVersion) );
        String bndEntryName = ( xmi
            ? ApplicationBnd.XMI_BND_NAME
            : ApplicationBnd.XML_BND_NAME );  
        Entry bndEntry = containerToAdapt.getEntry(bndEntryName);

        ApplicationBndComponentImpl bndFromConfig =
            getConfigOverrides(rootOverlay, artifactContainer);

        if (bndEntry == null) {
            return bndFromConfig;
        }

        ApplicationBnd bndFromEntry;
        try {
            ApplicationBndDDParser bndParser =
                new ApplicationBndDDParser(containerToAdapt, bndEntry, xmi);
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
