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
package com.ibm.ws.artifact.fat;

/**
 *
 */
public class JarTestData {

    public static Fs TESTDATA =
                    /*
                     * ################################################################################################
                     * Fs for com.ibm.wsspi.artifact.zip.internal.ZipFileContainer
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
                            "#\\TEST.JAR"
                            , /* uri count */
                            1
                            , /* uri array */
                            new String[] { "jar:file:#/TEST.JAR!/" }
                            ,
                            Fs.Dir("a", "/a", "jar:file:#/TEST.JAR!/a"
                                   , /* physical path */null
                                   , /* uri count */1
                                   , /* uri array */new String[] { "jar:file:#/TEST.JAR!/a/" }
                                   , Fs.Dir("aa", "/a/aa", "jar:file:#/TEST.JAR!/a/aa"
                                            , /* physical path */null
                                            , /* uri count */1
                                            , /* uri array */
                                            new String[] { "jar:file:#/TEST.JAR!/a/aa/" }
                                            , Fs.File("aa.txt", "/a/aa/aa.txt", true, "", 0, /* resurl */
                                                      "jar:file:#/TEST.JAR!/a/aa/aa.txt", /* physpath */
                                                      null)
                                                   )
                                   , Fs.Dir("ab", "/a/ab", "jar:file:#/TEST.JAR!/a/ab"
                                            , /* physical path */null
                                            , /* uri count */1
                                            , /* uri array */
                                            new String[] { "jar:file:#/TEST.JAR!/a/ab/" }
                                            ,
                                            Fs.Dir("aba", "/a/ab/aba",
                                                   "jar:file:#/TEST.JAR!/a/ab/aba"
                                                   , /* physical path */null
                                                   , /* uri count */1
                                                   , /* uri array */
                                                   new String[] { "jar:file:#/TEST.JAR!/a/ab/aba/" }
                                                   ,
                                                   Fs.File("aba.txt", "/a/ab/aba/aba.txt", true, "", 0, /* resurl */
                                                           "jar:file:#/TEST.JAR!/a/ab/aba/aba.txt", /* physpath */
                                                           null)
                                                            )
                                            , Fs.File("ab.txt", "/a/ab/ab.txt", true, "", 0, /* resurl */
                                                      "jar:file:#/TEST.JAR!/a/ab/ab.txt", /* physpath */
                                                      null)
                                                   )
                                   , Fs.File("a.txt", "/a/a.txt", true, "", 0, /* resurl */
                                             "jar:file:#/TEST.JAR!/a/a.txt", /* physpath */null)
                                            )
                            ,
                            Fs.Dir(
                                   "b",
                                   "/b",
                                   "jar:file:#/TEST.JAR!/b"
                                   , /* physical path */
                                   null
                                   , /* uri count */
                                   1
                                   , /* uri array */
                                   new String[] { "jar:file:#/TEST.JAR!/b/" }
                                   ,
                                   Fs.Dir(
                                          "bb",
                                          "/b/bb",
                                          "jar:file:#/TEST.JAR!/b/bb"
                                          , /* physical path */
                                          null
                                          , /* uri count */
                                          1
                                          , /* uri array */
                                          new String[] { "jar:file:#/TEST.JAR!/b/bb/" }
                                          ,
                                          Fs.Root(
                                                  "bb",
                                                  "/b/bb",
                                                  "a.jar",
                                                  "/b/bb/a.jar",
                                                  "jar:file:#/TEST.JAR!/b/bb/a.jar",
                                                  false,
                                                  null,
                                                  967
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/" }
                                                  ,
                                                  Fs.Dir(
                                                         "aa",
                                                         "/aa",
                                                         "jar:file:#/cacheDir/.cache/b/bb/a.jar!/aa"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/aa/" }
                                                         ,
                                                         Fs.File(
                                                                 "aa.txt",
                                                                 "/aa/aa.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/cacheDir/.cache/b/bb/a.jar!/aa/aa.txt", /* physpath */
                                                                 null)
                                                                  )
                                                  ,
                                                  Fs.Dir(
                                                         "ab",
                                                         "/ab",
                                                         "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/" }
                                                         ,
                                                         Fs.Dir(
                                                                "aba",
                                                                "/ab/aba",
                                                                "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/aba"
                                                                , /* physical path */
                                                                null
                                                                , /* uri count */
                                                                1
                                                                , /* uri array */
                                                                new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/aba/" }
                                                                ,
                                                                Fs.File(
                                                                        "aba.txt",
                                                                        "/ab/aba/aba.txt",
                                                                        true,
                                                                        "",
                                                                        0, /* resurl */
                                                                        "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/aba/aba.txt", /* physpath */
                                                                        null)
                                                                         )
                                                         ,
                                                         Fs.File(
                                                                 "ab.txt",
                                                                 "/ab/ab.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/cacheDir/.cache/b/bb/a.jar!/ab/ab.txt", /* physpath */
                                                                 null)
                                                                  )
                                                  ,
                                                  Fs.File(
                                                          "a.txt",
                                                          "/a.txt",
                                                          true,
                                                          "",
                                                          0, /* resurl */
                                                          "jar:file:#/cacheDir/.cache/b/bb/a.jar!/a.txt", /* physpath */
                                                          null)
                                                  ,
                                                  Fs.Dir(
                                                         "META-INF",
                                                         "/META-INF",
                                                         "jar:file:#/cacheDir/.cache/b/bb/a.jar!/META-INF"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/cacheDir/.cache/b/bb/a.jar!/META-INF/" }
                                                         ,
                                                         Fs.File(
                                                                 "MANIFEST.MF",
                                                                 "/META-INF/MANIFEST.MF",
                                                                 true,
                                                                 "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n",
                                                                 62, /* resurl */
                                                                 "jar:file:#/cacheDir/.cache/b/bb/a.jar!/META-INF/MANIFEST.MF", /* physpath */
                                                                 null)
                                                                  )
                                                          )
                                                   )
                                   , Fs.Dir("ba", "/b/ba", "jar:file:#/TEST.JAR!/b/ba"
                                            , /* physical path */null
                                            , /* uri count */1
                                            , /* uri array */
                                            new String[] { "jar:file:#/TEST.JAR!/b/ba/" }
                                            ,
                                            Fs.Dir("baa", "/b/ba/baa",
                                                   "jar:file:#/TEST.JAR!/b/ba/baa"
                                                   , /* physical path */null
                                                   , /* uri count */1
                                                   , /* uri array */
                                                   new String[] { "jar:file:#/TEST.JAR!/b/ba/baa/" }
                                                   ,
                                                   Fs.File("baa1.txt", "/b/ba/baa/baa1.txt", true, "", 0, /* resurl */
                                                           "jar:file:#/TEST.JAR!/b/ba/baa/baa1.txt", /* physpath */
                                                           null)
                                                   ,
                                                   Fs.File("baa2.txt", "/b/ba/baa/baa2.txt", true, "", 0, /* resurl */
                                                           "jar:file:#/TEST.JAR!/b/ba/baa/baa2.txt", /* physpath */
                                                           null)
                                                            )
                                                   )
                                            )
                            ,
                            Fs.Dir(
                                   "c",
                                   "/c",
                                   "jar:file:#/TEST.JAR!/c"
                                   , /* physical path */
                                   null
                                   , /* uri count */
                                   1
                                   , /* uri array */
                                   new String[] { "jar:file:#/TEST.JAR!/c/" }
                                   ,
                                   Fs.Root(
                                           "c",
                                           "/c",
                                           "a.jar",
                                           "/c/a.jar",
                                           "jar:file:#/TEST.JAR!/c/a.jar",
                                           false,
                                           null,
                                           967
                                           , /* physical path */
                                           null
                                           , /* uri count */
                                           1
                                           , /* uri array */
                                           new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/" }
                                           ,
                                           Fs.Dir(
                                                  "aa",
                                                  "/aa",
                                                  "jar:file:#/cacheDir/.cache/c/a.jar!/aa"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/aa/" }
                                                  ,
                                                  Fs.File(
                                                          "aa.txt",
                                                          "/aa/aa.txt",
                                                          true,
                                                          "",
                                                          0, /* resurl */
                                                          "jar:file:#/cacheDir/.cache/c/a.jar!/aa/aa.txt", /* physpath */
                                                          null)
                                                           )
                                           ,
                                           Fs.Dir(
                                                  "ab",
                                                  "/ab",
                                                  "jar:file:#/cacheDir/.cache/c/a.jar!/ab"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/ab/" }
                                                  ,
                                                  Fs.Dir(
                                                         "aba",
                                                         "/ab/aba",
                                                         "jar:file:#/cacheDir/.cache/c/a.jar!/ab/aba"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/ab/aba/" }
                                                         ,
                                                         Fs.File(
                                                                 "aba.txt",
                                                                 "/ab/aba/aba.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/cacheDir/.cache/c/a.jar!/ab/aba/aba.txt", /* physpath */
                                                                 null)
                                                                  )
                                                  ,
                                                  Fs.File(
                                                          "ab.txt",
                                                          "/ab/ab.txt",
                                                          true,
                                                          "",
                                                          0, /* resurl */
                                                          "jar:file:#/cacheDir/.cache/c/a.jar!/ab/ab.txt", /* physpath */
                                                          null)
                                                           )
                                           ,
                                           Fs.File("a.txt", "/a.txt", true, "", 0, /* resurl */
                                                   "jar:file:#/cacheDir/.cache/c/a.jar!/a.txt", /* physpath */
                                                   null)
                                           ,
                                           Fs.Dir(
                                                  "META-INF",
                                                  "/META-INF",
                                                  "jar:file:#/cacheDir/.cache/c/a.jar!/META-INF"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/cacheDir/.cache/c/a.jar!/META-INF/" }
                                                  ,
                                                  Fs.File(
                                                          "MANIFEST.MF",
                                                          "/META-INF/MANIFEST.MF",
                                                          true,
                                                          "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n",
                                                          62, /* resurl */
                                                          "jar:file:#/cacheDir/.cache/c/a.jar!/META-INF/MANIFEST.MF", /* physpath */
                                                          null)
                                                           )
                                                   )
                                   ,
                                   Fs.Root(
                                           "c",
                                           "/c",
                                           "b.jar",
                                           "/c/b.jar",
                                           "jar:file:#/TEST.JAR!/c/b.jar",
                                           false,
                                           null,
                                           1227
                                           , /* physical path */
                                           null
                                           , /* uri count */
                                           1
                                           , /* uri array */
                                           new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/" }
                                           ,
                                           Fs.Dir(
                                                  "bb",
                                                  "/bb",
                                                  "jar:file:#/cacheDir/.cache/c/b.jar!/bb"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/bb/" }
                                                  ,
                                                  Fs.Root(
                                                          "bb",
                                                          "/bb",
                                                          "a.jar",
                                                          "/bb/a.jar",
                                                          "jar:file:#/cacheDir/.cache/c/b.jar!/bb/a.jar",
                                                          false,
                                                          null,
                                                          967
                                                          , /* physical path */
                                                          null
                                                          , /* uri count */
                                                          1
                                                          , /* uri array */
                                                          new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/" }
                                                          ,
                                                          Fs.Dir(
                                                                 "aa",
                                                                 "/aa",
                                                                 "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/aa"
                                                                 , /* physical path */
                                                                 null
                                                                 , /* uri count */
                                                                 1
                                                                 , /* uri array */
                                                                 new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/aa/" }
                                                                 ,
                                                                 Fs.File(
                                                                         "aa.txt",
                                                                         "/aa/aa.txt",
                                                                         true,
                                                                         "",
                                                                         0, /* resurl */
                                                                         "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/aa/aa.txt", /* physpath */
                                                                         null)
                                                                          )
                                                          ,
                                                          Fs.Dir(
                                                                 "ab",
                                                                 "/ab",
                                                                 "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab"
                                                                 , /* physical path */
                                                                 null
                                                                 , /* uri count */
                                                                 1
                                                                 , /* uri array */
                                                                 new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/" }
                                                                 ,
                                                                 Fs.Dir(
                                                                        "aba",
                                                                        "/ab/aba",
                                                                        "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/aba"
                                                                        , /* physical path */
                                                                        null
                                                                        , /* uri count */
                                                                        1
                                                                        , /* uri array */
                                                                        new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/aba/" }
                                                                        ,
                                                                        Fs.File(
                                                                                "aba.txt",
                                                                                "/ab/aba/aba.txt",
                                                                                true,
                                                                                "",
                                                                                0, /* resurl */
                                                                                "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/aba/aba.txt", /* physpath */
                                                                                null)
                                                                                 )
                                                                 ,
                                                                 Fs.File(
                                                                         "ab.txt",
                                                                         "/ab/ab.txt",
                                                                         true,
                                                                         "",
                                                                         0, /* resurl */
                                                                         "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/ab/ab.txt", /* physpath */
                                                                         null)
                                                                          )
                                                          ,
                                                          Fs.File(
                                                                  "a.txt",
                                                                  "/a.txt",
                                                                  true,
                                                                  "",
                                                                  0, /* resurl */
                                                                  "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/a.txt", /* physpath */
                                                                  null)
                                                          ,
                                                          Fs.Dir(
                                                                 "META-INF",
                                                                 "/META-INF",
                                                                 "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/META-INF"
                                                                 , /* physical path */
                                                                 null
                                                                 , /* uri count */
                                                                 1
                                                                 , /* uri array */
                                                                 new String[] { "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/META-INF/" }
                                                                 ,
                                                                 Fs.File(
                                                                         "MANIFEST.MF",
                                                                         "/META-INF/MANIFEST.MF",
                                                                         true,
                                                                         "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n",
                                                                         62, /* resurl */
                                                                         "jar:file:#/cacheDir/.cache/c/.cache/b.jar/bb/a.jar!/META-INF/MANIFEST.MF", /* physpath */
                                                                         null)
                                                                          )
                                                                  )
                                                           )
                                           ,
                                           Fs.Dir(
                                                  "META-INF",
                                                  "/META-INF",
                                                  "jar:file:#/cacheDir/.cache/c/b.jar!/META-INF"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/META-INF/" }
                                                  ,
                                                  Fs.File(
                                                          "MANIFEST.MF",
                                                          "/META-INF/MANIFEST.MF",
                                                          true,
                                                          "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n",
                                                          62, /* resurl */
                                                          "jar:file:#/cacheDir/.cache/c/b.jar!/META-INF/MANIFEST.MF", /* physpath */
                                                          null)
                                                           )
                                           ,
                                           Fs.Dir(
                                                  "ba",
                                                  "/ba",
                                                  "jar:file:#/cacheDir/.cache/c/b.jar!/ba"
                                                  , /* physical path */
                                                  null
                                                  , /* uri count */
                                                  1
                                                  , /* uri array */
                                                  new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/ba/" }
                                                  ,
                                                  Fs.Dir(
                                                         "baa",
                                                         "/ba/baa",
                                                         "jar:file:#/cacheDir/.cache/c/b.jar!/ba/baa"
                                                         , /* physical path */
                                                         null
                                                         , /* uri count */
                                                         1
                                                         , /* uri array */
                                                         new String[] { "jar:file:#/cacheDir/.cache/c/b.jar!/ba/baa/" }
                                                         ,
                                                         Fs.File(
                                                                 "baa1.txt",
                                                                 "/ba/baa/baa1.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/cacheDir/.cache/c/b.jar!/ba/baa/baa1.txt", /* physpath */
                                                                 null)
                                                         ,
                                                         Fs.File(
                                                                 "baa2.txt",
                                                                 "/ba/baa/baa2.txt",
                                                                 true,
                                                                 "",
                                                                 0, /* resurl */
                                                                 "jar:file:#/cacheDir/.cache/c/b.jar!/ba/baa/baa2.txt", /* physpath */
                                                                 null)
                                                                  )
                                                           )
                                                   )
                                            )
                            , Fs.Dir("META-INF", "/META-INF", "jar:file:#/TEST.JAR!/META-INF"
                                     , /* physical path */null
                                     , /* uri count */1
                                     , /* uri array */
                                     new String[] { "jar:file:#/TEST.JAR!/META-INF/" }
                                     , Fs.File("MANIFEST.MF", "/META-INF/MANIFEST.MF", true, "Manifest-Version: 1.0\r\nCreated-By: 1.6.0 (IBM Corporation)\r\n\r\n", 62, /* resurl */
                                               "jar:file:#/TEST.JAR!/META-INF/MANIFEST.MF", /* physpath */
                                               null)
                                            )
                                    );
}
