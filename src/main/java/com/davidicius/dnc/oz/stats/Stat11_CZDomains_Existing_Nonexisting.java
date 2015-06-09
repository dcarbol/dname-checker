package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Stat11_CZDomains_Existing_Nonexisting implements Stat {
    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("11 - CZ domeny existujici a neexistujici");
        out.newLine();

        int exists = 0;
        int all = 0;
        for (Domain d : ctx.getDomains()) {
            String name = d.getName().toLowerCase();
            if (name.endsWith(".cz")) {
                if (d.isExists()) exists++;
                all++;
            }
        }

        out.write(String.format("%s\t%s", "Existujici", "Neexistujici"));
        out.newLine();

        out.write(String.format("%d\t%d", exists, all - exists));
        out.newLine();
    }

    public int priority() {
        return 0;
    }
}
