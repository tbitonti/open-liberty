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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import com.ibm.ws.kernel.boot.archive.UnixModeHelper;
import com.ibm.ws.kernel.boot.internal.BootstrapConstants;
import com.ibm.ws.kernel.boot.internal.FileUtils;

public class ZipArchive extends AbstractArchive {

    private static final UnixModeHelper helper;

    static {
        UnixModeHelper useHelper;
        try {
            // TODO: Is this test still needed?  The class was added in java 7.
            Class.forName("java.nio.file.attribute.PosixFilePermission");
            useHelper = new UnixModeHelperImpl();
        } catch ( ClassNotFoundException e ) {
            useHelper = null;
            // Expected on Java 6, in which case we don't use the helper and cope.
        } catch ( NoClassDefFoundError e ) {
            useHelper = null;
            // Expected in unit tests, in which case we don't use the helper and cope.
        }
        helper = useHelper;
    }

    /**
     * If possible, transfer the mode of the source file to the entry.
     * 
     * Do nothing if the permissions helper is not available, or if the
     * mode of the source file is unset (-1).
     * 
     * @param source A source file.
     * @param entry An archive entry.
     */
    private static void setMode(File source, ArchiveEntry entry) {
        if ( helper == null ) {
            return;
        }

        int mode = helper.getUnixMode(source);
        if (mode == -1) {
            return;
        }
        
        if (entry instanceof ZipArchiveEntry) {
            ((ZipArchiveEntry) entry).setUnixMode(mode);
        } else if (entry instanceof TarArchiveEntry) {
            ((TarArchiveEntry) entry).setMode(mode);
        }
    }

    //

    /**
     * Create a zip type archive.
     * 
     * "Zip type" is misleading: The archive can be a TAR archive wrapping a GZIP archive,
     * can be a simple TAR archive, or can be an actual ZIP type archive, depending on the
     * tail of the archive file name -- ".tar.gz", ".tar", or anything else.
     *
     * Creation of the zip archive immediately opens an archive output stream.
     * The archive must be closed (see {@link #close()}).
     *
     * @param archiveFile the target zip file.
     * 
     * @throws IOException Thrown if there is an error creating the archive file.
     */
    public ZipArchive(File archiveFile) throws IOException {
        super(archiveFile);

        FileOutputStream fOut = new FileOutputStream(archiveFile);
        TarArchiveOutputStream tarStream;

        String fileName = archiveFile.getName().toLowerCase();        
        if (fileName.endsWith(".tar.gz")) {
            this.archiveOutputStream = tarStream = new TarArchiveOutputStream(new GZIPOutputStream(fOut)); 
        } else if (fileName.endsWith(".tar")) {
            this.archiveOutputStream = tarStream = new TarArchiveOutputStream(fOut);
        } else { // zip,jar,tmp,etc
            tarStream = null;
            this.archiveOutputStream = new ZipArchiveOutputStream(fOut);
        }

        if ( tarStream != null ) {
            tarStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        }
    }

    /**
     * Add a single file entry to the archive.
     * 
     * The file entry may be a directory entry or a file entry.  In either
     * case, exactly one entry is added to the archive.
     * 
     * Do nothing if the source file is the archive itself.
     * 
     * @param entryPath The path to add to the archive.  This path may not be empty!
     * @Param source The source file which is to be added to the archive.
     * 
     * @throws IOException Thrown if an error occurred while adding the single
     *     file entry.
     */
    @Override
    public void addFileEntry(String entryPath, File source) throws IOException {
        processEntry(entryPath, source);
    }

    /**
     * Add a single file representing a directory, and add entries for the
     * contents of that directory.
     * 
     * Processing is not recursive: All children which are to be processed are
     * present in the child paths collection.
     * 
     * A directory entry is added only if a non-empty directory entry path
     * is specified.
     * 
     * Child files are located as relative children of the directory source
     * file.
     * 
     * The directory entry, if added, uses the specified directory path,
     * normalized.
     * 
     * Child entries are added using the directory entry path concatenated with
     * the child paths.
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
    public void addDirEntry(String dirEntryPath, File dirSource, List<String> childPaths) throws IOException {
        if ( !dirEntryPath.isEmpty() ) {
            processEntry(dirEntryPath, dirSource);
        }

        for ( String childPath : childPaths ) {
            String childEntryPath = dirEntryPath + childPath;
            File childSource = new File(dirSource, childPath);

            processEntry(childEntryPath, childSource);
        }
    }

    //
    
    protected final Set<String> entryPaths = new HashSet<String>();

    /**
     * Add an entry path.  Answer true or false telling if the path was added.
     *
     * @param path A path to add.
     *
     * @return True or false telling if the path was added.
     */
    protected boolean addEntryPath(String path) {
        return entryPaths.add(path);
    }
    
    protected Set<String> getEntryPaths() {
        return entryPaths;
    }

    protected boolean containsEntryPath(String path) {
        return entryPaths.contains(path);
    }    
    
    //

    @SuppressWarnings("rawtypes")
    private final ArchiveOutputStream archiveOutputStream;

    private final byte[] READ_BUFFER = new byte[32 * 1024];
    
    /**
     * Add a single entry to the archive.
     * 
     * Do nothing if the entry was already added.
     * 
     * Do nothing if the source file is the target file.
     * 
     * If the source file is a directory, add the directory entry and return.
     * 
     * If the source file is a simple file, add the file entry, and transfer
     * the file contents. 
     * 
     * Transfer the file mode to the new archive entry.
     * 
     * @param entryPath The path (name) of the entry which is added.
     * @param source The source file. A source file contributing file mode
     *    and (if a simple file) contents to the entry.
     *    
     * @throws IOException Thrown if the entry cannot be added.
     */
    @SuppressWarnings("unchecked")
    private void processEntry(String entryPath, File source) throws IOException {
        if ( source.isDirectory() ) {
            entryPath = FileUtils.normalizeDirPath(entryPath);
        } else {
            // TODO: Should non-directory paths also be normalized?
            // See 'FileUtils.normalizeEntryPath'.            
        }

        if ( !entryPaths.add(entryPath) ) {
            return;
        }

        if ( getArchiveCanonicalFile().equals( source.getCanonicalFile() ) ) {
            return;
        }

        ArchiveEntry entry = archiveOutputStream.createArchiveEntry(source, entryPath);
        setMode(source, entry);

        archiveOutputStream.putArchiveEntry(entry);

        try {
            if ( source.isFile() ) {
                try ( InputStream inputStream = new FileInputStream(source) ) {
                    int bytesRead = 0;
                    while ( (bytesRead = inputStream.read(READ_BUFFER)) != -1 ) {
                        archiveOutputStream.write(READ_BUFFER, 0, bytesRead);
                    }

                } catch ( IOException e ) {
                    System.out.println( MessageFormat.format(BootstrapConstants.messages.getString("info.unableZipFile"), source.getAbsolutePath(), e) );
                }
            }

        } finally {
            archiveOutputStream.closeArchiveEntry();
        }
    }

    /**
     * Main API: Close this archive.
     * 
     * Close is required on all archive, even if {@link #create()} is never invoked.
     * 
     * @throws IOException Thrown if an error occurs while closing the archive.
     */
    @Override
    public void close() throws IOException {
        archiveOutputStream.close();
    }
}
