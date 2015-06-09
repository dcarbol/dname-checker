package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Stat09_CZDomainsLengthHistogram implements Stat {
    int[] bands = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 30, 35, 40, 50, 60, 70, 80, 90, 100, 150, 200, 1000};

    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("09 - CZ domeny - pocet znaku - histogram");
        out.newLine();
        int[] counts = new int[bands.length];
        for (Domain d : ctx.getDomains()) {
            String name = d.getName().toLowerCase();
            if (!name.endsWith(".cz")) continue;

            int len = name.length() - 3;
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
