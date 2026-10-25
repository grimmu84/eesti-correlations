package app;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Finds pairs of series whose year-over-year changes move together.
 *
 * Why changes and not raw values? Almost everything in Estonia grew or shrank steadily since the 1990s,
 * so raw values of unrelated things correlate just because both have a trend. Comparing
 * "how much did it change this year" removes that trend and leaves the more interesting wiggles.
 */
public class Correlations {

    /** Pairs need at least this many years in common, otherwise a high correlation is too easy. */
    static final int MIN_YEARS = 12;
    /** At most this many top pairs may contain the same series, so the list is not all about one thing. */
    static final int MAX_PER_SERIES = 2;

    /** A pair of series and how strongly their yearly changes correlate. */
    public record Pair(Series a, Series b, double r, List<Integer> years, List<Double> changesA, List<Double> changesB) {
    }

    /** Percent change from the previous year, e.g. 100 -> 110 gives +10.0. Only for back-to-back years. */
    public static TreeMap<Integer, Double> yearOverYear(TreeMap<Integer, Double> values) {
        TreeMap<Integer, Double> changes = new TreeMap<>();
        for (Map.Entry<Integer, Double> entry : values.entrySet()) {
            Double previous = values.get(entry.getKey() - 1);
            if (previous != null && previous != 0) {
                changes.put(entry.getKey(), (entry.getValue() - previous) / Math.abs(previous) * 100);
            }
        }
        return changes;
    }

    /** Pearson correlation: +1 = always move together, -1 = always opposite, 0 = no relation. */
    public static double pearson(List<Double> x, List<Double> y) {
        int n = x.size();
        double meanX = x.stream().mapToDouble(d -> d).average().orElse(0);
        double meanY = y.stream().mapToDouble(d -> d).average().orElse(0);
        double sumXY = 0, sumXX = 0, sumYY = 0;
        for (int i = 0; i < n; i++) {
            double dx = x.get(i) - meanX;
            double dy = y.get(i) - meanY;
            sumXY += dx * dy;
            sumXX += dx * dx;
            sumYY += dy * dy;
        }
        return sumXY / Math.sqrt(sumXX * sumYY);
    }

    /** Compares every series with every other one and returns the most surprising pairs. */
    public static List<Pair> topPairs(Map<Series, TreeMap<Integer, Double>> changesBySeries, int howMany) {
        List<Series> all = new ArrayList<>(changesBySeries.keySet());
        List<Pair> pairs = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                Series a = all.get(i);
                Series b = all.get(j);
                if (a.topic().equals(b.topic())) {
                    continue; // same topic = expected to be related, so not surprising
                }
                TreeMap<Integer, Double> changesA = changesBySeries.get(a);
                TreeMap<Integer, Double> changesB = changesBySeries.get(b);

                // Keep only the years that both series have.
                List<Integer> years = new ArrayList<>();
                List<Double> xs = new ArrayList<>();
                List<Double> ys = new ArrayList<>();
                for (Integer year : changesA.keySet()) {
                    if (changesB.containsKey(year)) {
                        years.add(year);
                        xs.add(changesA.get(year));
                        ys.add(changesB.get(year));
                    }
                }
                if (years.size() < MIN_YEARS) {
                    continue;
                }
                double r = pearson(xs, ys);
                if (!Double.isNaN(r)) {
                    pairs.add(new Pair(a, b, r, years, xs, ys));
                }
            }
        }

        // Strongest correlation first, whether it is positive or negative.
        pairs.sort(Comparator.comparingDouble((Pair p) -> Math.abs(p.r())).reversed());

        List<Pair> top = new ArrayList<>();
        Map<Series, Integer> timesUsed = new HashMap<>();
        for (Pair pair : pairs) {
            if (top.size() == howMany) {
                break;
            }
            if (timesUsed.getOrDefault(pair.a(), 0) < MAX_PER_SERIES
                    && timesUsed.getOrDefault(pair.b(), 0) < MAX_PER_SERIES) {
                top.add(pair);
                timesUsed.merge(pair.a(), 1, Integer::sum);
                timesUsed.merge(pair.b(), 1, Integer::sum);
            }
        }
        return top;
    }
}
