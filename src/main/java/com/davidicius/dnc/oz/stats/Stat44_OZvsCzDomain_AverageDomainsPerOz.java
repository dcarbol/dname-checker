package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Stat44_OZvsCzDomain_AverageDomainsPerOz implements Stat {
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("44 - Prumerny pocet domen na jednu OZ");
        out.newLine();

        int sum = 0;
        int count = 0;
        for(StatOz oz : ctx.getFilteredOZ()) {
            ArrayList<String> domains = ctx.getOz2Domain().get(oz.getName());
            if (domains != null && domains.size() > 0) {
                sum += domains.size();
                count++;
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
