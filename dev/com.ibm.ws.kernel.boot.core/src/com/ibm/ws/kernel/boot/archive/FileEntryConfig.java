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
package com.ibm.ws.kernel.boot.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.ibm.ws.kernel.boot.internal.BootstrapConstants;
import com.ibm.ws.kernel.boot.internal.FileUtils;

/**
 * Configuration used to add a single simple file to an archive.
 * 
 * The configuration has two parameters: The path which is to be
 * used by the archive entry which is created, and a file which
 * contains the data which is to be added for the archive entry.
 * 
 * The source file must exist and must be a simple file.
 *
 * The entry path parameter is adjusted to be a valid path for
 * an archive entry: 
 * 
 * A null entry path is converted to an empty string, which
 * means that the source name is used as the entry path.
 *
 * Backward slashes ('\') are converted to forward slashes ('/'),
 * and any leading slash is removed.
 * 
 * If the entry path is empty, the name of the source file is
 * used as the entry path.
 *
 * If the entry path ends with a slash, the name of the source
 * file is appended to the entry path.
 *
 * Otherwise, the entry path is used as-is, and the name of the
 * source file is ignored.
 */
public class FileEntryConfig implements ArchiveEntryConfig {
    /**
     * Verify that a source file is valid.  That is, the source file
     * must exist and must be a simple file.
     *
     * @param source The source file which is to be tested.
     *
     * @throws IOException Thrown if the file does not exist, or is not
     *     a simple file.  Thrown as a {@link FileNotFoundException} if
     *     the file does not exist.
     */
    private static void verifySource(File source) throws IOException {
        if ( !source.exists() ) {
            throw new FileNotFoundException( BootstrapConstants.format("error.missing.loose.file", source.getAbsolutePath()) );            
        } else if ( !source.isFile() ) {
            throw new IOException( BootstrapConstants.format("error.nonsimple.loose.file", source.getAbsolutePath()) );
        }
    }
        
    /**
     * File entry configuration.  Used to add a single file to an archive.
     * 
     * Verification of the source file is performed when the configuration
     * is created.  That causes exceptions early, when configurations are
     * created.  The alternative is to perform verification later, when the
     * configuration is used to add an entry to an archive.
     * 
     * See {@link #configure(Archive)}.
     * 
     * The meaning of the parameters is per {@link Archive#addFileEntry(String, File)}.
     * 
     * @param entryPath The path to the target file.
     * @param source The target file.  This file must exist, and must be
     *     a simple file.
     *
     * @throws IOException Thrown if the source location does not exist or is not
     *     a simple file.
     */
    public FileEntryConfig(String entryPath, File source) throws IOException {
        verifySource(source);

        entryPath = FileUtils.normalizeEntryPath(entryPath);

        // The first case occurs from:
        // com.ibm.ws.kernel.boot.internal.commands.DumpProcessor
        //     .createDumpConfigs(String).
        //
        // The other two cases seem to occur from:
        // com.ibm.ws.kernel.boot.internal.commands.ProcessorUtils
        //     .createLooseExpandedArchiveEntryConfigs(LooseConfig, File, BootstrapConfig, String, boolean)
        // com.ibm.ws.kernel.boot.internal.commands.ProcessorUtils
        //     .processLooseConfig(String, LooseConfig, BootstrapConfig)

        if ( entryPath.isEmpty() ) {
            this.entryPath = source.getName();
        } else if ( entryPath.charAt(entryPath.length() - 1) == '/' ) {
            this.entryPath = entryPath + source.getName();
        } else {
            this.entryPath = entryPath;
        }

        this.source = source;
    }

    //

    private final String entryPath;

    @Override
    public String getEntryPath() {
        return this.entryPath;
    }

    private final File source;

    @Override
    public File getSource() {
        return this.source;
    }

    //

    /**
     * Main API: Add the file entry to the archive.
     * 
     * See {@link Archive#addFileEntry(String, File)}.
     *
     * @param archive The archive which will receive the file entry.
     * 
     * @throws IOException Thrown if the target source does not exist,
     *     is not a simple file, or could not be added.
     */
    @Override
    public void configure(Archive archive) throws IOException {
        verifySource(source);

        // Note: The entry path may have been adjusted
        //       when this file configuration was created.

        archive.addFileEntry(entryPath, source); // throws IOException
    }
}
