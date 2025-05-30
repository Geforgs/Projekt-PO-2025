package po25;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SatoriSubmission implements Submission {
    private final SatoriTask task;
    private final String id;
    private final String url;
    private final LocalDateTime time;
    private String verdict;
    private boolean complete;

    protected SatoriSubmission(SatoriTask task, String id, LocalDateTime time, String url){
        this.task = task;
        this.id = id;
        this.time = time;
        this.url = url;
        this.verdict = "QUE";
        complete = false;
    }

    @Override
    public String getSubmissionId() {
        return this.id;
    }

    @Override
    public String getTaskId() {
        return this.task.getId();
    }

    @Override
    public Optional<String> getContestId() {
        return Optional.of(this.task.contest.getId());
    }

    private void loadVerdict(){
        try{
            Map<String, SatoriSubmission> newSubmissions = new HashMap<>();
            Document doc = Jsoup.connect(this.url)
                    .cookie("satori_token", this.task.contest.satori.getRequiredToken())
                    .get();
            String result = doc.select("table").select("tr").get(1).select("td").get(4).text();
            if(!result.equals("None") && !result.equals("QUE")){
                this.verdict = result;
                this.complete = true;
            }else{
                this.verdict = "QUE";
            }
        }catch(Exception e){
            this.verdict = "Unknown";
        }
    }
    public String getVerdict() {
        if(!this.complete) this.loadVerdict();
        return this.verdict;
    }

    @Override
    public Optional<LocalDateTime> getSubmittedAt() {
        return Optional.of(this.time);
    }

    @Override
    public Optional<String> getLanguage() {
        return Optional.empty();
    }
}