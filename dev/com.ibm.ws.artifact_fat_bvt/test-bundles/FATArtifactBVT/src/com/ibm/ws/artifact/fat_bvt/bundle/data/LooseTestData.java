/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2011, 2017
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.fat_bvt.bundle.data;

import com.ibm.ws.artifact.fat_bvt.bundle.Fs;

/**
 * <p>Loose configuration test file set.</p>
 */
public class LooseTestData {
    public static Fs TESTDATA =
        Fs.Root( null, null, null,
                 Fs.DOES_NOT_HAVE_DATA, null, 0,
                 null,
                 0, new String[] {},

                 Fs.Root( "TEST.jar", "/TEST.jar", "file:#/xmlContentTestData/a/",
                          Fs.DOES_NOT_HAVE_DATA, null, 0,
                          "#\\xmlContentTestData\\a",
                          3, new String[] {
                              "file:#/xmlContentTestData/a/",
                              "file:#/TESTDATA/a/",
                              "file:#/TESTDATA/a/aa/" },

                          Fs.File( "a.txt", "/a.txt",
                                   Fs.DOES_HAVE_DATA, "xml content is present.", 23,
                                   "file:#/xmlContentTestData/a/a.txt", "#\\xmlContentTestData\\a\\a.txt" ),

                          Fs.Dir( "aa", "/aa",
                                  "file:#/TESTDATA/a/aa/", "#\\TESTDATA\\a\\aa",
                                  1, new String[] { "file:#/TESTDATA/a/aa/" },

                                  Fs.File( "aa.txt", "/aa/aa.txt",
                                           Fs.DOES_HAVE_DATA, "wibble", 6,
                                           "file:#/TESTDATA/a/aa/aa.txt", "#\\TESTDATA\\a\\aa\\aa.txt" ) ),

                          Fs.Dir( "ab", "/ab",
                                  "file:#/TESTDATA/a/ab/", "#\\TESTDATA\\a\\ab",
                                  1, new String[] { "file:#/TESTDATA/a/ab/" },

                                  Fs.File( "ab.txt", "/ab/ab.txt",
                                           Fs.DOES_HAVE_DATA, "fish", 4,
                                           "file:#/TESTDATA/a/ab/ab.txt", "#\\TESTDATA\\a\\ab\\ab.txt" ),

                                  Fs.Dir( "aba", "/ab/aba",
                                          "file:#/TESTDATA/a/ab/aba/", "#\\TESTDATA\\a\\ab\\aba",
                                          1, new String[] { "file:#/TESTDATA/a/ab/aba/" },

                                          Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                   Fs.DOES_HAVE_DATA, "cheese", 6,
                                                   "file:#/TESTDATA/a/ab/aba/aba.txt", "#\\TESTDATA\\a\\ab\\aba\\aba.txt" ) ) ),

                          Fs.File( "aa.txt", "/aa.txt",
                                   Fs.DOES_HAVE_DATA, "wibble", 6, "file:#/TESTDATA/a/aa/aa.txt",
                                   "#\\TESTDATA\\a\\aa\\aa.txt") ),

                 Fs.Root( "webApp.war", "/webApp.war", "file:#/TESTDATA/",
                          Fs.DOES_NOT_HAVE_DATA, null, 0,
                          "#\\TESTDATA",
                          1, new String[] { "file:#/TESTDATA/" },

                          Fs.Dir( "b", "/b",
                                  "file:#/TESTDATA/b/", "#\\TESTDATA\\b",
                                  1, new String[] { "file:#/TESTDATA/b/" },

                                  Fs.Dir( "ba", "/b/ba",
                                          "file:#/TESTDATA/b/ba/", "#\\TESTDATA\\b\\ba",
                                          1, new String[] { "file:#/TESTDATA/b/ba/" },

                                          Fs.Dir( "baa", "/b/ba/baa",
                                                  "file:#/TESTDATA/b/ba/baa/", "#\\TESTDATA\\b\\ba\\baa",
                                                  1, new String[] { "file:#/TESTDATA/b/ba/baa/" },

                                                  Fs.File( "baa1.txt", "/b/ba/baa/baa1.txt",
                                                           Fs.DOES_HAVE_DATA, "minion", 6,
                                                           "file:#/TESTDATA/b/ba/baa/baa1.txt", "#\\TESTDATA\\b\\ba\\baa\\baa1.txt" ),

                                                  Fs.File( "baa2.txt", "/b/ba/baa/baa2.txt",
                                                           Fs.DOES_HAVE_DATA, "chain", 5,
                                                           "file:#/TESTDATA/b/ba/baa/baa2.txt", "#\\TESTDATA\\b\\ba\\baa\\baa2.txt" ) ) ),
                                  Fs.Dir( "bb", "/b/bb",
                                          "file:#/TESTDATA/b/bb/", "#\\TESTDATA\\b\\bb",
                                          1, new String[] { "file:#/TESTDATA/b/bb/" },

                                          Fs.Root( "a.jar", "/b/bb/a.jar", "file:#/TESTDATA/b/bb/a.jar",
                                                   Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                   "#\\TESTDATA\\b\\bb\\a.jar",
                                                   1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/" },

                                                   Fs.Dir( "aa", "/aa",
                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/aa", null,
                                                           1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/aa/" },

                                                           Fs.File( "aa.txt", "/aa/aa.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/aa/aa.txt", null ) ),

                                                   Fs.Dir( "ab", "/ab",
                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/ab", null,
                                                           1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/" },

                                                           Fs.Dir( "aba", "/ab/aba",
                                                                   "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba", null,
                                                                   1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/" },

                                                                   Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                           Fs.File( "ab.txt", "/ab/ab.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/ab/ab.txt", null ) ),

                                                   Fs.File( "a.txt", "/a.txt",
                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                            "jar:file:#/TESTDATA/b/bb/a.jar!/a.txt", null ),

                                                   Fs.Dir( "META-INF", "/META-INF",
                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF", null,
                                                           1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/" },

                                                           Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                    Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ) ),

                          Fs.Dir( "c", "/c",
                                  "file:#/TESTDATA/c/", "#\\TESTDATA\\c",
                                  1, new String[] { "file:#/TESTDATA/c/" },

                                  Fs.Root( "a.jar", "/c/a.jar", "file:#/TESTDATA/c/a.jar",
                                           Fs.DOES_NOT_HAVE_DATA, null, 967,
                                           "#\\TESTDATA\\c\\a.jar",
                                           1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/" },

                                           Fs.Dir( "aa", "/aa",
                                                   "jar:file:#/TESTDATA/c/a.jar!/aa", null,
                                                   1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/aa/" },

                                                   Fs.File( "aa.txt", "/aa/aa.txt",
                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                            "jar:file:#/TESTDATA/c/a.jar!/aa/aa.txt", null ) ),
                                           Fs.Dir( "ab", "/ab",
                                                   "jar:file:#/TESTDATA/c/a.jar!/ab", null,
                                                   1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/" },

                                                   Fs.Dir( "aba", "/ab/aba",
                                                           "jar:file:#/TESTDATA/c/a.jar!/ab/aba", null,
                                                           1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/aba/" },

                                                           Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/TESTDATA/c/a.jar!/ab/aba/aba.txt", null ) ),

                                                   Fs.File( "ab.txt", "/ab/ab.txt",
                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                            "jar:file:#/TESTDATA/c/a.jar!/ab/ab.txt", null ) ),

                                           Fs.File( "a.txt", "/a.txt",
                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                    "jar:file:#/TESTDATA/c/a.jar!/a.txt", null ),

                                           Fs.Dir( "META-INF", "/META-INF",
                                                   "jar:file:#/TESTDATA/c/a.jar!/META-INF", null,
                                                   1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/META-INF/" },

                                                   Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                            Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                            "jar:file:#/TESTDATA/c/a.jar!/META-INF/MANIFEST.MF", null ) ) ),

                                  Fs.Root( "b.jar", "/c/b.jar", "file:#/TESTDATA/c/b.jar",
                                           Fs.DOES_NOT_HAVE_DATA, null, 1227,
                                           "#\\TESTDATA\\c\\b.jar",
                                           1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/" },

                                           Fs.Dir( "bb", "/bb",
                                                   "jar:file:#/TESTDATA/c/b.jar!/bb", null,
                                                   1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/bb/" },

                                                   Fs.Root( "a.jar", "/bb/a.jar", "jar:file:#/TESTDATA/c/b.jar!/bb/a.jar",
                                                            Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                            null,
                                                            1, new String[] { "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/" },

                                                            Fs.Dir( "aa", "/aa",
                                                                    "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/aa", null,
                                                                    1, new String[] { "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/aa/" },

                                                                    Fs.File( "aa.txt", "/aa/aa.txt",
                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                             "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", null ) ),

                                                            Fs.Dir( "ab", "/ab",
                                                                    "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/ab", null,
                                                                    1, new String[] { "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/ab/" },

                                                                    Fs.Dir( "aba", "/ab/aba",
                                                                            "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/ab/aba", null,
                                                                            1, new String[] { "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/ab/aba/" },

                                                                            Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                                     "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                                    Fs.File( "ab.txt", "/ab/ab.txt",
                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                             "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", null ) ),

                                                            Fs.File( "a.txt", "/a.txt",
                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                     "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/a.txt", null ),

                                                            Fs.Dir( "META-INF", "/META-INF",
                                                                    "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/META-INF", null,
                                                                    1, new String[] { "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/META-INF/" },

                                                                    Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                             Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                             "jar:file:#/cacheDir/webApp.war/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

                                           Fs.Dir( "META-INF", "/META-INF",
                                                   "jar:file:#/TESTDATA/c/b.jar!/META-INF", null,
                                                   1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/META-INF/" },

                                                   Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                            Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                            "jar:file:#/TESTDATA/c/b.jar!/META-INF/MANIFEST.MF", null ) ),

                                           Fs.Dir( "ba", "/ba",
                                                   "jar:file:#/TESTDATA/c/b.jar!/ba", null,
                                                   1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/" },

                                                   Fs.Dir( "baa", "/ba/baa",
                                                           "jar:file:#/TESTDATA/c/b.jar!/ba/baa", null,
                                                           1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/baa/" },

                                                           Fs.File( "baa1.txt", "/ba/baa/baa1.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa1.txt", null ),

                                                           Fs.File( "baa2.txt", "/ba/baa/baa2.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa2.txt", null ) ) ) ) ),

                          Fs.Dir( "WEB-INF", "/WEB-INF",
                                  "null", null,
                                  0, new String[] {},

                                  Fs.Dir( "classes", "/WEB-INF/classes",
                                          "null", null,
                                          1, new String[] { "file:#/TESTDATA/" },

                                          Fs.Dir( "a", "/WEB-INF/classes/a",
                                                  "file:#/TESTDATA/a/", "#\\TESTDATA\\a",
                                                  1, new String[] { "file:#/TESTDATA/a/" },

                                                  Fs.Dir( "aa", "/WEB-INF/classes/a/aa",
                                                          "file:#/TESTDATA/a/aa/", "#\\TESTDATA\\a\\aa",
                                                          1, new String[] { "file:#/TESTDATA/a/aa/" }),

                                                  Fs.Dir( "ab", "/WEB-INF/classes/a/ab",
                                                          "file:#/TESTDATA/a/ab/", "#\\TESTDATA\\a\\ab",
                                                          1, new String[] { "file:#/TESTDATA/a/ab/" },

                                                          Fs.Dir( "aba", "/WEB-INF/classes/a/ab/aba",
                                                                  "file:#/TESTDATA/a/ab/aba/", "#\\TESTDATA\\a\\ab\\aba",
                                                                  1, new String[] { "file:#/TESTDATA/a/ab/aba/" }) ) ),

                                          Fs.Dir( "b", "/WEB-INF/classes/b",
                                                  "file:#/TESTDATA/b/", "#\\TESTDATA\\b",
                                                  1, new String[] { "file:#/TESTDATA/b/" },

                                                  Fs.Dir( "ba", "/WEB-INF/classes/b/ba",
                                                          "file:#/TESTDATA/b/ba/", "#\\TESTDATA\\b\\ba",
                                                          1, new String[] { "file:#/TESTDATA/b/ba/" },

                                                          Fs.Dir( "baa", "/WEB-INF/classes/b/ba/baa",
                                                                  "file:#/TESTDATA/b/ba/baa/", "#\\TESTDATA\\b\\ba\\baa",
                                                                  1, new String[] { "file:#/TESTDATA/b/ba/baa/" }) ),

                                                  Fs.Dir( "bb", "/WEB-INF/classes/b/bb",
                                                          "file:#/TESTDATA/b/bb/", "#\\TESTDATA\\b\\bb",
                                                          1, new String[] { "file:#/TESTDATA/b/bb/" },

                                                          Fs.Root( "a.jar", "/WEB-INF/classes/b/bb/a.jar", "file:#/TESTDATA/b/bb/a.jar",
                                                                   Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                                   "#\\TESTDATA\\b\\bb\\a.jar",
                                                                   1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/" },

                                                                   Fs.Dir( "aa", "/aa",
                                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/aa", null,
                                                                           1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/aa/" },

                                                                           Fs.File( "aa.txt", "/aa/aa.txt",
                                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/aa/aa.txt", null ) ),

                                                                   Fs.Dir( "ab", "/ab",
                                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/ab", null,
                                                                           1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/" },

                                                                           Fs.Dir( "aba", "/ab/aba",
                                                                                   "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba", null,
                                                                                   1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/" },

                                                                                   Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                                            "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                                           Fs.File( "ab.txt", "/ab/ab.txt",
                                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/ab/ab.txt", null ) ),

                                                                   Fs.File( "a.txt", "/a.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/TESTDATA/b/bb/a.jar!/a.txt", null ),

                                                                   Fs.Dir( "META-INF", "/META-INF",
                                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF", null,
                                                                           1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/" },

                                                                           Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                                    Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ) ),

                                          Fs.Dir( "c", "/WEB-INF/classes/c",
                                                  "file:#/TESTDATA/c/", "#\\TESTDATA\\c",
                                                  1, new String[] { "file:#/TESTDATA/c/" },

                                                  Fs.Root( "a.jar", "/WEB-INF/classes/c/a.jar", "file:#/TESTDATA/c/a.jar",
                                                           Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                           "#\\TESTDATA\\c\\a.jar",
                                                           1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/" },

                                                           Fs.Dir( "aa", "/aa",
                                                                   "jar:file:#/TESTDATA/c/a.jar!/aa", null,
                                                                   1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/aa/" },

                                                                   Fs.File( "aa.txt", "/aa/aa.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/TESTDATA/c/a.jar!/aa/aa.txt", null ) ),

                                                           Fs.Dir( "ab", "/ab",
                                                                   "jar:file:#/TESTDATA/c/a.jar!/ab", null,
                                                                   1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/" },

                                                                   Fs.Dir( "aba", "/ab/aba",
                                                                           "jar:file:#/TESTDATA/c/a.jar!/ab/aba", null,
                                                                           1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/aba/" },

                                                                           Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                                    "jar:file:#/TESTDATA/c/a.jar!/ab/aba/aba.txt", null ) ),

                                                                   Fs.File( "ab.txt", "/ab/ab.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/TESTDATA/c/a.jar!/ab/ab.txt", null ) ),

                                                           Fs.File( "a.txt", "/a.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/TESTDATA/c/a.jar!/a.txt", null ),

                                                           Fs.Dir( "META-INF", "/META-INF",
                                                                   "jar:file:#/TESTDATA/c/a.jar!/META-INF", null,
                                                                   1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/META-INF/" },

                                                                   Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                            Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                            "jar:file:#/TESTDATA/c/a.jar!/META-INF/MANIFEST.MF", null ) ) ),

                                                  Fs.Root( "b.jar", "/WEB-INF/classes/c/b.jar", "file:#/TESTDATA/c/b.jar",
                                                           Fs.DOES_NOT_HAVE_DATA, null, 1227,
                                                           "#\\TESTDATA\\c\\b.jar",
                                                           1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/" },

                                                           Fs.Dir( "bb", "/bb",
                                                                   "jar:file:#/TESTDATA/c/b.jar!/bb", null,
                                                                   1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/bb/" },

                                                                   Fs.Root( "a.jar", "/bb/a.jar", "jar:file:#/TESTDATA/c/b.jar!/bb/a.jar",
                                                                            Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                                            null,
                                                                            1, new String[] { "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/" },

                                                                            Fs.Dir( "aa", "/aa",
                                                                                    "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/aa", null,
                                                                                    1, new String[] { "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/aa/" },

                                                                                    Fs.File( "aa.txt", "/aa/aa.txt",
                                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                                             "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", null ) ),

                                                                            Fs.Dir( "ab", "/ab",
                                                                                    "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/ab", null,
                                                                                    1, new String[] { "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/ab/" },

                                                                                    Fs.Dir( "aba", "/ab/aba",
                                                                                            "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/ab/aba", null,
                                                                                            1, new String[] { "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/ab/aba/" },

                                                                                            Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                                                     "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                                                    Fs.File( "ab.txt", "/ab/ab.txt",
                                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                                             "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", null ) ),

                                                                            Fs.File( "a.txt", "/a.txt",
                                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                                     "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/a.txt", null ),

                                                                            Fs.Dir( "META-INF", "/META-INF",
                                                                                    "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/META-INF", null,
                                                                                    1, new String[] { "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/META-INF/" },

                                                                                    Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                                             Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                                             "jar:file:#/cacheDir/webApp.war/WEB-INF/classes/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

                                                           Fs.Dir( "META-INF", "/META-INF",
                                                                   "jar:file:#/TESTDATA/c/b.jar!/META-INF", null,
                                                                   1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/META-INF/" },

                                                                   Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                            Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                            "jar:file:#/TESTDATA/c/b.jar!/META-INF/MANIFEST.MF", null ) ),

                                                           Fs.Dir( "ba", "/ba",
                                                                   "jar:file:#/TESTDATA/c/b.jar!/ba", null,
                                                                   1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/" },

                                                                   Fs.Dir( "baa", "/ba/baa",
                                                                           "jar:file:#/TESTDATA/c/b.jar!/ba/baa", null,
                                                                           1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/baa/" },

                                                                           Fs.File( "baa1.txt", "/ba/baa/baa1.txt",
                                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                                    "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa1.txt", null ),

                                                                           Fs.File( "baa2.txt", "/ba/baa/baa2.txt",
                                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                                    "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa2.txt", null ) ) ) ) ) ),

                                  Fs.Dir( "lib", "/WEB-INF/lib",
                                          "null", null,
                                          0, new String[] {},

                                          Fs.Root( "myutility.jar", "/WEB-INF/lib/myutility.jar", "file:#/TESTDATA/",
                                                   Fs.DOES_NOT_HAVE_DATA, null, 0,
                                                   "#\\TESTDATA",
                                                   1, new String[] { "file:#/TESTDATA/" },

                                                   Fs.Dir( "a", "/a",
                                                           "file:#/TESTDATA/a/", "#\\TESTDATA\\a",
                                                           1, new String[] { "file:#/TESTDATA/a/" },

                                                           Fs.Dir( "aa", "/a/aa",
                                                                   "file:#/TESTDATA/a/aa/", "#\\TESTDATA\\a\\aa",
                                                                   1, new String[] { "file:#/TESTDATA/a/aa/" }),

                                                           Fs.Dir( "ab", "/a/ab",
                                                                   "file:#/TESTDATA/a/ab/", "#\\TESTDATA\\a\\ab",
                                                                   1, new String[] { "file:#/TESTDATA/a/ab/" },

                                                                   Fs.Dir( "aba", "/a/ab/aba",
                                                                           "file:#/TESTDATA/a/ab/aba/", "#\\TESTDATA\\a\\ab\\aba",
                                                                           1, new String[] { "file:#/TESTDATA/a/ab/aba/" }) ) ),

                                                   Fs.Dir( "b", "/b",
                                                           "file:#/TESTDATA/b/", "#\\TESTDATA\\b",
                                                           1, new String[] { "file:#/TESTDATA/b/" },

                                                           Fs.Dir( "ba", "/b/ba",
                                                                   "file:#/TESTDATA/b/ba/", "#\\TESTDATA\\b\\ba",
                                                                   1, new String[] { "file:#/TESTDATA/b/ba/" },

                                                                   Fs.Dir( "baa", "/b/ba/baa",
                                                                           "file:#/TESTDATA/b/ba/baa/", "#\\TESTDATA\\b\\ba\\baa",
                                                                           1, new String[] { "file:#/TESTDATA/b/ba/baa/" }) ),

                                                           Fs.Dir( "bb", "/b/bb",
                                                                   "file:#/TESTDATA/b/bb/", "#\\TESTDATA\\b\\bb",
                                                                   1, new String[] { "file:#/TESTDATA/b/bb/" },

                                                                   Fs.Root( "a.jar", "/b/bb/a.jar", "file:#/TESTDATA/b/bb/a.jar",
                                                                            Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                                            "#\\TESTDATA\\b\\bb\\a.jar",
                                                                            1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/" },

                                                                            Fs.Dir( "aa", "/aa",
                                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/aa", null,
                                                                                    1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/aa/" },

                                                                                    Fs.File( "aa.txt", "/aa/aa.txt",
                                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                                             "jar:file:#/TESTDATA/b/bb/a.jar!/aa/aa.txt", null ) ),

                                                                            Fs.Dir( "ab", "/ab",
                                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/ab", null,
                                                                                    1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/" },

                                                                                    Fs.Dir( "aba", "/ab/aba",
                                                                                            "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba", null,
                                                                                            1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/" },

                                                                                            Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                                                     "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                                                    Fs.File( "ab.txt", "/ab/ab.txt",
                                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                                             "jar:file:#/TESTDATA/b/bb/a.jar!/ab/ab.txt", null ) ),

                                                                            Fs.File( "a.txt", "/a.txt",
                                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                                     "jar:file:#/TESTDATA/b/bb/a.jar!/a.txt", null ),

                                                                            Fs.Dir( "META-INF", "/META-INF",
                                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF", null,
                                                                                    1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/" },

                                                                                    Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                                             Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                                             "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ) ),

                                                   Fs.Dir( "c", "/c",
                                                           "file:#/TESTDATA/c/", "#\\TESTDATA\\c",
                                                           1, new String[] { "file:#/TESTDATA/c/" },

                                                           Fs.Root( "a.jar", "/c/a.jar", "file:#/TESTDATA/c/a.jar",
                                                                    Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                                    "#\\TESTDATA\\c\\a.jar",
                                                                    1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/" },

                                                                    Fs.Dir( "aa", "/aa",
                                                                            "jar:file:#/TESTDATA/c/a.jar!/aa", null,
                                                                            1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/aa/" },

                                                                            Fs.File( "aa.txt", "/aa/aa.txt",
                                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                                     "jar:file:#/TESTDATA/c/a.jar!/aa/aa.txt", null ) ),

                                                                    Fs.Dir( "ab", "/ab",
                                                                            "jar:file:#/TESTDATA/c/a.jar!/ab", null,
                                                                            1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/" },

                                                                            Fs.Dir( "aba", "/ab/aba",
                                                                                    "jar:file:#/TESTDATA/c/a.jar!/ab/aba", null,
                                                                                    1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/aba/" },

                                                                                    Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                                             "jar:file:#/TESTDATA/c/a.jar!/ab/aba/aba.txt", null ) ),

                                                                            Fs.File( "ab.txt", "/ab/ab.txt",
                                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                                     "jar:file:#/TESTDATA/c/a.jar!/ab/ab.txt", null ) ),

                                                                    Fs.File( "a.txt", "/a.txt",
                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                             "jar:file:#/TESTDATA/c/a.jar!/a.txt", null ),

                                                                    Fs.Dir( "META-INF", "/META-INF",
                                                                            "jar:file:#/TESTDATA/c/a.jar!/META-INF", null,
                                                                            1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/META-INF/" },

                                                                            Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                                     Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                                     "jar:file:#/TESTDATA/c/a.jar!/META-INF/MANIFEST.MF", null ) ) ),
                                                           Fs.Root( "b.jar", "/c/b.jar", "file:#/TESTDATA/c/b.jar",
                                                                    Fs.DOES_NOT_HAVE_DATA, null, 1227,
                                                                    "#\\TESTDATA\\c\\b.jar",
                                                                    1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/" },

                                                                    Fs.Dir( "bb", "/bb",
                                                                            "jar:file:#/TESTDATA/c/b.jar!/bb", null,
                                                                            1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/bb/" },

                                                                            Fs.Root( "a.jar", "/bb/a.jar", "jar:file:#/TESTDATA/c/b.jar!/bb/a.jar",
                                                                                     Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                                                     null,
                                                                                     1, new String[] { "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/" },

                                                                                     Fs.Dir( "aa", "/aa",
                                                                                             "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/aa", null,
                                                                                             1, new String[] { "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/aa/" },

                                                                                             Fs.File( "aa.txt", "/aa/aa.txt",
                                                                                                      Fs.DOES_HAVE_DATA, "", 0,
                                                                                                      "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", null ) ),

                                                                                     Fs.Dir( "ab", "/ab",
                                                                                             "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/ab", null,
                                                                                             1, new String[] { "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/ab/" },

                                                                                             Fs.Dir( "aba", "/ab/aba",
                                                                                                     "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/ab/aba", null,
                                                                                                     1, new String[] { "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/ab/aba/" },

                                                                                                     Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                                              Fs.DOES_HAVE_DATA, "", 0,
                                                                                                              "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                                                             Fs.File( "ab.txt", "/ab/ab.txt",
                                                                                                      Fs.DOES_HAVE_DATA, "", 0,
                                                                                                      "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", null ) ),

                                                                                     Fs.File( "a.txt", "/a.txt",
                                                                                              Fs.DOES_HAVE_DATA, "", 0,
                                                                                              "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/a.txt", null ),

                                                                                     Fs.Dir( "META-INF", "/META-INF",
                                                                                             "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/META-INF", null,
                                                                                             1, new String[] { "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/META-INF/" },

                                                                                             Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                                                      Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                                                      "jar:file:#/cacheDir/WEB-INF/lib/myutility.jar/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

                                                                    Fs.Dir( "META-INF", "/META-INF",
                                                                            "jar:file:#/TESTDATA/c/b.jar!/META-INF", null,
                                                                            1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/META-INF/" },

                                                                            Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                                     Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                                     "jar:file:#/TESTDATA/c/b.jar!/META-INF/MANIFEST.MF", null ) ),

                                                                    Fs.Dir( "ba", "/ba",
                                                                            "jar:file:#/TESTDATA/c/b.jar!/ba", null,
                                                                            1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/" },

                                                                            Fs.Dir( "baa", "/ba/baa",
                                                                                    "jar:file:#/TESTDATA/c/b.jar!/ba/baa", null,
                                                                                    1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/baa/" },

                                                                                    Fs.File( "baa1.txt", "/ba/baa/baa1.txt",
                                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                                             "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa1.txt", null ),

                                                                                    Fs.File( "baa2.txt", "/ba/baa/baa2.txt",
                                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                                             "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa2.txt", null ) ) ) ) ) ) ) ) ),

                 Fs.Root( "myjar.jar", "/myjar.jar", "file:#/TEST.JAR",
                          Fs.DOES_NOT_HAVE_DATA, null, 3344,
                          "#\\TEST.JAR",
                          1, new String[] { "jar:file:#/TEST.JAR!/" },

                          Fs.Dir( "a", "/a",
                                  "jar:file:#/TEST.JAR!/a", null,
                                  1, new String[] { "jar:file:#/TEST.JAR!/a/" },

                                  Fs.Dir( "aa", "/a/aa",
                                          "jar:file:#/TEST.JAR!/a/aa", null,
                                          1, new String[] { "jar:file:#/TEST.JAR!/a/aa/" },

                                          Fs.File( "aa.txt", "/a/aa/aa.txt",
                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                   "jar:file:#/TEST.JAR!/a/aa/aa.txt", null ) ),

                                  Fs.Dir( "ab", "/a/ab",
                                          "jar:file:#/TEST.JAR!/a/ab", null,
                                          1, new String[] { "jar:file:#/TEST.JAR!/a/ab/" },

                                          Fs.Dir( "aba", "/a/ab/aba",
                                                  "jar:file:#/TEST.JAR!/a/ab/aba", null,
                                                  1, new String[] { "jar:file:#/TEST.JAR!/a/ab/aba/" },

                                                  Fs.File( "aba.txt", "/a/ab/aba/aba.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/TEST.JAR!/a/ab/aba/aba.txt", null ) ),

                                          Fs.File( "ab.txt", "/a/ab/ab.txt",
                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                   "jar:file:#/TEST.JAR!/a/ab/ab.txt", null ) ),

                                  Fs.File( "a.txt", "/a/a.txt",
                                           Fs.DOES_HAVE_DATA, "", 0,
                                           "jar:file:#/TEST.JAR!/a/a.txt", null ) ),

                          Fs.Dir( "b", "/b",
                                  "jar:file:#/TEST.JAR!/b", null,
                                  1, new String[] { "jar:file:#/TEST.JAR!/b/" },

                                  Fs.Dir( "bb", "/b/bb",
                                          "jar:file:#/TEST.JAR!/b/bb", null,
                                          1, new String[] { "jar:file:#/TEST.JAR!/b/bb/" },

                                          Fs.Root( "a.jar", "/b/bb/a.jar", "jar:file:#/TEST.JAR!/b/bb/a.jar",
                                                   Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                   null,
                                                   1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/" },

                                                   Fs.Dir( "aa", "/aa",
                                                           "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/aa", null,
                                                           1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/aa/" },

                                                           Fs.File( "aa.txt", "/aa/aa.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/aa/aa.txt", null ) ),

                                                   Fs.Dir( "ab", "/ab",
                                                           "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/ab", null,
                                                           1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/ab/" },

                                                           Fs.Dir( "aba", "/ab/aba",
                                                                   "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/ab/aba", null,
                                                                   1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/ab/aba/" },

                                                                   Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                           Fs.File( "ab.txt", "/ab/ab.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/ab/ab.txt", null ) ),

                                                   Fs.File( "a.txt", "/a.txt",
                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                            "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/a.txt", null ),

                                                   Fs.Dir( "META-INF", "/META-INF",
                                                           "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/META-INF", null,
                                                           1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/META-INF/" },

                                                           Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                    Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                    "jar:file:#/cacheDir/.cache/myjar.jar/b/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

                                  Fs.Dir( "ba", "/b/ba",
                                          "jar:file:#/TEST.JAR!/b/ba", null,
                                          1, new String[] { "jar:file:#/TEST.JAR!/b/ba/" },

                                          Fs.Dir( "baa", "/b/ba/baa",
                                                  "jar:file:#/TEST.JAR!/b/ba/baa", null,
                                                  1, new String[] { "jar:file:#/TEST.JAR!/b/ba/baa/" },

                                                  Fs.File( "baa1.txt", "/b/ba/baa/baa1.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/TEST.JAR!/b/ba/baa/baa1.txt", null ),

                                                  Fs.File( "baa2.txt", "/b/ba/baa/baa2.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/TEST.JAR!/b/ba/baa/baa2.txt", null ) ) ) ),

                          Fs.Dir( "c", "/c",
                                  "jar:file:#/TEST.JAR!/c", null,
                                  1, new String[] { "jar:file:#/TEST.JAR!/c/" },

                                  Fs.Root( "a.jar", "/c/a.jar", "jar:file:#/TEST.JAR!/c/a.jar",
                                           Fs.DOES_NOT_HAVE_DATA, null, 967,
                                           null,
                                           1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/" },

                                           Fs.Dir( "aa", "/aa",
                                                   "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/aa", null,
                                                   1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/aa/" },

                                                   Fs.File( "aa.txt", "/aa/aa.txt",
                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                            "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/aa/aa.txt", null ) ),

                                           Fs.Dir( "ab", "/ab",
                                                   "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/ab", null,
                                                   1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/ab/" },

                                                   Fs.Dir( "aba", "/ab/aba",
                                                           "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/ab/aba", null,
                                                           1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/ab/aba/" },

                                                           Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/ab/aba/aba.txt", null ) ),

                                                   Fs.File( "ab.txt", "/ab/ab.txt",
                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                            "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/ab/ab.txt", null ) ),

                                           Fs.File( "a.txt", "/a.txt",
                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                    "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/a.txt", null ),

                                           Fs.Dir( "META-INF", "/META-INF",
                                                   "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/META-INF", null,
                                                   1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/META-INF/" },

                                                   Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                            Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                            "jar:file:#/cacheDir/.cache/myjar.jar/c/a.jar!/META-INF/MANIFEST.MF", null ) ) ),

                                  Fs.Root( "b.jar", "/c/b.jar", "jar:file:#/TEST.JAR!/c/b.jar",
                                           Fs.DOES_NOT_HAVE_DATA, null, 1227,
                                           null,
                                           1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/" },

                                           Fs.Dir( "bb", "/bb",
                                                   "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/bb", null,
                                                   1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/bb/" },

                                                   Fs.Root( "a.jar", "/bb/a.jar", "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/bb/a.jar",
                                                            Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                            null,
                                                            1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/" },

                                                            Fs.Dir( "aa", "/aa",
                                                                    "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/aa", null,
                                                                    1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/aa/" },

                                                                    Fs.File( "aa.txt", "/aa/aa.txt",
                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                             "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", null ) ),

                                                            Fs.Dir( "ab", "/ab",
                                                                    "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/ab", null,
                                                                    1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/ab/" },

                                                                    Fs.Dir( "aba", "/ab/aba",
                                                                            "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/ab/aba", null,
                                                                            1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/ab/aba/" },

                                                                            Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                                     "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                                    Fs.File( "ab.txt", "/ab/ab.txt",
                                                                             Fs.DOES_HAVE_DATA, "", 0,
                                                                             "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", null ) ),

                                                            Fs.File( "a.txt", "/a.txt",
                                                                     Fs.DOES_HAVE_DATA, "", 0,
                                                                     "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/a.txt", null ),

                                                            Fs.Dir( "META-INF", "/META-INF",
                                                                    "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/META-INF", null,
                                                                    1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/META-INF/" },

                                                                    Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                             Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                             "jar:file:#/cacheDir/.cache/myjar.jar/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

                                           Fs.Dir( "META-INF", "/META-INF",
                                                   "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/META-INF", null,
                                                   1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/META-INF/" },

                                                   Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                            Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                            "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/META-INF/MANIFEST.MF", null ) ),

                                           Fs.Dir( "ba", "/ba",
                                                   "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/ba", null,
                                                   1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/ba/" },

                                                   Fs.Dir( "baa", "/ba/baa",
                                                           "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/ba/baa", null,
                                                           1, new String[] { "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/ba/baa/" },

                                                           Fs.File( "baa1.txt", "/ba/baa/baa1.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/ba/baa/baa1.txt", null ),

                                                           Fs.File( "baa2.txt", "/ba/baa/baa2.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/cacheDir/.cache/myjar.jar/c/b.jar!/ba/baa/baa2.txt", null ) ) ) ) ),

                          Fs.Dir( "META-INF", "/META-INF",
                                  "jar:file:#/TEST.JAR!/META-INF", null,
                                  1, new String[] { "jar:file:#/TEST.JAR!/META-INF/" },

                                  Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                           Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                           "jar:file:#/TEST.JAR!/META-INF/MANIFEST.MF", null ) ) ),

                 Fs.Dir( "META-INF", "/META-INF",
                         "null", null,
                         1, new String[] { "file:#/TESTDATA/" },

                         Fs.Dir( "a", "/META-INF/a",
                                 "file:#/TESTDATA/a/", "#\\TESTDATA\\a",
                                 1, new String[] { "file:#/TESTDATA/a/" },

                                 Fs.File( "a.txt", "/META-INF/a/a.txt",
                                          Fs.DOES_HAVE_DATA, "", 0,
                                          "file:#/TESTDATA/a/a.txt", "#\\TESTDATA\\a\\a.txt" ),

                                 Fs.Dir( "aa", "/META-INF/a/aa",
                                         "file:#/TESTDATA/a/aa/", "#\\TESTDATA\\a\\aa",
                                         1, new String[] { "file:#/TESTDATA/a/aa/" }),

                                 Fs.Dir( "ab", "/META-INF/a/ab",
                                         "file:#/TESTDATA/a/ab/", "#\\TESTDATA\\a\\ab",
                                         1, new String[] { "file:#/TESTDATA/a/ab/" },

                                         Fs.Dir( "aba", "/META-INF/a/ab/aba",
                                                 "file:#/TESTDATA/a/ab/aba/", "#\\TESTDATA\\a\\ab\\aba",
                                                 1, new String[] { "file:#/TESTDATA/a/ab/aba/" }) ) ),

                         Fs.Dir( "b", "/META-INF/b",
                                 "file:#/TESTDATA/b/", "#\\TESTDATA\\b",
                                 1, new String[] { "file:#/TESTDATA/b/" },

                                 Fs.Dir( "ba", "/META-INF/b/ba",
                                         "file:#/TESTDATA/b/ba/", "#\\TESTDATA\\b\\ba",
                                         1, new String[] { "file:#/TESTDATA/b/ba/" },

                                         Fs.Dir( "baa", "/META-INF/b/ba/baa",
                                                 "file:#/TESTDATA/b/ba/baa/", "#\\TESTDATA\\b\\ba\\baa",
                                                 1, new String[] { "file:#/TESTDATA/b/ba/baa/" },

                                                 Fs.File( "baa1.txt", "/META-INF/b/ba/baa/baa1.txt",
                                                          Fs.DOES_HAVE_DATA, "minion", 6,
                                                          "file:#/TESTDATA/b/ba/baa/baa1.txt", "#\\TESTDATA\\b\\ba\\baa\\baa1.txt" ),

                                                 Fs.File( "baa2.txt", "/META-INF/b/ba/baa/baa2.txt",
                                                          Fs.DOES_HAVE_DATA, "chain", 5,
                                                          "file:#/TESTDATA/b/ba/baa/baa2.txt", "#\\TESTDATA\\b\\ba\\baa\\baa2.txt" ) ) ),

                                 Fs.Dir( "bb", "/META-INF/b/bb",
                                         "file:#/TESTDATA/b/bb/", "#\\TESTDATA\\b\\bb",
                                         1, new String[] { "file:#/TESTDATA/b/bb/" },

                                         Fs.Root( "a.jar", "/META-INF/b/bb/a.jar", "file:#/TESTDATA/b/bb/a.jar",
                                                  Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                  "#\\TESTDATA\\b\\bb\\a.jar",
                                                  1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/" },

                                                  Fs.Dir( "aa", "/aa",
                                                          "jar:file:#/TESTDATA/b/bb/a.jar!/aa", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/aa/" },

                                                          Fs.File( "aa.txt", "/aa/aa.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/b/bb/a.jar!/aa/aa.txt", null ) ),

                                                  Fs.Dir( "ab", "/ab",
                                                          "jar:file:#/TESTDATA/b/bb/a.jar!/ab", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/" },

                                                          Fs.Dir( "aba", "/ab/aba",
                                                                  "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba", null,
                                                                  1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/" },

                                                                  Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                          Fs.File( "ab.txt", "/ab/ab.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/b/bb/a.jar!/ab/ab.txt", null ) ),

                                                  Fs.File( "a.txt", "/a.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/a.txt", null ),

                                                  Fs.Dir( "META-INF", "/META-INF",
                                                          "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/" },

                                                          Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                   Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                   "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ) ),

                         Fs.Dir( "c", "/META-INF/c",
                                 "file:#/TESTDATA/c/", "#\\TESTDATA\\c",
                                 1, new String[] { "file:#/TESTDATA/c/" },

                                 Fs.Root( "a.jar", "/META-INF/c/a.jar", "file:#/TESTDATA/c/a.jar",
                                          Fs.DOES_NOT_HAVE_DATA, null, 967,
                                          "#\\TESTDATA\\c\\a.jar",
                                          1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/" },

                                          Fs.Dir( "aa", "/aa",
                                                  "jar:file:#/TESTDATA/c/a.jar!/aa", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/aa/" },

                                                  Fs.File( "aa.txt", "/aa/aa.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/TESTDATA/c/a.jar!/aa/aa.txt", null ) ),

                                          Fs.Dir( "ab", "/ab",
                                                  "jar:file:#/TESTDATA/c/a.jar!/ab", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/" },

                                                  Fs.Dir( "aba", "/ab/aba",
                                                          "jar:file:#/TESTDATA/c/a.jar!/ab/aba", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/aba/" },

                                                          Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/c/a.jar!/ab/aba/aba.txt", null ) ),

                                                  Fs.File( "ab.txt", "/ab/ab.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/TESTDATA/c/a.jar!/ab/ab.txt", null ) ),

                                          Fs.File( "a.txt", "/a.txt",
                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                   "jar:file:#/TESTDATA/c/a.jar!/a.txt", null ),

                                          Fs.Dir( "META-INF", "/META-INF",
                                                  "jar:file:#/TESTDATA/c/a.jar!/META-INF", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/META-INF/" },

                                                  Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                           Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                           "jar:file:#/TESTDATA/c/a.jar!/META-INF/MANIFEST.MF", null ) ) ),

                                 Fs.Root( "b.jar", "/META-INF/c/b.jar", "file:#/TESTDATA/c/b.jar",
                                          Fs.DOES_NOT_HAVE_DATA, null, 1227,
                                          "#\\TESTDATA\\c\\b.jar",
                                          1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/" },

                                          Fs.Dir( "bb", "/bb",
                                                  "jar:file:#/TESTDATA/c/b.jar!/bb", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/bb/" },

                                                  Fs.Root( "a.jar", "/bb/a.jar", "jar:file:#/TESTDATA/c/b.jar!/bb/a.jar",
                                                           Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                           null,
                                                           1, new String[] { "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/" },

                                                           Fs.Dir( "aa", "/aa",
                                                                   "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/aa", null,
                                                                   1, new String[] { "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/aa/" },

                                                                   Fs.File( "aa.txt", "/aa/aa.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", null ) ),

                                                           Fs.Dir( "ab", "/ab",
                                                                   "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/ab", null,
                                                                   1, new String[] { "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/ab/" },

                                                                   Fs.Dir( "aba", "/ab/aba",
                                                                           "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/ab/aba", null,
                                                                           1, new String[] { "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/ab/aba/" },

                                                                           Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                                    "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                                   Fs.File( "ab.txt", "/ab/ab.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", null ) ),

                                                           Fs.File( "a.txt", "/a.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/a.txt", null ),

                                                           Fs.Dir( "META-INF", "/META-INF",
                                                                   "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/META-INF", null,
                                                                   1, new String[] { "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/META-INF/" },

                                                                   Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                            Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                            "jar:file:#/cacheDir/META-INF/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

                                          Fs.Dir( "META-INF", "/META-INF",
                                                  "jar:file:#/TESTDATA/c/b.jar!/META-INF", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/META-INF/" },

                                                  Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                           Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                           "jar:file:#/TESTDATA/c/b.jar!/META-INF/MANIFEST.MF", null ) ),

                                          Fs.Dir( "ba", "/ba",
                                                  "jar:file:#/TESTDATA/c/b.jar!/ba", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/" },

                                                  Fs.Dir( "baa", "/ba/baa",
                                                          "jar:file:#/TESTDATA/c/b.jar!/ba/baa", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/baa/" },

                                                          Fs.File( "baa1.txt", "/ba/baa/baa1.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa1.txt", null ),

                                                          Fs.File( "baa2.txt", "/ba/baa/baa2.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa2.txt", null ) ) ) ) ) ),

                 Fs.Dir( "withSlash", "/withSlash",
                         "null", null,
                         1, new String[] { "file:#/TESTDATA/" },

                         Fs.Dir( "a", "/withSlash/a",
                                 "file:#/TESTDATA/a/", "#\\TESTDATA\\a",
                                 1, new String[] { "file:#/TESTDATA/a/" },

                                 Fs.File( "a.txt", "/withSlash/a/a.txt",
                                          Fs.DOES_HAVE_DATA, "", 0,
                                          "file:#/TESTDATA/a/a.txt", "#\\TESTDATA\\a\\a.txt" ),

                                 Fs.Dir( "aa", "/withSlash/a/aa",
                                         "file:#/TESTDATA/a/aa/", "#\\TESTDATA\\a\\aa",
                                         1, new String[] { "file:#/TESTDATA/a/aa/" },

                                         Fs.File( "aa.txt", "/withSlash/a/aa/aa.txt",
                                                  Fs.DOES_HAVE_DATA, "wibble", 6,
                                                  "file:#/TESTDATA/a/aa/aa.txt", "#\\TESTDATA\\a\\aa\\aa.txt" ) ),

                                 Fs.Dir( "ab", "/withSlash/a/ab",
                                         "file:#/TESTDATA/a/ab/", "#\\TESTDATA\\a\\ab",
                                         1, new String[] { "file:#/TESTDATA/a/ab/" },

                                         Fs.File( "ab.txt", "/withSlash/a/ab/ab.txt",
                                                  Fs.DOES_HAVE_DATA, "fish", 4,
                                                  "file:#/TESTDATA/a/ab/ab.txt", "#\\TESTDATA\\a\\ab\\ab.txt" ),

                                         Fs.Dir( "aba", "/withSlash/a/ab/aba",
                                                 "file:#/TESTDATA/a/ab/aba/", "#\\TESTDATA\\a\\ab\\aba",
                                                 1, new String[] { "file:#/TESTDATA/a/ab/aba/" },

                                                 Fs.File( "aba.txt", "/withSlash/a/ab/aba/aba.txt",
                                                          Fs.DOES_HAVE_DATA, "cheese", 6,
                                                          "file:#/TESTDATA/a/ab/aba/aba.txt", "#\\TESTDATA\\a\\ab\\aba\\aba.txt" ) ) ) ),

                         Fs.Dir( "b", "/withSlash/b",
                                 "file:#/TESTDATA/b/", "#\\TESTDATA\\b",
                                 1, new String[] { "file:#/TESTDATA/b/" },

                                 Fs.Dir( "ba", "/withSlash/b/ba",
                                         "file:#/TESTDATA/b/ba/", "#\\TESTDATA\\b\\ba",
                                         1, new String[] { "file:#/TESTDATA/b/ba/" },

                                         Fs.Dir( "baa", "/withSlash/b/ba/baa",
                                                 "file:#/TESTDATA/b/ba/baa/", "#\\TESTDATA\\b\\ba\\baa",
                                                 1, new String[] { "file:#/TESTDATA/b/ba/baa/" },

                                                 Fs.File( "baa1.txt", "/withSlash/b/ba/baa/baa1.txt",
                                                          Fs.DOES_HAVE_DATA, "minion", 6,
                                                          "file:#/TESTDATA/b/ba/baa/baa1.txt", "#\\TESTDATA\\b\\ba\\baa\\baa1.txt" ),

                                                 Fs.File( "baa2.txt", "/withSlash/b/ba/baa/baa2.txt",
                                                          Fs.DOES_HAVE_DATA, "chain", 5,
                                                          "file:#/TESTDATA/b/ba/baa/baa2.txt", "#\\TESTDATA\\b\\ba\\baa\\baa2.txt" ) ) ),

                                 Fs.Dir( "bb", "/withSlash/b/bb",
                                         "file:#/TESTDATA/b/bb/", "#\\TESTDATA\\b\\bb",
                                         1, new String[] { "file:#/TESTDATA/b/bb/" },

                                         Fs.Root( "a.jar", "/withSlash/b/bb/a.jar", "file:#/TESTDATA/b/bb/a.jar",
                                                  Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                  "#\\TESTDATA\\b\\bb\\a.jar",
                                                  1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/" },

                                                  Fs.Dir( "aa", "/aa",
                                                          "jar:file:#/TESTDATA/b/bb/a.jar!/aa", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/aa/" },

                                                          Fs.File( "aa.txt", "/aa/aa.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/b/bb/a.jar!/aa/aa.txt", null ) ),

                                                  Fs.Dir( "ab", "/ab",
                                                          "jar:file:#/TESTDATA/b/bb/a.jar!/ab", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/" },

                                                          Fs.Dir( "aba", "/ab/aba",
                                                                  "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba", null,
                                                                  1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/" },

                                                                  Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                          Fs.File( "ab.txt", "/ab/ab.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/b/bb/a.jar!/ab/ab.txt", null ) ),

                                                  Fs.File( "a.txt", "/a.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/a.txt", null ),

                                                  Fs.Dir( "META-INF", "/META-INF",
                                                          "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/" },

                                                          Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                   Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                   "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ) ),

                         Fs.Dir( "c", "/withSlash/c",
                                 "file:#/TESTDATA/c/", "#\\TESTDATA\\c",
                                 1, new String[] { "file:#/TESTDATA/c/" },

                                 Fs.Root( "a.jar", "/withSlash/c/a.jar", "file:#/TESTDATA/c/a.jar",
                                          Fs.DOES_NOT_HAVE_DATA, null, 967,
                                          "#\\TESTDATA\\c\\a.jar",
                                          1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/" },

                                          Fs.Dir( "aa", "/aa",
                                                  "jar:file:#/TESTDATA/c/a.jar!/aa", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/aa/" },

                                                  Fs.File( "aa.txt", "/aa/aa.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/TESTDATA/c/a.jar!/aa/aa.txt", null ) ),

                                          Fs.Dir( "ab", "/ab",
                                                  "jar:file:#/TESTDATA/c/a.jar!/ab", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/" },

                                                  Fs.Dir( "aba", "/ab/aba",
                                                          "jar:file:#/TESTDATA/c/a.jar!/ab/aba", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/aba/" },

                                                          Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/c/a.jar!/ab/aba/aba.txt", null ) ),

                                                  Fs.File( "ab.txt", "/ab/ab.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/TESTDATA/c/a.jar!/ab/ab.txt", null ) ),

                                          Fs.File( "a.txt", "/a.txt",
                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                   "jar:file:#/TESTDATA/c/a.jar!/a.txt", null ),

                                          Fs.Dir( "META-INF", "/META-INF",
                                                  "jar:file:#/TESTDATA/c/a.jar!/META-INF", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/a.jar!/META-INF/" },

                                                  Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                           Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                           "jar:file:#/TESTDATA/c/a.jar!/META-INF/MANIFEST.MF", null ) ) ),

                                 Fs.Root( "b.jar", "/withSlash/c/b.jar", "file:#/TESTDATA/c/b.jar",
                                          Fs.DOES_NOT_HAVE_DATA, null, 1227,
                                          "#\\TESTDATA\\c\\b.jar",
                                          1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/" },

                                          Fs.Dir( "bb", "/bb",
                                                  "jar:file:#/TESTDATA/c/b.jar!/bb", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/bb/" },

                                                  Fs.Root( "a.jar", "/bb/a.jar", "jar:file:#/TESTDATA/c/b.jar!/bb/a.jar",
                                                           Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                           null,
                                                           1, new String[] { "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/" },

                                                           Fs.Dir( "aa", "/aa",
                                                                   "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/aa", null,
                                                                   1, new String[] { "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/aa/" },

                                                                   Fs.File( "aa.txt", "/aa/aa.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", null ) ),

                                                           Fs.Dir( "ab", "/ab",
                                                                   "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/ab", null,
                                                                   1, new String[] { "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/ab/" },

                                                                   Fs.Dir( "aba", "/ab/aba",
                                                                           "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/ab/aba", null,
                                                                           1, new String[] { "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/ab/aba/" },

                                                                           Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                                    "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                                   Fs.File( "ab.txt", "/ab/ab.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", null ) ),

                                                           Fs.File( "a.txt", "/a.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/a.txt", null ),

                                                           Fs.Dir( "META-INF", "/META-INF",
                                                                   "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/META-INF", null,
                                                                   1, new String[] { "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/META-INF/" },

                                                                   Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                            Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                                            "jar:file:#/cacheDir/withSlash/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

                                          Fs.Dir( "META-INF", "/META-INF",
                                                  "jar:file:#/TESTDATA/c/b.jar!/META-INF", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/META-INF/" },

                                                  Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                           Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62,
                                                           "jar:file:#/TESTDATA/c/b.jar!/META-INF/MANIFEST.MF", null ) ),

                                          Fs.Dir( "ba", "/ba",
                                                  "jar:file:#/TESTDATA/c/b.jar!/ba", null,
                                                  1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/" },

                                                  Fs.Dir( "baa", "/ba/baa",
                                                          "jar:file:#/TESTDATA/c/b.jar!/ba/baa", null,
                                                          1, new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/baa/" },

                                                          Fs.File( "baa1.txt", "/ba/baa/baa1.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa1.txt", null ),

                                                          Fs.File( "baa2.txt", "/ba/baa/baa2.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                   "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa2.txt", null ) ) ) ) ) ) );
}
