package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class Trait05 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait05.class);

    public String getId() {
        return "05";
    }

    public String getName() {
        return "Title";
    }

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null || document == null) return false;
        if (oz == null) {
            log.warn(String.format("Domain %s, trait %s: Domain has no valid OZ'", domain.getProperty("name"), getName()));
            return false;
        }

        String domainName = domain.getProperty("name").toString().toLowerCase();
        Elements c = document.select("title");
        for (Element e : c) {
            String title = e.text();
            title = normalizePage(title);

            if (title.contains(domainName)) {
                continue;
            }

            if (title.contains(oz.getName())) {
                if (TraitsFactory.INSTANCE.isVERBOSE()) {
                    log.info(String.format("Domain %s, trait %s: Title '%s' contains OZ '%s''", domain.getProperty("name"), getName(), title, oz.getName()));
                }

                return true;
            }
        }

        return false;
    }
}
