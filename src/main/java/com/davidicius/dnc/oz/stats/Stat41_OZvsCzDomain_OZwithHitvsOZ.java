package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;

public class Stat41_OZvsCzDomain_OZwithHitvsOZ implements Stat {
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("41 - OZ - Hit v ceske domene vs. ostatni");
        out.newLine();

        out.write(String.format("%s\t%s", "OZ ma hit v CZ domene", "OZ nema hit v CZ domene"));
        out.newLine();

        out.write(String.format("%d\t%d", ctx.getOzWithHitInCzDomains().size(), ctx.getFilteredOZ().size() - ctx.getOzWithHitInCzDomains().size()));
        out.newLine();
    }

    public int priority() {
        return 2;
    }
}
