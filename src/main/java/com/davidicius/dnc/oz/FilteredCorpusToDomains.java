package com.davidicius.dnc.oz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FilteredCorpusToDomains {
    private static final Logger log = LoggerFactory.getLogger(FilteredCorpusToDomains.class);

    public static void main(String[] args) throws IOException {
        GlobalStringTable global = new GlobalStringTable();

        log.info("Loading...");
        StringIndex ss = StringIndex.loadIndex("habilitace\\FilteredCorpus2Domain.txt", global);
        log.info("Saving");
        ss.saveIndex("habilitace\\FilteredCorpus2DomainSorted.txt");
    }
}
