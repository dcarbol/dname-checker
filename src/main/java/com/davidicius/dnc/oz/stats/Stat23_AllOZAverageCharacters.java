package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Stat23_AllOZAverageCharacters implements Stat {
    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("23 - Vsechny OZ - prumerny pocet znaku");
        out.newLine();

        int upvCount = 0;
        long upvSum = 0;
        int ohimCount = 0;
        long ohimSum = 0;

        Set<StatOz> upvSet = new HashSet<StatOz>(ctx.getUpvList());
        Set<StatOz> ohimSet = new HashSet<StatOz>(ctx.getOhimList());
        Set<StatOz> all = new HashSet<StatOz>(upvSet);
        all.addAll(ohimSet);

        for (StatOz oz : all) {
            String name = oz.getName();
            if (upvSet.contains(oz)) {
                upvCount++;
                upvSum += name.length();
            }

            if (ohimSet.contains(oz)) {
                ohimCount++;
                ohimSum += name.length();
            }
        }

        out.write(String.format("%s\t%s\t%s", "UPV", "OHIM", "Vsechny OZ"));
        out.newLine();

        String line = String.format("%d\t%d\t%d", upvSum * 10 / upvCount, ohimSum * 10 / ohimCount, (upvSum + ohimSum) * 10 / (upvCount + ohimCount));
        out.write(line);
        out.newLine();
    }

    public int priority() {
        return 0;
    }
}
