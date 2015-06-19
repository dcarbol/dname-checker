package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Histogram;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Stat61_PatternInCzDomain_Histogram implements Stat {
    int top = 100;

    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("61 - Patterny v ceskych domenach, ktere obsahuji OZ");
        out.newLine();
        Pattern p = Pattern.compile(".{1,2}#OZ#.*");

        int[] counts = new int[top];
        String[] names = new String[top];
        int index = 0;

        Histogram histogram = new Histogram();
        for (String oz : ctx.getOz2Domain().getKeys()) {
            ArrayList<String> strings = ctx.getOz2Domain().get(oz);

            oz = oz.replaceAll("\\s+", "");
            for (String line : strings) {
                String[] parts = line.split("->");
                if (parts.length != 2) throw new IllegalStateException();

                String domain = parts[0].trim();
                domain = domain.replaceAll(oz, "#OZ#");
                if (domain.endsWith(".cz.cz")) domain = domain.substring(0, domain.length() - ".cz".length());
                histogram.add(domain);
            }
        }

        List<String> keys = histogram.getOrderedKeys();
        for (String word : keys) {
            if (word.equals("#OZ#.cz")) continue;

            if (p.matcher(word).matches()) {
                if (!word.startsWith("e#") &&
                    !word.startsWith("i#") &&
                    !word.startsWith("i-#") &&
                    !word.startsWith("e-#")) {
                    continue;
                }
            }

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
        return 3;
    }
}
