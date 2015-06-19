package com.davidicius.dnc.oz;

import java.util.*;

public class OZFactory {
    static public OZFactory OZ = new OZFactory();

    private Map<String, OZ> list = new LinkedHashMap<String, com.davidicius.dnc.oz.OZ>();

    private OZFactory() {
    }

    public void add(OZ oz) {
        list.put(oz.getName().toLowerCase().trim(), oz);
    }

    public OZ find(String oz) {
        oz = oz.toLowerCase().trim();
        return list.get(oz);
    }

    // skoda renault mercedes citroen bmw hyundai honda peugeot ford
    // bravo
    static {
        {
            OZ skoda = new OZ("skoda", Keys.AREA.CARS);
            skoda.addGoodDomain("auto-skoda.cz");
            skoda.addGoodDomain("skoda-auto.sk");
            skoda.addGoodDomain("skoda-auto.cz");
            skoda.addGoodDomain("skoda-auto.com");
            skoda.addGoodDomain("skoda.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("skoda", "audi", "seat", "volksvagen", "fabia", "octavia", "felicia", "passat", "porsche"));
            skoda.setCompetitors(ss);

            OZ.add(skoda);
        }

        {
            OZ fabia = new OZ("fabia", Keys.AREA.CARS);
            fabia.addGoodDomain("fabia.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("skoda", "fabia", "audi", "seat", "volksvagen", "octavia"));
            fabia.setCompetitors(ss);

            OZ.add(fabia);
        }

        {
            OZ octavia = new OZ("octavia", Keys.AREA.CARS);
            octavia.addGoodDomain("octavia.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("octavia", "skoda", "fabia", "audi", "seat", "volksvagen", "felicia"));
            octavia.setCompetitors(ss);

            OZ.add(octavia);
        }

        {
            OZ octavia = new OZ("passat", Keys.AREA.CARS);
            octavia.addGoodDomain("passat.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("passat", "skoda", "audi", "seat", "volksvagen", "porsche"));
            octavia.setCompetitors(ss);

            OZ.add(octavia);
        }

        {
            OZ octavia = new OZ("felicia", Keys.AREA.CARS);
            octavia.addGoodDomain("felicia.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("felicia", "octavia", "skoda", "fabia", "audi", "seat", "wolksvagen"));
            octavia.setCompetitors(ss);

            OZ.add(octavia);
        }

        {
            OZ volkswagen = new OZ("volkswagen", Keys.AREA.CARS);
            volkswagen.addGoodDomain("volkswagen.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("skoda", "audi", "seat", "wolksvagen", "octavia", "passat", "fabia", "felicia", "porsche", "tatra"));
            volkswagen.setCompetitors(ss);

            OZ.add(volkswagen);
        }
        {
            OZ renault = new OZ("renault", Keys.AREA.CARS);
            renault.addGoodDomain("renault.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("renault", "nissan", "dacia", "mitsubishi", "daimler"));
            renault.setCompetitors(ss);

            OZ.add(renault);
        }

        {
            OZ mercedes = new OZ("mercedes", Keys.AREA.CARS);
            mercedes.addGoodDomain("mercedes-benz.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("mercedes", "daimler", "chrysler"));
            mercedes.setCompetitors(ss);

            OZ.add(mercedes);
        }

        {
            OZ chrysler = new OZ("chrysler", Keys.AREA.CARS);
            chrysler.addGoodDomain("chrysler.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("daimler", "mercedes", "chrysler", "dodge", "jeep"));
            chrysler.setCompetitors(ss);

            OZ.add(chrysler);
        }

        {
            OZ lamborghini = new OZ("lamborghini", Keys.AREA.CARS);
            lamborghini.addGoodDomain("lamborghini.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("lamborghini", "audi"));
            lamborghini.setCompetitors(ss);

            OZ.add(lamborghini);
        }

        {
            OZ citroen = new OZ("citroen", Keys.AREA.CARS);
            citroen.addGoodDomain("citroen.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("citroen", "peugeot", "toyota"));
            citroen.setCompetitors(ss);

            OZ.add(citroen);
        }

        {
            OZ peugeot = new OZ("peugeot", Keys.AREA.CARS);
            peugeot.addGoodDomain("peugeot.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("citroen", "peugeot", "toyota"));
            peugeot.setCompetitors(ss);

            OZ.add(peugeot);
        }

        {
            OZ bmw = new OZ("bmw", Keys.AREA.CARS);
            bmw.addGoodDomain("bmw.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("bmw"));
            bmw.setCompetitors(ss);

            OZ.add(bmw);
        }

        {
            OZ hyundai = new OZ("hyundai", Keys.AREA.CARS);
            hyundai.addGoodDomain("hyundai.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("hyundai", "kia", "mitsubishi", "chrysler"));
            hyundai.setCompetitors(ss);

            OZ.add(hyundai);
        }

        {
            OZ kia = new OZ("kia", Keys.AREA.CARS);
            kia.addGoodDomain("kia.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("hyundai", "kia", "mitsubishi", "chrysler"));
            kia.setCompetitors(ss);

            OZ.add(kia);
        }

        {
            OZ honda = new OZ("honda", Keys.AREA.CARS);
            honda.addGoodDomain("honda.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("honda", "civic"));
            honda.setCompetitors(ss);

            OZ.add(honda);
        }

        {
            OZ civic = new OZ("civic", Keys.AREA.CARS);
            civic.addGoodDomain("honda.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("civic", "honda"));
            civic.setCompetitors(ss);

            OZ.add(civic);
        }

        {
            OZ oz = new OZ("ford", Keys.AREA.CARS);
            oz.addGoodDomain("ford.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("ford", "mustang", "escort", "mazda", "lincoln", "wrangler"));
            oz.setCompetitors(ss);

            OZ.add(oz);
        }

        {
            OZ mondeo = new OZ("mondeo", Keys.AREA.CARS);
            mondeo.addGoodDomain("ford.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("ford", "mondeo"));
            mondeo.setCompetitors(ss);

            OZ.add(mondeo);
        }

        {
            OZ oz = new OZ("opel", Keys.AREA.CARS);
            oz.addGoodDomain("opel.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("opel", "daewoo", "chevrolet"));
            oz.setCompetitors(ss);

            OZ.add(oz);
        }

        {
            OZ oz = new OZ("audi", Keys.AREA.CARS);
            oz.addGoodDomain("audi.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("skoda", "audi", "seat", "wolksvagen", "porsche", "passat", "octavia"));
            oz.setCompetitors(ss);

            OZ.add(oz);
        }

        {
            OZ toyota = new OZ("toyota", Keys.AREA.CARS);
            toyota.addGoodDomain("toyota.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("citroen", "peugeot", "toyota", "subaru", "lexus"));
            toyota.setCompetitors(ss);

            OZ.add(toyota);
        }

        {
            OZ suzuki = new OZ("suzuki", Keys.AREA.CARS);
            suzuki.addGoodDomain("suzuki.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("suzuki", "mazda", "mitsubishi", "restaurace", "subaru"));
            suzuki.setCompetitors(ss);

            OZ.add(suzuki);
        }

        {
            OZ subaru = new OZ("subaru", Keys.AREA.CARS);
            subaru.addGoodDomain("subaru.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("suzuki", "mazda", "mitsubishi", "restaurace", "subaru", "toyota", "daihatsu"));
            subaru.setCompetitors(ss);

            OZ.add(subaru);
        }
        {
            OZ daihatsu = new OZ("daihatsu", Keys.AREA.CARS);
            daihatsu.addGoodDomain("daihatsu.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("daihatsu"));
            daihatsu.setCompetitors(ss);

            OZ.add(daihatsu);
        }

        {
            OZ porsche = new OZ("porsche", Keys.AREA.CARS);
            porsche.addGoodDomain("porsche.cz");
            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("porsche", "skoda", "audi", "seat", "wolksvagen", "fabia", "bentley", "passat"));
            porsche.setCompetitors(ss);

            OZ.add(porsche);
        }

        {
            OZ nissan = new OZ("nissan", Keys.AREA.CARS);
            nissan.addGoodDomain("nissan.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("renault", "nissan", "dacia", "mitsubishi", "daimler"));
            nissan.setCompetitors(ss);

            OZ.add(nissan);
        }

        {
            OZ mazda = new OZ("mazda", Keys.AREA.CARS);
            mazda.addGoodDomain("mazda.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
//            ss.removeAll(Arrays.asList("suzuki", "mazda", "mitsubishi", "subaru"));
            ss.removeAll(Arrays.asList("suzuki", "mazda", "mitsubishi", "subaru"));
            mazda.setCompetitors(ss);

            OZ.add(mazda);
        }

        {
            OZ jeep = new OZ("jeep", Keys.AREA.CARS);
            jeep.addGoodDomain("jeep.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("jeep", "chrysler", "daimler", "fiat", "dodge", "ubytovani", "wrangler"));
            jeep.setCompetitors(ss);

            OZ.add(jeep);
        }

        {
            OZ wrangler = new OZ("wrangler", Keys.AREA.CARS);
            wrangler.addGoodDomain("jeep.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("jeep", "chrysler", "daimler", "fiat", "dodge", "wrangler"));
            wrangler.setCompetitors(ss);

            OZ.add(wrangler);
        }

        {
            OZ daimler = new OZ("daimler", Keys.AREA.CARS);
            daimler.addGoodDomain("daimler.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("jeep", "chrysler", "daimler", "fiat", "dodge", "wrangler"));
            daimler.setCompetitors(ss);

            OZ.add(daimler);
        }

        {
            OZ fiat = new OZ("fiat", Keys.AREA.CARS);
            fiat.addGoodDomain("fiat.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
//            ss.removeAll(Arrays.asList("jeep", "chrysler", "daimler", "fiat", "dodge", "wrangler"));
            ss.removeAll(Arrays.asList("fiat", "lancia", "jeep", "alfa romeo", "iveco", "renault", "chrysler", "ferrari"));
            fiat.setCompetitors(ss);

            OZ.add(fiat);
        }

        {
            OZ mitsubishi = new OZ("mitsubishi", Keys.AREA.CARS);
            mitsubishi.addGoodDomain("mitsubishi.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("mitsubishi", "daimler"));
            mitsubishi.setCompetitors(ss);

            OZ.add(mitsubishi);
        }

        {
            OZ ferrari = new OZ("ferrari", Keys.AREA.CARS);
            ferrari.addGoodDomain("ferrari.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("ferrari", "fiat"));
            ferrari.setCompetitors(ss);

            OZ.add(ferrari);
        }

        {
            OZ hummer = new OZ("hummer", Keys.AREA.CARS);
            hummer.addGoodDomain("hummer.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("hummer", "ubytovani"));
            hummer.setCompetitors(ss);

            OZ.add(hummer);
        }

        {
            OZ jaguar = new OZ("jaguar", Keys.AREA.CARS);
            jaguar.addGoodDomain("jaguar.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("jaguar", "daimler", "landrover"));
            jaguar.setCompetitors(ss);

            OZ.add(jaguar);
        }

        {
            OZ chevrolet = new OZ("chevrolet", Keys.AREA.CARS);
            chevrolet.addGoodDomain("chevrolet.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("chevrolet", "opel", "daewoo"));
            chevrolet.setCompetitors(ss);

            OZ.add(chevrolet);
        }

        {
            OZ landrover = new OZ("landrover", Keys.AREA.CARS);
            landrover.addGoodDomain("landrover.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("landrover", "jaguar"));
            landrover.setCompetitors(ss);

            OZ.add(landrover);
        }

        {
            OZ saab = new OZ("saab", Keys.AREA.CARS);
            saab.addGoodDomain("saabczech.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("saab"));
            saab.setCompetitors(ss);

            OZ.add(saab);
        }

        {
            OZ lexus = new OZ("lexus", Keys.AREA.CARS);
            lexus.addGoodDomain("lexus.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("lexus", "toyota", "seat"));
            lexus.setCompetitors(ss);

            OZ.add(lexus);
        }

        {
            OZ lexus = new OZ("cadillac", Keys.AREA.CARS);
            lexus.addGoodDomain("cadillac.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("cadillac"));
            lexus.setCompetitors(ss);

            OZ.add(lexus);
        }

        {
            OZ royce = new OZ("royce", Keys.AREA.CARS);
            royce.addGoodDomain("rolls-royce.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("royce", "bmw"));
            royce.setCompetitors(ss);

            OZ.add(royce);
        }

        {
            OZ bentley = new OZ("bentley", Keys.AREA.CARS);
            bentley.addGoodDomain("bentley.cz");

            Set<String> ss = new HashSet<String>(Keys.carsSet);
            ss.removeAll(Arrays.asList("bentley", "royce"));
            bentley.setCompetitors(ss);

            OZ.add(bentley);
        }

        {
            OZ bravo = new OZ("bravo", Keys.AREA.MEDIA);
//            skoda.addGoodDomain("auto-skoda.cz");
//            skoda.addGoodDomain("skoda-auto.sk");
//            skoda.addGoodDomain("skoda-auto.cz");
//            skoda.addGoodDomain("skoda-auto.com");
//            skoda.addGoodDomain("skoda.cz");

            Set<String> ss = new HashSet<String>();
            bravo.setCompetitors(ss);

            OZ.add(bravo);
        }

    }
}
