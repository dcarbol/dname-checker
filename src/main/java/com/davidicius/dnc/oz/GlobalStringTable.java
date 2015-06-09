package com.davidicius.dnc.oz;

import java.util.HashMap;
import java.util.Map;

public class GlobalStringTable {
    private Map<String, String> data = new HashMap<String, String>();

    public String add(String s) {
        String a = data.get(s);
        if (a != null) {
            return a;
        } else {
            data.put(s, s);
            return  s;
        }
    }
}
