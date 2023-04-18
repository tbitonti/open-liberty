/*******************************************************************************
 * Copyright (c) 1997, 2012 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import com.ibm.websphere.cache.DistributedObjectCache;
import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.cache.intf.DCache;
import com.ibm.ws.cache.intf.DCacheConfig;
import com.ibm.ws.cache.util.FieldInitializer;
import com.ibm.ws.ffdc.FFDCFilter;
import com.ibm.wsspi.kernel.service.location.WsLocationAdmin;
import com.ibm.wsspi.kernel.service.location.WsLocationConstants;

//@formatter:off
public class CacheConfig implements DCacheConfig, Cloneable {
    private static TraceComponent tc = Tr.register(CacheConfig.class,
                                                   "WebSphere Dynamic Cache",
                                                   "com.ibm.ws.cache.resources.dynacache");

    // Used in distributedmap.properties
    public static final String CACHE_NAME = "com.ibm.ws.cache.CacheConfig.cacheName";
    public static final String CACHE_SIZE = "com.ibm.ws.cache.CacheConfig.cacheSize";
    public static final String ENABLE_DISK_OFFLOAD = "com.ibm.ws.cache.CacheConfig.enableDiskOffload";
    public static final String DISK_OFFLOAD_LOCATION = "com.ibm.ws.cache.CacheConfig.diskOffloadLocation";
    public static final String USE_LISTENER_CONTEXT = "com.ibm.ws.cache.CacheConfig.useListenerContext";
    public static final String FLUSH_TO_DISK_ON_STOP = "com.ibm.ws.cache.CacheConfig.flushToDiskOnStop";
    public static final String ENABLE_LOCKING_SUPPORT = "com.ibm.ws.cache.CacheConfig.enableLockingSupport";
    public static final String DISABLE_DEPENDENCY_ID = "com.ibm.ws.cache.CacheConfig.disableDependencyId";
    public static final String DISABLE_TEMPLATES_SUPPORT = "com.ibm.ws.cache.CacheConfig.disableTemplatesSupport";
    public static final String ENABLE_NIO_SUPPORT = "com.ibm.ws.cache.CacheConfig.enableNioSupport";
    public static final String ENABLE_REPLICATION_ACKS = "com.ibm.ws.cache.CacheConfig.enableReplicationAcks";
    public static final String ENABLE_CACHE_REPLICATION = "com.ibm.ws.cache.CacheConfig.enableCacheReplication";
    public static final String REPLICATION_DOMAIN = "com.ibm.ws.cache.CacheConfig.replicationDomain";
    public static final String DISK_CLEANUP_FREQUENCY = "com.ibm.ws.cache.CacheConfig.htodCleanupFrequency";
    public static final String DISK_DELAY_OFFLOAD = "com.ibm.ws.cache.CacheConfig.htodDelayOffload";
    public static final String DISK_DELAY_OFFLOAD_ENTRIES_LIMIT = "com.ibm.ws.cache.CacheConfig.htodDelayOffloadEntriesLimit";
    public static final String DISK_DELAY_OFFLOAD_DEPID_BUCKETS = "com.ibm.ws.cache.CacheConfig.htodDelayOffloadDepIdBuckets";
    public static final String DISK_DELAY_OFFLOAD_TEMPLATE_BUCKETS = "com.ibm.ws.cache.CacheConfig.htodDelayOffloadTemplateBuckets";
    public static final String IGNORE_VALUE_IN_INVALIDATION_EVENT = "com.ibm.ws.cache.CacheConfig.ignoreValueInInvalidationEvent";
    public static final String DISKCACHE_PERFORMANCE_LEVEL = "com.ibm.ws.cache.CacheConfig.diskCachePerformanceLevel";
    public static final String DISKCACHE_EVICTION_POLICY = "com.ibm.ws.cache.CacheConfig.diskCacheEvictionPolicy";
    public static final String DISKCACHE_HIGH_THRESHOLD = "com.ibm.ws.cache.CacheConfig.diskCacheHighThreshold";
    public static final String DISKCACHE_LOW_THRESHOLD = "com.ibm.ws.cache.CacheConfig.diskCacheLowThreshold";
    public static final String DISKCACHE_SIZE = "com.ibm.ws.cache.CacheConfig.diskCacheSize";
    public static final String DISKCACHE_SIZE_GB = "com.ibm.ws.cache.CacheConfig.diskCacheSizeInGB";
    public static final String DISKCACHE_ENTRY_SIZE_MB = "com.ibm.ws.cache.CacheConfig.diskCacheEntrySizeInMB";
    public static final String USE_SERVER_CLASSLOADER = "com.ibm.ws.cache.CacheConfig.useServerClassLoader";
    public static final String DISKCACHE_EXPLICIT_BUFFER_LIMIT_ON_STOP = "com.ibm.ws.cache.CacheConfig.explicitBufferLimitOnStop";
    public static final String DISABLE_STORE_COOKIES = "com.ibm.ws.cache.CacheConfig.disableStoreCookies";
    public static final String FILTER_TIMEOUT_INVALIDATION = "com.ibm.ws.cache.CacheConfig.filterTimeOutInvalidation";
    public static final String FILTER_LRU_INVALIDATION = "com.ibm.ws.cache.CacheConfig.filterLRUInvalidation";
    public static final String FILTER_INACTIVITY_INVALIDATION = "com.ibm.ws.cache.CacheConfig.filterInactivityInvalidation";
    public static final String LRU_TO_DISK_TRIGGER_TIME = "com.ibm.ws.cache.CacheConfig.lruToDiskTriggerTime";
    public static final String LRU_TO_DISK_TRIGGER_PERCENT = "com.ibm.ws.cache.CacheConfig.lruToDiskTriggerPercent";
    public static final String CACHE_ENTRY_WINDOW = "com.ibm.ws.cache.CacheConfig.cacheEntryWindow";
    public static final String CACHE_PERCENTAGE_WINDOW = "com.ibm.ws.cache.CacheConfig.cachePercentageWindow";
    public static final String CACHE_INVALIDATE_ENTRY_WINDOW = "com.ibm.ws.cache.CacheConfig.cacheInvalidateEntryWindow";
    public static final String CACHE_INVALIDATE_PERCENT_WINDOW = "com.ibm.ws.cache.CacheConfig.cacheInvalidatePercentWindow";
    public static final String BATCH_UPDATE_MILLISECONDS = "com.ibm.ws.cache.CacheConfig.batchUpdateMilliseconds";
    public static final String CASCADE_CACHESPEC_PROPERTIES = "com.ibm.ws.cache.CacheConfig.cascadeCachespecProperties";
    public static final String CACHE_PROVIDER_NAME = "com.ibm.ws.cache.CacheConfig.cacheProviderName";
    public static final String MEMORY_CACHE_SIZE_IN_MB = "com.ibm.ws.cache.CacheConfig.memoryCacheSizeInMB";
    public static final String MEMORY_CACHE_HIGH_THRESHOLD = "com.ibm.ws.cache.CacheConfig.memoryCacheHighThreshold";
    public static final String MEMORY_CACHE_LOW_THRESHOLD = "com.ibm.ws.cache.CacheConfig.memoryCacheLowThreshold";
    public static final String CREATE_CACHE_AT_SERVER_STARTUP = "com.ibm.ws.cache.CacheConfig.createCacheAtServerStartup";
    public static final String REPLICATION_PAYLOAD_SIZE_IN_MB = "com.ibm.ws.cache.CacheConfig.replicationPayloadSizeInMB";
    public static final String CACHE_PROVIDER_OBJECT_GRID = "com.ibm.ws.objectgrid.dynacache.CacheProviderImpl";
    public static final String CACHE_PROVIDER_RESTORE_DYNACACHE_DEFAULTS = "com.ibm.ws.cache.CacheConfig.restoreDynacacheDefaults";
    public static final String AUTO_FLUSH_IMPORTS = "com.ibm.ws.cache.CacheConfig.autoFlushIncludes";
    public static final String PROPOGATE_INVALIDATIONS_NOT_SHARED = "com.ibm.ws.cache.CacheConfig.propogateInvalidationsNotShared";
    public static final String ALWAYS_SET_SURROGATE_CONTROL_HDR = "com.ibm.ws.cache.CacheConfig.alwaysSetSurrogateControlHdr";
    public static final String DISCARD_JSP_CONTENT = "discardJSPContent";
    public static final String USE_602_REQUIRED_ATTR_COMPATIBILITY = "com.ibm.ws.use602RequiredAttrCompatibility";
    public static final String ALWAYS_TRIGGER_COMMAND_INVALIDATIONS = "com.ibm.ws.CacheConfig.alwaysTriggerCommandInvalidations";
    public static final String CACHE_ENTRY_REF_COUNT_TRACKING = "com.ibm.ws.cache.CacheConfig.refCountTracking";
    public static final String ENABLE_INTER_CELL_INVALIDATION = "com.ibm.ws.cache.CacheConfig.enableInterCellInvalidation";
    public static final String ALWAYS_SYNCHRONIZE_ON_GETS = "com.ibm.ws.cache.CacheConfig.alwaysSynchronizeOnGets";
    public static final String FILTERED_STATUS_CODES = "com.ibm.ws.cache.CacheConfig.filteredStatusCodes";
    public static final String IGNORE_CACHEABLE_COMMAND_SERIALIZATION_EXCEPTION = "com.ibm.ws.cache.CacheConfig.ignoreCacheableCommandDeserializationException";
    public static final String DISK_DEPENDENCY_CACHE_INDEX_ENABLED = "com.ibm.ws.cache.CacheConfig.htodDependencyCacheIndexEnabled";
    public static final String LIBRARY_REF = "com.ibm.ws.cache.CacheConfig.libraryRef";
    public static final String WEBSERVICES_SET_REQUIRED_TRUE = "com.ibm.ws.cache.CacheConfig.webservicesSetRequiredTrue";

    // ---------------------------------------------------------
    // Warning - Never change these values!! They are
    // used by customers in cacheInstance.properties.
    // ---------------------------------------------------------
    public static final int CACHE_UNITS_ENTRIES = 0x01;
    public static final int CACHE_UNITS_KILOBYTES = 0x02;
    // ---------------------------------------------------------

    public static final int HIGH = 3;
    public static final int CUSTOM = 2;
    public static final int BALANCED = 1;
    public static final int LOW = 0;

    public static final int EVICTION_NONE = 0;
    public static final int EVICTION_RANDOM = 1;
    public static final int EVICTION_SIZE_BASED = 2;

    // -------------------------------------------------
    // Config settings - Behavior Change from v5
    // -------------------------------------------------
    boolean filterTimeOutInvalidation; // v5 was false
    boolean filterLRUInvalidation;
    boolean filterInactivityInvalidation;

    // Define for default value
    public static final int DEFAULT_DISABLE_CACHE_SIZE_MB = -1;
    public static final int DEFAULT_DISKCACHE_PERFORMANCE_LEVEL = BALANCED;
    public static final int DEFAULT_DISKCACHE_EVICTION_POLICY = EVICTION_NONE;
    public static final boolean DEFAULT_DISKCACHE_DELAY_OFFLOAD = true;
    public static final int DEFAULT_DISKCACHE_CLEANUP_FREQUENCY = 0;
    public static final int DEFAULT_MAX_BUFFERED_CACHE_IDS_PER_METADATA = 1000;
    public static final int DEFAULT_MAX_BUFFERED_DEPENDENCY_IDS = 1000;
    public static final int DEFAULT_MAX_BUFFERED_TEMPLATES = 100;
    public static final int DEFAULT_HIGH_THRESHOLD = 80; // unit in percent
    public static final int DEFAULT_LOW_THRESHOLD = 70; // unit in percent
    public static final int DEFAULT_DISKCACHE_SIZE = 0;
    public static final int DEFAULT_DISKCACHE_SIZE_GB = 0;
    public static final int DEFAULT_DISKCACHE_ENTRY_SIZE_MB = 0;
    public static final int DEFAULT_DISKCACHE_POOL_ENTRY_LIFE = 1000 * 60 * 5; // Life is five minutes
    public static final int DEFAULT_EXPLICIT_BUFFER_LIMIT_ON_STOP = 0; // explicit buffer limit on stop
    public static final int DEFAULT_ENTRY_WINDOW = 50; // PK32201 and PK35824 DRS (2% or 50 entries) batching fix
    public static final int DEFAULT_PERCENTAGE_WINDOW = 2; // PK32201 and PK35824 DRS (2% or 50 entries) batching fix
    public static final int DEFAULT_TLD_TIME_GRANULARITY = 5; // unit in sec
    public static final int DEFAULT_LRU_TO_DISK_TRIGGER_TIME = DEFAULT_TLD_TIME_GRANULARITY * 1000; // unit in msec
    public static final int DEFAULT_LRU_TO_DISK_TRIGGER_PERCENT = 0; // unit in percent
    public static final int DEFAULT_LRU_TO_DISK_TRIGGER_TIME_FOR_TRIMCACHE = 1000; // unit in msec
    public static final int DEFAULT_REPLICATION_PAYLOAD_SIZE_IN_MB = 10; // 10 MB

    // Define for maxinum and mininum values
    public static final int MAX_DISKCACHE_PERFORMANCE_LEVEL = HIGH;
    public static final int MIN_DISKCACHE_PERFORMANCE_LEVEL = LOW;
    public static final int MAX_CLEANUP_FREQUENCY = 1440;
    public static final int MIN_CLEANUP_FREQUENCY = 0;
    public static final int MAX_DISKCACHE_BUFFERED_CACHE_IDS_PER_METADATA = Integer.MAX_VALUE;
    public static final int MIN_DISKCACHE_BUFFERED_CACHE_IDS_PER_METADATA = 100;
    public static final int MAX_DISKCACHE_BUFFERED_DEPENDENCY_IDS = Integer.MAX_VALUE;
    public static final int MIN_DISKCACHE_BUFFERED_DEPENDENCY_IDS = 100;
    public static final int MAX_DISKCACHE_BUFFERED_TEMPLATES = Integer.MAX_VALUE;
    public static final int MIN_DISKCACHE_BUFFERED_TEMPLATES = 10;
    public static final int MAX_DISKCACHE_EVICTION_POLICY = EVICTION_SIZE_BASED;
    public static final int MIN_DISKCACHE_EVICTION_POLICY = EVICTION_NONE;
    public static final int MAX_DISKCACHE_SIZE = Integer.MAX_VALUE;
    public static final int MIN_DISKCACHE_SIZE = 20;
    public static final int MAX_DISKCACHE_SIZE_GB = Integer.MAX_VALUE;
    public static final int MIN_DISKCACHE_SIZE_GB = 3;
    public static final int MAX_DISKCACHE_ENTRY_SIZE_MB = Integer.MAX_VALUE;
    public static final int MIN_DISKCACHE_ENTRY_SIZE_MB = 0;
    public static final int MAX_HIGH_THRESHOLD = 100; // unit in percent
    public static final int MIN_HIGH_THRESHOLD = 1; // unit in percent
    public static final int MAX_LOW_THRESHOLD = 100; // unit in percent
    public static final int MIN_LOW_THRESHOLD = 1; // unit in percent
    public static final int MAX_LRU_TO_DISK_TRIGGER_TIME = DEFAULT_TLD_TIME_GRANULARITY * 1000; // unit in msec
    public static final int MIN_LRU_TO_DISK_TRIGGER_TIME = 1; // unit in msec
    public static final int MAX_LRU_TO_DISK_TRIGGER_PERCENT = 100; // unit in percent
    public static final int MIN_LRU_TO_DISK_TRIGGER_PERCENT = 0; // unit in percent

    /**
     * This determines how many cycles in the clock algorithm must pass before an unused
     * entry is chosen as a victim.  Each entry's clock starts with this and is decremented
     * each clock cycle.  A clock value of <= 0 implies a victim candidate.
     */
    public static int DEFAULT_PRIORITY = 1;
    public static int MAX_PRIORITY = 16;

    // -------------------------------------------------
    // Config settings - General
    // -------------------------------------------------
    boolean restoreDynacacheDefaults = true;
    boolean createCacheAtServerStartup;
    boolean autoFlushIncludes;

    String cacheName;
    String jndiName;
    int cachePriority = DEFAULT_PRIORITY;
    int jspCachePriority = DEFAULT_PRIORITY;
    int commandCachePriority = DEFAULT_PRIORITY;
    int diskHashBuckets = 1024;
    boolean webservicesSetRequiredTrue = true;

    // TODO: What is this for?  It appears to be unused / obsolete.
    String dtdDir;

    // -------------------------------------------------
    // Config settings - Cache Size
    // -------------------------------------------------
    int cacheSize = 2000;
    int memoryCacheSizeInMB = DEFAULT_DISABLE_CACHE_SIZE_MB; // default: -1 means disable
    int memoryCacheHighThreshold = 95;
    int memoryCacheLowThreshold = 80;

    // -------------------------------------------------
    // Config settings - Replication
    // -------------------------------------------------
    String replicationDomain;
    boolean enableCacheReplication;
    int replicationType = 0;
    int defaultShareType = EntryInfo.NOT_SHARED;
    int pushFrequency = 1; // seconds
    int batchUpdateInterval = 1000; // msec
    int batchUpdateMilliseconds = -1;
    // To indicate if replication is temporarily disabled due to congestion
    boolean drsDisabled;
    boolean drsBootstrapEnabled = true;
    int congestionSleepTimeMilliseconds = 250; // DRS congestion sleep time in ms
    int replicationPayloadSizeInMB = DEFAULT_REPLICATION_PAYLOAD_SIZE_IN_MB; // default payload size = 20 MB

    // -------------------------------------------------
    // Config settings - HTOD
    // -------------------------------------------------
    boolean flushToDiskOnStop;
    int htodCleanupFrequency = DEFAULT_DISKCACHE_CLEANUP_FREQUENCY;
    // in minutes; 0 means use cleanupHour instead; t >
    // 0 means run cleanup every t minutes; Max= 24 hr, Min 1 hr
    int diskCachePerformanceLevel = HIGH;
    // default: 1 means balanced setting which indicates some metadata will be kept in memory.
    // 0 means low setting which indicates limited metadata will be kept im memory.
    // 2 means custom setting which indicates some metadata will be kept im memory.
    // 3 means high setting which indicates all metadata will be kept in memory.
    int diskCacheEntrySizeInMB = DEFAULT_DISKCACHE_ENTRY_SIZE_MB;
    // default: 0 means disable or maximum size of
    // individual cache entry in MB.
    // Any cache entry larger than this when evicted from memory will not be offloaded to disk
    int diskCacheSizeInGB = DEFAULT_DISKCACHE_SIZE_GB;
    // default: 0 means disable or maximum disk cache size in GB
    int diskCacheSize = DEFAULT_DISKCACHE_SIZE;
    // default: 0 means disable or maximum disk cache size

    int diskCacheEvictionPolicy = EVICTION_RANDOM;
    int diskCacheHighThreshold = DEFAULT_HIGH_THRESHOLD;
    int diskCacheLowThreshold = DEFAULT_LOW_THRESHOLD;

    // -----------------------------------------------------------
    // Config settings
    // -----------------------------------------------------------
    boolean useListenerContext;
    boolean disableDependencyId;
    boolean enableLockingSupport;
    boolean disableTemplatesSupport;
    boolean enableReplicationAcks;
    boolean enableNioSupport;
    boolean propogateInvalidationsNotShared;
    boolean alwaysSetSurrogateControlHdr;
    String filteredStatusCodes;

    // -----------------------------------------------------------
    // Non-WCCM config items
    // -----------------------------------------------------------
    int maxTimeLimitInSeconds = 86400; // used by TimeLimitDaemon
    int configReloadInterval = 5000; // msec
    int timeGranularityInSeconds = DEFAULT_TLD_TIME_GRANULARITY; // used by TimeLimitDaemon

    // time in msec - frequency in which cache entries in memory are aynchronously offloaded to disk cache
    // Note: TLD granularity is a multiple of the lruToDiskTriggerTime
    // Default: 5000 msec or 5 sec (same as timeLimitDaemon)
    // Scope: applicable to all cache instances.
    int lruToDiskTriggerTime = DEFAULT_LRU_TO_DISK_TRIGGER_TIME;

    // Percentage of the memory cache size used as a overflow buffer when disk offload is enabled.
    // Cache entries in the overflow buffer are purged and asynchronously offloaded to disk at a
    // frequency of lruToDiskTriggerTime milliseconds. If the this memory overflow buffer is full,
    // cache entries are offloaded to disk synchronously on the callers thread.
    // Default: 0 - no overflow buffer
    // Scope: configurable per cache instance
    int lruToDiskTriggerPercent = DEFAULT_LRU_TO_DISK_TRIGGER_PERCENT;

    int timeHoldingInvalidations = 200000; // used by InvalidationAuditDaemon
    int htodCleanupHour = 0; // 0 means do it at midnight
    long htodInvalInterval = 24 * 60 * 60 * 1000; // ms
    boolean htodDelayOffload = DEFAULT_DISKCACHE_DELAY_OFFLOAD; // true means use Aux dep table
    int htodDelayOffloadDepIdBuckets = DEFAULT_MAX_BUFFERED_DEPENDENCY_IDS; // limit num of buckets for depid in Aux dep
    // table
    int htodDelayOffloadTemplateBuckets = DEFAULT_MAX_BUFFERED_TEMPLATES; // limit num of buckets for template in Aux
    // dep table
    int htodDelayOffloadEntriesLimit = DEFAULT_MAX_BUFFERED_CACHE_IDS_PER_METADATA; // limit num of cache entries for
    // depid or template in Aux dep
    // table; Min= 100
    int htodDataHashtableSize = 477551; // Hashtable size for disk cache entries
    int htodDepIdHashtableSize = 47743; // Hashtable size for dep id entries
    int htodTemplateHashtableSize = 1031; // Hashtable size for template entries
    int htodNumberOfPools = 20; // Number of primitive array pools
    int htodPoolSize = 2; // Number of primitive arrays in each pool
    int htodPoolEntryLife = DEFAULT_DISKCACHE_POOL_ENTRY_LIFE; // Life is five minutes
    int htodInvalidationBufferSize = 1000; // size of Invalidation buffer to trigger LPBT
    int htodInvalidationBufferLife = 1000 * 10; // Life for Invalidation buffer to trigger LPBT
    boolean htodDependencyCacheIndexEnabled;
    int explicitBufferLimitOnStop = DEFAULT_EXPLICIT_BUFFER_LIMIT_ON_STOP;

    int cacheEntryWindow = DEFAULT_ENTRY_WINDOW;
    int cachePercentageWindow = DEFAULT_PERCENTAGE_WINDOW;
    int cacheInvalidateEntryWindow = DEFAULT_ENTRY_WINDOW;
    int cacheInvalidatePercentWindow = DEFAULT_PERCENTAGE_WINDOW;

    public boolean disableTemplateInvalidation;
    boolean ignoreValueInInvalidationEvent; // default: false
    // false means the value is valid when firing invalidation event
    // true means the value is set to NULL when firing invalidation event
    boolean useServerClassLoader;
    boolean cascadeCachespecProperties;
    boolean use602RequiredAttrCompatibility;
    boolean alwaysTriggerCommandInvalidations;
    boolean alwaysSynchronizeOnGets;
    boolean ignoreCacheableCommandDeserializationException;

    String disableStoreCookies = "none";
    boolean cacheInstanceStoreCookies = true;

    // TODO: Unused in liberty.
    String topology = ""; // capturing the topology of a provider

    private boolean refCountTracking;
    private boolean enableInterCellInvalidation;

    //

    /**
     * Resolve a string value using location settings.
     *
     * Answer the default value if location settings are not available.
     *
     * See {@link Scheduler#getLocationAdmin()} and {@link WsLocationAdmin#resolveString}.
     *
     * @param value The value which is to be resolved.
     * @param defaultValue A supplier of a default value for when location settings
     *     are unavailable.
     *
     * @return The resolved value, or the supplied default value.
     */
    protected static String resolve(String value, Supplier<String> defaultValue) {
        WsLocationAdmin locationAdmin = Scheduler.getLocationAdmin();
        if ( locationAdmin == null ) {
            return defaultValue.get();
        } else {
            return locationAdmin.resolveString(value);
        }
    }

    //

    /**
     * Create a cache configuration from system properties.
     *
     * The cache unit {@link ServerCache#cacheUnit} value is
     * created using this initializer.
     */
    public CacheConfig() {
        // TODO: The server name is not set!
        // setServerName();

        // TODO: The properties are not recorded!
        // recordProperties( System.getProperties() );

        systemOverrideCacheConfig();
        determineCacheProvider();

        // TODO: Status codes are not parsed.
        // setStatusCodes();

        if ( tc.isDebugEnabled() ) {
            Tr.debug(tc, "CacheConfig cacheName=" + cacheName);
        }
    }

    /**
     * Convert a map into a properties object.
     *
     * This is done to enable the field initializer to
     * consume tables of configuration values which
     * are stored as java maps {@link java.util.Map}.
     *
     * @param pMap The table of properties which is to be converted.
     *
     * @return A properties object which contains all of the keys
     *     and values of the table.
     */
    public static Properties convert(Map<String, Object> pMap) {
        Properties props = new Properties();
        pMap.forEach( (pKey, pValue) -> {
            props.put(pKey, pValue);
        });
        return props;
    }

    /**
     * Create a cache configuration with defaults and with specified overrides.
     *
     * Use system properties as a first layer of overrides, then apply the
     * specified overrides.
     *
     * Used when creating configurations from the server configuration.  See
     * {@link CacheServiceImpl#parsePropertiesFromOSGiConfigAdmin}.
     *
     * @param overrides Overrides for the new configuration.
     */
    public CacheConfig(Map<String, Object> overrides) {
        setServerName();

        // TODO: This is backwards: Fields are initialized with the overrides
        //       having precedence.  Properties are stored with the system properties
        //       having precedence.
        recordProperties( overrides );
        recordProperties( System.getProperties() );

        systemOverrideCacheConfig();
        overrideCacheConfig( convert(overrides) );
        determineCacheProvider();

        setStatusCodes();

        if ( tc.isDebugEnabled() ) {
            Tr.debug(tc, "CacheConfig cacheName=" + cacheName);
        }
    }

    /**
     * Create a cache configuration from a base configuration, from system properties,
     * and from properties overrides.
     *
     * Configuration values are assigned first from the base configuration,
     * then from system properties, then from the properties override.
     *
     * Used by {@link CacheServiceImpl#addCacheInstanceConfig}.  The base
     * configuration is the default cache configuration,
     * {@link DCacheBase#DEFAULT_CACHE_NAME}.
     *
     * @param overrides Property overrides for the cache configuration.
     * @param baseConfig The base cache configuration.
     */
    public CacheConfig(Properties overrides, CacheConfig baseConfig) {
        overrideCacheConfig(baseConfig);

        setServerName();

        // TODO: This is backwards: Fields are initialized with the overrides
        //       having precedence.  Properties are stored with the system properties
        //       having precedence.
        recordProperties( overrides );
        recordProperties( System.getProperties() );

        // TODO: Reapplying the system properties is problematic if there are
        //       the same properties set in override properties.

        systemOverrideCacheConfig();
        overrideCacheConfig(overrides);
        determineCacheProvider();

        // TODO: Status codes are not parsed.
        // setStatusCodes();

        if ( tc.isDebugEnabled() ) {
            Tr.debug(tc, "CacheConfig cacheName=" + cacheName);
        }
    }

    @Override
    public Object clone() {
        CacheConfig clone;
        try {
            clone = (CacheConfig) super.clone();
        } catch ( CloneNotSupportedException e ) {
            FFDCFilter.processException(e, "com.ibm.ws.cache.CacheConfig", "314");
            return null; // Unexpected.  NPE's will result.
        }

        // Live cache configurations are cloned.  Reset the dynamic
        // fields to avoid problems.

        // The new configuration must be given new names.  Otherwise, there
        // is a collision with the source configuration.
        clone.cacheName = null;
        clone.jndiName = null;

        // The new configuration is not yet associated with either a cache
        // or a distributed object cache.
        clone.cache = null;
        clone.distributedObjectCache = null;

        // TODO: Why disable these?
        clone.enableServletSupport = false;
        clone.disableTemplatesSupport = false;
        clone.disableDependencyId = false;

        // Disable these: Avoid a collision with the source cache.
        clone.enableDiskOffload = false;
        clone.flushToDiskOnStop = false;

        // TODO: Handling of properties, which were just shallowly assigned.

        // TODO: Handling of external groups, which were just shallowly
        //       assigned.

        return clone;
    }

    /**
     * Reset dynamic state of the cache configuration.
     */
    protected void reset() {
        resetServerName();

        // The new configuration must be given new names.  Otherwise, there
        // is a collision with the source configuration.
        cacheName = null;
        // TODO: Why not reset this?
        // jndiName = null;

        // The new configuration is not yet associated with either a cache
        // or a distributed object cache.
        cache = null;
        distributedObjectCache = null;

        // TODO: Why not disable these, as is done by clone?
        // enableDiskOffload = false;
        // flushToDiskOnStop = false;
        // TODO ... and yet this is cleared.
        diskOffloadLocation = null;

        // TODO: Why disable this?  The workarea should compute
        //       to the same value.
        workareaDir = null;

        // TODO: What is this for?  It appears to be unused / obsolete.
        dtdDir = null;

        clearExternalGroups();
    }

    // Cache provider information ...

    public static final String CACHE_PROVIDER_DYNACACHE = "default";

    protected String cacheProviderName = CACHE_PROVIDER_DYNACACHE;
    protected boolean defaultProvider = true;

    // TODO: How is this used?
    // See: com.ibm.ws.cache.CacheServiceImpl.findOrCreateOSGiConfiguration(CacheConfig),
    // There the property is transferred into the new cache configuration.
    // That is a passive transfer.  There is no indication of the library reference
    // is eventually used.
    protected String libraryRef;

    @Override
    public String getCacheProviderName() {
        return cacheProviderName;
    }

    @Override
    @Trivial
    public boolean isDefaultCacheProvider() {
        return defaultProvider;
    }

    /**
     * Determine whether the default cache provider is in use.
     *
     * That is, if the cache provider name is {@link #CACHE_PROVIDER_DYNACACHE}.
     *
     * If the cache provider is unset, assign the default.
     */
    public void determineCacheProvider() {
        String providerCase;

        if ( cacheProviderName.equals("") ) {
            providerCase = "defaulted";
            defaultProvider = true;
            cacheProviderName = CacheConfig.CACHE_PROVIDER_DYNACACHE;
        } else {
            if ( cacheProviderName.equals(CACHE_PROVIDER_DYNACACHE) ) {
                providerCase = "default";
                defaultProvider = true;
            } else {
                providerCase = "alternate";
                defaultProvider = false;
            }
        }

        if ( tc.isDebugEnabled() ) {
            Tr.debug(tc, "Cache [ " + cacheName + " ] provider [ " + cacheProviderName + " ] (" + providerCase + ")");
        }
    }

    //

    /**
     * Record of properties which were used to populate this cache configuration.
     *
     * This is redundant with the actual configuration fields.
     *
     * This will contain all system properties and will contain all properties
     * provided as overrides.
     *
     * Since the properties record the fixed values of the configuration,
     * resetting the cache configuration does not reset these properties.
     */
    private final Map<String, String> properties = new HashMap<String, String>();

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    protected void recordProperties(Map<String, Object> props) {
        props.forEach( ( String key, Object value ) -> {
            properties.put(key, (String) value);
        } );
    }

    protected void recordProperties(Properties props) {
        props.forEach( ( Object key, Object value ) -> {
            properties.put( (String) key, (String) value );
        } );
    }

    //

    /**
     * Set configuration values with values from a base configuration.
     *
     * Only copy configuration values: Do not copy dynamic state.
     *
     * Disable disk-offloading.  Otherwise, the new cache configuration
     * could be used to write to the same cache location as the base
     * configuration.
     *
     * @param baseConfig A cache configuration from which to copy values.
     */
    public void overrideCacheConfig(CacheConfig baseConfig) {
        cachePriority = baseConfig.cachePriority;
        jspCachePriority = baseConfig.jspCachePriority;
        commandCachePriority = baseConfig.commandCachePriority;
        diskHashBuckets = baseConfig.diskHashBuckets;

        // DynamicCache settings - Cache Size
        // Common for ServletCacheInstance and ObjectCacheInstance
        cacheSize = baseConfig.cacheSize;
        cacheProviderName = baseConfig.cacheProviderName;

        // DynamicCache settings - Replication Defaults
        cacheProviderName = baseConfig.cacheProviderName;
        enableCacheReplication = baseConfig.enableCacheReplication;
        replicationType = baseConfig.replicationType;

        // DynamicCache settings - Replication
        setBatchUpdateInterval(baseConfig, baseConfig.pushFrequency);

        // DynamicCache settings - HTOD
        // Turn these off: Otherwise, a cache created from the new cache configuration
        // collides with a cache created from the base configuration.
        enableDiskOffload = false;
        flushToDiskOnStop = false;

        diskCacheSizeInGB = baseConfig.diskCacheSizeInGB;
        diskCacheSize = baseConfig.diskCacheSize;
        diskCacheEntrySizeInMB = baseConfig.diskCacheEntrySizeInMB;
        diskCachePerformanceLevel = baseConfig.diskCachePerformanceLevel;
        htodCleanupFrequency = baseConfig.htodCleanupFrequency;
        diskCacheEvictionPolicy = baseConfig.diskCacheEvictionPolicy;
        diskCacheHighThreshold = baseConfig.diskCacheHighThreshold;
        diskCacheLowThreshold = baseConfig.diskCacheLowThreshold;
        htodDelayOffloadEntriesLimit = baseConfig.htodDelayOffloadEntriesLimit;
        htodDelayOffloadDepIdBuckets = baseConfig.htodDelayOffloadDepIdBuckets;
        htodDelayOffloadTemplateBuckets = baseConfig.htodDelayOffloadTemplateBuckets;

        // TODO: There is a loss of history here.  If the base configuration was updated
        //       with properties, that information is not transferred.
    }

    /**
     * Override the cache configuration properties with system properties.
     */
    public void systemOverrideCacheConfig() {
        FieldInitializer.initFromSystemProperties(this);
    }

    /**
     * Override the cache configuration properties with specified properties.
     *
     * Use by {@link com.ibm.ws.cache.spi.DistributedMapFactory}.
     *
     * @param properties Properties used to override the cache configuration.
     */
    public void overrideCacheConfig(Properties properties) {
        if ( properties != null ) {
            FieldInitializer.initFromSystemProperties(this, properties);
        }

        processOffloadDirectory();

        // TODO: Why do this?
        if ( !enableServletSupport ) {
            disableTemplatesSupport = true;
        }
    }

    //

    private String workareaDir;

    protected String getWorkarea() {
        if ( workareaDir == null ) {
            workareaDir = resolve(WsLocationConstants.SYMBOL_SERVER_WORKAREA_DIR,
                              () -> System.getProperty("java.io.tmpdir"));
            if ( tc.isDebugEnabled() ) {
                Tr.debug(tc, "cache workarea [ " + workareaDir + " ]");
            }
        }
        return workareaDir;
    }


    protected boolean enableDiskOffload;
    protected String diskOffloadLocation;

    @Override
    public boolean isEnableDiskOffload() {
        return enableDiskOffload;
    }

    public void setOffloadOffloadLocationAndProcess(String diskOffloadLocation) {
        this.diskOffloadLocation = diskOffloadLocation;

        processOffloadDirectory();
    }

    // Used by test cases
    public void setDiskOffloadLocation(String diskOffloadLocation) {
        this.diskOffloadLocation = diskOffloadLocation;
    }

    // Used by test cases
    public void setEnableDiskOffload(boolean enableDiskOffload) {
        this.enableDiskOffload = enableDiskOffload;

        processOffloadDirectory();
    }

    /**
     * Process the offload location.
     *
     * Do nothing if disk offloading is disabled.
     *
     * Otherwise, resolve the disk offload location using location settings.
     *
     * If location settings are not available, use a peer to the workarea
     * directory.
     */
    private void processOffloadDirectory() {
        // TODO: This is not a proper location to use.
        Supplier<String> offloadLocation = () -> getWorkarea() + "_dynacache";

        if ( enableDiskOffload ) {
            if ( (diskOffloadLocation != null) && !diskOffloadLocation.isEmpty() ) {
                diskOffloadLocation = resolve(diskOffloadLocation, offloadLocation);
            } else {
                diskOffloadLocation = offloadLocation.get();
            }
            if ( tc.isDebugEnabled() ) {
                Tr.debug(tc, "cache offload [ " + diskOffloadLocation + " ]");
            }
        } else {
            if ( tc.isDebugEnabled() ) {
                Tr.debug(tc, "cache offload is disabled");
            }
        }
    }

    //

    /**
     * Answer the name of the node of the server.
     * Answer null, since Liberty does not use the
     * concept of nodes.
     *
     * @return The name of the node of the server.
     */
    @Override
    public String getServerNodeName() {
        return null;
    }

    private String serverName;

    /**
     * Answer the name of the server.  In Liberty,
     * this is obtained from the location admin.
     *
     * See {@link WsLocationAdmin#getServerName}.
     *
     * @return The name of the server.
     */
    @Override
    public String getServerServerName() {
        return serverName;
    }

    private void setServerName() {
        WsLocationAdmin locAdmin = Scheduler.getLocationAdmin();
        if ( locAdmin != null ) {
            serverName = locAdmin.getServerName();
        } else {
            // allow to run outside of OSGI for Unit tests
        }
    }

    private void resetServerName() {
        serverName = null;
    }

    //

    private int[] statusCodes;

    private void setStatusCodes() {
        if ( filteredStatusCodes == null ) {
            return;
        }

        if ( tc.isDebugEnabled() ) {
            Tr.debug(tc, "filteredStatusCodes " + filteredStatusCodes);
        }

        StringTokenizer codeTokens = new StringTokenizer(filteredStatusCodes);
        int[] codes = new int[ codeTokens.countTokens() ];

        for ( int codeNo = 0 ; codeTokens.hasMoreTokens(); codeNo++ ) {
            String codeToken = codeTokens.nextToken();

            int statusCode;
            try {
                statusCode = Integer.parseInt(codeToken);
            } catch ( NumberFormatException ex ) {
                Tr.error(tc, "Error parsing status code [ " + codeToken + " ]", ex);
                statusCode = -1;
            }

            if ( tc.isDebugEnabled() ) {
                Tr.debug(tc, "Status code [ " + codeToken + " ] [ " + statusCode + " ]");
            }
            codes[codeNo] = statusCode;
        }

        statusCodes = codes;
    }

    //

    boolean enableServletSupport = true;

    @Override
    public boolean isEnableServletSupport() {
        return enableServletSupport;
    }


    //

    @Override
    public int getBatchUpdateInterval() {
        return batchUpdateInterval;
    }

    @Override
    public int getCachePriority() {
        return cachePriority;
    }

    @Override
    public int getCacheSize() {
        return cacheSize;
    }

    public int getCacheSizeInMB() {
        return memoryCacheSizeInMB;
    }

    @Override
    public int getCleanupFrequency() {
        return htodCleanupFrequency;
    }

    @Override
    public int getDelayOffloadDepIdBuckets() {
        return htodDelayOffloadDepIdBuckets;
    }

    @Override
    public int getDelayOffloadEntriesLimit() {
        return htodDelayOffloadEntriesLimit;
    }

    @Override
    public int getDelayOffloadTemplateBuckets() {
        return htodDelayOffloadTemplateBuckets;
    }

    @Override
    public int getDiskCacheEntrySizeInMB() {
        return diskCacheEntrySizeInMB;
    }

    @Override
    public int getDiskCacheEvictionPolicy() {
        return diskCacheEvictionPolicy;
    }

    @Override
    public int getDiskCacheHighThreshold() {
        return diskCacheHighThreshold;
    }

    @Override
    public int getDiskCacheLowThreshold() {
        return diskCacheLowThreshold;
    }

    @Override
    public int getDiskCachePerformanceLevel() {
        return diskCachePerformanceLevel;
    }

    // Batching parameters for Dynacache - DRS interaction

    @Override
    public int getDiskCacheSize() {
        return diskCacheSize;
    }

    @Override
    public int getDiskCacheSizeInGB() {
        return diskCacheSizeInGB;
    }

    @Override
    public int getEntryWindow() {
        return cacheEntryWindow;
    }

    @Override
    public int getHighThresholdCacheSizeInMB() {
        return this.memoryCacheHighThreshold;
    }

    @Override
    public int getLowThresholdCacheSizeInMB() {
        return this.memoryCacheLowThreshold;
    }

    @Override
    public int getInvalidateEntryWindow() {
        return cacheInvalidateEntryWindow;
    }

    @Override
    public int getInvalidatePercentageWindow() {
        return this.cacheInvalidatePercentWindow;
    }

    @Override
    public int getLruToDiskTriggerPercent() {
        return lruToDiskTriggerPercent;
    }

    @Override
    public int getLruToDiskTriggerTime() {
        return lruToDiskTriggerTime;
    }

    @Override
    public long getMaxCacheSize() {
        return cacheSize;
    }

    @Override
    public long getMaxCacheSizeInMB() {
        return memoryCacheSizeInMB;
    }

    @Override
    public int getPercentageWindow() {
        return cachePercentageWindow;
    }

    @Override
    public int getCongestionSleepTimeMilliseconds() {
        return congestionSleepTimeMilliseconds;
    }

    @Override
    public boolean isCacheInstanceStoreCookies() {
        return cacheInstanceStoreCookies;
    }

    @Override
    public boolean isCascadeCachespecProperties() {
        return this.cascadeCachespecProperties;
    }

    @Override
    public boolean isDelayOffload() {
        return this.htodDelayOffload;
    }

    @Override
    public boolean isDrsBootstrapEnabled() {
        return this.drsBootstrapEnabled;
    }

    @Override
    public boolean isDrsDisabled() {
        return this.drsDisabled;
    }

    @Override
    public boolean isEnableCacheReplication() {
        return this.enableCacheReplication;
    }

    @Override
    public boolean isFilterLRUInvalidation() {
        return this.filterLRUInvalidation;
    }

    @Override
    public boolean isFilterTimeOutInvalidation() {
        return this.filterTimeOutInvalidation;
    }

    @Override
    public boolean isFilterInactivityInvalidation() {
        return this.filterInactivityInvalidation;
    }

    @Override
    public boolean isFlushToDiskOnStop() {
        return this.flushToDiskOnStop;
    }

    @Override
    public boolean isUseServerClassLoader() {
        return this.useServerClassLoader;
    }

    @Override
    public void setCacheInstanceStoreCookies(boolean cacheInstanceStoreCookies) {
        this.cacheInstanceStoreCookies = cacheInstanceStoreCookies;
    }

    @Override
    public void setCachePriority(int cachePriority) {
        if (cachePriority < 0) {
            cachePriority = DEFAULT_PRIORITY;
        }
        this.cachePriority = cachePriority;
    }

    @Override
    public void setDrsBootstrapEnabled(boolean drsBootstrapEnabled) {
        this.drsBootstrapEnabled = drsBootstrapEnabled;
        if ( tc.isDebugEnabled() ) {
            Tr.debug(tc, "setDRSBootstrap() cacheName=" + cacheName +
                         " drsBootstrap=" + drsBootstrapEnabled);
        }
    }

    @Override
    public void setDrsDisabled(boolean drsDisabled) {
        this.drsDisabled = drsDisabled;
    }

    // Used by test cases
    public void setEnableNioSupport(boolean b) {
        this.enableNioSupport = b;
    }

    public void setMaxCacheSize(int size) {
        this.cacheSize = size;
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }

    @Override
    public int getDefaultShareType() {
        return defaultShareType;
    }

    @Override
    public boolean isDisableDependencyId() {
        return disableDependencyId;
    }

    @Override
    public boolean isEnableNioSupport() {
        return enableNioSupport;
    }

    @Override
    public EvictorAlgorithmType getEvictorAlgorithmType() {
        return EvictorAlgorithmType.LRUEvictor;
    }

    @Override
    public boolean isDistributed() {
        return isEnableCacheReplication();
    }

    @Override
    public int getReplicationPayloadSizeInMB() {
        return replicationPayloadSizeInMB;
    }

    @Override
    public boolean isAutoFlushIncludes() {
        return autoFlushIncludes;
    }

    @Override
    public boolean alwaysSetSurrogateControlHdr() {
        return alwaysSetSurrogateControlHdr;
    }

    @Override
    public boolean isUse602RequiredAttrCompatibility() {
        return use602RequiredAttrCompatibility;
    }

    @Override
    public boolean alwaysTriggerCommandInvalidations() {
        return alwaysTriggerCommandInvalidations;
    }

    @Override
    public boolean alwaysSynchronizeOnGets() {
        return alwaysSynchronizeOnGets;
    }

    @Override
    public void setAlwaysSynchronizeOnGets(boolean alwaysSynchronizeOnGets) {
        this.alwaysSynchronizeOnGets = alwaysSynchronizeOnGets;
    }

    @Override
    public int[] getFilteredStatusCodes() {
        return statusCodes;
    }

    @Trivial
    public boolean isRefCountTrackingEnabled() {
        return refCountTracking;
    }

    @Override
    public boolean isEnableInterCellInvalidation() {
        return enableInterCellInvalidation;
    }

    @Override
    public boolean isWebservicesSetRequiredTrue() {
        return webservicesSetRequiredTrue;
    }

    // only called when the alternate cache provider could not create the cache.. we need to then revert to the default
    public void resetProvider(String cacheName) {
        if (tc.isDebugEnabled()) {
            Tr.debug(tc, "Reverting to the default Dynacache cache provider");
        }
        this.cacheProviderName = CACHE_PROVIDER_DYNACACHE;
        this.enableCacheReplication = false;
        this.defaultProvider = true;
        this.cacheName = cacheName;
    }

    /**
     * This method reverts the common configuration template for all cache instances to use Dynaache defaults. This
     * method only comes into play when ObjectGrid is configured as the cache provider for the default cache.
     */
    void restoreDynacacheProviderDefaults() { // restore commonConfig to Dynacache defaults
        if (restoreDynacacheDefaults) {
            if (cacheProviderName != CacheConfig.CACHE_PROVIDER_DYNACACHE) {
                cacheProviderName = CacheConfig.CACHE_PROVIDER_DYNACACHE;
                enableCacheReplication = false;
                if (tc.isDebugEnabled()) {
                    Tr.debug(tc, "OVERRIDING Object Grid default for " + cacheName);
                }
            }
        }
    }

    @Override
    public boolean isIgnoreCacheableCommandDeserializationException() {
        return ignoreCacheableCommandDeserializationException;
    }

    private void setBatchUpdateInterval(Object config, int pushFrequency) {

        if (tc.isDebugEnabled()) {
            Tr.entry(tc, "setBatchUpdateInterval", new Object[] { config });
        }

        if (pushFrequency < 1) {
            pushFrequency = 1;
        }

        // If we have batchUpdateMilliseconds defined, use it. Otherwise, use old method
        if (batchUpdateMilliseconds == -1) {
            batchUpdateInterval = pushFrequency * 1000;
        }

        if (tc.isDebugEnabled()) {
            Tr.exit(tc, "setBatchUpdateInterval", new Integer(batchUpdateInterval));
        }
    }

    public void setDiskCacheEvictionPolicy(int evictionPolicy) {
        String methodName = "setDiskCacheEvictionPolicy";
        if ( tc.isDebugEnabled() ) {
            Tr.entry(tc, methodName, Integer.valueOf(evictionPolicy));
        }

        if ( (evictionPolicy >= CacheConfig.EVICTION_NONE) && (evictionPolicy <= CacheConfig.EVICTION_SIZE_BASED) ) {
            diskCacheEvictionPolicy = evictionPolicy;
        } else {
            if (tc.isDebugEnabled()) {
                Tr.debug(tc, methodName + " ignoring [ " + evictionPolicy + " ]: Out of range");
            }
        }

        if ( tc.isDebugEnabled() ) {
            Tr.entry(tc, methodName, Integer.valueOf(diskCacheEvictionPolicy));
        }
    }

    public boolean isCreateCacheAtServerStartup() {
        return createCacheAtServerStartup;
    }

    public int getJspCachePriority() {
        return jspCachePriority;
    }


    //

    //

    DCache cache;
    DistributedObjectCache distributedObjectCache;

    public DCache getCache() {
        return cache;
    }

    public void setCache(DCache cache) {
        this.cache = cache;
    }

    public DistributedObjectCache getDistributedObjectCache() {
        return distributedObjectCache;
    }

    public void setDistributedObjectCache(DistributedObjectCache cache) {
        this.distributedObjectCache = cache;
    }

    // -------------------------------------------------
    // Config settings - External Cache Groups
    // -------------------------------------------------

    public static class ExternalCacheGroup {
        public String name;
        public int type;
        public List<ExternalCacheGroupMember> members = new ArrayList<ExternalCacheGroupMember>(0);
    }

    public static class ExternalCacheGroupMember {
        public String address;
        public String beanName;
    }

    private final List<ExternalCacheGroup> externalGroups = new ArrayList<ExternalCacheGroup>();

    public void addExternalGroup(ExternalCacheGroup group) {
        externalGroups.add(group);
    }

    protected void clearExternalGroups() {
        externalGroups.clear();
    }

    public List<ExternalCacheGroup> getExternalGroups() {
        return externalGroups;
    }

    //

    @Override
    public String toString() {
        return "CacheConfig [ " +
                        "cacheName=" + cacheName +
                        ", jndiName=" + jndiName +
                        ", cacheSize=" + cacheSize +
                        ", cache=" + cache +
                        ", libraryRef=" + libraryRef +
                        ", createCacheAtServerStartup=" + createCacheAtServerStartup +
                        ", distributedObjectCache=" + distributedObjectCache +
                        ", defaultProvider=" + defaultProvider +
                        ", enableCacheReplication=" + enableCacheReplication +
                        ", enableDiskOffload=" + enableDiskOffload +
                        ", enableNioSupport=" + enableNioSupport +
                        ", alwaysSetSurrogateControlHdr=" + alwaysSetSurrogateControlHdr +
                        ", externalGroups=" + externalGroups +
                        ", alwaysSynchronizeOnGets=" + alwaysSynchronizeOnGets +
                        ", alwaysTriggerCommandInvalidations=" + alwaysTriggerCommandInvalidations +
                        ", cacheInstanceStoreCookies=" + cacheInstanceStoreCookies +
                        ", cachePercentageWindow=" + cachePercentageWindow +
                        ", cachePriority=" + cachePriority +
                        ", cacheProviderName=" + cacheProviderName +
                        ", cascadeCachespecProperties=" + cascadeCachespecProperties +
                        ", commandCachePriority=" + commandCachePriority +
                        ", configReloadInterval=" + configReloadInterval +
                        ", disableDependencyId=" + disableDependencyId +
                        ", disableStoreCookies=" + disableStoreCookies +
                        ", disableTemplateInvalidation=" + disableTemplateInvalidation +
                        ", disableTemplatesSupport=" + disableTemplatesSupport +
                        ", diskCacheEntrySizeInMB=" + diskCacheEntrySizeInMB +
                        ", diskCacheEvictionPolicy=" + diskCacheEvictionPolicy +
                        ", diskCacheHighThreshold=" + diskCacheHighThreshold +
                        ", diskCacheLowThreshold=" + diskCacheLowThreshold +
                        ", diskCachePerformanceLevel=" + diskCachePerformanceLevel +
                        ", diskCacheSize=" + diskCacheSize +
                        ", diskCacheSizeInGB=" + diskCacheSizeInGB +
                        ", diskHashBuckets=" + diskHashBuckets +
                        ", diskOffloadLocation=" + diskOffloadLocation +
                        ", htodCleanupFrequency=" + htodCleanupFrequency +
                        ", htodCleanupHour=" + htodCleanupHour +
                        ", htodDataHashtableSize=" + htodDataHashtableSize +
                        ", htodDelayOffload=" + htodDelayOffload +
                        ", htodDelayOffloadDepIdBuckets=" + htodDelayOffloadDepIdBuckets +
                        ", htodDelayOffloadEntriesLimit=" + htodDelayOffloadEntriesLimit +
                        ", htodDelayOffloadTemplateBuckets=" + htodDelayOffloadTemplateBuckets +
                        ", htodDepIdHashtableSize=" + htodDepIdHashtableSize +
                        ", htodDependencyCacheIndexEnabled=" + htodDependencyCacheIndexEnabled +
                        ", htodInvalInterval=" + htodInvalInterval +
                        ", htodInvalidationBufferLife=" + htodInvalidationBufferLife +
                        ", htodInvalidationBufferSize=" + htodInvalidationBufferSize +
                        ", htodNumberOfPools=" + htodNumberOfPools +
                        ", htodPoolEntryLife=" + htodPoolEntryLife +
                        ", htodPoolSize=" + htodPoolSize +
                        ", htodTemplateHashtableSize=" + htodTemplateHashtableSize +
                        ", lruToDiskTriggerPercent=" + lruToDiskTriggerPercent +
                        ", lruToDiskTriggerTime=" + lruToDiskTriggerTime +
                        ", explicitBufferLimitOnStop=" + explicitBufferLimitOnStop +
                        ", drsBootstrapEnabled=" + drsBootstrapEnabled +
                        ", drsDisabled=" + drsDisabled +
                        ", dtdDir=" + dtdDir +
                        ", filterInactivityInvalidation=" + filterInactivityInvalidation +
                        ", filterLRUInvalidation=" + filterLRUInvalidation +
                        ", filterTimeOutInvalidation=" + filterTimeOutInvalidation +
                        ", filteredStatusCodes=" + filteredStatusCodes +
                        ", flushToDiskOnStop=" + flushToDiskOnStop +
                        ", ignoreCacheableCommandDeserializationException=" + ignoreCacheableCommandDeserializationException +
                        ", ignoreValueInInvalidationEvent=" + ignoreValueInInvalidationEvent +
                        ", jspCachePriority=" + jspCachePriority +
                        ", memoryCacheHighThreshold=" + memoryCacheHighThreshold +
                        ", memoryCacheLowThreshold=" + memoryCacheLowThreshold +
                        ", memoryCacheSizeInMB=" + memoryCacheSizeInMB +
                        ", refCountTracking=" + refCountTracking +
                        ", serverServerName=" + serverName +
                        ", statusCodesArray=" + Arrays.toString(statusCodes) +
                        ", tempDir=" + workareaDir +
                        ", batchUpdateInterval=" + batchUpdateInterval +
                        ", batchUpdateMilliseconds=" + batchUpdateMilliseconds +
                        ", timeGranularityInSeconds=" + timeGranularityInSeconds +
                        ", maxTimeLimitInSeconds=" + maxTimeLimitInSeconds +
                        ", timeHoldingInvalidations=" + timeHoldingInvalidations +

                        ", getClass()=" + getClass() + ", hashCode()=" + hashCode() +
                        ", toString()=" + super.toString() + "]";
    }
}
//@formatter:on