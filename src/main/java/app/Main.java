package app;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Program entry point: download data, find correlations, write index.html. */
public class Main {

    public static void main(String[] args) throws Exception {
        // Step 1: download (or read from cache) every series and turn it into yearly changes.
        System.out.println("Step 1: loading " + Series.ALL.size() + " series from Statistics Estonia");
        StatClient client = new StatClient();
        Map<Series, TreeMap<Integer, Double>> changes = new LinkedHashMap<>();
        for (Series series : Series.ALL) {
            try {
                TreeMap<Integer, Double> values = client.load(series);
                changes.put(series, Correlations.yearOverYear(values));
                System.out.printf("  %-18s %d years (%d-%d)%n", series.id(), values.size(), values.firstKey(), values.lastKey());
            } catch (Exception e) {
                System.out.println("  SKIPPED " + series.id() + ": " + e.getMessage());
            }
        }

        // Step 2: compare every pair and keep the ten strongest surprising ones.
        System.out.println("Step 2: comparing pairs");
        List<Correlations.Pair> top = Correlations.topPairs(changes, 10);
        for (Correlations.Pair pair : top) {
            System.out.printf("  r = %+.2f  %s  <->  %s  (%d years)%n",
                    pair.r(), pair.a().name(), pair.b().name(), pair.years().size());
        }

        // Step 3: write the web page.
        Path output = Path.of("index.html");
        HtmlReport.write(top, output);
        System.out.println("Step 3: done! Open " + output.toAbsolutePath() + " in your browser.");
    }
}
