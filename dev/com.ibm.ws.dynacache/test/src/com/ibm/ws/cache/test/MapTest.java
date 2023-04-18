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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.websphere.cache.CacheEntry;
import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.DistributedNioMap;
import com.ibm.websphere.cache.DistributedObjectCache;
import com.ibm.ws.cache.DistributedMapImpl;
import com.ibm.ws.cache.DistributedNioMapImpl;
import com.ibm.ws.cache.EntryInfo;
import com.ibm.wsspi.cache.DistributedObjectCacheFactory;

//@formatter:off
public class MapTest extends BaseTest {

    private static final String CACHE_NAME = MapTest.class.getSimpleName();

    public static String getCacheName() {
        return CACHE_NAME;
    }

    // Must be the same as the cache name; see DistributedObjectCacheFactory:411.
    public static String getMapName() {
        return CACHE_NAME;
    }

    public static DistributedMap getMap() {
        return getMap(getMapName());
    }

    //

    @BeforeClass
    public static void setup() throws Exception {
        BaseTest.setupCache(BaseTest.createCacheConfig(getCacheName()),
                            BaseTest::initializeServerCache);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        BaseTest.tearDownCache();
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

    private static final int MAP_ID_COUNT = 10000;

    private static final String[] MAP_IDS;
    private static final String[] MAP_DATA;

    static {
        String[] mapIds = new String[MAP_ID_COUNT];
        String[] mapData = new String[MAP_ID_COUNT];

        for (int idNo = 0; idNo < MAP_ID_COUNT; idNo++) {
            mapIds[idNo] = "test:" + idNo;
            mapData[idNo] = "test value:" + idNo;
        }

        MAP_IDS = mapIds;
        MAP_DATA = mapData;
    }

    public static String getMapId(int idNo) {
        return MAP_IDS[idNo];
    }

    public static String getMapData(int idNo) {
        return MAP_DATA[idNo];
    }

    public static void apply(int range, BiConsumer<String, String> action) {
        for (int idNo = 0; idNo < range; idNo++) {
            action.accept(getMapId(idNo), getMapData(idNo));
        }
    }

    @SuppressWarnings("unchecked")
    public static DistributedMap populateMap(int range) {
        DistributedMap storage = getMap();
        apply(range, (id, data) -> {
            verifyUpdate(storage, id, null, data);
        });
        return storage;
    }

    public static void populate(Map<Object, Object> storage, int range) {
        apply(range, (id, data) -> {
            verifyUpdate(storage, id, null, data);
        });
    }

    public static void verifyUpdate(Map<Object, Object> storage,
                                    Object key,
                                    Object initialValue, Object finalValue) {

        verifyGet(storage, key, initialValue);
        storage.put(key, finalValue);
        verifyGet(storage, key, finalValue);
    }

    public static DistributedMap populateMap(int range, int timeout, String[] deps) {
        DistributedMap storage = getMap();
        apply(range, (id, data) -> {
            verifyUpdate(storage, id, timeout, deps, null, data);
        });
        return storage;
    }

    public static void verifyUpdate(DistributedMap storage,
                                    Object key, int timeout, String[] deps,
                                    Object initialValue, Object finalValue) {

        verifyGet(storage, key, initialValue);
        storage.put(key, finalValue, 3, timeout, EntryInfo.NOT_SHARED, deps);
        verifyGet(storage, key, finalValue);
    }

    public static void verifyGet(Map<?, ?> storage, Object key, Object expectedValue) {
        assertEquals("Incorrect entry data", expectedValue, storage.get(key));
    }

    //

    @Test
    public void testType() throws Exception {
        assertTrue("Distributed map not a distributed object cache",
                   DistributedObjectCache.class.isAssignableFrom(DistributedMapImpl.class));
    }

    //

    @Test
    public void testDRSBootstrap() {
        DistributedMap map = getMap();

        map.setDRSBootstrap(true);
        assertTrue("setDRSBootStrap", map.isDRSBootstrapEnabled());

        map.setDRSBootstrap(false);
        assertFalse("setDRSBootStrap", map.isDRSBootstrapEnabled());
    }

    //

    @Test
    public void testDistribution() throws Exception {
        DistributedMap sourceMap = new DistributedMapImpl(getCache());
        DistributedNioMap sinkMap = new DistributedNioMapImpl(getCache());

        String mapKey = "mapKey";
        Map<String, String> mapValue = new HashMap<>();

        String elementKey = "key";
        String elementValue = "value";
        mapValue.put(elementKey, elementValue);

        sourceMap.put(mapKey, mapValue);
        verifyGet(sourceMap, mapKey, mapValue);

        CacheEntry sinkEntry = sinkMap.getCacheEntry(mapKey);
        assertNotNull("Null sink entry", sinkEntry);
        Object sinkGet = sinkEntry.getValue();
        assertSame("Incorrect sink get", mapValue, sinkGet);
    }

    //

    @Test
    public void testPutGet() throws Exception {
        populateMap(10000);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutReplace() throws Exception {
        DistributedMap map = populateMap(100);
        apply(100, (id, data) -> {
            // Re-use 'id' as a cheap changed value.
            verifyUpdate(map, id, data, id);
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutAll() throws Exception {
        DistributedMap map = getMap();

        String k1 = "one";
        String v1 = "val one";
        String k2 = "two";
        String v2 = "val two";
        String k3 = "three";
        String v3 = "val three";

        Map<Object, Object> fillMap = new HashMap<>(3);
        fillMap.put(k1, v1);
        fillMap.put(k2, v2);
        fillMap.put(k3, v3);

        verifyGet(map, k1, null);
        verifyGet(map, k2, null);
        verifyGet(map, k3, null);

        map.putAll(fillMap);

        verifyGet(map, k1, v1);
        verifyGet(map, k2, v2);
        verifyGet(map, k3, v3);
    }

    @Test
    public void testKeySet() throws Exception {
        DistributedMap map = populateMap(500);
        Set<?> keySet = map.keySet();
        apply(500, (id, data) -> {
            assertTrue("missing key", keySet.contains(id));
        });
    }

    @Test
    public void testValuesCollection() throws Exception {
        DistributedMap map = populateMap(500);
        Collection<?> values = map.values();
        apply(500, (id, data) -> {
            assertTrue("missing data", values.contains(data));
        });
    }

    @Test
    public void testContainsKey() throws Exception {
        DistributedMap map = populateMap(500);
        apply(500, (id, data) -> {
            assertTrue("missing key", map.containsKey(id));
        });
    }

    @Test
    public void testContainsValue() throws Exception {
        DistributedMap map = populateMap(500);
        apply(500, (id, data) -> {
            assertTrue("missing data", map.containsValue(data));
        });
    }

    //

    @Test
    public void testIsEmpty() throws Exception {
        DistributedMap map = getMap();
        assertTrue("map was not empty", map.isEmpty());
    }

    @Test
    public void testSize() {
        DistributedMap map = getMap();
        assertEquals("Incorrect initial map size", 0, map.size());

        map.put("one", "one");
        map.put("two", "two");
        map.put("three", "three");

        assertEquals("Incorrect map size", 3, map.size());
    }

    @Test
    public void testClear() {
        DistributedMap map = getMap();

        String[] deps = new String[] { "dependency id" };
        apply(500, (id, data) -> {
            verifyUpdate(map, id, 10, deps, null, data);
        });

        map.clear();

        apply(500, (id, data) -> {
            verifyGet(map, id, null);
        });
    }

    //

    @Test
    public void testNullAlias() throws Exception {
        DistributedMapImpl dm = new DistributedMapImpl(getCache());
        boolean failed;
        try {
            dm.addAlias(null, null);
            failed = false;
        } catch (RuntimeException e) {
            failed = true;
        }
        assertFalse("Added null alias", failed);
    }

    //

    @SuppressWarnings("unchecked")
    @Test
    public void testTimeout() throws Exception {
        String[] deps = new String[] { "dependency id" };
        DistributedMap map = populateMap(500, 1, deps);

        Thread.sleep(4000);

        apply(500, (id, data) -> {
            verifyGet(map, id, null);
        });

        map.setTimeToLive(3); // 3 sec
        map.setSharingPolicy(EntryInfo.NOT_SHARED);

        apply(500, (id, data) -> {
            map.put(id, data);
            verifyGet(map, id, data);
        });

        Thread.sleep(6000); // wait 6s for 3s timeout

        apply(500, (id, data) -> {
            verifyGet(map, id, null);
        });

        map.setTimeToLive(-1); // forever

        apply(500, (id, data) -> {
            verifyUpdate(map, id, null, data);
        });

        Thread.sleep(4000); // wait 4s

        apply(500, (id, data) -> {
            verifyGet(map, id, data);
        });
    }

    @Test
    public void testInvalidateId() throws Exception {
        String[] deps = new String[] { "dependency id" };
        DistributedMap map = populateMap(500, -1, deps);

        apply(500, (id, data) -> {
            map.invalidate(id);
        });

        apply(500, (id, data) -> {
            verifyGet(map, id, null);
        });
    }

    @Test
    public void testInvalidateGroup() throws Exception {
        String dep = "dependency id";
        String[] deps = new String[] { dep };
        DistributedMap map = populateMap(500, -1, deps);

        apply(500, (id, data) -> {
            verifyUpdate(map, id, -1, deps, null, data);
        });

        map.invalidate(dep);

        apply(500, (id, data) -> {
            verifyGet(map, id, null);
        });
    }

    @Test
    public void testInvalidate() throws Exception {
        DistributedMap map = getMap();

        String[] deps1 = new String[] { "depId1", "depId2", "depId3" };
        String[] deps2 = new String[] { "depId1", "depId2" };

        verifyUpdate(map, "one", -1, deps1, null, "value1");
        verifyUpdate(map, "two", -1, deps2, null, "value2");

        map.invalidate("depId3");

        verifyGet(map, "one", null);
        verifyGet(map, "two", "value2");
    }

    @Test
    public void testAlias() throws Exception {
        DistributedMap map = getMap();

        String[] deps = new String[] { "depId1", "depId2" };
        verifyUpdate(map, "one", -1, deps, null, "value1");

        map.addAlias("one", new String[] { "aliasOne", "aliasTwo" });
        String value = (String) map.get("aliasOne");
        assertEquals("get aliasOne", "value1", value);
        value = (String) map.get("aliasTwo");
        assertEquals("get aliasTwo", "value1", value);

        map.removeAlias("aliasOne");
        value = (String) map.get("aliasOne");
        assertNull("value not null after remove aliasOne", value);
        value = (String) map.get("aliasTwo");
        assertEquals("get aliasTwo", "value1", value);

        map.removeAlias("aliasTwo");
        value = (String) map.get("aliasOne");
        assertNull("value not null after remove aliasTwo", value);
        value = (String) map.get("one");
        assertEquals("get id - one", "value1", value);
    }

    // current performance target is 1.9.  we should decrease this
    // as much as possible.  this must be updated as we make
    // performance improvements.

    static final float targetRatio = 1.9f;

    @Test
    public void testPerformance() throws Exception {
        runPerformanceSuite(new TestParms(targetRatio, 5, 100, 25, 10));
    }

    @Test
    public void testPerformanceBig() throws Exception {
        runPerformanceSuite(new TestParms(targetRatio, 5, 1, 10000, 1));
    }

    protected void runPerformanceSuite(TestParms parms) throws Exception {
        Map<Object, Object> syncMap = Collections.synchronizedMap(new HashMap<Object, Object>());
        @SuppressWarnings("unchecked")
        Map<Object, Object> cacheMap = getMap();

        // Warm up ...
        long sw = runPerformance(syncMap, parms);
        long cw = runPerformance(cacheMap, parms);

        // Run 1 ...
        long s1 = runPerformance(syncMap, parms);
        long c1 = runPerformance(cacheMap, parms);

        // Run 2 ...
        long s2 = runPerformance(syncMap, parms);
        long c2 = runPerformance(cacheMap, parms);

        System.out.println("Threads [ " + parms.threads + " ]");
        System.out.println("Items   [ " + parms.items + " ]");
        System.out.println("Put loops [ " + parms.putLoops + " ]");
        System.out.println("Get loops [ " + parms.getLoops + " ]");
        System.out.println("          [ " + parms.putLoops * parms.getLoops + " ]");

        System.out.println("Ratio (target)      : " + parms.target);

        System.out.println("Standard (cold)     : " + sw + " ms");
        System.out.println("Distributed (cold)  : " + cw + " ms");
        float ratioC = ((float) (sw)) / ((float) (cw));
        System.out.println("Ratio (cold)        : " + ratioC);

        System.out.println("Standard (warm 1):  : " + s1 + " ms");
        System.out.println("Distributed (warm 1): " + c1 + " ms");
        float ratioW1 = ((float) (s1)) / ((float) (c1));
        System.out.println("Ratio (warm 1)      : " + ratioW1);

        System.out.println("Standard (warm 2):  : " + s2 + " ms");
        System.out.println("Distributed (warm 2): " + c2 + " ms");
        float ratioW2 = ((float) (s2)) / ((float) (c2));
        System.out.println("Ratio (warm 2)      : " + ratioW2);
    }

    protected long runPerformance(Map<Object, Object> storage, TestParms parms) throws Exception {
        long start = System.currentTimeMillis();

        TestThread threads[] = new TestThread[parms.threads];
        for (int threadNo = 0; threadNo < parms.threads; threadNo++) {
            threads[threadNo] = new TestThread("thread:" + threadNo, storage, parms);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        long end = System.currentTimeMillis();

        return end - start;
    }

    public static class TestParms {
        public final float target;

        public final int threads;

        public final int items;

        public final int putLoops;
        public final int getLoops;

        public TestParms(float target, int threads, int items, int putLoops, int getLoops) {
            this.target = target;
            this.threads = threads;
            this.items = items;
            this.putLoops = putLoops;
            this.getLoops = getLoops;
        }
    }

    public static class TestThread extends Thread {
        private final Map<Object, Object> storage;
        private final TestParms parms;

        public TestThread(String name, Map<Object, Object> storage, TestParms parms) {
            super(name);

            this.storage = storage;
            this.parms = parms;
        }

        @Override
        public void run() {
            String name = getName();

            Object[] ids = new Object[parms.items];
            Object[] data = new Object[parms.items];

            for (int idNo = 0; idNo < parms.items; idNo++) {
                ids[idNo] = name + ":" + idNo;
                data[idNo] = name + ": value:" + idNo;
            }

            for (int putNo = 0; putNo < parms.putLoops; putNo++) {
                for (int idNo = 0; idNo < parms.items; idNo++) {
                    Object id = ids[idNo];
                    Object value = data[idNo];
                    storage.put(id, value);
                }

                for (int getNo = 0; getNo < parms.getLoops; getNo++) {
                    for (int idNo = 0; idNo < parms.items; idNo++) {
                        Object id = ids[idNo];
                        Object expectedValue = data[idNo];
                        Object actualValue = storage.get(id);
                        assertSame("Inconsistent get", expectedValue, actualValue);
                    }
                }
            }

            storage.clear();
        }
    }

    //

    public static final String DISABLE_DEPENDENCY_ID =
        com.ibm.ws.cache.CacheConfig.DISABLE_DEPENDENCY_ID;

    @Test
    public void testDependencyDisabled() throws Exception {
        String nameEnabled = "mapEnabled";
        String nameDisabled = "mapDisabled";

        String id = "id";
        String data = "data";
        String depId = "dep id";
        String[] depIds = { depId };

        DistributedMap mapEnabled = getMap(nameEnabled);

        verifyUpdate(mapEnabled, id, 1, depIds, null, data);
        mapEnabled.invalidate(depId);
        verifyGet(mapEnabled, id, null);

        Properties propsDisabled = new Properties();
        propsDisabled.put(DISABLE_DEPENDENCY_ID, DistributedObjectCacheFactory.VALUE_TRUE);
        DistributedMap mapDisabled = getMap(nameDisabled, propsDisabled);

        verifyUpdate(mapDisabled, id, 1, depIds, null, data);
        mapDisabled.invalidate(depId);
        verifyGet(mapEnabled, id, data);
    }
}
//@formatter:on