package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Histogram;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Stat71_DetailedDomains_Bad_Good implements Stat {

    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("71 - Detailne zkoumane domeny - Rozlozeni BAD - GOOD");
        out.newLine();

        int bad = 0;
        int good = 0;
        int noExists = 0;
        int all = 0;
        for (Domain d : ctx.getDetailedDomains()) {
            if (d.isExists()) {
                if (d.isBad()) bad++;
                if (d.isNeutral() || d.isGood()) good++;

                if (d.isBad() && (d.isNeutral() || d.isGood())) {
                    System.out.println(d.getName());
                }
            } else {
                noExists++;
            }

            all++;
        }

        out.write(String.format("%s\t%s\t%s\t%s", "Vsechny", "Bad", "Good", "Neexistuji"));
        out.newLine();
        out.write(String.format("%d\t%d\t%d\t%s", all, bad, good, noExists));
        out.newLine();
    }

    public int priority() {
        return 3;
    }
}
