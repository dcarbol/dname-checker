package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trait11 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait11.class);

    public String getId() {
        return "11";
    }

    public String getName() {
        return "SContent";
    }

    private static String[] BAD = {"bmw", "citroen", "dacia", "fiat", "ford", "honda", "hyundai", "chevrolet", "mazda",  "tatra", "austin", "jawa",
            "mercedes", "mitsubishi", "opel", "peugeot", "renault", "suzuki", "subaru", "volvo", "alfa romeo", "casino", "automaty"};

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;
        if (page == null) return false;

        page = normalizePage(page);
        if (page.contains(" sex")) return true;
        if (page.contains(" porno")) return true;

        int count = 0;
        for (String key : BAD) {
            int start = 0;
            while (start < page.length()) {
                int index = page.indexOf(key, start);
                if (index >= 0) {
                    boolean before = index - 1 >= 0 && "=; ,\">".indexOf(page.charAt(index - 1)) >= 0;
                    boolean after = index + key.length() < page.length() && "=; ,\"<".indexOf(page.charAt(index + key.length() )) >= 0;
                    if (before && after) {
                        count++;
                    }

                    start = index + 1;
                } else {
                    break;
                }
            }
        }

        //@todo - spatna slova jsou ay na strance O Nas :(    eskoda-shop.cz
        return count >= 2;
    }
}
