/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2012
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.url.test;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
class ParseTest {

    private static final String TEST_NAME = "WSJarTest";

    static void test(PrintWriter writer) {
        testGroups(false, writer);
        testGroups(true, writer);
    }

    private static class Test {
        public String base;
        public String relative;
        public String result;
        public boolean skipJar;

        public Test(String base) {
            this(base, false);
        }

        public Test(String base, boolean skipJar) {
            this.base = base;
            this.skipJar = skipJar;
        }

        public Test(String base, String relative, String result) {
            this(base, relative, result, false);
        }

        public Test(String base, String relative, String result, boolean skipJar) {
            this.base = base;
            this.relative = relative;
            this.result = result;
            this.skipJar = skipJar;
        }
    }

    private static class TestGroup {
        private final String name;
        private final Test[] tests;
        private boolean skipJar;

        public TestGroup(String name, Test[] tests) {
            this.name = name;
            this.tests = tests;
        }

        public TestGroup(String name, Test[] tests, boolean skipJar) {
            this(name, tests);
            this.skipJar = skipJar;
        }
    }

    private static TestGroup[] testGroups = {
                                             new TestGroup("scheme-without-entry", new Test[] {
                                                                                               new Test("jar:"),
                                                                                               new Test("jar:file:"),
                                                                                               new Test("jar:file:a.jar"),
                                                                                               new Test("jar:file:a.jar!"),
                                             }, true), // JDK requires "!/"

                                             new TestGroup("scheme", new Test[] {
                                                                                 new Test("jar:file:a.jar!/"),
                                                                                 new Test("jar:file:a.jar!//"),
                                                                                 new Test("jar:file:a.jar!/./"),
                                                                                 new Test("jar:file:a/b!/c/d", "jar:file:e!/f", "jar:file:e!/f"),
                                             }),

                                             new TestGroup("relative-without-entry", new Test[] {
                                                                                                 new Test("jar:file:a/b", "", "jar:file:a/b"),
                                                                                                 new Test("jar:file:a/b", "c", "jar:file:a/c"),
                                                                                                 new Test("jar:file:a/b", "c/d", "jar:file:a/c/d"),
                                                                                                 new Test("jar:file:a/b", "../", "jar:file:a/../"),
                                             }, true),

                                             new TestGroup("relative", new Test[] {
                                                                                   new Test("jar:file:a/b!/c/d", "", "jar:file:a/b!/c/"),
                                                                                   new Test("jar:file:a/b!/c/d/", "", "jar:file:a/b!/c/d/"),
                                                                                   new Test("jar:file:a/b!/c/d", "/", "jar:file:a/b!/"),
                                                                                   new Test("jar:file:a/b!/c/d/", "/", "jar:file:a/b!/"),

                                                                                   new Test("jar:file:a/b!/c/d", "e", "jar:file:a/b!/c/e"),
                                                                                   new Test("jar:file:a/b!/c/d/", "e", "jar:file:a/b!/c/d/e"),
                                                                                   new Test("jar:file:a/b!/c/d", "e/", "jar:file:a/b!/c/e/"),
                                                                                   new Test("jar:file:a/b!/c/d/", "e/", "jar:file:a/b!/c/d/e/"),

                                                                                   new Test("jar:file:a/b!/c/d", "e/f", "jar:file:a/b!/c/e/f"),
                                                                                   new Test("jar:file:a/b!/c/d/", "e/f", "jar:file:a/b!/c/d/e/f"),
                                                                                   new Test("jar:file:a/b!/c/d", "e/f/", "jar:file:a/b!/c/e/f/"),
                                                                                   new Test("jar:file:a/b!/c/d/", "e/f/", "jar:file:a/b!/c/d/e/f/"),

                                                                                   new Test("jar:file:a/b!/c/d", "/e", "jar:file:a/b!/e"),
                                                                                   new Test("jar:file:a/b!/c/d/", "/e", "jar:file:a/b!/e"),
                                                                                   new Test("jar:file:a/b!/c/d", "/e/", "jar:file:a/b!/e/"),
                                                                                   new Test("jar:file:a/b!/c/d/", "/e/", "jar:file:a/b!/e/"),

                                                                                   new Test("jar:file:a/b!/c/d", "e/f!/g/h", "jar:file:a/b!/c/e/f!/g/h"),
                                                                                   new Test("jar:file:a/b!/c/d/", "e/f!/g/h", "jar:file:a/b!/c/d/e/f!/g/h"),
                                                                                   new Test("jar:file:a/b!/c/d", "e/f!/g/h/", "jar:file:a/b!/c/e/f!/g/h/"),
                                                                                   new Test("jar:file:a/b!/c/d/", "e/f!/g/h/", "jar:file:a/b!/c/d/e/f!/g/h/"),
                                             }),

                                             new TestGroup("canonicalization", new Test[] {
                                                                                           new Test("jar:file:!/c/d", ".", "jar:file:!/c/"),
                                                                                           new Test("jar:file:!/c/d/", ".", "jar:file:!/c/d/"),
                                                                                           new Test("jar:file:!/c/d", "./", "jar:file:!/c/"),
                                                                                           new Test("jar:file:!/c/d/", "./", "jar:file:!/c/d/"),

                                                                                           new Test("jar:file:!/c/d", "..", "jar:file:!/"),
                                                                                           new Test("jar:file:!/c/d/", "..", "jar:file:!/c/"),
                                                                                           new Test("jar:file:!/c/d", "../", "jar:file:!/"),
                                                                                           new Test("jar:file:!/c/d/", "../", "jar:file:!/c/"),

                                                                                           new Test("jar:file:a/b!/c/d", "../..//", "jar:file:a/b!//"),
                                                                                           new Test("jar:file:a/b!/c/d/", "../..//", "jar:file:a/b!//"),

                                                                                           new Test("jar:file:a/b!/c/d", "../e", "jar:file:a/b!/e"),
                                                                                           new Test("jar:file:a/b!/c/d/", "../e", "jar:file:a/b!/c/e"),
                                                                                           new Test("jar:file:a/b!/c/d", "../e/", "jar:file:a/b!/e/"),
                                                                                           new Test("jar:file:a/b!/c/d/", "../e/", "jar:file:a/b!/c/e/"),

                                                                                           new Test("jar:file:!/.", "", "jar:file:!/"),
                                                                                           new Test("jar:file:!/./", "", "jar:file:!/"),
                                                                                           new Test("jar:file:!/./a", "", "jar:file:!/"),
                                                                                           new Test("jar:file:!/./a/", "", "jar:file:!/a/"),

                                                                                           new Test("jar:file:!/a/.", "", "jar:file:!/a/"),
                                                                                           new Test("jar:file:!/a/./", "", "jar:file:!/a/"),
                                                                                           new Test("jar:file:!/a/./b", "", "jar:file:!/a/"),
                                                                                           new Test("jar:file:!/a/./b/", "", "jar:file:!/a/b/"),

                                                                                           new Test("jar:file:!/..", "", "jar:file:!/"),
                                                                                           new Test("jar:file:!/../", "", "jar:file:!/"),
                                                                                           new Test("jar:file:!/../a", "", "jar:file:!/"),
                                                                                           new Test("jar:file:!/../a/", "", "jar:file:!/a/"),

                                                                                           new Test("jar:file:!/a/..", "", "jar:file:!/", true), // JDK is wrong, see 416343
                                                                                           new Test("jar:file:!/a/../", "", "jar:file:!/"),
                                                                                           new Test("jar:file:!/a/../b", "", "jar:file:!/"),
                                                                                           new Test("jar:file:!/a/../b/", "", "jar:file:!/b/"),
                                             }),
    };

    private static void testGroups(boolean wsjar, PrintWriter writer) {
        writer.println(TEST_NAME + ": testGroups(wsjar=" + wsjar + ")");

        for (int i = 0; i < testGroups.length; i++) {
            if (wsjar || !testGroups[i].skipJar) {
                testGroup(testGroups[i], wsjar, writer);
            }
        }
    }

    private static void testGroup(TestGroup group, boolean wsjar, PrintWriter writer) {
        String testName = (wsjar ? "wsjar-parse-" : "jar-parse-") + group.name;
        writer.println(TEST_NAME + "." + testName + ": testGroup");

        boolean failed = false;

        for (int i = 0; i < group.tests.length; i++) {
            Test test = group.tests[i];

            // If this test is known not to work for "jar:" and that's what
            // we're running with, then skip the test.
            if (!wsjar && test.skipJar) {
                continue;
            }

            String base = test.base;
            if (wsjar) {
                base = "ws" + base;
            }

            URL baseURL;
            try {
                baseURL = new URL(base);
            } catch (MalformedURLException ex) {
                writer.println("FAIL: " + TEST_NAME + "." + testName + "[" + i + "]: new URL(\"" + base + "\")");
                ex.printStackTrace(System.out);
                failed = true;
                continue;
            }

            if (!baseURL.toString().equals(base)) {
                writer.println("FAIL: " + TEST_NAME + "." + testName + "[" + i + "]: new URL(\"" + base + "\") = \"" + baseURL + "\"");
                failed = true;
                continue;
            }

            String relative = test.relative;
            if (relative != null) {
                if (wsjar && relative.startsWith("jar:")) {
                    relative = "ws" + relative;
                }

                URL url;
                try {
                    url = new URL(baseURL, relative);
                } catch (MalformedURLException ex) {
                    writer.println("FAIL: " + TEST_NAME + "." + testName + "[" + i + "]: new URL(new URL(\"" + baseURL + "\"), \"" + relative + "\")");
                    ex.printStackTrace(System.out);
                    failed = true;
                    continue;
                }

                String result = test.result;
                if (wsjar) {
                    result = "ws" + result;
                }

                if (!url.toString().equals(result)) {
                    writer.println("FAIL: " + TEST_NAME + "." + testName + "[" + i + "]: new URL(new URL(\"" + baseURL + "\"), \"" + relative
                                       + "\") = \"" + url + "\" != \"" + result + "\"");
                    failed = true;
                    continue;
                }
            }
        }

        if (!failed) {
            writer.println("PASS: all wsjar parse tests passed.");
        }
    }
}
