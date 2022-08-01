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
package com.ibm.ws.config.xml.internal.metatype;

import java.util.List;

//@formatter:off
public class MetaTypeTester {

    public static final String[][] TEST_DATA = {
                { "", "" },
                { " ", "" },
                { "  ", "" },
                { ",", "", "" },
                { " ,", "", "" },
                { ", ", "", "" },
                { " , ", "", "" },
                { "a", "a" },
                { " a", "a" },
                { "a ", "a" },
                { " a ", "a" },
                { "a,", "a", "" },
                { " a,", "a", "" },
                { "a ,", "a", "" },
                { "a, ", "a", "" },
                { " a ,", "a", "" },
                { " a, ", "a", "" },
                { "a , ", "a", "" },
                { " a , ", "a", "" },
                { ",a", "", "a" },
                { " ,a", "", "a" },
                { ", a", "", "a" },
                { ",a ", "", "a" },
                { " , a", "", "a" },
                { " ,a ", "", "a" },
                { ", a ", "", "a" },
                { " , a ", "", "a" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { ",a,", "", "a", "" },
                { " , a ,", "", "a", "" },
                { " , a, ", "", "a", "" },
                { " ,a , ", "", "a", "" },
                { ", a , ", "", "a", "" },
                { " , a , ", "", "a", "" },
                { "a,b", "a", "b" },
                { " a,b", "a", "b" },
                { "a ,b", "a", "b" },
                { "a, b", "a", "b" },
                { "a,b ", "a", "b" },
                { " a ,b", "a", "b" },
                { " a, b", "a", "b" },
                { " a,b ", "a", "b" },
                { "a , b", "a", "b" },
                { "a ,b ", "a", "b" },
                { "a, b ", "a", "b" },
                { " a , b", "a", "b" },
                { " a ,b ", "a", "b" },
                { " a, b ", "a", "b" },
                { "a , b ", "a", "b" },
                { " a , b ", "a", "b" },

                { "\\", "" },
                { " \\", "" },
                { "\\ ", " " },
                { " \\ ", " " },

                { "\\,", "," },
                { " \\,", "," },
                { "\\ ,", " ", "" },
                { "\\, ", "," },
                { " \\ ,", " ", "" },
                { " \\, ", "," },
                { "\\ , ", " ", "" },
                { " \\ , ", " ", "" },

                { "\\a", "a" },
                { " \\a", "a" },
                { "a \\", "a " },
                { "a\\ ", "a " },
                { " \\ a", " a" },
                { " a\\ ", "a " },
                { "a \\ ", "a  " },
                { " \\ a ", " a" },
                { "a\\a", "aa" },
                { " a\\", "a" },
                { "\\ a", " a" },
                { "\\a ", "a" },
                { " a \\", "a " },
                { " \\a ", "a" },
                { "\\ a ", " a" },
                { " a \\ ", "a  " },

                { "  \\   ", " " },
                { "   \\   \\   ", "    " },
                { "\\ \\ ", "  " },
                { "\\ \\ abc\\ \\ ", "  abc  " },
                { "\\ \\ \\ \\ ", "    " },
                { "\\    abc   \\ ", "    abc    " },
                { "\\  \\  abc  \\  \\ ", "    abc     " },
                { "\\  \\  abc  def  \\  \\ ", "    abc  def     " },
                { "\\  \\  abc \\  def  \\  \\ ", "    abc   def     " } };

    private static String okText(boolean value) {
        return value ? "OK" : "KO";
    }

    public static void main(String[] args) {
        System.out.println("Test: " + MetaTypeTester.class.getSimpleName());

        String prefix = "Cases: [ " + TEST_DATA.length + " ]:";
        System.out.println(prefix);

        int failures = 0;

        StringBuilder builder = new StringBuilder();
        for ( String[] expectedData : TEST_DATA ) {
            String unparsed = expectedData[0];
            List<String> parsedList = MetaTypeHelper.parseValue(unparsed);
            formatParsed(unparsed, parsedList, builder);

            boolean verified = verify(parsedList, expectedData);
            builder.append(": ");
            builder.append(okText(verified));

            System.out.println( builder.toString() );
            builder.setLength(0);

            if ( !verified ) {
                failures++;
            }
        }

        String suffix = " " + okText(failures == 0);
        if ( failures != 0 ) {
            suffix += " [ " + failures + " failures ]";
        }
        System.out.println( prefix + suffix );
    }

    public static boolean verify(List<String> actual, String[] expected) {
        String unparsed = expected[0];

        int actualSize = actual.size();
        int expectedSize = expected.length - 1;

        if ( actualSize != expectedSize ) {
            System.out.println("KO: Value [ " + unparsed + " ]:" +
                               " Actual elements [ " + actual + " ];" +
                               " Expected Elements [ " + expected + " ]");
            return false;
        }

        boolean verified = true;
        for ( int offset = 0; offset < expectedSize; offset++ ) {
            String actualElement = actual.get(offset);
            String expectedElement = expected[offset + 1];

            if ( !actualElement.equals(expectedElement) ) {
                System.out.println("KO: Value [ " + unparsed + " ] element [ " + offset + " ]:" +
                                   " Actual [ " + actualElement + " ] Expected [ " + expectedElement + " ]");
                verified = false;
            }
        }
        return verified;
    }

    public static void formatParsed(String unparsed, List<String> parsedList, StringBuilder builder) {
        builder.append("[ \"");
        builder.append(unparsed);
        builder.append("\" ]: { ");

        boolean isFirst = true;
        for ( String parsedElement : parsedList ) {
            if ( isFirst ) {
                isFirst = false;
            } else {
                builder.append(", ");
            }
            builder.append('\"');
            builder.append(parsedElement);
            builder.append('\"');
        }

        builder.append(" }");
    }
}
//@formatter:on