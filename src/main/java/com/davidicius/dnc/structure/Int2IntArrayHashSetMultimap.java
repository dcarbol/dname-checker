package com.davidicius.dnc.structure;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class Int2IntArrayHashSetMultimap implements Int2IntSetMultimap, Serializable {

    /**
     * Internal helper class for multimap. Implementation is somewhat similar but with fixed number of values.
     */
    protected static class Key2HeadMap {

        private static int hash(int key) {
            int h = key;
            h ^= (h >>> 20) ^ (h >>> 12);
            return h ^ (h >>> 7) ^ (h >>> 4);
        }
        private int mask;
        protected int[] keys;
        private int[] headIndices; // index to first element of the collection
        private int[] sizes; // sizes[x] == 0 means the slot is unused

        private int size;
        private float loadFactor;
        private int threshold;

        protected Key2HeadMap(int expected, float loadFactor) {
            int desiredCapacity = (int) Math.ceil(expected / loadFactor);
            if (desiredCapacity > MAX_CAPACITY) desiredCapacity = MAX_CAPACITY;
            int capacity = 1;
            while (capacity < desiredCapacity) capacity <<= 1;
            keys = new int[capacity];
            mask = keys.length - 1;
            headIndices = new int[capacity];
            sizes = new int[capacity];
            this.loadFactor = loadFactor;
            threshold = (int) (capacity * loadFactor);
        }
        private int indexFor(int hash) {
            return hash & mask;
        }
        private int nextIndexFor(int index) {
            return index + 1 & mask;
        }
        protected int getIndexFor(int key) {
            int index = indexFor(hash(key));
            while (sizes[index] != 0) {
                if (key == keys[index]) return index;
                index = nextIndexFor(index);
            }
            return ~index;
        }

        /**
         *
         * @param key
         * @return index to map's arrays holding info about head for the given key. Negative means no such key existed but new free index has been found.
         * When negative, correct new index can be obtained by applying ~ on return value.
         */
        private int getOrPut(int key) {
            int index = indexFor(hash(key));
            while (sizes[index] != 0) {
                // Existing index
                if (key == keys[index]) return index;
                index = nextIndexFor(index);
            }

            // New index
            if (size == threshold) {
                int newCapacity = keys.length << 1;
                threshold = (int) (newCapacity * loadFactor);

                int[] oldKeys = keys;
                int[] oldHeadIndices = headIndices;
                int[] oldSizes = sizes;

                keys = new int[newCapacity];
                mask = keys.length - 1;
                headIndices = new int[newCapacity];
                sizes = new int[newCapacity];

                beginGrow(newCapacity);

                for (int oldIndex = 0; oldIndex < oldKeys.length; oldIndex++) {
                    if (oldSizes[oldIndex] == 0) continue;
                    int newIndex = indexFor(hash(oldKeys[oldIndex]));

                    while (sizes[newIndex] != 0) newIndex = nextIndexFor(newIndex);
                    keys[newIndex] = oldKeys[oldIndex];
                    headIndices[newIndex] = oldHeadIndices[oldIndex];
                    sizes[newIndex] = oldSizes[oldIndex];
                    moveIndex(oldIndex, newIndex);
                }

                endGrow();

                // Recompute index
                index = indexFor(hash(key));
                while (sizes[index] != 0) index = nextIndexFor(index);
            }
            return ~index;
        }

        // Notifications for children
        protected void beginGrow(int newCapacity) {}
        protected void moveIndex(int oldIndex, int newIndex) {}
        protected void endGrow() {}
    }

    private static final int DEFAULT_EXPECTED = 4;
    private static final int MAX_CAPACITY = 1 << 30; // = max power of 2
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int STATIC_SIZE_IN_BYTES =
                            8 // This instance
                            + 4 * 4 // mask, size, loadFactor, threshold
                            + 6 * 4 + 6 * 12 // keys, values, hashes, used, next, previous
                            // key2Head
                            + 4 + 8 // key2Head reference + instance
                            + 4 * 4 // mask, size, loadFactor, threshold
                            + 3 * 4 + 3 * 12; // keys, headIndices, sizes
    private static final int ENTRY_SIZE_IN_BYTES = 6 * 4; // keys, values, hashes, used, next, previous
    private static final int KEY2HEAD_ENTRY_SIZE_IN_BYTES = 3 * 4; // keys, headIndices, sizes

    private static int hash(int key, int value) {
        int h = key;
        h = 31 * h + value;

        // Taken from JDK7 HashMap, check if useful / optimize... http://stackoverflow.com/questions/9335169/understanding-strange-java-hash-function
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    protected Key2HeadMap key2Head;

    private int mask; // always keys.length - 1 (used for fast modulo)

    private int[] keys;
    private int[] values;
    private int[] hashes;
    private boolean[] used;
    private int[] next;
    private int[] previous; // This is needed for fast remove()

    private int size;
    private float loadFactor;
    private int threshold;

    public Int2IntArrayHashSetMultimap() {
        this(DEFAULT_EXPECTED);
    }

    public Int2IntArrayHashSetMultimap(int expectedNumberOfKeys) {
        this(expectedNumberOfKeys, expectedNumberOfKeys);
    }

    public Int2IntArrayHashSetMultimap(int expectedNumberOfKeys, int expectedNumberOfValues) {
        this(expectedNumberOfKeys, expectedNumberOfValues, DEFAULT_LOAD_FACTOR);
    }

    public Int2IntArrayHashSetMultimap(int expectedNumberOfKeys, int expectedNumberOfValues, float loadFactor) {
        if (loadFactor <= 0 || loadFactor >= 1 || Float.isNaN(loadFactor)) throw new IllegalArgumentException("Load factor must be between 0 and 1 (excluding): " + loadFactor);
        if (expectedNumberOfKeys < 0) throw new IllegalArgumentException("Illegal expectedNumberOfKeys: " + expectedNumberOfKeys);
        if (expectedNumberOfValues < 0) throw new IllegalArgumentException("Illegal expectedNumberOfValues: " + expectedNumberOfValues);

        int desiredCapacity = (int) Math.ceil(expectedNumberOfValues / loadFactor);
        if (desiredCapacity > MAX_CAPACITY) desiredCapacity = MAX_CAPACITY;

        // Find a power of 2 >= desiredCapacity
        int capacity = 1;
        while (capacity < desiredCapacity) capacity <<= 1;

        key2Head = createKey2HeadMap(expectedNumberOfKeys, loadFactor);
        keys = new int[capacity];
        mask = keys.length - 1;
        values = new int[capacity];
        hashes = new int[capacity];
        next = new int[capacity];
        previous = new int[capacity]; // This is needed for fast remove()
        used = new boolean[capacity];
        this.loadFactor = loadFactor;
        threshold = (int) (capacity * loadFactor);
    }

    protected Key2HeadMap createKey2HeadMap(int expectedNumberOfKeys, float loadFactor) {
        return new Key2HeadMap(expectedNumberOfKeys, loadFactor);
    }

    @Override
    public boolean put(int key, int value) {
        if (size == threshold) {
            // Grow
            int newCapacity = keys.length << 1;
            threshold = (int) (newCapacity * loadFactor);

            int[] oldKeys = keys;
            int[] oldValues = values;
            int[] oldHashes = hashes;
            int[] oldNext = next;

            keys = new int[newCapacity];
            mask = keys.length - 1;
            values = new int[newCapacity];
            hashes = new int[newCapacity];
            used = new boolean[newCapacity];
            next = new int[newCapacity];
            previous = new int[newCapacity];

            for (int i = 0; i < key2Head.headIndices.length; i++) {
                if (key2Head.sizes[i] == 0) continue;

                // Head index
                int oldHeadIndex = key2Head.headIndices[i];
                int headIndex = indexFor(oldHashes[oldHeadIndex]);
                while (used[headIndex]) headIndex = nextIndexFor(headIndex);
                keys[headIndex] = oldKeys[oldHeadIndex];
                values[headIndex] = oldValues[oldHeadIndex];
                hashes[headIndex] = oldHashes[oldHeadIndex];
                used[headIndex] = true;
                previous[headIndex] = headIndex;
                next[headIndex] = headIndex;
                key2Head.headIndices[i] = headIndex;

                // Others
                int oldIndex = oldNext[oldHeadIndex];
                while (oldIndex != oldHeadIndex) {
                    int index = indexFor(oldHashes[oldIndex]);
                    // Find free index
                    while (used[index]) index = nextIndexFor(index);
                    // Free index found, move
                    keys[index] = oldKeys[oldIndex];
                    values[index] = oldValues[oldIndex];
                    hashes[index] = oldHashes[oldIndex];
                    used[index] = true;
                    previous[index] = previous[headIndex];
                    next[index] = headIndex;
                    next[previous[index]] = index;
                    previous[headIndex] = index;
                    oldIndex = oldNext[oldIndex];
                }
            }
        }

        int hash = hash(key, value);
        int index = indexFor(hash);

        // Check for existing entry or find free index
        while (used[index]) {
            // Does it already contain the same value we want to put in?
            if (hash == hashes[index] && key == keys[index] && value == values[index]) {
                return false;
            }
            index = nextIndexFor(index);
        }

        // Free index found, create new entry
        keys[index] = key;
        values[index] = value;
        hashes[index] = hash;
        used[index] = true;

        int key2HeadIndex = key2Head.getOrPut(key);
        if (key2HeadIndex < 0) {
            // This is 1st element in the collection, establish circle for iterating and set as head.
            key2HeadIndex = ~key2HeadIndex;
            previous[index] = index;
            next[index] = index;

            key2Head.keys[key2HeadIndex] = key;
            key2Head.headIndices[key2HeadIndex] = index;
            key2Head.sizes[key2HeadIndex] = 1;
            key2Head.size++;
        } else {
            // Collection already exists, maintain circle for iterating.
            int headIndex = key2Head.headIndices[key2HeadIndex];
            previous[index] = previous[headIndex];
            next[index] = headIndex;
            next[previous[index]] = index;
            previous[headIndex] = index;
            key2Head.sizes[key2HeadIndex]++;
        }
        size++;
        key2HeadUpdated(key2HeadIndex);
        return true;
    }

    // Notifications for children
    protected void key2HeadUpdated(int key2HeadIndex) {}

    private int indexFor(int hash) {
        return hash & mask;
    }
    private int nextIndexFor(int index) {
        return index + 1 & mask;
    }
    /**
     *
     * @param key
     * @return index to map's arrays holding info about head for the given key. Negative means no such key existed but new free index has been found.
     * When negative, correct new index can be obtained by applying ~ on return value.
     */
    private int getIndexFor(int key, int value) {
        int hash = hash(key, value);
        int index = indexFor(hash);
        while (used[index]) {
            if (hash == hashes[index] && key == keys[index] && value == values[index]) {
                return index;
            }
            index = nextIndexFor(index);
        }
        return ~index;
    }

    /**
     *
     * @param key
     * @return set of values. The set's implementation is not copy of map's data, rather it works directly on top of them.
     * Because of that, if the map or the set is modified during iteration, the behaviour is unspecified.
     */
    @Override
    public IntSet get(final int key) {
        final Int2IntArrayHashSetMultimap map = this;
        return new IntSet() {
            @Override
            public int size() {
                int key2HeadIndex = key2Head.getOrPut(key);
                return key2HeadIndex < 0 ? 0: key2Head.sizes[key2HeadIndex];
            }
            @Override
            public boolean isEmpty() {
                return size() == 0;
            }
            @Override
            public boolean contains(int value) {
                return map.contains(key, value);
            }
            @Override
            public boolean contains(Object value) {
                return contains(((Integer) value).intValue());
            }
            @Deprecated
            @Override
            public IntIterator intIterator() {
                return iterator();
            }
            @Override
            public IntIterator iterator() {
                return new IntIterator() {
                    private int key2HeadIndex = key2Head.getOrPut(key);
                    private int current = key2HeadIndex < 0 ? -1 : key2Head.headIndices[key2HeadIndex];
                    private int toRemove = -1;

                    @Override
                    public boolean hasNext() {
                        return current != -1;
                    }

                    @Override
                    public int nextInt() {
                        if (!hasNext()) throw new NoSuchElementException("No more elements.");
                        int value = values[current];
                        toRemove = current;
                        current = next[current] == key2Head.headIndices[key2HeadIndex] ? -1 : next[current];
                        return value;
                    }
                    @Override
                    public Integer next() {
                        return nextInt();
                    }

                    @Override
                    public void remove() {
                        if (toRemove == -1) throw new IllegalStateException("remove() already called or next() not yet called.");
                        if (current != -1) {
                            int currentValue = values[current];
                            map.remove(key, values[toRemove]);
                            int freshIndex = map.getIndexFor(key, currentValue); // Not much performance, but need to refresh since the old index could no longer be valid :(.
                            current = freshIndex < 0 ? -1 : freshIndex;
                        } else {
                            map.remove(key, values[toRemove]);
                        }
                        toRemove = -1;
                    }

                    @Override
                    public int skip(int i) {
                        int skipped = 0;
                        for (; skipped < i && hasNext(); skipped++) {
                            nextInt();
                        }
                        return 0;
                    }
                };
            }

            @Override
            public int[] toIntArray() {
                return toIntArray(new int[size()]);
            }
            @Override
            public Integer[] toArray() {
                return toArray(new Integer[size()]);
            }
            @Override
            public int[] toIntArray(int[] a) {
                int size = size();
                int[] array = a.length >= size ? a : new int[size];
                int i = 0;
                IntIterator it = iterator();
                while (it.hasNext()) {
                    array[i] = it.nextInt();
                    i++;
                }
                return array;
            }
            @Override
            public int[] toArray(int[] a) {
                return toIntArray(a);
            }
            @SuppressWarnings("unchecked")
            @Override
            public <T> T[] toArray(T[] a) {
                int size = size();
                T[] array = a.length >= size ? a : (T[]) new Integer[size];
                int i = 0;
                for (Integer v : this) {
                    array[i] = (T) v;
                    i++;
                }
                return array;
            }

            @Override
            public boolean add(int value) {
                return map.put(key, value);
            }
            @Override
            public boolean add(Integer value) {
                return add(value.intValue());
            }
            @Override
            public boolean rem(int value) {
                return remove(value);
            }
            @Override
            public boolean remove(int value) {
                return map.remove(key, value);
            }
            @Override
            public boolean remove(Object value) {
                return remove(((Integer) value).intValue());
            }
            @Override
            public boolean containsAll(IntCollection intCollection) {
                IntIterator it = intCollection.iterator();
                while (it.hasNext()) {
                    if (!contains(it.nextInt())) return false;
                }
                return true;
            }
            @Override
            public boolean containsAll(Collection<?> c) {
                for (Object value : c) {
                    if (!contains(value)) return false;
                }
                return true;
            }
            @Override
            public boolean addAll(IntCollection intCollection) {
                boolean changed = false;
                IntIterator it = intCollection.iterator();
                while (it.hasNext()) {
                    if (add(it.nextInt())) changed = true;
                }
                return changed;
            }
            @Override
            public boolean addAll(Collection<? extends Integer> c) {
                boolean changed = false;
                for (Integer value : c) {
                    if (add(value)) changed = true;
                }
                return changed;
            }
            @Override
            public boolean retainAll(IntCollection intCollection) {
                boolean changed = false;
                IntIterator it = iterator();
                while (it.hasNext()) {
                    if (!intCollection.contains(it.nextInt())) {
                        it.remove();
                        changed = true;
                    }
                }
                return changed;
            }
            @Override
            public boolean retainAll(Collection<?> c) {
                boolean changed = false;
                Iterator<Integer> it = iterator();
                while (it.hasNext()) {
                    if (!c.contains(it.next())) {
                        it.remove();
                        changed = true;
                    }
                }
                return changed;
            }
            @Override
            public boolean removeAll(IntCollection intCollection) {
                boolean changed = false;
                IntIterator it = intCollection.iterator();
                while (it.hasNext()) {
                    if (remove(it.nextInt())) changed = true;
                }
                return changed;
            }
            @Override
            public boolean removeAll(Collection<?> c) {
                boolean changed = false;
                for (Object value : c) {
                    if (remove(value)) changed = true;
                }
                return changed;
            }
            @Override
            public void clear() {
                Iterator<Integer> it = iterator();
                while (it.hasNext()) {
                    it.next(); it.remove();
                }
            }
            @Override
            public boolean equals(Object o) {
                if (o == this) return true;
                if (!(o instanceof Set)) return false;

                Iterator<Integer> it1 = iterator();
                Iterator<?> it2 = ((Set<?>) o).iterator();
                while (it1.hasNext() && it2.hasNext()) {
                    Integer o1 = it1.next();
                    Object o2 = it2.next();
                    if (!o1.equals(o2)) return false;
                }
                return !(it1.hasNext() || it2.hasNext());
            }

            @Override
            public int hashCode() {
                int hashCode = 0;
                for (Integer value : this) {
                    hashCode += 31 * hashCode + value.hashCode();
                }
                return hashCode;
            }
        };
    }

    // God be with those who need to fix bug in this method :D
    @Override
    public boolean remove(int key, int value) {
        int hash = hash(key, value);
        int index = indexFor(hash);
        while (used[index]) {
            if (hash == hashes[index] && key == keys[index] && value == values[index]) {
                int key2HeadIndex = key2Head.getOrPut(key);
                key2Head.sizes[key2HeadIndex]--;
                key2HeadUpdated(key2HeadIndex);
                size--;

                // Cut relations to this entry
                if (index == key2Head.headIndices[key2HeadIndex]) key2Head.headIndices[key2HeadIndex] = next[index];
                previous[next[index]] = previous[index];
                next[previous[index]] = next[index];

                // Fill gap with existing entries (chain cannot be interrupted)
                int gap = index;
                int current = nextIndexFor(index);
                while (true) {
                    while (used[current]) {
                        int desiredIndex = indexFor(hashes[current]);
                        if (current >= gap ?
                                desiredIndex <= gap || desiredIndex > current :
                                desiredIndex <= gap && desiredIndex > current) break;
                        current = nextIndexFor(current);
                    }
                    if (!used[current]) break;
                    keys[gap] = keys[current];
                    values[gap] = values[current];
                    hashes[gap] = hashes[current];

                    int currentKeyHeadIndex = key2Head.getOrPut(keys[current]);
                    if (current == key2Head.headIndices[currentKeyHeadIndex]) key2Head.headIndices[currentKeyHeadIndex] = gap;

                    previous[gap] = previous[current];
                    next[gap] = next[current];
                    previous[next[current]] = gap;
                    next[previous[current]] = gap;

                    gap = current;
                    current = nextIndexFor(current);
                }

                used[gap] = false;

                return true;
            }
            index = nextIndexFor(index);
        }
        return false;
    }

    @Override
    public boolean contains(int key, int value) {
        int hash = hash(key, value);
        int index = indexFor(hash);
        while (used[index]) {
            if (hash == hashes[index] && key == keys[index] && value == values[index]) {
                return true;
            }
            index = nextIndexFor(index);
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public long getSizeInBytes() {
        return (long) STATIC_SIZE_IN_BYTES + ENTRY_SIZE_IN_BYTES * keys.length + KEY2HEAD_ENTRY_SIZE_IN_BYTES * key2Head.keys.length;
    }

    public float getLoadFactor() {
        return loadFactor;
    }

    public float getActualLoadFactor() {
        return (float) size / keys.length;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Int2IntSetMultimap)) return false;

        Int2IntSetMultimap m = (Int2IntSetMultimap) o;
        if (m.size() != size()) return false;

        for (int i = 0; i < key2Head.keys.length; i++) {
            if (key2Head.sizes[i] == 0) continue;

            Iterator<Integer> it = m.get(key2Head.keys[i]).iterator();
            int headIndex = key2Head.headIndices[i];
            int current = headIndex;
            do {
                if (!it.hasNext() || values[current] != it.next()) return false;
                current = next[current];
            } while (current != headIndex);

            if (it.hasNext()) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < key2Head.keys.length; i++) {
            if (key2Head.sizes[i] == 0) continue;
            int headIndex = key2Head.headIndices[i];
            int current = headIndex;
            do {
                h += hashes[current];
                current = next[current];
            } while (current != headIndex);
        }
        return h;
    }
}
