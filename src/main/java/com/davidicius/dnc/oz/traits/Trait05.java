package com.davidicius.dnc.oz.traits;

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

    Pattern pattern01 = Pattern.compile("klementa\\s+839");
    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null) return false;

//        if (pattern01.matcher(page.toLowerCase()).find()) {
//            return true;
//        }

        Document doc = Jsoup.parse(page);
        if (doc == null) {
            log.warn("Cannot parse page for domain: " + domain.getProperty("name"));
            return false;
        }

        Elements c = doc.select("title");
        for (Element e : c) {
            String title = e.text();
            title = normalizePage(title);

            if (title.contains(domain.getProperty("name").toString())) {
                continue;
            }

            // @todo Title obsahuje nazev OZ...
            if (title.contains("skoda")) {
                return true;
            }
        }

        return false;
    }
}
