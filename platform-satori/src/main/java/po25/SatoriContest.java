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
    private final String url="https://satori.tcs.uj.edu.pl/contest";
    private final String contestId;
    private final String title;
    protected final SatoriPlatform satori;
    private final String description;

    protected SatoriContest(String contestId, String title, String description, SatoriPlatform satori) {
        this.contestId = contestId;
        this.title = title;
        this.satori = satori;
        this.description = description;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public List<Task> getTasks() throws PlatformException {
        List<Task> tasks = new ArrayList<>();
        try{
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
        }catch(Exception e){
            throw new PlatformException(e.getMessage());
        }
        return tasks;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of(this.description);
    }

    @Override
    public Optional<Task> getTaskById(String taskId) throws PlatformException {
        return Optional.empty();
    }

    @Override
    public Optional<LocalDateTime> getStartTime() {
        return Optional.empty();
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.empty();
    }
}
