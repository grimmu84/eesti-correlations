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

    // The titles are in Estonian. They only use the nicknames in their basic form (nimetav kääne),
    // so any nickname fits into any title without changing its grammar.
    private static final List<String> TOGETHER_TITLES = List.of(
            "%s ja %s – sünnil lahutatud kaksikud?",
            "Värske uudis: %s ja %s jalutavad käsikäes",
            "%s ja %s: salajane side, millest keegi ei räägi",
            "%s ja %s? Väga kahtlane.",
            "%s × %s: üks armastuslugu",
            "%s ja %s tantsivad samas taktis",
            "Teadlased hämmingus: %s ja %s käivad ühte jalga",
            "%s ja %s – parimad sõbrad igavesti",
            "Kes oleks arvanud: %s ja %s on hingesugulased",
            "%s ja %s: puhas juhus (või ongi?)");

    private static final List<String> OPPOSITE_TITLES = List.of(
            "%s vs. %s: igavene rivaalitsemine",
            "%s ja %s ei salli teineteist silmaotsaski",
            "%s ja %s kiigel: üks üles, teine alla",
            "%s ja %s: nagu kass ja koer",
            "%s ja %s liiguvad alati vastassuunas");

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
            <html lang="et">
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Eesti kahtlased korrelatsioonid</title>
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
              <h1>Eesti kõige kahtlasemad korrelatsioonid</h1>
              <p class="intro">Statistikaameti aastaandmed. Iga graafik näitab kahe omavahel mitteseotud näitaja
                <b>muutust võrreldes eelmise aastaga</b> ning siin on paarid, mille muutused langevad kõige paremini
                kokku. Korrelatsioon ei tähenda põhjuslikku seost: need on kokkusattumused, mitte avastused.</p>
              <div class="grid" id="grid"></div>
              <footer>Andmed: Statistikaamet (andmed.stat.ee). r = aastaste muutuste Pearsoni korrelatsioonikordaja.</footer>
            </main>
            <script>
            const charts = /*DATA*/;
            const css = getComputedStyle(document.documentElement);
            const color = name => css.getPropertyValue(name).trim();
            const grid = document.getElementById("grid");
            // Estonian number format: decimal comma and a real minus sign, e.g. "+12,3" or "−0,56".
            const signed = (v, digits) => (v > 0 ? "+" : "") +
              v.toLocaleString("et-EE", { minimumFractionDigits: digits, maximumFractionDigits: digits });

            charts.forEach((c, i) => {
              const card = document.createElement("section");
              card.className = "card";
              const direction = c.r > 0 ? "liiguvad koos" : "liiguvad vastassuunas";
              card.innerHTML = `<h2></h2><p class="meta"></p><div class="canvas-box"><canvas></canvas></div>`;
              card.querySelector("h2").textContent = `${i + 1}. ${c.title}`;
              card.querySelector(".meta").textContent =
                `${c.nameA} vs ${c.nameB} · r = ${signed(c.r, 2)} · ${c.years.length} aastat (${direction})`;
              grid.appendChild(card);

              const percentAxis = (position, colorName, title) => ({
                position, title: { display: true, text: title, color: color(colorName) },
                ticks: { color: color(colorName), callback: v => v.toLocaleString("et-EE") + "%" },
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
                    tooltip: { callbacks: { label: t => `${t.dataset.label}: ${signed(t.parsed.y, 1)}%` } }
                  },
                  scales: {
                    x: { ticks: { color: color("--muted") }, grid: { display: false } },
                    yA: percentAxis("left", "--a", "muutus, %"),
                    yB: percentAxis("right", "--b", "muutus, %")
                  }
                }
              });
            });
            </script>
            </body>
            </html>
            """;
}
