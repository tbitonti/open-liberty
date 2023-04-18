/*******************************************************************************
 * Copyright (c) 1997, 2023 IBM Corporation and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.websphere.cache.CacheEntry;
import com.ibm.websphere.cache.ChangeEvent;
import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.DistributedNioMap;
import com.ibm.websphere.cache.DistributedObjectCache;
import com.ibm.websphere.cache.InvalidationEvent;
import com.ibm.ws.cache.CacheConfig;
import com.ibm.ws.cache.CacheServiceImplThunk;
import com.ibm.ws.cache.DistributedMapImpl;
import com.ibm.ws.cache.DistributedNioMapImpl;
import com.ibm.ws.cache.EntryInfo;
import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.cache.test.util.CacheEventData;
import com.ibm.ws.cache.test.util.CacheListener;

//@formatter:off
public class MapNioTest extends BaseTest {

    // Invalidation causes, defined by InvalidationEvent:
    public static final int CAUSE_EXPLICIT = InvalidationEvent.EXPLICIT;
    public static final int CAUSE_LRU = InvalidationEvent.LRU;
    public static final int CAUSE_TIMEOUT = InvalidationEvent.TIMEOUT;
    public static final int CAUSE_DISK_TIMEOUT = InvalidationEvent.DISK_TIMEOUT;
    public static final int CAUSE_CLEAR_ALL = InvalidationEvent.CLEAR_ALL;
    public static final int CAUSE_INACTIVE = InvalidationEvent.INACTIVE;
    public static final int CAUSE_DISK_GARBAGE_COLLECTOR =InvalidationEvent. DISK_GARBAGE_COLLECTOR;
    public static final int CAUSE_DISK_OVERFLOW = InvalidationEvent.DISK_OVERFLOW;

    // Invalidation *AND* change sources, defined by InvalidationEvent and by ChangeEvent:
    public static final int SOURCE_LOCAL = InvalidationEvent.LOCAL;
    public static final int SOURCE_REMOTE = InvalidationEvent.REMOTE;

    // Change event causes, defined by ChangeEvent:
    public static final int CHANGE_VALUE = ChangeEvent.EXISTING_VALUE_CHANGED;
    public static final int CHANGE_ADD = ChangeEvent.NEW_ENTRY_ADDED;
    public static final int CHANGE_TIME = ChangeEvent.EXPIRATION_TIMES_CHANGED;

    // public static final int SOURCE_LOCAL = ChangeEvent.LOCAL; // Same as InvalidationEvent.LOCAL.
    // public static final int SOURCE_REMOTE = ChangeEvent.REMOTE; // Same InvalidationEvent.REMOTE.

    protected void verifyEvents(CacheListener listener, Map<Object, Object> expectedData) {
        int expectedCause = listener.getEventType();

        List<? extends CacheEventData> actualEvents = listener.getEvents();

        Map<Object, Object> actualData = new HashMap<>(expectedData.size());

        for ( CacheEventData eventData : actualEvents ) {
            assertEquals("Unexpected event type", expectedCause, eventData.getCause());
            actualData.put(eventData.getId(), eventData.getValue());
        }

        expectedData.forEach((key, expectedValue) -> {
            Object actualValue = actualData.get(key);
            assertEquals("Missing event value", expectedValue, actualValue);
        });

        expectedData.forEach((key, actualValue) -> {
            Object expectedValue = expectedData.get(key);
            assertEquals("Extra event value", expectedValue, actualValue);
        });
    }

    //

    private static final String CACHE_NAME = MapNioTest.class.getSimpleName();

    public static String getCacheName() {
        return CACHE_NAME;
    }

    // Must be the same as the cache name; see DistributedObjectCacheFactory:411.
    public static String getMapName() {
        return CACHE_NAME;
    }

    public static DistributedNioMap getMap() {
        return getNioMap(getMapName());
    }

    //

    public static CacheConfig createCacheConfig(String cacheName) {
        CacheConfig cacheConfig = BaseTest.createCacheConfig(cacheName);

        cacheConfig.setMaxCacheSize(10);
        cacheConfig.setEnableNioSupport(true);

        return cacheConfig;
    }

    @BeforeClass
    public static void setup() throws Exception {
        BaseTest.setupCache(MapNioTest.createCacheConfig(getCacheName()),
                               BaseTest::initializeServerCache);
    }

    @AfterClass
    public static void teardown() {
        CacheServiceImplThunk.stop(ServerCache.getCacheService());
    }

    @Before
    public void setupTest() throws Exception {
        super.setupTest( getCacheName() );
        getMap().clear();
    }

    @After
    public void teardownTest() throws Exception {
        try {
            getMap().clear();
        } finally {
            super.teardownTest( getCacheName() );
        }
    }

    //

    protected void withMapEntry(DistributedNioMap map, String id, Consumer<CacheEntry> action) {
        CacheEntry ce = null;
        try {
            ce = map.getCacheEntry(id);
            action.accept(ce);
        } finally {
            if (ce != null) {
                ce.finish();
            }
        }
    }

    protected void invoke(DistributedNioMap map, Object key) throws Exception {
        map.hashCode();
        map.equals(map);

        map.getCacheEntry(key);

        map.invalidate(key); // wait
        map.invalidate(key, true); // wait
        map.invalidate(key, false); // don't wait

        map.size(false);
        map.size(true);

        map.clear();
    }

    //

    @Test
    public void testType() {
        assertTrue("Distributed NIO map not a distributed object cache",
                   DistributedObjectCache.class.isAssignableFrom(DistributedNioMapImpl.class));
    }

    @Test
    public void testNullAlias() throws Exception {
        DistributedNioMapImpl dnm = new DistributedNioMapImpl(getCache());

        boolean failed;
        try {
            dnm.addAlias(null, null);
            failed = false;
        } catch (RuntimeException e) {
            failed = true;
        }
        assertFalse("Added null alias", failed);
    }

    @Test
    public void testMap() throws Exception {
        Object key = new Object();
        Object value = new Object();
        Object userMetaData = new Object();
        int priority = 1;
        int timeToLive = -1;
        int sharingPolicy = EntryInfo.NOT_SHARED;

        Object deps[] = new Object[] { new Object(), new Object(), new Object() };
        Object aliases[] = new Object[] { new Object(), new Object(), new Object() };
        Object alias1 = aliases[1];

        //

        DistributedNioMap map = getMap();
        DistributedNioMap map1 = getMap();
        assertSame("Different NIO maps", map, map1);

        //

        Thread.sleep(2000);
        invoke(map, key);

        //

        map.put(key, value, userMetaData, priority, timeToLive, sharingPolicy, deps, aliases);
        map.addAlias(key, aliases);
        map.removeAlias(alias1);
        map.releaseLruEntries(10);
        Thread.sleep(2000);
        invoke(map, key);

        //

        map.put(key, value, userMetaData, priority, timeToLive, sharingPolicy, deps, aliases);
        map.putAndGet(key, value, userMetaData, priority, timeToLive, sharingPolicy, deps, aliases);
        map.addAlias(key, aliases);
        map.releaseLruEntries(10);
        map.removeAlias(alias1);
        Thread.sleep(2000);
        invoke(map, key);
    }

    @Test
    public void testDistribution() throws Exception {
        DistributedMap sourceMap = new DistributedMapImpl(getCache());
        DistributedNioMap sinkMap = new DistributedNioMapImpl(getCache());

        String elementKey = "key";
        String elementValue = "value";

        String mapKey = "mapKey";
        Hashtable<String, String> mapValue = new Hashtable<>();

        mapValue.put(elementKey, elementValue);

        sourceMap.put(mapKey, mapValue);

        Object sourceGet = sourceMap.get(mapKey);
        assertSame("Incorrect source get", mapValue, sourceGet);

        CacheEntry sinkEntry = sinkMap.getCacheEntry(mapKey);
        assertNotNull("Null sink entry", sinkEntry);

        Object sinkGet = sinkEntry.getValue();
        assertSame("Incorrect sink get", mapValue, sinkGet);
    }

    @Test
    public void testInvalidation() throws Exception {
        DistributedNioMap map = getMap();

        CacheListener invalidationListener = new CacheListener("invalidation");
        CacheListener changeListener = new CacheListener("change");

        try {
            map.addInvalidationListener(invalidationListener);
            map.addInvalidationListener(changeListener);

            runInvalidationTest(map, invalidationListener, changeListener);

        } finally {
            map.removeInvalidationListener(invalidationListener);
            map.removeInvalidationListener(changeListener);
        }
    }

    protected void runInvalidationTest(DistributedNioMap map,
                                       CacheListener invalidationListener,
                                       CacheListener changeListener) throws Exception {

        // Verify initial population of the distributed map.

        Map<Object, Object> populateMap = new HashMap<>(10);
        for (int idNo = 0; idNo < 10; idNo++) {
            populateMap.put( getId(idNo), getData(idNo) );
        }

        Map<Object, Object> emptyMap = Collections.emptyMap();

        invalidationListener.setEventType(CAUSE_EXPLICIT);
        changeListener.setEventType(CHANGE_ADD);

        changeListener.start();
        invalidationListener.start();
        map.enableListener(true);

        for (int idNo = 0; idNo < 10; idNo++) {
            String id = getId(idNo);
            String initialData = getData(idNo);
            String[] deps = getDeps(idNo);

            map.put(id, initialData, null, 3, 0, EntryInfo.NOT_SHARED, deps, null);

            withMapEntry(map, id, (CacheEntry ce) -> {
                assertNotNull("Null cache entry", ce);
                String finalData = (String) ce.getValue();
                assertEquals("Changed cache data", initialData, finalData);
            });
        }

        invalidationListener.stop();
        changeListener.stop();

        map.enableListener(false);

        verifyEvents(changeListener, populateMap);
        verifyEvents(invalidationListener, emptyMap);

        changeListener.clearEvents();
        invalidationListener.clearEvents();

        // Verify dependent invalidation.

        Map<Object, Object> invalidationMap1 = new HashMap<>(3);
        invalidationMap1.put(getId(1), getData(1));
        invalidationMap1.put(getId(2), getData(2));
        invalidationMap1.put(getId(3), getData(3));

        changeListener.start();
        invalidationListener.start();
        map.enableListener(true);

        map.invalidate(getId(1)); // invalidates 1 and, by dependency, 2 and 3

        for (int idNo = 1; idNo < 4; idNo++) {
            String id = getId(idNo);
            withMapEntry(map, id, (CacheEntry ce) -> {
                assertNull("Not null after invalidation: " + id, ce);
            });
        }

        map.enableListener(false);
        changeListener.stop();
        invalidationListener.stop();

        verifyEvents(changeListener, emptyMap);
        verifyEvents(invalidationListener, invalidationMap1);

        //

        Map<Object, Object> invalidationMap2 = new HashMap<>(3);
        invalidationMap2.put(getId(4), getData(4));
        invalidationMap2.put(getId(5), getData(5));
        invalidationMap2.put(getId(6), getData(6));
        invalidationMap2.put(getId(7), getData(7));

        map.invalidate(getId(4));
        map.invalidate(getId(5));
        map.invalidate(getId(6));
        map.invalidate(getId(7));

        for (int idNo = 4; idNo < 8; idNo++) {
            withMapEntry(map, getId(idNo), (CacheEntry ce) -> {
                assertNull("Entry not null after invalidation", ce);
            });
        }

        verifyEvents(invalidationListener, invalidationMap2);
        verifyEvents(changeListener, emptyMap);

        invalidationListener.setEventType(CAUSE_LRU);
        invalidationListener.start();
        changeListener.start();

        Map<Object, Object> invalidationMap3 = new HashMap<>(10);
        for ( int idNo = 0; idNo < 10; idNo++ ) {
            invalidationMap3.put(getId(idNo), getData(idNo));
        }

        String[] deps4 = new String[] { getId(4) };
        for (int idNo = 11; idNo < 20; idNo++) {
            map.put(getId(idNo), getData(idNo), null, 3, 0, EntryInfo.NOT_SHARED, deps4, null);
        }

        verifyEvents(invalidationListener, invalidationMap3);
        verifyEvents(changeListener, emptyMap);

        Map<Object, Object> invalidationMap4 = new HashMap<>(1);
        invalidationMap4.put("*", null);

        invalidationListener.setEventType(CAUSE_CLEAR_ALL);
        invalidationListener.start();
        changeListener.start();

        verifyEvents(invalidationListener, invalidationMap4);
    }
}
//@formatter:on
