package po25;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;

public class SatoriPlatform extends AbstractPlatform implements Platform {

    private Map<String, SatoriContest> contests = new HashMap<>();
    private boolean loaded = false;
    private boolean loadedSubmissions = false;
    private Map<String, SatoriSubmission> submissions = new HashMap<>();

    public SatoriPlatform() {
        super("https://satori.tcs.uj.edu.pl");
    }

    @Override
    public String getPlatformName() {
        return "Satori";
    }

    @Override
    protected String performPlatformLogin(String username, char[] password) throws PlatformException {
        try {
            Connection.Response res = Jsoup
                    .connect(this.baseApiUrl + "/login")
                    .data("login", username, "password", new String(password))
                    .method(Connection.Method.POST)
                    .timeout(10000)
                    .execute();

            Map<String, String> cookies = res.cookies();
            String token = cookies.get("satori_token");

            if (token == null || token.isEmpty()) {
                throw new PlatformException("Login failed: Satori token not found in response cookies.");
            }
            return token;
        } catch (Exception e) {
            throw new PlatformException("Login failed for Satori: " + e.getMessage(), e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @Override
    protected boolean validateTokenWithServer(String token) throws PlatformException {
        try {
            Document doc = Jsoup.connect(this.baseApiUrl)
                    .cookie("satori_token", token)
                    .timeout(10000)
                    .get();
            return doc.text().contains("Logged in");
        } catch (Exception e) {
            throw new PlatformException("Failed to validate Satori token with server: " + e.getMessage(), e);
        }
    }


    private void loadContests() throws PlatformException {
        this.loaded = false;
        Map<String, SatoriContest> newContests = new HashMap<>();

        try {
            Document doc = Jsoup.connect(this.baseApiUrl + "/contest/select")
                    .cookie("satori_token", getRequiredToken())
                    .timeout(30000)
                    .get();

            Element contestTableBody = doc.selectFirst("div#content table tbody");
            if (contestTableBody == null) {
                throw new PlatformException("Failed to parse contests page: main content table not found.");
            }

            for (Element row : contestTableBody.children()) {
                Element linkElement = row.selectFirst("td:first-child a[href]");
                if (linkElement == null) {
                    continue;
                }

                String href = linkElement.attr("href");
                String[] parts = href.split("/");
                if (parts.length < 3 || !parts[1].equals("contest") || parts[2].isEmpty()) {
                    System.err.println(getPlatformName() + ": Could not parse contest ID from href: " + href);
                    continue;
                }
                String contestId = parts[2];

                String contestName = linkElement.text();
                String description = row.child(1).text();

                SatoriContest parsedContest = new SatoriContest(contestId, contestName, description, this);

                newContests.put(contestId, parsedContest);
            }

            this.contests = newContests;
            this.loaded = true;
        } catch (Exception e) {
            throw new PlatformException("Failed to load Satori contests: " + e.getMessage(), e);
        }
    }
    private void loadSubmissions() throws PlatformException {
        if (!this.loaded) {
            loadContests();
        }
        Map<String, SatoriSubmission> newSubmissions = new HashMap<>();
        this.loadedSubmissions = false;
        for (SatoriContest contest : contests.values()) {
            contest.loadSubmissions();
            for (Submission submission : contest.getSubmissionHistory()) {
                String submissionId = submission.getSubmissionId();
                if (!submissions.containsKey(submissionId)) {
                    newSubmissions.put(submissionId, (SatoriSubmission) submission);
                } else {
                    newSubmissions.put(submissionId, submissions.get(submissionId));
                }
            }
        }

        submissions = newSubmissions;
        this.loadedSubmissions = true;
    }

    public void reload() throws PlatformException {
        this.loadContests();
        this.loadSubmissions();
    }

    @Override
    public List<Contest> getAllContests() throws PlatformException {
        if (!loaded) {
            loadContests();
        }
        return new ArrayList<>(contests.values());
    }

    @Override
    public Optional<Contest> getContestById(String contestId) throws PlatformException {
        if (!loaded) {
            loadContests();
        }
        return Optional.of(contests.get(contestId));
    }

    @Override

    public Submission submitSolution(Task task, String path, String languageId) throws PlatformException {
        if (!(task instanceof SatoriTask)) {
            throw new PlatformException("Task must be a SatoriTask instance to submit to Satori.");
        }

        return task.submit(path);
    }

    @Override
    public Submission getSubmission(String submissionId) throws PlatformException {
        if (!loadedSubmissions) {
            loadSubmissions();
        }

        return this.submissions.get(submissionId);
    }

    @Override
    public List<Submission> getSubmissionHistory() throws PlatformException{
        if(!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }
}