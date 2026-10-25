package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Writes index.html: one Chart.js line chart per correlated pair. */
public class HtmlReport {

    private static final List<String> TOGETHER_TITLES = List.of(
            "%s and %s: separated at birth?",
            "Breaking: %s and %s spotted holding hands",
            "The secret link between %s and %s",
            "More %s, more %s. Suspicious.",
            "%s × %s: a love story",
            "%s and %s, dancing to the same beat");

    private static final List<String> OPPOSITE_TITLES = List.of(
            "%s vs. %s: the eternal rivalry",
            "More %s, less %s. Explain that.",
            "%s and %s simply cannot stand each other",
            "Up with %s, down with %s",
            "The seesaw of %s and %s");

    public static void write(List<Correlations.Pair> pairs, Path file) throws IOException {
        List<Map<String, Object>> charts = new ArrayList<>();
        for (int i = 0; i < pairs.size(); i++) {
            Correlations.Pair pair = pairs.get(i);
            Map<String, Object> chart = new LinkedHashMap<>();
            chart.put("title", funnyTitle(pair, i));
            chart.put("nameA", pair.a().name());
            chart.put("nameB", pair.b().name());
            chart.put("r", Math.round(pair.r() * 100) / 100.0);
            chart.put("years", pair.years());
            chart.put("a", rounded(pair.changesA()));
            chart.put("b", rounded(pair.changesB()));
            charts.add(chart);
        }
        // "</" is escaped so a label can never close the <script> tag early.
        String data = new ObjectMapper().writeValueAsString(charts).replace("</", "<\\/");
        Files.writeString(file, TEMPLATE.replace("/*DATA*/", data));
    }

    private static String funnyTitle(Correlations.Pair pair, int index) {
        List<String> templates = pair.r() > 0 ? TOGETHER_TITLES : OPPOSITE_TITLES;
        String title = templates.get(index % templates.size()).formatted(pair.a().nickname(), pair.b().nickname());
        return Character.toUpperCase(title.charAt(0)) + title.substring(1);
    }

    private static List<Double> rounded(List<Double> values) {
        return values.stream().map(v -> Math.round(v * 10) / 10.0).toList();
    }

    private static final String TEMPLATE = """
            <!doctype html>
            <html lang="en">
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Estonian Spurious Correlations</title>
            <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
            <style>
              :root { --bg: #f6f5f1; --card: #ffffff; --text: #1d1d1b; --muted: #6b6a66; --grid: #e4e2dc;
                      --a: #2f6fdf; --b: #e0592a; }
              @media (prefers-color-scheme: dark) {
                :root { --bg: #161615; --card: #22221f; --text: #ecebe6; --muted: #a09e97; --grid: #383733;
                        --a: #6b9cff; --b: #ff8a5c; }
              }
              * { box-sizing: border-box; }
              body { margin: 0; background: var(--bg); color: var(--text);
                     font: 16px/1.5 system-ui, -apple-system, "Segoe UI", sans-serif; }
              main { max-width: 1100px; margin: 0 auto; padding: 32px 16px 48px; }
              h1 { font-size: 2rem; margin: 0 0 8px; }
              .intro { color: var(--muted); max-width: 70ch; margin: 0 0 28px; }
              .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(100%, 480px), 1fr)); gap: 20px; }
              .card { background: var(--card); border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
              .card h2 { font-size: 1.15rem; margin: 0 0 4px; }
              .meta { color: var(--muted); font-size: .9rem; margin: 0 0 12px; }
              .canvas-box { position: relative; height: 260px; }
              footer { color: var(--muted); font-size: .85rem; margin-top: 32px; }
            </style>
            </head>
            <body>
            <main>
              <h1>Estonia's most suspicious correlations</h1>
              <p class="intro">Yearly data from Statistics Estonia. Each chart shows the <b>percent change from the
                previous year</b> of two unrelated things, and the pairs below are the ones whose changes line up
                best. Correlation is not causation: these are coincidences, not discoveries.</p>
              <div class="grid" id="grid"></div>
              <footer>Data: Statistics Estonia (andmed.stat.ee). r = Pearson correlation of year-over-year changes.</footer>
            </main>
            <script>
            const charts = /*DATA*/;
            const css = getComputedStyle(document.documentElement);
            const color = name => css.getPropertyValue(name).trim();
            const grid = document.getElementById("grid");

            charts.forEach((c, i) => {
              const card = document.createElement("section");
              card.className = "card";
              const direction = c.r > 0 ? "move together" : "move in opposite directions";
              card.innerHTML = `<h2></h2><p class="meta"></p><div class="canvas-box"><canvas></canvas></div>`;
              card.querySelector("h2").textContent = `${i + 1}. ${c.title}`;
              card.querySelector(".meta").textContent =
                `${c.nameA} vs ${c.nameB} · r = ${c.r > 0 ? "+" : ""}${c.r.toFixed(2)} over ${c.years.length} years (they ${direction})`;
              grid.appendChild(card);

              const percentAxis = (position, colorName, title) => ({
                position, title: { display: true, text: title, color: color(colorName) },
                ticks: { color: color(colorName), callback: v => v + "%" },
                grid: { color: position === "left" ? color("--grid") : "transparent" }
              });
              new Chart(card.querySelector("canvas"), {
                type: "line",
                data: {
                  labels: c.years,
                  datasets: [
                    { label: c.nameA, data: c.a, yAxisID: "yA", borderColor: color("--a"), backgroundColor: color("--a"), tension: .25 },
                    { label: c.nameB, data: c.b, yAxisID: "yB", borderColor: color("--b"), backgroundColor: color("--b"), tension: .25 }
                  ]
                },
                options: {
                  maintainAspectRatio: false,
                  interaction: { mode: "index", intersect: false },
                  plugins: {
                    legend: { labels: { color: color("--text") } },
                    tooltip: { callbacks: { label: t => `${t.dataset.label}: ${t.parsed.y > 0 ? "+" : ""}${t.parsed.y}%` } }
                  },
                  scales: {
                    x: { ticks: { color: color("--muted") }, grid: { display: false } },
                    yA: percentAxis("left", "--a", "change, %"),
                    yB: percentAxis("right", "--b", "change, %")
                  }
                }
              });
            });
            </script>
            </body>
            </html>
            """;
}
