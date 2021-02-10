/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.ws.kernel.boot.archive.DirPattern.PatternStrategy;

/**
 * Security sensitive directory configuration.
 * 
 * This class specializes {@link DirEntryConfig}, stripping sensitive information
 * from files which added by {@link #configure(Archive)}.
 *
 * This specialized directory configuration is currently used when creating
 * server dumps, and is used on files in the server configuration folder. 
 * 
 * See com.ibm.ws.kernel.boot.internal.commands.DumpProcessor.createDumpConfigs(String).
 */
public class FilteredDirEntryConfig extends DirEntryConfig {

    /**
     * Create a directory configuration.  This will be used to add
     * a directory and select elements of that directory to an archive.
     *    
     * In addition to filtering as performed by the superclass, rewrite
     * select files to to remove sensitive information.
     * 
     * See {@link #configure(Archive)}.
     *
     * @param source The target directory.  The target directory must exist and must
     *     be a directory.
     * @param includeByDefault Control parameter: Whether directory elements
     *     are included by default, or are excluded by default.
     * @param selectionStrategy Control parameter: Whether explicit selections
     *     take precedence over explicit rejections, or whether explicit
     *     rejections take precedence over explicit selections.
     */    
    public FilteredDirEntryConfig(File source, boolean includeByDefault, PatternStrategy strategy) {
        super("", source, includeByDefault, strategy);
    }

    //

    private static final String OBSCURE_REGEX =
        "\"(\\{aes\\}|\\{xor\\}).*\"";
    private static final Pattern OBSCURE_PATTERN =
        Pattern.compile(OBSCURE_REGEX);
    private static final String OBSCURE_REPLACEMENT =
        "\"*****\"";

    // TODO: Should this account for spaces before or after the key?
    private static final String WLP_PASSWORD_STRING =
        "wlp.password.encryption.key";
    private static final String WLP_PASSWORD_REGEX =
        WLP_PASSWORD_STRING + "=.*$";
    private static final Pattern WLP_PASSWORD_PATTERN =
        Pattern.compile(WLP_PASSWORD_REGEX, Pattern.MULTILINE);
    private static final String WLP_PASSWORD_REPLACEMENT =
        WLP_PASSWORD_STRING + "=*****";

    /**
     * Main API: Add the filtered contents of the target
     * directory.
     * 
     * This specialization rewrites target files, blanking
     * out sensitive information.
     * 
     * Note that {@link Archive#addDirEntry(String, File, List)}
     * is not used.  Instead, files are individually added
     * using {@link Archive#addFileEntry(String, File)}.  That
     * enables the addition of the modified source files, which
     * are written to a temporary file before being added.
     * 
     * @param archive The archive to which to add entries.
     */
    @Override
    public void configure(Archive archive) throws IOException {
        List<String> selections = filterDirectory(); // throws IOException

        for ( String relPath : selections ) {
            File initialFile = new File(source, relPath);
            if ( initialFile.isDirectory() ) {
                continue;
            }

            Path initialPath = initialFile.toPath();

            // TODO: This relies on the use of a default
            //       character set.  This could be a problem
            //       if UTF-8 is assumed and the environment
            //       default is different.

            String initialContents =
                new String( Files.readAllBytes(initialPath) );
            // 'readAllBytes' throws IOException

            String pass1Contents = OBSCURE_PATTERN
                .matcher(initialContents)
                .replaceAll(OBSCURE_REPLACEMENT);
            String pass2Contents = WLP_PASSWORD_PATTERN
                .matcher(pass1Contents)
                .replaceAll(WLP_PASSWORD_REPLACEMENT);

            // The cost of the (long) string equals is expected
            // to be much less than the cost of the write, and
            // modifications are expected to be infrequent.
            //
            // Also, not doing a write means not changing the
            // file contents or time stamp, and not unnecessarily
            // using temporary space, both which are greatly
            // desired.

            File finalFile;
            if ( !pass2Contents.equals(initialContents) ) {
                Path finalPath = Files.createTempFile(null, null); // throws IOException
                Files.write(finalPath, pass2Contents.getBytes()); // throws IOException
                finalFile = finalPath.toFile();
            } else {
                finalFile = initialFile;
            }

            archive.addFileEntry(relPath, finalFile); // throws IOException
        }
    }
}
