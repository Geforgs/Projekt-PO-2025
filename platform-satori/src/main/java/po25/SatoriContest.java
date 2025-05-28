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
        return getTasks().stream().filter(t -> t.getId().equals(taskId)).findFirst();
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
        Map<String, SatoriTask> newTasks = new HashMap<>();
        try {
            String tasksUrl = this.satori.baseApiUrl + "/contest/" + this.contestId + "/problems";
            Document doc = Jsoup.connect(tasksUrl).cookie("satori_token", this.satori.getRequiredToken()).timeout(10000).get();

            Elements tables = doc.select("div#content table.problems tbody");

            for (Element table : tables) {
                for (Element problemRow : table.children()) {
                    Element linkElement = problemRow.selectFirst("td a[href*='/problem/']");
                    if (linkElement == null) {
                        continue;
                    }

                    String href = linkElement.attr("href");
                    String[] parts = href.split("/");

                    String taskId = null;
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (parts[i].equals("problem") && i + 1 < parts.length) {
                            taskId = parts[i + 1];
                            break;
                        }
                    }

                    if (taskId == null || taskId.isEmpty()) {
                        System.err.println(satori.getPlatformName() + ": Could not parse task ID from href: " + href + " for contest " + contestId);
                        continue;
                    }

                    String problemCode = problemRow.child(0).text();
                    String problemName = problemRow.child(1).text();
                    String taskUrl = this.satori.baseApiUrl + href;

                    if (!tasks.containsKey(taskId)) {
                        newTasks.put(taskId, new SatoriTask(taskId, problemCode, problemName, taskUrl, this));
                    } else {
                        newTasks.put(taskId, tasks.get(taskId));
                    }
                }
            }

            tasks = newTasks;
            this.loaded = true;
        } catch (IOException e) {
            throw new PlatformException("Failed to load tasks for Satori contest " + this.contestId + " due to network or parsing error: " + e.getMessage(), e);
        } catch (PlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new PlatformException("An unexpected error occurred while loading tasks for Satori contest " + this.contestId + ": " + e.getMessage(), e);
        }
    }

    protected void loadSubmissions() throws PlatformException {
        if (!this.loaded) {
            loadTasks();
        }
        Map<String, SatoriSubmission> newSubmissions = new HashMap<>();
        this.loadedSubmissions = false;

        if (this.tasks == null) {
            throw new PlatformException("Tasks not loaded or tasks map is null for contest " + contestId + ". Cannot load submissions.");
        }

        for (SatoriTask task : tasks.values()) {
            task.loadSubmissions();
            for (Submission submission : task.getSubmissionHistory()) {
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
}