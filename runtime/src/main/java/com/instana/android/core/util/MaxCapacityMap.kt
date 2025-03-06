/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import java.util.concurrent.locks.ReentrantReadWriteLock

@Suppress("unused")
class MaxCapacityMap<K, V>(
    @Suppress("MemberVisibilityCanBePrivate") val maxCapacity: Int
) {
    private var map = mutableMapOf<K, V>()
    private val lock = ReentrantReadWriteLock()  // Lock for concurrent read/write

    /**
     * Updates this map with the specified key/value pair, as long as the maxCapacity is not surpassed.
     *
     * @return true when the pair is added, false otherwise
     */
    fun put(key: K, value: V): Boolean {
        lock.writeLock().lock()
        try {
            return if (map.size < maxCapacity) {
                map[key] = value
                true
            } else {
                Logger.e("Tried to add an element to a full map")
                false
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Updates this map with key/value pairs from the specified map, as long as the maxCapacity is not surpassed.
     *
     * Either all pairs will be added or none will be.
     *
     * @return true when all pairs are added, false otherwise
     */
    fun putAll(from: Map<K, V>): Boolean {
        lock.writeLock().lock()
        try {
            val buffer = mutableMapOf<K, V>().apply {
                putAll(map)  // Existing entries in map
                putAll(from)  // New entries to be added
            }
            return if (buffer.size <= maxCapacity) {
                map.putAll(from)  // Apply changes to map
                true
            } else {
                Logger.e("Tried to add elements to a full map")
                false
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Get one element of the map.
     */
    fun get(key: K): V? {
        lock.readLock().lock()
        try {
            return map[key]
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Get a read-only <b>copy</b> of the map.
     */
    fun getAll(): Map<K, V> {
        lock.readLock().lock()
        try {
            return map.toMap()  // Return a copy of the map
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Removes the specified key and its corresponding value from this map.
     * @return the previous value associated with the key, or null if the key was not present in the map.
     */
    fun remove(key: K): V? {
        lock.writeLock().lock()
        try {
            return map.remove(key)
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Removes all elements from this map.
     */
    fun clear() {
        lock.writeLock().lock()
        try {
            map.clear()
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Creates a copy of the map, with the same content and maxCapacity
     */
    fun clone(): MaxCapacityMap<K, V> {
        lock.readLock().lock()  // Acquire read lock to ensure consistency
        try {
            val newMap = MaxCapacityMap<K, V>(maxCapacity)
            newMap.putAll(map)
            return newMap
        } finally {
            lock.readLock().unlock()  // Release read lock
        }
    }
}
