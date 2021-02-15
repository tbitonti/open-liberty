/*******************************************************************************
 * Copyright (c) 2013, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.boot.internal.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ibm.ws.kernel.boot.BootstrapConfig;
import com.ibm.ws.kernel.boot.Debug;
import com.ibm.ws.kernel.boot.EmbeddedServerImpl;
import com.ibm.ws.kernel.boot.LaunchArguments;
import com.ibm.ws.kernel.boot.ReturnCode;
import com.ibm.ws.kernel.boot.internal.BootstrapConstants;
import com.ibm.ws.kernel.boot.internal.FileUtils;
import com.ibm.ws.kernel.boot.internal.ServerLock;
import com.ibm.ws.kernel.boot.internal.commands.ArchiveProcessor.Pair;
import com.ibm.ws.kernel.boot.internal.commands.PackageProcessor.PackageOption;
import com.ibm.ws.kernel.boot.logging.TextFileOutputStreamFactory;
import com.ibm.wsspi.kernel.embeddable.Server;

/**
 * Command used to create a server package.
 * 
 * The expected usage is to create a new package command, then perform packaging
 * with one of the packaging APIs:
 * 
 * See {@link #PackageCommand(BootstrapConfig, LaunchArguments)},
 * 
 * See also {@link #doPackage()}, {@link #doPackageRuntimeOnly()},
 * and {@link #packageServerRuntime(File, boolean)}.
 * 
 * When packaging the runtime, no particular server instance is being
 * used.  Use archive name "wlp", and place the packaged server in the
 * common output root.
 *
 * Otherwise, use the server name as the archive name, and place the
 * packaged server in the server folder.
 * 
 * Packaging of the server runtime does not care if any server is
 * running.  Packaging of a particular server requires that that
 * server to not be running.
 */
public class PackageCommand {
    private static final String JAR_EXTENSION = ".jar";

    private final static List<String> SUPPORTED_EXTENSIONS = new ArrayList<>();

    static {
        SUPPORTED_EXTENSIONS.add(".zip");
        SUPPORTED_EXTENSIONS.add(".jar");
        SUPPORTED_EXTENSIONS.add(".pax");
        SUPPORTED_EXTENSIONS.add(".tar");
        SUPPORTED_EXTENSIONS.add(".tar.gz");
    }

    /**
     * Tell if a target file name which was supplied as
     * the name of the package archive has one of the supported
     * extensions.  If the extension is supported, it will
     * be used, and it will control the archive format which
     * is used.
     *
     * This test is not case sensitive.
     *
     * @param fileName The file name which is to be tested.
     *
     * @return True or false telling if the file has one of the
     *     supported extensions.
     */
    private static boolean isSupportedExtension(String fileName) {
        fileName = fileName.toLowerCase();
        for (String extension : SUPPORTED_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    //
    
    private static final void println(String msgKey, Object... parms) {
        System.out.println( BootstrapConstants.format(msgKey, parms) );
    }

    private static final void println(String msgKey) {
        System.out.println( BootstrapConstants.getMessage(msgKey) );
    }    
    
    //

    private final BootstrapConfig bootProps;

    private final boolean isClient;
    private final String serverName;

    // private final String serverConfigDir;
    private final String serverOutputDir; // usual location of the packaged server 
    private final File wlpOutputRoot; // used when packaging just the runtime

    private final String osRequest;
    private final String includeOption;
    private final String archiveOption;
    private final String rootOption;

    public PackageCommand(BootstrapConfig bootProps, LaunchArguments launchArgs) {
        this.bootProps = bootProps;

        this.serverName = bootProps.getProcessName();
        this.isClient = bootProps.getProcessType() == BootstrapConstants.LOC_PROCESS_TYPE_CLIENT;

        // Use the system property bootstrap config set:
        // all conversion/endings will be the same.

        // this.serverConfigDir = bootProps.get(BootstrapConstants.LOC_PROPERTY_SRVCFG_DIR);
        this.serverOutputDir = bootProps.get(BootstrapConstants.LOC_PROPERTY_SRVOUT_DIR);
        this.wlpOutputRoot = bootProps.getCommonOutputRoot();

        this.osRequest = launchArgs.getOption(BootstrapConstants.CLI_OS_REQUEST);
        this.includeOption = launchArgs.getOption(BootstrapConstants.CLI_PACKAGE_INCLUDE_VALUE);
        this.archiveOption = launchArgs.getOption(BootstrapConstants.CLI_ARG_ARCHIVE_TARGET);
        this.rootOption = launchArgs.getOption(BootstrapConstants.CLI_ROOT_PACKAGE_NAME);
    }

    /**
     * Main API: Package the server:
     * 
     * <ul>
     * <li>Emit initial processing messages.</li>
     * <li>Generate a descriptive file to include with the server.</li>
     * <li>Create the actual server package.</li>
     * <li>Cleanup the descriptive file, which is stored in the package.</li>
     * <li>Emit final processing messages.</li>
     * </ul>
     *
     * The server must not be running.  Fail with {@link ReturnCode#SERVER_ACTIVE_STATUS}
     * if the server is running.
     * 
     * An output archive is created.  Fail with {@link ReturnCode#ERROR_SERVER_PACKAGE}
     * if the packaging archive cannot be created.
     * 
     * Answer {@link ReturnCode#OK} if the packaging was successful.  Otherwise,
     * fail with a variety of failure codes, per {@link ReturnCode}.
     * 
     * @return The packaging return code.
     */
    public ReturnCode doPackage() {
        Date date = new Date();

        if (isClient) {
            println("info.clientPackaging", serverName);
        } else {
            println("info.serverPackaging", serverName);
        }

        // Packaging is not permitted while the server is running.
        //
        // Creation of the server lock ensures that the server
        // exists and is writable.
        //
        // The running test briefly obtains then releases the server.
        // This will fail if the server is running, as the running
        // server will have the lock.
        //
        // Note that the server lock is obtained for just a monent:
        // there is no guard against the server starting after this
        // lock test and the actual packaging step.
        //
        // Perhaps a try/finally with 'serverLock.obtainServerLock()'
        // 'serverLock.releaseServerLock()' should be used. 

        ServerLock serverLock = ServerLock.createServerLock(bootProps);
        if (serverLock.testServerRunning()) {
            if (isClient) {
                println("info.clientIsRunning", serverName);
                println("info.clientPackageUnreachable", serverName);
            } else {
                println("info.serverIsRunning", serverName);
                println("info.serverPackageUnreachable", serverName);
            }
            return ReturnCode.SERVER_ACTIVE_STATUS; // Failure
        }

        // The output archive must be valid.
        // TODO: What happens if this archive exists?
        File archive = getArchive(serverName, new File(serverOutputDir));
        if (archive == null) {
            println("error.package.extension");
            return ReturnCode.ERROR_SERVER_PACKAGE;
        }

        ReturnCode packageRc;

        // Temporarily create a messaging output file ...

        SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd_HH.mm.ss");
        String packageTimestamp = sdf.format(date);
        File packageInfoFile = new File(serverOutputDir,
            BootstrapConstants.SERVER_PACKAGE_INFO_FILE_PREFIX + packageTimestamp + ".txt");

        try {
            if ( !generatePackageInfo(packageInfoFile, date) ) {
                if (isClient) {            
                    println("info.clientPackageException", serverName);
                } else {
                    println("info.serverPackageException", serverName);                
                }
                return ReturnCode.ERROR_SERVER_PACKAGE;
            }

            // The meat of processing ...
            // See, in particular, PackageProcessor.execute.
            packageRc = packageServerRuntime(archive, false);

        } finally {
            // Strange to use a recursive operation: The package information
            // is a simple file.
            FileUtils.recursiveClean(packageInfoFile);
        }

        if (isClient) {
            if (packageRc == ReturnCode.OK) {
                println("info.clientPackageComplete", serverName, archive.getAbsolutePath());
            } else {
                println("info.clientPackageException", serverName);
            }
        } else {
            if (packageRc == ReturnCode.OK) {
                println("info.serverPackageComplete", serverName, archive.getAbsolutePath());
            } else {
                println("info.serverPackageException", serverName);
            }
        }

        return packageRc;
    }

    /**
     * Answer the packaging archive file.
     * 
     * This defaults to the server name plus an OS dependent
     * archive extension.
     * 
     * If an archive name was supplied as a packaging option,
     * and the archive includes a supported extension, make sure
     * the options are consistent.  Otherwise, add the OS
     * dependent extension to the archive name.
     * 
     * If a runnable archive was requested, and a supported
     * extension is present on the option supplied archive name,
     * the supplied archive name must have the "jar" extension.
     * 
     * Answer null if a runnable archive was requested and the
     * supplied archive has a supported extension other than "jar".
     * That will fail packaging.
     *
     * 
     * @param useServerName The default name of the archive file.
     * @param useOutputDir The directory in which to place the
     *     archive file.  Unused if an absolute server name was
     *     specified.
     *
     * @return The packaging archive file.  Null if the option
     *     supplied archive name cannot be used.
     */
    private File getArchive(String useServerName, File useOutputDir) {
        String archiveName;

        if ( (archiveOption == null) || archiveOption.isEmpty() ) {
            archiveName = useServerName + "." + getDefaultPackageExtension();

        } else {
            int index = archiveOption.lastIndexOf(".");
            if ( (index > 0) && isSupportedExtension(archiveOption) ) {
                // --include=runnable with non-.jar file extension
                // supplied to --archive is not allowed
                if ( PackageProcessor.IncludeOption.RUNNABLE.matches(includeOption) &&
                     !archiveOption.toLowerCase().endsWith(JAR_EXTENSION) ) {
                    return null; // Answering null causes packaging to fail.
                } else {
                    archiveName = archiveOption;
                }

            } else {
                // if no file extension (or non-supported) included on filename,
                // default to .zip, .jar, or .pax
                archiveName = archiveOption + '.' + getDefaultPackageExtension();
            }
        }

        File archive = new File(archiveName);
        if ( !archive.isAbsolute() ) {
            archive = new File(useOutputDir, archiveName);
        }
        return archive;
    }

    /**
     * Package the server, including only the server runtime.
     * in the package.
     * 
     * @return ReturnCode The processing return code.
     *     {@link ReturnCode#OK} if packaging was successful.
     *     Otherwise, a positive code indicating a particular
     *     failure.
     */
    public ReturnCode doPackageRuntimeOnly() {
        println("info.runtimePackaging");

        File archive = getArchive("wlp", wlpOutputRoot);
        if ( archive == null ) {
            println("error.package.extension");
            return ReturnCode.ERROR_SERVER_PACKAGE;
        }

        ReturnCode packageRc = packageServerRuntime(archive, true);

        if (packageRc == ReturnCode.OK) {
            println("info.runtimePackageComplete", archive.getAbsolutePath());
        } else {
            // TODO: Should the return code be displayed?
            println("info.runtimePackageException");
        }

        return packageRc;
    }

    /*
     * Return true for include values of:
     * include=minify
     * include=minify,runnable
     *
     * Otherwise return false.
     */
    private boolean includeMinifyorMinifyRunnable(String val) {
        return PackageProcessor.IncludeOption.MINIFY.matches(val);
    }

    /**
     * Package the server, conditionally including just the server
     * runtime, or including the server runtime and applications.
     *
     * @param packageFile The packaging file.
     * @param runtimeOnly Control parameter.  Tells if just the server
     *     runtime is to be placed in the package.
     *
     * @return ReturnCode The processing return code.
     *     {@link ReturnCode#OK} if packaging was successful.
     *     Otherwise, a positive code indicating a particular
     *     failure.
     */
    public ReturnCode packageServerRuntime(File packageFile, boolean runtimeOnly) {
        Set<String> libertyFiles;
        if (includeMinifyorMinifyRunnable(includeOption)) {
            libertyFiles = getMinifyPathsForPackage();
            if ( libertyFiles == null ) {
                return ReturnCode.ERROR_SERVER_PACKAGE;
            }
            println("info.serverPackagingBuildingArchive", serverName);
        } else {
            if (osRequest != null) {
                println("error.os.without.include");
                return ReturnCode.ERROR_SERVER_PACKAGE;
            }
            libertyFiles = null;
        }

        List<Pair<PackageOption, String>> options;
        if ( includeOption != null ) {
            options = new ArrayList<Pair<PackageOption, String>>(1);
            options.add(new Pair<PackageOption, String>(PackageOption.INCLUDE, includeOption));
        } else {
            options = null;
        }

        PackageProcessor processor =
            new PackageProcessor(serverName, packageFile, bootProps, options, libertyFiles);

        if ( rootOption != null ) {
            if ( processor.hasProductExtentions() && rootOption.trim().equalsIgnoreCase("") ) {
                println("warning.package.root.invalid", serverName);
            } else {
                if ( !PackageProcessor.IncludeOption.RUNNABLE.matches(includeOption) ) {
                    processor.setArchivePrefix(rootOption);
                } else {
                    // --server-root and --include=runnable are not valid together;
                    // ignore --server-root.
                    println("warning.package.root.invalid.runnable", serverName);
                }
            }
        }

        // Finally, do the actual packaging.
        return processor.execute(runtimeOnly);
    }

    /**
     * Write a brief descriptive message to a text file which is
     * included in the server package.
     * 
     * @param infoFile The file which is to receive the descriptive message.
     * @param serverName The name of the server which is being packaged.
     * @param date When the packaging is being performed.
     * 
     * @return True or false, telling if the file was successfully
     *     written.
     */
    private boolean generatePackageInfo(File infoFile, Date date) {
        try ( FileOutputStream packageInfoStream = TextFileOutputStreamFactory.createOutputStream(infoFile) ) {
            try ( BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(packageInfoStream)) ) {
                writer.append( getInfo(date) ); 
            }
        } catch ( IOException e ) {
            Debug.printStackTrace(e);
            println("error.failed.package.info", serverName, infoFile.getAbsolutePath(), e);
            return false;
        }
        return true;
    }

    /**
     * Generate a descriptive message for the server package.
     * 
     * The message gives the server name and the server include option, if
     * one was specified, and with the OS request, if that was specified.
     * 
     * An OS request is only possible when an include option is specified.
     *
     * @param date When the packaging is being performed.
     *
     * @return The descriptive message.
     */
    private String getInfo(Date date) {
        StringBuilder builder = new StringBuilder();

        builder.append("Package server " + serverName);
        if ( includeOption != null ) {
            builder.append(" with include option \"" + includeOption + "\"");
            if ( osRequest != null ) {
                builder.append(" and os " + osRequest);
            }
        } else {
            builder.append(" all");
        }
        builder.append(" at " + date + ".\n");
        
        return builder.toString();
    }
    
    /**
     * Answer the default extension for the packaging archive based
     * on the the current operating system and on include options.
     *
     * This extension controls the format of the packaging archive.
     *
     * On z/OS, always answer "pax" on z/OS.
     * 
     * Otherwise, answer "jar" or "zip" depending on whether a runnable
     * archive was requested.
     *
     * @return The default extension for the packaging archive.
     */
    private String getDefaultPackageExtension() {
        if ( "z/OS".equalsIgnoreCase( bootProps.get(BootstrapConstants.BOOTPROP_OS_NAME) ) ) {
            return "pax"; // Always "pax" on Z/OS
        }
        if ( PackageProcessor.IncludeOption.RUNNABLE.matches(includeOption) ) {
            return "jar"; // "jar" if a runnable archive is requested
        } else {
            return "zip"; // "zip" as the default
        }
    }

    /**
     * Obtain the paths which must be put into the minified server.
     * 
     * Partially start the server, then ask it for the necessary
     * paths, then stop the server.
     * 
     * The server runs in a remote process: Perform the start and
     * stop operations as futures.
     *
     * @return The paths necessary for the minified server.  Null
     *     if the paths could not be retrieved.
     */
    private Set<String> getMinifyPathsForPackage() {
        // This may take a few seconds ... emit a message ...
        println("info.serverPackagingCollectingInformation", serverName);

        EmbeddedServerImpl server = new EmbeddedServerImpl(serverName,
                bootProps.getUserRoot(),
                new File(serverOutputDir).getParentFile(),
                null, null, null, null,
                BootstrapConstants.LOC_AREA_NAME_WORKING_UTILS);

        // Add in the 'do not pass go' property.
        // This prevents the server from raising the start level,
        // preventing features from starting but starting the kernel
        // enough to be queried for configured features.

        Map<String, String> minifyServerProps = new HashMap<String, String>(1);
        minifyServerProps.put(BootstrapConstants.REQUEST_SERVER_CONTENT_PROPERTY, "1.0.0");

        // TODO: Allowing 10 minutes is questionable.
        //       The server is only partially starting, so the start
        //       should be relatively quick, much less than 10 minutes,
        //       even on a very slow system.

        if ( !handleServerFuture( server.start(minifyServerProps), 10L, TimeUnit.MINUTES) ) {
            println("error.minify.unable.to.start.server", serverName);
            return null;
        }

        // A FileNotFoundException means requested content is missing.

        // An IOException represents a more generic failure.

        // A null result might never occur, but its hard to tell
        // at this point, so be safe and check.

        Set<String> contentPaths;
        try {
            contentPaths = server.getServerContent(osRequest);
        } catch ( FileNotFoundException fnf ) {
            Debug.printStackTrace(fnf);
            String osMsg = osRequest;
            if (osMsg == null) {
                osMsg = "any";
            }
            println("unable.to.package.missing.file", serverName, osMsg, fnf.getMessage());
            return null;
        } catch ( IOException e ) {
            Debug.printStackTrace(e);
            println("error.unable.to.package", serverName, e);
            return null;
        }
        if ( contentPaths == null ) {
            println("error.minify.unable.to.determine.features", serverName);
            return null;
        }

        // TODO: A stopping message might be useful.

        if ( !handleServerFuture(server.stop(), 60L, TimeUnit.SECONDS) ) {
            println("error.minify.unable.to.stop.server", serverName);
            return null;
        }

        // TODO: A stopped message might be useful.

        return contentPaths;
    }
    
    private boolean handleServerFuture(
        Future<Server.Result> serverFuture,
        long timeUnits, TimeUnit timeUnit) {

        Server.Result rc;
        try {
            rc = serverFuture.get(timeUnits, timeUnit);        
        } catch ( TimeoutException | InterruptedException | ExecutionException e ) {
            Debug.printStackTrace(e);
            return false;
        }

        if ( rc == null ) {
            return false;
        } else if ( !rc.successful() ) {
            Debug.println(rc.getReturnCode());
            Exception e = rc.getException();
            if ( e != null ) {
                Debug.printStackTrace(e);
            }
            return false;
        } else {
            return true;
        }
    }
}
