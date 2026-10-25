# Estonia's most suspicious correlations

*Põnevaid korelatsioone statistikast*

Downloads 32 yearly statistics from [Statistics Estonia](https://andmed.stat.ee) (births, divorces,
sprat catches, trees eaten by moose, ...), finds pairs whose **year-over-year changes** move together,
and draws the top 10 as charts in `index.html`.

## How to run (Windows)

You only need Java 21. Maven downloads itself the first time through the "Maven wrapper" (`mvnw.cmd`).

1. Open PowerShell in this folder.
2. Run:

   ```
   .\mvnw.cmd compile exec:java
   ```

3. Double-click `index.html` to open it in your browser.

The first run downloads the data into the `cache` folder (about 30 seconds). Later runs use the cache.
Delete the `cache` folder to get fresh data.

On macOS/Linux use `./mvnw compile exec:java` instead.

## How it works

| File | What it does |
|---|---|
| `src/main/java/app/Series.java` | The list of statistics to download. Add your own here. |
| `src/main/java/app/StatClient.java` | Talks to the API with Java's `HttpClient` and caches the answers. |
| `src/main/java/app/Correlations.java` | Turns values into yearly % changes and computes correlations. |
| `src/main/java/app/HtmlReport.java` | Writes `index.html` with [Chart.js](https://www.chartjs.org/) charts. |
| `src/main/java/app/Main.java` | Runs the three steps in order. |

**Why year-over-year changes?** Many things grew steadily since the 1990s (wages, cars, taxes), so their raw
values correlate even when they have nothing to do with each other. Comparing "how much did it change this year"
removes that shared trend.

**What counts as surprising?** Every series has a topic. Pairs from the same topic (for example marriages and
divorces) are skipped, and so are pairs with fewer than 12 years in common. Everything that collapsed in 2020
(theatre, cinema, flights, buses...) shares the topic "going out", so the list is not just ten copies of COVID.

**Adding a series:** find a table on [andmed.stat.ee](https://andmed.stat.ee/en/stat), copy its path from the
API (for example `https://andmed.stat.ee/api/v1/en/stat/majandus/kalandus/KA20.PX`), and add a line to
`Series.java`. Variables you do not mention use their first value, which is usually "Total".
