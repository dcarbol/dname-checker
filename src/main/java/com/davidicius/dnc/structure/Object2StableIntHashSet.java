package com.davidicius.dnc.structure;

import com.davidicius.dnc.structure.Object2StableIntSet;

import java.io.Serializable;
import java.util.Arrays;

/**
 * User: David Carbol
 * Date: 4/19/15
 * Time: 7:38 PM
 */
public class Object2StableIntHashSet<V> implements Object2StableIntSet<V>, Serializable {
    private int capacity;
    private int initialCapacity;
    private int size;
    private int nextFreeStableIndex;
    private int threshold;
    private V[] index2String;
    private int[] index2StableIndex;
    private int[] stableIndex2Index;

    private float loadFactor;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAX_CAPACITY = 1 << 30;

    /**
     * Default structure is created (capacity 16, load factor 0.75)
     */
    public Object2StableIntHashSet() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Structure with requested capacity is created.
     * Time complexity: O(N), N ∞ initialCapacity
     *
     * @param initialCapacity Initial capacity (must not be below 0). Maximal capacity is 1 073 741 824
     */
    public Object2StableIntHashSet(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Created empty structure.
     * Time complexity: O(N), N ∞ initialCapacity
     *
     * @param initialCapacity Initial capacity (must not be below 0). Maximal capacity is 1 073 741 824
     * @param loadFactor      Initial load factor, default is 0.75. Must be in interval <0, 1>
     */
    public Object2StableIntHashSet(int initialCapacity, float loadFactor) {
        if (loadFactor <= 0 || loadFactor > 1 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Load factor must be between 0 and 1 (including): " + loadFactor);
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);

        this.loadFactor = loadFactor;
        this.initialCapacity = nearestPower(initialCapacity);

        init(this.initialCapacity);
    }

    /**
     * Lookup by key (by value)
     * Time complexity: O(1).
     * Note: Hash function for key is always called once. Also equals on keys is expected to be super fast.
     * Also equals on keys is expected to be super fast.
     * {@link Object#hashCode()} and {@link Object#equals(Object)} must be properly defined.
     *
     * @param key Null keys are supported. Stable index is always 0;
     * @return -1 in case the key is not found. Otherwise stable index is returned.
     */
    @Override
    public int contains(V key) {
        if (key == null) {
            return 0;
        }

        int h = narrowToCapacity(improveHash(key.hashCode()), capacity);
        int i = h;
        do {
            V value = index2String[i];
            if (value == null) {
                return -1;
            }

            if (value.equals(key)) {
                return index2StableIndex[i] + 1;
            }

            i++;
            i = narrowToCapacity(i, capacity);
        } while (i != h);

        return -1;
    }

    /**
     * Add key and return its stable index. If a key already exists, stable index of existing object is returned.
     * Time complexity: O(1). Resize of arrays happens log(N) times, amortized complexity log(N)/N -> 0
     * Note: Hash function for key is always called once (or twice for array resize).
     * Also equals on keys is expected to be super fast. Order of keys as added to the structure is preserved.
     * {@link Object#hashCode()} and {@link Object#equals(Object)} must be properly defined.
     *
     * @param key Null keys are supported
     * @return Zero based stable index of added key or an index of existing object with the equal value.
     *         -1 returned in case it was not possible to add a key (memory issue)
     */
    @Override
    public int add(V key) {
        if (key == null) {
            return 0;
        }

        int h = narrowToCapacity(improveHash(key.hashCode()), capacity);
        int i = h;
        do {
            V value = index2String[i];
            if (value == null) {
                break;
            }

            if (value.equals(key)) {
                return index2StableIndex[i] + 1;
            }

            i++;
            i = narrowToCapacity(i, capacity);
        } while (i != h);

        if (size == threshold) {
            if (resize()) {
                return add(key);
            } else {
                return -1;
            }
        }

        index2String[i] = key;
        int newStableIndex;
        newStableIndex = nextFreeStableIndex++;

        stableIndex2Index[newStableIndex] = i;
        index2StableIndex[i] = newStableIndex;

        size++;
        return newStableIndex + 1;
    }

    /**
     * Get object by stable index.
     * Time complexity: O(1) (super fast, 2x array lookup)
     *
     * @param stableIndex Stable index that represents a key. Index 0 represents NULL value.
     * @return Object identified by the stable index, null if no object associated with the index exists.
     */
    @Override
    public V get(int stableIndex) {
        if (stableIndex <= 0 || stableIndex >= stableIndex2Index.length) {
            return null;
        }

        int index = stableIndex2Index[stableIndex - 1];
        if (index != -1) {
            return index2String[index];
        } else {
            return null;
        }
    }

    /**
     * Remove key  (by value).
     * Time complexity: O(1)
     * Note: Hash function for key is always called once (or twice for array resize).
     * Also equals on keys is expected to be super fast.
     * During remove chains of keys can be moved (complexity O(1))
     * Order of keys as added to the structure is preserved even after a key removal.
     * {@link Object#hashCode()} and {@link Object#equals(Object)} must be properly defined.
     *
     * @param key Null keys are  supported.
     * @return Stable index of removed key. -1 if the key does not exist.
     */
    @Override
    public int remove(V key) {
        int stableIndex = contains(key);
        if (stableIndex == 0) {
            return 0;
        }

        if (stableIndex == -1) {
            return -1;
        }

        int rawStableIndex = stableIndex - 1;
        int index = stableIndex2Index[rawStableIndex];
        stableIndex2Index[rawStableIndex] = -1;
        size--;

        int gap = index;
        int current = narrowToCapacity(index + 1, capacity);
        while (true) {
            V ss = index2String[current];
            while (ss != null) {
                int desiredIndex = narrowToCapacity(improveHash(ss.hashCode()), capacity);
                if (current >= gap ?
                        desiredIndex <= gap || desiredIndex > current :
                        desiredIndex <= gap && desiredIndex > current) break;
                current = narrowToCapacity(current + 1, capacity);
                ss = index2String[current];
            }

            if (ss == null) break;

            index2String[gap] = ss;
            int si = index2StableIndex[current];
            stableIndex2Index[si] = gap;
            index2StableIndex[gap] = si;

            gap = current;
            current = narrowToCapacity(current + 1, capacity);
        }

        index2String[gap] = null;
        return stableIndex;
    }

    /**
     * Remove all keys.
     * Time complexity: O(N), N ∞ initialCapacity
     */
    @Override
    public void clear() {
        init(initialCapacity);
    }

    /**
     * Amount of keys stored in the structure.
     * Time complexity: O(1) (super fast)
     *
     * @return Number of keys
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     * Amount of keys stored in the structure.
     * Time complexity: O(1) (super fast)
     * * @return TRUE if there is no key stored in the structure
     */
    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    private static final long THIS_SIZE =
            8 +         // this instance
                    4 * 4 +       // capacity, initial capacity, size, threshold
                    3 * (12 + 4) +  // arrays index2Object, index2StableIndex, stableIndex2Index
                    4;          // loadFactor

    /**
     * Returns current amount of bytes  occupied by this structure.
     * Time complexity: O(1) (super fast)
     *
     * @return Bytes currently used by the structure
     */
    @Override
    public long getSizeInBytes() {
        return THIS_SIZE + ((long) index2String.length + (long) index2StableIndex.length + (long) stableIndex2Index.length) * 4;
    }

    /**
     * Returns amount of bytes needed to store provided number of key.
     * Time complexity: O(1) (super fast)
     *
     * @param size Number of keys to be stored
     * @return Bytes needed to store 'size' keys
     */
    @Override
    public long estimateSizeInBytes(int size) {
        if (size < 0) throw new IllegalArgumentException("Illegal size: " + size);

        long capacity = nearestPower(size);
        return THIS_SIZE + (capacity + capacity + capacity) * 4;
    }

    /**
     * Current capacity of hash structures.
     * Time complexity: O(1) (super fast)
     *
     * @return Current capacity of hash structures
     */
    public int capacity() {
        return this.capacity;
    }

    /**
     * Load factor of used hash structures.
     * Time complexity: O(1) (super fast)
     *
     * @return Load factor of hash structures.
     */
    public float loadFactor() {
        return this.loadFactor;
    }

    @SuppressWarnings("unchecked")
    private void init(int capacity) {
        index2String = (V[]) new Object[capacity];
        index2StableIndex = new int[capacity];
        stableIndex2Index = new int[capacity];
        Arrays.fill(stableIndex2Index, -1);

        this.capacity = capacity;
        this.threshold = (int) (capacity * loadFactor);
        this.size = 0;
        this.nextFreeStableIndex = 0;
    }

    private boolean resize() {
        if (threshold != size) throw new IllegalStateException();

        int newCapacity = capacity;
        int newThreshold;
        do {
            newCapacity <<= 1;
            newThreshold = (int) (newCapacity * loadFactor);
        } while (size == newThreshold);

        if (newCapacity == MAX_CAPACITY) {
            return false;
        }

        internalResize(newCapacity);
        threshold = newThreshold;
        capacity = newCapacity;

        return true;
    }

    @SuppressWarnings("unchecked")
    private void internalResize(int newCapacity) {
        V[] newIndex2String = (V[]) new Object[newCapacity];
        int[] newIndex2StableIndex = new int[newCapacity];
        int[] newStableIndex2Index = new int[newCapacity];
        Arrays.fill(newStableIndex2Index, capacity, newCapacity, -1);

        for (int stableIndex = 0; stableIndex < size; ++stableIndex) {
            int index = stableIndex2Index[stableIndex];
            if (index == -1) {
                newStableIndex2Index[stableIndex] = -1;
                continue;
            }

            V s = index2String[index];
            int h = narrowToCapacity(improveHash(s.hashCode()), newCapacity);
            int i = h;
            do {
                V value = newIndex2String[i];
                if (value == null) {
                    break;
                }

                if (value.equals(s)) {
                    throw new IllegalStateException();
                }

                i++;
                i = narrowToCapacity(i, newCapacity);

                if (i == h) {
                    throw new IllegalStateException();
                }
            } while (true);

            newIndex2String[i] = s;
            newStableIndex2Index[stableIndex] = i;
            newIndex2StableIndex[i] = stableIndex;
        }

        index2String = newIndex2String;
        index2StableIndex = newIndex2StableIndex;
        stableIndex2Index = newStableIndex2Index;
    }

    /**
     * Nearest greater power of 2.
     * 0 -> 1
     * 1 -> 1
     * 2 -> 2
     * 3 -> 4
     * 4 -> 4
     * 5 -> 8
     * 6 -> 8
     * ...
     */
    private static int nearestPower(int number) {
        return number == 0 ? 1 : (1 << (32 - Integer.numberOfLeadingZeros(number - 1)));
    }

    private static int narrowToCapacity(int i, int requestedCapacity) {
        return i & (requestedCapacity - 1);
    }

    private static int improveHash(int h) {
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Two structures are considered equal if all stable indexes map to the equal objects.
     * @param o Object to compare
     * @return True of the structures are equal
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Object2StableIntHashSet)) return false;

        Object2StableIntHashSet<?> s = (Object2StableIntHashSet<?>) o;
        if (this.size != s.size()) {
            return false;
        }

        if (this.stableIndex2Index.length != s.stableIndex2Index.length) {
            return false;
        }

        for (int i = 0; i < stableIndex2Index.length; ++i) {
            int a = this.stableIndex2Index[i];
            int b = s.stableIndex2Index[i];

            V as = null;
            V bs = null;

            if (a != -1) {
                as = index2String[a];
            }

            if (b != -1) {
                bs = index2String[b];
            }

            if (as == null) {
                if (bs != null) {
                    return false;
                }
            } else {
                if (!as.equals(bs)) return false;
            }
        }

        return true;
    }


    /**
     * Hash code, semantics the same as for equal
     * @return Hashcode
     */
    @Override
    public int hashCode() {
        int h = 0;
        for (int stableIndex = 0; stableIndex < stableIndex2Index.length; ++stableIndex) {
            int index = stableIndex2Index[stableIndex];
            h += internalHash(h) + stableIndex;
            h += internalHash(h) + index;

            if (index != -1) {
                h += internalHash(h) + index2String[index].hashCode();
            }
        }

        return h;
    }

    private static int internalHash(int h) {
        h = 31 * h;
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
}
