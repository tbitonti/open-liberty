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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.ws.kernel.boot.archive.DirPattern.PatternStrategy;
import com.ibm.ws.kernel.boot.internal.FileUtils;

public class DirEntryConfig implements ArchiveEntryConfig {
    /**
     * Create a directory configuration.  This will be used to add
     * a directory and select elements of that directory to an archive.
     * 
     * See {@link #configure(Archive)}.
     *
     * The meaning of the parameters is per
     * {@link Archive#addDirEntry(String, File, List)}.
     * 
     * @param entryPath The path to the directory.
     * @param source The target directory.  The target directory must exist and must
     *     be a directory.
     * @param includeByDefault Control parameter: Whether directory elements
     *     are included by default, or are excluded by default.
     * @param selectionStrategy Control parameter: Whether explicit selections
     *     take precedence over explicit rejections, or whether explicit
     *     rejections take precedence over explicit selections.
     */
    public DirEntryConfig(
        String entryPath,
        File source,
        boolean includeByDefault,
        PatternStrategy selectionStrategy) {

        // TFB: Removed the source validation.  Do that
        //      check when performing configuration.

        this.entryPath = FileUtils.normalizeDirPath( FileUtils.normalizeEntryPath(entryPath) );
        this.source = source;

        this.dirPattern = new DirPattern(includeByDefault, selectionStrategy);
    }

    //

    protected final String entryPath;
    
    @Override
    public String getEntryPath() {
        return entryPath;
    }

    protected final File source;
    
    @Override
    public File getSource() {
        return source;
    }

    protected final DirPattern dirPattern;

    public DirPattern getDirectoryPattern() {
        return dirPattern;
    }
    
    public void include(Pattern pattern) {
        getDirectoryPattern().addIncludePattern(pattern);
    }

    public void exclude(Pattern pattern) {
        getDirectoryPattern().addExcludePattern(pattern);
    }

    //

    /**
     * Main API: Select elements of the target directory
     * and add them to the archive.
     * 
     * See {@link Archive#addDirEntry(String, File, List)}.
     *
     * @param archive The archive which is to receive the directory entry.
     */
    @Override
    public void configure(Archive archive) throws IOException {
        archive.addDirEntry( entryPath, source, filterDirectory() );
    }

    /**
     * Recursively select files of the target directory.  Answer the relative
     * paths of the selected files.  Paths are relative to the source directory.
     * 
     * @return The selected paths.
     */
    protected List<String> filterDirectory() throws IOException {
        String prefix = "filterDirectory: ";

        if ( !source.exists() ) {
            System.out.println(prefix + "Ignore: " + source.getAbsolutePath() + " does not exist");
            return Collections.emptyList();
        } else if ( !source.isDirectory() ) {
            System.out.println(prefix + "Ignore: " + source.getAbsolutePath() + " is not a directory");
            return Collections.emptyList();

        } else {
            List<String> selections = new ArrayList<String>();
            filterDirectory(selections, source, "");
            return selections;
        }
    }

    /**
     * Recursively select files of the target directory.  Add
     * selected files to the selections collection.
     *
     * The target relative path must be empty, or must end with
     * a forward slash ('/').
     *
     * @param selections The paths of selected files.
     * @param targetDir The directory from which to select files.
     * @param targetRelPath The accumulated relative path of
     *     files which are to be added.
     *
     * @throws IOException Thrown if there is an error listing
     *     files.
     */
    protected void filterDirectory(
        List<String> selections,
        File targetDir, String targetRelPath) throws IOException {        

        String prefix = "filterDirectory: ";
        System.out.println(prefix + targetDir.getAbsolutePath());

        File[] children = targetDir.listFiles();
        if ( children == null ) {
            System.out.println(prefix + "Ignore: Null listing");
            return; // Strange, but still nothing to do
        }

        DirPattern usePattern = getDirectoryPattern();
        System.out.println(prefix +
            "Strategy: " + (usePattern.isIncludePreference() ? "include" : "exclude"));

        for ( File child : children ) {
            boolean isDirectory = child.isDirectory();

            // The target path is either empty, or ends with a slash.

            // The path of the child is not reliable, and is not used:
            // The first child is obtained from the source folder,
            // and the path of that folder is not reliably known.

            String childRelPath;
            if ( isDirectory ) {
                childRelPath = targetRelPath + child.getName() + '/';
            } else {
                childRelPath = targetRelPath + child.getName();
            }

            // TFB: This used to perform selection on the child
            //      absolute path.  That was changed to use the child
            //      relative path.
            //
            //      The path does not need to be normalized, since it
            //      is constructed from names and forward slashes.

            if ( usePattern.select(childRelPath) ) {
                System.out.println(prefix + "Select: " + childRelPath);
                selections.add(childRelPath);
            } else {
                System.out.println(prefix + "Reject: " + childRelPath);
            }

            if ( isDirectory ) {
                filterDirectory(selections, child, childRelPath);
            }
        }
    }
}
