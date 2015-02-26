package com.davidicius.dnc.oz;

import com.davidicius.dnc.oz.traits.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TraitsFactory {
    public static final TraitsFactory INSTANCE = new TraitsFactory();
    private Map<String, Trait> traits = new LinkedHashMap<String, Trait>();

    static {
        INSTANCE.register(new Trait00());
        INSTANCE.register(new Trait01());
        INSTANCE.register(new Trait02());
        INSTANCE.register(new Trait03());
        INSTANCE.register(new Trait04());
        INSTANCE.register(new Trait05());
        INSTANCE.register(new Trait06());
        INSTANCE.register(new Trait07());
        INSTANCE.register(new Trait08());
        INSTANCE.register(new Trait09());
        INSTANCE.register(new Trait10());
        INSTANCE.register(new Trait11());
        INSTANCE.register(new Trait12());
        INSTANCE.register(new Trait13());
        INSTANCE.register(new Trait14());
        INSTANCE.register(new Trait15());
    }

    public void register(Trait trait) {
        traits.put(trait.getId(), trait);
    }

    public Collection<Trait> traits() {
        return traits.values();
    }

    public Trait getTrait(String traitId) {
        return traits.get(traitId);
    }
}
