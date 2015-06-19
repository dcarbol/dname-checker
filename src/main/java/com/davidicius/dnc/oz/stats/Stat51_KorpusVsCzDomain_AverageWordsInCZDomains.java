package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Stat51_KorpusVsCzDomain_AverageWordsInCZDomains implements Stat {
    Pattern p = Pattern.compile("[A-Z0-9]+");
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("51 - Prumerny pocet slov v ceskych domenach");
        out.newLine();

        int sum = 0;
        int count = ctx.getCzDomains2CorpusLine().countKeys();
        for(String domain : ctx.getCzDomains2CorpusLine().getKeys()) {
            ArrayList<String> strings = ctx.getCzDomains2CorpusLine().get(domain);
            if (strings == null || strings.size() != 1) throw new IllegalStateException();
            String line = strings.get(0).trim();

            String[] parts = line.split("\\s+");
            for (String part : parts) {
                if (p.matcher(part).matches()) {
                    sum++;
                }
            }
        }

        int result = sum * 10 / count;
        out.write(String.format("%d", result));
        out.newLine();
    }

    public int priority() {
        return 2;
    }
}
