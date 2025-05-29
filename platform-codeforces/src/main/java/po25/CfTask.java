package po25;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.Optional;

/**
 * Implementacja interfejsu Task dla Codeforces.
 * Pobiera i cache'uje pełną treść zadania wraz z przykładowymi danymi oraz limitami.
 */
public class CfTask implements Task {
    private final String name;
    private String id;
    private final String url;

    private String content;
    private String sampleInput;
    private String sampleOutput;
    private String timeLimit;
    private String memoryLimit;
    private boolean loaded = false;

    public CfTask(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url  = url;
    }

    private void loadDetails() throws IOException {
        ChromeDriver driver = Browser.getChrome();
        Document doc;
        try{
            driver.get(url);
            doc = Jsoup.parse(driver.getPageSource());
        }catch (Exception e){
            throw e;
        } finally {
            driver.quit();
        }
        Element stmt = doc.selectFirst(".problem-statement");
        if (stmt == null) {
            this.content = "Nie udało się pobrać treści zadania.";
            loaded = true;
            return;
        }

        Element descDiv = stmt.selectFirst(".header ~ div");
        this.content = descDiv != null
                ? descDiv.text()
                : "";

        Element tl = stmt.selectFirst(".time-limit");
        this.timeLimit = tl != null ? tl.text() : null;
        Element ml = stmt.selectFirst(".memory-limit");
        this.memoryLimit = ml != null ? ml.text() : null;

        this.sampleInput  = parseSample(stmt, ".sample-test .input");
        this.sampleOutput = parseSample(stmt, ".sample-test .output");

        loaded = true;
    }

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
    public String getId() {
        return id;
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

    public Submission submit(String path) throws PlatformException{

        return null;
    }

    public String getUrl() {
        return url;
    }
}
