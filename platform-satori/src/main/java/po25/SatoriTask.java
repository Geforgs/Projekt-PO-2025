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

    SatoriTask(String id, String code, String name, String url, SatoriContest contest) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.url = url;
        this.contest = contest;
        loaded = false;
        try{
            this.load();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void load() throws PlatformException {
        try{
            Document doc = Jsoup.connect(this.url)
                    .cookie("satori_token", this.contest.satori.satoriToken)
                    .get();
            for(Element child: doc.body().getElementsByClass("mainsphinx").first().children()){
                System.out.println(child);
            }
        }catch(Exception e){
            throw new PlatformException(e.getMessage());
        }
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
    public String getContent() {
        return "";
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
