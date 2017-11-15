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
package com.ibm.ws.artifact.fat_bvt;

/**
 *
 */
public class DirTestData {
    public static Fs TESTDATA =
                    /*
                     * ################################################################################################
                     * Fs for com.ibm.wsspi.artifact.file.internal.FileContainer
                     */
                    Fs.Root("/", "/", null, null, null, false, null, 0
                            , /* physical path */"#\\TESTDATA"
                            , /* uri count */1
                            , /* uri array */new String[] { "file:#/TESTDATA/" }
                            , Fs.Dir("a", "/a", "file:#/TESTDATA/a/"
                                     , /* physical path */"#\\TESTDATA\\a"
                                     , /* uri count */1
                                     , /* uri array */new String[] { "file:#/TESTDATA/a/" }
                                     , Fs.File("a.txt", "/a/a.txt", true, "", 0, /* resurl */"file:#/TESTDATA/a/a.txt", /* physpath */"#\\TESTDATA\\a\\a.txt")
                                     , Fs.Dir("aa", "/a/aa", "file:#/TESTDATA/a/aa/"
                                              , /* physical path */"#\\TESTDATA\\a\\aa"
                                              , /* uri count */1
                                              , /* uri array */new String[] { "file:#/TESTDATA/a/aa/" }
                                              , Fs.File("aa.txt", "/a/aa/aa.txt", true, "wibble", 6, /* resurl */"file:#/TESTDATA/a/aa/aa.txt", /* physpath */
                                                        "#\\TESTDATA\\a\\aa\\aa.txt")
                                                     )
                                     , Fs.Dir("ab", "/a/ab", "file:#/TESTDATA/a/ab/"
                                              , /* physical path */"#\\TESTDATA\\a\\ab"
                                              , /* uri count */1
                                              , /* uri array */new String[] { "file:#/TESTDATA/a/ab/" }
                                              , Fs.File("ab.txt", "/a/ab/ab.txt", true, "fish", 4, /* resurl */"file:#/TESTDATA/a/ab/ab.txt", /* physpath */
                                                        "#\\TESTDATA\\a\\ab\\ab.txt")
                                              , Fs.Dir("aba", "/a/ab/aba", "file:#/TESTDATA/a/ab/aba/"
                                                       , /* physical path */"#\\TESTDATA\\a\\ab\\aba"
                                                       , /* uri count */1
                                                       , /* uri array */new String[] { "file:#/TESTDATA/a/ab/aba/" }
                                                       , Fs.File("aba.txt", "/a/ab/aba/aba.txt", true, "cheese", 6, /* resurl */"file:#/TESTDATA/a/ab/aba/aba.txt", /* physpath */
                                                                 "#\\TESTDATA\\a\\ab\\aba\\aba.txt")
                                                              )
                                                     )
                                            )
                            , Fs.Dir("b", "/b", "file:#/TESTDATA/b/"
                                     , /* physical path */"#\\TESTDATA\\b"
                                     , /* uri count */1
                                     , /* uri array */new String[] { "file:#/TESTDATA/b/" }
                                     , Fs.Dir("ba", "/b/ba", "file:#/TESTDATA/b/ba/"
                                              , /* physical path */"#\\TESTDATA\\b\\ba"
                                              , /* uri count */1
                                              , /* uri array */new String[] { "file:#/TESTDATA/b/ba/" }
                                              , Fs.Dir("baa", "/b/ba/baa", "file:#/TESTDATA/b/ba/baa/"
                                                       , /* physical path */"#\\TESTDATA\\b\\ba\\baa"
                                                       , /* uri count */1
                                                       , /* uri array */new String[] { "file:#/TESTDATA/b/ba/baa/" }
                                                       , Fs.File("baa1.txt", "/b/ba/baa/baa1.txt", true, "minion", 6, /* resurl */"file:#/TESTDATA/b/ba/baa/baa1.txt", /* physpath */
                                                                 "#\\TESTDATA\\b\\ba\\baa\\baa1.txt")
                                                       , Fs.File("baa2.txt", "/b/ba/baa/baa2.txt", true, "chain", 5, /* resurl */"file:#/TESTDATA/b/ba/baa/baa2.txt", /* physpath */
                                                                 "#\\TESTDATA\\b\\ba\\baa\\baa2.txt")
                                                              )
                                                     )
                                     , Fs.Dir("bb", "/b/bb", "file:#/TESTDATA/b/bb/"
                                              , /* physical path */"#\\TESTDATA\\b\\bb"
                                              , /* uri count */1
                                              , /* uri array */new String[] { "file:#/TESTDATA/b/bb/" }
                                              , Fs.Root("bb", "/b/bb", "a.jar", "/b/bb/a.jar", "file:#/TESTDATA/b/bb/a.jar", false, null, 967
                                                        , /* physical path */"#\\TESTDATA\\b\\bb\\a.jar"
                                                        , /* uri count */1
                                                        , /* uri array */new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/" }
                                                        , Fs.Dir("aa", "/aa", "jar:file:#/TESTDATA/b/bb/a.jar!/aa"
                                                                 , /* physical path */null
                                                                 , /* uri count */1
                                                                 , /* uri array */new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/aa/" }
                                                                 , Fs.File("aa.txt", "/aa/aa.txt", true, "", 0, /* resurl */"jar:file:#/TESTDATA/b/bb/a.jar!/aa/aa.txt", /* physpath */
                                                                           null)
                                                                        )
                                                        , Fs.Dir("ab", "/ab", "jar:file:#/TESTDATA/b/bb/a.jar!/ab"
                                                                 , /* physical path */null
                                                                 , /* uri count */1
                                                                 , /* uri array */new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/" }
                                                                 , Fs.Dir("aba", "/ab/aba", "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba"
                                                                          , /* physical path */null
                                                                          , /* uri count */1
                                                                          , /* uri array */new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/" }
                                                                          , Fs.File("aba.txt", "/ab/aba/aba.txt", true, "", 0, /* resurl */
                                                                                    "jar:file:#/TESTDATA/b/bb/a.jar!/ab/aba/aba.txt", /* physpath */null)
                                                                                 )
                                                                 , Fs.File("ab.txt", "/ab/ab.txt", true, "", 0, /* resurl */"jar:file:#/TESTDATA/b/bb/a.jar!/ab/ab.txt", /* physpath */
                                                                           null)
                                                                        )
                                                        , Fs.File("a.txt", "/a.txt", true, "", 0, /* resurl */"jar:file:#/TESTDATA/b/bb/a.jar!/a.txt", /* physpath */null)
                                                        , Fs.Dir("META-INF", "/META-INF", "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF"
                                                                 , /* physical path */null
                                                                 , /* uri count */1
                                                                 , /* uri array */new String[] { "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/" }
                                                                 , Fs.File("MANIFEST.MF", "/META-INF/MANIFEST.MF", true,
                                                                           "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, /* resurl */
                                                                           "jar:file:#/TESTDATA/b/bb/a.jar!/META-INF/MANIFEST.MF", /* physpath */null)
                                                                        )
                                                              )
                                                     )
                                            )
                            , Fs.Dir("c", "/c", "file:#/TESTDATA/c/"
                                     , /* physical path */"#\\TESTDATA\\c"
                                     , /* uri count */1
                                     , /* uri array */new String[] { "file:#/TESTDATA/c/" }
                                     , Fs.Root("c", "/c", "a.jar", "/c/a.jar", "file:#/TESTDATA/c/a.jar", false, null, 967
                                               , /* physical path */"#\\TESTDATA\\c\\a.jar"
                                               , /* uri count */1
                                               , /* uri array */new String[] { "jar:file:#/TESTDATA/c/a.jar!/" }
                                               , Fs.Dir("aa", "/aa", "jar:file:#/TESTDATA/c/a.jar!/aa"
                                                        , /* physical path */null
                                                        , /* uri count */1
                                                        , /* uri array */new String[] { "jar:file:#/TESTDATA/c/a.jar!/aa/" }
                                                        , Fs.File("aa.txt", "/aa/aa.txt", true, "", 0, /* resurl */"jar:file:#/TESTDATA/c/a.jar!/aa/aa.txt", /* physpath */null)
                                                               )
                                               , Fs.Dir("ab", "/ab", "jar:file:#/TESTDATA/c/a.jar!/ab"
                                                        , /* physical path */null
                                                        , /* uri count */1
                                                        , /* uri array */new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/" }
                                                        , Fs.Dir("aba", "/ab/aba", "jar:file:#/TESTDATA/c/a.jar!/ab/aba"
                                                                 , /* physical path */null
                                                                 , /* uri count */1
                                                                 , /* uri array */new String[] { "jar:file:#/TESTDATA/c/a.jar!/ab/aba/" }
                                                                 , Fs.File("aba.txt", "/ab/aba/aba.txt", true, "", 0, /* resurl */"jar:file:#/TESTDATA/c/a.jar!/ab/aba/aba.txt", /* physpath */
                                                                           null)
                                                                        )
                                                        , Fs.File("ab.txt", "/ab/ab.txt", true, "", 0, /* resurl */"jar:file:#/TESTDATA/c/a.jar!/ab/ab.txt", /* physpath */null)
                                                               )
                                               , Fs.File("a.txt", "/a.txt", true, "", 0, /* resurl */"jar:file:#/TESTDATA/c/a.jar!/a.txt", /* physpath */null)
                                               , Fs.Dir("META-INF", "/META-INF", "jar:file:#/TESTDATA/c/a.jar!/META-INF"
                                                        , /* physical path */null
                                                        , /* uri count */1
                                                        , /* uri array */new String[] { "jar:file:#/TESTDATA/c/a.jar!/META-INF/" }
                                                        , Fs.File("MANIFEST.MF", "/META-INF/MANIFEST.MF", true,
                                                                  "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, /* resurl */
                                                                  "jar:file:#/TESTDATA/c/a.jar!/META-INF/MANIFEST.MF", /* physpath */null)
                                                               )
                                                     )
                                     , Fs.Root("c", "/c", "b.jar", "/c/b.jar", "file:#/TESTDATA/c/b.jar", false, null, 1227
                                               , /* physical path */"#\\TESTDATA\\c\\b.jar"
                                               , /* uri count */1
                                               , /* uri array */new String[] { "jar:file:#/TESTDATA/c/b.jar!/" }
                                               , Fs.Dir("bb", "/bb", "jar:file:#/TESTDATA/c/b.jar!/bb"
                                                        , /* physical path */null
                                                        , /* uri count */1
                                                        , /* uri array */new String[] { "jar:file:#/TESTDATA/c/b.jar!/bb/" }
                                                        , Fs.Root("bb", "/bb", "a.jar", "/bb/a.jar", "jar:file:#/TESTDATA/c/b.jar!/bb/a.jar", false, null, 967
                                                                  , /* physical path */null
                                                                  , /* uri count */1
                                                                  , /* uri array */new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/" }
                                                                  , Fs.Dir("aa", "/aa", "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/aa"
                                                                           , /* physical path */null
                                                                           , /* uri count */1
                                                                           , /* uri array */new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/aa/" }
                                                                           , Fs.File("aa.txt", "/aa/aa.txt", true, "", 0, /* resurl */
                                                                                     "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/aa/aa.txt", /* physpath */null)
                                                                                  )
                                                                  , Fs.Dir("ab", "/ab", "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab"
                                                                           , /* physical path */null
                                                                           , /* uri count */1
                                                                           , /* uri array */new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/" }
                                                                           , Fs.Dir("aba", "/ab/aba", "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/aba"
                                                                                    , /* physical path */null
                                                                                    , /* uri count */1
                                                                                    , /* uri array */new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/aba/" }
                                                                                    , Fs.File("aba.txt", "/ab/aba/aba.txt", true, "", 0, /* resurl */
                                                                                              "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", /* physpath */null)
                                                                                           )
                                                                           , Fs.File("ab.txt", "/ab/ab.txt", true, "", 0, /* resurl */
                                                                                     "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/ab/ab.txt", /* physpath */null)
                                                                                  )
                                                                  , Fs.File("a.txt", "/a.txt", true, "", 0, /* resurl */"jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/a.txt", /* physpath */
                                                                            null)
                                                                  , Fs.Dir("META-INF", "/META-INF", "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/META-INF"
                                                                           , /* physical path */null
                                                                           , /* uri count */1
                                                                           , /* uri array */new String[] { "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/META-INF/" }
                                                                           , Fs.File("MANIFEST.MF", "/META-INF/MANIFEST.MF", true,
                                                                                     "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, /* resurl */
                                                                                     "jar:file:#/cacheDir/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", /* physpath */null)
                                                                                  )
                                                                        )
                                                               )
                                               , Fs.Dir("META-INF", "/META-INF", "jar:file:#/TESTDATA/c/b.jar!/META-INF"
                                                        , /* physical path */null
                                                        , /* uri count */1
                                                        , /* uri array */new String[] { "jar:file:#/TESTDATA/c/b.jar!/META-INF/" }
                                                        , Fs.File("MANIFEST.MF", "/META-INF/MANIFEST.MF", true,
                                                                  "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, /* resurl */
                                                                  "jar:file:#/TESTDATA/c/b.jar!/META-INF/MANIFEST.MF", /* physpath */null)
                                                               )
                                               , Fs.Dir("ba", "/ba", "jar:file:#/TESTDATA/c/b.jar!/ba"
                                                        , /* physical path */null
                                                        , /* uri count */1
                                                        , /* uri array */new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/" }
                                                        , Fs.Dir("baa", "/ba/baa", "jar:file:#/TESTDATA/c/b.jar!/ba/baa"
                                                                 , /* physical path */null
                                                                 , /* uri count */1
                                                                 , /* uri array */new String[] { "jar:file:#/TESTDATA/c/b.jar!/ba/baa/" }
                                                                 , Fs.File("baa1.txt", "/ba/baa/baa1.txt", true, "", 0, /* resurl */"jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa1.txt", /* physpath */
                                                                           null)
                                                                 , Fs.File("baa2.txt", "/ba/baa/baa2.txt", true, "", 0, /* resurl */"jar:file:#/TESTDATA/c/b.jar!/ba/baa/baa2.txt", /* physpath */
                                                                           null)
                                                                        )
                                                               )
                                                     )
                                            )
                                    );
}
