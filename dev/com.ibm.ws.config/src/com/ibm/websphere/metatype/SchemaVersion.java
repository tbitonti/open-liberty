/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.websphere.metatype;

// If new versions are added, for example V2, be consistent and make it V2_0("2.0").  The version
// representation here is, unfortunately, not consistent with the "OutputVersion", but we can't do
// anything about that now.  Note getNormalizedVersion() adds ".0" when the version does not
// contain a ".".  This is to make, for example, v1 = v1_0, or "1" = "1.0".

public enum SchemaVersion {
    v1_0("1.0"),
    v1_1("1.1");

    public static SchemaVersion lookup(String version) {
        if (version == null) {
            return v1_0; // Default
        }

        version = version.trim();
        int vLen = version.length();

        if (vLen == 0) {
            return v1_0; // Map "" to "1.0".

        } else if (version.charAt(0) != '1') {
            return null; // Need char0 to be '1'.
        } else if (vLen == 1) {
            return v1_0; // Map "1" to "1.0"

        } else if (version.charAt(1) != '.') {
            return null; // Need char1 to be '.'.

        } else if (vLen != 3) {
            return null; // Need exactly three characters. "1." is not allowed.
        } else {
            char c2 = version.charAt(2);
            if (c2 == '0') {
                return v1_0; // char2 must either be '0' ...
            } else if (c2 == '1') {
                return v1_1; // ... or must be '1'.
            } else {
                return null; // Neither '0' nor '1.
            }
        }
    }

    public static boolean isValid(String version) {
        return (lookup(version) != null);
    }

    public static SchemaVersion getEnum(String version) {
        SchemaVersion schemaVersion = lookup(version);
        if (schemaVersion != null) {
            return schemaVersion;
        } else {
            throw new IllegalArgumentException(version);
        }
    }

    //

    private SchemaVersion(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    private final String value;

    public String getValue() {
        return value;
    }

    //

    @Deprecated
    public static String getNormalizedVersion(String version) {
        if (version == null) {
            return "1.0";
        }

        version = version.trim();

        if (version.isEmpty()) {
            return "1.0";
        }

        if (!version.contains(".")) {
            return version + ".0";
        }

        return version;
    }
}
