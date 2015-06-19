package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Histogram;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Stat53_KorpusVsDomain_MostUsedWords implements Stat {
    int top = 100;
    public static Set<String> ignore = new HashSet<String>();

    static {
        ignore.add("ghgjgkjgjkg");
    }
    Pattern p = Pattern.compile("[A-Z0-9]{3,200}");

    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("53 - Nejpouzivanejsi slova v ceskych domenach");
        out.newLine();

        int[] counts = new int[top];
        String[] names = new String[top];
        int index = 0;

        Histogram histogram = new Histogram();
        for(String domain : ctx.getCzDomains2CorpusLine().getKeys()) {
            ArrayList<String> strings = ctx.getCzDomains2CorpusLine().get(domain);
            if (strings == null || strings.size() != 1) throw new IllegalStateException();
            String line = strings.get(0).trim();

            String[] parts = line.split("\\s+");
            for (String part : parts) {
                if (p.matcher(part).matches()) {
                    histogram.add(part.toLowerCase());
                }
            }

        }

        List<String> keys = histogram.getOrderedKeys();
        for (String word : keys) {
            if (ignore.contains(word)) continue;

            names[index] = word;
            counts[index] = histogram.countForKey(word);
            index++;
            if (index == top) break;
        }

        for (String name : names) {
            out.write(String.format("%s\t", name));
        }
        out.newLine();

        for (int count : counts) {
            out.write(String.format("%d\t", count));
        }
        out.newLine();
    }

    public int priority() {
        return 2;
    }
}
