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
package com.ibm.ws.config.admin.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ibm.ws.config.admin.ConfigID;
import com.ibm.ws.config.admin.RawConfigID;

//@formatter:off
public class ConfigIDTest {
    // [ parentID "//" ] pid [ '(' childAttribute ')' ] [ '[' id ']' ]

    public static final String[] VALID_SERIALIZED_IDS = {
        "()",
        "[]",
        "()[]",

        "pid()",
        "(child)",
        "[id]",

        "pid()[]",
        "(child)[]",
        "()[id]",

        "(child)[pid]",
        "pid()[id]",
        "pid(child)[]",

        "pid",
        "pid(child)",
        "pid[pid]",
        "pid(child)[pid]",

        "pid0//pid1",
        "pid0//pid1//pid2",

        "pid0[id0]//pid1[id1]",
        "pid0[id0]//pid1[id1]//pid2[id2]",

        "pid0(child0)//pid1(child1)",
        "pid0(child0)//pid1(child1)//pid2(child2)",

        "pid0(child0)[id0]//pid1(child1)[id1]",
        "pid0(child0)[id0]//pid1(child1)[id1]//pid2(child2)[id2]",

        "pid0(child0)//pid1[id1]",
        "pid0//pid1(child1)//pid2[id2]",
    };

    @Test
    public void testRegressions() throws Exception {
        int failures = 0;

        System.out.println("Regression test serialization of valid configuration IDs");

        for (String serializedId : VALID_SERIALIZED_IDS) {
            ConfigID deserializedIdNew = ConfigID.deserialize(serializedId);
            ConfigID deserializedIdOld = ConfigID.fromProperty(serializedId);

            String reserializedIdNew = deserializedIdNew.toString();
            String reserializedIdOld = deserializedIdOld.toString();

            boolean passed = deserializedIdNew.equals(deserializedIdOld);

            if ( passed ) {
                System.out.println("  ID [ " + serializedId + " ]" +
                                   " ==> [ " + reserializedIdNew + " ]: OK");
            } else {
                System.out.println("  ID [ " + serializedId + " ]" +
                                   " ==> (new) [ " + reserializedIdNew + " ]" +
                                   " (old) [ " + reserializedIdOld + " ]: KO");
            }

            if (!passed) {
                failures++;
            }
        }

        assertEquals("Serialization failures", 0, failures);
    }

    @Test
    public void testValid() throws Exception {
        int failures = 0;

        System.out.println("Test serialization of valid configuration IDs");

        for (String serializedId : VALID_SERIALIZED_IDS) {
            ConfigID deserializedId = ConfigID.deserialize(serializedId);
            String reserializedId = deserializedId.toString();
            boolean passed = serializedId.equals(reserializedId);

            if ( passed ) {
                System.out.println("  ID [ " + serializedId + " ]: OK");
            } else {
                System.out.println("  ID [ " + serializedId + " ]" +
                                   " ==> [ " + reserializedId + " ]: KO");
            }

            if (!passed) {
                failures++;
            }
        }

        assertEquals("Serialization failures", 0, failures);
    }

    public static final String[][] NON_VALID_SERIALIZED_IDS = {
        { "(", "()" },
        { "[", "[]" },
        { "()[", "()[]" },

        { "()x", "()", },
        { "[]x", "[]", },
        { "()[]x", "()[]" },

        { "pid(", "pid()" },
        { "pid[", "pid[]" },
        { "pid()[", "pid()[]" },

        { "pid()x", "pid()", },
        { "pid[]x", "pid[]", },
        { "pid()[]x", "pid()[]" }
    };

    @Test
    public void testNonValid() throws Exception {
        System.out.println("Test serialization of non-valid configuration IDs");

        int failures = 0;

        for (String[] ids : NON_VALID_SERIALIZED_IDS) {
            String nonValidId = ids[0];
            String validId = ids[1];

            ConfigID deserializedId = ConfigID.deserialize(nonValidId);
            String reserializedId = deserializedId.toString();

            boolean passed = reserializedId.equals(validId);

            String msg = "  ID [ " + nonValidId + " ]" +
                         " ==> [ " + reserializedId + " ]";
            if ( passed ) {
                msg += ": OK";
            } else {
                msg += " expected [ " + validId + " ]" +
                       ": KO";
            }
            System.out.println(msg);

            if (!passed) {
                failures++;
            }
        }

        assertEquals("Serialization failures", 0, failures);
    }

    public static final int CHILD_FREQUENCY = 3;

    public static List<ConfigID> createIDTree(int depth, int width) {
        // depth == 4, width == 4:
        // 1,
        // 1 + 4,
        // 1 + 4 + 4*4,
        // 1 + 4 + 4*4 + 4*4*4,
        // 1 + 4 + 4*4 + 4*4*4 + 4*4*4*4

        int numNodes = 1;
        int lastWidth = 1;
        for ( int lastDepth = 0; lastDepth < depth; lastDepth++ ) {
            lastWidth *= width;
            numNodes += lastWidth;
        }

        List<ConfigID> idTree = new ArrayList<>(numNodes);

        int nodeNo = 0;
        idTree.add( createID(null, nodeNo++, 0) );

        int lastDepth = 0;
        int lastDepthStart = 0;
        int lastDepthEnd = 1;

        while ( lastDepth < depth ) {
            for ( int prevNo = lastDepthStart; prevNo < lastDepthEnd; prevNo++ ) {
                ConfigID prevID = idTree.get(prevNo);

                for ( int childNo = 0; childNo < width; childNo++ ) {
                    idTree.add( createID(prevID, nodeNo++, childNo) );
                }
            }

            lastDepth++;
            lastDepthStart = lastDepthEnd;
            lastDepthEnd = nodeNo;
        }

        return idTree;
    }

    public static ConfigID createID(ConfigID parentID, int nodeNo, int childNo) {
        boolean addChild = (nodeNo % CHILD_FREQUENCY) == 0;

        String pid = "pid" + Integer.toString(nodeNo);
        if ( !addChild ) {
            return new ConfigID(parentID, pid, null, null);

        } else {
            String childAttribute = "child" + Integer.toString(childNo);
            String id = pid.substring(1);
            return new ConfigID(parentID, pid, id, childAttribute);
        }
    }

    @Test
    public void testStore() throws Exception {
        verifyStore(2, 2);
        verifyStore(4, 2);
        verifyStore(6, 2);

        verifyStore(2, 3);
        verifyStore(3, 3);
        verifyStore(4, 3);

        verifyStore(2, 4);
        verifyStore(3, 4);
        verifyStore(4, 4);

        verifyStore(2, 5);
        verifyStore(3, 5);
    }

    public void verifyStore(int depth, int width) {
        System.out.println("Verifying ID uniqueness; tree depth [ " + depth + " ] node width [ " + width + " ]");

        List<ConfigID> idTree = createIDTree(depth, width);
        int numUniqueIds = idTree.size();

        System.out.println("  Created [ " + numUniqueIds + " ]");

        List<ConfigID> recreatedTree = new ArrayList<>(numUniqueIds);

        Map<RawConfigID, ConfigID> idStore = new HashMap<>(numUniqueIds);

        for ( ConfigID id : idTree ) {
            recreatedTree.add( ConfigID.deserialize(id.toString(), idStore) );
        }

        int numInStore = idStore.size();
        int numRecreated = recreatedTree.size();

        System.out.println("  Storage [ " + numInStore + " ]");
        System.out.println("  Recreated [ " + numRecreated + " ]");

        boolean anyMissing = false;
        for ( ConfigID configId : recreatedTree ) {
            if ( !idStore.containsKey(configId) ) {
                System.out.println("Missing ID [ " + configId + " ]");
                anyMissing = true;
            }

            for ( ConfigID parentId = configId.getParent();
                  parentId != null;
                  parentId = parentId.getParent() ) {

                if ( !idStore.containsKey(parentId) ) {
                    System.out.println("Missing parent [ " + parentId + " ] of [ " + configId + " ]");
                    anyMissing = true;
                }
            }
        }

        assertEquals("Unexpected stored IDs", numUniqueIds, numInStore);
        assertEquals("Unexpected recreated IDs", numUniqueIds, numRecreated);
        assertFalse("IDs are missing from storage", anyMissing);
    }

    @Test
    public void testValidation() throws Exception {
        String badPidSlash = "xxx // xxx"; // Double slash is never valid.
        String badPidParen = "xxx ( xxx"; // An open paren causes the PID to terminate too soon.
        String badPidBracket = "xxx [ xxx"; // An open bracket causes the PID to terminate too soon.

        String badChildSlash = "xxx // xxx"; // Double slash is never valid.
        String badChildParen = "xxx ) xxx"; // A close parent causes the child attribute to terminate too soon.

        String badIdSlash = "xxx // xxx"; // Double slash is never valid.
        String badIdBracket = "xxx ] xxx"; // A close bracket causes the ID to terminate too soon.

        boolean failed = false;
        if ( !testValidation(badPidSlash, null, null) ) {
            failed = true;
        }
        if ( !testValidation(badPidParen, null, null)) {
            failed = true;
        }
        if ( !testValidation(badPidBracket, null, null)) {
            failed = true;
        }
        if ( !testValidation(null, null, badChildSlash)) {
            failed = true;
        }
        if ( !testValidation(null, null, badChildParen) ) {
            failed = true;
        }
        if ( !testValidation(null, badIdSlash, null)) {
            failed = true;
        }
        if ( !testValidation(null, badIdBracket, null)) {
            failed = true;
        }

        assertFalse("Validation failure", failed);
    }

    protected boolean testValidation(String pid, String id, String childAttribute) {
        ConfigID configId;
        IllegalArgumentException boundException;
        try {
            configId = new ConfigID(null, pid, id, childAttribute, ConfigID.DO_VALIDATE);
            boundException = null;
        } catch ( IllegalArgumentException e ) {
            configId = null;
            boundException = e;
        }

        System.out.println("PID [ " + pid + " ] Child [ " + childAttribute + " ] ID [ " + id + " ]");
        System.out.println("  ConfigID [ " + configId + " ]");
        System.out.println("  Exception [ " + boundException + " ]");

        return ( boundException != null );
    }
}
