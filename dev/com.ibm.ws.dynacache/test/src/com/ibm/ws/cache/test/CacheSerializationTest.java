/*******************************************************************************
 * Copyright (c) 1997,2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.cache.test;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.ws.cache.CacheConfig;
import com.ibm.ws.cache.CacheConfigThunk;
import com.ibm.ws.cache.test.util.FileUtil;

public class CacheSerializationTest extends BaseTest {

    public static String getCacheName() {
        return CacheSerializationTest.class.getSimpleName();
    }

    public static final String CACHE_PATH = "./build/dynacache";

    //

    public static CacheConfig getCacheConfig(String cacheName, String cachePath) {
        CacheConfig cacheConfig = new CacheConfig();
        CacheConfigThunk.setCacheName(cacheConfig, cacheName);

        cacheConfig.setEnableDiskOffload(true);
        cacheConfig.setDiskOffloadLocation(cachePath);
        cacheConfig.setMaxCacheSize(500);

        return cacheConfig;
    }

    public static CacheConfig cacheConfig;

    public static CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    protected static CacheConfig setCacheConfig(CacheConfig cacheConfig) {
        return (CacheSerializationTest.cacheConfig = cacheConfig);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        FileUtil.setupDir(CACHE_PATH);
        BaseTest.setupCache(setCacheConfig(getCacheConfig(getCacheName(), CACHE_PATH)),
                            BaseTest::initializeServerCache);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            BaseTest.tearDownCache();
        } finally {
            setCacheConfig(null);
        }
    }

    @Before
    public void setupTest() {
        super.setupTest(getCacheName());
    }

    @After
    public void teardownTest() {
        super.teardownTest(getCacheName());
    }

    //

    @Test
    public void testEnabled() throws Exception {
        assertTrue("Disk not enabled", getCacheConfig().isEnableDiskOffload());
    }

    @Test
    public void testNonSerializable() throws Exception {
        int entryCount = getCacheConfig().getCacheSize() + 100;
        Object nonSerializableIn = new Object();
        forEachEntryId(entryCount, (id) -> {
            verifySetEntry(id, UNSET_TIME_LIMIT, nonSerializableIn);
        });
    }

    @Test
    public void testSerializable() throws Exception {
        int entryCount = getCacheConfig().getCacheSize() + 100;
        String serializableIn = "serializable";
        forEachEntryId(entryCount, (id) -> {
            verifySetEntry(id, UNSET_TIME_LIMIT, serializableIn);
        });
    }
}
