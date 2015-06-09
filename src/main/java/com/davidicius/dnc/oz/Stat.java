package com.davidicius.dnc.oz;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public interface Stat {
    public void printStat(StatsContext ctx, BufferedWriter out) throws IOException;
    public int priority();
}
