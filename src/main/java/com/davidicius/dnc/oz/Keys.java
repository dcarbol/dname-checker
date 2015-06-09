package com.davidicius.dnc.oz;

import java.util.*;

public class Keys {
    private static String[] cars = {};
    private static String[] services = {"truhlarstvi", "ubytovani", "letenky", "bydleni", "hotel", "restaurace", "matrace", "parfemy", "virivka", "virivky"};
    private static String[] media = {};

    public static Set<String> carsSet = new HashSet<String>();
    static {
        carsSet.addAll(Arrays.asList("avia", "alfa romeo", "audi", "austin", "bmw", "bentley", "cadillac", "citroen", "civic", "daf", "daihatsu", "dacia", "dodge", "fabia", "felicia", "fiat", "ford", "honda", "hyundai", "chevrolet", "daimler", "chrysler", "tatra", "iveco", "jawa", "jeep",  "hummer",
                "lincoln", "jaguar", "lexus", "liaz", "lamborghini", "man", "mazda", "mercedes", "mitsubishi", "mustang", "nissan", "opel", "octavia", "porsche", "ferrari",  "passat", "peugeot", "renault", "royce", "suzuki", "subaru", "volvo", "wrangler",  "saab", "skoda", "seat", "toyota", "wolksvagen", "landrover"));
    }

    public enum AREA {
        CARS, SERVICES, MEDIA
    }

    private static Map<AREA, String[]> map = new HashMap<AREA, String[]>();
    static {
        map.put(AREA.CARS, cars);
        map.put(AREA.SERVICES, services);
        map.put(AREA.MEDIA, media);
    }

    public static Set<String> wordsForAreaExcept(AREA exceptArea) {
        Set<String> result = new HashSet<String>(64);
        for (AREA a : AREA.values()) {
            if (a.equals(exceptArea)) continue;

            String[] k = map.get(a);
            result.addAll(Arrays.asList(k));
        }

        return result;
    }

}
