package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Stat05_AllDomainsAverageCharacters implements Stat {
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("05 - Vsechny domeny - prumerny pocet znaku");
        out.newLine();

        int czCount = 0;
        long czSum = 0;
        int skCount = 0;
        long skSum = 0;
        int otherCount = 0;
        long otherSum = 0;

        for (Domain d : ctx.getDomains()) {
            String name = d.getName().toLowerCase();
            if (name.endsWith(".cz")) {
                czCount++;
                czSum += name.length() - 3;
            } else if (name.endsWith(".sk")) {
                skCount++;
                skSum += name.length() - 3;
            } else {
                otherCount++;
                otherSum += name.length() - 3;
            }
        }

        out.write(String.format("%s\t%s\t%s\t%s", "CZ - domeny", "SK - domeny", "Ostatni - domeny", "Vsechny - domeny"));
        out.newLine();

        String line = String.format("%d\t%d\t%d\t%d", czSum * 10 / czCount, skSum * 10 / skCount, otherSum * 10 / otherCount, (czSum + skSum + otherSum) * 10 / (czCount + skCount + otherCount));
        out.write(line);
        out.newLine();
    }

    public int priority() {
        return 0;
    }
}
