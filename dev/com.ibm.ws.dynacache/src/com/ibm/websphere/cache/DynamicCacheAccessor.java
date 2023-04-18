/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
package com.ibm.websphere.cache;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.cache.DCacheBase;
import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.ffdc.FFDCFilter;

/**
 * This class provides applications with access to the Dynamic Cache,
 * allowing programmatic inspection and manipulation of WebSphere's
 * cache.
 *
 * @ibm-api
 */
//@formatter:off
public final class DynamicCacheAccessor {
    private static TraceComponent tc =
        Tr.register(DynamicCacheAccessor.class,
                    "WebSphere Dynamic Cache", "com.ibm.ws.cache.resources.dynacache");

    /**
     * Answer the default dynamic cache.
     *
     * @return The dynamic cache.  Null if caching is disabled
     *
     * @see #getDistributedMap()
     * @see DistributedMap
     *
     * @deprecated Use {@link DistributedMap} to store and manage objects in cache.
     *             {@link DynamicCacheAccessor#getDistributedMap}
     *             will return the distributed for accessing the default dynamic
     *             cache.
     * @ibm-api
     */
    @Deprecated
    public static com.ibm.websphere.cache.Cache getCache() {
        if ( isServletCachingEnabled() ) {
            return ServerCache.cache;
        } else {
            return null;
        }
    }

    /**
     * Tell if dynamic caching is enabled.  That is, if either servlet or object
     * caching is enabled.
     *
     * See {@link #isServletCachingEnabled} and {@link #isObjectCachingEnabled}.
     *
     * @return True or false telling if dynamic caching is enabled.
     *
     * @ibm-api
     */
    public static boolean isCachingEnabled() {
        return ( ServerCache.servletCacheEnabled || ServerCache.objectCacheEnabled );
    }

    /**
     * Tell if dynamic servlet caching is enable.
     *
     * @return True or false telling if dynamic servlet caching is enabled.
     *
     * @ibm-api
     */
     public static boolean isServletCachingEnabled() {
         return ServerCache.servletCacheEnabled;
     }

     /**
      * Tell if dynamic object caching is enable.
      *
      * @return True or false telling if dynamic servlet caching is enabled.
      *
      * @ibm-api
      */
      public static boolean isObjectCachingEnabled() {
          return ServerCache.objectCacheEnabled;
      }

    /**
     * Answer the distributed map for the default dynamic object cache.
     *
     * @return The distributed map for the default dynamic object cache.
     *     Null if object caching is disabled.
     *
     * @since v6.0
     * @ibm-api
     * @deprecated baseCache is used for servlet caching. It should not be used
     *             as a DistributedMap.
     */
    @Deprecated
    public static DistributedMap getDistributedMap() {
        String methodName="getDistributedMap";

        if ( !isObjectCachingEnabled() ) {
            Tr.error(tc, "DYNA1060W", new Object[] {DCacheBase.DEFAULT_BASE_JNDI_NAME});
            // DYNA1060E=DYNA1060E: WebSphere Dynamic Cache instance named {0}
            //                      cannot be used because of Dynamic Object cache
            //                      service has not be started.
            return null;
        }

        if ( tc.isEntryEnabled() ) {
            Tr.entry(tc, methodName);
        }

        DistributedMap distributedMap = null;
        Context context = null;
        try {
            context = new InitialContext();
            distributedMap  = (DistributedObjectCache)context.lookup(DCacheBase.DEFAULT_BASE_JNDI_NAME);
        } catch ( NamingException e ) {
            FFDCFilter.processException(e, "com.ibm.websphere.cache.DynamicCacheAccessor.getDistributedMap", "99",
                                           com.ibm.websphere.cache.DynamicCacheAccessor.class);
            // No need to do anything else, since FFDC prints stack traces
        } finally {
            try {
                if ( context != null ) {
                    context.close();
                }
            } catch ( NamingException e ) {
                FFDCFilter.processException(e, "com.ibm.websphere.cache.DynamicCacheAccessor.getDistributedMap", "110",
                                               com.ibm.websphere.cache.DynamicCacheAccessor.class);
            }
        }

        if ( tc.isEntryEnabled() ) {
            Tr.exit(tc, methodName, distributedMap);
        }
        return distributedMap;
    }
}
//@formatter:on