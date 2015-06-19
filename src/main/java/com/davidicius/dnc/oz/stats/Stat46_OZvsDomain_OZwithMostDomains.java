package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.Stat;
import com.davidicius.dnc.oz.StatOz;
import com.davidicius.dnc.oz.StatsContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class Stat46_OZvsDomain_OZwithMostDomains implements Stat {
    int top = 100;
    public static Set<String> ignore = new HashSet<String>();

    static {
        ignore.add("ting");
        ignore.add("ator");
        ignore.add("ecka");
        ignore.add("tnet");
        ignore.add("eren");
        ignore.add("onet");
        ignore.add("tart");
        ignore.add("tory");
        ignore.add("epraha");
        ignore.add("tana");
        ignore.add("aska");
        ignore.add("ding");
        ignore.add("ocar");
        ignore.add("ping");
        ignore.add("ning");
    }

    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {

        out.write("46 - TOP OZ s nejvice domenami");
        out.newLine();

        int[] counts = new int[top];
        String[] names = new String[top];
        int index = 0;

        ArrayList<String> list = new ArrayList<String>(ctx.getOz2Domain().getKeys());

        Collections.sort(list, ctx.getOzComparator());

        for (String oz : list) {
            if (ignore.contains(oz)) continue;

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
