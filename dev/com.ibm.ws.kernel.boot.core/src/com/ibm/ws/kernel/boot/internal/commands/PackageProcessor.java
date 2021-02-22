/*******************************************************************************
 * Copyright (c) 2012, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.boot.internal.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import com.ibm.ws.kernel.boot.BootstrapConfig;
import com.ibm.ws.kernel.boot.Debug;
import com.ibm.ws.kernel.boot.ReturnCode;
import com.ibm.ws.kernel.boot.archive.Archive;
import com.ibm.ws.kernel.boot.archive.ArchiveEntryConfig;
import com.ibm.ws.kernel.boot.archive.ArchiveFactory;
import com.ibm.ws.kernel.boot.archive.DirEntryConfig;
import com.ibm.ws.kernel.boot.internal.BootstrapConstants;
import com.ibm.ws.kernel.boot.internal.FileUtils;
import com.ibm.ws.kernel.boot.internal.commands.ProcessorUtils.LooseConfig;
import com.ibm.ws.kernel.provisioning.ProductExtension;
import com.ibm.ws.kernel.provisioning.ProductExtensionInfo;

/**
 * Package processor.
 */
public class PackageProcessor implements ArchiveProcessor {
    // Miscellaneous helpers ...

    private static void println(String msgKey, Object... msgParms) {
        System.out.println( BootstrapConstants.format(msgKey, msgParms) );
    }

    private static boolean java2SecurityEnabled() {
        return ( System.getSecurityManager() != null );
    }

    // Package options ...

    private final String processName;

    private final File packageFile;
    private final boolean isPackageJar;

    private final Set<String> explicitPaths;

    // Package include options ...

    // Keys are comma delimited lists of IncludeOption print strings.
    private final Map<PackageOption, String> options;

    public enum PackageOption {
        INCLUDE;
    }

    public enum IncludeOption {
        ALL("all"),
        USR("usr"),
        MINIFY("minify"),
        WLP("wlp"),
        RUNNABLE("runnable");

        private final String value;

        private IncludeOption(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        /**
         * Tell if a values list, encoded as a comma delimited list
         * of option values, matches this include option.  The list
         * matches this option if one of the list elements is equal
         * to the value of this option.  Case is ignored.
         * 
         * @param optionValuesText A comma delimited list of of
         *     option values.  May be null.
         *
         * @return True or false telling if this option's value is
         *     one of the elements of the value list.
         */
        public boolean matches(String optionValuesText) {
            if (optionValuesText != null) {
                String[] optionValuesArray = optionValuesText.split(",");
                for (String optionValue : optionValuesArray) {
                    if (optionValue.equalsIgnoreCase(value)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * @return true for include values of "include=minify"
     */
    public static boolean containsMinify(String val) {
        return IncludeOption.MINIFY.matches(val);
    }

    /**
     * @return true for include value of "include=usr".
     */
    public static boolean containsUsr(String val) {
        return IncludeOption.USR.matches(val);
    }
    
    /**
     * @return true for include value of "include=usr".
     */
    public static boolean containsRunnable(String val) {
        return IncludeOption.RUNNABLE.matches(val);
    }

    /**
     * @return true for include values of "include=all"
     *     or "include=runnable".  "minify" may not be
     *     present with "runnable".
     */
    private static boolean containsAllorNoMinifyRunnable(String val) {
        return ( IncludeOption.ALL.matches(val) ||
                 (IncludeOption.RUNNABLE.matches(val) &&
                  !IncludeOption.MINIFY.matches(val)) );
    }

    /**
     * @return true if --include=runnable was specified on the package command.
     */
    private boolean includeRunnable() {
        return containsRunnable(options.get(PackageOption.INCLUDE));
    }

    /**
     * @return true if --include=usr was specified on the package command.
     */
    private boolean includeUsr() {
        return containsUsr( options.get(PackageOption.INCLUDE) );
    }

    // Property file constants ...
    
    private static final String PROP_FILE_NAME =
        "/lib/versions/WebSphereApplicationServer.properties";
    private static final String BACKUP_PROP_FILE_NAME =
        "WebSphereApplicationServer.properties.bak";
    private static final String INSTALL_TYPE_PROPERTY_NAME =
        "com.ibm.websphere.productInstallType";
    private static final String INSTALL_TYPE_ARCHIVE = "Archive";

    // Boot properties and cached boot property values ...

    // TODO: Where is this used?
    protected static final String DEFAULT_CONFIG_LOCATION_KEY = "configLocation";

    private final BootstrapConfig bootProps;

    private final boolean isClientProcess;

    private final File installRoot;
    private final File installParent;
    private final File propFile;
    
    private final File wlpUserDir;
    private final File processConfigDir;
    private final File processOutputDir;

    private final File prodExtDir;
    
    private final File workAreaTmpDir;
    private final File backupPropFile;

    private Set<File> getLooseConfigFiles() {
        return ProcessorUtils.getLooseConfigFiles(bootProps);
    }

    public boolean hasProductExtensions() {
        return ( prodExtDir.exists() );
    }

    // Optional override of the server root location ...
    
    public static final String PACKAGE_ARCHIVE_PREFIX = "wlp/";

    private String packagePrefix = PACKAGE_ARCHIVE_PREFIX;
    private boolean isServerRootOptionSet;

    // Core API ...

    // Called from ...
    //   com.ibm.ws.kernel.boot.internal.commands.
    //     PackageCommand.packageServerRuntime(File, boolean)
    // ... when 'server-root' is specified as a command line option.

    public void setArchivePrefix(String prefix) {
        packagePrefix = prefix + '/';
        isServerRootOptionSet = true;
    }

    // Core API ...
    
    // Explicit paths are used when the include option is 'minify'.
    //
    // The explicit paths are used both when selecting files beneath
    // the product installation root, and when selecting files beneath
    // product extension locations.

    public PackageProcessor(
        String processName,
        File packageFile,
        BootstrapConfig bootProps,
        List<Pair<PackageOption, String>> options,
        Set<String> explicitPaths) {

        this.processName = processName;

        this.packageFile = packageFile;
        this.isPackageJar = packageFile.getName().endsWith(".jar");

        this.options = new HashMap<PackageOption, String>();
        if (options != null) {
            for (Pair<PackageOption, String> option : options) {
                this.options.put(option.getPairKey(), option.getPairValue());
            }
        }

        this.bootProps = bootProps;

        this.isClientProcess = bootProps.getProcessType() == BootstrapConstants.LOC_PROCESS_TYPE_CLIENT;

        this.workAreaTmpDir = new File(bootProps.get(BootstrapConstants.LOC_PROPERTY_SRVTMP_DIR));
        this.backupPropFile = new File(workAreaTmpDir, BACKUP_PROP_FILE_NAME);

        this.installRoot = bootProps.getInstallRoot();
        this.installParent = installRoot.getParentFile();
        this.propFile = new File(installRoot, PROP_FILE_NAME);

        this.wlpUserDir = bootProps.getUserRoot();
        this.prodExtDir = ProcessorUtils.getFileFromDirectory(
            wlpUserDir.getParentFile(), "/etc/extension");

        this.processConfigDir = bootProps.getConfigFile(null);
        this.processOutputDir = bootProps.getOutputFile(null);

        this.explicitPaths = explicitPaths;
    }

    // Core API ...
    
    public ReturnCode execute(boolean runtimeOnly) {
        // Dont allow --include=usr and --archive=*.jar combination
        if (includeUsr() && isPackageJar) {
            println("error.package.usr.jar", processName);
            return ReturnCode.ERROR_SERVER_PACKAGE;
        }

        try {
            this.workAreaTmpDir.mkdirs();        

            try {
                ReturnCode rc = backupPropertyFile();
                if (!rc.equals(ReturnCode.OK)) {
                    return rc;
                }

                try ( Archive archive = ArchiveFactory.create(packageFile, java2SecurityEnabled()) ) {  
                    // Prepare the manifest for a jar archive.
                    // Start with the original manifest of the installation.
                    // Adjust the manifest for USR and RUNNABLE includes.
                    //
                    // The manifest must be the first entry of the archive.

                    if ( isPackageJar ) {
                        File manifestFile = getManifestFile();
                        if ( manifestFile == null ) {
                            println("error.minify.missing.manifest", processName);
                            return ReturnCode.ERROR_SERVER_PACKAGE;
                        }

                        // Build a special manifest for --include=usr.
                        // Build a special manifest for --include=runnable
                        // Otherwise, use the manifest as is.
                        if ( includeUsr() ) {
                            manifestFile = buildUsrManifest(manifestFile);
                        } else if ( includeRunnable() ) {
                            manifestFile = buildRunnableManifest(manifestFile);
                        }
                        archive.addFileEntry("META-INF/MANIFEST.MF", manifestFile); 

                        // add any meta-inf folder content, and add the auto-extract code.
                        archive.addEntryConfigs( createSelfExtractConfigs() );
                    }

                    // Next add entry configurations based on the include options ...

                    // The default is "all".
                    // "all" takes precedence over all other include options.
                    // Next, "runnable", as long as "minify" is not present.
                    // Then "usr", then finally "minify".  "minify" is not
                    // allowed on a client process.

                    List<ArchiveEntryConfig> optionConfigs = new ArrayList<ArchiveEntryConfig>();
                    if ( options.isEmpty()) {
                        addAllConfigs(optionConfigs, runtimeOnly);
                    } else {
                        String includeValue = options.get(PackageOption.INCLUDE);
                        if ( containsAllorNoMinifyRunnable(includeValue) ) {
                            addAllConfigs(optionConfigs, runtimeOnly);
                        } else if ( containsUsr(includeValue) ) {
                            addUsrConfigs(optionConfigs, true);
                        } else if ( !isClientProcess && containsMinify(includeValue) ) {
                            addMinifyConfigs(optionConfigs);
                        } else {
                            println("warn.packageServer.include.unknownOption", includeValue);
                            addAllConfigs(optionConfigs, runtimeOnly);
                        }
                    }
                    archive.addEntryConfigs(optionConfigs);

                    // Finally, create the archive ...
                    archive.create();

                    return ReturnCode.OK;
                }

            } finally {
                try {
                    restorePropertyFile();
                } finally {
                    FileUtils.recursiveClean(workAreaTmpDir);
                }
            }

        } catch ( Exception e ) {
            println("error.unableZipDir", e);
            Debug.printStackTrace(e);
            return ReturnCode.ERROR_SERVER_PACKAGE;
        }
    }

    // Manifest entry configuration handling ...

    /**
     * Locate the installation manifest.  Look first in the "jar -jar"
     * extraction location, "lib/extra/META-INF/MANIFEST.MF", relative
     * to the installation root.  Second, look in the direct extraction location,
     * "META-INF/MANIFEST.MF", next to the installation root.
     *
     * @return The location of the installation manifest.  Null
     *     if it does not exist in either of the expected locations.
     */
    private File getManifestFile() {
        // The is redundant with initial processing of 'createSelfExtractConfigs',
        // but not enough is saved by removing the redundancy, which would require
        // either multiple return values, or would require two new
        // non-final instance variables.
        
        File manifestFile = new File(installRoot, "lib/extract/META-INF/MANIFEST.MF");
        if ( !manifestFile.exists() ) {
            manifestFile = new File(installParent, "META-INF/MANIFEST.MF");
            if ( !manifestFile.exists() ) {
                manifestFile = null;
            }
        }
        return manifestFile;
    }
    
    /**
     * Create a proper manifest file for the --include=usr option. The manifest
     * is a copy of the installation manifest, with the following edits:
     *
     * <ul>
     * <li>Removed: License-Agreement</li>
     * <li>Removed: License-Information</li>
     * <li>Added: Applies-To: com.ibm.websphere.appserver</li>
     * <li>Added: Extract-Installer: false</li>
     * </ul>
     *
     * @param installationManifest The location of the base installation manifest.
     *
     * @return the manifest file
     * 
     * @throws IOException If the old manifest could not be read, or if the
     *    updated manifest could not be written.
     */
    protected File buildUsrManifest(File installationManifest) throws IOException {
        Manifest mf = new Manifest();
        try ( InputStream input = new FileInputStream(installationManifest) ) {
            mf.read(input);
        }

        Attributes mainAttributes = mf.getMainAttributes();

        mainAttributes.remove(new Attributes.Name("License-Agreement"));
        mainAttributes.remove(new Attributes.Name("License-Information"));

        mainAttributes.remove(new Attributes.Name("Applies-To"));
        mainAttributes.putValue("Applies-To", "com.ibm.websphere.appserver");

        mainAttributes.remove(new Attributes.Name("Extract-Installer"));
        mainAttributes.putValue("Extract-Installer", "false");

        File newMani = new File(workAreaTmpDir, "MANIFEST.usrinclude.tmp");
        try ( FileOutputStream output = new FileOutputStream(newMani) ) {
            mf.write(output);
        }

        return newMani;
    }

    /**
     * Create a proper manifest file for the --include=execute option. The manifest is a copy
     * of the given installation manifest, with the following edits:
     *
     * Change from: "Main-Class: wlp.lib.extract.SelfExtract"
     * 
     * to: "Main-Class: wlp.lib.extract.SelfExtractRun"
     * 
     * add: "Server-Name: &lt;processName&gt;"
     *
     * @return the manifest file
     * 
     * @throws IOException Thrown if the old manifest could not be read, or if
     *     the updated manifest could not be written.
     */
    protected File buildRunnableManifest(File installationManifest) throws IOException {
        Manifest mf = new Manifest();
        try ( InputStream input = new FileInputStream(installationManifest) ) { 
            mf.read(input);
        }

        Attributes mainAttributes = mf.getMainAttributes();

        mainAttributes.remove(new Attributes.Name("License-Agreement"));
        mainAttributes.remove(new Attributes.Name("License-Information"));

        mainAttributes.putValue("Main-Class", "wlp.lib.extract.SelfExtractRun");
        mainAttributes.putValue("Server-Name", processName);

        // For Java 9, we need to apply the /wlp/lib/platform/java/java9.options to the manifest.
        String specVersion = System.getProperty("java.specification.version");
        if ((specVersion != null) && !specVersion.startsWith("1.")) {
            Map<String, String> map = readJava9Options();
            mainAttributes.putValue("Add-Exports", map.get("exports"));
            mainAttributes.putValue("Add-Opens", map.get("opens"));
        }

        File newMani = new File(workAreaTmpDir, "MANIFEST.usrinclude.tmp");
        try ( FileOutputStream out = new FileOutputStream(newMani) ) {
            mf.write(out);
        }

        return newMani;
    }

    private List<ArchiveEntryConfig> createSelfExtractConfigs() throws IOException {
        List<ArchiveEntryConfig> selfExtractConfigs = new ArrayList<ArchiveEntryConfig>();

        // The is redundant with 'getManifestFile', but not enough
        // is saved by removing the redundancy, which would require
        // either multiple return values, or would require two new
        // non-final instance variables.

        File metaInf = new File(installRoot, "lib/extract/META-INF");
        if (!metaInf.exists()) {
            // maybe user didn't extract file with jar -jar, but unzipped..
            // so look for META-INF above WLP Root !!
            metaInf = new File(installParent, "META-INF");
        }

        // TFB: This was changed from *excluding* by default to "including*
        //      by default.  Otherwise, the exclusion of the manifest file
        //      has no effect.  The intent seems to be to include the
        //      previously excluded META-INF folder, while excluding the
        //      manifest, which was added previously.

        // MetaInf will not be null now, as manifest has already been found
        // not null using the same tests within the calling 'execute' method.

        // Add the META-INF folder ...
        DirEntryConfig metaInfConfig =
            DirEntryConfig.includeDir(selfExtractConfigs, "META-INF/", metaInf);
        // But exclude the manifest, which was added previously.
        metaInfConfig.exclude(new File(metaInf, "MANIFEST.MF"));

        // Add the self-extraction entries.
        addLibExtractDir(selfExtractConfigs);

        return selfExtractConfigs;
    }

    private void addLibExtractDir(List<ArchiveEntryConfig> packagingConfigs)
        throws IOException {

        File libExtractDir = new File(installRoot, "lib/extract");

        try {
            // Include the entire 'lib/extract' directory.
            @SuppressWarnings("unused")
            DirEntryConfig extractConfig = DirEntryConfig.includeDir(
                packagingConfigs, packagePrefix + "lib/extract", libExtractDir);

        } catch ( FileNotFoundException e ) {
            println("error.package.missingLibExtractDir");
            Debug.printStackTrace(e);            
            throw e;
        }
        // leave other IOExceptions to be thrown upwards
    }

    //

    private void addMinifyConfigs(List<ArchiveEntryConfig> packagingConfigs)
        throws IOException {

        // Create an empty (includeByDefault==false) config for the root dir of liberty
        DirEntryConfig rootDirConfig =
            DirEntryConfig.excludeDir(packagingConfigs, packagePrefix, installRoot);

        // Add any explicitly specified paths ... and remember the added patterns.
        List<Pattern> explicitPatterns = new ArrayList<Pattern>( explicitPaths.size() );
        for ( String explicitPath : explicitPaths ) {
            explicitPatterns.add( rootDirConfig.includePath(explicitPath) );
        }

        // Add product extensions ...
        //
        // These *must* be in directories which do not overlap with
        // the product location.
        //
        // Note that a non-absolute extension location is resolved as a peer
        // of the installation root, not as a child of the installation root.

        for (ProductExtensionInfo extInfo : ProductExtension.getProductExtensions()) {
            String extLocation = extInfo.getLocation();
            File extDir = new File(extLocation);
            if (!extDir.isAbsolute()) {
                extDir = ProcessorUtils.getFileFromDirectory(installParent, extLocation);
            }

            DirEntryConfig extensionConfig =
                DirEntryConfig.excludeDir(packagingConfigs, extLocation, extDir);
            extensionConfig.include(explicitPatterns);
        }

         // Add back the lafiles directory
         // these are 'owned' by the installer.
        File lafilesDir = new File(installRoot, "lafiles");
        if (lafilesDir.exists()) {
            @SuppressWarnings("unused")
            DirEntryConfig lafilesDirConfig =
                DirEntryConfig.includeDir(packagingConfigs, packagePrefix + "lafiles", lafilesDir);
        }

        // Add back the templates directory
        // (these are orphans that need resolution still)
        File templatesDir = new File(installRoot, "templates");
        @SuppressWarnings("unused")
        DirEntryConfig templatesDirConfig =
            DirEntryConfig.includeDir(packagingConfigs, packagePrefix + "templates", templatesDir);

        // Add back the templates directory (if building a jar, it's already added)
        // these are 'owned' by the installer.
        if ( !isPackageJar ) {
            addLibExtractDir(packagingConfigs);
        }

        // Add usr content.
        addUsrConfigs(packagingConfigs, false);

        // add the package_<timestamp>.txt
        addPkgInfoConfigs(packagingConfigs);
    }

    private void addAllConfigs(List<ArchiveEntryConfig> packagingConfigs, boolean runtimeOnly)
        throws IOException {

        // Add wlp's root directory
        DirEntryConfig rootDirConfig =
            DirEntryConfig.includeDir(packagingConfigs, packagePrefix, installRoot);

        // include all underneath install-root except usr directory
        rootDirConfig.excludeExpression(
            REGEX_SEP + Pattern.quote(installRoot.getName()) + REGEX_SEP +
            BootstrapConstants.LOC_AREA_NAME_USR);

        // if we are building a jar, we have already included the lib/extract content.
        if ( isPackageJar ) {
            File libExtract = new File(installRoot, "lib/extract");
            rootDirConfig.exclude(libExtract);
        }

        // exclude the server.usr.dir and server.output.dir, because they may
        // be specified underneath install-root

        String installRootAbsPath = installRoot.getAbsolutePath();

        String userRootAbsPath = wlpUserDir.getAbsolutePath();
        String processOutputAbsPath = processOutputDir.getAbsolutePath();
        if (userRootAbsPath.contains(installRootAbsPath)) {
            rootDirConfig.excludeExpression(Pattern.quote(userRootAbsPath));
        }
        if (processOutputAbsPath.contains(installRootAbsPath)) {
            rootDirConfig.excludeExpression(Pattern.quote(processOutputAbsPath));
        }

        // Add usr directory
        if (!runtimeOnly) {
            addUsrConfigs(packagingConfigs, true);
        }

        // add the package_<timestamp>.txt
        addPkgInfoConfigs(packagingConfigs);

        // Add product extensions
        if (prodExtDir.exists()) {
            @SuppressWarnings("unused")
            DirEntryConfig prodExtDirConfig = DirEntryConfig.includeDir(
                packagingConfigs, packagePrefix + "etc/extensions", prodExtDir);
        }

        for (ProductExtensionInfo info : ProductExtension.getProductExtensions()) {
            File extensionDir = new File(info.getLocation());
            if (!extensionDir.isAbsolute()) {
                extensionDir = ProcessorUtils.getFileFromDirectory(installParent, info.getLocation());
            }

            @SuppressWarnings("unused")            
            DirEntryConfig looseExtConfig = DirEntryConfig.includeDir(
                packagingConfigs, info.getLocation(), extensionDir);
        }
    }

    private void addUsrConfigs(
        List<ArchiveEntryConfig> packagingConfigs, boolean addUsrExtension)
        throws IOException {

        // Add the archived loose files
        ReferencedResources referencedResources = getReferencedResources(packagingConfigs);

        // Add process config directory 
        // either <userRoot>/servers/<processName>
        // or     <userRoot>/clients/<processName>)
        String locAreaName;
        if ( isClientProcess ) {
            locAreaName = BootstrapConstants.LOC_AREA_NAME_CLIENTS;
        } else {
            locAreaName = BootstrapConstants.LOC_AREA_NAME_SERVERS;
        }

        // if --server-root set, then don't add /usr/ in path
        String processPrefix = packagePrefix;
        if ( !isServerRootOptionSet || !includeUsr() ) {
            processPrefix += BootstrapConstants.LOC_AREA_NAME_USR + '/';
        }
        processPrefix += locAreaName + '/' + processName + '/';

        DirEntryConfig processConfigDirConfig =
            DirEntryConfig.includeDir(packagingConfigs, processPrefix,  processConfigDir);

        // avoid any special characters in processName when construct patterns
        String regexProcessName = Pattern.quote(processName);
        String regexProcessPath = REGEX_SEP + regexProcessName + REGEX_SEP;

        // {server.config.dir} may be equal {server.output.dir},
        // Exclude workarea and logs directories
        processConfigDirConfig.excludeExpressions(
            regexProcessPath + "workarea",
            regexProcessPath + "logs");
        processConfigDirConfig.includeExpression(
            regexProcessPath + "workarea" + REGEX_SEP + "\\.sLock$");

        // Exclude dump directory
        processConfigDirConfig.excludeExpression(
            regexProcessPath + "dump_" + REGEX_TIMESTAMP);

        // Exclude javadump outputs
        processConfigDirConfig.excludeExpressions(
            regexProcessPath + "core\\.[^\\\\/]+\\.dmp",
            regexProcessPath + "heapdump\\.[^\\\\/]+\\.phd",
            regexProcessPath + "java\\.[^\\\\/]+\\.hprof",
            regexProcessPath + "javacore\\.[^\\\\/]+\\.txt",
            regexProcessPath + "javadump\\.[^\\\\/]+\\.txt");

        // Exclude server package and dump files.
        processConfigDirConfig.excludeExpressions(
            REGEX_SEP + regexProcessName + "\\.(zip|pax|jar)$",
            REGEX_SEP + regexProcessName + "\\.dump-" + REGEX_TIMESTAMP + "\\.(zip|pax)$");

        // Exclude the package_<timestamp>.txt file, will add it later
        processConfigDirConfig.excludeExpression(
            regexProcessPath + "package_" + REGEX_TIMESTAMP + "\\.txt");

        // exclude loose xml files from server config directory
        for (File looseApp : referencedResources.looseApps) {
            String looseAppExpr = "." + looseApp.getName().replace(".", "\\.");
            processConfigDirConfig.excludeExpression(looseAppExpr);
        }

        // Add shared directory
        File sharedDir = ProcessorUtils.getFileFromDirectory(wlpUserDir, "shared");
        if (sharedDir.exists()) {
            String entryPrefix = packagePrefix;
            if (!isServerRootOptionSet || !includeUsr()) {
                entryPrefix += BootstrapConstants.LOC_AREA_NAME_USR + '/';
            }
            entryPrefix +=  BootstrapConstants.LOC_AREA_NAME_SHARED + '/';                 

            DirEntryConfig sharedDirConfig =
                DirEntryConfig.includeDir(packagingConfigs, entryPrefix, sharedDir);

            // exclude security sensitive files
            sharedDirConfig.excludeExpressions(
                REGEX_SEP + "resources" + REGEX_SEP + "security" + REGEX_SEP + "key.jks",
                REGEX_SEP + "resources" + REGEX_SEP + "security" + REGEX_SEP + "key.p12");

            // exclude loose xml files
            for (File looseApp : referencedResources.looseApps) {
                if (FileUtils.isUnderDirectory(looseApp, sharedDir)) {
                    String looseAppExpr = "." + looseApp.getName().replace(".", "\\.");
                    sharedDirConfig.excludeExpression(looseAppExpr);
                }
            }
        }

        // Add /usr/extension directory...aka user features

        if (addUsrExtension) {
            File extensionDir = ProcessorUtils.getFileFromDirectory(wlpUserDir, BootstrapConstants.LOC_AREA_NAME_EXTENSION);
            if (extensionDir.exists()) {
                String extensionPrefix = packagePrefix;
                if ( !isServerRootOptionSet || !includeUsr() ) {
                    extensionPrefix += BootstrapConstants.LOC_AREA_NAME_USR + '/';
                }
                extensionPrefix += BootstrapConstants.LOC_AREA_NAME_EXTENSION + '/';

                @SuppressWarnings("unused")
                DirEntryConfig extensionConfig =
                    DirEntryConfig.includeDir(packagingConfigs, extensionPrefix, extensionDir);
            }
        }
    }

    private void addPkgInfoConfigs(List<ArchiveEntryConfig> pkgInfoConfigs)
        throws IOException {

        // Include the package_<timestamp>.txt that was generated
        // in the server output dir.  
        // and must be move into lib/versions

        DirEntryConfig pkgInfoConfig = DirEntryConfig.excludeDir(pkgInfoConfigs,
            packagePrefix + BootstrapConstants.LOC_AREA_NAME_LIB + '/' + "versions" + '/',
            processOutputDir);

        pkgInfoConfig.includeExpression(
            REGEX_SEP + Pattern.quote(processName) + REGEX_SEP +
            "package_" + REGEX_TIMESTAMP + "\\.txt");
    }

    private static class ReferencedResources {
        public final Set<File> looseApps = new HashSet<File>();
    }

    private ReferencedResources getReferencedResources(List<ArchiveEntryConfig> packagingConfigs) {
        ReferencedResources referencedResources = new ReferencedResources();

        addLooseApplications(packagingConfigs, referencedResources.looseApps);

        return referencedResources;
    }

    /**
     * Populate the loose applications collection, and add entries
     * for each of these loose applications to the package configurations.
     *
     * The loose applications are needed, as they must be excluded, which
     * is done later in the processing.
     *
     * Ignore any loose configuration which cannot be processed, including
     * ignoring any exceptions which occur while attempting to repackage
     * the loose configuration files.
     *
     * @param packagingConfigs The overall packaging configurations.
     * @param looseApps Storage for loose application files.
     */
    private void addLooseApplications(
        List<ArchiveEntryConfig> packagingConfigs, Set<File> looseApps) {

        looseApps.addAll( getLooseConfigFiles() );

        // TFB: No longer remove loose configuration files
        //      in case of a processing failure.  That means,
        //      the loose configuration files will be excluded
        //      by later processing.
        //
        // TFB: Add the intermediate configurations for the
        //      repackaged application only if all all obtained
        //      without an exception.

        Iterator<File> it = looseApps.iterator();
        while ( it.hasNext() ) {
            File looseFile = it.next();

            LooseConfig looseConfig;
            try {
                looseConfig = ProcessorUtils.convertToLooseConfig(looseFile);
                if ( looseConfig == null ) {
                    println("warn.package.invalid.looseFile", looseFile);
                    // it.remove(); // TFB: Do exclude the loose configuration file.
                    continue;
                }
            } catch ( Exception e ) {
                println("warn.package.invalid.looseFile", looseFile);
                Debug.printStackTrace(e);
                // it.remove(); // TFB: Do exclude the loose configuration file.
                continue;
            }

            List<ArchiveEntryConfig> looseConfigs = new ArrayList<ArchiveEntryConfig>();

            try {
                looseConfigs.addAll(
                    ProcessorUtils.createLooseExpandedArchiveEntryConfigs(
                        looseConfig, looseFile, bootProps, packagePrefix, includeUsr()));
            } catch ( Exception e ) {
                println("warning.unableToPackageLooseConfigFileMissingPath", looseFile);
                Debug.printStackTrace(e);
                // it.remove(); // TFB: Do exclude the loose configuration file.
                continue; // Don't add the incompletely generated loose entry configurations.
            }

            packagingConfigs.addAll(looseConfigs);
        }
    }

    // Property file handling ...

    // TODO: This should be handled by creating the new
    //       property file in the temporary work area, and
    //       mapping the updated property file into the
    //       package, the same as is done for the manifest.
    //
    //       While there are steps to restore the property file
    //       that is not reliable: A crash or hard kill of the
    //       packaging process will leave the property file
    //       in an incorrect state.

    /**
     * Update the product installation type to 'Archive'.
     * 
     * Do nothing if the installation type is already 'Archive'.
     * 
     * If an update is made, create a backup of the product
     * properties file.  That file will be restored at the
     * conclusion of packaging.
     *
     * @return {@link ReturnCode#OK} if the installation type
     *     was successfully updated.  Any other return code
     *     if the update failed.
     */
    private ReturnCode backupPropertyFile() {
        // TODO: Is this OK?  Should this be an error,
        //       or should a warning be displayed?
        if ( !propFile.exists() ) {
            return ReturnCode.OK;
        }

        try {
            Properties wlpProp = new Properties();
            try ( InputStream originalInput = new FileInputStream(propFile) ) { 
                wlpProp.load(originalInput);
            }

            String origInstallType = wlpProp.getProperty(INSTALL_TYPE_PROPERTY_NAME);
            if ( (origInstallType != null) && origInstallType.equals(INSTALL_TYPE_ARCHIVE) ) {
                return ReturnCode.OK;
            }
            String backupComment = INSTALL_TYPE_PROPERTY_NAME + "=" + origInstallType;

            copy(propFile, backupPropFile);

            wlpProp.setProperty(INSTALL_TYPE_PROPERTY_NAME, INSTALL_TYPE_ARCHIVE);
            try ( OutputStream originalOutput = new FileOutputStream(propFile) ) {
                wlpProp.store(originalOutput, backupComment);
            }

            return ReturnCode.OK;
            
        } catch (Exception e) {
            Debug.printStackTrace(e);
            return ReturnCode.RUNTIME_EXCEPTION;
        }
    }

    /**
     * Restore the product properties file.  It may have been
     * updated during packaging.
     * 
     * Do nothing if the properties file does not exist, or
     * if the backup properties file does not exist.  (No backup
     * will is made if the product installation type was
     * already 'Archive'.)
     *
     * @return {@link ReturnCode#OK} if the product properties
     *     file was successfully restored.  Any other return code
     *     indicates a failure.
     */
    private ReturnCode restorePropertyFile() {
        // Allowed: See 'backupPropertyFile' for comments.
        if ( !propFile.exists() ) {
            return ReturnCode.OK;
        }

        // Possible, if either the property file did not exist,
        // or if it already had the correct installation type.
        if ( !backupPropFile.exists() ) {
            return ReturnCode.OK;
        }

        try {
            copy(backupPropFile, propFile);

            // The cleanup step will delete the backup file.
            // propBackupFile.delete();

            return ReturnCode.OK;

        } catch (Exception e) {
            Debug.printStackTrace(e);
            return ReturnCode.RUNTIME_EXCEPTION;
        }
    }

    private void copy(File inputFile, File outputFile) throws IOException {
        Files.copy(inputFile.toPath(), outputFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES,
            StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Scrape "exports" and "opens" from Java9 options.
     *
     * @return The options and exports as space delimited lists.
     *
     * @throws IOException Thrown if the read failed.
     */
    private Map<String, String> readJava9Options() throws IOException {
        StringBuilder exports = new StringBuilder();
        StringBuilder opens = new StringBuilder();

        String optionsPath = installRoot.getAbsolutePath() +
                File.separator + "lib" +
                File.separator + "platform" +
                File.separator + "java" +
                File.separator + "java9.options";

        try ( FileReader optionsFileReader = new FileReader(optionsPath) ) {
            BufferedReader optionsLineReader = new BufferedReader(optionsFileReader);

            String line;
            while ( (line = optionsLineReader.readLine()) != null ) {
                // TODO: Should the lines be trimmed?

                if (line.startsWith("#")) {
                    continue;
                }

                // TODO: Should this be "startsWith".
                // TODO: Should the values be trimmed?

                if (line.contains("--add-export")) {
                    line = optionsLineReader.readLine();
                    if ( line != null ) {
                        exports.append(getValue(line));
                        exports.append(' ');
                    } else {
                        break; // TODO: Malformed Java9 options.
                    }
                } else if (line.contains("--add-open")) {
                    line = optionsLineReader.readLine();
                    if ( line != null ) {
                        opens.append(getValue(line));
                        exports.append(' ');
                    } else {
                        break; // TODO: Malformed Java9 options.
                    }
                }
            }
        }

        Map<String, String> java9Options = new HashMap<String, String>();
        java9Options.put("exports", exports.toString().trim());
        java9Options.put("opens", opens.toString().trim());

        return java9Options;
    }

    private String getValue(String line) {
        int loc = line.indexOf("=");
        return line.substring(0, loc);
    }
}