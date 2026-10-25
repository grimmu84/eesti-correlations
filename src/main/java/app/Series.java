package app;

import java.util.List;
import java.util.Map;

/**
 * One yearly time series we want from Statistics Estonia.
 *
 * @param id       short name, also used as the cache file name
 * @param name     proper label shown on the chart (in Estonian)
 * @param nickname playful name used in the funny chart titles (in Estonian)
 * @param topic    pairs from the same topic are skipped, because their correlation is not surprising
 * @param table    path of the table under https://andmed.stat.ee/api/v1/en/stat/
 * @param picks    which value to take for a table variable, keyed by variable code or English name.
 *                 Variables not listed here use their first value (usually "Total" or "Whole country").
 */
public record Series(String id, String name, String nickname, String topic, String table, Map<String, String> picks) {

    /** The ~30 series this project downloads. Add your own lines here! */
    public static final List<Series> ALL = List.of(
            new Series("births", "Elussünnid", "beebid", "people",
                    "rahvastik/rahvastikunaitajad-ja-koosseis/demograafilised-pehinaitajad/RV030.PX", Map.of("Indicator", "1")),
            new Series("deaths", "Surmad (kõik põhjused)", "matused", "mortality",
                    "rahvastik/rahvastikunaitajad-ja-koosseis/demograafilised-pehinaitajad/RV030.PX", Map.of("Indicator", "2")),
            new Series("abortions", "Indutseeritud abordid", "abordid", "people",
                    "rahvastik/rahvastikusundmused/abordid/RV173.px", Map.of("Type", "1")),
            new Series("marriages", "Abielud", "pulmad", "love",
                    "rahvastik/rahvastikunaitajad-ja-koosseis/demograafilised-pehinaitajad/RV047.PX", Map.of("Indicator", "1")),
            new Series("divorces", "Lahutused", "lahutused", "love",
                    "rahvastik/rahvastikunaitajad-ja-koosseis/demograafilised-pehinaitajad/RV047.PX", Map.of("Indicator", "5")),
            new Series("immigration", "Sisseränne välisriikidest", "sisserändajad", "migration",
                    "rahvastik/rahvastikusundmused/ranne/RVR03.PX", Map.of("Indicator", "1", "Type of migration", "2")),
            new Series("emigration", "Väljaränne välisriikidesse", "väljarändajad", "migration",
                    "rahvastik/rahvastikusundmused/ranne/RVR03.PX", Map.of("Indicator", "2", "Type of migration", "2")),
            new Series("suicides", "Enesetapud", "enesetapud", "mortality",
                    "rahvastik/rahvastikusundmused/surmad/RV56.PX", Map.of("Cause of death by ICD-10", "82")),
            new Series("alcohol-poisoning", "Surmad alkoholimürgistusest", "alkoholimürgistused", "mortality",
                    "rahvastik/rahvastikusundmused/surmad/RV56.PX", Map.of("Cause of death by ICD-10", "81")),
            new Series("traffic-deaths", "Transpordiõnnetustes hukkunud", "liiklussurmad", "mortality",
                    "rahvastik/rahvastikusundmused/surmad/RV56.PX", Map.of("Cause of death by ICD-10", "75")),
            new Series("drownings", "Uppumised", "uppumised", "mortality",
                    "rahvastik/rahvastikusundmused/surmad/RV56.PX", Map.of("Cause of death by ICD-10", "77")),
            new Series("baltic-fish", "Läänemere kalapüük", "Läänemere kala", "fish",
                    "majandus/kalandus/KA20.PX", Map.of()),
            new Series("sprats", "Läänemere kilupüük", "kilud", "fish",
                    "majandus/kalandus/KA20.PX", Map.of("Species", "7")),
            new Series("peipsi-fish", "Peipsi järve kalapüük", "Peipsi kala", "fish",
                    "majandus/kalandus/KA30.PX", Map.of("Waterbody", "2")),
            new Series("milk", "Piima kokkuost", "piim", "farming",
                    "majandus/pellumajandus/pellumajandussaaduste-tootmine/loomakasvatussaaduste-tootmine/PM181.px", Map.of("Indicator", "PRO1")),
            new Series("milk-price", "Piima keskmine hind", "piima hind", "farming",
                    "majandus/pellumajandus/pellumajandussaaduste-tootmine/loomakasvatussaaduste-tootmine/PM181.px", Map.of("Indicator", "AVP")),
            new Series("windfall", "Tormimurrus hukkunud mets", "tormimurd", "forest",
                    "keskkond/loodusvarad-ja-nende-kasutamine/metsavaru/KK513.PX", Map.of("Reason for destruction", "7")),
            new Series("game-damage", "Ulukikahjustustes hukkunud mets", "põtrade näritud puud", "forest",
                    "keskkond/loodusvarad-ja-nende-kasutamine/metsavaru/KK513.PX", Map.of("Reason for destruction", "3")),
            new Series("electricity", "Elektritoodang", "elekter", "energy",
                    "majandus/energeetika/energia-tarbimine-ja-tootmine/aastastatistika/KE033.PX", Map.of("Indicator", "PROD_E")),
            new Series("oil-shale", "Elektrijaamades põletatud põlevkivi", "põletatud põlevkivi", "energy",
                    "majandus/energeetika/energia-tarbimine-ja-tootmine/aastastatistika/KE033.PX",
                    Map.of("Indicator", "CONS_FUEL_T", "Type of fuel/energy", "S2100_EE")),
            new Series("gas-price", "Maagaasi hind ettevõtetele", "gaasiarve", "energy",
                    "majandus/energeetika/energia-tarbimine-ja-tootmine/aastastatistika/KE08.PX",
                    Map.of("Type of fuel/energy", "G3000X3100_3200")),
            new Series("cars", "Registreeritud sõiduautod", "autod", "cars",
                    "majandus/transport/registreeritud-liiklusvahendid/TS32.PX", Map.of()),
            new Series("rail-freight", "Raudteel veetud kaup", "raudteekaup", "economy",
                    "majandus/transport/raudteetransport/TS1420.px", Map.of("Indicator", "FREIGHT")),
            new Series("taxes", "Riigieelarve maksutulu", "maksud", "economy",
                    "majandus/rahandus/valitsemissektori-rahandus/maksud/RR01.PX", Map.of()),
            // "Going out" series all crashed in 2020, so pairing them with each other only rediscovers COVID.
            new Series("rail-passengers", "Raudteereisijad", "rongisõitjad", "going out",
                    "majandus/transport/raudteetransport/TS1420.px", Map.of("Indicator", "PASSENGERS")),
            new Series("bus-passengers", "Bussireisijad", "bussisõitjad", "going out",
                    "majandus/transport/maanteetransport/TS542.px", Map.of()),
            new Series("airport", "Tallinna lennujaama reisijad", "lennureisijad", "going out",
                    "majandus/transport/ehutransport/TS204.px", Map.of()),
            new Series("theatre", "Teatrikülastused", "teatrisõbrad", "going out",
                    "sotsiaalelu/kultuur/teater/KU091.PX", Map.of("Indicator", "5")),
            new Series("cinema", "Kinokülastused", "kinoskäijad", "going out",
                    "sotsiaalelu/kultuur/film/KU07.PX", Map.of("Indicator", "7")),
            new Series("museums", "Muuseumikülastused", "muuseumikülastajad", "going out",
                    "sotsiaalelu/kultuur/muuseumid/KU05.PX", Map.of("Indicator", "13")),
            new Series("library-loans", "Rahvaraamatukogude laenutused", "laenutatud raamatud", "books",
                    "sotsiaalelu/kultuur/raamatukogud/KU01.PX", Map.of("Indicator", "6")),
            new Series("books", "Ilmunud raamatunimetused", "uued raamatud", "books",
                    "sotsiaalelu/kultuur/trukitoodang/KU12.PX", Map.of())
    );
}
