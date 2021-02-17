/*******************************************************************************
 * Copyright (c) 2012-2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.boot.internal.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.ws.kernel.boot.BootstrapConfig;
import com.ibm.ws.kernel.boot.ReturnCode;
import com.ibm.ws.kernel.boot.archive.Archive;
import com.ibm.ws.kernel.boot.archive.ArchiveEntryConfig;
import com.ibm.ws.kernel.boot.archive.ArchiveFactory;
import com.ibm.ws.kernel.boot.archive.DirEntryConfig;
import com.ibm.ws.kernel.boot.archive.DirPattern.PatternStrategy;
import com.ibm.ws.kernel.boot.archive.FileEntryConfig;
import com.ibm.ws.kernel.boot.archive.FilteredDirEntryConfig;
import com.ibm.ws.kernel.boot.internal.BootstrapConstants;

/**
 * execute the dump command
 */
public class DumpProcessor implements ArchiveProcessor {
    private final String serverName;
    private final File dumpFile;
    private final BootstrapConfig bootProps;
    private final List<String> javaDumps;

    /**
     * Create a server dump.  The contents of the dump are specific files taken
     * from the configuration folder of a specified server (a subdirectory
     * of {@link BootstrapConfig#getUserRoot()}, plus specific files from the
     * server output folder (per {@link BootstrapConfig#getOutputFile(String)},
     * and including specific server dumps.
     *
     * A subset of the files obtained from the server configuration folder
     * are obscured.  See {@link FilteredDirEntryConfig}.
     *
     * @param serverName The name of the server which is to be dumped.
     * @param dumpFile The file into which to place the dump.
     * @param bootProps Properties which locate the user root and server output file. 
     * @param javaDumps Specific java dumps which are to be included in the dump.
     */
    public DumpProcessor(String serverName, File dumpFile, BootstrapConfig bootProps, List<String> javaDumps) {
        this.serverName = serverName;
        this.dumpFile = dumpFile;
        this.bootProps = bootProps;
        this.javaDumps = javaDumps;
    }

    public ReturnCode execute() {
        try ( Archive archive = ArchiveFactory.create(dumpFile) ) { 
            archive.addEntryConfigs( createDumpConfigs(serverName) );
            archive.create();

        } catch ( IOException e ) {
            System.out.println( BootstrapConstants.format("error.unableZipDir", e) );
            return ReturnCode.ERROR_SERVER_DUMP;
        }

        return ReturnCode.OK;
    }

    //

    private static final boolean DO_INCLUDE = true;
    private static final boolean DO_EXCLUDE = false;

    private static final boolean DO_OBSCURE = true;

    private static DirEntryConfig addDir(
            List<ArchiveEntryConfig> configs,
            File source, boolean doInclude, boolean doFilter) {

        PatternStrategy strategy =
            ( doInclude ? PatternStrategy.IncludePreference
                        : PatternStrategy.ExcludePreference );

        DirEntryConfig dirEntryConfig;
        if ( doFilter ) {
            dirEntryConfig = new FilteredDirEntryConfig(source, doInclude, strategy);
        } else {
            dirEntryConfig = new DirEntryConfig("", source, doInclude, strategy);            
        }

        configs.add(dirEntryConfig);

        return dirEntryConfig;
    }

    
    private List<ArchiveEntryConfig> createDumpConfigs(String serverName) {
        List<ArchiveEntryConfig> dumpConfigs = new ArrayList<ArchiveEntryConfig>();

        File configDir = new File(bootProps.getUserRoot(), "servers/" + serverName);

        // avoid any special characters in serverName when construct patterns
        String regexServerName = Pattern.quote(serverName);
        String regexServerPath = REGEX_SEP + regexServerName + REGEX_SEP;        

        // Add select contents from the server configuration folder.
        // Obscure those select contents.
        DirEntryConfig obscuedConfigs = addDir(dumpConfigs, configDir, DO_EXCLUDE, DO_OBSCURE);

        // Include XML and properties files.
        obscuedConfigs.includeExpressions(
                regexServerPath + ".*\\.xml",
                regexServerPath + ".*\\.properties" );
        // Include configuration drop-ins.
        obscuedConfigs.includeExpression(
                regexServerPath + "configDropins");
        // Exclude dump directory // TODO: This seems unnecessary, given the default
        // is to exclude.
        obscuedConfigs.excludeExpressions(
                regexServerPath + "dump_" + REGEX_TIMESTAMP,
                regexServerPath + "autopd");

        // Add the remainder of the server configuration filer.
        // The remainder is not obscured.
        DirEntryConfig unobscuredConfigs = addDir(dumpConfigs, configDir, DO_INCLUDE, !DO_OBSCURE);

        // Exclude user apps
        unobscuredConfigs.excludeExpressions(
                regexServerPath + "dropins",
                regexServerPath + "apps");

        // TODO: These next two cases won't exclude anything:
        //
        //       The patterns are specified with no wild-card matching,
        //       hence will only exclude exact matches.
        //
        //       The patterns also are not absolute, while matching
        //       is done on absolute paths.
        //
        //       Together, that means matches will never be found.

        // Exclude security-sensitive files under resources/security.
        unobscuredConfigs.excludeExpression(
                REGEX_SEP + "resources" + REGEX_SEP + "security");
        // As a best effort, try to avoid packaging security-sensitive .jks and .p12 files.
        unobscuredConfigs.excludeExpressions(
                "\\.jks$",
                "\\.p12$");

        // {server.config.dir} may be equal {server.output.dir}, so let's first exclude
        // Exclude dump directory
        unobscuredConfigs.excludeExpressions(
                regexServerPath + "dump_" + REGEX_TIMESTAMP,
                regexServerPath + "autopd");
        // Exclude workarea and logs directoriese
        unobscuredConfigs.excludeExpressions(
                regexServerPath + "logs",
                regexServerPath + "workarea");
        // Exclude server package and dump files.
        unobscuredConfigs.excludeExpressions(
                REGEX_SEP + regexServerName + "\\.(zip|pax)$",
                REGEX_SEP + regexServerName + "\\.dump-" + REGEX_TIMESTAMP + "\\.(zip|pax)$",
                regexServerPath + "core\\.[^\\\\/]+\\.dmp",
                regexServerPath + "heapdump\\.[^\\\\/]+\\.phd",
                regexServerPath + "java\\.[^\\\\/]+\\.hprof",
                regexServerPath + "javacore\\.[^\\\\/]+\\.txt",
                regexServerPath + "javadump\\.[^\\\\/]+\\.txt");

        // Add server output directory
        File outputDir = bootProps.getOutputFile(null);
        DirEntryConfig outputConfigs = addDir(dumpConfigs, outputDir, DO_EXCLUDE, !DO_OBSCURE);

        outputConfigs.includeExpressions(
            regexServerPath + "dump_" + REGEX_TIMESTAMP,
            regexServerPath + "autopd",
            regexServerPath + "logs",
            regexServerPath + "workarea");

        String regexWorkareaPath = regexServerPath + "workarea" + REGEX_SEP;
        
        outputConfigs.excludeExpressions(
            regexWorkareaPath + "\\.sLock$",
            regexWorkareaPath + "\\.sCommand$");

        // As the sub-osgi system will also create some locked files under .manager directory,
        // exclude all the org.eclipse.osgi/.manager/*.*
        outputConfigs.excludeExpression(
                regexWorkareaPath+
                ".*" + "org\\.eclipse\\.osgi"
                + REGEX_SEP + "\\.manager");

        // exclude application cache files
        outputConfigs.excludeExpression(
                regexWorkareaPath
                + "org\\.eclipse\\.osgi" + REGEX_SEP
                + "bundles" + REGEX_SEP
                + "\\d+" + REGEX_SEP
                + "data" + REGEX_SEP
                + ".*com\\.ibm\\.ws\\.app\\.manager_gen");

        // TODO: The annotation cache would be nice to include in the dump.
        //       Actually, why not include the application cache?  Is there
        //       a security issue?

        for ( String javaDump : javaDumps ) {
            dumpConfigs.add( new FileEntryConfig("", new File(javaDump)) );
        }

        return dumpConfigs;
    }
}
