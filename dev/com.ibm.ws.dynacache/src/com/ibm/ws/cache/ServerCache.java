/*******************************************************************************
 * Copyright (c) 1997, 2009 IBM Corporation and others.
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
package com.ibm.ws.cache;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.cache.intf.CommandCache;
import com.ibm.ws.cache.intf.DCache;
import com.ibm.ws.cache.intf.JSPCache;
import com.ibm.ws.cache.stat.CachePerfFactory;
import com.ibm.wsspi.cache.CacheFeatureSupport;
import com.ibm.wsspi.cache.CacheProvider;
import com.ibm.wsspi.cache.CoreCache;

/**
 * This class creates and holds all(servlet and/or object) cache instances created.
 */
//@formatter:off
@Component(service = ServerCache.class, immediate = true,
           configurationPolicy = ConfigurationPolicy.IGNORE, property = "service.vendor=IBM")
public class ServerCache {
    private static TraceComponent tc = Tr.register(ServerCache.class,
                                                   "WebSphere Dynamic Cache",
                                                   "com.ibm.ws.cache.resources.dynacache");

    // Use this dependency to control service ordering,
    // but get real CacheService reference earlier via static method below.
    @Reference(service = CacheService.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.STATIC,
               target = "(id=baseCache)")
    protected void setCacheService(CacheService cs) {
        // Empty
    }

    private final static AtomicReference<CachePerfFactory> cachePerfFactoryRef =
        new AtomicReference<CachePerfFactory>();

    @Reference(service = CachePerfFactory.class,
               cardinality = ReferenceCardinality.OPTIONAL,
               policyOption = ReferencePolicyOption.GREEDY,
               policy = ReferencePolicy.DYNAMIC)
    protected void setCachePerfFactory(CachePerfFactory perfFactory) {
        cachePerfFactoryRef.set(perfFactory);

        for ( DCache dcache : cacheInstances.values() ) {
            dcache.setCachePerf(perfFactory);
        }
    }

    protected void unsetCachePerfFactory(CachePerfFactory perfFactory) {
        for ( DCache dcache : cacheInstances.values() ) {
            dcache.setCachePerf(null);
        }

        cachePerfFactoryRef.set(null);
    }

    //

    @Activate
    protected void activate(ComponentContext context) {
        if ( TraceComponent.isAnyTracingEnabled() && tc.isEventEnabled() ) {
            Tr.event(tc, "activate", context);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if ( TraceComponent.isAnyTracingEnabled() && tc.isEventEnabled() ) {
            Tr.event(tc, "deactivate", context);
        }
    }

    //

    private static int sharingPolicy = EntryInfo.NOT_SHARED;

    public static int getSharingPolicy() {
        return sharingPolicy;
    }

    public static void setSharingPolicy(int sharingPolicy) {
        ServerCache.sharingPolicy = sharingPolicy;
    }

    private static int pushFrequency = 0;

    public static int getPushFrequency() {
        return pushFrequency;
    }

    public static void setPushFrequency(int pushFrequency) {
        ServerCache.pushFrequency = pushFrequency;
    }

    //

    private static CacheService cacheService;

    public static void setCacheServiceEarly(CacheService cs) {
        cacheService = cs;
    }

    protected static void unsetCacheService(CacheService cs) {
        cacheService = null;
    }

    public final static CacheService getCacheService() {
        if ( cacheService == null ) {
            if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                Tr.debug(tc, "*** Cache Service has not been started.  NPEs are likely.");
            }
        }
        return cacheService;
    }

    //

    public static volatile boolean coreCacheEnabled;
    public static volatile boolean objectCacheEnabled;
    public static volatile boolean servletCacheEnabled;

    public static CacheUnit cacheUnit = new CacheUnitImpl( new CacheConfig() );

    public static JSPCache jspCache;
    public static CommandCache commandCache;

    // ---------------------------------------------------------
    // For Perf Advisor
    // ---------------------------------------------------------
    public static CacheInstanceInfo[] getCacheInstanceInfo() {
        if ( !coreCacheEnabled ) {
            if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                Tr.debug(tc, "Core Cache Service has not been started.");
            }
            return null;
        }

        return getCacheService().getCacheInstanceInfo();
    }

    // For CacheMonitor
    public static ArrayList getServletCacheInstanceNames() {
        ArrayList instanceNames;
        if ( !ServerCache.servletCacheEnabled ) {
            instanceNames = new ArrayList();
            if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                Tr.debug(tc, "CacheMonitor: Servlet Cache Service has not been started.");
            }
        } else {
            instanceNames = getCacheService().getServletCacheInstanceNames();
        }
        return instanceNames;
    }

    public static ArrayList getObjectCacheInstanceNames() {
        CacheService cs = getCacheService();
        return ( (cs == null) ? null : cs.getObjectCacheInstanceNames() );
    }

    //

    public static JSPCache getJspCache(String cacheName) {
        String methodName = "getJspCache";
        if ( !servletCacheEnabled ) {
            Tr.error(tc, "DYNA1059W", new Object[] { cacheName });
            // DYNA1059W=DYNA1059W: WebSphere Dynamic Cache instance named {0}
            // cannot be used because of Dynamic Servlet cache service has not be started.
            return null;
        }

        JSPCache cacheOut = null;
        if ( cacheName != null ) {
            try {
                cacheOut = cacheUnit.getJSPCache(cacheName);
            } catch ( Exception e ) {
                Tr.error(tc, "DYNA1003E", new Object[] { cacheName, e });
                // DYNA1003E=DYNA1003E: WebSphere Dynamic Cache instance named {0}
                // can not be initialized because of error {1}.
            }
        } else {
            cacheOut = ServerCache.jspCache;
        }
        if ( cacheOut == null ) {
            if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                Tr.debug(tc, methodName + " could not find " + cacheName);
            }
        }

        return cacheOut;
    }

    public static CommandCache getCommandCache(String cacheName) {
        String methodName = "getCommandCache";

        if ( !servletCacheEnabled ) {
            Tr.error(tc, "DYNA1059W", new Object[] { cacheName });
            // DYNA1059W=DYNA1059W: WebSphere Dynamic Cache instance named {0}
            // cannot be used because of Dynamic Servlet cache service has not be started.
            return null;
        }

        CommandCache cacheOut = null;
        if ( cacheName != null ) {
            try {
                cacheOut = cacheUnit.getCommandCache(cacheName);
            } catch ( Exception e ) {
                Tr.error(tc, "DYNA1003E", new Object[] { cacheName, e });
                // DYNA1003E=DYNA1003E: WebSphere Dynamic Cache instance named {0}
                // can not be initialized because of error {1}.
            }
        } else {
            cacheOut = ServerCache.commandCache;
        }
        if ( cacheOut == null ) {
            if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                Tr.debug(tc, methodName + " could not find " + cacheName);
            }
        }

        return cacheOut;
    }

    //

    public static DCache cache;

    private static Map<String, DCache> cacheInstances =
        new ConcurrentHashMap<String, DCache>();

    @Trivial
    public static Map getCacheInstances() {
        return cacheInstances;
    }

    public static int getActiveCacheInstanceCount() {
        return cacheInstances.size();
    }

    @Trivial
    public static DCache getCache(String cacheName) {
        String methodName = "getCache";

        if ( cacheName != null ) {
            cacheName = normalizeCacheName(cacheName, null);
        }

        DCache cacheOut;
        if ( (cacheName == null) || cacheName.equalsIgnoreCase(DCacheBase.DEFAULT_CACHE_NAME) ) {
            cacheOut = ServerCache.cache;
        } else {
            cacheOut = cacheInstances.get(cacheName);
        }
        if ( cacheOut == null ) {
            if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                Tr.debug(tc, methodName + " could not find " + cacheName);
            }
        }

        return cacheOut;
    }

    public static DCache getConfiguredCache(String cacheName) {
        String methodName = "getConfiguredCache";

        if ( !coreCacheEnabled ) {
            Tr.error(tc, "DYNA1003E", new Object[] { cacheName, "Core Cache Service has not been started." });
            // DYNA1003E=DYNA1003E: WebSphere Dynamic Cache instance named {0}
            // can not be initialized because of error {1}.
            return null;
        }

        if ( TraceComponent.isAnyTracingEnabled() && tc.isEntryEnabled() ) {
            Tr.entry(tc, methodName + " cacheName=" + cacheName);
        }

        DCache cacheOut = getCache(cacheName);
        if ( cacheOut != null ) {
            if ( TraceComponent.isAnyTracingEnabled() && tc.isEntryEnabled() ) {
                Tr.exit(tc, methodName + " retrieved " + cacheName);
            }
            return cacheOut;
        }

        CacheConfig config = getCacheService().getCacheInstanceConfig(cacheName);
        if ( config == null ) {
            Tr.error(tc, "DYNA1004E", new Object[] { cacheName });
            // DYNA1004E=DYNA1004E: WebSphere Dynamic Cache instance named {0}
            // can not be initialized because it  is not configured.
            // DYNA1004E.explanation=This message indicates the named WebSphere Dynamic Cache
            // instance can not  be initialized. The named instance is not available.
            // DYNA1004E.useraction=Use the WebSphere Administrative Console to configure
            // a cache instance  resource named {0}.

            if ( TraceComponent.isAnyTracingEnabled() && tc.isEntryEnabled() ) {
                Tr.exit(tc, methodName + " no configuration " + cacheName);
            }
            return null;
        }

        cacheOut = createCache(config.cacheName, config);
        if ( TraceComponent.isAnyTracingEnabled() && tc.isEntryEnabled() ) {
            Tr.exit(tc, methodName + " configured " + cacheName);
        }
        return cacheOut;
    }

    public synchronized static DCache createCache(String cacheName, CacheConfig cacheConfig) {
        String methodName = "createCache";

        if ( TraceComponent.isAnyTracingEnabled() && tc.isEntryEnabled() ) {
            Tr.entry(tc, methodName + " cacheName=" + cacheName +
                         " config=" + cacheConfig +
                         " enableReplication=" + cacheConfig.enableCacheReplication +
                         " provider: " + cacheConfig.cacheProviderName);
        }

        if ( !coreCacheEnabled ) {
            Tr.error(tc, "DYNA1003E", new Object[] { cacheName, "Core Cache Service has not been started." });
            // DYNA1003E=DYNA1003E: WebSphere Dynamic Cache instance named {0}
            // can not be initialized because of error {1}.

            if ( TraceComponent.isAnyTracingEnabled() && tc.isEntryEnabled() ) {
                Tr.exit(tc, methodName);
            }
            return null;
        }

        String normalizedCacheName = normalizeCacheName(cacheName, cacheConfig);

        if ( !cacheName.equals(normalizedCacheName) ) {
            if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                Tr.debug(tc, methodName + " normalized cache name [ " + tempCacheName + " ]");
            }
        }

        cacheConfig.determineCacheProvider();

        DCache cacheOut = getCache(normalizedCacheName);
        if ( cacheOut != null ) {
            if ( TraceComponent.isAnyTracingEnabled() && tc.isEntryEnabled() ) {
                Tr.exit(tc, methodName + " " + cacheOut);
            }
            return cacheOut;
        }

        String cacheProviderName;
        if ( cacheConfig.isDefaultCacheProvider() ) {
            cacheProviderName = CacheConfig.CACHE_PROVIDER_DYNACACHE;

        } else {
            cacheProviderName = cacheConfig.cacheProviderName;

            boolean cacheProviderError = false;

            CacheProvider cacheProvider = CacheProviderLoaderImpl.getInstance().getCacheProvider(cacheProviderName);
            if ( cacheProvider != null ) {
                // when firing event listeners we have to use cacheName non prefixed
                cacheConfig.cacheName = normalizedCacheName;
                CoreCache coreCache = cacheProvider.createCache(cacheConfig);
                CacheFeatureSupport featureSupport = cacheProvider.getCacheFeatureSupport();
                if ( (coreCache == null) || (featureSupport == null) ) {
                    cacheProviderError = true;
                    Tr.error(tc, "DYNA1066E", new Object[] { cacheConfig.cacheProviderName, cacheConfig.cacheName });
                    Tr.error(tc, "ENGLISH ONLY MESSAGE: coreCache == null || featureSupport == null....Check FFDC logs for Exceptions");
                    // DYNA1066E=DYNA1066E: Unable to initialize the cache provider \"{0}\".
                    // The Dynamic cache will be used as default cache provider to create cache instance \"{1}\".

                } else {
                    // start all services except TimeLimitDaemon
                    cacheUnit.startServices(false);

                    cacheOut = new CacheProviderWrapper(cacheConfig, featureSupport, coreCache);
                    cacheInstances.put(normalizedCacheName, cacheOut);

                    coreCache.start();
                    if ( normalizedCacheName.equals(DCacheBase.DEFAULT_CACHE_NAME) ) {
                        ServerCache.cache = cacheOut;
                    }
                    cacheConfig.cache = cacheOut;
                    cacheConfig.defaultProvider = false;

                    Tr.info(tc, "DYNA1001I", new Object[] { normalizedCacheName });
                    Tr.info(tc, "DYNA1071I", new Object[] { cacheProviderName });
                    // DYNA1001I=DYNA1001I: WebSphere Dynamic Cache instance named {0} initialized successfully.
                    // DYNA1071I=DYNA1071I: The cache provider \"{0}\" is being used.
                }

            } else {
                cacheProviderError = true;
                Tr.error(tc, "ENGLISH ONLY MESSAGE: cacheProvider is null. Check for the cache provider libraries ");
            }

            if ( cacheProviderError ) {
                cacheConfig.resetProvider(normalizedCacheName);
            }
        }

        if ( cacheOut == null ) {
            cacheProviderName = CacheConfig.CACHE_PROVIDER_DYNACACHE;

            // start all services including the time limit daemon
            cacheUnit.startServices(true);

            cacheOut = new Cache(normalizedCacheName, cacheConfig);
            cacheConfig.cache = cacheOut;

            cacheOut.setBatchUpdateDaemon( cacheUnit.getBatchUpdateDaemon() );

            CachePerfFactory factory = cachePerfFactoryRef.get();
            cacheOut.setCachePerf(factory);

            NotificationService ns = null; // TODO: Need an explanation.

            RemoteServices remoteServices = cacheUnit.getRemoteService();
            remoteServices.setNotificationService( new NullNotificationService() );

            cacheOut.setRemoteServices(remoteServices);
            cacheOut.setTimeLimitDaemon( cacheUnit.getTimeLimitDaemon() );
            cacheOut.setInvalidationAuditDaemon( cacheUnit.getInvalidationAuditDaemon() );

            if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                Tr.debug(tc, "Cache settings are: " + " cacheSize=" + cacheConfig.cacheSize +
                             " cachePriority=" + cacheConfig.cachePriority);
            }

            if ( cacheConfig.enableCacheReplication && (ns != null) ) {
                if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                    Tr.debug(tc, "Cache Replication is enabled: " +
                                 " replicationDomain=" + cacheConfig.replicationDomain +
                                 " replicationType=" + cacheConfig.replicationType +
                                 " defaultShareType=" + cacheConfig.defaultShareType);
                }
                ns.setCacheStatisticsListener( cacheOut.getCacheStatisticsListener() );
            } else {
                Tr.debug(tc, "Cache Replication is not enabled");
            }

            cacheInstances.put(normalizedCacheName, cacheOut);

            cacheOut.start();

            if (normalizedCacheName.equals(DCacheBase.DEFAULT_CACHE_NAME)) {
                ServerCache.cache = cacheOut;
            }

            Tr.info(tc, "DYNA1001I", new Object[] { normalizedCacheName });
            Tr.info(tc, "DYNA1071I", new Object[] { cacheProviderName });
            // DYNA1001I=DYNA1001I: WebSphere Dynamic Cache instance named {0} initialized successfully.
            // DYNA1071I=DYNA1071I: The cache provider \"{0}\" is being used.
        }

        if ( TraceComponent.isAnyTracingEnabled() && tc.isEntryEnabled() ) {
            Tr.exit(tc, methodName + " " + cacheOut);
        }
        return cacheOut;
    }

    /**
     * Test the cache configuration cache name.  If the set cache name does not
     * match the input cache name, change the configuration cache name to the
     * input cache name.
     *
     * First, adjust the input cache name: If the input cache name is either
     * the base JNDI cache name or the distributed JNDI cache name, change the
     * name to the non-JNDI name.
     *
     * @param cacheName A cache name which is to be tested against the configuration
     *     cache name.
     * @param cacheConfig A cache configuration which may be updated.
     *
     * @param The adjusted cache name.  Either, the input cache name, or the non-JNDI
     *     form of the input cache name.
     *
     * See {@link DCacheBase#DEFAULT_BASE_JNDI_NAME}, {@link DCacheBase#DEFAULT_CACHE_NAME},
     * {@link DCacheBase#DEFAULT_DMAP_JNDI_NAME}, and {@link DCacheBase#DEFAULT_DISTRIBUTED_MAP_NAME}.
     */
    @Trivial
    public static String normalizeCacheName(String cacheName, CacheConfig cacheConfig) {
        String normalizedCacheName;
        String normalizedCase;
        if ( cacheName.equalsIgnoreCase(DCacheBase.DEFAULT_BASE_JNDI_NAME) ) {
            normalizedCacheName = DCacheBase.DEFAULT_CACHE_NAME;
            normalizedCase = "base JNDI name";
        } else if ( cacheName.equalsIgnoreCase(DCacheBase.DEFAULT_DMAP_JNDI_NAME) ) {
            normalizedCacheName = DCacheBase.DEFAULT_DISTRIBUTED_MAP_NAME;
            normalizedCase = "distributed JNDI name";
        } else {
            normalizedCacheName = cacheName;
            normalizedCase = "specified name";
        }

        if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
            Tr.debug(tc, "Using cache name [ " + normalizedCacheName + " ]" +
                         " for [ " + cacheName + " ]: " + normalizedCase);
        }

        if ( (cacheConfig != null) && !normalizedCacheName.equals(cacheConfig.cacheName) ) {
            if ( TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled() ) {
                Tr.debug(tc, "Replacing configured cache name [ " + cacheConfig.cacheName + " ]" +
                             "with normalized cache name [ " + cacheName + + " ]");
            }
            cacheConfig.cacheName = normalizedCacheName;
        }

        return normalizedCacheName;
    }
}
//@formatter:on