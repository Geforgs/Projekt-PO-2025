package po25;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

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
        submissions = new HashMap<>();
        tasks = new HashMap<>();
    }

    @Override
    public String getId() {
        return this.contestId;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void reload() throws PlatformException {
        this.loadTasks();
        this.loadSubmissions();
    }

    @Override
    public List<Task> getTasks() throws PlatformException {
        if (!loaded) loadTasks();
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of(this.description);
    }

    @Override
    public Optional<Task> getTaskById(String taskId) throws PlatformException {
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

    public List<Submission> getSubmissionHistory() throws PlatformException {
        if (!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }

    private void loadTasks() throws PlatformException {
        this.loaded = false;
        Map<String, SatoriTask> newTasksMap = new HashMap<>();

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
            throw new PlatformException("Failed to load tasks for Satori contest " + this.contestId
                    + " due to network or parsing error: " + e.getMessage(), e);
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("An unexpected error occurred while loading tasks for Satori contest "
                    + this.contestId + ": " + e.getMessage(), e);
        }
    }

    protected void loadSubmissions() throws PlatformException {
        if (!this.loaded) {
            loadTasks();
        }
        Map<String, SatoriSubmission> newSubmissions = new HashMap<>();
        this.loadedSubmissions = false;

        if (this.tasks == null || this.tasks.isEmpty()) {
            System.err.println("SatoriContest: Tasks not loaded or tasks map is empty for contest " + contestId + ". Cannot load submissions.");
            this.loadedSubmissions = true;
            this.submissions = newSubmissions;
            return;
        }

        for (SatoriTask task : this.tasks.values()) {
            task.loadSubmissions();
            for (Submission submission : task.getSubmissionHistory()) {
                if (!this.submissions.containsKey(submission.getSubmissionId())) {
                    newSubmissions.put(submission.getSubmissionId(), (SatoriSubmission) submission);
                } else {
                    newSubmissions.put(submission.getSubmissionId(), this.submissions.get(submission.getSubmissionId()));
                }
            }
        }
        this.submissions = newSubmissions;
        this.loadedSubmissions = true;
    }
}