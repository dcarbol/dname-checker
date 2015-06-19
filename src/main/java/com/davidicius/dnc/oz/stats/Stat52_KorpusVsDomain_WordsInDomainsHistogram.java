package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Stat52_KorpusVsDomain_WordsInDomainsHistogram implements Stat {
    int[] bands = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1000};
    Pattern p = Pattern.compile("[A-Z0-9]+");

    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("52 - Pocet slov v ceskych domenach - histogram");
        out.newLine();
        int[] counts = new int[bands.length];

        for(String domain : ctx.getCzDomains2CorpusLine().getKeys()) {
            ArrayList<String> strings = ctx.getCzDomains2CorpusLine().get(domain);
            if (strings == null || strings.size() != 1) throw new IllegalStateException();
            String line = strings.get(0).trim();

            String[] parts = line.split("\\s+");
            int len = 0;
            for (String part : parts) {
                if (p.matcher(part).matches()) {
                    len++;
                }
            }

            int i = 0;
            while (i + 1 < bands.length ) {
                if (len >= bands[i] && len < bands[i+1]) {
                    counts[i]++;
                    break;
                }

                i++;
            }
        }

        counts[0] += ctx.getCzDomains().size() - ctx.getCzDomains2CorpusLine().getKeys().size();
        for (int i = 0; i < bands.length - 1; ++i) {
            int a = bands[i];
            int b = bands[i+1];
            String bb = (a + 1 == b) ? Integer.toString(a) : String.format("%d-%d", a, b);

            out.write(String.format("%s\t", bb));
        }
        out.newLine();

        for (int i = 0; i < bands.length - 1; ++i) {
            out.write(String.format("%d\t", counts[i]));
        }
        out.newLine();
    }

    public int priority() {
        return 2;
    }
}
