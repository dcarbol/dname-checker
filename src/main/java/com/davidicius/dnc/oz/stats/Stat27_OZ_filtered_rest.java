package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Stat27_OZ_filtered_rest implements Stat {
    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("27 - OZ filtrovane vs ostatni");
        out.newLine();

        out.write(String.format("%s\t%s", "Filtered OZ",  "Ostatni OZ"));
        out.newLine();

        out.write(String.format("%d\t%d", ctx.getFilteredOZ().size(), ctx.getAllOZ().size() - ctx.getFilteredOZ().size()));
        out.newLine();
    }

    public int priority() {
        return 1;
    }
}
