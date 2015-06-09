package com.davidicius.dnc.oz;

import java.io.*;
import java.util.*;

public class StringIndexFile extends StringIndex {
    private BufferedWriter bw;
    private String lastKey;

    public StringIndexFile(String filename) throws IOException {
        bw = new BufferedWriter(new FileWriter(filename));
    }

    public void close() throws IOException {
        if (bw != null) {
            bw.close();
        }
    }

    public void put(String key, String value) {
        try {
            if (!key.equals(lastKey)) {
                bw.write("Index:" + key);
                bw.newLine();

                lastKey = key;
            }

            bw.write("   ");
            bw.write(value);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public Set<String> getKeys() {
        throw new IllegalStateException();
    }

    public void setList(String key, ArrayList<String> list) {
        throw new IllegalStateException();
    }

    public ArrayList<String> get(String key) {
        throw new IllegalStateException();
    }

    public int countKeys() {
        throw new IllegalStateException();
    }

    public void saveIndex(String file) throws IOException {
        throw new IllegalStateException();
    }

    public void deleteKey(String key) {
        throw new IllegalStateException();
    }
}
