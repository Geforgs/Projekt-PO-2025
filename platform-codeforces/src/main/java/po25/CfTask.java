package po25;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Optional;

/**
 * Implementacja interfejsu Task dla Codeforces.
 * Pobiera i cache'uje pełną treść zadania wraz z przykładowymi danymi oraz limitami.
 * Zachowuje oryginalne formatowanie próbek przez użycie wholeText().
 */
public class CfTask implements Task {
    private final String name;
    private final String url;

    // Lazy-loaded pola
    private String content;
    private String sampleInput;
    private String sampleOutput;
    private String timeLimit;
    private String memoryLimit;
    private boolean loaded = false;

    public CfTask(String name, String url) {
        this.name = name;
        this.url  = url;
    }

    /**
     * Pobiera i parsuje treść zadania tylko raz.
     */
    private void loadDetails() throws IOException {
        // 1) Pobieramy stronę
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .get();

        // 2) Złap blok zadania
        Element stmt = doc.selectFirst(".problem-statement");
        if (stmt == null) {
            this.content = "Nie udało się pobrać treści zadania.";
            loaded = true;
            return;
        }

        // 3) Opis zadania: pierwszy <div> po .header
        Element descDiv = stmt.selectFirst(".header ~ div");
        this.content = descDiv != null
                ? descDiv.text()
                : "";

        // 4) Limity
        Element tl = stmt.selectFirst(".time-limit");
        this.timeLimit = tl != null ? tl.text() : null;
        Element ml = stmt.selectFirst(".memory-limit");
        this.memoryLimit = ml != null ? ml.text() : null;

        // 5) Próbki: generalna funkcja
        this.sampleInput  = parseSample(stmt, ".sample-test .input");
        this.sampleOutput = parseSample(stmt, ".sample-test .output");

        loaded = true;
    }

    /**
     * Parsuje sample wejścia/wyjścia z kontenera selector,
     * obsługuje div.test-example-line lub pre z <br>.
     */
    private String parseSample(Element stmt, String selector) {
        Element container = stmt.selectFirst(selector);
        if (container == null) return null;

        Elements lines = container.select(".test-example-line");
        if (!lines.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Element line : lines) {
                sb.append(line.text()).append("\n");
            }
            return sb.toString().trim();
        }

        // b) fallback: <pre> + <br>
        Element pre = container.selectFirst("pre");
        if (pre != null) {
            String raw = pre.html();
            return raw
                    .replaceAll("(?i)<br\\s*/?>", "\n")
                    .replaceAll("&nbsp;", " ")
                    .trim();
        }

        return null;
    }

    private void ensureLoaded() {
        if (!loaded) {
            try {
                loadDetails();
            } catch (IOException e) {
                throw new RuntimeException("Błąd pobierania zadania: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContent() {
        ensureLoaded();
        return content;
    }

    @Override
    public Optional<String> getSampleInput() {
        ensureLoaded();
        return Optional.ofNullable(sampleInput);
    }

    @Override
    public Optional<String> getSampleOutput() {
        ensureLoaded();
        return Optional.ofNullable(sampleOutput);
    }

    @Override
    public Optional<String> getTimeLimit() {
        ensureLoaded();
        return Optional.ofNullable(timeLimit);
    }

    @Override
    public Optional<String> getMemoryLimit() {
        ensureLoaded();
        return Optional.ofNullable(memoryLimit);
    }

    public String getUrl() {
        return url;
    }
}
