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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A pattern used to select files.  See {@link #select(String)} for
 * selection details.
 */
public class DirPattern {
    /**
     * Helper: Normalize a path to all forward slashes.
     *
     * @param path The path which is to be normalized.
     *
     * @return The normalized path.
     */
    public static String normalize(String path) {
        return ( path.replace('\\',  '/') );
    }

    /**
     * Helper enumeration.  Used to mark a pattern as preferring
     * includes or performing excludes.
     * 
     * See {@link #select(String)}.
     */
    public enum PatternStrategy {
        IncludePreference, ExcludePreference;
    }

    /**
     * Create a directory selection pattern.
     * 
     * Selections are specified by adding to the include and exclude
     * pattern sets.  See {@link #getIncludePatterns()} and
     * {@link #getExcludePatterns()}.
     *
     * @param includeByDefault True or false telling if the default
     *     is to include a target path.
     *
     * @param strategy Which strategy to use when selecting files.
     */
    public DirPattern(boolean includeByDefault, PatternStrategy strategy) {
        this.includeByDefault = includeByDefault;

        this.strategy = strategy;

        this.includePatterns = null;
        this.excludePatterns = null;
    }

    private final boolean includeByDefault;

    public boolean isIncludeByDefault() {
        return includeByDefault;
    }

    private final PatternStrategy strategy;

    public PatternStrategy getStrategy() {
        return strategy;
    }

    public boolean isIncludePreference() {
        return ( getStrategy() == PatternStrategy.IncludePreference );
    }

    private Set<Pattern> includePatterns;

    public void addIncludePattern(Pattern pattern) {
        if ( includePatterns == null ) {
            includePatterns = new HashSet<Pattern>(1);
        }
        includePatterns.add(pattern);
    }

    /**
     * Answer the include patterns of this directory pattern.
     * 
     * Patterns cannot be added to the result collection.  Use
     * instead {@link #addIncludePattern(Pattern)}.
     *
     * @return The include patterns of this directory pattern.
     */
    public Set<? extends Pattern> getIncludePatterns() {
        return ( (includePatterns == null) ? Collections.emptySet() : includePatterns );
    }

    protected boolean isIncluded(String path) {
        if ( includePatterns == null ) {
            return false;
        }

        for ( Pattern pattern : includePatterns ) {
            Matcher includeMatcher = pattern.matcher(path);
            if ( includeMatcher.find() ) {
                return true;
            }
        }
        return false;
    }

    private Set<Pattern> excludePatterns;

    protected void addExcludePattern(Pattern pattern) {
        if ( excludePatterns == null ) {
            excludePatterns = new HashSet<Pattern>(1);
        }
        excludePatterns.add(pattern);
    }

    /**
     * Answer the exclude patterns of this directory pattern.
     * 
     * Patterns cannot be added to the result collection.  Use
     * instead {@link #addExcludePattern(Pattern)}.
     *
     * @return The exclude patterns of this directory pattern.
     */
    public Set<? extends Pattern> getExcludePatterns() {
        return ( (excludePatterns == null) ? Collections.emptySet() : excludePatterns );
    }

    protected boolean isExcluded(String path) {
        if ( excludePatterns == null ) {
            return false;
        }

        for ( Pattern pattern : getExcludePatterns() ) {
            Matcher excludeMatcher = pattern.matcher(path);
            if ( excludeMatcher.find() ) {
                return true;
            }
        }
        return false;
    }
    
    //

    /**
     * Main API: Tell if a path is to be selected.  The path
     * must be normalized.
     *
     * There are four cases, depending on the strategy and depending
     * on the inclusion default setting.
     * 
     * If inclusion is the default, and the inclusion strategy is being
     * used, a path is included unless it is explicitly excluded, and
     * only if the path is not explicitly included.
     * 
     * If exclusion is the default, and the inclusion strategy is being
     * used, a path is excluded unless it is explicitly included.  The
     * exclude patterns are not used.
     * 
     * If inclusion is the default, and the exclusion strategy is being
     * used, a path is included unless it is explicitly excluded.  The
     * include patterns are not used.
     * 
     * If exclusion is the default, and the exclusion strategy is being
     * used, a path is included only if it is explicitly included, and if
     * the path is not explicitly excluded.
     *
     * @param path The path which is to be tested.
     *
     * @return True or false telling if the path is selected.
     */
    public boolean select(String path) {
        return ( isIncludePreference() ? includePreference(path) : excludePreference(path) );
    }

    /**
     * Tell if a path is included.  The path must be normalized.
     * Inclusions have preference over exclusions.
     * 
     * @param path The path which is to be tested.
     *
     * @return True or false telling if the path is included.
     */
    protected boolean includePreference(String path) {
        boolean include = includeByDefault;

        // If we are excluding by default, there is no point to
        // performing exclude checks.

        // An explicit exclude overrides a default include.

        if ( include && isExcluded(path) ) {
            include = false;
        }

        // If we are including by default, and the file was not
        // excluded, then there is no point to performing include
        // checks.
        
        // An explicit include overrides either an explicit exclude,
        // or a default exclude.

        if ( !include && isIncluded(path) ) {
            include = true;
        }

        return include;
    }

    /**
     * Tell if a path is included.  The path must be normalized.
     * Exclusions have preference over inclusions.
     * 
     * @param path The path which is to be tested.
     *
     * @return True or false telling if the path is included.
     */    
    protected boolean excludePreference(String path) {
        boolean include = includeByDefault;

        // If we are including by default, there is no point to
        // performing include checks.

        // An explicit include overrides a default exclude.

        if ( !include && isIncluded(path) ) {
            include = true;
        }

        // If we are excluding by default, and the file was not
        // included, then there is no point to performing exclude
        // checks.
        
        // An explicit exclude overrides either an explicit include,
        // or a default include.

        if ( include && isExcluded(path) ) {
            include = false;
        }

        return include;
    }
}