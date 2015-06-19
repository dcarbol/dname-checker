package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Stat47_OZvsDomain_UpvOZwithMostDomains implements Stat {
    int top = 100;
    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {

        out.write("47 - TOP UPV OZ s nejvice domenami");
        out.newLine();

        int[] counts = new int[top];
        String[] names = new String[top];
        int index = 0;

        ArrayList<String> list = new ArrayList<String>(ctx.getOz2Domain().getKeys());

        Collections.sort(list, ctx.getOzComparator());

        Set<String> upv = new HashSet<String>(ctx.getUpvList().size());
        for (StatOz oz : ctx.getUpvList()) {
            upv.add(oz.getName());
        }

        for (String oz : list) {
            if (!upv.contains(oz)) continue;
            if (Stat46_OZvsDomain_OZwithMostDomains.ignore.contains(oz)) continue;

            ArrayList<String> domains = ctx.getOz2Domain().get(oz);
            names[index] = oz;
            counts[index] = domains.size();
            index++;
            if (index == top) break;
        }

        for (String name : names) {
            out.write(String.format("%s\t", name));
        }
        out.newLine();

        for (int count : counts) {
            out.write(String.format("%d\t", count));
        }
        out.newLine();
    }

    public int priority() {
        return 2;
    }
}
