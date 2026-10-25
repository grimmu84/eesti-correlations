package app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Downloads series from the Statistics Estonia PxWeb API and keeps a copy in the "cache" folder,
 * so the second run is instant and works offline. Delete the folder to download fresh data.
 */
public class StatClient {

    private static final String API = "https://andmed.stat.ee/api/v1/en/stat/";
    private static final Path CACHE_DIR = Path.of("cache");

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    private final ObjectMapper json = new ObjectMapper();

    /** Returns the series as year -> value, sorted by year. */
    public TreeMap<Integer, Double> load(Series series) throws IOException, InterruptedException {
        Path cacheFile = CACHE_DIR.resolve(series.id() + ".json");
        if (!Files.exists(cacheFile)) {
            String data = download(series);
            Files.createDirectories(CACHE_DIR);
            Files.writeString(cacheFile, data);
            Thread.sleep(500); // be polite: the API allows only a few requests per second
        }
        return parseJsonStat(json.readTree(cacheFile.toFile()));
    }

    /** Step 1: ask the table which variables it has. Step 2: ask for exactly one line of data. */
    private String download(Series series) throws IOException, InterruptedException {
        System.out.println("  downloading " + series.id() + " ...");
        String url = API + series.table();
        JsonNode metadata = json.readTree(send(HttpRequest.newBuilder(URI.create(url)).GET()));

        // Build the query: all years, and exactly one value for every other variable.
        ObjectNode query = json.createObjectNode();
        ArrayNode selections = query.putArray("query");
        for (JsonNode variable : metadata.get("variables")) {
            String code = variable.get("code").asText();
            ObjectNode selection = selections.addObject().put("code", code).putObject("selection");
            if (variable.path("time").asBoolean()) {
                selection.put("filter", "all").putArray("values").add("*");
            } else {
                String value = pickValue(series, variable);
                selection.put("filter", "item").putArray("values").add(value);
            }
        }
        query.putObject("response").put("format", "json-stat2");

        return send(HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(query))));
    }

    /** Uses the value from the series' picks if there is one, otherwise the first value. */
    private String pickValue(Series series, JsonNode variable) {
        String chosen = null;
        for (Map.Entry<String, String> pick : series.picks().entrySet()) {
            if (pick.getKey().equalsIgnoreCase(variable.get("code").asText())
                    || pick.getKey().equalsIgnoreCase(variable.get("text").asText())) {
                chosen = pick.getValue();
            }
        }
        if (chosen == null) {
            return variable.get("values").get(0).asText();
        }
        for (JsonNode allowed : variable.get("values")) {
            if (allowed.asText().equals(chosen)) {
                return chosen;
            }
        }
        throw new IllegalArgumentException("Series '" + series.id() + "': value '" + chosen
                + "' does not exist for variable '" + variable.get("text").asText() + "'");
    }

    private String send(HttpRequest.Builder request) throws IOException, InterruptedException {
        HttpResponse<String> response = http.send(request.timeout(Duration.ofSeconds(60)).build(),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " from " + response.uri());
        }
        return response.body();
    }

    /**
     * Reads the JSON-stat format. Because we picked a single value for every variable except the year,
     * the "value" list simply holds one number per year, in the same order as the years.
     */
    private TreeMap<Integer, Double> parseJsonStat(JsonNode root) {
        String timeDimension = root.path("role").path("time").path(0).asText(null);
        if (timeDimension == null) {
            timeDimension = root.get("id").get(0).asText();
        }
        JsonNode yearIndex = root.get("dimension").get(timeDimension).get("category").get("index");
        JsonNode values = root.get("value");

        TreeMap<Integer, Double> result = new TreeMap<>();
        Iterator<Map.Entry<String, JsonNode>> years = yearIndex.fields();
        while (years.hasNext()) {
            Map.Entry<String, JsonNode> year = years.next();
            JsonNode value = values.get(year.getValue().asInt());
            if (value != null && value.isNumber()) {
                result.put(Integer.parseInt(year.getKey().trim()), value.asDouble());
            }
        }
        return result;
    }
}
