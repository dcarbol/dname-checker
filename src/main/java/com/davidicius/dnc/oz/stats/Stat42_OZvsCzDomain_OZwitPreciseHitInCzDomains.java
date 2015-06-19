package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Stat42_OZvsCzDomain_OZwitPreciseHitInCzDomains implements Stat {
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("42 - OZ - Presne hity v ceske domene vs. ostatni");
        out.newLine();

        out.write(String.format("%s\t%s", "OZ ma hit v CZ domene", "OZ nema hit v CZ domene"));
        out.newLine();

        int count = 0;
        for(StatOz oz : ctx.getFilteredOZ()) {
            ArrayList<String> domains = ctx.getOz2Domain().get(oz.getName());
            if (domains != null && domains.size() > 0) {
                count++;
            }
        }

        out.write(String.format("%d\t%d", count, ctx.getFilteredOZ().size() - count));
        out.newLine();
    }

    public int priority() {
        return 2;
    }
}
