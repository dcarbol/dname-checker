package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Histogram;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Stat72_DetailedDomains_Bad_Good_perOz implements Stat {

    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("72 - Detailne zkoumane domeny - Rozlozeni BAD - GOOD pro jednotlive OZ");
        out.newLine();

        Histogram all = new Histogram();
        Histogram bad = new Histogram();
        Histogram good = new Histogram();
        for (Domain d : ctx.getDetailedDomains()) {
            if (!d.isExists()) continue;

            String oz = d.getOzName();
            if (d.isBad()) {
                bad.add(oz);
            }

            if (d.isGood() || d.isNeutral()){
                good.add(oz);
            }

            all.add(oz);
        }

        List<String> keys = all.getOrderedKeys();
        for (String oz : keys) {
            out.write(String.format("%s\t", oz));
        }
        out.newLine();

        for (String oz : keys) {
            out.write(String.format("%d\t", bad.countForKey(oz)));
        }
        out.newLine();

        for (String oz : keys) {
            out.write(String.format("%d\t", good.countForKey(oz)));
        }
        out.newLine();

    }

    public int priority() {
        return 3;
    }
}
