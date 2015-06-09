package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Stat10_AllDomainsPartsHistogram implements Stat {
    int[] bands = {1, 2, 3, 1000};

    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("10 - CZ domeny - pocet casti - histogram");
        out.newLine();
        int[] counts = new int[bands.length];
        for (Domain d : ctx.getDomains()) {
            String name = d.getName().toLowerCase();
            if (!name.endsWith(".cz")) continue;

            String[] parts = name.split("-");
            int len = parts.length;
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
        return 0;
    }
}
