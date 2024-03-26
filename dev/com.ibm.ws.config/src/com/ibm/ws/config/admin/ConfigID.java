/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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

package com.ibm.ws.config.admin;

import java.io.Serializable;
import java.util.Map;

import com.ibm.websphere.ras.annotation.Trivial;

/**
 * Type for configuration IDs.
 *
 * The type embeds the serialization format of configuration IDs,
 * including both the serialization and deserialization operations.
 *
 * <pre>
 *   [ parentID "//" ] pid [ '(' childAttribute ')' ] [ '[' id ']' ]
 * </pre>
 *
 * <pre>
 *   com.ibm.ws.classloading.sharedlibrary[global]//
 *     com.ibm.ws.kernel.metatype.helper.fileset(fileset)[default-1]
 * </pre>
 *
 * <pre>
 * pid2 // pid1 // pid0
 * </pre>
 */

@Trivial
public class ConfigID extends RawConfigID implements Serializable {
    private static final long serialVersionUID = -7188381474767207297L;

    //

    public static ConfigID deserialize(String property) {
        return deserialize(property, null);
    }

    /**
     * Deserialize a configuration ID.
     *
     * This is done, in particular, when reading a configuration ID from
     * a server configuration.
     *
     * Deserialization creates parent IDs, which are accessible only through the
     * returned ID.
     *
     * @param property A property value containing a serialized configuration ID.
     *
     * @return The deserialized configuration ID.
     */
    public static ConfigID deserialize(String property, Map<RawConfigID, ConfigID> storage) {
        int pLen = property.length();

        ConfigID lastParent = null;
        int lastSlash = 0;
        int nextSlash;

        while (lastSlash < pLen) {
            nextSlash = property.indexOf("//", lastSlash);
            if (nextSlash == -1) {
                nextSlash = pLen;
            }

            int startId = -1;
            int endId = -1;
            boolean afterId = false;

            int startAttr = -1;
            int endAttr = -1;
            boolean afterAttr = false;

            // pid [ '(' childAttribute ')' ] [ '[' id ']' ]

            for (int nextOffset = lastSlash; nextOffset < nextSlash; nextOffset++) {
                char nextChar = property.charAt(nextOffset);

                if (endId != -1) { // Completed the ID
                    afterId = true; // There were extra characters after the ID.
                    break; // ignore the rest
                } else if (startId != -1) { // Began the ID
                    if (nextChar == ']') {
                        endId = nextOffset; // Complete the ID.
                    } else {
                        // Began but did not complete the ID.
                    }

                } else if (startAttr == -1) { // Nothing yet
                    if (nextChar == '(') {
                        startAttr = nextOffset + 1; // Begin the child attribute.
                    } else if (nextChar == '[') {
                        startId = nextOffset + 1; // Begin the ID
                    }
                } else if (endAttr == -1) { // Began but did not complete the child attribute.
                    if (nextChar == ')') {
                        endAttr = nextOffset; // End the child attribute.
                    } else {
                        // Began but did not complete the child attribute.
                    }

                } else { // ((startAttr != -1) && (endAttr != -1)): Completed the child attribute
                    if (nextChar == '[') { // If anything, the ID must be next
                        startId = nextOffset + 1; // Begin the ID.
                    } else {
                        afterAttr = true; // There were extra characters after the child attribute.
                    }
                }
            }

            String id;
            boolean unclosedId;
            if (startId != -1) {
                if (endId == -1) {
                    endId = nextSlash;
                    unclosedId = true;
                } else {
                    unclosedId = false;
                }
                id = property.substring(startId, endId);
            } else {
                unclosedId = false;
                id = null;
            }

            String childAttr;
            boolean unclosedAttr;
            if (startAttr != -1) {
                if (endAttr == -1) {
                    endAttr = nextSlash;
                    unclosedAttr = true;
                } else {
                    unclosedAttr = false;
                }
                childAttr = property.substring(startAttr, endAttr);
            } else {
                unclosedAttr = false;
                childAttr = null;
            }

            int startPid = lastSlash;
            int endPid;
            if (startAttr != -1) {
                endPid = startAttr - 1;
            } else if (startId != -1) {
                endPid = startId - 1;
            } else {
                endPid = nextSlash;
            }
            String pid = property.substring(startPid, endPid);

            boolean haveError = (afterId || afterAttr || unclosedId || unclosedAttr);
            if (haveError) {
                String fragment = property.substring(lastSlash, nextSlash);

                if (afterId) {
                    System.out.println("Warning: Configuration ID fragment [ " + fragment + " ] of [ " + property + " ]" +
                                       " has characters after the ID close character ']'.");
                }
                if (afterAttr) {
                    System.out.println("Warning: Configuration ID fragment [ " + fragment + " ] of [ " + property + " ]" +
                                       " has characters after the child attibute close character ')'.");
                }
                if (unclosedId) {
                    System.out.println("Warning: Configuration ID fragment [ " + fragment + " ] of [ " + property + " ]" +
                                       " has no closing ID character ']'.");
                }
                if (unclosedAttr) {
                    System.out.println("Warning: Configuration ID fragment [ " + fragment + " ] of [ " + property + " ]" +
                                       " has no closing child attribute character ')'.");
                }
            }

            if (storage == null) {
                lastParent = new ConfigID(lastParent, pid, id, childAttr);

            } else {
                RawConfigID lookupId = new RawConfigID(lastParent, pid, id, childAttr);
                synchronized (storage) {
                    // 'computeIfAbsent' doesn't work: Use the actual ID as the
                    // key.  Otherwise, we unnecessarily keep the raw IDs.
                    lastParent = storage.get(lookupId);
                    if (lastParent == null) {
                        lastParent = lookupId.asConfigID();
                        storage.put(lastParent, lastParent);
                    }
                }
            }

            lastSlash = nextSlash + 2; // Skip "//".
        }

        return lastParent;
    }

    //

    /**
     * Deserialize a configuration ID.
     *
     * This is done, in particular, when reading a configuration ID from
     * a server configuration.
     *
     * Deserialization creates parent IDs, which are accessible only through the
     * returned ID.
     *
     * @param property A property value containing a serialized configuration ID.
     *
     * @return The deserialized configuration ID.
     *
     */
    public static ConfigID fromProperty(String property) {
        String[] idFragments = property.split("//");

        ConfigID id = null;
        for (String idFragment : idFragments) {
            id = constructId(id, idFragment);
        }

        return id;
    }

    /**
     * Deserialize an ID fragment. This is a serialized configuration ID which
     * no parents.
     *
     * @param parentId   The parent of the new configuration ID.
     * @param idFragment A serialized configuration ID, with all parents removed.
     *
     * @return The configuration ID.
     */
    private static ConfigID constructId(ConfigID parentId, String idFragment) {
        String childAttribute = parseChildAttribute(idFragment);
        String pid = parsePid(idFragment);
        String id = parseId(idFragment);

        return new ConfigID(parentId, pid, id, childAttribute);
    }

    /**
     * Parse the ID value from a configuration ID fragment.
     *
     * @param idFragment A serialized configuration ID, with all parents removed.
     *
     * @return The ID of the configuration ID. Null if no ID is present.
     */
    private static String parseId(String idFragment) {
        int startIdx = idFragment.indexOf('[');
        if (startIdx == -1) {
            return null;
        }

        int endIdx = idFragment.indexOf(']', startIdx + 1);
        if (endIdx == -1) {
            return null;
        }

        return idFragment.substring(startIdx + 1, endIdx);
    }

    /**
     * Parse the PID value from a configuration ID fragment.
     *
     * @param idFragment A serialized configuration ID, with all parents removed.
     *
     * @return The PID of the configuration ID. Null if no PID is present.
     */
    private static String parsePid(String idFragment) {
        int idx = idFragment.indexOf('(');
        if (idx == -1) {
            idx = idFragment.indexOf('[');
        }
        if (idx == -1) {
            return idFragment;
        }

        return idFragment.substring(0, idx);
    }

    /**
     * Parse the child attribute value from a configuration ID fragment.
     *
     * @param idFragment A serialized configuration ID, with all parents removed.
     *
     * @return The child attribute value of the configuration ID. Null if no
     *         child attribute value is present.
     */
    private static String parseChildAttribute(String idFragment) {
        int startIdx = idFragment.indexOf('(');
        if (startIdx == -1) {
            return null;
        }

        int endIdx = idFragment.indexOf(')', startIdx + 1);
        if (endIdx == -1) {
            return null;
        }

        return idFragment.substring(startIdx + 1, endIdx);
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

    protected static void validate(String pid, String id, String childAttribute) {
        if (pid != null) {
            char lastChar = '\0';
            for (int charNo = 0; charNo < pid.length(); charNo++) {
                char c = pid.charAt(charNo);
                if ((c == '(') || (c == '[')) {
                    throw new IllegalArgumentException("Configuration ID PID [ " + pid + " ] includes non-valid character [ " + c + " ]");
                } else if ((c == '/') && (lastChar == '/')) {
                    throw new IllegalArgumentException("Configuration ID PID [ " + pid + " ] includes non-valid characters [ // ]");
                }
                lastChar = c;
            }
        }

        if (childAttribute != null) {
            char lastChar = '\0';
            for (int charNo = 0; charNo < childAttribute.length(); charNo++) {
                char c = childAttribute.charAt(charNo);
                if (c == ')') {
                    throw new IllegalArgumentException("Configuration ID child attribute [ " + childAttribute + " ] includes non-valid character [ " + c + " ]");
                } else if ((c == '/') && (lastChar == '/')) {
                    throw new IllegalArgumentException("Configuration ID child attribute [ " + childAttribute + " ] includes non-valid characters [ // ]");
                }
                lastChar = c;
            }
        }

        if (id != null) {
            char lastChar = '\0';
            for (int charNo = 0; charNo < id.length(); charNo++) {
                char c = id.charAt(charNo);
                if (c == ']') {
                    throw new IllegalArgumentException("Configuration ID id [ " + id + " ] includes non-valid character [ " + c + " ]");
                } else if ((c == '/') && (lastChar == '/')) {
                    throw new IllegalArgumentException("Configuration ID id [ " + id + " ] includes non-valid characters [ // ]");
                }
                lastChar = c;
            }
        }
    }

    public ConfigID(ConfigID parent, String pid, String id, String childAttribute) {
        this(parent, pid, id, childAttribute, !DO_VALIDATE);
    }

    public static final boolean DO_VALIDATE = true;

    public ConfigID(ConfigID parent, String pid, String id, String childAttribute, boolean validate) {
        super(parent, pid, id, childAttribute);

        if (validate) {
            validate(pid, id, childAttribute);
        }

        String parentString;
        int asStringLen;

        if (parent != null) {
            parentString = parent.toString();
            asStringLen = parentString.length() + 2;
        } else {
            parentString = null;
            asStringLen = 0;
        }

        if (pid != null) {
            asStringLen += pid.length();
        }
        if (childAttribute != null) {
            asStringLen += childAttribute.length() + 2;
        }
        if (id != null) {
            asStringLen += id.length() + 2;
        }

        StringBuilder builder = new StringBuilder(asStringLen);

        if (parentString != null) {
            builder.append(parentString);
            builder.append("//");
        }

        if (pid != null) {
            builder.append(pid);
        }
        if (childAttribute != null) {
            builder.append('(');
            builder.append(childAttribute);
            builder.append(')');
        }
        if (id != null) {
            builder.append('[');
            builder.append(id);
            builder.append(']');
        }

        this.asString = builder.toString();
    }

    //

    @Override
    public boolean equals(Object otherObj) {
        if (this == otherObj) {
            return true;
        } else if (otherObj == null) {
            return false;

        } else if (otherObj instanceof ConfigID) {
            ConfigID other = (ConfigID) otherObj;
            if (hashCode != other.hashCode) {
                return false;
            } else {
                return asString.equals(other.asString);
            }
        } else if (otherObj instanceof RawConfigID) {
            return super.equals(otherObj);

        } else {
            return false;
        }
    }

    private final String asString;

    /**
     * Answer the string representation of this configuration ID.
     *
     * <pre>
     * [ parentID "//" ] pid [ '(' childAttribute ')' ] [ '[' id ']' ]
     * </pre>
     *
     * @return The string representation of this configuration ID.
     */
    @Override
    @Trivial
    public String toString() {
        return asString;
    }
}
