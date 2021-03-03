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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.ws.kernel.boot.archive.DirPattern.PatternStrategy;
import com.ibm.ws.kernel.boot.internal.BootstrapConstants;
import com.ibm.ws.kernel.boot.internal.FileUtils;

public class DirEntryConfig implements ArchiveEntryConfig {
    
    public static DirEntryConfig includeDir(
        List<ArchiveEntryConfig> configs, String entryPrefix, File source)
        throws IOException {
        return addDir(configs, entryPrefix, source, DO_INCLUDE);
    }

    public static DirEntryConfig excludeDir(
        List<ArchiveEntryConfig> configs, String entryPrefix, File source)
        throws IOException {
        return addDir(configs, entryPrefix, source, DO_EXCLUDE);
    }

    public static DirEntryConfig includeDir(List<ArchiveEntryConfig> configs, File source)
        throws IOException {
        return addDir(configs, EMPTY_PREFIX, source, DO_INCLUDE);
    }

    public static DirEntryConfig excludeDir(List<ArchiveEntryConfig> configs, File source)
        throws IOException {
        return addDir(configs, EMPTY_PREFIX, source, DO_EXCLUDE);
    }    

    //

    public static DirEntryConfig addDir(
            List<ArchiveEntryConfig> configs,
            File source, boolean doInclude) throws IOException {
        return addDir(configs, EMPTY_PREFIX, source, doInclude);
    }

    /** Control parameter: Add entries with no prefix. */
    public static final String EMPTY_PREFIX = "";

    /** Control parameter value: Select include preference. */
    public static final boolean DO_INCLUDE = true;
    /** Control parameter value: Select exclude preference. */    
    public static final boolean DO_EXCLUDE = false;

    /**
     * Create and add a directory configuration to a list of configurations.
     *
     * @param configs Storage for the configurations.
     * @param entryPrefix The prefix to entry paths added from the directory.
     * @param source Source folder for the directory configuration.
     * @param doInclude Control parameter: Tells which preference to use
     *     for the new directory configuration.  True selects include
     *     preference.  False selects exclude preference.
     * @param doObscure Control parameter: Tells if the configuration
     *     should obscure the files which it adds.
     *
     * @return The new directory configuration.
     * 
     * @throws IOException Thrown if the source location does not exist or is not
     *     a directory.
     */
    public static DirEntryConfig addDir(
            List<ArchiveEntryConfig> configs,
            String entryPrefix, File source, boolean doInclude) throws IOException {

        PatternStrategy strategy =
            ( doInclude ? PatternStrategy.IncludePreference
                        : PatternStrategy.ExcludePreference );

        DirEntryConfig dirEntryConfig =
            new DirEntryConfig(entryPrefix, source, doInclude, strategy);            

        configs.add(dirEntryConfig);

        return dirEntryConfig;
    }    

    //

    public static FilteredDirEntryConfig includeObscuredDir(
        List<ArchiveEntryConfig> configs, File source)
        throws IOException {
        return obscureDir(configs, source, DO_INCLUDE);
    }    
    
    public static FilteredDirEntryConfig excludeObscuredDir(
        List<ArchiveEntryConfig> configs, File source)
        throws IOException{
        return obscureDir(configs, source, DO_EXCLUDE);
    }    
    
    /**
     * Create and add a directory configuration to a list of configurations.
     * Obscure any files added from the directory.
     *
     * @param configs Storage for the configurations.
     * @param source Source folder for the directory configuration.
     * @param doInclude Control parameter: Tells which preference to use
     *     for the new directory configuration.  True selects include
     *     preference.  False selects exclude preference.
     *
     * @return The new directory configuration.
     */
    public static FilteredDirEntryConfig obscureDir(
            List<ArchiveEntryConfig> configs,
            File source, boolean doInclude) throws IOException {

        PatternStrategy strategy =
            ( doInclude ? PatternStrategy.IncludePreference
                        : PatternStrategy.ExcludePreference );

        FilteredDirEntryConfig dirEntryConfig =
            new FilteredDirEntryConfig(source, doInclude, strategy);

        configs.add(dirEntryConfig);

        return dirEntryConfig;
    }    
    
    
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
     * @param entryPrefix The entry path for which to obtain a normalized
     *     directory entry path.
     *
     * @return The entry path as a normalized directory entry path.
     */
    public static String asDirEntryPath(String entryPrefix) {
        return FileUtils.normalizeDirEntryPath(entryPrefix);
    }

    /**
     * Verify that a source file is valid.  That is, the source file
     * must exist and must be a directory.
     *
     * @param source The source file which is to be tested.
     *
     * @throws IOException Thrown if the file does not exist, or is not
     *     a directory.  Thrown as a {@link FileNotFoundException} if
     *     the file does not exist.
     */    
    private static void verifySource(File source) throws IOException {
        if ( !source.exists() ) {
            throw new FileNotFoundException( BootstrapConstants.format("error.missing.loose.file", source.getAbsolutePath()) );            
        } else if ( !source.isDirectory() ) {
            throw new IOException( BootstrapConstants.format("error.nondirectory.loose.file", source.getAbsolutePath()) );
        }
    }
    
    /**
     * Create a directory configuration.  This will be used to add
     * a directory and select elements of that directory to an archive.
     *
     * Verification of the source file is performed when the configuration
     * is created.  That causes exceptions early, when configurations are
     * created.  The alternative is to perform verification later, when the
     * configuration is used to add an entry to an archive.
     *
     * See {@link #configure(Archive)}.
     *
     * The meaning of the parameters is per
     * {@link Archive#addDirEntry(String, File, List)}.
     * 
     * @param entryPrefix The path to the directory.
     * @param source The source directory.  The source must exist and must
     *     be a directory.
     * @param includeByDefault Control parameter: Whether directory elements
     *     are included by default, or are excluded by default.
     * @param selectionStrategy Control parameter: Whether explicit selections
     *     take precedence over explicit rejections, or whether explicit
     *     rejections take precedence over explicit selections.
     * 
     * @throws IOException Thrown if the source location does not exist or is not
     *     a directory.
     */
    public DirEntryConfig(
        String entryPrefix,
        File source,
        boolean includeByDefault,
        PatternStrategy selectionStrategy) throws IOException {

        verifySource(source);

        this.entryPrefix = asDirEntryPath(entryPrefix);
        this.source = source;

        // System.out.println("DirEntryConfig: Prefix (raw) [ " + entryPrefix + " ]");
        // System.out.println("DirEntryConfig: Prefix [ " + this.entryPrefix + " ]");
        // System.out.println("DirEntryConfig: Source [ " + this.source.getAbsolutePath() + " ]");

        this.dirPattern = new DirPattern(includeByDefault, selectionStrategy);
    }

    //

    protected final String entryPrefix;

    @Override
    public String getEntryPath() {
        return entryPrefix;
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

    public void include(Iterable<? extends Pattern> patterns) {
        for ( Pattern pattern : patterns ) {
            getDirectoryPattern().addIncludePattern(pattern);
        }
    }
    
    public void include(Pattern pattern) {
        getDirectoryPattern().addIncludePattern(pattern);
    }

    public Pattern include(File file) {
        String path = DirPattern.cannonize(file);
        Pattern pattern = pathAsPattern(path); 
        include(pattern);
        return pattern;
    }

    public Pattern includePath(String path) {
        return include( new File(path) );
    }
    
    public Pattern includeExpression(String regEx) {
        Pattern pattern = expressionAsPattern(regEx);
        include(pattern);
        return pattern;
    }
    
    public void includeExpressions(String ... expressions) {
        for ( String regEx : expressions ) {
            includeExpression(regEx);
        }
    }
    
    public void exclude(Iterable<? extends Pattern> patterns) {
        for ( Pattern pattern : patterns ) {
            getDirectoryPattern().addExcludePattern(pattern);
        }
    }
    
    public void exclude(Pattern pattern) {
        getDirectoryPattern().addExcludePattern(pattern);
    }

    public Pattern exclude(File file) {
        String path = DirPattern.cannonize(file);
        Pattern pattern = pathAsPattern(path);
        exclude(pattern);
        return pattern;
    }

    public Pattern excludePath(String path) {
        return exclude( new File(path) );
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
        // System.out.println("configure: Prefix [ " + entryPrefix + " ]");
        // System.out.println("configure: source [ " + source.getAbsolutePath() + " ]");

        archive.addDirEntry( entryPrefix, source, filterDirectory() );
    }

    /**
     * Recursively select files of the target directory.  Answer the relative
     * paths of the selected files, using foward slashes.  Answer paths relative
     * to the source directory.
     *
     * Files are matched on their absolute paths.  See {@link DirPattern} for
     * matching rules.  Most important: An expression matches a path if the
     * expression occurs anywhere in the path.
     *
     * @return The selected relative paths.
     *
     * @throws IOException Thrown if the target source does not exist,
     *     is not a simple file, or could not be added.
     */
    protected List<String> filterDirectory() throws IOException {
        // Cannonize the source path, and build on this to generate
        // paths to match against.  This avoids additional calls
        // to 'getAbsolutePath'.

        verifySource(source);

        String sourcePath = DirPattern.cannonize(source);

        List<String> selections = new ArrayList<String>();
        filterDirectory(selections, source, sourcePath, "");
        return selections;
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

        // System.out.println(prefix + "Rel [ " + targetRelPath + " ]");
        // System.out.println(prefix + "Abs [ " + targetAbsPath + " ]");

        DirPattern usePattern = getDirectoryPattern();
        // System.out.println(prefix +
        //     "Strategy: " + (usePattern.isIncludePreference() ? "include" : "exclude"));

        File[] children = targetDir.listFiles();
        if ( children == null ) {
            // System.out.println(prefix + "Ignore: Null listing");
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

            // TODO: Using the absolute path can result in false
            //       positives: The source path is included in the
            //       path which is tested.
            //
            //       This could be changed, but might affect customer
            //       specified patterns which use patterns that include
            //       portions of the source path.
            //
            //       To minimize false matches, files and paths which
            //       are to be matched exactly are added using their
            //       absolute paths.  To stop using absolute paths for
            //       matching, files and paths which are added would
            //       need to be changed to obtain the path relative
            //       to the source directory.

            if ( usePattern.select(childAbsPath) ) {
                // System.out.println(prefix + "Select: " + childRelPath);
                selections.add(childRelPath);
            } else {
                // System.out.println(prefix + "Reject: " + childRelPath);
            }

            if ( isDirectory ) {
                filterDirectory(selections, child, childAbsPath, childRelPath);
            }
        }
    }
}
