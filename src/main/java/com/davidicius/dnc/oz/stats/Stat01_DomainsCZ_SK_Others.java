package com.davidicius.dnc.oz.stats;

import com.davidicius.dnc.oz.*;

import java.io.BufferedWriter;
import java.io.IOException;

public class Stat01_DomainsCZ_SK_Others implements Stat {
    public void printStat(StatsContext ctx,  BufferedWriter out) throws IOException {
        out.write("01 - Vsechny vstupni domeny rozdeleny podle domeny prvniho radu");
        out.newLine();

        CountMap cm = new CountMap();
        for (Domain d : ctx.getDomains()) {
            String name = d.getName().toLowerCase();
            if (name.endsWith(".cz")) cm.add(".CZ");
            else if (name.endsWith(".sk")) cm.add(".SK");
            else
                cm.add("Ostatni");
        }

        out.write(String.format("%s\t%s\t%s\t", ".CZ", ".SK", "Ostatni"));
        out.newLine();

        out.write(String.format("%d\t%d\t%d\t", cm.get(".CZ"), cm.get(".SK"), cm.get("Ostatni")));
        out.newLine();
    }

    public int priority() {
        return 0;
    }
}
