package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Stat45_OZvsDomain_DomainsPerOzHistogram implements Stat {
    int[] bands = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1000};

    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("45 - Pocet domen pro OZ - histogram");
        out.newLine();
        int[] counts = new int[bands.length];
        for (StatOz oz : ctx.getFilteredOZ()) {
            ArrayList<String> domains = ctx.getOz2Domain().get(oz.getName());
            int len = domains == null ? 0 : domains.size();

            int i = 0;
            while (i + 1 < bands.length ) {
                if (len >= bands[i] && len < bands[i+1]) {
                    counts[i]++;
                    break;
                }

                i++;
            }
        }

        for (int i = 0; i < bands.length - 1; ++i) {
            int a = bands[i];
            int b = bands[i+1];
            String bb = (a + 1 == b) ? Integer.toString(a) : String.format("%d-%d", a, b);

            out.write(String.format("%s\t", bb));
        }
        out.newLine();

        int sum = 0;
        for (int i = 0; i < bands.length - 1; ++i) {
            out.write(String.format("%d\t", counts[i]));
            sum += counts[i];
        }
        out.newLine();
        System.out.println(sum);
    }

    public int priority() {
        return 2;
    }
}
