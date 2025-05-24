package po25;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Optional;

public class SatoriTask implements Task {
    private final String id;
    private final String code;
    private final String name;
    private final String url;
    private final SatoriContest contest;
    private boolean loaded;
    private String content;
    private String parsedContent;

    SatoriTask(String id, String code, String name, String url, SatoriContest contest) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.url = url;
        this.contest = contest;
        loaded = false;
    }

    private void load() throws PlatformException {
        try{
            this.loaded = false;
            Document doc = Jsoup.connect(this.url)
                    .cookie("satori_token", this.contest.satori.satoriToken)
                    .get();
            StringBuilder contentBuilder = new StringBuilder();
            StringBuilder parsedContentBuilder = new StringBuilder();
            for(Element child: doc.body().getElementsByClass("mainsphinx").first().children()){
                contentBuilder.append(child.toString()).append("\n");
                parsedContentBuilder.append(parse(child)).append("\n");
            }
            this.content = contentBuilder.toString();
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
                answer.append(childText).append("\n");
            }
        }else{
            answer.append(element.text().replace("\\(", "").replace("\\)", "").replace("\\le", "<="));
        }
        return answer.toString();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.code + " " + this.name;
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
}