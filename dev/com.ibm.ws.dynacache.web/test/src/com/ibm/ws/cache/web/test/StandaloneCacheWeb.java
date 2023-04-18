// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.web.test;

import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.DistributedNioMap;
import com.ibm.ws.cache.CacheConfig;
import com.ibm.ws.cache.CacheServiceImpl;
import com.ibm.ws.cache.DCacheBase;
import com.ibm.ws.cache.ObjectCacheUnitImpl;
import com.ibm.ws.cache.Scheduler;
import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.cache.spi.DistributedMapFactory;
import com.ibm.ws.cache.test.StandaloneCache;
import com.ibm.ws.cache.test.web.ServletCacheUnitImpl;
import com.ibm.wsspi.cache.DistributedObjectCacheFactory;

public class StandaloneCacheWeb extends StandaloneCache {

    /**
     * Wiring up of Dynacache internals for standalone client access
     */
    public static synchronized void initialize(String testName) throws Exception {
        Scheduler scheduler = new Scheduler();
        ScheduledThreadPoolExecutor ste = new ScheduledThreadPoolExecutor(5);
        scheduler.setExecutorService(ste);
        scheduler.setScheduledExecutorService(ste);
        scheduler.activate();

        ServerCache.coreCacheEnabled = true;
        ServerCache.objectCacheEnabled = true;
        ServerCache.servletCacheEnabled = true;
        ServerCache.cacheUnit.setObjectCacheUnit(new ObjectCacheUnitImpl());
        ServerCache.cacheUnit.setServletCacheUnit(new ServletCacheUnitImpl());

        CacheServiceImpl cs = new CacheServiceImpl();
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("id", DCacheBase.DEFAULT_CACHE_NAME);
        cs.start(new StubComponentContext(), props);

        CacheConfig baseCacheConfig = cs.getCacheConfig();
        ServerCache.createCache(baseCacheConfig.getCacheName(), baseCacheConfig);
    }

    public static synchronized void initialize(CacheConfig cc, String testName) {
        try {
            initialize(testName);
            ServerCache.getCacheService().addCacheInstanceConfig(cc, true);
            ((CacheServiceImpl) ServerCache.getCacheService()).setCacheName(cc.cacheName);
            ServerCache.createCache(cc.getCacheName(), cc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static public DistributedMap getMap() {
        return DistributedMapFactory.getMap(DCacheBase.DEFAULT_DISTRIBUTED_MAP_NAME);
    }

    static public DistributedNioMap getNioMap(String name) {
        return DistributedObjectCacheFactory.getMap(name);
    }

}