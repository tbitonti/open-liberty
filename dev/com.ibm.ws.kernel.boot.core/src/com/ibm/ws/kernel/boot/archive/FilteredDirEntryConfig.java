/*******************************************************************************
 * Copyright (c) 2020,2025 IBM Corporation and others.
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.ws.kernel.boot.archive.DirPattern.PatternStrategy;

/**
 * Strip security sensitive information from configuration files.
 *
 * Look for AES and XOR prefixed values; replace such values with asterisks.
 * 
 * Look for "wlp.password.encryption.key" entries and replace their values with asterisks.
 */
public class FilteredDirEntryConfig extends DirEntryConfig {

    private static final String FILTER_REGEX = "\"(\\{aes\\}|\\{xor\\}).*\"";
    private static final Pattern obscuredValuePattern = Pattern.compile(FILTER_REGEX);
    private static final String OBSCURED_VALUE = "\"*****\"";

    private static final String WLP_PASSWORD_ENCYRPTION_STRING = "wlp.password.encryption.key";
    private static final String WLP_PASSWORD_ENCRYPTION_REGEX = WLP_PASSWORD_ENCYRPTION_STRING + "=.*$";
    private static final Pattern wlpPasswordEncryptionPattern = Pattern.compile(WLP_PASSWORD_ENCRYPTION_REGEX, Pattern.MULTILINE);

    public FilteredDirEntryConfig(File source, boolean includeByDefault, PatternStrategy strategy) throws IOException {
        super("", source, includeByDefault, strategy);
    }

    // TODO: This creation of temporary files is mildly inefficient.
    //       Allowing it, for now, because filtered files are expected to be
    //       small, and small in number.

    // TODO: File modes are not preserved.

    /**
     * Override: Replace all selected files with obscured copies.
     * 
     * Ignore all directory entries.
     */
    @Override
    public void configure(Archive archive) throws IOException {
        List<String> childPaths = new ArrayList<String>();
        filterDirectory(childPaths, dirPattern, "");

        for ( String childPath : childPaths ) {
            File origFile = new File(source, childPath);
            if ( origFile.isDirectory() ) {
                continue;
            }
            
            Path obscuredChild = Files.createTempFile(null, null);

            String origContents = new String( Files.readAllBytes( origFile.toPath() ) );

            String obscuredContents = obscuredValuePattern.matcher(origContents).replaceAll(OBSCURED_VALUE);
            obscuredContents = wlpPasswordEncryptionPattern.matcher(obscuredContents).replaceAll(WLP_PASSWORD_ENCYRPTION_STRING + "=*****");

            Files.write( obscuredChild, obscuredContents.getBytes() );
            
            archive.addFileEntry( childPath, obscuredChild.toFile() );
            
            // TODO: delete the temporary file?
        }
    }
}
