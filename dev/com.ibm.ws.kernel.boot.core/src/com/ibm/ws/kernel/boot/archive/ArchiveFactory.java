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
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

import com.ibm.ws.kernel.boot.Debug;
import com.ibm.ws.kernel.boot.security.WLPDynamicPolicy;

/**
 * Archive factory API.  The main factory methods are
 * {@link #create(File)}, {@link #create(String)}, and
 * {@link #create(File, boolean)}.
 * 
 * The archive type is a subtype of {@link Closeable}.  After
 * being created, an archive must be closed. 
 */
public class ArchiveFactory {
    /*
     * This class uses a URLClassLoader and reflection to provide the implementations of the Archive interface.
     * This is done so that archive subclasses (such as ZipArchive) that depend on third party libraries (such
     * as Apache Commons Compress) do not inadvertently make those libraries available to Liberty server and/or
     * application code by being in the JVM's application classpath.
     * This class uses a URLClassLoader and reflection to provide the
     * implementations of the Archive interface.
     *
     * Archive implementations should reside in the com.ibm.ws.kernel.boot.archive bundle, and their class names
     * This is done so that archive subclasses (such as ZipArchive) that
     * depend on third party libraries (such as Apache Commons Compress)
     * do not inadvertently make those libraries available to Liberty
     * server and/or application code by being in the JVM's application
     * class path.
     *
     * Archive implementations must reside in the bundle
     * "com.ibm.ws.kernel.boot.archive", and their class names
     * should be declared here.
     */

    private final static URL ARCHIVE_IMPL_BUNDLE_URL;
    static {
        URL u = ArchiveFactory.class.getProtectionDomain().getCodeSource().getLocation();
        String path = u.toExternalForm();
        path = path.replaceAll("com.ibm.ws.kernel.boot_", "com.ibm.ws.kernel.boot.archive_");
        path = path.replaceAll("/com.ibm.ws.kernel.boot.core/build/classes/", "/com.ibm.ws.kernel.boot.archive/build/classes/");
        try {
            u = new URL(path);
        } catch (MalformedURLException e) {
            Debug.printStackTrace(e);
            u = null;
        }
        ARCHIVE_IMPL_BUNDLE_URL = u;
    }

    // PAX: Portable Archive Exchange file created by pax, a command line utility
    // used for creating and extracting archives; may store the archive in one
    // of pax's several supported formats, which include bcpio, cpio, sv4cpio,
    // sv4crc, tar, and ustar.
    //
    // Ref: https://fileinfo.com/extension/pax

    /** Implementation class name for creating PAX archives. */
    private final static String PAX_ARCHIVE_CLASS_NAME =
        "com.ibm.ws.kernel.boot.archive.internal.PaxArchive";

    /** Implementation class name for creating ZIP archives. */
    private final static String ZIP_ARCHIVE_CLASS_NAME =
        "com.ibm.ws.kernel.boot.archive.internal.ZipArchive";

    /**
     * Archive factory API: Create an archive.
     * 
     * This factory method does not adjust the security policy.  If that
     * is to be done, use instead {@link #create(File, boolean)}.
     * 
     * @param archivePath The path to the file which is to be created.
     *     This must be an absolute path.
     *
     * @return The new archive.
     *
     * @throws IOException Thrown if the archive could not be created.
     *     This may be a reflection exception, wrapped into an IOException.
     */    
    public static Archive create(final String archivePath) throws IOException {
        File archiveFile = new File(archivePath);
        return create(archiveFile);
    }

    public static Archive create(final File archiveFile) throws IOException {
        if (!archiveFile.isAbsolute())
            throw new IllegalArgumentException(archiveFile.getPath());

        String className;
        if (archiveFile.getName().endsWith(".pax")) {
            className = PAX_ARCHIVE_CLASS_NAME;
        } else {
            className = ZIP_ARCHIVE_CLASS_NAME;
        }
        // could add other archive type in future

        try {
            @SuppressWarnings("resource")
            URLClassLoader loader = new URLClassLoader(new URL[] { ARCHIVE_IMPL_BUNDLE_URL });
            @SuppressWarnings("unchecked")
            Class<? extends Archive> archiveImplClass = (Class<? extends Archive>) loader.loadClass(className);
            Constructor<? extends Archive> ctor = archiveImplClass.getConstructor(File.class);
            return ctor.newInstance(archiveFile);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Archive factory API.  Create an archive.  If requested,
     * add the archive bundle URL to the security policy.
     * See {@link Policy#setPolicy(Policy)}, and
     * {@link WLPDynamicPolicy}.
     *
     * @param archiveFile The file which is to be created.
     * @param j2security Control parameter: Tells if the
     *    security policy is to be updated.
     *
     * @return The new archive.
     */
    public static Archive create(final File archiveFile, boolean j2security) throws IOException {
        Archive arch = null;
        if (j2security == true) {
            List<URL> urlList = new ArrayList<URL>();
            urlList.add(ARCHIVE_IMPL_BUNDLE_URL);
            Policy wlpPolicy = new WLPDynamicPolicy(Policy.getPolicy(), urlList);
            Policy.setPolicy(wlpPolicy);
            wlpPolicy.refresh();
            arch = create(archiveFile);
        } else {
            arch = create(archiveFile);
        }
        return arch;
    }
}
