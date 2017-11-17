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
 * <p>Directory test file set.</p>
 */
public class DirTestData {
    public static Fs TESTDATA =
        Fs.Root( null, null, null,
                 Fs.DOES_NOT_HAVE_DATA, null, 0,
                 "#\\TESTDATA",
                 1, new String[] { "file:#/TESTDATA/" },

                 Fs.Dir( "a", "/a",
                         "file:#/TESTDATA/a/", "#\\TESTDATA\\a",
                         1, new String[] { "file:#/TESTDATA/a/" },

                         Fs.File( "a.txt", "/a/a.txt",
                                  Fs.DOES_HAVE_DATA, "", 0,
                                  "file:#/TESTDATA/a/a.txt", "#\\TESTDATA\\a\\a.txt" ),

                         Fs.Dir( "aa", "/a/aa",
                                 "file:#/TESTDATA/a/aa/", "#\\TESTDATA\\a\\aa",
                                 1, new String[] { "file:#/TESTDATA/a/aa/" },

                                 Fs.File( "aa.txt", "/a/aa/aa.txt",
                                          Fs.DOES_HAVE_DATA, "wibble", 6,
                                          "file:#/TESTDATA/a/aa/aa.txt",  "#\\TESTDATA\\a\\aa\\aa.txt" ) ),

                         Fs.Dir("ab", "/a/ab",
                                "file:#/TESTDATA/a/ab/", "#\\TESTDATA\\a\\ab",
                                1, new String[] { "file:#/TESTDATA/a/ab/" },

                                Fs.File( "ab.txt", "/a/ab/ab.txt",
                                         Fs.DOES_HAVE_DATA, "fish", 4,
                                         "file:#/TESTDATA/a/ab/ab.txt", "#\\TESTDATA\\a\\ab\\ab.txt" ),

                                Fs.Dir( "aba", "/a/ab/aba",
                                        "file:#/TESTDATA/a/ab/aba/", "#\\TESTDATA\\a\\ab\\aba",
                                        1, new String[] { "file:#/TESTDATA/a/ab/aba/" },

                                        Fs.File( "aba.txt", "/a/ab/aba/aba.txt",
                                                 Fs.DOES_HAVE_DATA, "cheese", 6,
                                                 "file:#/TESTDATA/a/ab/aba/aba.txt", "#\\TESTDATA\\a\\ab\\aba\\aba.txt" ) ) ) ),

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
                                  Fs.DOES_NOT_HAVE_DATA, null, 967, "#\\TESTDATA\\c\\a.jar",
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
                                                   1, new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/" },

                                                   Fs.Dir( "aa", "/aa",
                                                           "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/aa", null,
                                                           1, new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/aa/" },

                                                           Fs.File( "aa.txt", "/aa/aa.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0, 
                                                                    "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/aa/aa.txt", null ) ),

                                                   Fs.Dir( "ab", "/ab",
                                                           "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab", null,
                                                           1, new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/" },

                                                           Fs.Dir( "aba", "/ab/aba",
                                                                   "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/aba", null,
                                                                   1, new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/aba/" },

                                                                   Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                                            "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                           Fs.File( "ab.txt", "/ab/ab.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0, 
                                                                    "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/ab.txt", null ) ),

                                                   Fs.File( "a.txt", "/a.txt",
                                                            Fs.DOES_HAVE_DATA, "", 0,
                                                            "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/a.txt", null ),

                                                   Fs.Dir( "META-INF", "/META-INF",
                                                           "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/META-INF", null,
                                                           1, new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/META-INF/" },

                                                           Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                    Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                                                    "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

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
                                                           "jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa2.txt", null ) ) ) ) ) );
}
