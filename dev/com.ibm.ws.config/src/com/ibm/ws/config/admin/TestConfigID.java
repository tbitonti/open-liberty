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
package com.ibm.ws.config.admin;

public class TestConfigID {
    public static final String[][] PRINT_STRINGS =

                    { { "PID", null },
                      { "PID(child)", null },
                      { "PID(child)[ID]", null },
                      { "PID[ID]", null },

                      { "", "Empty" },

                      { "()", "Empty PID" },
                      { "(child)", "Empty PID" },
                      { "[]", "Empty PID" },
                      { "[ID]", "Empty PID" },
                      { "()[ID]", "Empty PID" },
                      { "(child)[]", "Empty PID" },
                      { "()[]", "Empty PID" },

                      { "PID()", "Empty child attribute" },
                      { "PID[]", "Empty ID" },
                      { "PID()[]", "Empty child attribute" },
                      { "PID(child)[]", "Empty ID" },
                      { "PID()[ID]", "Empty child attribute" },

                      { "/", "Unexpected slash character" },
                      { "/PID(child)[PID]", "Unexpected slash character" },
                      { "PID/(child)[PID]", "Unexpected slash character" },
                      { "PID(/child)[PID]", "Unexpected slash character" },
                      { "PID(child/)[PID]", "Unexpected slash character" },
                      { "PID(child)[/PID]", "Unexpected slash character" },
                      { "PID(child)[PID/]", "Unexpected slash character" },

                      { "PID[ID](child)", "Child attribute after ID" },
                      { "PID(child)x", "Character after child attribute" },
                      { "PID[ID]x", "Character after ID" },
                      { "PID(child)x[ID]", "Character after child attribute" },
                      { "PID(child)[ID]x", "Character after ID" },
                      { "PID(child)x[ID]x", "Character after child attribute" },
                      { "PID(child", "Unclosed child attribute" },
                      { "PID(child[ID]", "Unexpected open bracket" },
                      { "PID[ID", "Unclosed ID" },
                      { "PID(child)[ID", "Unclosed ID" },
                      { "PID(()[ID]", "Unexpected open parentheses" },
                      { "PID([)[ID]", "Unexpected open bracket" },
                      { "PID(])[ID]", "Unexpected close bracket" },
                      { "PID(child)[(]", "Unexpected open parentheses" },
                      { "PID(child)[)]", "Unexpected close parenthesis" },
                      { "PID(child)[[]", "Unexpected open bracket" },
                    };

    private static final String TAIL_MARKER = "not valid: ";

    public static final String[] PROPERTY_STRINGS = { "PID",
                                                      "PID(child)",
                                                      "PID[ID]",
                                                      "PID(child)[ID]" };

    public static void main(String[] args) {
        testPrintStrings();
        testPropertyStrings();
    }

    public static void testPropertyStrings() {
        int pCount = PROPERTY_STRINGS.length;

        for (int pNo = 0; pNo < pCount; pNo++) {
            String initialPString = PROPERTY_STRINGS[pNo];
            testProperty(initialPString);
        }

        for (int pNo1 = 0; pNo1 < pCount; pNo1++) {
            for (int pNo2 = 0; pNo2 < pCount; pNo2++) {
                if (pNo1 == pNo2) {
                    continue;
                }
                String initialPString = PROPERTY_STRINGS[pNo1] + "//" + PROPERTY_STRINGS[pNo2];
                testProperty(initialPString);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (int pNo1 = 0; pNo1 < pCount; pNo1++) {
            for (int pNo2 = 0; pNo2 < pCount; pNo2++) {
                if (pNo1 == pNo2) {
                    continue;
                }
                if (builder.length() != 0) {
                    builder.append("//");
                }
                builder.append(PROPERTY_STRINGS[pNo1]);
            }

            String initialPString = builder.toString();
            builder.setLength(0);

            testProperty(initialPString);
        }

        for (int pNo1 = 0; pNo1 < pCount; pNo1++) {
            if (builder.length() != 0) {
                builder.append("//");
            }
            builder.append(PROPERTY_STRINGS[pNo1]);
        }

        String initialPString = builder.toString();
        builder.setLength(0);

        testProperty(initialPString);
    }

    public static void testProperty(String initialPString) {
        ConfigID configID = ConfigID.fromProperty(initialPString);
        String finalPString = configID.toString();

        String prefix = (finalPString.equals(initialPString) ? "OK" : "KO");
        System.out.println(prefix + " [ " + initialPString + " ] [ " + finalPString + " ]");
    }

    public static void testPrintStrings() {
        for (String[] nonValidData : PRINT_STRINGS) {
            String printString = nonValidData[0];
            String expectedMessage = nonValidData[1];
            if (expectedMessage == null) {
                expectedMessage = printString;
            }

            ConfigID configId;
            String message;
            try {
                configId = ConfigID.parseConfigId(printString);
                message = configId.toString();
            } catch (IllegalArgumentException e) {
                message = e.getMessage();
                int tailLoc = message.indexOf(TAIL_MARKER);
                if (tailLoc != -1) {
                    message = message.substring(tailLoc + TAIL_MARKER.length());
                }
            }

            String prefix = (message.equals(expectedMessage) ? "OK" : "KO");
            System.out.println(prefix + " [ " + printString + " ] [ " + message + " ]");
        }
    }
}
