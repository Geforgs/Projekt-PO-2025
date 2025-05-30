package po25;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Implementacja interfejsu Contest dla Codeforces.
 */
public class CfContest implements Contest {
    protected final String id;
    private final String title;
    private final LocalDateTime start;
    private final LocalDateTime end;
    protected final CodeforcesPlatform codeforces;
    private Map<String, CfTask> tasks;
    private Map<String, CfSubmission> submissions;
    private boolean loadedSubmissions;

    CfContest(String id, String title,
              java.time.ZonedDateTime startZdt,
              java.time.ZonedDateTime endZdt,
              CodeforcesPlatform codeforces) {
        this.id = id;
        this.title = title;
        this.start = startZdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        this.end   = endZdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        this.codeforces = codeforces;
        this.loadedSubmissions = false;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<Task> getTasks() throws PlatformException {
        if (tasks == null) {
            this.loadTasks();
        }
        return new ArrayList<>(tasks.values());
    }

    private void loadTasks() throws PlatformException {
        try {
            String url = this.codeforces.API_BASE + "/contest.standings"
                    + "?contestId=" + this.getId()
                    + "&from=1&count=1000";
            String body = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .execute()
                    .body();

            JSONObject root = new JSONObject(body);
            if (!"OK".equals(root.getString("status"))) {
                throw new PlatformException("CF API error: " + root);
            }

            JSONArray problems = root
                    .getJSONObject("result")
                    .getJSONArray("problems");

            this.tasks = new HashMap<>();
            for (int i = 0; i < problems.length(); i++) {
                JSONObject p = problems.getJSONObject(i);
                String index = p.getString("index");
                String name  = p.getString("name");
                String link  = "https://codeforces.com/contest/"
                        + this.getId()
                        + "/problem/" + index;
                tasks.put(index, new CfTask(index, name, link, this));
            }
        } catch (IOException e) {
            throw new PlatformException("Błąd pobierania zadań", e);
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public Optional<Task> getTaskById(String taskId) throws PlatformException {
        return getTasks().stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst();
    }

    @Override
    public Optional<LocalDateTime> getStartTime() {
        return Optional.of(start);
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.of(end);
    }

    @Override
    public String toString() {
        return title + " (starts: " + start + ")";
    }

    protected void loadSubmissions() throws PlatformException {
        if(this.tasks == null) this.loadTasks();
        Map<String, CfSubmission> newSubmissions = new HashMap<>();
        this.loadedSubmissions = false;
        for(CfTask task: tasks.values()) {
            task.loadSubmissions();
            for(Submission submission: task.getSubmissionHistory()) {
                String submissionId = submission.getSubmissionId();
                if(!submissions.containsKey(submissionId)){
                    newSubmissions.put(submissionId, (CfSubmission) submission);
                }else{
                    newSubmissions.put(submissionId, submissions.get(submissionId));
                }
            }
        }
        submissions = newSubmissions;
        this.loadedSubmissions = true;
    }

    @Override
    public List<Submission> getSubmissionHistory() throws PlatformException{
        if(!this.loadedSubmissions) this.loadSubmissions();
        return new ArrayList<>(submissions.values());
    }
}
