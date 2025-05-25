package po25;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SatoriTask implements Task {
    private final String id;
    private final String code;
    private final String name;
    private final String url;
    protected final SatoriContest contest;
    private boolean loaded;
    private boolean loadedSubmissions;
    private String content;
    private String parsedContent;
    private Map<String, Submission> submissions;

    SatoriTask(String id, String code, String name, String url, SatoriContest contest) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.url = url;
        this.contest = contest;
        loaded = false;
        loadedSubmissions = false;
    }

    private void load() throws PlatformException {
        try{
            this.loaded = false;
            Document doc = Jsoup.connect(this.url)
                    .cookie("satori_token", this.contest.satori.satoriToken)
                    .get();
            this.content = doc.body().getElementsByClass("mainsphinx").first().toString();
            StringBuilder parsedContentBuilder = new StringBuilder();
            for(Element child: doc.body().getElementsByClass("mainsphinx").first().children()){
                parsedContentBuilder.append(parse(child));
            }
            this.parsedContent = parsedContentBuilder.toString();
            this.loaded = true;
        }catch(Exception e){
            throw new PlatformException(e.getMessage());
        }
    }

    private String parse(Element element){
        StringBuilder answer = new StringBuilder();
        if(element.nodeName().equals("div") || element.nodeName().equals("table")){
            for(Element child: element.children()){
                String childText = parse(child);
                answer.append(childText);
            }
        }else{
            answer.append(element.text().replace("\\(", "").replace("\\)", "").replace("\\le", "<="));
            answer.append("\n");
        }
        return answer.toString();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.code + ": " + this.name;
    }

    protected String getCode(){
        return this.code;
    }

    @Override
    public String getContent()  {
        if(!this.loaded){
            try{
                this.load();
            }catch (PlatformException e){
                throw new RuntimeException(e);
            }
        }
        return this.parsedContent;
    }

    public String getUnparsedContent() {
        if(!this.loaded){
            try{
                this.load();
            }catch (PlatformException e){
                throw new RuntimeException(e);
            }
        }
        return this.content;
    }

    @Override
    public Optional<String> getSampleInput() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getSampleOutput() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getTimeLimit() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getMemoryLimit() {
        return Optional.empty();
    }

    public Submission submit(String path) throws PlatformException {
        try{
            File code = new File(path);
            Connection.Response res = Jsoup
                    .connect(this.contest.url + "/" + this.contest.contestId + "/submit")
                    .cookie("satori_token", this.contest.satori.satoriToken)
                    .data("problem", this.getId())
                    .data("codefile", code.getAbsolutePath(), Files.newInputStream(code.toPath()))
                    .method(Connection.Method.POST)
                    .execute();
            Document doc = res.parse();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime time = LocalDateTime.parse(doc.select("table").select("tr").get(1).children().get(2).text(), formatter);
            return new SatoriSubmission(this, doc.select("table").select("tr").get(1).children().get(0).text(), time);
        }catch (Exception e){
            throw new PlatformException(e.getMessage());
        }
    }

    public List<Submission> getSubmissionHistory() throws PlatformException {
        if(!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }

    private void loadSubmissions() throws PlatformException {
        try{
            this.loadedSubmissions = false;
            submissions = new HashMap<>();
            Document doc = Jsoup.connect(this.contest.url + "/" + this.contest.contestId + "/results?results_limit=2000000000&results_filter_problem=" + this.id)
                    .cookie("satori_token", this.contest.satori.satoriToken)
                    .get();
            Elements result = doc.select("table").select("tr");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for(int i=1;i<result.size();i++){
                LocalDateTime time = LocalDateTime.parse(result.get(i).children().get(2).text(), formatter);
                submissions.put(result.get(i).children().get(0).text(), new SatoriSubmission(this, result.get(i).children().get(0).text(), time));
            }
            this.loadedSubmissions = true;
        }catch(Exception e){
            throw new PlatformException(e.getMessage());
        }
    }

    public void reload() throws PlatformException {
        this.load();
        this.loadSubmissions();
    }
}