package po25;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SatoriPlatform implements Platform {
    protected String satoriToken;
    private boolean loggedIn = false;
    protected final String url="https://satori.tcs.uj.edu.pl";

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

    @Override
    public List<Contest> getAllContests() throws PlatformException{
        List<Contest> contests = new ArrayList<>();
        try{
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
                contests.add(new SatoriContest(parsedId.toString(), tableRow.child(0).text(), tableRow.child(1).text(), this));
            }
        }catch (Exception e){
            throw new PlatformException("get all contests failed");
        }
        return contests;
    }

    @Override
    public Optional<Contest> getContestById(String contestId) throws PlatformException{
        return getAllContests().stream()
                .filter(c -> c.getId().equals(contestId))
                .findFirst();
    }
}
