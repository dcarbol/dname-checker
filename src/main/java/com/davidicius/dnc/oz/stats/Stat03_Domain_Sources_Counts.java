package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Stat03_Domain_Sources_Counts implements Stat {
    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("03 - Zdroje domen, ktere jsme meli k dispozici");
        out.newLine();

        int allSources = 0;
        int czDomeny = 0;
        int skDomeny = 0;
        int tomDomeny = 0;
        int top1m = 0;
        for (Domain d : ctx.getDomains()) {
            if (d.isCzDomeny()) czDomeny++;
            if (d.isSkDomeny()) skDomeny++;
            if (d.isCzTom()) tomDomeny++;
            if (d.isTop1m()) top1m++;
            allSources++;
        }

        out.write(String.format("%s\t%s\t%s\t%s", "CZ - domeny", "TOM - domeny", "SK - domeny", "TOP1m - domeny"));
        out.newLine();

        out.write(String.format("%d\t%d\t%d\t%d", czDomeny, tomDomeny, skDomeny, top1m));
        out.newLine();
    }

    public int priority() {
        return 0;
    }
}
