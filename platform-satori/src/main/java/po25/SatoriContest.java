package po25;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SatoriContest implements Contest {
    protected final String url="https://satori.tcs.uj.edu.pl/contest";
    protected final String contestId;
    private final String title;
    protected final SatoriPlatform satori;
    private final String description;
    private List<Task> tasks;
    private boolean loaded = false;

    protected SatoriContest(String contestId, String title, String description, SatoriPlatform satori) {
        this.contestId = contestId;
        this.title = title;
        this.satori = satori;
        this.description = description;
    }

    @Override
    public String getId() {
        return this.contestId;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    private void loadTasks() throws PlatformException {
        try{
            this.loaded = false;
            tasks = new ArrayList<>();
            Document doc = Jsoup.connect(this.url + "/" + this.contestId + "/problems")
                    .cookie("satori_token", this.satori.satoriToken)
                    .get();
            Elements tables = doc.select("tbody");
            for(Element table : tables) {
                for(Element problem: table.children()) {
                    if((problem.child(0).text().equals("Code"))) continue;
                    tasks.add(new SatoriTask(problem.select("a").first().attr("href").split("/")[4], problem.child(0).text(), problem.child(1).text(), this.satori.url + problem.select("a").first().attr("href"), this));
                }
            }
            this.loaded = true;
        }catch(Exception e){
            throw new PlatformException(e.getMessage());
        }
    }

    public void reloadTasks() throws PlatformException {
        this.loadTasks();
    }

    @Override
    public List<Task> getTasks() throws PlatformException {
        if(!loaded) loadTasks();
        return tasks;
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
}
