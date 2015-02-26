package com.davidicius.dnc.oz.traits;

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
        return "Links";
    }

//    Pattern pattern01 = Pattern.compile("klementa\\s+839");
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

        Elements c = doc.select("a");
        for (Element e : c) {
            String href = e.attr("href");
            try {
                URL url = new URL(href);
                String host = url.getHost().toLowerCase();
                if (host.startsWith("www.")) host = host.substring("www.".length());

                //@todo
                if (host.equals("auto-skoda.cz")) return true;
                if (host.equals("skoda-auto.cz")) return true;
                if (host.equals("skoda-auto.com")) return true;
                if (host.endsWith(".auto-skoda.cz")) return true;
                if (host.endsWith(".skoda-auto.cz")) return true;
                if (host.equals("skoda.cz")) return true;
                if (host.equals("skoda-auto.sk")) return true;

            } catch (MalformedURLException e1) {
            }
        }


        return false;
    }
}
