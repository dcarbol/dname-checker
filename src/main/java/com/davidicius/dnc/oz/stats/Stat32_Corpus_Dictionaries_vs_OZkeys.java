package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;

public class Stat32_Corpus_Dictionaries_vs_OZkeys implements Stat {
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("32 -Korpus - Slova ze slovniku a generovana z OZ");
        out.newLine();

        int ozKeys = 0;
        int dictKeys = 0;
        int both = 0;

        for (String key : ctx.getAllCorpus()) {
            boolean a = ctx.getKeysFromOz().contains(key);
            boolean b = ctx.getCzDictionary().contains(key) || ctx.getEnCorpus().contains(key) || ctx.getCzCorpus().contains(key) || ctx.getCzCities().contains(key) || ctx.getCzNames().contains(key);

            if (a && !b) {
                ozKeys++;
            }
            if (a && b) {
                both++;
            }
            if (!a && b) {
                dictKeys++;
            }
        }

        out.write(String.format("%s\t%s\t%s", "Slova - OZ", "Slova - Slovniky", "Slova - Oboji"));
        out.newLine();

        out.write(String.format("%d\t%d\t%d", ozKeys, dictKeys, both));
        out.newLine();
    }

    public int priority() {
        return 1;
    }
}
