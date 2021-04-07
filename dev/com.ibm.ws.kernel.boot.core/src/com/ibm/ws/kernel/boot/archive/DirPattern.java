/*******************************************************************************
 * Copyright (c) 2012,2021 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A pattern used to select files.  See {@link #select(String)} for
 * selection details.
 *
 * Selection is based on regular expressions, using {@link Matcher#find()},
 * with selection if the target expression is found anywhere in a candidate
 * value.
 *
 * (Note the difference between {@link Matcher#find()}, which answers true
 * if the expression is found anywhere in the candidate value, and
 * {@link Matcher#matches()}, which requires that the entire candidate value
 * match the expression.
 */
public class DirPattern {
    private static final boolean IS_WINDOWS = File.separatorChar == '\\';

    /**
     * Answer a canonical path of a file.
     * 
     * This is <strong>not</strong> the same as
     * {@link File#getCanonicalPath()}.
     * 
     * Canonical paths are necessary so that match expressions have
     * a single consistent format.  This is necessary, in particular,
     * for loose configuration matching, which uses forward slashes.
     *
     * This method obtains a canonical path using these rules:
     *
     * <ul>
     * <li>Replace a null path with an empty path.</li>
     * <li>On windows normalize a leading drive letter.</li>
     * <li>Replace all backward slashes ('\') with forward slashes ('/').</li>
     * <li>Add a trailing slash if the target is a directory.</li>
     * </ul>
     *
     * @param path The path which is to be normalized.
     *
     * @return The normalized path.
     */
    public static String cannonize(File file) {
        String path = file.getAbsolutePath();
        boolean needLastSlash = file.isDirectory();

        int length = path.length();
        if ( length == 0 ) {
            return "/"; // Unexpected
        }

        char lastChar = path.charAt(length - 1);

        boolean lastIsSlash =
            ( (IS_WINDOWS && (lastChar == '\\')) ||
              (!IS_WINDOWS && (lastChar == '/')) );

        if ( lastIsSlash && (length == 1) ) {
            return ( IS_WINDOWS ? "/" : path ); // Unexpected
        }

        if ( !IS_WINDOWS ) {
            if ( lastIsSlash ) {
                if ( !needLastSlash ) {
                    return path.substring(1, length - 1);
                } else {
                    return path;
                }
            } else {
                if ( needLastSlash ) {
                    return path + '/';
                } else {
                    return path;
                }
            }
        }
        
        char finalChar0 = 0;

        if ( length > 1 ) {
            char char1 = path.charAt(1);
            if ( char1 == ':' ) {
                char initialChar0 = path.charAt(0);
                if ( (initialChar0 >= 'a') && (initialChar0 <= 'z')) {
                    finalChar0 = Character.toUpperCase(initialChar0);
                }
                if ( (length == 3) && lastIsSlash ) {
                    if ( finalChar0 == 0 ) {
                        if ( lastChar == '\\' ) {
                            return ( new String( new char[] { initialChar0, ':', '/' } ) );
                        } else {
                            return path;
                        }
                    } else {
                        return ( new String( new char[] { finalChar0, ':', '/' } ) );
                    }
                }
            }
        }

        int start = ( (finalChar0 != 0) ? 1 : 0 );
        int end = ((lastIsSlash && !needLastSlash) ? (length - 1) : length);

        StringBuilder normalized = new StringBuilder(length);

        if ( finalChar0 != 0 ) {
            normalized.append(finalChar0);
        }
        for ( int charNo = start; charNo < end; charNo++ ) {
            char nextChar = path.charAt(charNo);
            if ( nextChar == '\\' ) {
                nextChar = '/';
            }
            normalized.append(nextChar);
        }
        if ( !lastIsSlash && needLastSlash ) {
            normalized.append('/');
        }

        return normalized.toString();
    }

    boolean includeByDefault;

    Set<Pattern> includePatterns;
    Set<Pattern> excludePatterns;
    PatternStrategy strategy;

    /**
     * An pattern to filter the directory content
     * 
     * @param includeByDefault if a file underneath base directory is included by default when no pattern apply to it.
     * @param strategy when a file matches both the includePattern and excludePattern, decide which take preference.
     */
    public DirPattern(boolean includeByDefault, PatternStrategy strategy) {
        this.includeByDefault = includeByDefault;

        this.strategy = strategy;
        this.includePatterns = new HashSet<Pattern>();
        this.excludePatterns = new HashSet<Pattern>();
    }

    public Set<Pattern> getIncludePatterns() {
        return includePatterns;
    }

    public Set<Pattern> getExcludePatterns() {
        return excludePatterns;
    }

    public PatternStrategy getStrategy() {
        return strategy;
    }

    public boolean isIncludeByDefault() {
        return includeByDefault;
    }

    /**
     * Simple regexp filter mechanism for controlling which directories and/or files are included in the zipfile.
     * Include Patterns override Exclude Patterns.
     * 
     * @param file
     * @param excludePattern
     * @param includePattern
     * @return if the file should be included.
     */
    static boolean includePreference(File file, Set<Pattern> excludePattern, Set<Pattern> includePattern, boolean includeByDefault) {
        boolean include = includeByDefault;

        // Iterate over exclude patterns, if there is any match, exclude
        if (include) {
            for (Pattern pattern : excludePattern) {
                Matcher excludeMatcher = pattern.matcher(file.getAbsolutePath());
                if (excludeMatcher.find()) {
                    include = false;
                    break;
                }
            }
        }

        // Iterate over include patterns, if there is any match, include
        if (!include) {
            for (Pattern pattern : includePattern) {
                Matcher includeMatcher = pattern.matcher(file.getAbsolutePath());
                if (includeMatcher.find()) {
                    include = true; // If we are here, we are overriding an exclude
                    break;
                }
            }
        }

        return include;
    }

    /**
     * Include if match the includePattern, then exclude that if it matches the excludePattern
     * 
     * @param file
     * @param excludePattern
     * @param includePattern
     * @return if the file should be included.
     */
    static boolean excludePreference(File file, Set<Pattern> excludePattern, Set<Pattern> includePattern, boolean includeByDefault) {
        boolean include = includeByDefault;

        // Iterate over include patterns, if there is any match, include
        if (!include) {
            for (Pattern pattern : includePattern) {
                Matcher includeMatcher = pattern.matcher(file.getAbsolutePath());
                if (includeMatcher.find()) {
                    include = true;
                    break;
                }
            }
        }

        // Iterate over exclude patterns, if there is any match, exclude
        if (include) {
            for (Pattern pattern : excludePattern) {
                Matcher excludeMatcher = pattern.matcher(file.getAbsolutePath());
                if (excludeMatcher.find()) {
                    include = false; // If we are here, we are overriding an include
                    break;
                }
            }
        }

        return include;
    }

    public enum PatternStrategy {
        IncludePreference, ExcludePreference;
    }

}