/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.config.admin;

import java.io.Serializable;

import com.ibm.websphere.ras.annotation.Trivial;

@Trivial
public class ConfigID implements Serializable {
    private static final long serialVersionUID = -7188381474767207297L;

    //

    /**
     * Parse a configuration ID from a print string.
     *
     * The full format is:
     * <code>
     * parentConfigID//PID(childAttribute)[ID]
     * </code>
     *
     * Minimally, a PID must be present. There can be any number of parent
     * configuration ID's. Both child attribute and ID are optional.
     *
     * See {@link ConfigID#printString}.
     *
     * @param printString The configuration ID print string which is to be parsed.
     *
     * @return A new configuration ID.
     *
     * @throws IllegalArgumentException If a non-valid print string is provided.
     */
    public static ConfigID fromProperty(String printString) {
        String[] noParentPrintStrings = printString.split("//");

        ConfigID configID = null;
        for (String noParentPrintString : noParentPrintStrings) {
            configID = parseConfigId(configID, noParentPrintString);
        }

        return configID;
    }

    /**
     * Parse a no-parent configuration ID print string. Answer
     * a no-parent configuration ID.
     *
     * See {@link #parseConfigId(ConfigID, String)}.
     *
     * @param printString A no-parent configuration ID print string.
     *
     * @return A new no-parent configuration ID.
     *
     * @throws IllegalArgumentException If a non-valid no-parent print string
     *                                      is provided.
     */
    public static ConfigID parseConfigId(String printString) {
        return parseConfigId(null, printString);
    }

    /**
     * Parse a no-parent configuration ID print string. Create and
     * return a configuration ID using the parse data and using the
     * supplied parent configuration ID.
     *
     * The parent configuration may be null.
     *
     * @param parentConfigID    The parent configuration ID for the new configuration ID.
     * @param printString A no-parent configuration ID print string.
     *
     * @return A new configuration ID.
     *
     * @throws IllegalArgumentException If a non-valid no-parent print string
     *                                      is provided.
     */
    public static ConfigID parseConfigId(ConfigID parentConfigID, String printString) {
        int childStart = -1;
        int childEnd = -1;

        int idStart = -1;
        int idEnd = -1;

        String failure = null;

        int length = printString.length();
        if (length == 0) {
            failure = "Empty";
        }

        if (failure == null) {
            for (int charNo = 0; (failure == null) && (charNo < length); charNo++) {
                char c = printString.charAt(charNo);

                if (c == '/') {
                    failure = "Unexpected slash character";

                } else if (idEnd != -1) {
                    if (c == '(') {
                        failure = "Child attribute after ID";
                    } else {
                        failure = "Character after ID";
                    }

                } else if (c == '(') {
                    if (charNo == 0) {
                        failure = "Empty PID";
                    } else if ((childStart != -1) || (idStart != -1)) {
                        failure = "Unexpected open parentheses";
                    } else {
                        childStart = charNo;
                    }
                } else if (c == ')') {
                    if ((childStart == -1) || (childEnd != -1)) {
                        failure = "Unexpected close parenthesis";
                    } else if (charNo == childStart + 1) {
                        failure = "Empty child attribute";
                    } else {
                        childEnd = charNo;
                    }

                } else if (c == '[') {
                    if (charNo == 0) {
                        failure = "Empty PID";
                    } else if (((childStart != -1) && (childEnd == -1)) || (idStart != -1)) {
                        failure = "Unexpected open bracket";
                    } else {
                        idStart = charNo;
                    }
                } else if (c == ']') {
                    if ((idStart == -1) || (idEnd != -1)) {
                        failure = "Unexpected close bracket";
                    } else if (charNo == idStart + 1) {
                        failure = "Empty ID";
                    } else {
                        idEnd = charNo;
                    }

                } else if ((childEnd != -1) && (idStart == -1)) {
                    failure = "Character after child attribute";

                } else {
                    // Valid character
                }
            }
        }

        if (failure == null) {
            if ((childStart != -1) && (childEnd == -1)) {
                failure = "Unclosed child attribute";
            } else if ((idStart != -1) && (idEnd == -1)) {
                failure = "Unclosed ID";
            }
        }

        if (failure != null) {
            throw new IllegalArgumentException("Configuration ID [ " + printString + " ] is not valid: " + failure);
        }

        String pid;
        String childAttribute;
        String id;

        if (childStart != -1) {
            pid = printString.substring(0, childStart);
            childAttribute = printString.substring(childStart + 1, childEnd);
        } else {
            pid = null;
            childAttribute = null;
        }

        if (idStart != -1) {
            if (pid == null) {
                pid = printString.substring(0, idStart);
            }
            id = printString.substring(idStart + 1, idEnd);
        } else {
            id = null;
        }

        if (pid == null) {
            pid = printString;
        }

        return new ConfigID(parentConfigID, pid, id, childAttribute);
    }

    //

    public ConfigID(String pid) {
        this(null, pid, null, null);
    }

    public ConfigID(String pid, String id) {
        this(null, pid, id, null);
    }

    public ConfigID(ConfigID parent, String pid, String id) {
        this(parent, pid, id, null);
    }

    public ConfigID(ConfigID parent, String pid, String id, String childAttribute) {
        this.pid = pid;
        this.id = id;
        this.parentConfigID = parent;
        this.childAttribute = childAttribute;

        int prime = 31;
        int useHashCode = 1;
        useHashCode = prime * useHashCode + ((pid == null) ? 0 : pid.hashCode());
        useHashCode = prime * useHashCode + ((id == null) ? 0 : id.hashCode());
        useHashCode = prime * useHashCode + ((parent == null) ? 0 : parent.hashCode());
        useHashCode = prime * useHashCode + ((childAttribute == null) ? 0 : childAttribute.hashCode());
        this.hashCode = useHashCode;

        this.printString = null;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof ConfigID)) {
            return false;
        }
        ConfigID other = (ConfigID) obj;

        // Can't be equal if they have different hash codes.
        if (hashCode != other.hashCode) {
            return false;
        }

        // Short cut if the print strings have been generated.
        if ((printString != null) && (other.printString != null)) {
            return printString.equals(other.printString);
        }

        if (pid == null) {
            if (other.pid != null) {
                return false;
            }
        } else if (!pid.equals(other.pid)) {
            return false;
        }

        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }

        if (parentConfigID == null) {
            if (other.parentConfigID != null) {
                return false;
            }
        } else if (!parentConfigID.equals(other.parentConfigID)) {
            return false;
        }

        if (childAttribute == null) {
            if (other.childAttribute != null) {
                return false;
            }
        } else if (!childAttribute.equals(other.childAttribute)) {
            return false;
        }

        return true;
    }

    /**
     * Answer a print string for this configuration ID.
     *
     * The full pattern is:
     *
     * <code>
     * parentID//PID(childAttribute)[ID]
     * </code>
     *
     * Other usual patterns are:
     *
     * <code>
     * PID
     * PID[ID]
     * parentID//PID[ID]
     * </code>
     *
     * @return A print string for this configuration ID.
     */
    @Override
    @Trivial
    public String toString() {
        if (printString == null) {
            StringBuilder builder = new StringBuilder();
            if (parentConfigID != null) {
                builder.append(parentConfigID.toString());
                builder.append("//");
            }
            builder.append(pid);

            if (childAttribute != null) {
                builder.append('(').append(childAttribute).append(')');
            }
            if (id != null) {
                builder.append('[').append(id).append(']');
            }
            printString = builder.toString();
        }
        return printString;
    }

    //

    private final String pid;
    private final String id;
    private final ConfigID parentConfigID;
    private final String childAttribute;

    private final int hashCode;
    private String printString;

    public String getPid() {
        return pid;
    }

    public String getId() {
        return id;
    }

    public ConfigID getParent() {
        return this.parentConfigID;
    }

    public String getChildAttribute() {
        return this.childAttribute;
    }
}
