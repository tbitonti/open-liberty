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
public class BundleTestData {
    public static Fs TESTDATA =
                    /*
                     * ################################################################################################
                     * Fs for com.ibm.wsspi.artifact.bundle.internal.BundleArchive
                     */
                    Fs.Root(
                            "/",
                            "/",
                            null,
                            null,
                            null,
                            false,
                            null,
                            0
                            , /* physical path */
                            null
                            , /* uri count */
                            1
                            , /* uri array */
                            new String[] { "bundleentry://#/" }
                            ,
                            Fs.Dir("d", "/d", "bundleentry://#/d/"
                                   , /* physical path */null
                                   , /* uri count */1
                                   , /* uri array */new String[] { "bundleentry://#/d/" }
                                   , Fs.File("d.txt", "/d/d.txt", true, "File in fragment", 16, /* resurl */"bundleentry://#/d/d.txt", /* physpath */null)
                                            )
                            ,
                            Fs.Dir(
                                   "b",
                                   "/b",
                                   "bundleentry://#/b/"
                                   , /* physical path */
                                   null
                                   , /* uri count */
                                   1
                                   , /* uri array */
                                   new String[] { "bundleentry://#/b/" }
                                   ,
                                   Fs.Dir(
                                          "bb",
                                          "/b/bb",
                                          "bundleentry://#/b/bb/"
                                          , /* physical path */
                                          null
                                          , /* uri count */
                                          1
                                          , /* uri array */
                                          new String[] { "bundleentry://#/b/bb/" }
                                          ,
                                          Fs.Root(
                                                  "bb",
                                                  "/b/bb",
                                                  "a.jar",
                                                  "/b/bb/a.jar",
                                                  "bundleentry://#/b/bb/a.jar",
                                                  false,
                                                  null,
                                                  967
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/b/bb/a.jar!/" }
                                                  ,
                                                  Fs.Dir(
                                                         "aa",
                                                         "/aa",
                                                         "jar:file:#/b/bb/a.jar!/aa"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/b/bb/a.jar!/aa/" }
                                                         ,
                                                         Fs.File(
                                                                 "aa.txt",
                                                                 "/aa/aa.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/b/bb/a.jar!/aa/aa.txt", /* physpath */
                                                                 null)
                                                                  )
                                                  ,
                                                  Fs.Dir(
                                                         "ab",
                                                         "/ab",
                                                         "jar:file:#/b/bb/a.jar!/ab"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/b/bb/a.jar!/ab/" }
                                                         ,
                                                         Fs.Dir(
                                                                "aba",
                                                                "/ab/aba",
                                                                "jar:file:#/b/bb/a.jar!/ab/aba"
                                                                , /* physical path */
                                                                null
                                                                , /* uri count */
                                                                1
                                                                , /* uri array */
                                                                new String[] { "jar:file:#/b/bb/a.jar!/ab/aba/" }
                                                                ,
                                                                Fs.File(
                                                                        "aba.txt",
                                                                        "/ab/aba/aba.txt",
                                                                        true,
                                                                        "",
                                                                        0, /* resurl */
                                                                        "jar:file:#/b/bb/a.jar!/ab/aba/aba.txt", /* physpath */
                                                                        null)
                                                                         )
                                                         ,
                                                         Fs.File(
                                                                 "ab.txt",
                                                                 "/ab/ab.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/b/bb/a.jar!/ab/ab.txt", /* physpath */
                                                                 null)
                                                                  )
                                                  ,
                                                  Fs.File(
                                                          "a.txt",
                                                          "/a.txt",
                                                          true,
                                                          "",
                                                          0, /* resurl */
                                                          "jar:file:#/b/bb/a.jar!/a.txt", /* physpath */
                                                          null)
                                                  ,
                                                  Fs.Dir(
                                                         "META-INF",
                                                         "/META-INF",
                                                         "jar:file:#/b/bb/a.jar!/META-INF"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/b/bb/a.jar!/META-INF/" }
                                                         ,
                                                         Fs.File(
                                                                 "MANIFEST.MF",
                                                                 "/META-INF/MANIFEST.MF",
                                                                 true,
                                                                 "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n",
                                                                 62, /* resurl */
                                                                 "jar:file:#/b/bb/a.jar!/META-INF/MANIFEST.MF", /* physpath */
                                                                 null)
                                                                  )
                                                          )
                                                   )
                                   , Fs.Dir("ba", "/b/ba", "bundleentry://#/b/ba/"
                                            , /* physical path */null
                                            , /* uri count */1
                                            , /* uri array */new String[] { "bundleentry://#/b/ba/" }
                                            , Fs.Dir("baa", "/b/ba/baa", "bundleentry://#/b/ba/baa/"
                                                     , /* physical path */null
                                                     , /* uri count */1
                                                     , /* uri array */new String[] { "bundleentry://#/b/ba/baa/" }
                                                     , Fs.File("baa1.txt", "/b/ba/baa/baa1.txt", true, "minion", 6, /* resurl */"bundleentry://#/b/ba/baa/baa1.txt", /* physpath */
                                                               null)
                                                     , Fs.File("baa2.txt", "/b/ba/baa/baa2.txt", true, "chain", 5, /* resurl */"bundleentry://#/b/ba/baa/baa2.txt", /* physpath */
                                                               null)
                                                            )
                                                   )
                                            )
                            ,
                            Fs.Dir("META-INF", "/META-INF", "bundleentry://#/META-INF/"
                                   , /* physical path */null
                                   , /* uri count */1
                                   , /* uri array */new String[] { "bundleentry://#/META-INF/" }
                                   , Fs.File("MANIFEST.MF", "/META-INF/MANIFEST.MF", true, "Manifest-Version: 1.0\r\nBundle-RequiredExecutionEnvironment: Java", 192, /* resurl */
                                             "bundleentry://#/META-INF/MANIFEST.MF", /* physpath */null)
                                            )
                            ,
                            Fs.Dir(
                                   "c",
                                   "/c",
                                   "bundleentry://#/c/"
                                   , /* physical path */
                                   null
                                   , /* uri count */
                                   1
                                   , /* uri array */
                                   new String[] { "bundleentry://#/c/" }
                                   ,
                                   Fs.Root(
                                           "c",
                                           "/c",
                                           "a.jar",
                                           "/c/a.jar",
                                           "bundleentry://#/c/a.jar",
                                           false,
                                           null,
                                           967
                                           , /* physical path */
                                           null
                                           , /* uri count */
                                           1
                                           , /* uri array */
                                           new String[] { "jar:file:#/c/a.jar!/" }
                                           ,
                                           Fs.Dir(
                                                  "aa",
                                                  "/aa",
                                                  "jar:file:#/c/a.jar!/aa"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/c/a.jar!/aa/" }
                                                  ,
                                                  Fs.File(
                                                          "aa.txt",
                                                          "/aa/aa.txt",
                                                          true,
                                                          "",
                                                          0, /* resurl */
                                                          "jar:file:#/c/a.jar!/aa/aa.txt", /* physpath */
                                                          null)
                                                           )
                                           ,
                                           Fs.Dir(
                                                  "ab",
                                                  "/ab",
                                                  "jar:file:#/c/a.jar!/ab"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/c/a.jar!/ab/" }
                                                  ,
                                                  Fs.Dir(
                                                         "aba",
                                                         "/ab/aba",
                                                         "jar:file:#/c/a.jar!/ab/aba"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/c/a.jar!/ab/aba/" }
                                                         ,
                                                         Fs.File(
                                                                 "aba.txt",
                                                                 "/ab/aba/aba.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/c/a.jar!/ab/aba/aba.txt", /* physpath */
                                                                 null)
                                                                  )
                                                  ,
                                                  Fs.File(
                                                          "ab.txt",
                                                          "/ab/ab.txt",
                                                          true,
                                                          "",
                                                          0, /* resurl */
                                                          "jar:file:#/c/a.jar!/ab/ab.txt", /* physpath */
                                                          null)
                                                           )
                                           ,
                                           Fs.File(
                                                   "a.txt",
                                                   "/a.txt",
                                                   true,
                                                   "",
                                                   0, /* resurl */
                                                   "jar:file:#/c/a.jar!/a.txt", /* physpath */
                                                   null)
                                           ,
                                           Fs.Dir(
                                                  "META-INF",
                                                  "/META-INF",
                                                  "jar:file:#/c/a.jar!/META-INF"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/c/a.jar!/META-INF/" }
                                                  ,
                                                  Fs.File(
                                                          "MANIFEST.MF",
                                                          "/META-INF/MANIFEST.MF",
                                                          true,
                                                          "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n",
                                                          62, /* resurl */
                                                          "jar:file:#/c/a.jar!/META-INF/MANIFEST.MF", /* physpath */
                                                          null)
                                                           )
                                                   )
                                   ,
                                   Fs.Root(
                                           "c",
                                           "/c",
                                           "b.jar",
                                           "/c/b.jar",
                                           "bundleentry://#/c/b.jar",
                                           false,
                                           null,
                                           1227
                                           , /* physical path */
                                           null
                                           , /* uri count */
                                           1
                                           , /* uri array */
                                           new String[] { "jar:file:#/c/b.jar!/" }
                                           ,
                                           Fs.Dir(
                                                  "bb",
                                                  "/bb",
                                                  "jar:file:#/c/b.jar!/bb"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/c/b.jar!/bb/" }
                                                  ,
                                                  Fs.Root(
                                                          "bb",
                                                          "/bb",
                                                          "a.jar",
                                                          "/bb/a.jar",
                                                          "jar:file:#/c/b.jar!/bb/a.jar",
                                                          false,
                                                          null,
                                                          967
                                                          , /* physical path */
                                                          null
                                                          , /* uri count */
                                                          1
                                                          , /* uri array */
                                                          new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/" }
                                                          ,
                                                          Fs.Dir(
                                                                 "aa",
                                                                 "/aa",
                                                                 "jar:file:#/c/.cache/b.jar/bb/a.jar!/aa"
                                                                 , /* physical path */
                                                                 null
                                                                 , /* uri count */
                                                                 1
                                                                 , /* uri array */
                                                                 new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/aa/" }
                                                                 ,
                                                                 Fs.File(
                                                                         "aa.txt",
                                                                         "/aa/aa.txt",
                                                                         true,
                                                                         "",
                                                                         0, /* resurl */
                                                                         "jar:file:#/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", /* physpath */
                                                                         null)
                                                                          )
                                                          ,
                                                          Fs.Dir(
                                                                 "ab",
                                                                 "/ab",
                                                                 "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab"
                                                                 , /* physical path */
                                                                 null
                                                                 , /* uri count */
                                                                 1
                                                                 , /* uri array */
                                                                 new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/" }
                                                                 ,
                                                                 Fs.Dir(
                                                                        "aba",
                                                                        "/ab/aba",
                                                                        "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/aba"
                                                                        , /* physical path */
                                                                        null
                                                                        , /* uri count */
                                                                        1
                                                                        , /* uri array */
                                                                        new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/aba/" }
                                                                        ,
                                                                        Fs.File(
                                                                                "aba.txt",
                                                                                "/ab/aba/aba.txt",
                                                                                true,
                                                                                "",
                                                                                0, /* resurl */
                                                                                "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", /* physpath */
                                                                                null)
                                                                                 )
                                                                 ,
                                                                 Fs.File(
                                                                         "ab.txt",
                                                                         "/ab/ab.txt",
                                                                         true,
                                                                         "",
                                                                         0, /* resurl */
                                                                         "jar:file:#/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", /* physpath */
                                                                         null)
                                                                          )
                                                          ,
                                                          Fs.File(
                                                                  "a.txt",
                                                                  "/a.txt",
                                                                  true,
                                                                  "",
                                                                  0, /* resurl */
                                                                  "jar:file:#/c/.cache/b.jar/bb/a.jar!/a.txt", /* physpath */
                                                                  null)
                                                          ,
                                                          Fs.Dir(
                                                                 "META-INF",
                                                                 "/META-INF",
                                                                 "jar:file:#/c/.cache/b.jar/bb/a.jar!/META-INF"
                                                                 , /* physical path */
                                                                 null
                                                                 , /* uri count */
                                                                 1
                                                                 , /* uri array */
                                                                 new String[] { "jar:file:#/c/.cache/b.jar/bb/a.jar!/META-INF/" }
                                                                 ,
                                                                 Fs.File(
                                                                         "MANIFEST.MF",
                                                                         "/META-INF/MANIFEST.MF",
                                                                         true,
                                                                         "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n",
                                                                         62, /* resurl */
                                                                         "jar:file:#/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", /* physpath */
                                                                         null)
                                                                          )
                                                                  )
                                                           )
                                           ,
                                           Fs.Dir(
                                                  "META-INF",
                                                  "/META-INF",
                                                  "jar:file:#/c/b.jar!/META-INF"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/c/b.jar!/META-INF/" }
                                                  ,
                                                  Fs.File(
                                                          "MANIFEST.MF",
                                                          "/META-INF/MANIFEST.MF",
                                                          true,
                                                          "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n",
                                                          62, /* resurl */
                                                          "jar:file:#/c/b.jar!/META-INF/MANIFEST.MF", /* physpath */
                                                          null)
                                                           )
                                           ,
                                           Fs.Dir(
                                                  "ba",
                                                  "/ba",
                                                  "jar:file:#/c/b.jar!/ba"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/c/b.jar!/ba/" }
                                                  ,
                                                  Fs.Dir(
                                                         "baa",
                                                         "/ba/baa",
                                                         "jar:file:#/c/b.jar!/ba/baa"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/c/b.jar!/ba/baa/" }
                                                         ,
                                                         Fs.File(
                                                                 "baa1.txt",
                                                                 "/ba/baa/baa1.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/c/b.jar!/ba/baa/baa1.txt", /* physpath */
                                                                 null)
                                                         ,
                                                         Fs.File(
                                                                 "baa2.txt",
                                                                 "/ba/baa/baa2.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/c/b.jar!/ba/baa/baa2.txt", /* physpath */
                                                                 null)
                                                                  )
                                                           )
                                                   )
                                            )
                            , Fs.Dir("a", "/a", "bundleentry://#/a/"
                                     , /* physical path */null
                                     , /* uri count */1
                                     , /* uri array */new String[] { "bundleentry://#/a/" }
                                     , Fs.File("a.txt", "/a/a.txt", true, "", 0, /* resurl */"bundleentry://#/a/a.txt", /* physpath */null)
                                     , Fs.Dir("aa", "/a/aa", "bundleentry://#/a/aa/"
                                              , /* physical path */null
                                              , /* uri count */1
                                              , /* uri array */new String[] { "bundleentry://#/a/aa/" }
                                              , Fs.File("aa.txt", "/a/aa/aa.txt", true, "wibble", 6, /* resurl */"bundleentry://#/a/aa/aa.txt", /* physpath */null)
                                                     )
                                     , Fs.Dir("ab", "/a/ab", "bundleentry://#/a/ab/"
                                              , /* physical path */null
                                              , /* uri count */1
                                              , /* uri array */new String[] { "bundleentry://#/a/ab/" }
                                              , Fs.Dir("aba", "/a/ab/aba", "bundleentry://#/a/ab/aba/"
                                                       , /* physical path */null
                                                       , /* uri count */1
                                                       , /* uri array */new String[] { "bundleentry://#/a/ab/aba/" }
                                                       , Fs.File("aba.txt", "/a/ab/aba/aba.txt", true, "cheese", 6, /* resurl */"bundleentry://#/a/ab/aba/aba.txt", /* physpath */
                                                                 null)
                                                              )
                                              , Fs.File("ab.txt", "/a/ab/ab.txt", true, "fish", 4, /* resurl */"bundleentry://#/a/ab/ab.txt", /* physpath */null)
                                                     )
                                            )
                                    );
}
