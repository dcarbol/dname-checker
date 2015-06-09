package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.OZFactory;
import com.davidicius.dnc.oz.TraitsFactory;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class Trait04 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait04.class);

    public String getId() {
        return "04";
    }

    public String getName() {
        return "GLinks";
    }

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null || document == null) return false;

        if (oz == null) {
            log.warn(String.format("Domain %s, trait %s: Domain has no valid OZ'", domain.getProperty("name"), getName()));
            return false;
        }

        Elements c = document.select("a");
        for (Element e : c) {
            String href = e.attr("href");

            String goodDomain = oz.isGoodDomain(href);
            if (goodDomain != null) {
                if (TraitsFactory.INSTANCE.isVERBOSE()) {
                    log.info(String.format("Domain %s, trait %s: <a href='%s'> references good domain '%s''", domain.getProperty("name"), getName(), href, goodDomain));
                }

                return true;
            }
        }

        return false;
    }
}
