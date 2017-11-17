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
 * <p>Bundle test file set.</p>
 */
public class BundleTestData {
    public static Fs TESTDATA =
        Fs.Root( null, null, null,
                 Fs.DOES_NOT_HAVE_DATA, null, 0,
                 null,
                 1, new String[] { "bundleentry://#/" },

                 Fs.Dir( "d", "/d",
                         "bundleentry://#/d/", null,
                         1, new String[] { "bundleentry://#/d/" },
                         Fs.File( "d.txt", "/d/d.txt",
                                  Fs.DOES_HAVE_DATA, "File in fragment", 16,
                                  "bundleentry://#/d/d.txt", null ) ),

                 Fs.Dir( "b", "/b",
                         "bundleentry://#/b/", null,
                         1, new String[] { "bundleentry://#/b/" },

                         Fs.Dir( "bb", "/b/bb",
                                 "bundleentry://#/b/bb/", null,
                                 1, new String[] { "bundleentry://#/b/bb/" },

                                 Fs.Root( "a.jar", "/b/bb/a.jar", "bundleentry://#/b/bb/a.jar",
                                          Fs.DOES_NOT_HAVE_DATA, null, 967,
                                          null,
                                          1, new String[] { "jar:file:#/b/bb/a.jar!/" },

                                          Fs.Dir( "aa", "/aa",
                                                  "jar:file:#/b/bb/a.jar!/aa", null,
                                                  1, new String[] { "jar:file:#/b/bb/a.jar!/aa/" },

                                                  Fs.File( "aa.txt", "/aa/aa.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0,
                                                           "jar:file:#/b/bb/a.jar!/aa/aa.txt", null ) ),

                                          Fs.Dir( "ab", "/ab",
                                                  "jar:file:#/b/bb/a.jar!/ab", null,
                                                  1, new String[] { "jar:file:#/b/bb/a.jar!/ab/" },

                                                  Fs.Dir( "aba", "/ab/aba",
                                                          "jar:file:#/b/bb/a.jar!/ab/aba", null,
                                                          1, new String[] { "jar:file:#/b/bb/a.jar!/ab/aba/" },

                                                          Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                   Fs.DOES_HAVE_DATA, "", 0,
                                                                    "jar:file:#/b/bb/a.jar!/ab/aba/aba.txt", null ) ),

                                              Fs.File( "ab.txt", "/ab/ab.txt",
                                                       Fs.DOES_HAVE_DATA, "", 0, 
                                                       "jar:file:#/b/bb/a.jar!/ab/ab.txt", null ) ),

                                          Fs.File( "a.txt", "/a.txt",
                                                   Fs.DOES_HAVE_DATA, "", 0, 
                                                   "jar:file:#/b/bb/a.jar!/a.txt", null ) ,

                                          Fs.Dir( "META-INF", "/META-INF",
                                                  "jar:file:#/b/bb/a.jar!/META-INF", 
                                                  null, 
                                                  1,  new String[] { "jar:file:#/b/bb/a.jar!/META-INF/" },

                                                  Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                           Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                                           "jar:file:#/b/bb/a.jar!/META-INF/MANIFEST.MF",  null ) ) ) ),

                         Fs.Dir( "ba", "/b/ba",
                                 "bundleentry://#/b/ba/", null,
                                 1, new String[] { "bundleentry://#/b/ba/" },

                                 Fs.Dir( "baa", "/b/ba/baa",
                                         "bundleentry://#/b/ba/baa/", null,
                                         1, new String[] { "bundleentry://#/b/ba/baa/" },

                                         Fs.File( "baa1.txt", "/b/ba/baa/baa1.txt",
                                                  Fs.DOES_HAVE_DATA, "minion", 6,
                                                  "bundleentry://#/b/ba/baa/baa1.txt", null ),

                                         Fs.File( "baa2.txt", "/b/ba/baa/baa2.txt",
                                                  Fs.DOES_HAVE_DATA, "chain", 5,
                                                  "bundleentry://#/b/ba/baa/baa2.txt",null ) ) ) ) ,

                 Fs.Dir( "META-INF", "/META-INF",
                         "bundleentry://#/META-INF/", null,
                         1, new String[] { "bundleentry://#/META-INF/" },

                         Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                  Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nBundle-RequiredExecutionEnvironment: Java", 192, 
                                  "bundleentry://#/META-INF/MANIFEST.MF", null ) ),

                 Fs.Dir( "c", "/c",
                         "bundleentry://#/c/", null,
                         1, new String[] { "bundleentry://#/c/" },
                         Fs.Root( "a.jar", "/c/a.jar", "bundleentry://#/c/a.jar",
                                  Fs.DOES_NOT_HAVE_DATA, null, 967,
                                  null,
                                  1, new String[] { "jar:file:#/c/a.jar!/" },

                                  Fs.Dir( "aa", "/aa",
                                          "jar:file:#/c/a.jar!/aa", null,
                                          1, new String[] { "jar:file:#/c/a.jar!/aa/" },

                                          Fs.File( "aa.txt", "/aa/aa.txt",
                                                   Fs.DOES_HAVE_DATA, "", 0, 
                                                   "jar:file:#/c/a.jar!/aa/aa.txt", null ) ),

                                  Fs.Dir( "ab", "/ab",
                                          "jar:file:#/c/a.jar!/ab", null,
                                          1, new String[] { "jar:file:#/c/a.jar!/ab/" },

                                          Fs.Dir( "aba", "/ab/aba",
                                                  "jar:file:#/c/a.jar!/ab/aba", null,
                                                  1, new String[] { "jar:file:#/c/a.jar!/ab/aba/" },
                                                  Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0, 
                                                           "jar:file:#/c/a.jar!/ab/aba/aba.txt",                                                            null ) ),

                                          Fs.File( "ab.txt", "/ab/ab.txt",
                                                   Fs.DOES_HAVE_DATA, "", 0, 
                                                   "jar:file:#/c/a.jar!/ab/ab.txt", null ) ),

                                  Fs.File( "a.txt", "/a.txt",
                                           Fs.DOES_HAVE_DATA, "", 0, 
                                           "jar:file:#/c/a.jar!/a.txt", null ),

                                  Fs.Dir( "META-INF", "/META-INF",
                                          "jar:file:#/c/a.jar!/META-INF", null,
                                          1, new String[] { "jar:file:#/c/a.jar!/META-INF/" },

                                          Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                   Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                                   "jar:file:#/c/a.jar!/META-INF/MANIFEST.MF", null ) ) ),

                         Fs.Root( "b.jar", "/c/b.jar", "bundleentry://#/c/b.jar",
                                  Fs.DOES_NOT_HAVE_DATA, null, 1227,
                                  null,
                                  1, new String[] { "jar:file:#/c/b.jar!/" },

                                  Fs.Dir( "bb", "/bb",
                                          "jar:file:#/c/b.jar!/bb", null,
                                          1, new String[] { "jar:file:#/c/b.jar!/bb/" },

                                          Fs.Root( "a.jar", "/bb/a.jar", "jar:file:#/c/b.jar!/bb/a.jar",
                                                   Fs.DOES_NOT_HAVE_DATA, null, 967,
                                                   null,
                                                   1, new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/" },

                                                   Fs.Dir( "aa", "/aa",
                                                           "jar:file:#/c/.cache/b.jar/bb/a.jar!/aa", null,
                                                           1, new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/aa/" },

                                                           Fs.File( "aa.txt", "/aa/aa.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0, 
                                                                    "jar:file:#/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", null ) ),

                                                   Fs.Dir( "ab", "/ab",
                                                           "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab", null,
                                                           1, new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/" },

                                                           Fs.Dir( "aba", "/ab/aba",
                                                                   "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/aba", null,
                                                                   1, new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/aba/" },

                                                                   Fs.File( "aba.txt", "/ab/aba/aba.txt",
                                                                            Fs.DOES_HAVE_DATA, "", 0, 
                                                                            "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", null ) ) ,

                                                           Fs.File( "ab.txt", "/ab/ab.txt",
                                                                    Fs.DOES_HAVE_DATA, "", 0, 
                                                                    "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", null ) ),

                                                   Fs.File( "a.txt", "/a.txt",
                                                            Fs.DOES_HAVE_DATA, "", 0, 
                                                            "jar:file:#/c/.cache/b.jar/bb/a.jar!/a.txt", null ),

                                                   Fs.Dir( "META-INF", "/META-INF",
                                                           "jar:file:#/c/.cache/b.jar/bb/a.jar!/META-INF", null,
                                                           1, new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/META-INF/" },

                                                           Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                                    Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                                                    "jar:file:#/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF",  null ) ) ) ),

                                  Fs.Dir( "META-INF", "/META-INF",
                                          "jar:file:#/c/b.jar!/META-INF", null,
                                          1, new String[] { "jar:file:#/c/b.jar!/META-INF/" },

                                          Fs.File( "MANIFEST.MF", "/META-INF/MANIFEST.MF",
                                                   Fs.DOES_HAVE_DATA, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, 
                                                   "jar:file:#/c/b.jar!/META-INF/MANIFEST.MF", null ) ),

                                  Fs.Dir( "ba", "/ba",
                                          "jar:file:#/c/b.jar!/ba", null,
                                          1, new String[] { "jar:file:#/c/b.jar!/ba/" },

                                          Fs.Dir( "baa", "/ba/baa",
                                                  "jar:file:#/c/b.jar!/ba/baa", null,
                                                  1, new String[] { "jar:file:#/c/b.jar!/ba/baa/" },

                                                  Fs.File( "baa1.txt", "/ba/baa/baa1.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0, 
                                                           "jar:file:#/c/b.jar!/ba/baa/baa1.txt", null ),

                                                  Fs.File( "baa2.txt", "/ba/baa/baa2.txt",
                                                           Fs.DOES_HAVE_DATA, "", 0, 
                                                           "jar:file:#/c/b.jar!/ba/baa/baa2.txt", null ) ) ) ) ),

                 Fs.Dir( "a", "/a",
                         "bundleentry://#/a/", null,
                         1, new String[] { "bundleentry://#/a/" },

                         Fs.File( "a.txt", "/a/a.txt",
                                  Fs.DOES_HAVE_DATA, "", 0,
                                  "bundleentry://#/a/a.txt", null ),

                         Fs.Dir( "aa", "/a/aa",
                                 "bundleentry://#/a/aa/", null,
                                 1, new String[] { "bundleentry://#/a/aa/" },

                                 Fs.File( "aa.txt", "/a/aa/aa.txt",
                                          Fs.DOES_HAVE_DATA, "wibble", 6,
                                          "bundleentry://#/a/aa/aa.txt", null ) ),

                         Fs.Dir( "ab", "/a/ab",
                                 "bundleentry://#/a/ab/", null,
                                 1, new String[] { "bundleentry://#/a/ab/" },

                                 Fs.Dir( "aba", "/a/ab/aba",
                                         "bundleentry://#/a/ab/aba/", null,
                                         1, new String[] { "bundleentry://#/a/ab/aba/" },

                                         Fs.File( "aba.txt", "/a/ab/aba/aba.txt",
                                                  Fs.DOES_HAVE_DATA, "cheese", 6,
                                                  "bundleentry://#/a/ab/aba/aba.txt", null ) ),

                                 Fs.File( "ab.txt", "/a/ab/ab.txt",
                                          Fs.DOES_HAVE_DATA, "fish", 4,
                                          "bundleentry://#/a/ab/ab.txt", null ) ) ) );
}
