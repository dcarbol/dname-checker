package com.davidicius.dnc.oz;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class OZ {
    private String name;
    private Set<String> goodDomains = new HashSet<String>();
    private String goodWords = "";
    private Set<String> competitors;
    private Keys.AREA area;

    public OZ(String name,Keys.AREA area) {
        this.name = name;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void addGoodDomain(String domain) {
        goodDomains.add(domain.toLowerCase().trim());
    }

    public Iterator<String> goodDomains() {
        return goodDomains.iterator();
    }

    public String isGoodDomain(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
            String host = url.getHost().toLowerCase();
            if (host.startsWith("www.")) host = host.substring("www.".length());

            for (String goodDomain : goodDomains) {
                if (host.equals(goodDomain)) return goodDomain;
                if (host.endsWith("." + goodDomain))  return "." + goodDomain;
            }

            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public void setCompetitors(Set<String> competitors) {
        this.competitors = competitors;
    }

    public Set<String> getCompetitors() {
        return competitors;
    }

    public Keys.AREA getArea() {
        return area;
    }

    public String getGoodWords() {
        return goodWords;
    }

    public void setGoodWords(String goodWords) {
        this.goodWords = goodWords;
    }
}
