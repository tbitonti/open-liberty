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
package com.ibm.ws.kernel.boot.archive;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface use for creating archives.
 * 
 * The API has three major steps:
 * 
 * A target location is specified when a new archive instance is created.
 * Currently, all implementations specified a location as a java file.
 * 
 * The intended contents of the archive are specified by adding archive entry
 * configurations {@link ArchiveEntryConfig} to the archive.  The order of the
 * archive entry configurations specifies the order in which entries are added
 * to the archive.
 *
 * The archive file is created by invoking {@link #create()}, which creates
 * the actual archive file and places entries into the archive according to the
 * entry configurations which were previously added.
 * 
 * {@link #create()} uses {@link #addFileEntry(String, File)} and {@ #addDirEntry(String, File, List)}
 * to process particular entries.
 */
public interface Archive extends Closeable {
    /**
     * Add a single archive entry configuration to the archive.
     * 
     * @param entryConfig An archive entry configuration.
     */
    void addEntryConfig(ArchiveEntryConfig entryConfig);

    /**
     * Add multiple archive entry configurations to the archive.
     * 
     * The order of the configurations specifies the order in which
     * the entries are added to the archive.
     * 
     * @param entryConfigList Archive entry configurations.
     */
    void addEntryConfigs(List<ArchiveEntryConfig> entryConfigList);

    //
    
    /**
     * Assemble (package) the archive.
     *
     * Process the entry configurations, adding files and directories.
     * 
     * Processing proceeds through {@link #addFileEntry(String, File)} and
     * {@link #addDirEntry(String, File, List)}.
     * 
     * While {@link #create()} is the main API used to assemble the archive,
     * the target archive is set when creating the archive instance, and,
     * {@link #addFileEntry(String, File)} and {@link #addDirEntry(String, File, List)}
     * may be invoked directly.  Further, completion of the archive is not
     * guaranteed until the archive instance is closed.
     * 
     * @return The archive file which was created.
     * 
     * @throws IOException Thrown if the archive could not be created.
     */
    File create() throws IOException;    

    /**
     * Main API: Close any resources used when creating the target archive.
     *
     * Usually, this means closing an archive input stream.
     *
     * @throws IOException Thrown if an error occurs while closing the archive.
     *     This usually means a failure to write the full archive file.
     */
    @Override
    void close() throws IOException;
    
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
    void addFileEntry(String entryPath, File source) throws IOException;

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
    void addDirEntry(String entryPath, File source, List<String> dirContent) throws IOException;
}
