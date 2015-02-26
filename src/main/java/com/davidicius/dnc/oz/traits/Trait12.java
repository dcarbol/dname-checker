package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trait12 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait12.class);

    public String getId() {
        return "12";
    }

    public String getName() {
        return "GOwner";
    }

    public static String[] NAMES = {"jiri", "jan", "miroslav", "vladimir"};

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null) return false;

        String owner = domain.getProperty("owner");
        if (owner == null) return false;

        owner = normalizePage(owner);
        //@todo
        if (owner.contains("skoda")) {
            return true;
        }

        //@todo Hledat jmena ve strance //manazerskeporadenstvi-jiriskoda.cz
        page = normalizePage(page);
        int index = 0;
        int delta = 40;
        while (index < page.length()) {
            index = page.indexOf("skoda", index);
            if (index >= 0) {
                int from = Math.max(0, index - delta);
                int to = Math.min(index + delta, page.length());
                String part = page.substring(from, to);

                for (String key : NAMES) {
                    int keyIndex = part.indexOf(key);
                    if (keyIndex >= 0) {
                        if (keyIndex < 40 && keyIndex >= 40- (key.length() + 2)) {
                            return true;
                        }

                        if (keyIndex >= 40 + "skoda".length() && keyIndex <= 40 + "skoda".length() + 2 ) {
                            return true;
                        }
                    }
                }

                if (part.contains("roman") && !part.contains("new roman")) {
                    return true;
                }

                index++;
            } else {
                break;
            }
        }

        return false;
    }
}
