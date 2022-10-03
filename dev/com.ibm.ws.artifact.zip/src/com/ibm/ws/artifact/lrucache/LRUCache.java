/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.artifact.lrucache;

import java.io.PrintStream;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * LRU Cache API
 *
 * The cache stores the most recently retrieved values, up to
 * a limit set by the cache policy.
 * 
 * Cache operations are thread-safe.  Generally, any get must
 * update the cache state and must block concurrent activity.
 * 
 * A get of a previously stored value moves that value first in
 * the access list.
 * 
 * A get of an unstored value places that value in the cache
 * and sets the value as first in the access list.  Before storing
 * the value to the cache, values are removed from the cache until
 * sufficient space is available to store the new value.
 * 
 * Storage may be limited by count and by total size, with values
 * stored in the policy.
 * 
 * Values will not be stored if they are rejected by the cache policy
 * or if they are too large to be stored (also according to the policy).
 *
 * This API provides no means of removing values from the cache, or of
 * clearing the cache.  If the cache is to be cleared, a new, empty,
 * cache should be created.
 *
 * Cache statistics are available.  These may be retrieved or displayed
 * to a print stream.
 *
 * @param <K> The type of cache keys.
 * @param <V> The type of cache values.
 */
public interface LRUCache<K, V> {
    String description();
    void describe(PrintStream output);

    LRUCachePolicy<K, V> policy();
    LRUCacheSource<K, V> source();
    LRUCacheStats stats();

    V get(K key) throws Exception;
    boolean isStored(K key);    

    boolean isEmpty();
    int size();

    void forEach(BiConsumer<? super K, ? super V> action);
    Map<K, V> snapshot();
}
