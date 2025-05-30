package po25;

import org.jsoup.Jsoup;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementacja interfejsu Platform dla Codeforces.
 */
public class CodeforcesPlatform implements Platform {
    protected static final String API_BASE = "https://codeforces.com/api";
    private static final String url = "https://codeforces.com";
    private boolean loggedIn = false;
    private boolean loadedSubmissions = false;
    private boolean loaded = false;
    protected String username;
    private Map<String, CfContest> contests = new HashMap<>();
    private Map<String, CfSubmission> submissions = new HashMap<>();

    @Override
    public String getPlatformName() {
        return "Codeforces";
    }

    @Override
    public void login(String username, char[] password) throws PlatformException {
        String stringPassword = new String(password);
        Arrays.fill(password, ' ');

        ChromeDriver driver = Browser.getChrome();
        this.username = username;
        try{
            driver.get(url + "/enter");
            driver.navigate().refresh();
            int timeout = 16000;
            while(true){
                if(driver.getTitle().equals("Codeforces")) {
                    driver.get(url + "/enter");
                    timeout = 1000;
                    Thread.sleep(timeout);
                }else if(driver.getTitle().equals("Login - Codeforces")){
                    break;
                }else{
                    Thread.sleep(timeout);
                    timeout *= 2;
                }
            }
            driver.findElement(By.id("handleOrEmail")).sendKeys(username);
            driver.findElement(By.id("password")).sendKeys(stringPassword);
            driver.findElement(By.className("submit")).click();
        }catch (Exception e){
            throw new PlatformException(e.getMessage());
        } finally {
            driver.quit();
        }
        loggedIn = true;
    }

    @Override
    public boolean isSessionValid() {
        return loggedIn;
    }

    @Override
    public void logout() throws PlatformException  {
        ChromeDriver driver = Browser.getChrome();
        try{
            driver.get(url);
            Thread.sleep(1000);
            driver.get(driver.findElement(By.xpath("//a[text()='Logout']")).getAttribute("href").toString());
        }catch (Exception e){
            throw new PlatformException(e.getMessage());
        } finally {
            driver.quit();
        }
        loggedIn = false;
    }

    private void loadContests() throws PlatformException {
        try {
            Map<String, CfContest> newContests = new HashMap<>();
            String url = API_BASE + "/contest.list?gym=false";
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

            JSONArray arr = root.getJSONArray("result");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject c = arr.getJSONObject(i);
                String id       = Long.toString(c.getLong("id"));
                String name   = c.getString("name");
                String phase  = c.getString("phase");
                long startSec = c.getLong("startTimeSeconds");
                int  durSec   = c.getInt("durationSeconds");

                ZonedDateTime start = ZonedDateTime.ofInstant(
                        Instant.ofEpochSecond(startSec),
                        ZoneId.systemDefault());
                ZonedDateTime end   = start.plusSeconds(durSec);

                if ("BEFORE".equals(phase) || "FINISHED".equals(phase)) {
                    newContests.put(id, new CfContest(id, name, start, end, this));
                }
            }
            this.contests = newContests;
            this.loaded = false;
        } catch (IOException e) {
            throw new PlatformException("Błąd sieciowy przy pobieraniu listy konkursów", e);
        }
    }

    @Override
    public List<Contest> getAllContests() throws PlatformException {
        if(!loaded) loadContests();
        return new ArrayList<>(contests.values());
    }

    @Override
    public Optional<Contest> getContestById(String contestId) throws PlatformException {
        return getAllContests().stream()
                .filter(c -> c.getId().equals(contestId))
                .findFirst();
    }

    @Override
    public Submission submitSolution(Task task, String path, String languageId) throws PlatformException{
        return task.submit(path);
    }

    @Override
    public Submission getSubmission(String submissionId) throws PlatformException{
        if(!loadedSubmissions) loadSubmissions();
        return this.submissions.get(submissionId);
    }

    @Override
    public List<Submission> getSubmissionHistory() throws PlatformException{
        if(!loadedSubmissions) loadSubmissions();
        return new ArrayList<>(submissions.values());
    }

    private void loadSubmissions() throws PlatformException {
        if(!this.loaded) loadContests();
        Map<String, CfSubmission> newSubmissions = new HashMap<>();
        this.loadedSubmissions = false;
        for(CfContest contest: contests.values()){
            contest.loadSubmissions();
            for(Submission submission: contest.getSubmissionHistory()){
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
}
