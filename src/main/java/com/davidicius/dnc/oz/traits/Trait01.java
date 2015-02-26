package com.davidicius.dnc.oz.traits;

import com.tinkerpop.blueprints.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Trait01 extends AbstractTrait {
    private static final Logger log = LoggerFactory.getLogger(Trait01.class);

    public String getId() {
        return "01";
    }

    public String getName() {
        return "Park";
    }

    Pattern pattern01 = Pattern.compile("domena\\s+je\\s+registrovana");
    Pattern pattern02 = Pattern.compile("web\\s+neexistuje");
    Pattern pattern03 = Pattern.compile("domena\\s+je\\s+jiz\\s+registrovana");
    Pattern pattern04 = Pattern.compile("tato\\s+domena\\s+je\\s+zaregistrovana\\s+prostrednictvim\\s+domenoveho");
    Pattern pattern05 = Pattern.compile("domena\\s+na\\s+prodej");
    Pattern pattern06 = Pattern.compile("the\\s+domain\\s+name\\s+is\\s+registered");
    Pattern pattern07 = Pattern.compile("stranky\\s+v\\s+rekonstrukci");
    Pattern pattern08 = Pattern.compile("kontaktujte\\s+na\\s+domeny");
    Pattern pattern09 = Pattern.compile("under\\s+construction");
    Pattern pattern10 = Pattern.compile("website\\s+is\\s+up\\s+and\\s+running");
    Pattern pattern11 = Pattern.compile("domena\\s+byla\\s+zaregistrovana");
    Pattern pattern12 = Pattern.compile("have\\s+permission\\s+to\\s+access");
    Pattern pattern13 = Pattern.compile("domain.{1,100}sale");
    Pattern pattern14 = Pattern.compile("server.{1,20}not\\s+found");
    Pattern pattern15 = Pattern.compile("this\\s+page\\s+is\\s+parked");
    Pattern pattern16 = Pattern.compile("domena\\s+je\\s+blokovana\\s+poskytovatelem");

    public boolean hasTrait(Vertex domain, String page) {
        if (!forceExists(domain)) return false;
        if (!forceLoaded(domain, page)) return false;

        page = normalizePage(page);
        if (pattern01.matcher(page).find()) {
            return true;
        }

        if (pattern02.matcher(page).find()) {
            return true;
        }

        if (pattern03.matcher(page).find()) {
            return true;
        }

        if (pattern04.matcher(page).find()) {
            return true;
        }

        if (pattern05.matcher(page).find()) {
            return true;
        }

        if (pattern06.matcher(page).find()) {
            return true;
        }

        if (pattern07.matcher(page).find()) {
            return true;
        }

        if (pattern08.matcher(page).find()) {
            return true;
        }

        if (pattern09.matcher(page).find()) {
            return true;
        }

        if (pattern10.matcher(page).find()) {
            return true;
        }

        if (pattern11.matcher(page).find()) {
            return true;
        }

        if (pattern12.matcher(page).find()) {
            return true;
        }

        if (pattern13.matcher(page).find()) {
            return true;
        }

        if (pattern14.matcher(page).find()) {
            return true;
        }

        if (pattern15.matcher(page).find()) {
            return true;
        }

        if (pattern16.matcher(page).find()) {
            return true;
        }

        if (page.contains("http://domainfwding.com")) {
            return true;
        }

        if (page.length() < 100 && !(page.contains("refresh") && page.contains("http-equiv"))) {
            return true;
        }

        return false;
    }
}

