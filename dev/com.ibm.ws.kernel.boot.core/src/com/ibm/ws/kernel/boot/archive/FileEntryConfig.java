/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
import java.io.IOException;

import com.ibm.ws.kernel.boot.internal.FileUtils;

public class FileEntryConfig implements ArchiveEntryConfig {
    /**
     * File entry configuration.  Used to add a single file to an archive.
     * 
     * See {@link #configure(Archive)}.
     * 
     * The meaning of the parameters is per {@link Archive#addFileEntry(String, File)}.
     * 
     * @param entryPath The path to the target file.
     * @param source The target file.  This file must exist,
     *     and must be a simple file.
     */
    public FileEntryConfig(String entryPath, File source) {
        if ( !source.exists() ) {
            throw new IllegalArgumentException("Target " + source.getAbsolutePath() + " does not exist.");
        } else if ( !source.isFile() ) {
            throw new IllegalArgumentException("Target " + source.getAbsolutePath() + " is not a file.");
        }

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
     */
    @Override
    public void configure(Archive archive) throws IOException {
        archive.addFileEntry(entryPath, source);
    }
}
