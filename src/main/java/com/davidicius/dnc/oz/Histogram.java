package com.davidicius.dnc.oz;

import java.io.*;
import java.util.*;

public class Histogram {
    static class IntHolder {
        public int value;
    }

    private Map<String, IntHolder> index = new HashMap<String, IntHolder>(10*1000);

    public void add(String key) {
        IntHolder h = index.get(key);
        if (h == null) {
            h = new IntHolder();
            h.value = 1;
            index.put(key, h);
        } else {
            h.value++;
        }
    }

    public int countForKey(String key) {
        IntHolder h = index.get(key);
        return h == null ? 0 : h.value;
    }

    public List<String> getOrderedKeys() {
        List<String> result = new ArrayList<String>(index.keySet());
        Collections.sort(result, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return countForKey(o2) - countForKey(o1);
            }
        });

        return result;
    }

    public void save(String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        List<String> keys = new ArrayList<String>(index.keySet());
        Collections.sort(keys, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return index.get(o2).value - index.get(o1).value;
            }
        });

        for (String key : keys) {
            bw.write(key + ":" + index.get(key).value);
            bw.newLine();
        }

        bw.close();
    }

}
