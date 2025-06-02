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
package com.ibm.ws.kernel.boot.archive.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.ws.kernel.boot.archive.Archive;
import com.ibm.ws.kernel.boot.archive.ArchiveEntryConfig;

public abstract class AbstractArchive implements Archive {

    /**
     * Create a new archive which is intended to create a target
     * archive file.
     * 
     * Subclass initializers may allocate resources immediately:
     * {@link #close()} must be invoked even if {@link #create()}
     * is never invoked.
     * 
     * @param archiveFile A file which is to be created as an archive.
     * 
     * @throws IOException Thrown if the target file cannot be used.
     */
    protected AbstractArchive(File archiveFile) throws IOException {
        this.archiveFile = archiveFile;
        this.archiveAbsolutePath = archiveFile.getAbsolutePath();
        this.archiveCanonicalFile = archiveFile.getCanonicalFile();

        this.entryConfigs = new ArrayList<ArchiveEntryConfig>();
    }

    //
    
    private final File archiveFile;
    private final String archiveAbsolutePath; // Cache this!  Recomputing it is expensive.
    private final File archiveCanonicalFile; // Cache this!  Recomputing it is expensive.

    public File getArchiveFile() {
        return archiveFile;
    }

    public String getArchiveAbsolutePath() {
        return archiveAbsolutePath;
    }
    public File getArchiveCanonicalFile() {
        return archiveCanonicalFile;
    }
    
    //

    protected final List<ArchiveEntryConfig> entryConfigs;

    /**
     * Default implementation: Add an entry configuration.
     * 
     * This implementation simply stores the configuration for later processing.
     * 
     * No validation is done on the entry configuration.
     * 
     * @param entryConfig An entry configuration which is added to the archive.
     */
    @Override
    public void addEntryConfig(ArchiveEntryConfig entryConfig) {
        entryConfigs.add(entryConfig);
    }

    /**
     * Default implementation: Add entry configurations.
     * 
     * This implementation simply stores the configurations for later processing.
     * 
     * No validation is done on the entry configurations.
     * 
     * @param newEntryConfig Entry configuration which are added to the archive.
     */    
    @Override
    public void addEntryConfigs(List<ArchiveEntryConfig> newEntryConfigs) {
        this.entryConfigs.addAll(newEntryConfigs);
    }

    //
    
    /**
     * Main API: Create the target archive.
     * 
     * This default implementation processes all entry configurations which were previously
     * stored using {@Link #addEntryConfig(ArchiveEntryConfig)} and {@link #addEntryConfigs(List)}.
     * 
     * {@link #addFileEntry(String, File)} and {@link #addDirEntry(String, File, List)} are
     * public and may be invoked before {@link #create()}!
     * 
     * @return The created target file.
     * 
     * @throws IOException If an error occurs while creating the archive.
     */
    @Override
    public File create() throws IOException {
        for ( ArchiveEntryConfig config : entryConfigs ) {
            config.configure(this);
        }

        return getArchiveFile();
    }

    /**
     * Main API: Close any resources used when creating the target archive.
     * 
     * Usually, this means closing an archive input stream.
     * 
     * @throws IOException Thrown if an error occurs while closing the archive.
     *     This usually means a failure to write the full archive file.
     */
    @Override
    public abstract void close() throws IOException;
    
    //
    
    /**
     * Directly add a single entry to the archive.  The entry can be a directory entry or
     * a simple file entry.
     * 
     * This bypasses {@link #addEntryConfig(ArchiveEntryConfig)} and {@link #addEntryConfigs(List)}.
     * 
     * @param entryPath The path to add to the archive.  This path may not be empty!
     * @Param source The source file which is to be added to the archive.
     * 
     * @throws IOException Thrown if an error occurred while adding the single
     *     file entry.
     */
    @Override
    public abstract void addFileEntry(String entryPath, File source) throws IOException;
    
    /**
     * Directly add a directory entry and child entries to the archive.
     * 
     * Do not add the directory entry if the directory path is empty.
     * 
     * This bypasses {@link #addEntryConfig(ArchiveEntryConfig)} and {@link #addEntryConfigs(List)}.
     * 
     * @param dirEntryPath The path of the directory which is to be added.  This path
     *     may be empty.
     * @param dirSource The directory which is to be added.
     * @param childPaths Paths of children of the directory which are to be added.
     * 
     * @throws IOException Thrown if an error occurred while adding the directory
     *     entry or one of the directory elements.
     */    
    @Override    
    public abstract void addDirEntry(String dirEntryPath, File dirSource, List<String> childPaths) throws IOException;
}
