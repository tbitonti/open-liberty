/*******************************************************************************
 * Copyright (c) 1997, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.artifact.url.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.osgi.service.url.AbstractURLStreamHandlerService;

import com.ibm.ws.artifact.url.WSJarURLConnection;
import com.ibm.ws.artifact.zip.cache.ZipCachingService;
import com.ibm.ws.artifact.zip.cache.ZipFileHandle;
import com.ibm.ws.kernel.security.thread.ThreadIdentityManager;
import com.ibm.wsspi.kernel.service.utils.ParserUtils;

/**
 * <p>URL Handler for class loading.</p>
 *
 * <p>This URL handler supports a new "wsjar" protocol, with the following
 * syntax:</p>
 *
 * <code>
 * wsjar:file:{URL}!/entryname
 * </code>
 */
public class WSJarURLStreamHandler extends AbstractURLStreamHandlerService { // LIDB3418
    /**
     * <p>Set the zip caching service of this handler.</p>
     * 
     * <p>A "wsjar" handler provides a path to an archive (say, JAR or ZIP) and the path
     * of an entry in that archive.</p>
     * 
     * <p>A connection formed through the handler does is made by obtaining the archive
     * from the zip caching server then opening an input stream on the entry within
     * the archive.</p>
     * 
     * <p>The zip caching service is used instead of directly accessing the archive
     * so to optimize opens of the zip file.  The service keeps a cache of opened
     * zip files, which helps to reduce the number of times archive files are opened.</p>
     * 
     * @param zipCachingService The zip caching service used by this handler.
     */
    protected void setZipCachingService(ZipCachingService zipCachingService) {
        this.zipCachingService = zipCachingService;
    }

    //

    /** <p>The service which handles connections made through this handler.</p> */
    private ZipCachingService zipCachingService;

    //

    // begin 412642
    @Override
    protected void parseURL(URL url, String relativeUrlText, int pathStart, int pathEnd) {
        boolean hasProtocol = ( (pathStart != 0) && (relativeUrlText.charAt(pathStart - 1) == ':') );

        relativeUrlText = relativeUrlText.substring(pathStart, pathEnd);

        String relativePath;
        if ( hasProtocol ) {
            // If the "wsjar:" protocol was specified in the path, then we are
            // not parsing relative to another URL.  The "jar" protocol
            // requires that the path contain an entry path and that the base
            // URL be valid.  For backwards compatibility, we enforce neither.
            relativePath = relativeUrlText;
        } else {
            relativePath = parseRelativeURL(url, relativeUrlText);
        }

        setURL( url,
                "wsjar",
                url.getHost(), url.getPort(),
                url.getAuthority(), url.getUserInfo(),
                relativePath,
                url.getQuery(), url.getRef() );
    }

    /**
     * <p>Create a URL relative to a specified URL.  Answer the text of that URL.
     *
     * <p>For example, for URL:</p>
     * <code>wsjar:file:/a/b!/c</code>
     *
     * <p>And for non-canonical entry path:</p>
     * <code>../../d/e</code>
     *
     * </p>Generate the URL text:</p>
     * <code>wsjar:file:/a/b!/d/e</code>
     *
     * @param url The relative to which to generate new relative URL text.
     * @param nonNormalizedEntryPath A non-normalized entry path for which
     *     to generate a new relative URL.
     *
     * @return Text of the new relative URL. 
     */
    private static String parseRelativeURL(URL url, String nonNormalizedEntryPath) {
        String urlPath = url.getPath();
        int index = urlPath.indexOf("!/");
        if ( index == -1 ) {
            // The "jar" protocol requires that the path contain an entry path.
            // For backwards compatibility, we do not.  Instead, we just
            // canonicalize the base URL.
            try {
                url = Utils.newURL( Utils.newURL(urlPath), nonNormalizedEntryPath );
            } catch ( MalformedURLException e ) {
                throw new RuntimeException(e);
            }
            return url.toString();
        }

        String entryPath;

        if ( nonNormalizedEntryPath.startsWith("/") ) {
            // This is an absolute entry path, so we ignore the existing
            // entry path.
            entryPath = canonicalize(nonNormalizedEntryPath);

        } else {
            // Extract the existing entry path.
            entryPath = urlPath.substring(index + 1);

            if ( entryPath.endsWith("/") ) {
                // The entry ends with '/', so we can directly append the
                // relative entry path.
                entryPath = canonicalize(entryPath + nonNormalizedEntryPath);
            } else if ( entryPath.endsWith("/.") ) {
                // We don't want the logic below because "." is not a "true" base name.
                entryPath = canonicalize(entryPath + '/' + nonNormalizedEntryPath);
            } else {
                // Strip off the base name with a ".." path segment and
                // append the relative entry path.
                entryPath = canonicalize(entryPath + "/../" + nonNormalizedEntryPath);
            }
        }

        return urlPath.substring(0, index) + '!' + entryPath;
    }

    // Removes all ".." and "." path segments.  All paths passed to and
    // returned from this function must start with with "/".
    private static String canonicalize(String path) {
        int pathLength = path.length();

        StringBuilder canonicalPath = new StringBuilder(pathLength);

        // The first character must be '/'.  Append it to the result, and then
        // skip the first character in the input string.

        canonicalPath.append('/');

        for ( int charNo = 1; charNo < pathLength; charNo++ ) {
            char nextChar = path.charAt(charNo);

            if ( (nextChar != '.') || (canonicalPath.charAt(canonicalPath.length() - 1) != '/') ) {
                // A character other than '.', or we're not at the beginning of a path segment.
                canonicalPath.append(nextChar);

            } else if ( (charNo + 1 == pathLength) || (path.charAt(charNo + 1) == '/') ) {
                // A "." path segment.  Skip the following "/".
                charNo++;

            } else if ( (path.charAt(charNo + 1) == '.') &&
                        ((charNo + 2 == pathLength) || (path.charAt(charNo + 2) == '/')) ) {
                // A ".." path segment.  Skip the following "./".
                charNo += 2;

                int canonicalPathLength = canonicalPath.length();
                if ( --canonicalPathLength > 0 ) {
                    // Walk backwards until we find a '/'.
                    while ( (canonicalPath.charAt(canonicalPathLength - 1) != '/') && (--canonicalPathLength > 0) ) {
                        // EMPTY
                    }
                    canonicalPath.setLength(canonicalPathLength);
                }

            } else {
                // A path segment that starts with a '.' that is not "." or "..".
                canonicalPath.append('.');
            }
        }

        return canonicalPath.toString();
    }
    // end 412642

    // begin 408408.2
    /**
     * <p>Open a connection to the specified URL.</p>
     * 
     * <p>Two modes are supported:</p>
     * 
     * <p>For backwards compatibility, the URL need not have an entry path.  When
     * no entry path is specified, open a connection to the archive itself.</p>
     * 
     * <p>When an entry path is specified, open a connection to the archive and
     * to the specified entry of the archive.</p>
     * 
     * <p>When connecting to an an archive and to an entry of the archive, the
     * connection is not fully formed until an action is performed through the new
     * URL connection.</p>
     * 
     * @param url The URL for which to open a connection.
     * 
     * @return A connection to the URL.
     * 
     * @throws IOException Thrown if the URL does not contain an entry path, and
     *     the connection to the archive file could not be made.
     */
    @Override
    public URLConnection openConnection(URL url) throws IOException {
        String path = url.getPath();
        int resourceDelimiterIndex = path.indexOf("!/");

        if ( resourceDelimiterIndex == -1 ) {
            // The "jar" protocol requires that the path contain an entry path.
            // For backwards compatibility, we do not.  Instead, we just
            // open a connection to the underlying URL.
            return Utils.newURL(path).openConnection(); // throws IOException

        } else {
            // First strip the resource name out of the path.
            String urlString = ParserUtils.decode( path.substring(0, resourceDelimiterIndex) );

            // Note that we strip off the leading "/" because ZipFile.getEntry does
            // not expect it to be present.
            String entryPath = ParserUtils.decode( path.substring(resourceDelimiterIndex + 2) );

            // Since the URL we were passed may reference a file in a remote file system with
            // a UNC based name (\\Myhost\Mypath), we must take care to construct a new "host agnostic"
            // URL so that when we call getPath() on it we get the whole path, irregardless of whether
            // the resource is on a local or a remote file system. We will also now validate
            // our urlString has the proper "file" protocol prefix.
            URL jarURL = constructUNCTolerantURL("file", urlString);

            // TODO: Either here, or inside the connection, the path to the archive
            //       should be canonicalized.  That doesn't seem to be done currently,
            //       which means the underlying zip file handle service may not be
            //       fully reducing the number of zip file opens because it does not
            //       match the paths successfully.

            return new WSJarURLConnectionImpl( url, jarURL.getPath(), entryPath, zipCachingService );
        }
    }

    /**
     * <p>Construct a purely path based URL from an input string that may contain UNC
     * compliant host names.   For example:</p>
     *
     * <p>Remote file system (UNC convention):</p>
     * 
     * <code>protocolPrefix://host/path</code>
     * <code>protocolPrefix:////host/path</code>
     *
     * <p>Local file system:</p>
     * 
     * <code>protocolPrefix:/path</code>
     * <code>protocolPrefix:/C:/path</code>
     *
     * <p>Throw an {@link IOException} if the protocol text does not start with a
     * protocol value.</p>
     *
     * <p>Note: UNC paths are used by several OS platforms to map remote file systems.
     * Using this method guarantees that even if the pathComponent contains a UNC based
     * host, the host prefix of the path will be treated as path information and not
     * interpreted as a host component part of the URL returned by this method.</p>
     *
     * @param protocol The protocol which must be the start of the URL text.
     * @param urlText Text of a URL which is to used to create a hostless URL.
     *
     * @return A URL that is constructed consisting of only path information
     *     (having no implicit host information).
     *
     * @throws IOException Thrown if the URL text does not begin with the specified protocol.
     */
    protected static URL constructUNCTolerantURL(String protocol, String urlText) throws IOException {
        int protocolEnd = urlText.indexOf(':');
        if ( protocolEnd < 0 ) {
            throw new IOException("URL [ " + urlText + " ] does not have protocol [ " + protocol + " ]");
        }

        String urlProtocol = urlText.substring(0, protocolEnd);
        if ( !urlProtocol.equalsIgnoreCase(protocol) ) {
            throw new IOException("URL [ " + urlText + " ] does not have protocol [ " + protocol + " ]");
        }

        String urlPath = urlText.substring(protocolEnd + 1);

        // By using this constructor we make sure the JDK does not attempt to interpret
        // leading characters as a host name. This assures that any embedded UNC encoded
        // string prefixes are ignored and subsequent calls to getPath on this URL will
        // always return the full path string (including the UNC host prefix).
        return Utils.newURL(urlProtocol, "", -1, urlPath);
    }

    //

    /**
     * <p>URL connection implementation for the wsjar protocol.</p>
     * 
     * <p>The connection encodes a path to a ZIP file and a path to an
     * entry of the zip file.</p>
     * 
     * <p>The connection is made using the zip caching service.</p>
     */
    private static class WSJarURLConnectionImpl extends URLConnection implements WSJarURLConnection {
        WSJarURLConnectionImpl(
            URL url,
            String zipFilePath,
            String zipEntryPath,
            ZipCachingService zipCachingService) {

            super(url);

            this.zipCachingService = zipCachingService;

            this.zipFilePath = zipFilePath;

            // TODO: This replacement is suspect.  Very probably,
            //       an IllegalArgumentException should be thrown.
            this.zipEntryPath = ( (zipEntryPath == null) ? "" : zipEntryPath );

            this.contentType = null;
            this.contentLength = null;
        }

        //

        /** <p>The service which will open the connection.</p> */
        private final ZipCachingService zipCachingService;

        /**
         * <p>Retrieve the zip file handle for the mapped zip file.</p>
         * 
         * <pThe handle is not guaranteed to be unique: The zip handle service
         * caches the zip file handles, but does not have a protocol which
         * ensures that the zip file handle is retained.</p>
         *
         * @return A handle to the zip file of this connection.
         * 
         * @throws IOException Thrown if the zip file handle could not
         *     be obtained.  Not currently thrown.
         */
        private ZipFileHandle getZipFileHandle() throws IOException {
            return zipCachingService.openZipFile(zipFilePath); // throws IOException
        }

        //

        /** <p>The canonical path of the zip file to which to connect.</p> */
        private final String zipFilePath;

        @Override
        public File getFile() {
            return new File(zipFilePath);
        }

        // TODO: Shouldn't the last modified time be obtained from the mapped entry,
        //       not from the entire zip file?
        @Override
        // PK84650
        public long getLastModified() {
            // TODO: Would be nice to store this, but don't know if that is safe.
            //       Arguably, since the content type and content length are stored,
            //       this is safe to store.
            return Utils.getLastModified( getFile() );
        }

        @Override
        public Permission getPermission() throws IOException {
            String permissionPath;
            if ( File.separatorChar != '/' ) {
                permissionPath = zipFilePath.replace('/', File.separatorChar);
            } else {
                permissionPath = zipFilePath;
            }
            return new FilePermission(permissionPath, "read");
        }

        //

        /** <p>The path of the entry to which to connect.</p> */
        private final String zipEntryPath;

        @Override
        public String getEntry() {
            return zipEntryPath;
        }

        //

        /**
         * <p>Obtain the input stream for the zip file and entry.</p>
         * 
         * <p>Perform the operation within a region protected by
         * {@link ThreadIdentityManager#runAsserver}.  This makes sure
         * access to the zip file uses the server ID rather than the ID
         * of a sync'ed user.</p>
         * 
         * <p>These steps are necessary for running on Z/OS, in relation
         * to 'syncToOSThread'.</p>
         * 
         * <p>Obtaining an input stream holds the mapped zip file open.
         * The input stream must be closed at the end of its use.</p>
         * 
         * @return The input stream for the zip file and entry.
         * 
         * @throws IOException Thrown if the input stream could not be obtained.
         */
        @Override
        public InputStream getInputStream() throws IOException {
            Object token = ThreadIdentityManager.runAsServer();
            try {
                return getInputStreamInternal(); // throws IOException
            } finally {
                ThreadIdentityManager.reset(token);
            }
        }

        // TODO: Not sure why this is synchronized.  The entire class
        //       should be made thread safe (or should be not thread safe).
        //       Picking portions to synchronize is not reliable.

        private synchronized InputStream getInputStreamInternal() throws IOException {
            // Add to the open count of the handle.  That means the handle
            // must be closed.  Normally, the close occurs through the input
            // stream which is returned.  If the input stream is not returned,
            // the close must be performed locally.

            ZipFileHandle zipFileHandle = getZipFileHandle(); // throws IOException // PK72252
            ZipFile zipFile = zipFileHandle.open(); // throws IOException 

            try {
                ZipEntry zipEntry = zipFile.getEntry(zipEntryPath);
                if ( zipEntry == null ) {
                    throw new FileNotFoundException("Entry [ " + zipEntryPath + " ] not found in [ " + zipFilePath + " ]");
                }

                // Successfully opening the zip file and finding the mapped entry are
                // the conditions on forming a connection.
                setConnectedToZipHandle(true);

                InputStream entryInputStream =
                    zipFileHandle.getInputStream(zipFile, zipEntry); // throws IOException

                ZipEntryInputStream zipEntryInputStream =
                    new ZipEntryInputStream(zipFileHandle, entryInputStream); // PK72252

                // The entry input stream takes the responsibility
                // for closing the zip file handle.                
                zipFileHandle = null; // PK72252

                return zipEntryInputStream;

            } finally {
                // Null after the zip entry input stream is created: That stream
                // takes responsibility for closing the zip file handle.
                if ( zipFileHandle != null ) { // PK72252
                    zipFileHandle.close();
                }
            }
        }

        public static final String UNKNOWN_CONTENT_TYPE = "content/unknown";

        private String contentType;
        private Long contentLength;

        @Override
        public String getContentType() {
            if ( contentType == null ) {
                // Ask URL connection to figure it out from the entry data.
                String useContentType = getStreamContentType();
                if ( useContentType == null ) {
                    // Ask URL connection to figure it out from the entry name.                    
                    useContentType = guessContentTypeFromName(zipEntryPath);
                    if ( useContentType == null ) {
                        // Couldn't figure it out: Assign as unknown.
                        useContentType = UNKNOWN_CONTENT_TYPE;
                    }
                }

                this.contentType = useContentType;
            }

            return contentType;
        }

        private String getStreamContentType() {
            try {
                InputStream entryInputStream = getInputStream(); // throws IOException
                try {
                    // URLConnection.guessContentTypeFromStream needs a stream which
                    // supports marks.
                    return guessContentTypeFromStream( new BufferedInputStream(entryInputStream) );
                    // 'guessContentTypeFromStream' throws IOException
                } finally {
                    entryInputStream.close(); // throws IOException
                }

            } catch ( IOException e ) {
                // FFDC

                // Do not throw the exception: URLConnection.getContentType does not
                // support a thrown exception.

                // Do not assign the unknown content type if an exception occurred.
                // Allow the step of guessing the content type from the entry path 
                // to be performed.
                return null;
            }
        }

        @Override
        public long getContentLengthLong() {
            if ( contentLength != null ) {
                return contentLength.longValue();
            }

            long useContentLength = computeContentLength();

            contentLength = Long.valueOf(useContentLength);

            return useContentLength;
        }

        private long computeContentLength() {
            ZipFileHandle zipFileHandle;
            try {
                zipFileHandle = getZipFileHandle(); // throws IOException
            } catch ( IOException e ) {
                // FFDC
                return -1L;
            }

            ZipFile zipFile;
            try {
                zipFile = zipFileHandle.open(); // throws IOException
            } catch ( IOException e ) {
                // FFDC
                return -1L;
            }

            long useContentLength;

            try {
                ZipEntry zipEntry = zipFile.getEntry(zipEntryPath);
                if ( zipEntry == null ) {
                    useContentLength = -1L;

                } else {
                    // The conditions of forming a connection are that the handle is obtained,
                    // the zip file can be opened, and the mapped entry exists.
                    setConnectedToZipHandle(true);

                    useContentLength = zipEntry.getSize();
                    if ( useContentLength < 0L ) {
                        // Compressed entry: Have to stream to figure out the size.
                        try {
                            InputStream entryInputStream =
                                zipFile.getInputStream(zipEntry); // throws IOException
                            try {
                                useContentLength = Utils.getStreamLength(entryInputStream); // throws IOException
                            } finally {
                                entryInputStream.close(); // throws IOException
                            }
                        } catch ( IOException e ) {
                            // FFDC
                            useContentLength = -1L;
                        }
                    }
                }

            } finally {
                zipFileHandle.close();
            }

            return useContentLength;
        }
        
        @Override
        public int getContentLength() {
            long contentLengthLong = getContentLengthLong();
            if ( contentLengthLong > Integer.MAX_VALUE ) {
                return -1;
            } else {
                return (int) contentLengthLong;
            }
        }

        //

        private boolean connectedToZipHandle;

        private boolean getConnectedToZipHandle() {
            return connectedToZipHandle;
        }
        
        private void setConnectedToZipHandle(boolean connectedToZipHandle) {
            this.connectedToZipHandle = connectedToZipHandle;
        }
        
        /**
         * <p>Create the connection.  Mark this handler as connected.</p>
         * 
         * <p>Fail with an IO exception if the connection cannot be formed.
         * For a mapped zip entry, failure usually means that the zip file
         * could not be opened or that the mapped entry does not exist
         * in the zip file.</p>
         */
        @Override
        public void connect() throws IOException {
            if ( getConnectedToZipHandle() ) {
                return;
            }

            // A zip file handle is necessary to form the connection.
            // The current implementation of 'getZipFileHandle' never throws an exception.
            ZipFileHandle zipFileHandle = getZipFileHandle(); // throws IOException // PK72252

            // The zip file must be opened to form the connection.
            // Opening the zip file *can* fail.
            ZipFile zipFile = zipFileHandle.open(); // throws IOException // PK72252

            // The entry must exist to form the connection.            
            boolean entryExists;
            try {
                entryExists = (zipFile.getEntry(zipEntryPath) != null);
            } finally {
                zipFileHandle.close(); // PK72252
            }
            if ( !entryExists ) {
                throw new FileNotFoundException("Entry [ " + zipEntryPath + " ] not found in [ " + zipFilePath + " ]");
            }

            // The conditions of forming a connection are that the handle is obtained,
            // the zip file can be opened, and the mapped entry exists.
            setConnectedToZipHandle(true);
        }
    }

    /**
     * <p>Wrapper for an input stream which was obtained from a zip file handle.</p>
     * 
     * <p>A reference to the zip file handle is necessary: The close of the input
     * stream must close the zip file handle.</p>
     */
    private static class ZipEntryInputStream extends FilterInputStream {
        private ZipFileHandle zipFileHandle;

        ZipEntryInputStream(ZipFileHandle zipFileHandle, InputStream inputStream) {
            super(inputStream);

            this.zipFileHandle = zipFileHandle;
        }

        /**
         * <p>Make sure to close the input stream.</p>
         * 
         * @throws Throwable An {@link IOException} can be thrown when
         *     attempting to close the input stream.
         */
        @Override
        public void finalize() throws Throwable {
            close(); // throws IOException

            super.finalize(); // throws Throwable
        }

        // TODO: Not sure why this is synchronized.  The entire class
        //       should be made thread safe (or should be not thread safe).
        //       Picking portions to synchronize is not reliable.

        /**
         * <p>Extend to close the mapped zip file handle.</p>
         * 
         * @throws IOException Thrown if the close fails.  Never thrown
         *     by this implementation. 
         */
        @Override
        public synchronized void close() throws IOException { // 578280
            if ( zipFileHandle == null ) { // 578280
                return;
            }

            // D531229.1 Always close the input stream even though
            // ZipFile.close claims it does automatically.  See 531229.2.
            try {
                super.close(); // throws IOException
            } catch ( IOException e ) {
                // FFDC
                // TODO: This maybe should be re-thrown after closing
                //       the zip file handle.
            }

            // Guard against an exception in ZipFileHandle.close:
            ZipFileHandle useZipFileHandle = zipFileHandle;
            zipFileHandle = null;

            // The key step: If the zip file handle isn't closed, it will
            // be held and held open forever by the zip file cache.
            useZipFileHandle.close(); // 578280
        }
    }

    // end 408408.2
}
