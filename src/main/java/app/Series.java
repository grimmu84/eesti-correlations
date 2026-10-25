package app;

import java.util.List;
import java.util.Map;

/**
 * One yearly time series we want from Statistics Estonia.
 *
 * @param id       short name, also used as the cache file name
 * @param name     proper label shown on the chart
 * @param nickname playful name used in the funny chart titles
 * @param topic    pairs from the same topic are skipped, because their correlation is not surprising
 * @param table    path of the table under https://andmed.stat.ee/api/v1/en/stat/
 * @param picks    which value to take for a table variable, keyed by variable code or English name.
 *                 Variables not listed here use their first value (usually "Total" or "Whole country").
 */
public record Series(String id, String name, String nickname, String topic, String table, Map<String, String> picks) {

    /** The ~30 series this project downloads. Add your own lines here! */
    public static final List<Series> ALL = List.of(
            new Series("births", "Live births", "babies", "people",
                    "rahvastik/rahvastikunaitajad-ja-koosseis/demograafilised-pehinaitajad/RV030.PX", Map.of("Indicator", "1")),
            new Series("deaths", "Deaths (all causes)", "funerals", "mortality",
                    "rahvastik/rahvastikunaitajad-ja-koosseis/demograafilised-pehinaitajad/RV030.PX", Map.of("Indicator", "2")),
            new Series("abortions", "Induced abortions", "abortions", "people",
                    "rahvastik/rahvastikusundmused/abordid/RV173.px", Map.of("Type", "1")),
            new Series("marriages", "Marriages", "weddings", "love",
                    "rahvastik/rahvastikunaitajad-ja-koosseis/demograafilised-pehinaitajad/RV047.PX", Map.of("Indicator", "1")),
            new Series("divorces", "Divorces", "divorces", "love",
                    "rahvastik/rahvastikunaitajad-ja-koosseis/demograafilised-pehinaitajad/RV047.PX", Map.of("Indicator", "5")),
            new Series("immigration", "External immigration", "people moving in", "migration",
                    "rahvastik/rahvastikusundmused/ranne/RVR03.PX", Map.of("Indicator", "1", "Type of migration", "2")),
            new Series("emigration", "External emigration", "people moving out", "migration",
                    "rahvastik/rahvastikusundmused/ranne/RVR03.PX", Map.of("Indicator", "2", "Type of migration", "2")),
            new Series("suicides", "Deaths by suicide", "suicides", "mortality",
                    "rahvastik/rahvastikusundmused/surmad/RV56.PX", Map.of("Cause of death by ICD-10", "82")),
            new Series("alcohol-poisoning", "Deaths from alcohol poisoning", "alcohol poisonings", "mortality",
                    "rahvastik/rahvastikusundmused/surmad/RV56.PX", Map.of("Cause of death by ICD-10", "81")),
            new Series("traffic-deaths", "Deaths in transport accidents", "traffic deaths", "mortality",
                    "rahvastik/rahvastikusundmused/surmad/RV56.PX", Map.of("Cause of death by ICD-10", "75")),
            new Series("drownings", "Accidental drownings", "drownings", "mortality",
                    "rahvastik/rahvastikusundmused/surmad/RV56.PX", Map.of("Cause of death by ICD-10", "77")),
            new Series("baltic-fish", "Baltic Sea fish catch", "Baltic fish caught", "fish",
                    "majandus/kalandus/KA20.PX", Map.of()),
            new Series("sprats", "Baltic sprat catch", "sprats caught", "fish",
                    "majandus/kalandus/KA20.PX", Map.of("Species", "7")),
            new Series("peipsi-fish", "Lake Peipsi fish catch", "Lake Peipsi fish", "fish",
                    "majandus/kalandus/KA30.PX", Map.of("Waterbody", "2")),
            new Series("milk", "Milk purchased from farms", "milk", "farming",
                    "majandus/pellumajandus/pellumajandussaaduste-tootmine/loomakasvatussaaduste-tootmine/PM181.px", Map.of("Indicator", "PRO1")),
            new Series("milk-price", "Average price of milk", "the price of milk", "farming",
                    "majandus/pellumajandus/pellumajandussaaduste-tootmine/loomakasvatussaaduste-tootmine/PM181.px", Map.of("Indicator", "AVP")),
            new Series("windfall", "Forest destroyed by windfall", "trees blown over", "forest",
                    "keskkond/loodusvarad-ja-nende-kasutamine/metsavaru/KK513.PX", Map.of("Reason for destruction", "7")),
            new Series("game-damage", "Forest destroyed by game animals", "trees eaten by moose", "forest",
                    "keskkond/loodusvarad-ja-nende-kasutamine/metsavaru/KK513.PX", Map.of("Reason for destruction", "3")),
            new Series("electricity", "Electricity production", "electricity", "energy",
                    "majandus/energeetika/energia-tarbimine-ja-tootmine/aastastatistika/KE033.PX", Map.of("Indicator", "PROD_E")),
            new Series("oil-shale", "Oil shale burned in power plants", "oil shale burned", "energy",
                    "majandus/energeetika/energia-tarbimine-ja-tootmine/aastastatistika/KE033.PX",
                    Map.of("Indicator", "CONS_FUEL_T", "Type of fuel/energy", "S2100_EE")),
            new Series("gas-price", "Natural gas price for enterprises", "the gas bill", "energy",
                    "majandus/energeetika/energia-tarbimine-ja-tootmine/aastastatistika/KE08.PX",
                    Map.of("Type of fuel/energy", "G3000X3100_3200")),
            new Series("cars", "Registered passenger cars", "cars", "cars",
                    "majandus/transport/registreeritud-liiklusvahendid/TS32.PX", Map.of()),
            new Series("rail-freight", "Goods transported by rail", "rail freight", "economy",
                    "majandus/transport/raudteetransport/TS1420.px", Map.of("Indicator", "FREIGHT")),
            new Series("taxes", "State budget tax revenue", "taxes collected", "economy",
                    "majandus/rahandus/valitsemissektori-rahandus/maksud/RR01.PX", Map.of()),
            // "Going out" series all crashed in 2020, so pairing them with each other only rediscovers COVID.
            new Series("rail-passengers", "Railway passengers", "train riders", "going out",
                    "majandus/transport/raudteetransport/TS1420.px", Map.of("Indicator", "PASSENGERS")),
            new Series("bus-passengers", "Bus passengers", "bus riders", "going out",
                    "majandus/transport/maanteetransport/TS542.px", Map.of()),
            new Series("airport", "Tallinn Airport passengers", "air travellers", "going out",
                    "majandus/transport/ehutransport/TS204.px", Map.of()),
            new Series("theatre", "Theatre attendance", "theatre-goers", "going out",
                    "sotsiaalelu/kultuur/teater/KU091.PX", Map.of("Indicator", "5")),
            new Series("cinema", "Cinema attendance", "cinema-goers", "going out",
                    "sotsiaalelu/kultuur/film/KU07.PX", Map.of("Indicator", "7")),
            new Series("museums", "Museum attendance", "museum visitors", "going out",
                    "sotsiaalelu/kultuur/muuseumid/KU05.PX", Map.of("Indicator", "13")),
            new Series("library-loans", "Public library loans", "library books borrowed", "books",
                    "sotsiaalelu/kultuur/raamatukogud/KU01.PX", Map.of("Indicator", "6")),
            new Series("books", "Book titles published", "new books", "books",
                    "sotsiaalelu/kultuur/trukitoodang/KU12.PX", Map.of())
    );
}
