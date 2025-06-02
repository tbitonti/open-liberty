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

import java.io.File;
import java.io.IOException;

import com.ibm.ws.kernel.boot.internal.FileUtils;

public class FileEntryConfig implements ArchiveEntryConfig {
    /**
     * Encapsulate the addition of a single file to an archive.
     * 
     * If the entry path is empty, or ends with a "/", append the source file
     * name to the entry path.
     * 
     * Otherwise, use the entry path as is.
     * 
     * Throw an exception if the source file does not exist, or is not a simple file.
     * 
     * @param entryPath the entry path in the archive, could be either a file path or a directory path.
     * @param source the source file.
     * 
     * @throws IllegalArgumentException if the source File object does not exist or is not a file.
     */
    public FileEntryConfig(String entryPath, File source) {
        entryPath = FileUtils.normalizeEntryPath(entryPath);

        if ( entryPath.equals("") || entryPath.endsWith("/") ) {
            this.entryPath = entryPath + source.getName();
        } else {
            this.entryPath = entryPath;
        }

        if ( !source.exists() ) {
            throw new IllegalArgumentException("The source does not exist.");
        }

        if ( !source.isFile() ) {
            throw new IllegalArgumentException("The source is not a file.");
        }

        this.source = source;
    }

    //
    
    private final String entryPath;
    
    @Override
    public String getEntryPath() {
        return this.entryPath;
    }

    //
    
    private final File source;
    
    @Override
    public File getSource() {
        return this.source;
    }

    //
    
    @Override
    public void configure(Archive archive) throws IOException {
        archive.addFileEntry(entryPath, source);
    }

}
