package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.DbService;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class Trait15 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait15.class);

    public String getId() {
        return "15";
    }

    public String getName() {
        return "BLinks";
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

        String domainName = domain.getProperty("name");
        Elements c = doc.select("a");
        int count = 0;
        for (Element e : c) {
            String href = e.attr("href");
            try {
                URL url = new URL(href);
                String host = url.getHost().toLowerCase();
                if (host.startsWith("www.")) host = host.substring("www.".length());

                if (domainName.equals(host)) continue;

                Vertex vertex = DbService.db.getDomain(host);
                if (vertex != null) {
                    Iterable<Edge> edges = vertex.getEdges(Direction.OUT, "bad");
                    boolean bad = edges.iterator().hasNext();

                    if (bad) {
                        count++;
                    }
                }

            } catch (MalformedURLException e1) {
            }
        }


        return count > 0;
    }
}
