package po25;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.*;

public class SatoriContest implements Contest {
    protected final String url="https://satori.tcs.uj.edu.pl/contest";
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
        if(!loaded) loadTasks();
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of(this.description);
    }

    @Override
    public Optional<Task> getTaskById(String taskId) throws PlatformException {
        return getTasks().stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst();
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
        if(!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }

    private void loadTasks() throws PlatformException {
        try{
            this.loaded = false;
            Map<String, SatoriTask> newTasks = new HashMap<>();
            Document doc = Jsoup.connect(this.url + "/" + this.contestId + "/problems")
                    .cookie("satori_token", this.satori.satoriToken)
                    .get();
            Elements tables = doc.select("tbody");
            for(Element table : tables) {
                for(Element problem: table.children()) {
                    if((problem.child(0).text().equals("Code"))) continue;
                    String taskId = problem.select("a").first().attr("href").split("/")[4];
                    if(!tasks.containsKey(taskId)){
                        newTasks.put(taskId, new SatoriTask(taskId, problem.child(0).text(), problem.child(1).text(), this.satori.url + problem.select("a").first().attr("href"), this));
                    }else{
                        newTasks.put(taskId, tasks.get(taskId));
                    }
                }
            }
            tasks = newTasks;
            this.loaded = true;
        }catch(Exception e){
            throw new PlatformException(e.getMessage());
        }
    }

    public void loadSubmissions() throws PlatformException {
        if(!this.loaded) this.loadTasks();
        Map<String, SatoriSubmission> newSubmissions = new HashMap<>();
        this.loadedSubmissions = false;
        for(SatoriTask task: tasks.values()) {
            task.loadSubmissions();
            for(Submission submission: task.getSubmissionHistory()) {
                String submissionId = submission.getSubmissionId();
                if(!submissions.containsKey(submissionId)){
                    newSubmissions.put(submissionId, (SatoriSubmission) submission);
                }else{
                    newSubmissions.put(submissionId, submissions.get(submissionId));
                }
            }
        }
        submissions = newSubmissions;
        this.loadedSubmissions = true;
    }
}
