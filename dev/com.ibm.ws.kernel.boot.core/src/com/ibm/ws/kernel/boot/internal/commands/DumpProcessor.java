/*******************************************************************************
 * Copyright (c) 2012,2025 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.boot.internal.commands;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.ws.kernel.boot.BootstrapConfig;
import com.ibm.ws.kernel.boot.ReturnCode;
import com.ibm.ws.kernel.boot.archive.Archive;
import com.ibm.ws.kernel.boot.archive.ArchiveEntryConfig;
import com.ibm.ws.kernel.boot.archive.ArchiveFactory;
import com.ibm.ws.kernel.boot.archive.DirEntryConfig;
import com.ibm.ws.kernel.boot.archive.DirPattern;
import com.ibm.ws.kernel.boot.archive.DirPattern.PatternStrategy;
import com.ibm.ws.kernel.boot.archive.FileEntryConfig;
import com.ibm.ws.kernel.boot.archive.FilteredDirEntryConfig;
import com.ibm.ws.kernel.boot.cmdline.Utils;
import com.ibm.ws.kernel.boot.internal.BootstrapConstants;

/**
 * Dump command processor.
 * 
 * The main API points are the constructor, which defines the command
 * parameters, and method <code>execute</code>, which performs the dump
 * command and generates a result code.
 */
public class DumpProcessor implements ArchiveProcessor {
    private static String format(String msgId, Object... parms) {
        return MessageFormat.format(BootstrapConstants.messages.getString(msgId), parms);
    }

    //

    private final String serverName;
    private final String regexServerName;
    private final File serverConfigDir;
    private final File serverOutputDir;

    public String getServerName() {
        return serverName;
    }

    private final BootstrapConfig bootProps;

    public BootstrapConfig getBootProps() {
        return bootProps;
    }

    public boolean isServerLogDirConfigured() {
        return bootProps.isConfigured();
    }
    
    private final List<String> javaDumps;

    private final File dumpFile;

    //
    
    /**
     * Main API: Create a dump command processor.
     * 
     * Collect server files into a specified dump file.
     * 
     * Do not included files which have the same paths.
     * 
     * Server files are located according to the user root folder obtained
     * from the boot properties, and according to the server output directory,
     * also obtained from boot properties, and from the server folder, located
     * using the server name relative to the user root folder.  
     *
     * No validation is done of the dump parameters.  Some validation will have
     * been done when setting the boot properties.  Most validation is done when
     * performing the dump operation.
     * 
     * @param serverName The name of the server for which to create the dump.
     * @param dumpFile The dump file which is to be created.
     * @param bootProps Bootstrap configuration values.
     * @param javaDumps Explicit java dumps which are to be included in the dump archive. 
     */
    public DumpProcessor(String serverName, File dumpFile, BootstrapConfig bootProps, List<String> javaDumps) {
        this.serverName = serverName;
        this.regexServerName = Pattern.quote(serverName); // Avoid special characters.
        this.serverConfigDir = new File( bootProps.getUserRoot(), "servers/" + serverName );
        // BootstrapConstants.LOC_AREA_NAME_SERVERS
        this.serverOutputDir = bootProps.getOutputFile(null);
        
        this.bootProps = bootProps;

        this.javaDumps = javaDumps;

        this.dumpFile = dumpFile;
    }

    /**
     * Main API: Perform the dump operation.
     * 
     * The operation will fail if any of the main dump locations cannot be used.
     * These are the dump file, the server folder, and the server output folder.
     * 
     * @return A return code indicating success or failure of the dump operation.
     */
    public ReturnCode execute() {
        Archive archive = null;
        try {
            archive = ArchiveFactory.create(dumpFile); // throws IOException
            archive.addEntryConfigs( createDumpConfigs() );
            // 'createDumpCOnfigs' throws IllegalArgumentException, IOException
            archive.create(); // throws IOException

        } catch ( IllegalArgumentException | IOException e ) {
            System.out.println( format("error.unableZipDir", e) );
            return ReturnCode.ERROR_SERVER_DUMP;
        } finally {
            Utils.tryToClose(archive);
        }

        return ReturnCode.OK;
    }

    /**
     * Local synonym of the regular expression separator.
     * 
     * This is the file system path separator, escaped if necessary.
     */
    private static final String SEP = REGEX_SEPARATOR;
    
    /**
     * Create a pattern for files in the server folder.
     * 
     * @param tail The pattern for files in the server folder.
     * 
     * @return A  pattern for files in the server folder.
     */
    private Pattern serverPattern(String tail) {
        return Pattern.compile(SEP + regexServerName + SEP + tail);
    }

    // See:
    //   https://www.ibm.com/docs/en/was-liberty/base?topic=liberty-customizing-environment
    //
    // Should there be extra steps to collect "server.env"?
    //   ${wlp.install.dir}/etc/server.env [NOT COLLECTED]
    //   ${server.config.dir}/server.env [COLLECTED]
    //
    // Should there be extra steps to collect "jvm.options"?
    //   ${wlp.install.dir}/usr/shared/jvm.options [NOT COLLECTED]
    //   ${server.config.dir}/configDropins/defaults/jvm.options [COLLECTED]
    //   ${server.config.dir}/jvm.options [COLLECTED]
    //   ${server.config.dir}/configDropins/overrides/jvm.options [COLLECTED]
    // 
    // Should key environment values be collected?
    //   wlp.install.dir
    //   server.config.dir
    //
    //   JAVA_HOME
    //   JVM_ARGS
    //
    //   WLP_OUTPUT_DIR
    //   WLP_USER_DIR
    //   WLP_DEBUG_ADDRESS

    /**
     * Create an archive configuration for a server dump.  The archive
     * includes configuration files, log files, some work files, and
     * explicitly requested dumps.
     * 
     * @return An archive configuration
     *
     * @throws IOException Thrown if an expected source file does not exist.
     * @throws IllegalArgumentException Thrown if an expected input directory
     *     is not a directory or if an expected input simply file is not a
     *     simple file.
     */
    private List<ArchiveEntryConfig> createDumpConfigs() throws IOException {
        List<ArchiveEntryConfig> entryConfigs = new ArrayList<ArchiveEntryConfig>();

        // First distinguished subset: configuration files.
        //
        // These are located relative to the server folder.
        //
        // Obscure these files.
        //
        // FilteredDirEntryConfig.configure details the obscurement algorithm.
        //
        // An exception is thrown if the folder does not exist, or is not a folder.
        
        FilteredDirEntryConfig configFiles =
            new FilteredDirEntryConfig(serverConfigDir,
                                       DirPattern.EXCLUDE_BY_DEFAULT,
                                       PatternStrategy.ExcludePreference); // throws IOException, IllegalArgumentException
        
        // TODO: Candidates
        //
        // *.xml (one directory above the server folder)
        // *.properties (one directory above the server folder)
        
        configFiles.include(serverPattern(".*\\.xml"));
        configFiles.include(serverPattern(".*\\.properties"));
        configFiles.include(serverPattern("configDropins")); // ? BootstrapConstants

        // Include these later ...
        //
        // TODO: Are these excludes needed?  The configuration entry is set
        //       to exclude by default.

        configFiles.exclude(serverPattern("dump_" + REGEX_TIMESTAMP));
        configFiles.exclude(serverPattern("autopd")); // ? BootstrapConstants

        // TODO: Should 'logs' and 'workarea' be excluded?

        entryConfigs.add(configFiles);        

        // Second distinguished subset: non-configuration files.
        //
        // An exception is thrown if the folder does not exist, or is not a folder.
        
        DirEntryConfig nonConfigFiles =
            new DirEntryConfig("",
                               serverConfigDir,
                               DirPattern.INCLUDE_BY_DEFAULT,
                               PatternStrategy.ExcludePreference); // throws IOException, IllegalArgumentException

        // Do not include application locations.

        nonConfigFiles.exclude(serverPattern("dropins")); // LOC_AREA_NAME_DROP = "dropins";
        nonConfigFiles.exclude(serverPattern("apps")); // LOC_AREA_NAME_APP = "apps";

        // Exclude security-sensitive files:

        // TODO: Should these be excluded from the prior filtered entry?  

        // LOC_AREA_NAME_RES = "resources";
        nonConfigFiles.exclude(Pattern.compile(SEP + "resources" + SEP + "security")); // security sensitive
        nonConfigFiles.exclude(Pattern.compile("\\.jks$")); // security sensitive
        nonConfigFiles.exclude(Pattern.compile("\\.p12$")); // security sensitive.

        // Include these later ...        
        nonConfigFiles.exclude(serverPattern("dump_" + REGEX_TIMESTAMP));
        nonConfigFiles.exclude(serverPattern("autopd")); // ? BootstrapConstants
        
        // Include these later ...                
        nonConfigFiles.exclude(serverPattern("logs")); // BootstrapConstants.LOC_AREA_NAME_LOGS
        nonConfigFiles.exclude(serverPattern("workarea")); // BootstrapConstants.LOC_AREA_NAME_WORKING

        // Exclude server package files ...
        nonConfigFiles.exclude(Pattern.compile(SEP + regexServerName + "\\.(zip|pax)$"));
        nonConfigFiles.exclude(Pattern.compile(SEP + regexServerName + "\\.dump-" + REGEX_TIMESTAMP + "\\.(zip|pax)$"));
        
        // Exclude various dump files ...
        nonConfigFiles.exclude(serverPattern("core\\.[^\\\\/]+\\.dmp"));
        nonConfigFiles.exclude(serverPattern("heapdump\\.[^\\\\/]+\\.phd"));
        nonConfigFiles.exclude(serverPattern("java\\.[^\\\\/]+\\.hprof"));
        nonConfigFiles.exclude(serverPattern("javacore\\.[^\\\\/]+\\.txt"));
        nonConfigFiles.exclude(serverPattern("javadump\\.[^\\\\/]+\\.txt"));

        entryConfigs.add(nonConfigFiles);

        // Third, add server outputs.
        
        DirEntryConfig outputConfigFiles =
            new DirEntryConfig("",
                               serverOutputDir,
                               DirPattern.EXCLUDE_BY_DEFAULT,
                               PatternStrategy.ExcludePreference); // throws IOException, IllegalArgumentException

        // Finally, include these.
        outputConfigFiles.include(serverPattern("dump_" + REGEX_TIMESTAMP));
        outputConfigFiles.include(serverPattern("autopd")); // ? BootstrapConstants
        outputConfigFiles.include(serverPattern("logs")); // BootstrapConstants.LOC_AREA_NAME_LOGS

        // Include workearea ... but ...

        // BootstrapConstants.LOC_AREA_NAME_WORKING
        outputConfigFiles.include(serverPattern("workarea"));

        // ... exclude work-area locked files ...
        outputConfigFiles.exclude(serverPattern("workarea" + SEP + "\\.sLock$"));
        outputConfigFiles.exclude(serverPattern("workarea" + SEP + "\\.sCommand$"));

        // ... exclude OSGI specific log files ...
        String OSGI_SUBPATH = "workarea" + SEP + ".*" + "org\\.eclipse\\.osgi" + SEP;
        outputConfigFiles.exclude(serverPattern(OSGI_SUBPATH + "\\.manager"));

        // ... and exclude OSGI application cache files.
        String APP_CACHE_PATH = "bundles" + SEP + "\\d+" + SEP + "data" + SEP + ".*com\\.ibm\\.ws\\.app\\.manager_gen";
        outputConfigFiles.exclude(serverPattern(OSGI_SUBPATH + APP_CACHE_PATH));

        entryConfigs.add(outputConfigFiles);

        // Include explicitly requested java dump files.

        for ( String javaDump : javaDumps ) {
            File f = new File(javaDump);
            if ( f.exists() ) {
                FileEntryConfig feg = new FileEntryConfig("", f); // throws IllegalArgumentException
                entryConfigs.add(feg);
            } else {
                System.out.println( format("error.missingDumpFile", javaDump) );
            }
        }

        return entryConfigs;
    }
}
