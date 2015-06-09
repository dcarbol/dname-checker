package com.davidicius.dnc.structure;

import it.unimi.dsi.fastutil.ints.IntSet;

public interface Int2IntSetMultimap {
    boolean put(int key, int value);
    IntSet get(int key);
    boolean remove(int key, int value);
    boolean contains(int key, int value);
    int size();
    boolean isEmpty();
    long getSizeInBytes();
}
