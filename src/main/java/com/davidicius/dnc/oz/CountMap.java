package com.davidicius.dnc.oz;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CountMap {
    private Map<String, Integer> data = new HashMap<String, Integer>();

    public void add(String key) {
        Integer value = data.get(key);
        if (value == null) {
            data.put(key, 1);
        } else {
            data.put(key, value + 1);
        }
    }

    public int get(String key) {
        if (data.containsKey(key)) {
            return data.get(key);
        } else {
            return 0;
        }
    }

    public Collection<String> keys() {
        return data.keySet();
    }
}
