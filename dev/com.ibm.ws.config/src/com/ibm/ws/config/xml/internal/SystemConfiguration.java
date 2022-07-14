/*******************************************************************************
 * Copyright (c) 2013, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.config.xml.internal;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import com.ibm.websphere.config.ConfigParserException;
import com.ibm.websphere.config.ConfigUpdateException;
import com.ibm.websphere.config.ConfigValidationException;
import com.ibm.websphere.config.WSConfigurationHelper;
import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.config.admin.SystemConfigSupport;
import com.ibm.ws.config.xml.internal.ConfigComparator.DeltaType;
import com.ibm.ws.config.xml.internal.variables.ConfigVariableRegistry;
import com.ibm.ws.kernel.LibertyProcess;
import com.ibm.wsspi.kernel.service.location.VariableRegistry;
import com.ibm.wsspi.kernel.service.location.WsLocationAdmin;
import com.ibm.wsspi.kernel.service.utils.OnErrorUtil;
import com.ibm.wsspi.kernel.service.utils.OnErrorUtil.OnError;

import io.openliberty.checkpoint.spi.CheckpointHook;

/**
 * Represents the configuration of the entire system at runtime, comprising variables, all XML configuration, and all default configuration
 */
// @formatter:off
class SystemConfiguration implements CheckpointHook {
    static final TraceComponent tc =
        Tr.register(SystemConfiguration.class,
                    XMLConfigConstants.TR_GROUP, XMLConfigConstants.NLS_PROPS);

    //

    private final BundleProcessor bundleProcessor;

    private final ServiceRegistration<WSConfigurationHelper> wsConfigurationHelperRegistration;
    private final ServiceRegistration<CheckpointHook> checkpointHookRegistration;

    //

    SystemConfiguration(BundleContext bundleContext,
                        SystemConfigSupport caSupport,
                        ConfigurationAdmin configAdmin) {

        this.locationTracker = new ServiceTracker<>(bundleContext, WsLocationAdmin.class.getName(), null);
        this.locationTracker.open();
        WsLocationAdmin locationService = this.locationTracker.getService();

        //

        this.variableRegistryTracker = new ServiceTracker<>(bundleContext, VariableRegistry.class.getName(), null);
        this.variableRegistryTracker.open();

        VariableRegistry variableRegistryService = null;
        try {
            variableRegistryService = this.variableRegistryTracker.waitForService(0); // Indefinite wait
        } catch ( InterruptedException e ) {
            // Auto FFDC
        }

        OnError onError = getOnError(variableRegistryService);
        if ( onError != OnError.WARN ) {
            ErrorHandler.INSTANCE.setOnError(onError);
        }

        ServiceReference<LibertyProcess> processRef =
            bundleContext.getServiceReference(LibertyProcess.class);
        LibertyProcess libertyProcess = bundleContext.getService(processRef);
        this.configVariableRegistry =
            new ConfigVariableRegistry( variableRegistryService,
                                        libertyProcess.getArgs(),
                                        bundleContext.getDataFile("variableCacheData"),
                                        locationService );
        //

        this.metatypeRegistryTracker =
            new ServiceTracker<>(bundleContext, MetaTypeRegistry.class.getName(), null);
        this.metatypeRegistryTracker.open();
        MetaTypeRegistry metatypeRegistry = this.metatypeRegistryTracker.getService();

        this.extendedMetatypeManager = new ExtendedMetatypeManager(metatypeRegistry, configAdmin);
        this.extendedMetatypeManager.init();

        //

        XMLConfigParser parser = new XMLConfigParser(locationService, this.configVariableRegistry);
        this.serverXMLConfig = new ServerXMLConfiguration(bundleContext, locationService, parser);
        this.defaultConfig = new DefaultConfiguration(parser);

        this.configRetriever = new ConfigRetriever(caSupport, configAdmin, this.configVariableRegistry);
        this.configValidator = new ConfigValidator(metatypeRegistry, this.configVariableRegistry);
        this.configValidator.setConfiguration(this.serverXMLConfig);

        ConfigEvaluator ce = new ConfigEvaluator(this.configRetriever, metatypeRegistry, this.configVariableRegistry, this.serverXMLConfig);
        this.configUpdater = new ConfigUpdater(ce, caSupport, this.configVariableRegistry, metatypeRegistry, this.extendedMetatypeManager);
        this.configChangeHandler = new ChangeHandler(caSupport, this.configVariableRegistry, this.extendedMetatypeManager, this.configRetriever, this.configValidator, configUpdater, metatypeRegistry);
        this.configRefresher = new ConfigRefresher(bundleContext, this.configChangeHandler, this.serverXMLConfig, this.configVariableRegistry);

        this.bundleProcessor =
            new BundleProcessor(bundleContext, this, locationService, this.configUpdater, this.configChangeHandler, this.configValidator, this.configRetriever);

        WSConfigurationHelper wsConfigHelper =
            new WSConfigurationHelperImpl(metatypeRegistry, ce, this.bundleProcessor);
        Dictionary<String, ?> helperProperties =
            FrameworkUtil.asDictionary( Collections.singletonMap("service.vendor", "IBM") );
        this.wsConfigurationHelperRegistration =
            bundleContext.registerService(WSConfigurationHelper.class, wsConfigHelper, helperProperties);

        // register restore hook to reprocess config if necessary
        // Service ranking of checkpointHookRegistration needs to be greater than com.ibm.ws.kernel.service.location.internal.Activator.checkpointHookRegistration.
        // This is important in order to maintain the order of running the hooks.
        Dictionary<String, ?> checkpointProperties =
            FrameworkUtil.asDictionary(Collections.singletonMap(Constants.SERVICE_RANKING, 1000));
        this.checkpointHookRegistration =
            bundleContext.registerService(CheckpointHook.class, this, checkpointProperties);
    }

    private OnError getOnError(VariableRegistry variableRegistry) {
        if ( variableRegistry == null ) {
            return OnError.WARN; // Should never happen
        }

        String onErrorVar = "${" + OnErrorUtil.CFG_KEY_ON_ERROR + "}";
        String onErrorVal = variableRegistry.resolveString(onErrorVar);
        if ( onErrorVal.equals(onErrorVar) ) {
            return OnErrorUtil.OnError.WARN; // Unset: Assign 'WARN' as a default.
        }

        OnError onError;

        String onErrorFormatted = onErrorVal.trim().toUpperCase();
        try {
            onError = Enum.valueOf(OnErrorUtil.OnError.class, onErrorFormatted);

        } catch ( IllegalArgumentException err ) {
            // Unconditionally display a warning:
            // The error occurred in determining the on-error value!
            if ( tc.isWarningEnabled() ) {
                Tr.warning(tc, "warn.config.invalid.value",
                           OnErrorUtil.CFG_KEY_ON_ERROR, onErrorVal, OnErrorUtil.CFG_VALID_OPTIONS);
            }

            onError = OnErrorUtil.OnError.WARN; // Error: Assign 'WARN' as a default.
            onErrorFormatted = onError.toString();
        }

        // Correct the variable registry with a validated entry if needed
        if ( !onErrorVal.equals(onErrorFormatted) ) {
            variableRegistry.replaceVariable(OnErrorUtil.CFG_KEY_ON_ERROR, onErrorFormatted);
        }

        return onError;
    }

    //

    private ServiceTracker<WsLocationAdmin, WsLocationAdmin> locationTracker;

    private ServiceTracker<VariableRegistry, VariableRegistry> variableRegistryTracker;
    private final ConfigVariableRegistry configVariableRegistry;

    private ServiceTracker<MetaTypeRegistry, MetaTypeRegistry> metatypeRegistryTracker;
    private final ExtendedMetatypeManager extendedMetatypeManager;

    //

    private final ServerXMLConfiguration serverXMLConfig;
    private final DefaultConfiguration defaultConfig;

    private final ConfigRetriever configRetriever;
    private final ConfigValidator configValidator;

    private final ConfigUpdater configUpdater;
    private final ChangeHandler configChangeHandler;
    private final ConfigRefresher configRefresher;

    //

    ServerConfiguration getServerConfiguration() {
        return serverXMLConfig.getConfiguration();
    }

    ServerConfiguration copyServerConfiguration() {
        return serverXMLConfig.copyConfiguration();
    }

    Collection<String> fetchConfigurationFilePaths() {
        return serverXMLConfig.getFilesToMonitor();
    }

    BaseConfiguration loadDefaultConfiguration(Bundle bundle)
        throws ConfigUpdateException, ConfigValidationException {

        return defaultConfig.load(bundle, serverXMLConfig, configVariableRegistry);
    }

    BaseConfiguration addDefaultConfiguration(String pid, Dictionary<String, String> props)
        throws ConfigUpdateException {

        return defaultConfig.add(pid, props, serverXMLConfig, configVariableRegistry);
    }

    BaseConfiguration addDefaultConfiguration(InputStream input)
        throws ConfigValidationException, ConfigUpdateException {

        return defaultConfig.add(input, serverXMLConfig, configVariableRegistry);
    }

    //

    void start() throws ConfigUpdateException, ConfigValidationException, ConfigParserException {
        if ( serverXMLConfig.hasConfigRoot() ) {
            configRefresher.start();
            serverXMLConfig.loadInitialConfiguration(configVariableRegistry);
        }

        boolean doReprocess;
        if ( serverXMLConfig.isModified() || !configVariableRegistry.variablesChanged().isEmpty() ) {
            configVariableRegistry.clearVariableCache();
            configChangeHandler.updateAtStartup( serverXMLConfig.getConfiguration() );
            serverXMLConfig.setConfigReadTime();
            doReprocess = true;
        } else {
            doReprocess = false;
        }

        bundleProcessor.startProcessor(doReprocess);
    }

    void stop() {
        bundleProcessor.stopProcessor();
        configRefresher.stop();

        if ( wsConfigurationHelperRegistration != null ) {
            wsConfigurationHelperRegistration.unregister();
        }
        if ( checkpointHookRegistration != null ) {
            checkpointHookRegistration.unregister();
        }

        if ( null != locationTracker ) {
            locationTracker.close();
            locationTracker = null;
        }
        if ( null != variableRegistryTracker ) {
            variableRegistryTracker.close();
            variableRegistryTracker = null;
        }
        if ( null != metatypeRegistryTracker ) {
            metatypeRegistryTracker.close();
            metatypeRegistryTracker = null;
        }
    }

    @Override
    public void restore() {
        if ( serverXMLConfig.isModified() ) {
            configRefresher.refreshConfiguration();
        } else {
            Map<String, DeltaType> deltaTypes = configVariableRegistry.variablesChanged();
            if ( !deltaTypes.isEmpty() ) {
                configRefresher.variableRefresh(deltaTypes);
            }
        }
    }

    //

    void bundleRemoved(Bundle bundle) {
        BaseConfiguration config = serverXMLConfig.getDefaultConfiguration();
        config.remove(defaultConfig.remove(bundle));
    }

    boolean removeDefaultConfiguration(String pid, String id) throws ConfigUpdateException {
        ServerConfiguration oldConfig = serverXMLConfig.copyConfiguration();

        BaseConfiguration cfg = serverXMLConfig.getDefaultConfiguration();

        // TODO: 'remove', below, can never work.
        boolean removed = cfg.remove(pid, id);

        BaseConfiguration runtimeCfg = defaultConfig.getRuntimeDefaultConfiguration(pid);
        if ( runtimeCfg != null ) {
            // TODO: 'remove', below, can never work.
            runtimeCfg.remove(pid, id);
        }

        if ( removed) {
            configVariableRegistry.setDefaultVariables( cfg.getVariables() );
            removeDefaultConfiguration(oldConfig);
        }

        return removed;
    }

    void removeDefaultConfiguration(ServerConfiguration oldConfig) throws ConfigUpdateException {
        configChangeHandler.removeDefaultConfiguration(oldConfig, serverXMLConfig);
    }
}
// @formatter: on