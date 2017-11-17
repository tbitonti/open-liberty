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
 * <p>Zip archive test file set.</p>
 */
public class JarTestData {
    public static Fs TESTDATA =
        Fs.Root( null, null, null,
                 Fs.DOES_NOT_HAVE_DATA, null, 0, 
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
                                         1, new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/" },

                                         Fs.Dir( "aa", "/aa",
                                                 "jar:file:#/cacheDir/.cache/b/bb/a.jar!/aa", null, 
                                                 1, new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/aa/" },

                                                 Fs.File( "aa.txt", "/aa/aa.txt",
                                                          Fs.DOES_HAVE_DATA, "", 0, 
                                                          "jar:file:#/cacheDir/.cache/b/bb/a.jar!/aa/aa.txt", null ) ),

                                         Fs.Dir( "ab", "/ab",
                                                 "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab", null, 
                                                 1, new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/" },

                                                 Fs.Dir( "aba", "/ab/aba",
                                                         "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/aba", null, 
                                                         1, new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/aba/" },

                                                         Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                  Fs.DOES_HAVE_DATA, "", 0, 
                                                                  "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                 Fs.File( "ab.txt", "/ab/ab.txt",
                                                          Fs.DOES_HAVE_DATA, "", 0, 
                                                          "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/ab.txt", null ) ),

                                         Fs.File( "a.txt", "/a.txt",
                                                  Fs.DOES_HAVE_DATA, "", 0, 
                                                  "jar:file:#/cacheDir/.cache/b/bb/a.jar!/a.txt", null ),

                                         Fs.Dir( "META-INF", "/META-INF",
                                                 "jar:file:#/cacheDir/.cache/b/bb/a.jar!/META-INF", null, 
                                                 1, new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/META-INF/" },

                                                 Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                          Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                                          "jar:file:#/cacheDir/.cache/b/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

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
                                 1, new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/" },

                                 Fs.Dir( "aa", "/aa",
                                         "jar:file:#/cacheDir/.cache/c/a.jar!/aa", null, 
                                         1, new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/aa/" },

                                         Fs.File( "aa.txt", "/aa/aa.txt",
                                                  Fs.DOES_HAVE_DATA, "", 0, 
                                                  "jar:file:#/cacheDir/.cache/c/a.jar!/aa/aa.txt", null ) ),

                                 Fs.Dir( "ab", "/ab",
                                         "jar:file:#/cacheDir/.cache/c/a.jar!/ab", null, 
                                         1, new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/ab/" },

                                         Fs.Dir( "aba", "/ab/aba",
                                                 "jar:file:#/cacheDir/.cache/c/a.jar!/ab/aba", null, 
                                                 1, new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/ab/aba/" },

                                                 Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                          Fs.DOES_HAVE_DATA, "", 0, 
                                                          "jar:file:#/cacheDir/.cache/c/a.jar!/ab/aba/aba.txt", null ) ),

                                         Fs.File( "ab.txt", "/ab/ab.txt",
                                                  Fs.DOES_HAVE_DATA, "", 0, 
                                                  "jar:file:#/cacheDir/.cache/c/a.jar!/ab/ab.txt", null ) ),

                                 Fs.File( "a.txt", "/a.txt",
                                          Fs.DOES_HAVE_DATA, "", 0, 
                                          "jar:file:#/cacheDir/.cache/c/a.jar!/a.txt", null ),

                                 Fs.Dir( "META-INF", "/META-INF",
                                         "jar:file:#/cacheDir/.cache/c/a.jar!/META-INF", null, 
                                         1, new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/META-INF/" },

                                         Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                  Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                                  "jar:file:#/cacheDir/.cache/c/a.jar!/META-INF/MANIFEST.MF", null ) ) ),

                        Fs.Root( "b.jar", "/c/b.jar", "jar:file:#/TEST.JAR!/c/b.jar",
                                 Fs.DOES_NOT_HAVE_DATA, null, 1227, 
                                 null, 
                                 1, new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/" },

                                 Fs.Dir( "bb", "/bb",
                                         "jar:file:#/cacheDir/.cache/c/b.jar!/bb", null, 
                                         1, new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/bb/" },

                                         Fs.Root( "a.jar", "/bb/a.jar", "jar:file:#/cacheDir/.cache/c/b.jar!/bb/a.jar",
                                                  Fs.DOES_NOT_HAVE_DATA, null, 967, 
                                                  null, 
                                                  1, new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/" },

                                                  Fs.Dir( "aa", "/aa",
                                                          "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/aa", null, 
                                                          1, new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/aa/" },

                                                          Fs.File( "aa.txt", "/aa/aa.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0, 
                                                                   "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", null ) ),

                                                  Fs.Dir( "ab", "/ab",
                                                          "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab", null, 
                                                          1, new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/" },

                                                          Fs.Dir( "aba", "/ab/aba",
                                                                  "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/aba",                                                                         null, 
                                                                  1, new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/aba/" },

                                                                  Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                           Fs.DOES_HAVE_DATA, "", 0, 
                                                                           "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                                          Fs.File( "ab.txt", "/ab/ab.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0, 
                                                                   "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", null ) ),

                                                  Fs.File( "a.txt", "/a.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0, 
                                                           "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/a.txt", null ),

                                                  Fs.Dir( "META-INF", "/META-INF",
                                                          "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/META-INF", null, 
                                                          1, new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/META-INF/" },

                                                          Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                   Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                                                   "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", null ) ) ) ),

                                 Fs.Dir( "META-INF", "/META-INF",
                                         "jar:file:#/cacheDir/.cache/c/b.jar!/META-INF", null, 
                                         1, new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/META-INF/" },

                                         Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                  Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                                  "jar:file:#/cacheDir/.cache/c/b.jar!/META-INF/MANIFEST.MF", null ) ),

                                           Fs.Dir( "ba", "/ba",
                                                   "jar:file:#/cacheDir/.cache/c/b.jar!/ba", null, 
                                                   1, new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/ba/" },

                                                   Fs.Dir( "baa", "/ba/baa",
                                                           "jar:file:#/cacheDir/.cache/c/b.jar!/ba/baa", null, 
                                                           1, new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/ba/baa/" },

                                                           Fs.File( "baa1.txt", "/ba/baa/baa1.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0, 
                                                                    "jar:file:#/cacheDir/.cache/c/b.jar!/ba/baa/baa1.txt", null ),

                                                           Fs.File( "baa2.txt", "/ba/baa/baa2.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0, 
                                                                    "jar:file:#/cacheDir/.cache/c/b.jar!/ba/baa/baa2.txt", null ) ) ) ) ),

                Fs.Dir( "META-INF", "/META-INF",
                        "jar:file:#/TEST.JAR!/META-INF", null,
                        1, new String[] { "jar:file:#/TEST.JAR!/META-INF/" },

                        Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                 Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                 "jar:file:#/TEST.JAR!/META-INF/MANIFEST.MF", null ) ) );
}
