package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Domain;
import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Stat49_OZvsCzDomain_OzWithDirectDomain implements Stat {
    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("49 - Pocet OZ s primou CZ domenou");
        out.newLine();

        Set<String> czDomains = new HashSet<String>(ctx.getCzDomains().size());
        for (Domain domain : ctx.getCzDomains()) {
            czDomains.add(domain.getName());
        }

        int removeSpacesHit = 0;
        int replaceSpacesWithMinus = 0;
        int noSimpleHit = 0;
        for (String ozName : ctx.getOzWithHitInCzDomains()) {
            String simple = ozName.replaceAll("\\s+", "");
            simple = simple + ".cz";
            if (czDomains.contains(simple)) {
                removeSpacesHit++;
                continue;
            }

            String minus = ozName.replaceAll("\\s+", "-");
            minus = minus + ".cz";
            if (czDomains.contains(minus)) {
                replaceSpacesWithMinus++;
                continue;
            }

            noSimpleHit++;
        }

        out.write(String.format("%s\t%s\t%s", "A B -> AB.cz", "A B -> A-B.cz", "Jiny hit"));
        out.newLine();

        out.write(String.format("%d\t%d\t%d", removeSpacesHit, replaceSpacesWithMinus, noSimpleHit));
        out.newLine();
    }

    public int priority() {
        return 3;
    }
}
