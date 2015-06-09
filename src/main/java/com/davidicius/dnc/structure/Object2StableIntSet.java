package com.davidicius.dnc.structure;

/**
 * User: David Carbol
 * Date: 4/19/15
 * Time: 6:48 PM
 */
public interface Object2StableIntSet<V> {

    public static final int NULL_OBJECT_STABLE_INDEX = 0;

    /**
     * Lookup by key (by value)
     * @link Object#equals(Object)} must be properly defined.
     * @param key Null keys are supported (0 is returned)
     * @return -1 in case the key is not found. Otherwise stable index is returned.
     */
    public int contains(V key);

    /**
     * Add key and return its stable index. If a key already exists, stable index of existing object is returned.
     * {@link Object#equals(Object)} must be properly defined.
     * @param key Null keys are  supported (0 is returned)
     * @return Zero based stable index of added key or an index of existing object with the equal value
     */
    public int add(V key);

    /**
     * Get object by stable index.
     * @param stableIndex Stable index that represents a key. 0 represents NULL value.
     * @return Object identified by the stable index, null if no object associated with the index exists.
     */
    public V get(int stableIndex);

    /**
     * Remove key  (by value).
     * {@link Object#equals(Object)} must be properly defined.
     * @param key Null keys are supported (0 is returned)
     * @return Stable index of removed key. -1 if the key does not exist.
     */
    public int remove(V key);

    /**
     * Remove all keys.
     */
    public void clear();

    /**
     * Amount of keys stored in the structure.
     * * @return Number of keys
     */
    public int size();

    /**
     * Amount of keys stored in the structure.
     * Time complexity: O(1) (super fast)
     * * @return TRUE if there is no key stored in the structure
     */
    public boolean isEmpty();

    /**
     * Returns current amount of bytes  occupied by this structure.
     * @return Bytes currently used by the structure
     */
    long getSizeInBytes();

    /**
     * Returns amount of bytes needed to store provided number of key.
     * @param size Number of keys to be stored
     * @return Bytes needed to store 'size' keys
     */
    long estimateSizeInBytes(int size);
}
