package po25;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SatoriTask implements Task {
    private final String id;
    private final String code;
    private final String name;
    private final String url;
    protected final SatoriContest contest;
    private boolean loaded;
    private boolean loadedSubmissions;
    private String content;
    private String parsedContent;
    private Map<String, SatoriSubmission> submissions;
    private String css;

    SatoriTask(String id, String code, String name, String url, SatoriContest contest) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.url = url;
        this.contest = contest;
        this.loaded = false;
        this.loadedSubmissions = false;
        this.submissions = new HashMap<>();
    }

    private void load() throws PlatformException, ConnectionException, LoginException {
        this.loaded = false;
        if (!this.contest.satori.isSessionValid()) {
            throw new LoginException("You are not logged in.");
        }
        try {
            Document doc = Jsoup.connect(this.url)
                    .cookie("satori_token", this.contest.satori.getRequiredToken())
                    .timeout(10000)
                    .get();

            StringBuilder cssBuilder = new StringBuilder();
            for (Element link : doc.select("link")) {
                String cssUrl = this.contest.satori.baseApiUrl + link.attr("href");

                if (link.attr("rel").equals("stylesheet")) {
                    Document cssBody = Jsoup.connect(cssUrl)
                            .cookie("satori_token", this.contest.satori.getRequiredToken())
                            .timeout(10000)
                            .get();
                    cssBuilder.append(cssBody.body().text()).append('\n');
                }
            }

            this.css = cssBuilder.toString();
            this.content = doc.body().getElementsByClass("mainsphinx").first().toString();
            StringBuilder parsedContentBuilder = new StringBuilder();
            for (Element child : doc.body().getElementsByClass("mainsphinx").first().children()) {
                parsedContentBuilder.append(parseTextContent(child));
            }
            this.parsedContent = parsedContentBuilder.toString();
            this.loaded = true;
        } catch (IOException e) {
            throw new ConnectionException("Failed to load Satori task " + this.id + " content due to network or parsing error: " + e.getMessage(), e);
        }
    }

    private String parseTextContent(Element element) {
        StringBuilder answer = new StringBuilder();

        if (element.nodeName().equals("div") || element.nodeName().equals("table")) {
            for (Element child : element.children()) {
                String childText = parseTextContent(child);
                answer.append(childText);
            }
        } else {
            answer.append(element.text().replace("\\(", "").replace("\\)", "").replace("\\le", "<="));
            answer.append("\n");
        }
        return answer.toString();
    }

    public String getCss() throws PlatformException, ConnectionException, LoginException {
        if (!loaded) this.load();
        return this.css;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.code + ": " + this.name;
    }

    protected String getCode() {
        return this.code;
    }

    @Override
    public String getContent() throws PlatformException, ConnectionException, LoginException {
        if (!this.loaded) {
            this.load();
        }
        return this.parsedContent;
    }

    @Override
    public String getUnparsedContent() throws PlatformException, ConnectionException, LoginException {
        if (!this.loaded) {
            this.load();
        }
        return this.content;
    }

    @Override
    public Optional<String> getSampleInput() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getSampleOutput() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getTimeLimit() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getMemoryLimit() {
        return Optional.empty();
    }

    @Override
    public Submission submit(String filePath) throws PlatformException, ConnectionException, LoginException {
        if (!this.contest.satori.isSessionValid()) {
            throw new LoginException("You are not logged in.");
        }
        try {
            File codeFile = new File(filePath);
            if (!codeFile.exists() || !codeFile.isFile()) {
                throw new PlatformException("Source code file not found or is not a file: " + filePath);
            }
            String submitUrl = this.contest.satori.baseApiUrl + "/contest/" + this.contest.contestId + "/submit";

            Connection.Response res = Jsoup
                    .connect(submitUrl)
                    .cookie("satori_token", this.contest.satori.getRequiredToken())
                    .data("problem", this.id)
                    .data("codefile", codeFile.getAbsolutePath(), Files.newInputStream(codeFile.toPath()))
                    .method(Connection.Method.POST)
                    .timeout(30000)
                    .execute();

            Document doc = res.parse();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime time = LocalDateTime.parse(doc.select("table").select("tr").get(1).children().get(2).text(), formatter);
            String submissionId = doc.select("table").select("tr").get(1).children().get(0).text();
            submissions.put(submissionId, new SatoriSubmission(this, submissionId, time, "https://satori.tcs.uj.edu.pl/contest/" + this.contest.getId() + "/results/" + submissionId));
            return submissions.get(submissionId);
        } catch (IOException e) {
            throw new ConnectionException("Failed to submit solution for Satori task " + this.id + " due to network error: " + e.getMessage(), e);
        }
    }

    public List<Submission> getSubmissionHistory() throws PlatformException, ConnectionException, LoginException {
        if (!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }

    protected void loadSubmissions() throws PlatformException, ConnectionException, LoginException {
        if (!this.contest.satori.isSessionValid()) {
            throw new LoginException("You are not logged in.");
        }
        this.loadedSubmissions = false;
        Map<String, SatoriSubmission> newSubmissions = new HashMap<>();

        try {
            String resultsUrl = this.contest.satori.baseApiUrl + "/contest/" + this.contest.contestId +
                    "/results?results_limit=2000000000&results_filter_problem=" + this.id;

            Document doc = Jsoup.connect(resultsUrl)
                    .cookie("satori_token", this.contest.satori.getRequiredToken())
                    .timeout(30000)
                    .get();

            Elements result = doc.select("table").select("tr");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 1; i < result.size(); i++) {
                String submissionId = result.get(i).children().get(0).text();
                if (!submissions.containsKey(submissionId)) {
                    LocalDateTime time = LocalDateTime.parse(result.get(i).children().get(2).text(), formatter);
                    newSubmissions.put(submissionId, new SatoriSubmission(this, submissionId, time, "https://satori.tcs.uj.edu.pl/contest/" + this.contest.getId() + "/results/" + submissionId));
                } else {
                    newSubmissions.put(submissionId, submissions.get(submissionId));
                }
            }

            submissions = newSubmissions;
            this.loadedSubmissions = true;
        } catch (IOException e) {
            throw new ConnectionException("Failed to load submissions for Satori task " + this.id + " due to network or parsing error: " + e.getMessage(), e);
        }
    }

    public void reload() throws PlatformException, ConnectionException, LoginException {
        this.load();
        this.loadSubmissions();
    }
}