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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;

import com.ibm.websphere.config.ConfigParserException;
import com.ibm.websphere.config.ConfigValidationException;
import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.config.xml.ConfigVariables;
import com.ibm.ws.config.xml.LibertyVariable;
import com.ibm.ws.config.xml.internal.variables.ConfigVariableRegistry;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.wsspi.kernel.service.location.WsLocationAdmin;
import com.ibm.wsspi.kernel.service.location.WsLocationConstants;
import com.ibm.wsspi.kernel.service.location.WsResource;
import com.ibm.wsspi.kernel.service.utils.TimestampUtils;

class ServerXMLConfiguration {
    private static final TraceComponent tc = Tr.register(ServerXMLConfiguration.class,
                                                         XMLConfigConstants.TR_GROUP, XMLConfigConstants.NLS_PROPS);

    private static long readConfigStamp(BundleContext bundleContext) {
        if (bundleContext == null) {
            return 0L;
        }

        File configStamp = bundleContext.getDataFile("configStamp");
        if ((configStamp != null) && configStamp.exists() && configStamp.canRead()) {
            return TimestampUtils.readTimeFromFile(configStamp);
        } else {
            return 0L;
        }
    }

    //

    private static final String CONFIG_DROPINS = "configDropins";
    private static final String CONFIG_DROPIN_DEFAULTS = CONFIG_DROPINS + '/' + "defaults/";
    private static final String CONFIG_DROPIN_OVERRIDES = CONFIG_DROPINS + '/' + "overrides/";

    ServerXMLConfiguration(BundleContext bundleContext,
                           WsLocationAdmin locationService,
                           XMLConfigParser parser) {

        if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
            Tr.debug(tc, "WsLocationAdmin locations=" + locationService.printLocations(false));
        }

        this.bundleContext = bundleContext;
        this.parser = parser;

        this.configDir = locationService.resolveResource(WsLocationConstants.SYMBOL_SERVER_CONFIG_DIR);
        this.configDefaults = locationService.resolveResource(WsLocationConstants.SYMBOL_SERVER_CONFIG_DIR + "/" + CONFIG_DROPIN_DEFAULTS);
        this.configOverrides = locationService.resolveResource(WsLocationConstants.SYMBOL_SERVER_CONFIG_DIR + "/" + CONFIG_DROPIN_OVERRIDES);
        this.configRoot = locationService.resolveResource(WsLocationConstants.SYMBOL_SERVER_CONFIG_DIR + "/" + WsLocationConstants.SYMBOL_PROCESS_TYPE + ".xml");

        // Determines if any of the configuration files used by current server has
        // been updated since the last run.
        this.configReadTime = readConfigStamp(bundleContext);

    }

    //

    private final BundleContext bundleContext;

    private final XMLConfigParser parser;

    //

    private final WsResource configDir;
    private final WsResource configDefaults;
    private final WsResource configOverrides;
    private final WsResource configRoot;

    private ServerConfiguration serverConfiguration;

    private volatile long configReadTime = 0;

    public WsResource getConfigDir() {
        return configDir;
    }

    boolean hasConfigRoot() {
        return configRoot != null;
    }

    /**
     * This sets a server's base configuration by processing server's root
     * configuration document(i.e. server.cfg)
     * and any of its included configuration resources, but not individual
     * bundle's default configurations(i.e. bundle.cfg).
     * <P>
     * Generally, this should be only done once at the beginning before any of
     * the bundle's default configurations are processed.
     */
    @FFDCIgnore(ConfigParserTolerableException.class)
    public void loadInitialConfiguration(ConfigVariableRegistry variableRegistry) throws ConfigValidationException, ConfigParserException {
        if (configRoot != null && configRoot.exists()) {
            try {
                serverConfiguration = loadServerConfiguration();
                if (serverConfiguration == null) {
                    // This only happens if there is a parser error and onError has been set to IGNORE or WARN.
                    // We're just avoiding an NPE here. The user will see the server start up with a warning
                    // that nothing has been configured. This is less than ideal in the case of IGNORE, but it's
                    // the behavior the user has asked for.
                    serverConfiguration = new ServerConfiguration();
                }
            } catch (ConfigParserTolerableException ex) {
                // This only gets caught here if OnError = FAIL..
                // rethrow so the server will shut down
                throw ex;
            } catch (ConfigParserException ex) {
                Tr.error(tc, "error.config.update.init", ex.getMessage());
                serverConfiguration = new ServerConfiguration();
                if (ErrorHandler.INSTANCE.fail())
                    throw ex;
            }

            serverConfiguration.setDefaultConfiguration(new BaseConfiguration());
        }

        try {
            variableRegistry.updateSystemVariables(getVariables());
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("service.vendor", "IBM");
            bundleContext.registerService(ConfigVariables.class, variableRegistry, properties);
        } catch (ConfigMergeException e) {
            if (ErrorHandler.INSTANCE.fail()) {
                throw new ConfigParserTolerableException(e);
            }
        }
    }

    public void setConfigReadTime() {
        setConfigReadTime(getLastResourceModifiedTime());
    }

    public void setConfigReadTime(long time) {
        TimestampUtils.writeTimeToFile(bundleContext.getDataFile("configStamp"), time);
        configReadTime = time;
    }

    private long getLastResourceModifiedTime() {
        long lastModified = configRoot.getLastModified();

        if (serverConfiguration != null) {
            for (WsResource resource : serverConfiguration.getIncludes()) {
                lastModified = Long.max(lastModified, resource.getLastModified());
            }
        }

        for (String name : getChildXMLNames(configDefaults)) {
            WsResource resource = configDefaults.resolveRelative(name);
            lastModified = Long.max(lastModified, resource.getLastModified());
        }

        for (String name : getChildXMLNames(configOverrides)) {
            WsResource resource = configOverrides.resolveRelative(name);
            lastModified = Long.max(lastModified, resource.getLastModified());
        }

        return lastModified;
    }

    // Remove milliseconds from timestamp values to address inconsistencies in container file systems
    long reduceTimestampPrecision(long value) {
        return (value / 1000) * 1000;
    }

    public boolean isModified() {
        return reduceTimestampPrecision(getLastResourceModifiedTime()) != reduceTimestampPrecision(configReadTime);
    }

    public Collection<String> getFilesToMonitor() {
        Collection<String> files = new HashSet<String>();
        files.add(configRoot.toRepositoryPath());

        for (WsResource resource : serverConfiguration.getIncludes()) {
            String path = resource.toRepositoryPath();
            if (path != null) {
                files.add(path);
            }
        }

        return files;
    }

    /**
     * Get the directories that should be monitored for changes. At the moment, this is
     * configDropins/defaults and configDropins/overrides
     */
    public Collection<String> getDirectoriesToMonitor() {
        Collection<String> files = new HashSet<String>();
        if (configDefaults != null) {
            files.add(configDefaults.toRepositoryPath());
        }

        if (configOverrides != null) {
            files.add(configOverrides.toRepositoryPath());
        }

        return files;
    }

    /**
     * To maintain the same order across platforms, we have to implement our own comparator.
     * Otherwise, "aardvark.xml" would come before "Zebra.xml" on windows, and vice versa on unix.
     */
    private static class AlphaComparator implements Comparator<String> {
        @Override
        public int compare(String n1, String n2) {
            return n1.compareToIgnoreCase(n2);
        }
    }

    @FFDCIgnore({ ConfigParserException.class, ConfigParserTolerableException.class })
    private ServerConfiguration loadServerConfiguration() throws ConfigValidationException, ConfigParserException {
        ServerConfiguration configuration = null;

        try {
            try {
                // Initialize the configuration object here, so that as the parser progresses
                // we maintain the information if an exception is thrown.
                configuration = new ServerConfiguration();

                // Load files from configDropins/defaults first
                parseDirectoryFiles(configDefaults, configuration);

                // Parse server.xml and its includes
                parser.parseServerConfiguration(configRoot, configuration);

                // Parse files from configDropins/overrides
                parseDirectoryFiles(configOverrides, configuration);

                configuration.updateLastModified(configRoot.getLastModified());

            } catch (ConfigParserTolerableException ex) {
                // We know what this is, so no need to retry
                throw ex;

            } catch (ConfigParserException cpe) {
                // Wait a short period of time and retry. This is to attempt to handle the case where we
                // parse the configuration in the middle of a file update.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore
                } finally {
                    // Reset the server configuration so that we can start over from the beginning.
                    configuration = new ServerConfiguration();
                    parser.parseServerConfiguration(configRoot, configuration);
                }
            }
        } catch (ConfigParserException ex) {
            if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
                Tr.debug(tc, "Exception while parsing root and referenced config documents.  Message=" + ex.getMessage());
            }

            parser.handleParseError(ex, null);

            if (ErrorHandler.INSTANCE.fail()) {
                // if onError=FAIL, bubble the exception up the stack
                throw ex;
            } else if (ex instanceof ConfigParserTolerableException) {
                // Mark the last update for the configuration so that we don't try to load it again
                configuration.updateLastModified(configRoot.getLastModified());
            } else {
                // onError isn't set to FAIL, but we can't tolerate this exception either
                // so null the configuration reference
                configuration = null;
            }

        }

        return configuration;
    }

    private List<String> getChildXMLNames(WsResource target) {
        if (target == null) {
            return Collections.emptyList();
        }
        File targetFile = target.asFile();
        if ((targetFile == null) || !targetFile.exists()) {
            return Collections.emptyList();
        }
        String[] childNames = targetFile.list();
        if (childNames == null) {
            return Collections.emptyList();
        }

        List<String> xmlChildNames = null;

        for (String childName : childNames) {
            if (!endsWithIgnoreCase(childName, ".xml")) {
                continue;
            }
            File xmlChild = new File(targetFile, childName);
            if (!xmlChild.isFile()) {
                continue;
            }
            if (xmlChildNames == null) {
                xmlChildNames = new ArrayList<>(childNames.length);
            }
            xmlChildNames.add(childName);
        }

        return ((xmlChildNames == null) ? Collections.emptyList() : xmlChildNames);
    }

    public static boolean endsWithIgnoreCase(String value, String suffix) {
        int vLen = value.length();
        int sLen = suffix.length();
        return ((vLen >= sLen) && value.regionMatches(false, vLen - sLen, suffix, 0, sLen));
    }

    private void parseDirectoryFiles(WsResource directory, ServerConfiguration configuration) throws ConfigParserException, ConfigValidationException {
        List<String> childXMLNames = getChildXMLNames(directory);

        Collections.sort(childXMLNames, (name1, name2) -> name1.compareToIgnoreCase(name2));

        for (String xmlName : childXMLNames) {
            WsResource xmlFile = directory.resolveRelative(xmlName);
            if (xmlFile == null) {
                // This should never happen, but it's conceivable that someone could remove a file
                // after listFiles and before getChild
                if (tc.isDebugEnabled()) {
                    Tr.debug(tc, xmlName + " was not found in directory " + directory.getName() + ". Ignoring. ");
                }
                continue;
            }

            Tr.audit(tc, "audit.dropin.being.processed", xmlFile.asFile());
            try {
                parser.parseServerConfiguration(xmlFile, configuration);
            } catch (ConfigParserException ex) {
                parser.handleParseError(ex, null);

                if (ErrorHandler.INSTANCE.fail()) {
                    throw ex; // if onError=FAIL, bubble the exception up the stack
                } else {
                    // Mark the last update for the configuration so that we don't try to load it again
                    configuration.updateLastModified(configRoot.getLastModified());
                }
            }
        }
    }

    @FFDCIgnore(ConfigParserTolerableException.class)
    ServerConfiguration loadNewConfiguration() {
        ServerConfiguration newConfiguration = null;
        if (configRoot.exists()) {
            try {
                newConfiguration = loadServerConfiguration();
                setConfigReadTime();
            } catch (ConfigParserTolerableException e) {
                // This is only thrown if OnError = FAIL
                String message = e.getMessage() == null ? "Parser Failure" : e.getMessage();
                Tr.error(tc, "error.config.update.init", new Object[] { message });
            } catch (ConfigValidationException e) {
                Tr.warning(tc, "warn.configValidator.refreshFailed");
            } catch (ConfigParserException e) {
                Tr.error(tc, "error.config.update.init", new Object[] { e.getMessage() });
            }

            if (newConfiguration == null) {
                return null;
            }
        } else {
            newConfiguration = new ServerConfiguration();
        }

        newConfiguration.setDefaultConfiguration(serverConfiguration.getDefaultConfiguration());
        return newConfiguration;
    }

    public ServerConfiguration getConfiguration() {
        return serverConfiguration;
    }

    public BaseConfiguration getDefaultConfiguration() {
        return serverConfiguration.getDefaultConfiguration();
    }

    public Map<String, LibertyVariable> getVariables() throws ConfigMergeException {
        return serverConfiguration.getVariables();
    }

    public void setNewConfiguration(ServerConfiguration newConfiguration) {
        this.serverConfiguration = newConfiguration;
    }

    public ServerConfiguration copyConfiguration() {
        ServerConfiguration copy = new ServerConfiguration();
        BaseConfiguration dflt = new BaseConfiguration();
        copy.add(getConfiguration());
        dflt.add(getDefaultConfiguration());
        copy.setDefaultConfiguration(dflt);
        return copy;
    }
}
