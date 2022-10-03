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

import java.util.function.BiPredicate;

/**
 * LRU cache policy.
 * 
 * Policy settings are:
 * 
 * <ul>
 * <li>A <strong>selector</strong>, which selects which key
 *     values are to be cached.</li>
 * <li>A <strong>maximum value size</strong> value, which
 *     limits the sizes of values which will cached.</li>
 * <li>A <strong>maximum value count</strong> value, which
 *     limits how many values will be cached.</li>
 * <li>A <string>maximum total size</strong> value, which
 *     limits the total size of values which will be cached.</li>
 * </ul>
 * 
 * The <strong>selector</strong> and <strong>maximum value size</strong>
 * determine what values will be stored in the cache.
 * 
 * The <strong>maximum value count</strong> and <strong>maximum total size</strong>
 * values determine how many values will be stored before values will start to
 * be removed from the cache. 
 *
 * @param <K> The type of cache keys.
 * @param <V> The type of cache values.
 */
public class LRUCachePolicy<K, V> {
    public LRUCachePolicy(BiPredicate<K, V> selector,
                          int maxValueSize,
                          int maxValueCount,
                          int maxTotalSize) {

        this.selector = selector;

        this.maxValueSize = maxValueSize;
        this.maxValueCount = maxValueCount;
        this.maxTotalSize = maxTotalSize;
    }

    private final BiPredicate<K, V> selector;
    
    public BiPredicate<K, V> selector() {
        return selector;
    }
    
    public boolean select(K key, V value) {
        return selector().test(key, value);
    }
    
    private final int maxValueSize;

    public int maxValueSize() {
        return maxValueSize;
    }

    private final int maxValueCount; 

    public int maxValueCount() {
        return maxValueCount;
    }

    private final int maxTotalSize;
    
    public int maxTotalSize() {
        return maxTotalSize;
    }
}
