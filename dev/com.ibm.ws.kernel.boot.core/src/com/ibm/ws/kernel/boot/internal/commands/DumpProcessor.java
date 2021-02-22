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
            return ReturnCode.OK;

        } catch ( IOException e ) {
            System.out.println( BootstrapConstants.format("error.unableZipDir", e) );
            return ReturnCode.ERROR_SERVER_DUMP;
        }
    }

    private List<ArchiveEntryConfig> createDumpConfigs(String serverName) throws IOException {
        List<ArchiveEntryConfig> dumpConfigs = new ArrayList<ArchiveEntryConfig>();

        File configDir = new File(bootProps.getUserRoot(), "servers/" + serverName);

        // Prefixes used for files which are matched relative
        // to the server folder.
        //
        // TODO: This is approximate ... a server named 'servers' would cause
        //       problems, as would any subdirectory named 'servers'.

        String regexServerName = Pattern.quote(serverName);
        String regexServerPath = REGEX_SEP + regexServerName + REGEX_SEP;        

        // Add select contents from the server configuration folder.

        // Obscure some of the files ...
        //
        // Use exclude preference: Explicit excludes override
        // explicit includes.

        FilteredDirEntryConfig obscuredConfigs =
            DirEntryConfig.excludeObscuredDir(dumpConfigs, configDir);

        // Obscure XML and properties files.
        obscuredConfigs.includeExpressions(
            regexServerPath + ".*\\.xml",
            regexServerPath + ".*\\.properties" );
        // Obscure configuration drop-ins ...
        obscuredConfigs.includeExpression(
            regexServerPath + "configDropins");
        // Do *not* obscure the dump folders.
        obscuredConfigs.excludeExpressions(
            regexServerPath + "dump_" + REGEX_TIMESTAMP,
            regexServerPath + "autopd");

        // And do not obscure other files ...
        //
        // Use include preference ... include everything except
        // what is excluded.  Explicit includes override explicit
        // excludes.

        DirEntryConfig unobscuredConfigs =
            DirEntryConfig.includeDir(dumpConfigs, configDir);

        // Do not include any application files ...
        unobscuredConfigs.excludeExpressions(
            regexServerPath + "dropins",
            regexServerPath + "apps");

        // Exclude "resources/security".  Files here are sensitive.
        unobscuredConfigs.excludeExpression(
            REGEX_SEP + "resources" + REGEX_SEP + "security");

        // Also exclude ".jks" and ".p12" files.  These files are
        // also sensitive.
        unobscuredConfigs.excludeExpressions(
            "\\.jks$",
            "\\.p12$");

        // Exclude dump folders.
        unobscuredConfigs.excludeExpressions(
            regexServerPath + "dump_" + REGEX_TIMESTAMP,
            regexServerPath + "autopd");

        // Exclude "workarea" and "logs" directories.
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
        DirEntryConfig outputConfigs =
            DirEntryConfig.excludeDir(dumpConfigs, outputDir);

        // Finally, include the dump, logs, and workarea.
        outputConfigs.includeExpressions(
            regexServerPath + "dump_" + REGEX_TIMESTAMP,
            regexServerPath + "autopd",
            regexServerPath + "logs",
            regexServerPath + "workarea");

        String regexWorkareaPath = regexServerPath + "workarea" + REGEX_SEP;

        // But, exclude and lock files.
        outputConfigs.excludeExpressions(
            regexWorkareaPath + "\\.sLock$",
            regexWorkareaPath + "\\.sCommand$");

        // As the sub-osgi system will also create some locked files
        // under ".manager", exclude "org.eclipse.osgi/.manager/*.*"
        outputConfigs.excludeExpression(
            regexWorkareaPath +
            ".*" + "org\\.eclipse\\.osgi" + REGEX_SEP +
            "\\.manager");

        // Exclude application cache files
        outputConfigs.excludeExpression(
            regexWorkareaPath +
            "org\\.eclipse\\.osgi" + REGEX_SEP +
            "bundles" + REGEX_SEP + "\\d +" + REGEX_SEP + "data" + REGEX_SEP +
            ".*com\\.ibm\\.ws\\.app\\.manager_gen");

        // Add explicit java dumps.
        for ( String javaDump : javaDumps ) {
            dumpConfigs.add( new FileEntryConfig("", new File(javaDump)) );
        }

        return dumpConfigs;
    }
}
