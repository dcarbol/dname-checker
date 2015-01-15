package com.davidicius.dnc;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Starter {
    private static final Logger log = LoggerFactory.getLogger(Starter.class);

    public static void main(String[] args) throws IOException {
        log.debug("DNC 0.2 Started...");
        String domainName = "kofsdsola.cz";
        Document doc = Jsoup.connect("http://www.nic.cz/whois/?q=" + domainName).get();
        if (doc.toString().contains("Záznam nenalezen")) {
            log.info("No results for domain '" + domainName + "'");
            return;
        }

        Elements select = doc.select("table.result>tbody>tr");
        if (select.size() == 0) {
            log.warn("Corrupted result page for domain '" + domainName + "'");
            return;
        }

        for (Element element : select) {
            Elements list = element.getElementsByTag("th");
            if (list.size() != 1) {
                log.warn("Corrupted result page for domain '" + domainName + "'");
                continue;
            }

            Element e = list.get(0);
            String key = e.text().trim();

            list = element.getElementsByTag("td");
            if (list.size() != 1) {
                log.warn("Corrupted result page for domain '" + domainName + "'");
                continue;
            }

            e = list.get(0);
            String value = e.text().trim();

            System.out.println(String.format("'%s' = '%s'", key, value));

        }
    }
}

