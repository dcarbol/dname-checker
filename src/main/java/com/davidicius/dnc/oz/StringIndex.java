package com.davidicius.dnc.oz;

import java.io.*;
import java.util.*;

public class StringIndex {
    private Map<String, ArrayList<String>> index = new HashMap<String, ArrayList<String>>(10*1000);

    public void put(String key, String value) {
        ArrayList<String> list = index.get(key);
        if (list == null) {
            list = new ArrayList<String>(1);
            index.put(key, list);
        }

        list.add(value);
    }

    public Set<String> getKeys() {
        return index.keySet();
    }

    public void setList(String key, ArrayList<String> list) {
        index.put(key, list);
    }

    public ArrayList<String> get(String key) {
        return index.get(key);
    }

    public int countKeys() {
        return index.keySet().size();
    }

    public static StringIndex loadIndex(String filename) throws IOException {
        return  loadIndex(filename, null);
    }

    public static StringIndex loadIndex(String filename, GlobalStringTable global) throws IOException {
        if (global == null) {
            global = new GlobalStringTable();
        }

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String currentKey = null;
        StringIndex index = new StringIndex();
        while (true) {
            String line = br.readLine();
            if (line == null) break;

            line = line.trim();
            if (line.startsWith("Index:")) {
                currentKey = line.substring("Index:".length());
            } else {
                if (currentKey == null) throw new IllegalStateException();
                index.put(global.add(currentKey), global.add(line));
            }
        }
        br.close();

        return index;
    }

    public void saveIndex(String file) throws IOException {
        System.out.println("Saving index to: " + file);

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        List<String> keys = new ArrayList<String>(index.keySet());
        Collections.sort(keys, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return index.get(o2).size() - index.get(o1).size();
            }
        });

        for (String key : keys) {
            bw.write("Index:" + key);
//            bw.write(key);
            bw.newLine();

            ArrayList<String> list = index.get(key);
            for (int i = 0; i < list.size(); i++) {
                bw.write("   ");
                bw.write(list.get(i));
                bw.newLine();
            }
        }

        bw.close();
    }

    public void deleteKey(String key) {
        index.remove(key);
    }
}
