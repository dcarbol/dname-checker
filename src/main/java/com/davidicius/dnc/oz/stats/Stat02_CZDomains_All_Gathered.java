package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Stat02_CZDomains_All_Gathered implements Stat {
    public static final int ALL_CZ_DOMAINS_FROM_NIC = 1200000;
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("02 - Vsechny registrovane CZ domeny vs. domeny, ktere jsme ziskali");
        out.newLine();

        int countGathered = 0;
        for (Domain d : ctx.getDomains()) {
            String name = d.getName().toLowerCase();
            if (!name.endsWith(".cz")) continue;

            countGathered++;
        }

        out.write(String.format("%s\t%s", "Zpracovany", "Ostatni"));
        out.newLine();

        out.write(String.format("%d\t%d", countGathered, ALL_CZ_DOMAINS_FROM_NIC - countGathered));
        out.newLine();
    }

    public int priority() {
        return 0;
    }

}
