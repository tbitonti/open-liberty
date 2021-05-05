/*******************************************************************************
 * Copyright (c) 2011, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.boot.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.kernel.boot.BootstrapConfig;
import com.ibm.ws.kernel.boot.cmdline.Utils;
import com.ibm.ws.kernel.boot.logging.TextFileOutputStreamFactory;

public class FileUtils {
    private static final boolean IS_WSL;

    static {
        String OS_NAME = System.getProperty("os.name");
        String OS_VERSION = System.getProperty("os.version");

        IS_WSL = ( OS_NAME.toLowerCase(Locale.ENGLISH).contains("linux") &&
                   OS_VERSION.toLowerCase(Locale.ENGLISH).contains("microsoft") );
    }

    @Trivial
    public static boolean isWSL() {
        return IS_WSL;
    }    

    //

    /**
     * Pattern for matching absolute URI.  For example:, "file:/" or "schema:/".
     **/
    public static final Pattern ABSOLUTE_URI = Pattern.compile("^[^/#\\?]+?:/.*");

    /**
     * Match files in a target directory.
     * 
     * If no patterns are provided, or if the patterns collection is empty,
     * answer all of the files in the target directory.
     * 
     * Depending on the include control parameter, either, answer all files which
     * match at least one of the patterns, or answer all files which match none of
     * the patterns. 
     * 
     * @param target The target directory which is to be listed.
     * @param patterns The patterns used to select files of the target directory.
     * @param include Control parameter: When true, select files which match any
     *     of the patterns.  When false, select files which match none of the
     *     patterns.
     * @return The selected files.
     */
    public static File[] listFiles(File target, List<Pattern> patterns, boolean include) {
        if ( (patterns == null) || patterns.isEmpty() ) {
            return target.listFiles();
        }

        return target.listFiles( (File targetDir, String childName) -> {
            for ( Pattern pattern : patterns ) {
                Matcher matcher = pattern.matcher(childName);
                if ( matcher.matches() ) {
                    return include;
                }
            }
            return !include;
        });
    }

    /**
     * Copy a file by reading and writing the bytes of the file.
     *
     * Create parent directories of the target file if necessary.
     *
     * Do nothing if the destination file is null.
     *
     * @param dest The file which is to be created.
     * @param source The file which is to be copied.
     *
     * @throws IOException Thrown if the copy failed.
     */
    public static void copyFile(File dest, File source) throws IOException {
        if ( dest == null ) {
            return;
        }
        try ( InputStream input = new FileInputStream(source) ) {
            createFile(dest, input);
        }
    }

    /** The size the buffer used when copying files. */
    protected static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    /**
     * Copy an input stream to a destination file.
     *
     * Create the parent directories of the target file
     * if necessary.
     *
     * Close the input stream after making the copy.
     *
     * Truncate the destination file if it already exists.
     *
     * Do nothing either the input stream or the destination file is null.
     *
     * @param dest The file which is to be created.
     * @param input The stream which is to be copied.
     *
     * @throws IOException Thrown if the destination could be written.
     *     Thrown if a parent directory of the destination could not be created.
     */
    public static void createFile(File dest, InputStream input) throws IOException {
        // TFB:
        // TODO: If the input not null but the destination is null,
        //       the input stream will not be closed.
        if ( (input == null) || (dest == null) ) {
            return;
        }

        try {
            File destParent = dest.getParentFile();
            if ( !destParent.exists() ) {
                if (!destParent.mkdirs()) {
                    throw new FileNotFoundException( destParent.getAbsolutePath() );
                }
            }

            try ( OutputStream fos = TextFileOutputStreamFactory.createOutputStream(dest) ) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int count = -1;
                while ( (count = input.read(buffer)) > 0 ) {
                    fos.write(buffer, 0, count);
                }
                fos.flush();
            }

        } finally {
            Utils.tryToClose(input);
        }
    }

    /**
     * Copy an input stream to a destination file.
     *
     * Do not create any parent directories for the destination.
     *
     * Close the input stream after making the copy.
     *
     * Append to the destination file if it already exists.
     *
     * Do nothing either the input stream or the destination file is null.
     *
     * @param dest The file which is to be created.
     * @param input The stream which is to be copied.
     *
     * @throws IOException Thrown if the destination could be written.
     *     Thrown if a parent directory of the destination does not exist.
     *     
     * <strong>Note: {@link #appendFile(File, InputStream)} is not symmetric
     * with {@link #createFile(String, InputStream)}.  Both will create
     * the target file.  However, while {@link #createFile(File, InputStream)
     * will create parent directories of the target file,
     * {@link #appendFile(File, InputStream)} will not.</strong>
     */
    public static void appendFile(File dest, InputStream sourceInput) throws IOException {
        if ( (sourceInput == null) || (dest == null) ) {
            return;
        }

        try {
            if ( !dest.getParentFile().exists() ) {
                throw new FileNotFoundException();
            }

            try ( OutputStream fos = TextFileOutputStreamFactory.createOutputStream(dest, true) ) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int count = -1;
                while ( (count = sourceInput.read(buffer)) > 0 ) {
                    fos.write(buffer, 0, count);
                }
                fos.flush();
            }
            
        } finally {
            Utils.tryToClose(sourceInput);
        }
    }

    /**
     * Recursively copy a directory.
     * 
     * Do nothing if the source is not a directory.
     * 
     * The destination directory must exist.  For ensure that a
     * complete copy is made, the destination should be empty.
     * Simples files will be overwritten.  Sub-directories of
     * the source are not copied if a corresponding sub-directory
     * of the destination already exists.
     *
     * Children of the source directory which are neither directories
     * not simple files are ignored.
     *
     * (No error occurs if a destination sub-directory already
     * exists.  Copying silently skips that sub-directory.)
     *
     * Copying stops on any error.  This usually means that the
     * copied directory is incomplete.
     *
     * @param source The directory which is to be copied.
     * @param destination The directory which is to receive the copy.
     *
     * @throws IOException Thrown if the copy could not be made.
     */
    public static void copyDir(File source, File destination) throws IOException {
        File[] sourceChildren = source.listFiles();

        if ( sourceChildren != null ) {
            for ( File sourceChild : sourceChildren ) {
                File destChild = new File(destination, sourceChild.getName());
                if ( sourceChild.isDirectory() ) {
                    if ( destChild.mkdir() ) {
                        copyDir(sourceChild, destChild);
                    } else {
                        // The destination directory already exists,
                        // or creation of the directory failed.
                        // Silently skip the sub-directory.
                    }
                } else if ( sourceChild.isFile() ) {
                    copyFile(destChild, sourceChild);
                } else {
                    // Other special file type. Silently ignore.
                }
            }
        }
    }

    /**
     * Create a temporary file in a specified directory.
     * 
     * If the parent directory is null, create the temporary file directly
     * in the system temporary folder:
     * 
     * java.io.tmpdir/tempFile
     * 
     * If the parent directory is an absolute path, create the temporary file
     * within that directory:
     * 
     * parentDir/tempFile
     * 
     * If the parent directory is a relative path, create the temporary file
     * within that directory relative to the system temporary folder:
     * 
     * java.io.tmpdir/parentDir/tempFile
     *
     * See {@link File#createTempFile(String, String, File)}.
     *
     * @param prefix A prefix to put on the temporary file name.
     *     May be null.
     * @param suffix A suffix to put on the temporary file name.
     *     May be null.
     * @param parent The path of the parent of the temporary file.  May be null.
     *     May be an absolute or a relative path.
     * 
     * @return A new temporary file.
     *
     * @throws IOException Thrown if the temporary file could not be created. 
     */
    public static File createTempFile(String prefix, String suffix, String parent)
        throws IOException {
        return File.createTempFile( prefix, suffix, resolveParent(parent) );
    }

    public static final String SYSTEM_TEMP = System.getProperty("java.io.tmpdir");

    /**
     * Resolve a path either as an absolute file or relative to the system
     * temporary folder.
     *
     * If the path is absolute, use the path as given.
     * 
     * If the path is not absolute, use the path  relative to the system
     * temporary location.
     *
     * Often, the system temporary location is specified using an absolute
     * path, in which case the resolved path will be absolute.  However,
     * since the system temporary location need not be absolute, the resolved
     * path need not be absolute.
     *
     * Answer null if the path is null.
     *
     * @param path The path to resolve.
     *
     * @return The resolved path (as a file).
     */
    private static File resolveParent(String path) {
        if ( path == null ) {
            return null;
        }

        File resolved = new File(path);
        if ( !resolved.isAbsolute() ) {
            resolved = new File(SYSTEM_TEMP, path);
            if ( !resolved.exists() ) {
                if ( !resolved.mkdirs() ) {
                    // This will probably lead to an NPE.
                    // A thrown exception might be better.
                    return null;
                }
            }
        }
        
        return resolved;
    }

    /**
     * Tell if two given files have an ancestor-descendant relationship.
     *
     * @param child The candidate descendant file.
     * @param parent The candidate ancestor file.
     *
     * @return True or false telling if the candidate descendant is
     *     a descendant of the candidate ancestor.
     *
     * See {@link URI#relativize(URI)}.
     * 
     * @deprecated Use instead {@link #isUnderDirectory(URI, URI)}.
     */
    @Deprecated
    public static boolean isUnderDirectory(File child, File parent) {
        if ( (child == null) || (parent == null) ) {
            return false;
        }

        // TODO: Use of URI's is expensive.  Most especially,
        //       do not use this in a loop, as each invocation
        //       will re-obtain the absolute paths of the two
        //       files.

        URI childUri = child.toURI();
        URI parentUri = parent.toURI();
        URI relativeUri = parentUri.relativize(childUri);
        return ( relativeUri != (childUri) );

    }
    
    /**
     * Tell if two given file URIs have an ancestor-descendant relationship.
     *
     * @param childUri The URI of the candidate descendant file.
     * @param parentUri The URI of the candidate ancestor file.
     *
     * @return True or false telling if the candidate descendant is
     *     a descendant of the candidate ancestor.
     *
     * See {@link URI#relativize(URI)}.
     */
    public static boolean isUnderDirectory(URI childUri, URI parentUri) {
        URI relativeUri = parentUri.relativize(childUri);
        return ( relativeUri != (childUri) );
    }    

    /**
     * Create a target directory.  Tell if the directory was
     * created.
     *
     * Do nothing and answer false if the target
     * already exists.
     *
     * Do not create parent directories of the target directory.
     *
     * No error occurs if the target already exists and is not
     * a directory.
     *
     * @param dir The directory which is to be created.
     *
     * @return True or false telling if the directory did not
     *     exist and was created.
     */
    public static boolean createDir(File dir) {
        return ( !dir.exists() && dir.mkdir() );
    }

    /**
     * Convert the file path string to a file.
     * 
     * Replace symbols in the path using symbol definitions
     * in the bootstrap configuration.
     * 
     * When the path is a relative path, adjust the path to
     * be relative to the server directory. (See 
     * {@link com.ibm.ws.kernel.boot.BootstrapConfig#getConfigFile(String)}
     * and {@link BootstrapConfig#getSeverFile(filePath)}.
     * 
     * Throw an {@link IllegalArgumentException} if an undefined symbol
     * is present in the path.
     *
     * @param path The path which is to be converted.
     * @param bootProps Symbol table, including the server folder
     *     location.
     *
     * @return The path turned into a file, with symbols resolved, and
     *     adjusted to be relative to the server folder.
     *
     * @throws IllegalArgumentException If the path contains an undefined
     *     symbol.
     */
    public static File convertPathToFile(String path, BootstrapConfig bootProps) {
        if ( path == null ) {
            throw new NullPointerException();
        }

        String completedPath = bootProps.replaceSymbols(path);

        String resolvedPath = normalizeFilePath(completedPath);
        File resolvedFile = new File(resolvedPath);

        if ( !resolvedFile.isAbsolute() ) {
            resolvedFile = bootProps.getConfigFile(resolvedPath);
        }

        return resolvedFile;
    }

    /**
     * Normalize a path.
     * 
     * Replace any "\/" sequences and any "/\" sequences
     * with the file separator character.
     * 
     * If the separator character is '/', replace all '\'
     * characters with the separator character.
     *
     * @param path The path which is to be normalized.
     *
     * @return The normalized path.
     */
    private static String normalizeFilePath(String path) {
        // TODO: These two replacements are particular to this
        //       method.  Other 'normalize' type operations don't
        //       seem to ever do these replacements.  Why are they
        //       done by this method?

        if ( path.contains("\\/") ) {
            path = path.replace("\\/", File.separator);
        } else if ( path.contains("/\\") ) {
            path = path.replace("/\\", File.separator);
        }

        // The Linux platform could not get the file correctly if
        // the file separator is "\", not sure other *nix could
        // handle it correctly, so use File.separatorChar == '/' to judge

        if ( File.separatorChar == '/' ) {
            path = path.replace('\\', File.separatorChar);
        }
        return path;
    }

    /**
     * Obtain a path value which is suitable for use as a
     * ZIP archive entry name.
     * 
     * Convert a null path to an empty string.
     * 
     * Replace all '\' characters with '/', and strip
     * any leading '/'.
     *
     * @param path The path which is to be normalized.
     *
     * @return The normalized path.
     */
    public static String normalizeEntryPath(String path) {
        if ( path == null ) {
            return "";
        }
        
        int pathLength = path.length();
        if ( pathLength == 0 ) {
            return path;
        }

        char char0 = path.charAt(0);
        boolean hasLeadingSlash = ( (char0 == '\\') || (char0 == '/') );

        if ( pathLength == 1 ) {
            if ( hasLeadingSlash ) {
                return "";
            } else {
                return path;
            }
        }

        if ( hasLeadingSlash ) {
            path = path.substring(1);
        }

        path = path.replace('\\', '/');

        return path;
    }

    /**
     * Normalize a path to represent a directory.
     *
     * Convert a null path into an empty string.
     * 
     * Leave an empty path empty.
     * 
     * For all other cases, replace all '\' characters
     * with '/' characters, and add a trailing '/'
     * character if necessary.
     *
     * @param path The path which is to be normalized.
     *
     * @return The normalized path.
     */
    public static String normalizeDirPath(String path) {
        if ( path == null ) {
            return "";
        }

        int pathLength = path.length();
        if ( pathLength == 0 ) {
            return path;
        }

        // TODO: The cases are a little strange.  Why are
        //       empty paths left empty?

        path = path.replace('\\', '/');

        if ( path.charAt(pathLength - 1) != '/' ) {
            path += '/';
        }

        return path;
    }

    /**
     * On windows, normalize the drive letter of a path.
     * 
     * Simply answer the path, unchanged, if the path does
     * not start with a drive letter, which is an alphabetic
     * character followed by a colon (':').
     *
     * Normalization of the drive letter changes the drive
     * letter to upper case.  The remainder of the path is
     * unchanged.
     *
     * @param path A path which is to be normalized.
     *
     * @return The normalized path.
     */
    public static String normalizePathDrive(String path) {
        if ( (File.separatorChar != '\\') ||
             (path.length() <= 1) || 
             (path.charAt(1) != ':') ) {
            return path;
        }
        
        char c0 = path.charAt(0);
        if ( (c0 < 'a') || (c0 > 'z') ) {
            return path;
        }

        return Character.toUpperCase(c0) + path.substring(1);
    }

    // ++++

    private static final boolean isSlash(char c) {
        return ( (c == '\\') || (c == '/') );
    }
    
    /**
     * Strip "." and ".." elements from a file URI.
     *
     * @param path A file URI.
     *
     * @return The file URI with "." and ".." elements removed.
     */
    public static String normalize(String path) {
        // (1) Don't handle problem schemas.
        
        // We don't want to normalize if this is not a file name. This could be
        // improved, but might involve some work.
        if ( (path.startsWith("http:") || (path.startsWith("https:")) || (path.startsWith("ftp:"))) ) {
            return path;
        }

        // (2) Deal with problem prefixes.

        // Remove problem prefixes from the file URI.  These will be
        // added back after normalizing the rest of the URI.

        String prefix;
        int prefixLen;
        boolean slashChange;

        int pathLen = path.length();

        if ( (pathLen >= 9) && path.startsWith("file:////") ) {
            prefix = "file://"; // Note that "//" left on the path.
            prefixLen = 7;
            path = path.substring(7);
            pathLen -= 7;
            slashChange = true;
        } else if ( (pathLen >= 9) && path.startsWith("file:///") ) {
            prefix = "file:///";
            prefixLen = 8;
            path = path.substring(8);
            pathLen -= 8;
            slashChange = false;
        } else if ( (pathLen >= 6) && path.startsWith("file:/") && (path.charAt(7) != '/') ) {
            prefix = "file:/";
            prefixLen = 6;
            path = path.substring(6);
            pathLen -= 6;
            slashChange = false;
        } else {
            // Note that a prefix of "file://" is left on the path. 
            prefix = "";
            prefixLen = 0;
            slashChange = false;            
        }

        // (3) Count the elements while detecting windows slashes,
        // extra slashes, and "." or ".." elements.

        int numElements = 0;

        int scanCharNo = 0;

        boolean doSlashify = false;
        boolean hasDotSegments = false;
        int justAfterLastSlash = 0;

        for ( ; scanCharNo < pathLen; scanCharNo++ ) {
            char c = path.charAt(scanCharNo);

            if ( (scanCharNo == 0) && (c == '.') ) {                
                hasDotSegments = true;
            }

            // Only do something upon encountering a slash.

            if ( !isSlash(c) ) {
                continue;
            }
            
            if ( c == '\\' ) {
                // Will need to slashify, but only if this is not a windows
                // leading slash.
                if ( (scanCharNo > 0) || (pathLen <= 3) || (path.charAt(2) != ':') ) {
                    doSlashify = true;
                }
            }

            if ( scanCharNo == 0 ) {
                // Skip leading slashes, which are present in UNC paths.
                // E.g., "\\remote\location".

                while ( (scanCharNo + 1 < pathLen) && isSlash(path.charAt(scanCharNo + 1)) ) {
                    scanCharNo++;
                }
                if ( scanCharNo > 0 ) {
                    slashChange = true;
                }

                numElements++;
                
            } else if ( scanCharNo == justAfterLastSlash ) {
                // Not at the beginning, and this slash immediately follows another.
                // This slash will be removed.
                slashChange = true;

            } else {
                // This slash immediately follows a non-slash character.
                numElements++;
            }

            if ( !hasDotSegments ) {
                if ( (scanCharNo + 1 < pathLen) && (path.charAt(scanCharNo + 1) == '.') ) {
                    hasDotSegments = true;
                }
            }

            justAfterLastSlash = scanCharNo + 1;
        }

        if ( scanCharNo > justAfterLastSlash ) {
            // Characters trailing the last slash.
            numElements++;
        }

        if ( (pathLen > 3) && isSlash(path.charAt(0)) && (path.charAt(2) == ':') ) {
            // Remove the leading slash on windows. "/c:/windows/path"            
            path = path.substring(1);
            pathLen--;
        }

        if ( doSlashify ) {
            path = slashify(path);
        }
        
        if ( !slashChange && !hasDotSegments ) {
            return ( (prefixLen == 0) ? path : prefix + path );
        }

        boolean pathChanged = false;

        List<String> segments = new ArrayList<String>(numElements);
        
        justAfterLastSlash = 0;
        for ( int charNo = 0; charNo < pathLen; charNo++ ) {
            if ( path.charAt(charNo) != '/' ) {
                continue;
            }
            
            if ( charNo == 0 ) {
                // Add leading slashes as a single element.
                while ( path.charAt(charNo + 1) == '/' ) {
                    charNo++;
                }
                segments.add( path.substring(0, charNo + 1) );
            } else if ( charNo == justAfterLastSlash ) {
                // Extra slashes in the middle of the path.  Ignore these.
                pathChanged = true;
            } else {
                segments.add( path.substring(justAfterLastSlash, charNo + 1) );
            }
            justAfterLastSlash = charNo + 1; // increment beyond the slash character
        }
        if ( scanCharNo > justAfterLastSlash ) {
            segments.add( path.substring(justAfterLastSlash) );
        }
        
        int segmentNo = segments.size();
        while ( segmentNo > 0 ) {
            segmentNo--;
            String segment = trimSlash( segments.get(segmentNo) );

            if ( segment.equals("..") ) {
                if ( segmentNo == 0 ) {
                    // Leave any leading "..".
                } else { 
                    int prevSegmentNo = segmentNo - 1;
                    String prevSegment = trimSlash( segments.get(prevSegmentNo) );
                    if ( prevSegment.equals(".") ) {
                        // "./.." becomes ".."
                        pathChanged = true;
                        segments.remove(prevSegmentNo);
                    } else if ( prevSegment.equals("..") || isSymbol(prevSegment) ) {
                        // Leave "../.." or "${blah}/.."
                    } else {
                        // Remove "blah/.."
                        pathChanged = true;
                        segments.remove(segmentNo);
                        segments.remove(prevSegmentNo);
                    }

                    // with i-- we should see the .. again on the next round which we need for ../..
                    if ( segments.size() == prevSegmentNo ) {
                        segmentNo = prevSegmentNo;
                    }
                }

            } else if (segment.equals(".")) {
                // Remove "."
                pathChanged = true;
                segments.remove(segmentNo);

            } else {
                // Normal segment; leave it alone.
            }
        }

        if ( prefixLen == 0 ) {
            if ( !pathChanged ) {
                return path;
            }
        }

        StringBuilder sb = new StringBuilder( prefixLen + path.length() );
        sb.append(prefix);
        if ( pathChanged ) {
            for ( String segment : segments ) {
                sb.append(segment);
            }
        } else {
            sb.append(path);
        }
        return sb.toString();
    }

    // ++++

    /**
     * Test if a path is absolute.<p>
     * Returns true if the path is an absolute one.<br>
     * Eg. c:/wibble/fish, /wibble/fish, ${wibble}/fish
     *
     * @param normalizedPath
     * @return true if absolute, false otherwise.
     */
    @Trivial
    public static boolean pathIsAbsolute(String normalizedPath) {
        // Absolute path with leading /
        if (normalizedPath.length() > 0 && normalizedPath.charAt(0) == '/')
            return true;

        // Absolute path with symbolic
        if (isSymbol(normalizedPath))
            return true;

        if (normalizedPath.contains(":")) {
            // Absolute windows path with leading c:/
            if (normalizedPath.length() > 3 && normalizedPath.charAt(1) == ':' && normalizedPath.charAt(2) == '/')
                return true;

            // Absolute path with scheme, e.g. file://whatever
            Matcher m = ABSOLUTE_URI.matcher(normalizedPath);
            if (m.matches())
                return true;
        }

        return false;
    }

    @Trivial
    private static String trimSlash(String segment) {
        int len = segment.length();
        if ( len == 0 ) {
            return segment;
        } else {
            int last = len - 1;
            if ( segment.charAt(last) != '/' ) {
                return segment;
            } else {
                return segment.substring(0, last);
            }
        }
    }

    /**
     * Convert \ separators into /'s
     *
     * @param path path to process, must not be null.
     * @return filePath with \'s converted to /.
     */
    @Trivial
    public static String slashify(String path) {
        return path.replace('\\', '/');
    }

    /**
     * Result from cleaning a server directory.
     */
    private static enum CleanResult {
        /**
         * One or more deletes failed.  The root delete was either
         * not attempted, or failed.
         */
        FAILED,
        /**
         * All deletes, including the root delete, were successful.
         */
        SUCCESS_EMPTY,
        /**
         * One or more delete was skipped.  Any parent directory
         * deletes were not attempted, and the root delete was not
         * attempted.
         */
        SUCCESS_NONEMPTY;
    }

    private static final boolean BENEATH_WORK = true;
    
    private static CleanResult cleanServer(boolean beneathWork, File target) {
        CleanResult targetResult;

        if ( isDirectory(target) ) {
            targetResult = CleanResult.SUCCESS_EMPTY;
            File[] children = target.listFiles();
            if ( children != null ) {
                boolean isWork = isWorkParent( target.getName() );
                for ( File child : children ) {
                    CleanResult childResult = cleanServer(isWork, child);
                    if ( targetResult != CleanResult.FAILED ) {
                        // Nothing to do if there is alreay an overall failure.
                        if ( (childResult == CleanResult.FAILED) ) {
                            // Promote a child failure to an overall failure.
                            targetResult = CleanResult.FAILED;
                        } else if ( childResult == CleanResult.SUCCESS_NONEMPTY ) {
                            // Promote a non-empty result over an empty result.
                            targetResult = childResult;
                        }
                    }
                }
            }

            // targetResult == FAILED
            //   one or more of the children could not be deleted
            // targetResult == SUCCESS_EMPTY
            //   all children were deleted
            // targetResult == SUCCESS_NONEMPTY
            //   all necessary children were deleted;
            //   at least one child was not deleted

            if ( targetResult == CleanResult.SUCCESS_EMPTY ) {
                if ( !delete(target) ) {
                    targetResult = CleanResult.FAILED;
                }
            }

        } else {
            if ( beneathWork && isWorkChild( target.getName() ) ) {
                targetResult = CleanResult.SUCCESS_NONEMPTY;
            } else {
                if ( delete(target) ) {
                    targetResult = CleanResult.SUCCESS_EMPTY;
                } else {
                    targetResult = CleanResult.FAILED;
                }
            }
        }

        return targetResult;
    }
    
    /**
     * Recursively clean a server folder.
     *
     * Remove all files and directories, except for runtime
     * ".sLock" and ".sRunning" files under "workarea" or
     * "workarea-utils".  (Do not remove any directory above
     * one of these files.)
     *  
     * Answer true if the target is null, or does not exist.
     *
     * @param target The directory which is to be cleaned.
     *
     * @return True or false telling if all non-runtime files
     *     and directories were deleted.
     */
    public static boolean recursiveClean(File target) {
        if ( target == null ) {
            return true;
        } else if ( !exists(target) ) {
            return true;
        } else {
            return ( cleanServer(!BENEATH_WORK, target) != CleanResult.FAILED );
        }
    }

    // Detect:
    //     "workarea" or "workarea-utils" as parent
    // together with
    //     ".sLock" or ".sRunning" as child

    private static boolean isWorkParent(String name) {
        return ( BootstrapConstants.LOC_AREA_NAME_WORKING.equals(name) ||
                 BootstrapConstants.LOC_AREA_NAME_WORKING_UTILS.equals(name) );
    }

    private static boolean isWorkChild(String name) {
        return ( BootstrapConstants.S_LOCK_FILE.equals(name) ||
                 BootstrapConstants.SERVER_RUNNING_FILE.equals(name) );
    }
        
    private static boolean exists(File file) {
        Boolean exists = AccessController.doPrivileged((PrivilegedAction<Boolean>) (() -> { 
            return ( file.exists() ? Boolean.TRUE : Boolean.FALSE ); }));
        return exists.booleanValue();
    }

    private static Boolean isDirectory(File file) {
        Boolean isDir = AccessController.doPrivileged((PrivilegedAction<Boolean>) (() -> { 
            return ( file.isDirectory() ? Boolean.TRUE : Boolean.FALSE ); }));
        return isDir.booleanValue();
    }

    private static Boolean delete(File file) {
        Boolean didDelete = AccessController.doPrivileged((PrivilegedAction<Boolean>) (() -> { 
            return ( file.delete() ? Boolean.TRUE : Boolean.FALSE ); }));
        return didDelete.booleanValue();
    }
    
    /**
     * Create a file using the path of a URL.
     * 
     * The URL must have no authority, or must have the "file"
     * authority.  Unpredictable values will be obtained if the
     * URL does not have a file URL.
     *
     * The path of the file which is return has a normalized
     * (upper case) drive letter.
     *
     * @param url A file URL.
     *
     * @return A file which has the path of the URL.
     */
    public static File getFile(URL url) {
        String path;
        
        try {
            // The URL for a UNC path is file:////server/path, but the
            // deprecated File.toURL() as used by java -jar/-cp incorrectly
            // returns file://server/path/, which has an invalid authority
            // component.  Rewrite any URLs with an authority ala
            // http://wiki.eclipse.org/Eclipse/UNC_Paths
            if ( url.getAuthority() != null ) {
                url = new URL("file://" + url.toString().substring("file:".length()));
            }
            path = new File(url.toURI()).getPath();
        } catch ( MalformedURLException e ) {
            path = null;
        } catch ( URISyntaxException e ) {
            path = null;
        } catch ( IllegalArgumentException e ) {
            path = null;
        }

        if ( path == null ) {
            path = url.getPath();
        }

        return new File( normalizePathDrive(path) );
    }

    /**
     * Tell if a value is for a symbol substitution.
     * 
     * The test is implemented by testing if the value starts with "${".
     * No test on the symbol name is performed, and the test does not
     * look for a closing '}' character.
     * 
     *
     * @param s The string to test.
     * @return True or false telling if the value is a string substitution.
     */
    @Trivial
    public static boolean isSymbol(String s) {
        return ( (s.length() > 3) && (s.charAt(0) == '$') && (s.charAt(1) == '{') );
    }

    /**
     * Tell if a value embeds a symbolic substitution.
     * 
     * A symbolic substitution is a sequence "${}" with
     * an identifier within the braces.
     * 
     * The test is imprecise: Syntactically non-valid
     * substitutions will be accepted, for example, "${{}"
     * or "${$}".
     *
     * Answer null if the value is null.
     *
     * @param value The value which is to be tested.
     *
     * @return True or false telling if a symbolic substitution
     *     is present in the value.
     */
    // TODO: Test me.
    @Trivial
    public static boolean containsSymbol(String value) {
        if ( value == null ) {
            return false;
        }

        // Looking for "${c}"

        int length = value.length();
        if (length < 4) {
            return false; // Need at least 4 characters
        }

        int startPos = value.indexOf('$');
        if (startPos < 0) {
            return false; // No '$' was found.
        }
        startPos++; // Skip the '$'

        if ( (length - startPos) < 3 ) {
            return false; // Need at least 3 more characters.
        }

        if ( value.charAt(startPos) != '{') {
            return false; // No following '{'.
        }
        startPos += 2; // Skip the '{' and the first identifier char

        int endPos = value.indexOf('}', startPos);

        return ( endPos > 0 ); // Was the '}' found.
    }

    /**
     * Fully read a file.  Answer the read bytes as a string
     * using the default character set.
     *
     * Answer null if the file is null, or does not exist, or
     * cannot be read.
     * 
     * Answer null if an exception occurs while reading the file.
     *
     * @param inputFile The file which is to be read.
     *
     * @return The bytes of the file as a string.
     */
    @Trivial
    public static String readFile(File inputFile) {
        if ( (inputFile == null) || !inputFile.exists() || !inputFile.canRead() ) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try ( InputStream inputStream = new FileInputStream(inputFile) ) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int count = -1;
            while ( (count = inputStream.read(buffer)) > 0 ) {
                outputStream.write(buffer, 0, count);
            }
        } catch ( Exception e ) {
            return null;
        }

        return outputStream.toString();
    }
}
