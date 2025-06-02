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

/**
 * Encapsulate adding entries into an archive.
 * 
 * Validation occurs when creating a new configuration.
 */
public interface ArchiveEntryConfig {

    /**
     * Answer the path of the entry which is to be added.
     * 
     * @return The path of the entry which is to be added.
     */
    String getEntryPath();

    /**
     * The source file which is to be added.
     * 
     * Contributes a file mode, and, if not a directory, the entries contents.
     * 
     * @return A file which is to be added. 
     */
    File getSource();

    /**
     * Process this entry configuration into the archive.
     * 
     * Use either {@link com.ibm.ws.kernel.boot.archive.Archive.addFileEntry(String, File)}
     * {@link com.ibm.ws.kernel.boot.archive.Archive.addDirEntry(String, File, List<String>)}
     * to process entries into the archive.
     * 
     * @param archive The archive which is to receive entries.
     * 
     * @throws IOException Thrown if the entries cannot be processed.
     */
    void configure(Archive archive) throws IOException;

}
