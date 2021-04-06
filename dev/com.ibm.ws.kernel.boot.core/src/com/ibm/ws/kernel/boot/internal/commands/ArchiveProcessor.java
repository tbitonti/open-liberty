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
package com.ibm.ws.kernel.boot.internal.commands;

import java.io.File;

/**
 * interface of package/dump processor
 */
public interface ArchiveProcessor {
    /**
     * Obsolete file system sensitive path separator for packaging
     * regular expressions.
     * 
     * These now consistently use unix separators.  Do not use
     * this constant.  Use instead {@link #REGEX_SEP}.
     */
    public static final String REGEX_SEPARATOR = 
        File.separator.equals("\\") ? "\\\\" : File.separator;

    /**
     * Path separator for packaging regular expressions.
     *
     * These now consistently use unix separators.  Do not use
     * the obsolete {@link #REGEX_SEPARATOR}.
     */
    public static final char REGEX_SEP = '/'; 

    public static final String REGEX_TIMESTAMP =
        "\\d\\d\\.\\d\\d\\.\\d\\d_\\d\\d\\.\\d\\d\\.\\d\\d";

    // TFB:
    // This class should be designed away.  Current use from
    // com.ibm.ws.kernel.boot.internal.commands.PackageCommand.packageServerRuntime(File, boolean)
    // simply uses it as a vehicle for transporting option values
    // into the new package processor using a list of pairs.  Since
    // the final storage is a map, that transport can directly use
    // a map.

    public static class Pair<U, V> {
        private final U pairKey;
        private final V pairValue;

        public Pair(U pairKey, V pairValue) {
            super();
            this.pairKey = pairKey;
            this.pairValue = pairValue;
        }

        @Override
        public String toString() {
            return "Pair[" + pairKey + '=' + pairValue + ']';
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((pairKey == null) ? 0 : pairKey.hashCode());
            result = prime * result + ((pairValue == null) ? 0 : pairValue.hashCode());
            return result;
        }

        public U getPairKey() {
            return pairKey;
        }

        public V getPairValue() {
            return pairValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (this == obj) {
                return true;
            }
            
            if (getClass() != obj.getClass()) {
                return false;
            }
            @SuppressWarnings("rawtypes")
            Pair other = (Pair) obj;

            Object otherKey = other.pairKey;
            if (pairKey == null) {
                if (otherKey != null) {
                    return false;
                }
            } else {
                if (otherKey == null) {
                    return false;
                } else if (!pairKey.equals(otherKey)) {
                    return false;
                }
            }

            Object otherValue = other.pairValue;
            if (pairValue == null) {
                if (otherValue != null) {
                    return false;
                }
            } else {
                if (otherValue == null) {
                    return false;
                } else if (!pairValue.equals(otherValue)) {
                    return false;
                }
            }

            return true;
        }
    }
}
