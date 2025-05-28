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
        String passwordStr = new String(password);
        try {
            Connection.Response res = Jsoup
                    .connect(this.baseApiUrl + "/login")
                    .data("login", username, "password", passwordStr)
                    .method(Connection.Method.POST)
                    .timeout(10000)
                    .execute();

            Map<String, String> cookies = res.cookies();
            String token = cookies.get("satori_token");

            if (token == null || token.isEmpty()) {
                throw new PlatformException("Login failed: Satori token not found in response cookies.");
            }
            return token;
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("Login failed for Satori: " + e.getMessage(), e);
        } finally {
            // clear char[] password vairable
        }
    }

    // this needs to check for 'Logged in as <userName>' on Satori website
    @Override
    protected boolean validateTokenWithServer(String token) throws PlatformException {
        return false;
    }


    private void loadContests() throws PlatformException {
        try {
            this.loaded = false;
            Map<String, SatoriContest> newContests = new HashMap<>();

            Document doc = Jsoup.connect(this.baseApiUrl + "/contest/select")
                    .cookie("satori_token", getRequiredToken())
                    .timeout(10000)
                    .get();

            Element table = doc.select("div[id=content]").select("table").select("tbody").first();
            if (table == null) {
                throw new PlatformException("Failed to parse contests page: main content table not found.");
            }

            for (Element tableRow : table.children()) {
                if (tableRow.children().isEmpty() || tableRow.child(0).text().equals("Name")) {
                    continue;
                }
                Element linkElement = tableRow.child(0).select("a").first();
                if (linkElement == null) {
                    System.err.println(getPlatformName() + ": Skipping row, no contest link found: " + tableRow.text());
                    continue;
                }
                String unparsedIdHref = linkElement.attr("href");
                String[] parts = unparsedIdHref.split("/");

                if (parts.length < 3 || !parts[1].equals("contest") || parts[2].isEmpty()) {
                    System.err.println(getPlatformName() + ": Could not parse contest ID from href: " + unparsedIdHref);
                    continue;
                }
                String contestId = parts[2];

                if (!contests.containsKey(contestId)) {
                    newContests.put(contestId, new SatoriContest(contestId, tableRow.child(0).text(), tableRow.child(1).text(), this));
                } else {
                    SatoriContest existingContest = contests.get(contestId);
                    newContests.put(contestId, existingContest);
                }
            }
            contests = newContests;
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

    // what about languageId?
    public Submission submitSolution(Task task, String path, String languageId) throws PlatformException {
        return ((SatoriTask) task).submit(path);
    }

    public Submission getSubmission(String submissionId) throws PlatformException {
        if (!loadedSubmissions) {
            loadSubmissions();
        }
        return this.submissions.get(submissionId);
    }

    public List<Submission> getSubmissionHistory() throws PlatformException {
        if (!loadedSubmissions) {
            loadSubmissions();
        }
        return new ArrayList<>(submissions.values());
    }
}