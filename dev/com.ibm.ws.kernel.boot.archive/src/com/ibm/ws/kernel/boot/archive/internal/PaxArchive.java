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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.ws.kernel.boot.Debug;

/**
 * An implementation of {@code Archive} will start a pax command
 * in a separate process to archive all file names that it reads
 * from its standard input.
 * 
 * For pax documentation, see {@link https://en.wikipedia.org/wiki/Pax_(command)}.
 *
 * <table>
 * <tr>
 *   <th>Option</th>
 *   <th></th>
 *   <th>Description</th>
 * </tr>
 * <tr>
 *   <th>(none)</th>
 *   <th>list</th>
 *   <th>Shows the archive contents.  Do not modify or extract anything.</th>
 * </tr>
 * <tr>
 *   <th>-r</th>
 *   <th>read</th>
 *   <th>Reads and extracts contents of an archive.</th>
 * </tr>
 * <tr>
 *   <th><strong>-w</strong></th>
 *   <th>write</th>
 *   <th>Creates archives or appends files to an archive</th>
 * </tr>
 * <tr>
 *   <th>rw</th>
 *   <th>copy</th>
 *   <th>Read and copy files and directory tree to a specified directory.</th>
 * </tr>
 * <tr/>
 * <tr>
 *   <td><strong>-f</strong></td>
 *   <td>archive</td>
 *   <td>The target archive.</td>
 * </tr>
 * <tr>
 *   <td><strong>-d</strong></td>
 *   <td>directory</td>
 *   <td>Do not recurse into directories which are being added to the archive.</td>
 * </tr>
 * <tr>
 *   <td><strong>-a</strong></td>
 *   <td>append</td>
 *   </td>Append to an existing archive.</td>
 * </tr>
 * <tr>
 *   <td><strong>-s repstr</strong></td>
 *   <td>replace name</td>
 *   </td>Modify the member name using the pattern specified by the replacement string.</td>
 * </tr>
 * <tr>
 *   <td><strong>-x pax</strong></td>
 *   <td>archive format</td>
 *   <td>Use the "pax" archive format.  Required on Z/OS.</td>
 * </tr>
 * </table>
 *
 * This implementation uses the options as shown below:
 * <pre>
 *   pax
 *     -wd
 *     [ -x pax ]
 *     [ -s @^@entryPrefix@ ]
 *     -f ${archiveFile}
 * </pre>
 * 
 * The '-x' (archive format) option is used only on Z/OS.
 * 
 * The '-s' (replace name) option is used only if an entry prefix is specified.
 */
public class PaxArchive extends AbstractArchive {

    public PaxArchive(File archiveFile) throws IOException {
        super(archiveFile);
    }

    /**
     * Main API: Close this archive.  That means stopping the PAX process.
     *
     * Do nothing if no entry has been added to the process, in which case
     * the PAX process was never started.
     *
     * @throws IOException Thrown if the archive could not be closed.
     *     That means, if the PAX process could not be stopped.
     */
    @Override
    public void close() throws IOException {
        synchronized ( paxProcessLock ) {        
            stopPax(); // throws IOException
        }
    }

    //
    
    /**
     * Main API: Add a single file to the archive.
     * 
     * Start (or restart) PAX, then write the absolute path of the file
     * to the PAX process standard input.
     * 
     * @param entryPath Optional path prefix.  This is removed from the name
     *     of the entry which is added to the archive.
     * @param source The source file.
     * 
     * @throws IOException Thrown if the PAX process could not be started.
     */
    @Override
    public void addFileEntry(String entryPath, File source) throws IOException {
        String sourcePath = source.getAbsolutePath();
        
        synchronized ( paxProcessLock ) {
            stopPax(); // throws IOException            
            startPax(null, entryPath, IS_FILE_NAME); // throws IOException
            paxPrint(sourcePath);
        }
    }

    /**
     * Main API: Add files of a directory into the archive.
     * 
     * Start (or restart) PAX, then write the relative paths of the
     * directory contents to the PAX process standard input.
     * 
     * @param entryPath Optional path prefix.  This is removed from the name
     *     of the entry which is added to the archive.
     * @param source The source file.
     * @param dirContents The relative paths of entries which are to be
     *     added.
     *
     * @throws IOException Thrown if the PAX process could not be started.
     */    
    @Override
    public void addDirEntry(String entryPath, File source, List<String> dirContent) throws IOException {
        synchronized ( paxProcessLock ) {
            stopPax(); // throws IOException
            startPax(source, entryPath, !IS_FILE_NAME); // throws IOException
            paxPrint(dirContent);
        }
    }
    
    //
        
    private static String paxCommand;
    private static volatile boolean isSetPaxCommand;

    protected static class PaxCommandLock {
        // Empty
    }
    private static final PaxCommandLock paxCommandLock = new PaxCommandLock();

    /**
     * Determine the path of the PAX command.
     * 
     * Test '/bin/pax' and '/usr/bin/pax', in that order.
     * 
     * @return The PAX command path.  Null if not found.
     */
    private static String getPaxCommand() {
        if ( !isSetPaxCommand ) {
            synchronized( paxCommandLock ) {
                if ( !isSetPaxCommand ) {
                    paxCommand = setPaxCommand();
                    isSetPaxCommand = true;
                }
            }
        }
        return paxCommand;
    }

    /**
     * Compute the PAX command path.  Look in '/bin/pax' and '/usr/bin/pax',
     * in that order.
     * 
     * @return The PAX command path.  Null if not found.
     */
    private static String setPaxCommand() {
        for ( String candidate : Arrays.asList("/bin/pax", "/usr/bin/pax") ) {
            File pax = new File(candidate);
            if ( pax.exists() ) {
                return candidate;
            }
        }
        return null;
    }  

    //

    private static final boolean isZOS = ( "z/OS".equalsIgnoreCase(System.getProperty("os.name")) );

    /**
     * Tell if the current OS is Z/OS.
     * 
     * When the current OS is Z/OS, the archive format is set to "pax".
     *
     * @return True or false telling if the current OS is Z/OS.
     */
    public static boolean isZOS() {
        return isZOS;
    }

    //

    private static final boolean IS_FILE_NAME = true;

    /**
     * Prepare parameters for launching the PAX process.
     *
     * Append to an existing archive file.
     *
     * @param entryPath Option entry path.  This is removed from the names of entries
     *     which are added to the archive.
     * @param isFileName Control parameter: Are the parameters for adding a single file
     *     or a collection of files relative to a directory.
     * @param archiveAbsolutePath The absolute path to the archive.
     * @param archiveFile The archive file.
     *
     * @return Parameters used to launch a PAX process.
     *
     * @throws IOException Thrown if the parameters cannot be prepared.  This
     *     implementation throws an exception only if the PAX command was not
     *     located.
     */    
    private static List<String> preparePaxParameters(
            String entryPath, boolean isFileName,
            String archiveAbsolutePath, File archiveFile) throws IOException {

        String usePaxCommand = getPaxCommand();
        if ( usePaxCommand == null ) {
            throw new IOException("pax is not in /bin or /usr/bin");
        }            

        // Build the pax command:
        //
        // <paxCommand>
        //   -wd
        //   [ -x pax ]
        //   [ -s @^@entryPrefix@ ]
        //   [ -a ]
        //   -f ${archiveAbsolutePath}

        List<String> args = new ArrayList<String>();
        args.add(usePaxCommand);
        args.add("-wd");

        if ( isZOS() ) {
            args.add("-x"); // On Z/OS: Set the archive format to PAX.
            args.add("pax");
        }

        // Use 'ed' style regex to handle the entry path
        if ( (entryPath != null) && !entryPath.isEmpty() ) {
            args.add("-s");
            if ( isFileName ) {
                args.add("@.*@" + entryPath + "@");
            } else { // is entry prefix
                args.add("@^@" + entryPath + "@");
            }
        }

        if ( archiveFile.exists() ) {
            args.add("-a"); // Append to the archive.
        }

        args.add("-f"); // Set the target archive.
        args.add(archiveAbsolutePath);

        return args;
    }

    /** The spawned PAX process. */
    private Process paxProcess;

    /** Stream for sending data to the PAX process standard input. */
    private PrintStream paxStdin;

    /**
     * Print a single path the PAX standard input.
     * 
     * That triggers the PAX process to add an entry and its contents
     * to the archive.
     * 
     * @param path A path to a file which is to be added to the archive. 
     */
    private void paxPrint(String path) {
        // Requires a scope that guarantees pax standard input.
        paxStdin.println(path);
    }
    
    /**
     * Print multiple paths to the PAX standard input.
     * 
     * That triggers the PAX process to add an entry and its contents
     * to the archive for each of the paths.
     * 
     * @param paths Paths to files which is to be added to the archive. 
     */
    private void paxPrint(List<String> paths) {
        // Requires a scope that guarantees pax standard input.
        for ( String path : paths ) {
            paxPrint(path);
        }
    }
    
    protected static class PaxProcessLock {
        // EMPTY
    }

    /**
     * PAX process lock.  This is used to add thread safety to the PAX
     * process.  The PAX process is started, stopped, and while running,
     * has paths sent to it through its standard input.
     */
    private final PaxProcessLock paxProcessLock = new PaxProcessLock();

    //

    /**
     * Prepare parameters for launching the PAX process.
     *
     * Add the archive absolute path and file to the method parameters.
     * See {@link #preparePaxParameters(String, boolean, String, File).
     * 
     * @param entryPath Option entry path.  This is removed from the names of entries
     *     which are added to the archive.
     * @param isFileName Control parameter: Are the parameters for adding a single file
     *     or a collection of files relative to a directory.
     *     
     * @return Parameters used to launch a PAX process.
     * 
     * @throws IOException Thrown if the parameters cannot be prepared.
     */
    private List<String> preparePaxParameters(String entryPath, boolean isFileName) throws IOException {
        return preparePaxParameters( entryPath, isFileName, getArchiveAbsolutePath(), getArchiveFile() ); 
    }

    private static final boolean AUTO_FLUSH = true;

    /**
     * Start or restart the PAX process.  Open pax standard input.
     * 
     * If the PAX process is not yet running, start it.  If the PAX process is
     * running (which will be as a result of {@link #addFileEntry(String, File)}
     * or {@link #addDirEntry(String, File, List)}, stop the PAX process, then
     * restart it.
     * 
     * @param processDir The directory out of which the PAX process runs.
     * @param entryPath Optional entry path.  This is stripped from the names
     *     of entries which are added.
     * @param isFileName Control parameter: Will a single file or a directory
     *     be added?
     *     
     * @throws IOException Thrown if the 
     */
    private void startPax(File processDir, String entryPath, boolean isFileName) throws IOException {
        // External synchronization is required.
        
        List<String> paxParms = preparePaxParameters(entryPath, isFileName); // throws IOException

        ProcessBuilder processBuilder = new ProcessBuilder(paxParms);

        processBuilder.inheritIO(); // TFB Replaces 'transfer' of standard error and standard output.

        if ( processDir != null ) {
            processBuilder.directory(processDir);
        }

        // Open ordering: paxProcess is opened, then paxStdin is opened.
        // Close ordering: paxStdin is closed, then paxProcess is closed.
        
        paxProcess = processBuilder.start(); // throws IOException
        paxStdin = new PrintStream( paxProcess.getOutputStream(), AUTO_FLUSH );

        // No file names were provided as parameters to PAX.  The PAX process
        // expects names of files which are to be added to the archive to be
        // provided through the PAX process standard input.
        //
        // AUTO_FLUSH lets PAX process each path while code loops to provide
        // the next path.
    }

    /**
     * Stop the PAX process.
     *
     * Do nothing if the PAX process is not running.
     *
     * If the pax process is running, close the process's standard
     * input, transfer output from the process's standard error and
     * standard output to this process's standard output, then wait
     * for the PAX process return code.
     * 
     * @throws IOException Thrown if the wait for the PAX process
     *     return code is interrupted.
     */    
    private void stopPax() throws IOException {
        // External synchronization is required.

        // paxStdin cannot be set if the pax process is null.
        if ( paxProcess == null ) {
            return;
        }

        // Open ordering: paxProcess is opened, then paxStdin is opened.
        // Close ordering: paxStdin is closed, then paxProcess is closed.

        try {
            if ( paxStdin != null ) {
                try {
                    paxStdin.close();
                } finally {
                    paxStdin = null;
                }
            }

            try {
                // TODO: Handling the error and input stream in this
                //       way is questionable.  The two streams are likely to
                //       have intermixed activity, possibly with either having
                //       no output for a substantial intervals.
                //
                //       Also, having the wait after transferring the streams
                //       is strange, as the streams will not likely cease
                //       output until the process completes.

                // transferStream( paxProcess.getErrorStream(), System.out ); // TFB
                // transferStream( paxProcess.getInputStream(), System.out ); // TFB
                
                // TFB: Removed 'transferStream', replaced with 'inheritIO'.
        
                // TODO: There is no timeout on the PAX process wait!
                int returnCode = paxProcess.waitFor();
                if ( returnCode != 0 ) {
                    throw new IOException( getPaxCommand() + " returned with " + returnCode );
                }

            } catch ( InterruptedException ie ) {
                throw new IOException(ie);
            }

        } finally {
            paxProcess = null;
        }
    }    
    
    //

    /**
     * Transfer lines from an input stream to an output stream.
     * 
     * Close the input stream when finished.  Do NOT close the output stream.
     * 
     * Handle any exception which occurs.  Neither a read nor a write
     * failure will be thrown as an exception.
     * 
     * @param inputStream An input stream.
     * @param outputStream An output stream.
     */
    @SuppressWarnings("unused")
    private static void transferStream(InputStream inputStream, PrintStream outputStream) {
        try {
            try {
                transferLines(inputStream, outputStream);
            } finally {
                inputStream.close();
            }

        } catch ( IOException e ) {
            Debug.printStackTrace(e);        
        }
    }
    
    /**
     * Transfer the lines of an input stream to standard output.
     * 
     * @param inputStream A stream which is to be transferred to standard output.
     */
    private static void transferLines(InputStream inputStream, PrintStream outputStream) throws IOException {
        try ( InputStreamReader isr = new InputStreamReader(inputStream);
              BufferedReader br = new BufferedReader(isr) ) {

            String line;
            while ((line = br.readLine()) != null) {
                outputStream.println(line);
            }
        }
    }
}
