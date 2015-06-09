package com.davidicius.dnc.oz;

import com.davidicius.dnc.structure.Int2IntArrayHashSetMultimap;
import com.davidicius.dnc.structure.Int2IntSetMultimap;
import com.davidicius.dnc.structure.Object2StableIntHashSet;
import com.davidicius.dnc.structure.Object2StableIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.io.*;
import java.util.*;

public class StringIndexFast {
    private Object2StableIntSet<String> table = new Object2StableIntHashSet<String>(1000);
    private Int2IntSetMultimap data = new Int2IntArrayHashSetMultimap(1000);
    private IntSet keySet = new IntArraySet();

    public StringIndexFast(Object2StableIntSet<String> table) {
        this.table = table;
    }

    public void put(String key, String value) {
        int stableIndexKey = table.add(key);
        int stableIndexValue = table.add(value);

        data.put(stableIndexKey, stableIndexValue);
        keySet.add(stableIndexKey);
    }

    public Set<String> getKeys() {
        Set<String> keys = new HashSet<String>(keySet.size());
        for (int key : keySet) {
            keys.add(table.get(key));
        }

        return keys;
    }

    public void setList(String key, ArrayList<String> list) {
        throw new IllegalStateException();
    }

    public IntSet get(String key) {
        return data.get(table.contains(key));
    }

    public int countKeys() {
        return keySet.size();
    }

    public static StringIndexFast loadIndex(String filename, Object2StableIntSet<String> table) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String currentKey = null;
        StringIndexFast index = new StringIndexFast(table);
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.startsWith("Index:")) {
                currentKey = line.substring("Index:".length());
            } else {
                if (currentKey == null) throw new IllegalStateException();
                index.put(currentKey, line);
            }
        }
        br.close();

        return index;
    }

    public void saveIndex(String file) throws IOException {
        System.out.println("Saving index to: " + file);

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        IntArrayList keys = new IntArrayList(keySet);
        Collections.sort(keys, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return data.get(o2).size() - data.get(o1).size();
            }
        });

        for (int key : keys) {
            bw.write("Index:" + table.get(key));
//            bw.write(key);
            bw.newLine();

            IntSet values = data.get(key);
            for (int value : values) {
                bw.write("   ");
                bw.write(table.get(value));
                bw.newLine();
            }
        }

        bw.close();
    }

    public void deleteKey(String key) {
        int keyIndex = table.contains(key);
        if (keyIndex != -1) {
            IntSet values = data.get(keyIndex);
            for (int value : values) {
                data.remove(keyIndex, value);
            }

            keySet.rem(keyIndex);
        }
    }
}
