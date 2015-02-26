package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class Trait02 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait02.class);

    public String getId() {
        return "02";
    }

    public String getName() {
        return "Forward";
    }

    Pattern pattern01 = Pattern.compile("http-equiv\\s*=\\s*\"refresh\"");

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;

        if (page != null) {
            if (pattern01.matcher(page.toLowerCase()).find()) {
                return true;
            }
        }

        String f = domain.getProperty("forward");
        if (f == null || f.trim().equals("")) return false;

        String domainName = domain.getProperty("name");

        try {
            URL furl = new URL(f.trim());
            String path = furl.getPath().toLowerCase();
            if (path.contains("suspendedpage")) {
                return true;
            }

            if (path.contains("non-existing-virtual")) {
                return true;
            }

            String fh = furl.getHost();
            if (fh.startsWith("www.")) fh = fh.substring("www.".length());

            String dh = domainName;
            if (dh.startsWith("www.")) dh = dh.substring("www.".length());

            //@todo - ignorovat forward na legalni stranky...
            // @toto / ignorovat forward na strankz STEJNEHO ownera
            if (fh.equals("skoda-auto.sk")) {
                return false;
            }

            if (fh.equals("skoda-auto.cz")) {
                return false;
            }

            if (fh.equals("skoda.cz")) {
                return false;
            }

            if (dh.equals(fh)) return false;

            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
