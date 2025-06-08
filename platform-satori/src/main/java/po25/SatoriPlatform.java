package po25;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SatoriPlatform extends AbstractPlatform implements Platform {

    private Map<String, SatoriContest> contests = new TreeMap<>();
    private boolean loaded = false;
    private boolean loadedSubmissions = false;
    private Map<String, SatoriSubmission> submissions = new TreeMap<>();

    public SatoriPlatform() {
        super("https://satori.tcs.uj.edu.pl");
    }

    @Override
    public String getPlatformName() {
        return "Satori";
    }

    @Override
    protected String performPlatformLogin(String username, char[] password) throws ConnectionException {
        try {
            Connection.Response res = Jsoup
                    .connect(this.baseApiUrl + "/login")
                    .data("login", username, "password", new String(password))
                    .method(Connection.Method.POST)
                    .timeout(10000)
                    .execute();

            Map<String, String> cookies = res.cookies();
            return cookies.get("satori_token");
        } catch (IOException e){
            throw new ConnectionException("Check your Internet");
        }finally {
            Arrays.fill(password, '\0');
        }
    }

    @Override
    protected boolean validateTokenWithServer(String token) throws ConnectionException {
        try {
            Document doc = Jsoup.connect(this.baseApiUrl)
                    .cookie("satori_token", token)
                    .timeout(10000)
                    .get();
            return doc.text().contains("Logged in");
        } catch (IOException e){
            throw new ConnectionException("Check your Internet");
        }
    }


    private void loadContests() throws LoginException, ConnectionException, PlatformException {
        Map<String, SatoriContest> newContests = new TreeMap<>();
        if (!this.isSessionValid()){
            throw new LoginException("You are not logged in " + this.getPlatformName());
        }
        try {
            Document doc = Jsoup.connect(this.baseApiUrl + "/contest/select")
                    .cookie("satori_token", getRequiredToken())
                    .timeout(30000)
                    .get();
            Element contestTableBody;
            if(doc.select("div#content table tbody").size() > 2){
                contestTableBody = doc.select("div#content table tbody").get(1);
            }else if(doc.select("div#content table tbody").size() > 1){
                contestTableBody = doc.selectFirst("div#content table tbody");
            }else{
                throw new PlatformException("Some problem with satori");
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
        }  catch (IOException e){
            throw new ConnectionException("Check your Internet");
        }
    }
    private void loadSubmissions() throws PlatformException, ConnectionException, LoginException {
        if (!this.loaded) {
            loadContests();
        }
        if (!this.isSessionValid()){
            throw new LoginException("You are not logged in " + this.getPlatformName());
        }
        ExecutorService es = Executors.newCachedThreadPool();
        final Map<String, SatoriSubmission> newSubmissions = new TreeMap<>();
        Set<SatoriContest> unsuccesses = new HashSet<>();
        this.loadedSubmissions = false;
        for (SatoriContest contest : contests.values()) {
            es.execute(new Runnable() {
                public void run() {
                    try{
                        contest.loadSubmissions();
                        for (Submission submission : contest.getSubmissionHistory()) {
                            synchronized (newSubmissions) {
                                String submissionId = submission.getSubmissionId();
                                if (!submissions.containsKey(submissionId)) {
                                    newSubmissions.put(submissionId, (SatoriSubmission) submission);
                                } else {
                                    newSubmissions.put(submissionId, submissions.get(submissionId));
                                }
                            }
                        }
                    }catch (Exception e){
                        unsuccesses.add(contest);
                    }
                }
            });
        }
        es.shutdown();
        try {
            boolean finished = es.awaitTermination(5, TimeUnit.MINUTES);
            if (!finished || unsuccesses.size() > 0) {
                throw new PlatformException("Unsaccessfull grabing submissions");
            }
        }catch (InterruptedException e){
            throw new PlatformException("Unsaccessfull grabing submissions");
        }
        submissions = newSubmissions;
        this.loadedSubmissions = true;
    }

    public void reload() throws PlatformException, ConnectionException, LoginException {
        this.loadContests();
        this.loadSubmissions();
    }

    @Override
    public List<Contest> getAllContests() throws PlatformException, LoginException, ConnectionException {
        if (!loaded) {
            loadContests();
        }
        return new ArrayList<>(contests.values());
    }

    @Override
    public Optional<Contest> getContestById(String contestId) throws PlatformException, LoginException, ConnectionException {
        if (!loaded) {
            loadContests();
        }
        return Optional.of(contests.get(contestId));
    }

    @Override

    public Submission submitSolution(Task task, String path, String languageId) throws PlatformException, ConnectionException, LoginException {
        if (!(task instanceof SatoriTask)) {
            throw new PlatformException("Task must be a SatoriTask instance to submit to Satori.");
        }

        return task.submit(path);
    }

    @Override
    public Submission getSubmission(String submissionId) throws PlatformException, ConnectionException, LoginException {
        if (!loadedSubmissions) {
            loadSubmissions();
        }

        return this.submissions.get(submissionId);
    }

    @Override
    public List<Submission> getSubmissionHistory() throws PlatformException, ConnectionException, LoginException{
        if(!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }
}