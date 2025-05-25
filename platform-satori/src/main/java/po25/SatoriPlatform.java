package po25;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;

public class SatoriPlatform implements Platform {
    protected String satoriToken;
    private boolean loggedIn = false;
    protected final String url="https://satori.tcs.uj.edu.pl";
    private Map<String, SatoriContest> contests;
    private boolean loaded = false;
    private boolean loadedSubmissions = false;
    private Map<String, SatoriSubmission> submissions;

    @Override
    public String getPlatformName() {
        return "Satori";
    }

    @Override
    public void login(String username, String password) throws PlatformException{
        try{
            Connection.Response res = Jsoup
                    .connect(url + "/login")
                    .data("login", username, "password", password)
                    .method(Connection.Method.POST)
                    .execute();
            Map<String, String> cookies = res.cookies();
            satoriToken = cookies.get("satori_token");
            if(satoriToken == null) throw new PlatformException("Login failed");
            loggedIn = true;
        }catch (Exception e){
            throw new PlatformException("Login failed");
        }
    }

    @Override
    public boolean isSessionValid(){
        return this.loggedIn;
    }

    @Override
    public void logout(){
        this.loggedIn = false;
    }

    private void loadContests() throws PlatformException {
        try{
            this.loaded = false;
            contests = new HashMap<>();
            Document doc = Jsoup.connect(url + "/contest/select")
                    .cookie("satori_token", satoriToken)
                    .get();
            Element table = doc.select("div[id=content]").select("table").select("tbody").first();
            for(Element tableRow : table.children()){
                if(tableRow.child(0).text().equals("Name")) continue;
                String unparsedId = tableRow.child(0).select("a").attr("href");
                StringBuilder parsedId = new StringBuilder();
                for(int i=9;i<unparsedId.length()-1;i++){
                    parsedId.append(unparsedId.charAt(i));
                }
                contests.put(parsedId.toString(), new SatoriContest(parsedId.toString(), tableRow.child(0).text(), tableRow.child(1).text(), this));
            }
            this.loaded = true;
        }catch (Exception e){
            throw new PlatformException("get all contests failed");
        }
    }

    private void loadSubmissions() throws PlatformException {
        try{
            if(!this.loaded) loadContests();
            this.loadedSubmissions = false;
            submissions = new HashMap<>();
            for(SatoriContest contest: contests.values()){
                for(Submission submission: contest.getSubmissionHistory()){
                    submissions.put(((SatoriSubmission) submission).getId(), (SatoriSubmission) submission);
                }
            }
            this.loadedSubmissions = true;
        }catch (Exception e){
            throw new PlatformException("get all contests failed");
        }
    }

    public void reload() throws PlatformException {
        this.loadContests();
        this.loadSubmissions();
    }

    @Override
    public List<Contest> getAllContests() throws PlatformException{
        if(!loaded) loadContests();
        return new ArrayList<>(contests.values());
    }

    @Override
    public Optional<Contest> getContestById(String contestId) throws PlatformException{
        if(!loaded) loadContests();
        return Optional.of(contests.get(contestId));
    }

    Submission submitSolution(Task task, String path, String languageId) throws PlatformException{
        return ((SatoriTask) task).submit(path);
    }

    Submission getSubmission(String submissionId) throws PlatformException{
        if(!loadedSubmissions) loadSubmissions();
        return this.submissions.get(submissionId);
    }

    List<Submission> getSubmissionHistory() throws PlatformException{
        if(!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }
}
