package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Stat21_OZ_UPV_OHIM_records implements Stat {
    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException {
        out.write("21 - Vsechny vstupni OZ zaznamy rozdeleny podle zdroje");
        out.newLine();

        Set<StatOz> upvSet = new HashSet<StatOz>(ctx.getUpvList());
        Set<StatOz> ohimSet = new HashSet<StatOz>(ctx.getOhimList());

        out.write(String.format("%s\t%s\t%s\t%s\t", "UPV - zaznamy", "UPV - oz", "OHIM - zaznamy", "OHIM - oz"));
        out.newLine();

        out.write(String.format("%d\t%d\t%d\t%d\t", ctx.getUpvList().size(), upvSet.size(), ctx.getOhimList().size(), ohimSet.size()));
        out.newLine();
    }

    public int priority() {
        return 0;
    }
}
