/*******************************************************************************
 * Copyright (c) 2017,2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.cache.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Consumer;

import org.junit.Rule;

import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.DistributedNioMap;
import com.ibm.ws.cache.CacheConfig;
import com.ibm.ws.cache.CacheConfigThunk;
import com.ibm.ws.cache.CacheEntry;
import com.ibm.ws.cache.CacheEntryThunk;
import com.ibm.ws.cache.CacheServiceImpl;
import com.ibm.ws.cache.CacheServiceImplThunk;
import com.ibm.ws.cache.DistributedMapImpl;
import com.ibm.ws.cache.DistributedNioMapImpl;
import com.ibm.ws.cache.EntryInfo;
import com.ibm.ws.cache.ObjectCacheUnitImpl;
import com.ibm.ws.cache.Scheduler;
import com.ibm.ws.cache.SchedulerThunk;
import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.cache.intf.DCache;
import com.ibm.ws.cache.spi.DistributedMapFactory;
import com.ibm.wsspi.cache.DistributedObjectCacheFactory;

import test.common.SharedOutputManager;

//@formatter:off
public class BaseTest {
    public static long getTime() {
        return System.currentTimeMillis();
    }

    //

    public static final String TRACE_STRING =
        "*=audit=enabled";
    public static final String TRACE_STRING_FULL =
        "*=audit=enabled:WebSphere Dynamic Cache=all=enabled";

    public String getTraceString() {
        return TRACE_STRING;
    }

    @Rule
    public SharedOutputManager outputMgr =
        SharedOutputManager.getInstance().trace(getTraceString());

    //

    public static CacheConfig createCacheConfig(String cacheName) {
        CacheConfig cacheConfig = new CacheConfig();
        CacheConfigThunk.setCacheName(cacheConfig, cacheName);
        return cacheConfig;
    }

    public static void tearDownCache() throws Exception {
        CacheServiceImpl cacheService = ((CacheServiceImpl) ServerCache.getCacheService());
        CacheServiceImplThunk.stop(cacheService);
    }

    public static void setupCache(CacheConfig cacheConfig, Runnable cacheInitializer) throws Exception {
        initializeScheduler();

        cacheInitializer.run();

        createBaseCache( cacheConfig.getCacheName() );
    }

    public static DCache getCache(String cacheName) {
        return ServerCache.getCache(cacheName);
    }

    //

    private DCache cache;

    private DCache setCache(String cacheName) {
        if ( cache == null ) {
            cache = getCache(cacheName);
        }
        return cache;
    }

    protected DCache getCache() {
        return cache;
    }

    public void setupTest(String cacheName) {
        setCache(cacheName).clear();
    }

    public void teardownTest(String cacheName) {
        getCache().clear();
    }

    //

    protected static void initializeScheduler() {
        Scheduler scheduler = new Scheduler();
        ScheduledThreadPoolExecutor ste = new ScheduledThreadPoolExecutor(5);
        SchedulerThunk.setExecutorService(scheduler, ste);
        SchedulerThunk.setScheduledExecutorService(scheduler, ste);
        scheduler.activate();
    }

    public static void initializeServerCache() {
        ServerCache.coreCacheEnabled = true;

        ServerCache.objectCacheEnabled = true;
        ServerCache.cacheUnit.setObjectCacheUnit(new ObjectCacheUnitImpl());
    }

    protected static void createBaseCache(String cacheName) throws Exception {
        CacheServiceImpl useCacheService = CacheServiceImplThunk.start();
        CacheConfig useCacheConfig = useCacheService.getCacheConfig();

        useCacheService.addCacheInstanceConfig(useCacheConfig, true);
        useCacheService.setCacheName(cacheName);

        ServerCache.createCache(cacheName, useCacheConfig);
    }

    //

    public CacheEntry getEntry(String id) {
        return (CacheEntry) getCache().getEntry(id);
    }

    public void setEntry(CacheEntry entry) {
        getCache().setEntry(entry);
    }

    public static boolean DO_WAIT = true;

    public void invalidateEntry(String id, boolean wait) {
        getCache().invalidateById(id, wait);
    }

    //

    public CacheEntry createEntry(String entryId, Object dataId) {
        return createEntry(UNSET_TIME_LIMIT, entryId, dataId);
    }

    public CacheEntry createEntry(String entryId) {
        return createEntry(UNSET_TIME_LIMIT, entryId, DATA_ID);
    }

    public CacheEntry createEntry(int timeLimit, String entryId) {
        return createEntry(timeLimit, entryId, DATA_ID);
    }

    public static final int UNSET_TIME_LIMIT = -1;
    public static final String DATA_ID = "my data id";

    public CacheEntry createEntry(int timeLimit, String entryId, Object dataId) {
        EntryInfo ei = new EntryInfo();
        ei.setId(entryId);
        if ( timeLimit != UNSET_TIME_LIMIT ) {
            ei.setTimeLimit(timeLimit);
        }
        ei.addDataId(dataId);

        CacheEntry entry = new CacheEntry();
        CacheEntryThunk.setValue(entry, entryId);
        entry.copyMetaData(ei);

        return entry;
    }

    public void verifyEntry(CacheEntry entry, int timeLimit, String id, Object data) {
        assertNotNull("null entry", entry);

        assertEquals("incorrect time limit", timeLimit, entry.getTimeLimit());
        assertEquals("incorrect value", id, entry.getValue());
    }

    public void verifyAbsentEntry(String id) {
        CacheEntry getEntry = getEntry(id);
        assertNull("non-null entry", getEntry);
    }

    public CacheEntry verifyPresentEntry(String id) {
        CacheEntry getEntry = getEntry(id);
        assertNotNull("null entry", getEntry);
        return getEntry;
    }

    protected void verifySetEntry(String id, int timeLimit, Object data) {
        verifyAbsentEntry(id);

        CacheEntry setEntry = createEntry(timeLimit, id, data);
        setEntry(setEntry);
        CacheEntry getEntry = getEntry(id);
        verifyEntry(getEntry, timeLimit, id, data);
    }

    protected void verifyUpdateEntry(String id,
                                     int initialLimit, String initialData,
                                     int finalLimit, String finalData) {

        CacheEntry initialEntry = getEntry(id);
        verifyEntry(initialEntry, initialLimit, id, initialData);

        CacheEntry setEntry = createEntry(finalLimit, id, finalData);

        long startTime = getTime();
        setEntry(setEntry);
        CacheEntry getEntry = getEntry(id);
        long endTime = getTime();
        long elapsed = endTime - startTime;

        if ( ((initialLimit == UNSET_TIME_LIMIT) || (elapsed < initialLimit)) &&
             ((finalLimit == UNSET_TIME_LIMIT) || (elapsed < finalLimit)) ) {
            verifyEntry(getEntry, finalLimit, id, finalData);
        }
    }

    //

    public static final int NUM_IDS = 100;
    public static final String[] IDS;
    public static final String[] DATA;

    static {
        String[] useIds = new String[NUM_IDS];
        String[] useData = new String[NUM_IDS];
        for (int idNo = 0; idNo < NUM_IDS; idNo++) {
            useIds[idNo] = "test:" + idNo;
            useData[idNo] = "this is a test value:" + idNo;
        }
        IDS = useIds;
        DATA = useData;
    }

    public static String getId(int idOffset) {
        return IDS[idOffset];
    }

    public static String getData(int idOffset) {
        return DATA[idOffset];
    }

    public static void forEachEntryId(Consumer<String> action) {
        for ( String id : IDS ) {
            action.accept(id);
        }
    }

    public static void forEachEntryId(int numIds, Consumer<String> action) {
        for ( int idNo = 0; idNo < numIds; idNo++ ) {
            String id = ((idNo < IDS.length) ? IDS[idNo] : ("test:" + idNo));
            action.accept(id);
        }
    }

    private static final String[][] DEPS;

    static {
        String[] d12 = new String[] { getId(1), getId(2) }; // 1, 3
        String[] d1 = new String[] { getId(1) };            // 2
        String[] d2 = new String[] { getId(2) };            // 4, 5, 6
        String[] d3 = new String[] { getId(3) };            // 8, 9
        String[] d4 = new String[] { getId(4) };            // 0, 7

        String[][] useDeps = new String[][] {
            d4,         // 0
            d12,        // 1
            d1,         // 2
            d12,        // 3
            d2, d2, d2, // 4, 5, 6
            d4,         // 7
            d3, d3,     // 8, 9
        };

        // 3 <- 1, 2   (simple dependency)
        // 2 <- 1 <- 2 (loop)
        // 1 <- 1      (self loop)

        // 4 <- 2      (simple dependency)
        // 5 <- 2      (simple dependency)
        // 6 <- 2      (simple dependency)
        // 7 <- 4      (simple dependency)

        DEPS = useDeps;
    }

    public static String[] getDeps(int idNo) {
        return DEPS[idNo];
    }

    public void populateCache(int timeLimit) {
        forEachEntryId( (String id) -> {
            verifyAbsentEntry(id);

            CacheEntry setEntry = createEntry(timeLimit, id, id);
            setEntry(setEntry);

            CacheEntry getEntry = getEntry(id);
            verifyEntry(getEntry, timeLimit, id, id);
        } );
    }

    //

    /**
     * Directly create and return a distributed map.  Do not name
     * and register the map.
     *
     * @return A new distributed map.
     */
    public DistributedMap createMap() {
        return new DistributedMapImpl(getCache());
    }

    /**
     * Answer the distributed map which is associated with the
     * specified name.  Answer null if distributed maps are
     * not enabled in the cache.  Create a new map if one is not
     * already registered with the specified name.  Use default
     * properties for a new map.
     *
     * @param name The name associated with the distributed map.
     *
     * @return The distributed map which is associated with the
     *     specified name.
     */
    @SuppressWarnings("deprecation")
    public static DistributedMap getMap(String name) {
        return DistributedMapFactory.getMap(name);
    }

    /**
     * Answer the distributed map which is associated with the
     * specified name.  Answer null if distributed maps are
     * not enabled in the cache.  Create a new map if one is not
     * already registered with the specified name.  Use default
     * properties for a new map.
     *
     * @param name The name associated with the distributed map.
     * @param props Properties for the new map.
     *
     * @return The distributed map which is associated with the
     *     specified name.
     */
    @SuppressWarnings("deprecation")
    public static DistributedMap getMap(String name, Properties props) {
        return DistributedMapFactory.getMap(name, props);
    }

    /**
     * Directly create and return a distributed NIO map.  Do not name
     * and register the map.
     *
     * @return A new distributed NIO map.
     */
    public DistributedNioMap createNioMap() {
        return new DistributedNioMapImpl(getCache());
    }

    /**
     * Answer the distributed NIO map which is associated with the
     * specified name.  Answer null if distributed maps are
     * not enabled in the cache.  Create a new map if one is not
     * already registered with the specified name.  Use default
     * properties for a new map.
     *
     * @param name The name associated with the distributed NIO map.
     *
     * @return The distributed NIO map which is associated with the
     *     specified name.
     */
    public static DistributedNioMap getNioMap(String name) {
        return DistributedObjectCacheFactory.getMap(name);
    }
}
//@formatter:on
