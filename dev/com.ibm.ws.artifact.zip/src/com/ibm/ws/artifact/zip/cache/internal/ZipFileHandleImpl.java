/*******************************************************************************
 * Copyright (c) 2012,2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.artifact.zip.cache.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.artifact.zip.cache.ZipCachingProperties;
import com.ibm.ws.artifact.zip.cache.ZipFileHandle;
import com.ibm.ws.artifact.zip.internal.FileUtils;

/**
 * A handle to a zip file.
 *
 * These are expected to be held by {@link ZipCachingServiceImpl} and shared
 * between code which accesses the zip file.
 *
 * In addition to the caching provided by the zip caching service, each zip
 * file handle caches the last 16 entries which had 8K or fewer bytes.  That
 * means the zip caching service will use up to 64MB of storage.
 */
public class ZipFileHandleImpl implements ZipFileHandle {
    private static final TraceComponent tc = Tr.register(ZipFileHandleImpl.class);

    @Trivial
    ZipFileHandleImpl(String path) {
        // TODO: Logging shows that different code is requesting zip file handles
        //       with different path formats.
        //
        // For example, on Windows, WSJarURLStreamHandler provides a path which
        // has a leading slash and a drive letter:
        //
        // com.ibm.ws.artifact.url.internal.WSJarURLStreamHandler.connect
        //
        // /c:/Liberty/openliberty-all-18.0.0.3-20180817-1300/
        //   wlp/usr/servers/test2/apps/expanded/TestServlet40.ear/TestServlet40.war/WEB-INF/lib/TestServlet40.jar
        //
        // While other code paths are providing a path which omits the leading slash:
        //
        // c:/Liberty/openliberty-all-18.0.0.3-20180817-1300/
        //   wlp/usr/servers/test2/apps/expanded/TestServlet40.ear/TestServlet40.war/WEB-INF/lib/TestServlet40.jar
        //
        // These path formats must be unified to a single consistent format
        // for maximum sharing of zip file handles.

        this.path = path;
        this.file = new File(path);
    }

    private final String path;
    private final File file;

    @Trivial
    public String getPath() {
        return path;
    }

    @Trivial
    public File getFile() {
        return file;
    }

    @Trivial
    public long getLastModified() {
        return FileUtils.fileLastModified( getFile() );
    }

    //

    private class ZipFileLock {
        // EMPTY
    }
    private final ZipFileLock zipFileLock = new ZipFileLock();
    private ZipFile zipFile;
    private int openCount;

    //

    @Trivial
    private void debug(String methodName, String text) {
        if ( !tc.isDebugEnabled() ) {
            return;
        }

        String message =
            methodName +
            " ZipFileHandle@0x" + Integer.toHexString(hashCode()) +
            " (" + path + ", " + Integer.toString(openCount) + ")" +
            " " + text;
        Tr.debug(tc, message);
    }

    @Trivial
    private void warning(String methodName, String text) {
        String message =
            methodName +
            " ZipFileHandle@0x" + Integer.toHexString(hashCode()) +
            " (" + path + ", " + Integer.toString(openCount) + ")" +
            " " + text;
        Tr.warning(tc, message);
    }

    //

    private static final ZipFileReaper zipFileReaper;

    static {
        int useMaxPending = ZipCachingProperties.ZIP_CACHE_REAPER_MAX_PENDING;
        if ( useMaxPending == 0 ) {
            zipFileReaper = null;
        } else {
            zipFileReaper = new ZipFileReaper(
                "zip cache reaper",
                ZipCachingProperties.ZIP_REAPER_DEBUG_STATE,
                ZipCachingProperties.ZIP_CACHE_REAPER_MAX_PENDING,
                ZipCachingProperties.ZIP_CACHE_REAPER_QUICK_PEND_MIN,
                ZipCachingProperties.ZIP_CACHE_REAPER_QUICK_PEND_MAX,
                ZipCachingProperties.ZIP_CACHE_REAPER_SLOW_PEND_MIN,
                ZipCachingProperties.ZIP_CACHE_REAPER_SLOW_PEND_MAX);
        }
    }

    /**
     * Open the zip file.  Create and assign the zip file if this is the first
     * open.  Increase the open count by one.
     *
     * If this is the first open and the zip file could not be created, the
     * open count is not increased.
     *
     * @return The zip file.
     */
    @Override
    public ZipFile open() throws IOException {
        String methodName = "open";

        synchronized( zipFileLock ) {
            if ( zipFile == null ) {
                debug(methodName, "Opening");
                if ( zipFileReaper == null ) {
                    zipFile = ZipFileUtils.openZipFile(file); // throws IOException
                } else {
                    zipFile = zipFileReaper.open(path);
                }
            }

            openCount++;
            debug(methodName, "Opened");

            return zipFile;
        }
    }

    @Override
    public void close() {
        String methodName = "close";

        boolean extraClose;

        synchronized ( zipFileLock ) {
            if ( !(extraClose = (openCount == 0)) ) {
                debug(methodName, "Closing");

                openCount = openCount - 1;

                // Closing the handle does NOT cause the entry cache to
                // be invalidated.  Entries are invalidated when their
                // CRC changes, regardless of the handle having been
                // closed then re-opened, and regardless of a change
                // to the zip file last modified time.

                if ( openCount == 0 ) {
                    if ( zipFileReaper == null ) {
                        ZipFile useZipFile = zipFile;
                        zipFile = null;
                        try {
                            useZipFile.close();
                        } catch ( IOException e ) {
                            // FFDC
                        }
                    } else {
                        zipFile = null;
                        zipFileReaper.close(path);
                    }
                }

                debug(methodName, "Closed");
            }
        }

        if ( extraClose && tc.isDebugEnabled() ) {
            debug(methodName, "Extra close");

            Exception e = new Exception();
            ByteArrayOutputStream stackStream = new ByteArrayOutputStream();
            PrintStream stackPrintStream = new PrintStream(stackStream);
            e.printStackTrace(stackPrintStream);

            Tr.debug( tc, stackStream.toString() );
        }
    }

    //

    private static class ZipEntriesLock {
        // EMPTY
    }
    private static final ZipEntriesLock zipEntriesLock = new ZipEntriesLock();
    private static final Map<String, byte[]> zipEntries;

    static {
        if ( (ZipCachingProperties.ZIP_CACHE_ENTRY_LIMIT == 0) ||
             (ZipCachingProperties.ZIP_CACHE_ENTRY_MAX == 0) ) {
            zipEntries = null;
        } else {
            zipEntries = new CacheHashMap<String, byte[]>(ZipCachingProperties.ZIP_CACHE_ENTRY_MAX);
        }
    }

    private static class CacheHashMap<K, V>
        extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 1L;

        private final int ivMaxSize;

        public CacheHashMap(int maxSize) {
            this(maxSize, 16, .75f, true);
        }

        public CacheHashMap(int maxSize, int initialCapacity, float loadFactor, boolean accessOrder) {
            super(initialCapacity, loadFactor, accessOrder);

            this.ivMaxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > ivMaxSize;
        }
    }

    private static final ByteArrayInputStream EMPTY_STREAM =
        new ByteArrayInputStream( new byte[0] );

    @Override
    public InputStream getInputStream(ZipFile useZipFile, String zipEntryName) throws IOException {
        String methodName = "getInputStream";

        synchronized( zipFileLock ) {
            if ( openCount == 0 ) {
                warning(methodName, "Closed: Active zip [ " + useZipFile + " ] entry [ " + zipEntryName + " ]");
            } else if ( useZipFile != zipFile ) {
                warning(methodName, "Unstable: New zip [ " + zipFile + " ] active zip [ " + useZipFile + " ] entry [ " + zipEntryName + " ]");
            }
        }

        ZipEntry zipEntry = useZipFile.getEntry(zipEntryName);
        if ( zipEntry == null ) {
            ZipFileUtils.diagnose("getInputStream(" + zipEntryName + ")", file, zipFile);
            throw new FileNotFoundException("Zip file [ " + getPath() + " ] does not contain entry [ " + zipEntryName + " ]");
        }

        return getInputStream(useZipFile, zipEntry);
    }

    /**
     * Answer an input stream for an entry of a zip file.  When the entry is a
     * class entry which has 8K or fewer bytes, read all of the entry bytes immediately
     * and cache the bytes in this handle.  Subsequent input stream requests which
     * locate cached bytes will answer a stream on those bytes.
     *
     * @param useZipFile The zip file for which to answer an input stream
     * @param zipEntry The zip entry for which to answer the input stream.
     *
     * @return An input stream on the bytes of the entry.  Null for an directory
     *     type entry, or an entry which has zero bytes.
     *
     * @throws IOException Thrown if the entry bytes could not be read.
     */
    @Override
    @Trivial
    public InputStream getInputStream(ZipFile useZipFile, ZipEntry zipEntry) throws IOException {
        String methodName = "getInputStream";
        boolean doDebug = tc.isDebugEnabled();

        String entryName = zipEntry.getName();

        if ( zipEntry.isDirectory() ) {
            if ( doDebug ) {
                debug(methodName, "ZipEntry [ " + entryName + " ] [ null ] (Not using cache: Directory entry)");
            }
            return null;
        }

        long entrySize = zipEntry.getSize();
        if ( entrySize == 0 ) {
            if ( doDebug ) {
                debug(methodName, "ZipEntry [ " + entryName + " ] [ empty stream ] (Not using cache: Empty entry)");
            }
            return EMPTY_STREAM;
        }

        long entryCrc = zipEntry.getCrc();
        if ( doDebug ) {
            debug(methodName, "ZipEntry [ " + entryName + " ] Size [ " + entrySize + " ] CRC [ " + entryCrc + " ]");
        }

        boolean doNotCache;
        String doNotCacheReason;

        if ( zipEntries == null ) { // No entry cache.
            doNotCache = true;
            doNotCacheReason = "Do not cache: Entry cache disabled";

        } else if ( entrySize > ZipCachingProperties.ZIP_CACHE_ENTRY_LIMIT) { // Too big for the cache
            doNotCache = true;
            doNotCacheReason = "Do not cache: Too big";
        } else if ( entryCrc == -1 ) {
            doNotCache = true;
            doNotCacheReason = "Do not cache: CRC is unknown";

        } else if ( entryName.equalsIgnoreCase("META-INF/MANIFEST.MF") ) {
            doNotCache = false;
            doNotCacheReason = "Cache META-INF/MANIFEST.MF";
        } else if ( entryName.endsWith(".class") ) {
            doNotCache = false;
            doNotCacheReason = "Cache .class resources";

        } else {
            doNotCache = true;
            doNotCacheReason = "Do not cache: Not manifest or class resource";
        }

        if ( doDebug ) {
            debug(methodName, "Entry [ " + entryName + " ] [ non-null ] [ " + doNotCacheReason + " ]");
        }

        if ( doNotCache ) {
            return useZipFile.getInputStream(zipEntry); // throws IOException
        }

        // No longer use the last modified time to invalidate entries.
        //
        // Firstly, because we don't test and refresh the zip file based on the last
        // modified time.
        //
        // Secondly, fetching the last modified time every time an input stream is
        // obtained is very expensive -- accounting for 2-4 percent of startup time
        // when annotation scans are enabled.

        String entryCacheKey = entryName + ":::" + Long.toString(entryCrc);

        // Note that only the individual gets and puts are protected.
        //
        // That means that simultaneous get misses are possible, which
        // will result in double reads and double puts.
        //
        // That is unfortunate, but is harmless.
        //
        // The simultaneous puts are allowed because they should be very
        // rare.
        //
        // They are allowed because blocking entry gets while waiting for
        // reads could create large delays.

        byte[] entryBytes;
        synchronized( zipEntriesLock ) {
            entryBytes = zipEntries.get(entryCacheKey);
        }

        if ( entryBytes == null ) {
            InputStream inputStream = useZipFile.getInputStream(zipEntry); // throws IOException
            try {
                entryBytes = read(inputStream, (int) entrySize, entryName); // throws IOException
            } finally {
                inputStream.close(); // throws IOException
            }

            synchronized( zipEntriesLock ) {
                zipEntries.put(entryCacheKey, entryBytes);
            }
        }

        return new ByteArrayInputStream(entryBytes);
    }

    /**
     * Read an exact count of bytes from an input stream.
     *
     * @param inputStream The stream from which to read the bytes.
     *
     * @param expectedRead The number of bytes which are to be read.
     * @param name A name associated with the stream.
     *
     * @return The bytes read from the stream.
     *
     * @throws IOException Throw if the read failed, including the case where
     *     insufficient bytes were available to be read.
     */
    @Trivial
    private static byte[] read(InputStream inputStream, int expectedRead, String name) throws IOException {
        byte[] bytes = new byte[expectedRead];

        int remainingRead = expectedRead;
        int totalRead = 0;

        while ( remainingRead > 0 ) {
            int nextRead = inputStream.read(bytes, totalRead, remainingRead); // throws IOException
            if ( nextRead <= 0 ) {
                // 'nextRead == 0' should only ever happen if 'remainingRead == 0', which ought
                // never be the case here.  Treat a '0' return value as an error.
                //
                // 'nextRead == -1' means the end of input was reached.

                throw new IOException(
                    "Read only [ " + Integer.valueOf(totalRead) + " ]" +
                    " of expected [ " + Integer.valueOf(expectedRead) + " ] bytes" +
                    " from [ " + name + " ]");
            } else {
                remainingRead -= nextRead;
                totalRead += nextRead;
            }
        }

        return bytes;
    }
}
