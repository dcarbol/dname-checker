package com.davidicius.dnc.oz.traits;

import com.davidicius.dnc.oz.Trait;
import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.util.regex.Pattern;

public abstract class AbstractTrait implements Trait {
    private static final Logger log = LoggerFactory.getLogger(AbstractTrait.class);

    protected boolean forceExists(Vertex domain) {
        String exists = domain.getProperty("exists");
        return !(exists == null || exists.equals("N") || exists.equals("?"));
    }

    protected boolean forceLoaded(Vertex domain, String page) {
        String loaded = domain.getProperty("loaded");
        if (loaded == null || loaded.equals("?") || loaded.equals("E")) {
//            log.warn(String.format("Trait '%s', domain '%s': 'loaded' property must not be ? or E", domain.getProperty("name"), getName()));
            return false;
        }

        return page != null;
    }

    protected String normalizePage(String s) {
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        noAccent = pattern.matcher(noAccent).replaceAll("");

        return noAccent.replaceAll("\\s+", " ").toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractTrait)) return false;

        AbstractTrait that = (AbstractTrait) o;

        return getId().equals(that.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
