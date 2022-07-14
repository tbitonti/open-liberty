/*******************************************************************************
 * Copyright (c) 2014, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.websphere.metatype;

public enum OutputVersion {
    v1("1"), v2("2");

    public static OutputVersion n_lookup(String nVersion) {
        if (v1.getValue().equals(nVersion)) {
            return v1;
        } else if (v2.getValue().equals(nVersion)) {
            return v2;
        } else {
            return null;
        }
    }

    public static OutputVersion lookup(String version) {
        if (version == null) {
            return v1;
        }

        version = version.trim();

        int vLen = version.length();
        if (vLen == 0) {
            return v1;
        }

        if (vLen >= 2) {
            if ((version.charAt(vLen - 2) == '.') &&
                (version.charAt(vLen - 1) == '0')) {
                version = version.substring(0, vLen - 2);
            }
        }

        return n_lookup(version);
    }

    public static final boolean DO_THROW = true;

    public static OutputVersion getEnum(String version) {
        return getEnum(version, DO_THROW);
    }

    public static OutputVersion getEnum(String version, boolean doThrow) {
        OutputVersion v = lookup(version);
        if ((v == null) && doThrow) {
            throw new IllegalArgumentException(version);
        }
        return v;
    }

    public static boolean isValid(String version) {
        return (lookup(version) != null);
    }

    //

    private OutputVersion(String version) {
        this.value = version;
    }

    @Override
    public String toString() {
        return value;
    }

    //

    private final String value;

    public String getValue() {
        return value;
    }

    //

    /**
     * Normalize a version print string.
     *
     * Trim leading and trailing spaces from a non-null print string.
     *
     * Answer <code>"1"</code> if the print string is null or empty.
     *
     * Remove any trailing ".0" from a non-null print string.
     *
     * @return The normalized print string.
     */
    @Deprecated
    public static String getNormalizedVersion(String version) {
        if (version == null) {
            return "1";
        }

        version = version.trim();
        if (version.isEmpty()) {
            return "1";
        }

        int vLen = version.length();
        if (vLen < 2) {
            return version;
        }

        // That ".0" normalizes to "" is not a problem:
        // Neither ".0" nor "" match current output versions.
        // An exception results after the lookup in either
        // case.

        if ((version.charAt(vLen - 2) == '.') &&
            (version.charAt(vLen - 1) == '0')) {
            return version.substring(0, vLen - 2);
        }

        return version;
    }
}