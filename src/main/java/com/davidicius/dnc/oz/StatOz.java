package com.davidicius.dnc.oz;

public class StatOz {
    private String name;

    public StatOz(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatOz statOz = (StatOz) o;

        if (!name.equals(statOz.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
