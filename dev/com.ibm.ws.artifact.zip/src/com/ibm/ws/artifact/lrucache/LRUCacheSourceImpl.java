package com.ibm.ws.artifact.lrucache;

import java.util.HashMap;
import java.util.Map;

public abstract class LRUCacheSourceImpl<K, V> implements LRUCacheSource<K, V> {

    public LRUCacheSourceImpl(int capacity) {
        this( new HashMap<K, V>(capacity) );
    }
    
    public LRUCacheSourceImpl(Map<K, V> storage) {
        this.storage = storage;
    }

    private final Map<K, V> storage;

    protected Map<K, V> storage() {
        return storage;
    }

    protected V put(K key, V value) {
        return storage().put(key, value);
    }

    protected void putAll(Map<K, V> values) {
        storage().putAll(values);
    }

    @Override
    public abstract long getSize(K key, V value) throws Exception;

    public boolean contains(K key) throws Exception {
        return storage().containsKey(key);
    }

    @Override
    public V acquire(K key) throws Exception {
        return storage().get(key);
    }

    @Override
    public void discard(K key, V value) throws Exception {
        // Ignore
    }
}
