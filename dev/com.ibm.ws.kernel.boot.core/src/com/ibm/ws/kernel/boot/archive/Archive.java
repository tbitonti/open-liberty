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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Type use to represent archives and their contents.
 * 
 * Used currently for server dump and server package commands.
 * 
 * Expected usage is to add entries to the archive, then to invoke
 * {@link #create()} to create the archive on disk.
 * 
 * The archive type is a subtype of {@link Closeable}.  After being
 * created, an archive must be closed. 
 */
public interface Archive extends Closeable {
    int DEFAULT_BUFFER_SIZE = 4096;

    /**
     * Add an entry to the archive.
     * 
     * @param entryConfig The configuration of the entry which
     *     is to be added.
     */
    void addEntryConfig(ArchiveEntryConfig entryConfig);

    /**
     * Add entries to the archive.
     * 
     * @param entryConfigs The configurations of the entries which
     *     are to be added.
     */
    void addEntryConfigs(List<ArchiveEntryConfig> entryConfigs);

    /**
     * Add a file entry to the archive.
     * 
     * @param entryPath The name to use for the new archive entry.
     * @param source The file containing the contents of the entry
     *     which is to be added.  The source file must exist and must
     *     be a simple file.
     *
     * @throws IOException Thrown if an error occurs while adding the
     *     file entry to the archive.
     */
    void addFileEntry(String entryPath, File source) throws IOException;

    /**
     * Add elements of a directory to the archive.
     * 
     * @param entryPath the relative target path of entries which
     *     are to be added.
     * @param source The folder which contains the entries which
     *     are to be added.  The source folder must exist and must
     *     be a directory.
     * @param dirContent The relative paths of entries which are to be
     *     added.
     *
     * @throws IOException Thrown if an error occurs while adding the
     *     directory elements to the archive.
     */
    void addDirEntry(String entryPath, File source, List<String> dirContent) throws IOException;
    
    /**
     * Assemble an archive using the entry configuration which have
     * been added.
     * 
     * A new new archive will be created, if necessary.
     * 
     * If the archive already exists, any entry configurations which
     * would add duplicate entries are ignored.  That is, existing
     * archive entries take precedence over any which might be added.
     * 
     * @return The new archive file.
     *
     * @throws IOException If an error occurred while creating the archive.
     */
    File create() throws IOException;
}
