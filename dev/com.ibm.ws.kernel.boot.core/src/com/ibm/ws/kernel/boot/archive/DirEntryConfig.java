/*******************************************************************************
 * Copyright (c) 2012-2020 IBM Corporation and others.
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.ws.kernel.boot.archive.DirPattern.PatternStrategy;
import com.ibm.ws.kernel.boot.internal.FileUtils;

public class DirEntryConfig implements ArchiveEntryConfig {

    /**
     * Encapsulate adding a directory to an archive.
     *
     * The directory is expected to be augmented by the addition of include patterns and by the
     * addition of exclude patterns.
     * 
     * See {@link #include(Pattern)} and {@link #exclude(Pattern)}.
     *
     * @param entryPath        The path of the directory entry in the archive.
     * @param source           The directory which is to be added.
     * @param includeByDefault Control parameter: Tells whether inclusion or exclusion has precedence.
     * @param strategy         The inclusion strategy.  Either include or exclude.
     */
    public DirEntryConfig(String entryPath, File source, boolean includeByDefault, PatternStrategy strategy) throws IOException {
        entryPath = FileUtils.normalizeEntryPath(entryPath);

        this.entryPath = FileUtils.normalizeDirPath(entryPath);

        if ( !source.exists() ) {
            throw new FileNotFoundException( source.getAbsolutePath() );
        } else if ( !source.isDirectory() ) {
            throw new IllegalArgumentException("The source [ " + source.getAbsolutePath() + " ] is not a directory.");
        }

        this.source = source;

        this.dirPattern = new DirPattern(includeByDefault, strategy);
    }

    //
    
    protected final String entryPath;
    
    @Override
    public String getEntryPath() {
        return this.entryPath;
    }

    //
    
    protected final File source;
    
    @Override
    public File getSource() {
        return this.source;
    }

    //
    
    protected final DirPattern dirPattern;
    
    public void include(Pattern pattern) {
        dirPattern.getIncludePatterns().add(pattern);
    }

    public void exclude(Pattern pattern) {
        dirPattern.getExcludePatterns().add(pattern);
    }
    
    //

    /**
     * Processing this directory configuration into the archive.
     * 
     * Collect the paths of children, then process the directory
     * and the children into the archive.
     * 
     * The directory is added even if no children are available, or
     * were selected.
     * 
     * @param archive The archive which is to receive the directory
     *     and its children.
     *     
     * @throws IOException Thrown if the directory cannot be added
     *     to the archive.
     */
    @Override
    public void configure(Archive archive) throws IOException {
        List<String> childPaths = new ArrayList<String>();

        filterDirectory(childPaths, dirPattern, "");

        archive.addDirEntry(entryPath, source, childPaths);
    }

    /**
     * Recursively filter the source using the supplied patterns.
     *
     * @param childPaths Collected child paths.
     * @param pattern The pattern to apply to children of the directory.
     * @param currentDirPath The path to the current directory which is being processed.
     * 
     * @throws IOException Thrown if file processing fails.
     */
    protected void filterDirectory(List<String> childPaths, DirPattern pattern, String currentDirPath) throws IOException {
        File currentDir = new File(source, currentDirPath);
        if ( !currentDir.exists() ) {
            return;
        }
        
        File[] children = currentDir.listFiles();
        if ( children == null ) {
            return;
        }
        
        for ( File child : children ) {
            if ( pattern.include(child) ) {
                childPaths.add( currentDirPath + child.getName() );
            }

            if ( child.isDirectory() ) {
                filterDirectory(childPaths, pattern, currentDirPath + child.getName() + "/");
            }
        }
    }
}
