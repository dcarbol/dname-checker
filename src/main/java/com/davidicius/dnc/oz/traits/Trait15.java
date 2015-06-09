package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.DbService;
import com.davidicius.dnc.oz.OZ;
import com.davidicius.dnc.oz.OzWorker;
import com.davidicius.dnc.oz.TraitsFactory;
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

    public boolean hasTrait(Vertex domain, String page, Document document, OZ oz) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null || document == null) return false;


        String domainName = domain.getProperty("name");
        Elements c = document.select("a");
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
                        if (TraitsFactory.INSTANCE.isVERBOSE()) {
                            log.info(String.format("Domain %s, trait %s: HREF to bad host '%s' for domain '%s'", domain.getProperty("name"), getName(), host, OzWorker.toStringDOM(vertex)));
                        }

                        count++;
                    }
                }

            } catch (MalformedURLException e1) {
            }
        }


        return count > 0;
    }
}
