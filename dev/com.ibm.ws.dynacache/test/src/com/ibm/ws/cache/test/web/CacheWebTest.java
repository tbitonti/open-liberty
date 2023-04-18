/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.cache.test.web;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

// import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.cache.test.BaseTest;
import com.ibm.ws.cache.test.CacheTest;

public class CacheWebTest extends CacheTest {

    public static String getCacheName() {
        return CacheWebTest.class.getSimpleName();
    }

    public static void initializeServerCache() {
        BaseTest.initializeServerCache();

        // ServerCache.servletCacheEnabled = true;
        // ServerCache.cacheUnit.setServletCacheUnit(new ServletCacheUnitImpl());
    }

    @BeforeClass
    public static void setUp() throws Exception {
        BaseTest.setupCache(BaseTest.createCacheConfig(getCacheName()),
                            () -> CacheWebTest.initializeServerCache());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        BaseTest.tearDownCache();
    }

    @Override
    @Before
    public void setupTest() {
        super.setupTest(getCacheName());
    }

    @Override
    @After
    public void teardownTest() {
        super.teardownTest(getCacheName());
    }
}
