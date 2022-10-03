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

/**
 * A source of values for a LRU cache.
 * 
 * Invoking {@link LRUCache#get} on a key which is not stored in
 * the cache causes an invocation of {@link #acquire}.
 * 
 * The cache policy may limit what values are stored in the cache
 * based on the size of a value.  Value sizes are obtained by
 * {@link #getSize}.
 * 
 * Eviction of a value from the cache causes an invocation of
 * {@link #discard}.  (This often does nothing.)
 *
 * @param <K> The type of cache keys.
 * @param <V> The type of cache values.
 */
public interface LRUCacheSource<K, V> {
    long getSize(K key, V value) throws Exception;

    boolean contains(K key) throws Exception;
    V acquire(K key) throws Exception;
    void discard(K key, V value) throws Exception;
}

