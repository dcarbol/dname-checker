package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class Trait09 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait09.class);

    public String getId() {
        return "09";
    }

    public String getName() {
        return "SURL";
    }

    Pattern pattern01 = Pattern.compile("w.*\\d+\\..+\\..+");

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;

        String dh = domain.getProperty("name");
        if (dh.startsWith("www.")) dh = dh.substring("www.".length());

        if (dh.startsWith("www") || dh.startsWith("www-")) {
            return true;
        }

        if (pattern01.matcher(dh).matches()) {
            return true;
        }

        String f = domain.getProperty("forward");
        if (f != null && !f.trim().equals("")) {
            URL furl;
            try {
                furl = new URL(f.trim());
                String fh = furl.getHost();

                if (pattern01.matcher(fh).matches()) {
                    return true;
                }

            } catch (MalformedURLException e) {
            }
        }


        // @Todo- OZ nezev slova
        String[] keywords = {"skoda"};
        String[] parts = dh.split("-");

        double coef = 0;
        for (String part : parts) {
            int usedChars = 0;
            for (String key : keywords) {
                if (part.contains(key)) {
                    usedChars += key.length();
                }
            }

            double c = usedChars * 1.0 / part.length();
            coef += c;
        }

        coef = coef / parts.length;
        return coef < 0.15;
    }
}
