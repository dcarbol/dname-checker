package com.davidicius.dnc.oz;

public class Domain {
    String name;
    String owner;
    String ozName;
    String traits;
    boolean exists;
    boolean loaded;
    boolean czDomeny;
    boolean czTom;
    boolean top1m;
    boolean skDomeny;
    boolean bad;
    boolean good;
    boolean neutral;

    public Domain(String name, String owner, String ozName, String traits, boolean exists, boolean loaded, boolean czDomeny, boolean czTom, boolean top1m, boolean skDomeny, boolean bad, boolean good, boolean neutral) {
        this.name = name;
        this.owner = owner;
        this.ozName = ozName;
        this.traits = traits;
        this.exists = exists;
        this.loaded = loaded;
        this.czDomeny = czDomeny;
        this.czTom = czTom;
        this.top1m = top1m;
        this.skDomeny = skDomeny;
        this.bad = bad;
        this.good = good;
        this.neutral = neutral;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getOzName() {
        return ozName;
    }

    public String getTraits() {
        return traits;
    }

    public boolean isExists() {
        return exists;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isCzDomeny() {
        return czDomeny;
    }

    public boolean isCzTom() {
        return czTom;
    }

    public boolean isTop1m() {
        return top1m;
    }

    public boolean isSkDomeny() {
        return skDomeny;
    }

    public boolean isBad() {
        return bad;
    }

    public boolean isGood() {
        return good;
    }

    public boolean isNeutral() {
        return neutral;
    }
}