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
package com.ibm.ws.kernel.boot.archive;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirPattern {

    public enum PatternStrategy {
        IncludePreference, ExcludePreference;
    }

    /**
     * An pattern to filter directory contents.
     * 
     * @param includeByDefault Control parameter: The inclusion strategy used for files unmatched by no include or exclude pattern. 
     * @param selectionPreference Control parameter: Tells if inclusion has priority over exclusion.
     */
    public DirPattern(boolean includeByDefault, PatternStrategy selectionPreference) {
        this.includeByDefault = includeByDefault;

        this.selectionPreference = selectionPreference;
        this.includePatterns = new HashSet<Pattern>();
        this.excludePatterns = new HashSet<Pattern>();
    }

    public static final boolean INCLUDE_BY_DEFAULT = true;
    public static final boolean EXCLUDE_BY_DEFAULT = !INCLUDE_BY_DEFAULT;

    private final boolean includeByDefault;

    public boolean isIncludeByDefault() {
        return includeByDefault;
    }

    private final PatternStrategy selectionPreference;

    public PatternStrategy getStrategy() {
        return selectionPreference;
    }
    
    private final Set<Pattern> includePatterns;
    private final Set<Pattern> excludePatterns;
    
    public Set<Pattern> getIncludePatterns() {
        return includePatterns;
    }

    public Set<Pattern> getExcludePatterns() {
        return excludePatterns;
    }

    public boolean include(File file) {
        if ( getStrategy() == PatternStrategy.IncludePreference ) {
            return includePreference(file, getExcludePatterns(), getIncludePatterns(), isIncludeByDefault());
        } else {
            return excludePreference(file, getExcludePatterns(), getIncludePatterns(), isIncludeByDefault());
        }
    }
    
    /**
     * Tell if a file is selected.  Include patterns have precedence over exclude patterns.
     * 
     * If the default is to select everything, de-select files which match the exclude pattern,
     * but re-select files which also match the include pattern.
     * 
     * If the default is to select nothing, select files which match the include pattern.
     * 
     * @param file The file which is to be tested.  Test using the absolute path of the file.
     * @param excludePatterns Patterns of file names which are to be excluded.  Ignored if
     *     the default is to select nothing.
     * @param includePatterns Patterns of file names which are to be included.  Ignored if
     *     no exclusion matches the file.
     * @param defaultSelection Control parameter: Does selection default to select everything
     *     or to select nothing.
     *     
     * @return True or false telling if a file is to be selected.
     */
    public static boolean includePreference(File file,
        Set<Pattern> excludePatterns, Set<Pattern> includePatterns, boolean defaultSelection) {

        String path = file.getAbsolutePath();
        
        boolean select = defaultSelection;
        
        if ( select && find(path, excludePatterns) ) {
            select = false; // Explicitly un-selected.
        }
        
        if ( !select && find(path, includePatterns) ) {
            select = true; // Override!
        }

        return select;
    }

    /**
     * Tell if a file is selected.  Exclude patterns have precedence over include patterns.
     * 
     * @param file A file which is to be tested.  Test using the absolute path of the file.
     * @param excludePatterns Patterns of file names which are to be excluded.  Ignored if
     *     the default is to exclude everything and if no inclusion matches the file.
     * @param includePatterns Patterns of file names which are to be included.  Ignored if
     *     the default is to include everything.
     * @param defaultSelection Control parameter: Does selection default to select everything
     *     or to select nothing.
     *     
     * @return True or false telling if a file is to be selected.
     */
    public static boolean excludePreference(File file,
            Set<Pattern> excludePatterns, Set<Pattern> includePatterns, boolean defaultSelection) {

        String path = file.getAbsolutePath();
        
        boolean select = defaultSelection;
        
        if ( !select && find(path, includePatterns) ) {
            select = true; // Explicitly selected.
        }
        
        if ( select && find(path, excludePatterns) ) {
            select = false; // Override!
        }

        return select;
    }
    
    private static boolean find(String path, Set<Pattern> patterns) {
        for ( Pattern pattern : patterns ) {
            Matcher matcher = pattern.matcher(path);
            if ( matcher.find() ) {
                return true;
            }
        }
        return false;
    }    
}