/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.config.xml.internal;

//@formatter:off
public class StringUtilsTest {
    public static void main(String[] args) {
        StringUtilsTest.testNames();
        StringUtilsTest.testToString();
    }

    public static final String[] TEST_NAMES = {
        "",
        " ", "!", "!!", "!!!",
        "a", "a b", "a9 b1",
        "A", "A B", "A9 B1",
        "!a!b", "!a!a!b!b", "!a!a!a!b!b!b"
    };

    public static void testNames() {
        System.out.println("Testing [ " + TEST_NAMES.length + "] values:");

        for ( String name : TEST_NAMES ) {
            String converted = StringUtils.replaceNonAlpha(name);
            System.out.println("  [ " + name + " ] -> [ " + converted + " ]");
        }
    }

    public static final String[] TEST_STRINGS = {
        null,
        "a", ",", "\\",
        "abc", "abc,abc", "abc\\abc", "abc,/abc", "abc,abc,abc,abc"
    };

    public static final String[][] TEST_STRING_ARRAYS = {
        { }, { "a" }, { "a", "b" }, { "a", "b", "c" },
        { "a,a", "b,b", "c,c" }
    };

    public static final Object[][] TEST_ARRAYS = {
        { }, { "a" }, { "a", "b" }, { "a", "b", "c" },
        { "a,a", "b,b", "c,c" },
        { Integer.valueOf(0) },
        { Integer.valueOf(0), Integer.valueOf(1) },
        { Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2) }
    };

    public static final Integer[] TEST_INTEGER = { Integer.valueOf(9) };

    public static void testToString() {

        System.out.println("Test 'convertoString' on simple string values:");
        for ( String string : TEST_STRINGS ) {
            String toString = StringUtils.convertToString(string);
            System.out.println("  [ " + string + " ] -> [ " + toString + " ]");
        }

        System.out.println("Test 'convertoString' on string array values:");
        for ( String[] strings : TEST_STRING_ARRAYS ) {
            String toString = StringUtils.convertToString(strings);
            System.out.println("  [ " + strings + " ] -> [ " + toString + " ]");
        }

        System.out.println("Test 'convertoString' on array values:");
        for ( Object[] array : TEST_ARRAYS ) {
            String toString = StringUtils.convertToString(array);
            System.out.println("  [ " + array + " ] -> [ " + toString + " ]");
        }

        System.out.println("Test 'convertoString' on unknown value:");
        for ( Integer integer : TEST_INTEGER ) {
            String toString = StringUtils.convertToString(integer);
            System.out.println("  [ " + integer + " ] -> [ " + toString + " ]");
        }
    }
}
//@formatter:on