/*******************************************************************************
 * Copyright (c) 1997,2023 IBM Corporation and others.
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//@formatter:off
public class CacheTest extends BaseTest {

    public static String getCacheName() {
        return CacheTest.class.getSimpleName();
    }

    @BeforeClass
    public static void setUp() throws Exception {
        BaseTest.setupCache( BaseTest.createCacheConfig(getCacheName()),
                             BaseTest::initializeServerCache );
    }

    @AfterClass
    public static void tearDown() throws Exception {
        BaseTest.tearDownCache();
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
    public void testSetGet() throws Exception {
        int timeLimit = 600;
        populateCache(timeLimit);
    }

    @Test
    public void testUpdate() throws Exception {
        int initialLimit = 50000;
        int finalLimit = 60000;

        populateCache(initialLimit);

        forEachEntryId( (String id) -> {
            String data0 = id;
            String data1 = "updated" + id;
            // Change the data ...
            verifyUpdateEntry(id, initialLimit, data0, initialLimit, data1);
            // ... then change the time limit.
            verifyUpdateEntry(id, initialLimit, data1, finalLimit, data1);
        } );
    }

    @Test
    public void testTimeout() throws Exception {
        int timeLimit = 1;
        populateCache(timeLimit);
        Thread.sleep(4000);
    }

    @Test
    public void testInvalidateNoWait() throws Exception {
        populateCache(UNSET_TIME_LIMIT);

        forEachEntryId( (String id) -> {
            invalidateEntry(id, !DO_WAIT);
        } );

        // Force a flush of the batch update daemon.
        getCache().invalidateById("junk id", DO_WAIT);

        forEachEntryId( (String id) -> {
            verifyAbsentEntry(id);
        } );
    }

    @Test
    public void testInvalidateWait() throws Exception {
        populateCache(UNSET_TIME_LIMIT);

        forEachEntryId( (String id) -> {
            invalidateEntry(id, DO_WAIT);
            verifyAbsentEntry(id);
        } );
    }
}
//@formatter:on