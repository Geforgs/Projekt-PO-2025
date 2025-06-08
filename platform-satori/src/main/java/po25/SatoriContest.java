package po25;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SatoriContest implements Contest {
    protected final String contestId;
    private final String title;
    protected final SatoriPlatform satori;
    private final String description;
    private Map<String, SatoriTask> tasks;
    private boolean loaded;
    private boolean loadedSubmissions;
    private Map<String, SatoriSubmission> submissions;

    protected SatoriContest(String contestId, String title, String description, SatoriPlatform satori) {
        this.contestId = contestId;
        this.title = title;
        this.satori = satori;
        this.description = description;
        this.loaded = false;
        this.loadedSubmissions = false;
        submissions = new TreeMap<>();
        tasks = new TreeMap<>();
    }

    @Override
    public String getId() {
        return this.contestId;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void reload() throws PlatformException, ConnectionException, LoginException {
        this.loadTasks();
        this.loadSubmissions();
    }

    @Override
    public List<Task> getTasks() throws PlatformException, ConnectionException, LoginException {
        if (!loaded) loadTasks();
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of(this.description);
    }

    @Override
    public Optional<Task> getTaskById(String taskId) throws PlatformException, ConnectionException, LoginException {
        if (!loaded) {
            loadTasks();
        }

        return Optional.ofNullable(this.tasks.get(taskId));
    }

    @Override
    public Optional<LocalDateTime> getStartTime() {
        return Optional.empty();
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return this.title;
    }

    @Override
    public List<Submission> getSubmissionHistory() throws PlatformException, ConnectionException, LoginException {
        if (!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }

    private void loadTasks() throws PlatformException, ConnectionException, LoginException {
        this.loaded = false;
        Map<String, SatoriTask> newTasksMap = new TreeMap<>();
        if (!this.satori.isSessionValid()){
            throw new LoginException("You are not logged in " + this.satori.getPlatformName());
        }

        try {
            String contestProblemsPageUrl = this.satori.baseApiUrl + "/contest/" + this.contestId + "/problems";
            Document doc = Jsoup.connect(contestProblemsPageUrl)
                    .cookie("satori_token", this.satori.getRequiredToken())
                    .timeout(30000)
                    .get();


            Elements problemRows = doc.select("tbody > tr");

            for (Element row : problemRows) {
                Elements cells = row.children();


                if (cells.isEmpty() || "Code".equalsIgnoreCase(cells.get(0).text().trim())) {
                    continue;
                }

                if (cells.size() < 2) {
                    System.err.println("SatoriContest: Skipping malformed task row (not enough cells) in contest "
                            + this.contestId + ": " + row.html());
                    continue;
                }

                Element linkElement = row.select("a[href]").first();
                if (linkElement == null) {
                    System.err.println("SatoriContest: No link found in task row for contest "
                            + this.contestId + ": " + row.html());
                    continue;
                }

                String problemHref = linkElement.attr("href");
                String[] hrefParts = problemHref.split("/");

                if (hrefParts.length <= 4) {
                    System.err.println("SatoriContest: Could not parse taskId from href '" + problemHref
                            + "' in contest " + this.contestId + ". Expected at least 5 parts after split.");
                    continue;
                }
                String taskId = hrefParts[4];

                String taskCode = cells.get(0).text().trim();
                String taskTitle = cells.get(1).text().trim();
                String taskUrl = linkElement.absUrl("href");

                SatoriTask taskInstance = this.tasks.get(taskId);
                if (taskInstance == null) {
                    taskInstance = new SatoriTask(taskId, taskCode, taskTitle, taskUrl, this);
                }

                newTasksMap.put(taskId, taskInstance);
            }

            this.tasks = newTasksMap;
            this.loaded = true;

        } catch (IOException e) {
            throw new ConnectionException("Failed to load tasks for Satori contest " + this.contestId
                    + " due to network or parsing error: " + e.getMessage(), e);
        }
    }

    public void loadSubmissions() throws PlatformException, ConnectionException, LoginException {
        if (!this.loaded) {
            loadTasks();
        }
        if (!this.satori.isSessionValid()){
            throw new LoginException("You are not logged in " + this.satori.getPlatformName());
        }
        ExecutorService es = Executors.newCachedThreadPool();
        final Map<String, SatoriSubmission> newSubmissions = new TreeMap<>();
        Set<SatoriTask> unsuccesses = new HashSet<>();
        this.loadedSubmissions = false;

        for (SatoriTask task : this.tasks.values()) {
            es.execute(new Runnable() {
                public void run() {
                    try{
                        task.loadSubmissions();
                        for (Submission submission : task.getSubmissionHistory()) {
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
                        unsuccesses.add(task);
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
        this.submissions = newSubmissions;
        this.loadedSubmissions = true;
    }
}
