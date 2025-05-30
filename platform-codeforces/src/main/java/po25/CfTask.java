package po25;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

/**
 * Implementacja interfejsu Task dla Codeforces.
 * Pobiera i cache'uje pełną treść zadania wraz z przykładowymi danymi oraz limitami.
 */
public class CfTask implements Task {
    private final String name;
    private String id;
    private final String url;
    protected final CfContest contest;

    private String content;
    private String sampleInput;
    private String sampleOutput;
    private String timeLimit;
    private String memoryLimit;
    private boolean loaded;
    private boolean loadedSubmissions;
    private Map<String, CfSubmission> submissions;

    protected CfTask(String id, String name, String url, CfContest contest) {
        this.id = id;
        this.name = name;
        this.url  = url;
        this.contest = contest;
        this.loaded = false;
        this.loadedSubmissions = false;
        this.submissions = new HashMap<>();
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

    @Override
    public Submission submit(String path) throws PlatformException{
        ChromeDriver driver = Browser.getChrome();
        String submissionId;
        try{
            driver.get("https://codeforces.com/contest/" + this.contest.id + "/submit");
            Select select = new Select(driver.findElement(By.name("submittedProblemIndex")));
            select.selectByVisibleText(this.getId() + " - " + this.getName());
            WebElement element = driver.findElement(By.id("sourceCodeTextarea"));
            element.clear();
            element.sendKeys(Keys.TAB);
            String code = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            element.sendKeys(code);
            Thread.sleep(1000);
            driver.findElement(By.id("singlePageSubmitButton")).click();
            Thread.sleep(1000);
            submissionId = driver.findElement(By.className("highlighted-row")).findElement(By.className("view-source")).getText();
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM/dd/yyyy HH:mm").toFormatter(Locale.ENGLISH);
            LocalDateTime time = LocalDateTime.parse(driver.findElement(By.className("highlighted-row")).findElement(By.className("status-small")).getText().replace("UTC+2", ""), formatter);
            this.submissions.put(submissionId, new CfSubmission(this, submissionId, "https://codeforces.com/contest/" + this.contest.id + "/submission/" + submissionId, time));
        } catch (Exception e) {
            throw new PlatformException(e.getMessage());
        } finally {
            driver.quit();
        }
        return this.submissions.get(submissionId);
    }

    protected void loadSubmissions() throws PlatformException {
        ChromeDriver driver = Browser.getChrome();
        try{
            Map<String, CfSubmission> newSubmissions = new HashMap<>();
            driver.get("https://codeforces.com/contest/" + this.contest.id + "/status");
            Select select = new Select(driver.findElement(By.name("frameProblemIndex")));
            select.selectByVisibleText(this.getId() + " - " + this.getName());
            WebElement element = driver.findElement(By.id("participantSubstring"));
            element.clear();
            element.sendKeys(Keys.TAB);
            element.sendKeys(this.contest.codeforces.username);
            driver.findElement(By.xpath("//input[@value='Apply']")).click();
            List<WebElement> elements = driver.findElements(By.className("inactive"));
            if(elements.size() > 2){
                elements = driver.findElements(By.className("highlighted-row"));
                for(WebElement submission: elements){
                    String submissionId = submission.findElement(By.className("view-source")).getText();
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM/dd/yyyy HH:mm").toFormatter(Locale.ENGLISH);
                    LocalDateTime time = LocalDateTime.parse(submission.findElement(By.className("status-small")).getText().replace("UTC+2", ""), formatter);
                    if(this.submissions.containsKey(submissionId)){
                        newSubmissions.put(submissionId, this.submissions.get(submissionId));
                    }else{
                        newSubmissions.put(submissionId, new CfSubmission(this, submissionId, "https://codeforces.com/contest/" + this.contest.id + "/submission/" + submissionId, time));
                    }
                }
                driver.get(driver.findElements(By.className("arrow")).get(2).getAttribute("href").toString());
                elements = driver.findElements(By.className("inactive"));
                while(elements.size() == 0){
                    elements = driver.findElements(By.className("highlighted-row"));
                    for(WebElement submission: elements){
                        String submissionId = submission.findElement(By.className("view-source")).getText();
                        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM/dd/yyyy HH:mm").toFormatter(Locale.ENGLISH);
                        LocalDateTime time = LocalDateTime.parse(submission.findElement(By.className("status-small")).getText().replace("UTC+2", ""), formatter);
                        if(this.submissions.containsKey(submissionId)){
                            newSubmissions.put(submissionId, this.submissions.get(submissionId));
                        }else{
                            newSubmissions.put(submissionId, new CfSubmission(this, submissionId, "https://codeforces.com/contest/" + this.contest.id + "/submission/" + submissionId, time));
                        }
                    }
                    driver.get(driver.findElements(By.className("arrow")).get(3).getAttribute("href").toString());
                    elements = driver.findElements(By.className("inactive"));
                }
            }else{
                elements = driver.findElements(By.className("highlighted-row"));
                for(WebElement submission: elements){
                    String submissionId = submission.findElement(By.className("view-source")).getText();
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM/dd/yyyy HH:mm").toFormatter(Locale.ENGLISH);
                    LocalDateTime time = LocalDateTime.parse(submission.findElement(By.className("status-small")).getText().replace("UTC+2", ""), formatter);
                    if(this.submissions.containsKey(submissionId)){
                        newSubmissions.put(submissionId, this.submissions.get(submissionId));
                    }else{
                        newSubmissions.put(submissionId, new CfSubmission(this, submissionId,  "https://codeforces.com/contest/" + this.contest.id + "/submission/" + submissionId, time));
                    }
                }
            }
            this.submissions = newSubmissions;
            this.loadedSubmissions = true;
        } catch (Exception e) {
            throw new PlatformException(e.getMessage());
        } finally {
            driver.quit();
        }
    }

    @Override
    public List<Submission> getSubmissionHistory() throws PlatformException {
        if(!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }

    public String getUrl() {
        return url;
    }
}
