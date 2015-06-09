package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.CountMap;
import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;

public class Stat31_Dictionaries_Counts implements Stat {
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("31 -Slovniky - pocty klicovych slov");
        out.newLine();

        out.write(String.format("%s\t%s\t%s\t%s\t%s", "Cesky slovnik", "Anglicky korpus", "Cesky korpus", "Ceska jmena", "Ceske obce"));
        out.newLine();

        out.write(String.format("%d\t%d\t%d\t%d\t%d", ctx.getCzDictionary().size(), ctx.getEnCorpus().size(), ctx.getCzCorpus().size(), ctx.getCzNames().size(), ctx.getCzCities().size()));
        out.newLine();
    }

    public int priority() {
        return 0;
    }
}
