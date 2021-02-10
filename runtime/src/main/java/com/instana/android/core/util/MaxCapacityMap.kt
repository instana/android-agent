/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

@Suppress("unused")
class MaxCapacityMap<K, V>(
    @Suppress("MemberVisibilityCanBePrivate") val maxCapacity: Int
) {
    private var map = mutableMapOf<K, V>()

    /**
     * Updates this map with the specified key/value pair , as long as the maxCapacity is not surpassed.
     *
     * @return true when the pair is added, false otherwise
     */
    fun put(key: K, value: V): Boolean {
        return if (map.size < maxCapacity) {
            map[key] = value
            true
        } else {
            Logger.e("Tried to add an element to a full map")
            false
        }
    }

    /**
     * Updates this map with key/value pairs from the specified map from, as long as the maxCapacity is not surpassed.
     *
     * Either all pairs will be added or none will be.
     *
     * @return true when all pairs are added, false otherwise
     */
    fun putAll(from: Map<K, V>): Boolean {
        val buffer = mutableMapOf<K, V>().apply {
            putAll(map)
            putAll(from)
        }
        return if (buffer.size <= maxCapacity) {
            map = buffer
            true
        } else {
            Logger.e("Tried to add elements to a full map")
            false
        }
    }

    /**
     * Get one element of the map.
     */
    fun get(key: K): V? {
        return map[key]
    }

    /**
     * Get a read-only <b>copy</b> of the map.
     */
    fun getAll(): Map<K, V> {
        return map.toMap()
    }

    /**
     * Removes the specified key and its corresponding value from this map.
     * @return the previous value associated with the key, or null if the key was not present in the map.
     */
    fun remove(key: K): V? {
        return map.remove(key)
    }

    /**
     * Removes all elements from this map.
     */
    fun clear() {
        map.clear()
    }

    /**
     * Creates a copy of the map, with the same content and maxCapacity
     */
    fun clone(): MaxCapacityMap<K, V> {
        val newMap = MaxCapacityMap<K, V>(maxCapacity)
        newMap.putAll(map)
        return newMap
    }
}
