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
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.ws.kernel.boot.archive.DirPattern.PatternStrategy;
import com.ibm.ws.kernel.boot.internal.BootstrapConstants;
import com.ibm.ws.kernel.boot.internal.FileUtils;

public class DirEntryConfig implements ArchiveEntryConfig {
    /**
     * Answer pattern which exactly matches a specified path.
     * Quote the path and compile it into a pattern.
     *
     * @param path A path for which to make a pattern.
     *
     * @return The pattern which matches the path exactly.
     */
    public static Pattern pathAsPattern(String path) {
        return Pattern.compile( Pattern.quote(path) );
    }

    /**
     * Answer the pattern for a specified regular expression.
     *
     * @param regEx A regular expression.
     *
     * @return The pattern for the regular expression.
     */
    public static Pattern expressionAsPattern(String regEx) {
        return Pattern.compile(regEx);
    }
    
    /**
     * Obtain the directory entry path for a specified entry path.
     * 
     * Normalize the path, converting all slashes to forward slashes,
     * and removing any leading slash.  Then, make sure the path
     * ends with a slash.
     *
     * @param entryPath The entry path for which to obtain a normalized
     *     directory entry path.
     *
     * @return The entry path as a normalized directory entry path.
     */
    public static String asDirEntryPath(String entryPath) {
        return FileUtils.normalizeDirPath( FileUtils.normalizeEntryPath(entryPath) );
    }

    /**
     * Convert a file to one which has an absolute path.
     *
     * @param file A file for which to obtain an absolute file.
     *
     * @return The file having an absolute path.
     */
    public static File asAbsFile(File file) {
        String path = file.getPath();
        String absPath = file.getAbsolutePath();

        return ( path.equals(absPath) ? file : new File(absPath) );
    }
    
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

        this.entryPath = asDirEntryPath(entryPath);
        this.source = asAbsFile(source);

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

    public void include(File file) {
        String path = DirPattern.normalize(file);
        include( pathAsPattern(path) );
    }

    public void includeExpression(String regEx) {
        include( expressionAsPattern(regEx) );
    }
    
    public void includeExpressions(String ... expressions) {
        for ( String regEx : expressions ) {
            includeExpression(regEx);
        }
    }
    
    public void exclude(Pattern pattern) {
        getDirectoryPattern().addExcludePattern(pattern);
    }

    public void exclude(File file) {
        String path = DirPattern.normalize(file);
        exclude( pathAsPattern(path) );
    }

    public void excludeExpression(String regEx) {
        exclude( expressionAsPattern(regEx) );
    }
    
    public void excludeExpressions(String ... expressions) {
        for ( String regEx : expressions ) {
            exclude( expressionAsPattern(regEx) );
        }
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
     *
     * @throws IOException Thrown if the target source does not exist,
     *     is not a simple file, or could not be added.
     */
    protected List<String> filterDirectory() throws IOException {
        if ( !source.exists() ) {
            throw new IOException( BootstrapConstants.format("error.missing.loose.file", source.getAbsolutePath()) );            
        } else if ( !source.isDirectory() ) {
            throw new IOException( BootstrapConstants.format("error.nondirectory.loose.file", source.getAbsolutePath()) );

        } else {
            String sourcePath = DirPattern.normalize(source);

            List<String> selections = new ArrayList<String>();
            filterDirectory(selections, source, sourcePath, "");
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
     * Both the target absolute path and the target relative path
     * are needed: Absolute paths are used for matching, while
     * relative paths are put into the selections.
     *
     * @param selections The paths of selected files.
     * @param targetDir The directory from which to select files.
     * @param targetAbsPath The absolute path of the target directory.
     * @param targetRelPath The relative path of the target directory.
     */
    protected void filterDirectory(
        List<String> selections,
        File targetDir, String targetAbsPath, String targetRelPath) {        

        String prefix = "filterDirectory: ";

        System.out.println(prefix + "Rel [ " + targetRelPath + " ]");
        System.out.println(prefix + "Abs [ " + targetAbsPath + " ]");

        DirPattern usePattern = getDirectoryPattern();
        System.out.println(prefix +
            "Strategy: " + (usePattern.isIncludePreference() ? "include" : "exclude"));

        File[] children = targetDir.listFiles();
        if ( children == null ) {
            System.out.println(prefix + "Ignore: Null listing");
            return; // Strange, but still nothing to do
        }

        for ( File child : children ) {
            String childName = child.getName();
            boolean isDirectory = child.isDirectory();

            String childAbsPath;
            String childRelPath;
            if ( isDirectory ) {
                childAbsPath = targetAbsPath + childName + '/';
                childRelPath = targetRelPath + childName + '/';
            } else {
                childAbsPath = targetAbsPath + childName;
                childRelPath = targetRelPath + childName;
            }

            if ( usePattern.select(childAbsPath) ) {
                System.out.println(prefix + "Select: " + childAbsPath);
                selections.add(childRelPath);
            } else {
                System.out.println(prefix + "Reject: " + childAbsPath);
            }

            if ( isDirectory ) {
                filterDirectory(selections, child, childAbsPath, childRelPath);
            }
        }
    }
}
