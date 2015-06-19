package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Stat43_OZvsCzDomain_DomainsLinkedToOz implements Stat {
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("43 - Domeny a jejich vazba na OZ");
        out.newLine();

        out.write(String.format("%s\t%s", "Domena obsahuje OZ", "Domena neobsahuje OZ"));
        out.newLine();

        Set<String> dd = new HashSet<String>(1000);
        for(StatOz oz : ctx.getFilteredOZ()) {
            ArrayList<String> domains = ctx.getOz2Domain().get(oz.getName());
            if (domains != null) {
                for (String domain : domains) {
                    dd.add(domain);
                }
            }
        }

        out.write(String.format("%d\t%d", dd.size(), ctx.getCzDomains().size() - dd.size()));
        out.newLine();
    }

    public int priority() {
        return 2;
    }
}
