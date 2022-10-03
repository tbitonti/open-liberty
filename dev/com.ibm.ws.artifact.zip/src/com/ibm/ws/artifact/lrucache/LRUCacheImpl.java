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
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.ibm.ws.artifact.lrucache.util.LockableImpl;

public class LRUCacheImpl<K, V> extends LockableImpl implements LRUCache<K, V> {
    public LRUCacheImpl(String description, LRUCachePolicy<K, V> policy, LRUCacheSource<K, V> source) {
        super();

        this.description = description;

        this.policy = policy;
        this.source = source;
        this.stats = new LRUCacheStats(policy);

        this.lruAnchor = new Cell<K, V>();
        this.storage = new HashMap<>();
    }

    private final String description;
    
    @Override    
    public String description() {
        return description;
    }

    @Override
    public void describe(PrintStream output) {
        stats().describe(output, description());
    }
    
    private final LRUCachePolicy<K, V> policy;

    @Override
    public LRUCachePolicy<K, V> policy() {
        return policy;
    }

    private final LRUCacheSource<K, V> source;
    
    public LRUCacheSource<K, V> source() { 
        return source;
    }

    private final LRUCacheStats stats;

    protected LRUCacheStats rawStats() {
        return stats;
    }

    /**
     * Answer the current cache statistics.
     * 
     * This answers a thread-safe copy of the statistics.  Writes
     * are blocked until the copy is completed.
     * 
     * @return The current cache statistics.
     */
    public LRUCacheStats stats() {
        return blockWrites( () -> new LRUCacheStats(stats) );
    }

    //

    private static class Cell<CK, CV> {
        private Cell<CK, CV> prev;
        private Cell<CK, CV> next;

        private CK key;
        private CV value;
        private long size;

        /**
         * Make a new anchor cell.  The cell points at itself
         * both directions.
         */
        protected Cell() {
            this.prev = this;
            this.next = this;
            
            this.key = null;
            this.value = null;
            this.size = -1L;
        }
        
        /**
         * Make a new cell, pointing the cell at the specified next and
         * previous cells.  The bounding cells must be updated in a separate step.
         * 
         * @param key The key of the new cell.
         * @param value The value of the new cell.
         * @param size The size of the value of the new cell.
         * 
         * @param prev The previous cell.
         * @param next The next cell.
         */
        protected Cell(CK key, CV value, long size, Cell<CK, CV> prev, Cell<CK, CV> next) {
            this.prev = prev;
            this.next = next;

            this.key = key;
            this.value = value;
            this.size = size;
        }        

        /**
         * Change the data of the cell.  The cell position
         * is not changed.
         *  
         * @param key The new key for the cell.
         * @param value The new value for the cell.
         * @param size The new size for the cell.
         */
        protected void update(CK key, CV value, long size) {
            this.key = key;
            this.value = value;
            this.size = size;
        }

        //

        /**
         * Detach this cell.  The initial next and previous cells
         * are updated.
         * 
         * An exception is thrown if this cell is the anchor, which
         * signifies that the list is empty.
         */
        protected void remove() {
            Cell<CK, CV> oldNext = next;
            if ( oldNext == this ) {
                throw new IndexOutOfBoundsException("Empty list");
            }

            // Point the bounding cells at each other.
            Cell<CK, CV> oldPrev = prev;
            oldNext.prev = oldPrev;
            oldPrev.next = oldNext;

            // Detach this cell from the bounding cells.
            this.next = null;
            this.prev = null;
            
            this.key = null;
            this.value = null;
            this.size = -1L;
        }

        /**
         * Either, create a new cell, or, update the the specified cell,
         * then put the cell after this cell.
         * 
         * Do nothing if the cell already follows this cell.
         * Otherwise, the next and previous of the cell are updated,
         * this cell and the next of this cell are updated, and
         * the cell which was moved is updated.
         *
         * @param newNext The cell to move to be next to
         *     this cell.
         * @param key The key of the new cell.
         * @param value The value of the new cell.
         * @param size The size of the new cell.
         * 
         * @return The cell that was made next.
         */
        protected Cell<CK, CV> makeNext(Cell<CK, CV> newCell, CK key, CV value, long size) {
            if ( newCell == null ) {
                return makeNext(key, value, size);

            } else {
                newCell.update(key, value, size);
                makeNext(newCell);
                return newCell;
            }
        }

        /**
         * Add a new cell after this cell.
         * 
         * @param key The key of the new cell.
         * @param value The value of the new cell.
         * @param size The size of the new cell.
         * 
         * @return The new cell.
         */
        protected Cell<CK, CV> makeNext(CK key, CV value, long size) {
            Cell<CK, CV> oldNext = next;
            
            // Point the new cell at the bounding cells.
            Cell<CK, CV> newNext = new Cell<CK, CV>(key, value, size, this, oldNext);

            // Point the bounding cells at the new cell.
            this.next = newNext;
            newNext.next = oldNext;

            return newNext;
        }

        /**
         * Shift a cell so that it is after this cell.
         * 
         * Do nothing if the cell already follows this cell.
         * Otherwise, the next and previous of the cell are updated,
         * this cell and the next of this cell are updated, and
         * the cell which was moved is updated.
         *
         * @param newNext The cell to move to be next to
         *     this cell.
         *     
         * @return The cell that was made next.
         */
        protected Cell<CK, CV> makeNext(Cell<CK, CV> newNext) {
            Cell<CK, CV> oldPrev = newNext.prev;
            if ( oldPrev == this.prev ) {
                return newNext; // Nothing to do
            }
            
            // Point bounding cells of the new next at each other.
            Cell<CK, CV> oldNext = newNext.next;
            oldPrev.next = oldNext;
            oldNext.prev = oldPrev;

            Cell<CK, CV> thisNext = this.next;
            
            // Point this cell and the next at the new cell.            
            thisNext.prev = newNext;
            this.next = newNext;
            
            // Point the new cell at this cell and the next.
            newNext.prev = this;
            newNext.next = thisNext;
            
            return newNext;
        }
    }

    private final Cell<K, V> lruAnchor;
    private final Map<K, Cell<K, V>> storage;

    /**
     * Answer the size of this cache.  That is, the
     * number of values currently stored in the cache.
     * 
     * Capacity values are avalable on the {@link #policy()}.
     *
     * @return The number of values stored in the cache. 
     */
    @Override
    public int size() {
        return blockWrites( () -> storage.size() );
    }

    /**
     * Tell if no values are stored in this cache.
     * 
     * @return True or false, telling if no values are stored
     *         in this cache.
     */
    @Override
    public boolean isEmpty() {
        return blockWrites( () -> storage.isEmpty() );
    }

    /**
     * Tell if a key is stored in this cache.  This does not
     * test if any value is available for the key, just whether
     * or not a value is currently stored in the cache.
     * 
     * @param key The key which is to be tested.
     *
     * @return True or false telling if a value is currently stored
     *     for the key in the cache.
     */
    @Override
    public boolean isStored(K key) {
        return blockWrites( () -> storage.containsKey(key) );
    }

    /**
     * Core cache API: Retrieve the value associated with the key.
     * 
     * If no value is currently associated with the key, attempt to
     * retrieve the value.  If no value can be retrieved, answer null.
     * The cache state is not updated.  Otherwise, update the cache
     * state and return the value.
     */
    @Override
    public V get(K key) throws Exception {
        LRUCacheStats useStats = rawStats();
        
        // First: Is a value cached for the key?
        // If the key is already cached, the key becomes the first
        // (most recently used) key.

        V valueHit = blockWrites( () -> { 
            Cell<K, V> hitCell = storage.get(key);
            if ( hitCell == null ) {
                return null;
            } else {
                lruAnchor.makeNext(hitCell);
                useStats.recordHit(hitCell.size);
                return hitCell.value;
            }
        });
        if ( valueHit != null ) {
            return valueHit;
        }

        // Second: Can a value be acquired for the key?
        // If no value is available for the key, answer null.
        // Failed lookups are not recorded: The cache state is
        // not updated.
        //
        // This step is done OUTSIDE of the write lock.
        // acquiring values is the slowest step, by far.
        //
        // The cost of doing acquire outside of the write lock
        // is possible concurrent calls to acquire the same key
        // (with the further consequence of having to check if the
        // a was stored by a duplicate check.)
        //
        // Concurrent gets on the same key are expected to be rare.

        LRUCacheSource<K, V> useSource = source();        
        V value = useSource.acquire(key);
        if ( value == null ) {
            return null;
        }

        // Third: Is the value either not of interest, or too big
        // to be stored.
        //
        // If the value is not of interest, or is too big, record
        // that the value was not stored, and return the value.

        return blockWrites( () -> {
            long valueSize = useSource.getSize(key, value);
            LRUCachePolicy<K, V> usePolicy = policy();
            if ( !usePolicy.select(key, value) ) {
                useStats.recordUnselected(valueSize);
                return value;
            }
            if ( valueSize > usePolicy.maxValueSize() ) {
                useStats.recordTooBig(valueSize);            
                return value;
            }

            Cell<K, V> cellCollision = storage.get(key);
            if ( cellCollision != null ) {
                useStats.recordHit(cellCollision.size);                
                return cellCollision.value;
            }

            Cell<K, V> lastRemoved = null;
            while ( useStats.mustRemove(valueSize) ) {
                lastRemoved = lruAnchor.prev;
                K removedKey = lastRemoved.key;
                V removedValue = lastRemoved.value;
                long removedSize = lastRemoved.size;
                lastRemoved.remove();
                storage.remove(removedKey);
                useSource.discard(removedKey, removedValue);
                useStats.recordRemove(removedSize);
            }

            Cell<K, V> addedCell =
                lruAnchor.makeNext(lastRemoved, key, value, valueSize);
            storage.put(key, addedCell);
            useStats.recordMiss(valueSize);

            return value;            
        });
    }

    // These are protected.
    
    /**
     * Iterate across the keys and values of this cache, applying the action
     * to each key and value pair.
     * 
     * Writes are blocked during the iteration.  Consider using {@link #snapshot()}
     * as an alternative.
     * 
     * @param action A consumer of key and value pairs.
     */
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        blockWrites( () -> storage.forEach( (k, c) -> action.accept(k, c.value)) );
    }
    
    /**
     * Obtain a table of the keys and values of this cache.
     *
     * Writes are blocked while building the table.
     *
     * This may be preferred to {@link #forEach(BiConsumer)}, since writes
     * are blocked for less time.
     *
     * @return A table of the keys and values of this cache.
     */
    @Override
    public Map<K, V> snapshot() {
        return blockWrites( () -> {
            Map<K, V> snapshot = new HashMap<K, V>( size() );
            forEach( (k, v) -> snapshot.put(k, v) );
            return snapshot;
        });
    }
}
