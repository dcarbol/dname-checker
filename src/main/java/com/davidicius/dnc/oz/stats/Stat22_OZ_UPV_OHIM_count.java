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

public class Stat22_OZ_UPV_OHIM_count implements Stat {
    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("22 - OZ podle zdroje");
        out.newLine();

        Set<StatOz> upvSet = new HashSet<StatOz>(ctx.getUpvList());
        Set<StatOz> ohimSet = new HashSet<StatOz>(ctx.getOhimList());

        Set<StatOz> a = new HashSet<StatOz>(upvSet);
        a.removeAll(ohimSet);

        Set<StatOz> b = new HashSet<StatOz>(ohimSet);
        b.removeAll(upvSet);

        upvSet.retainAll(ohimSet);
        out.write(String.format("%s\t%s\t%s", "UPV",  "OHIM", "UPV + OHIM"));
        out.newLine();

        out.write(String.format("%d\t%d\t%d", a.size(), b.size(), upvSet.size()));
        out.newLine();
    }

    public int priority() {
        return 0;
    }
}
