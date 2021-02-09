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
import java.io.IOException;

/**
 * Type used to add entries to archives.
 */
public interface ArchiveEntryConfig {
    /**
     * The path of the entry which is to be added to an archive.
     * 
     * @return The path of the entry which is to be added.
     */
    String getEntryPath();

    /**
     * A file which contains the contents of the entry which
     * is to be added to an archive.
     * 
     * When adding a directory, the root folder which contains
     * files which are to be added.
     * 
     * @return A file which contains the contents of the
     *     entry which are to be added.
     */
    File getSource();

    /**
     * Add an entry to an archive.
     * 
     * The precise action depends on the type of entry which is
     * being added.  Concrete subclass {@link FileEntryConfig}
     * adds a single simple entry to the archive.  Concrete
     * subclass {@link DirEntryConfig} adds entries of a directory
     * to the archive.
     * 
     * @param archive The archive which is to receive new entries.
     *
     * @throws IOException Thrown if an error occurred while adding
     *     entries.
     */
    void configure(Archive archive) throws IOException;
}
