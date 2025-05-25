package po25;

import org.jsoup.Jsoup;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacja interfejsu Platform dla Codeforces.
 */
public class CodeforcesPlatform implements Platform {
    private static final String API_BASE = "https://codeforces.com/api";
    private boolean loggedIn = false;

    @Override
    public String getPlatformName() {
        return "Codeforces";
    }

    @Override
    public void login(String username, String password) throws PlatformException {
        loggedIn = true;
    }

    @Override
    public boolean isSessionValid() {
        return loggedIn;
    }

    @Override
    public void logout() {
        loggedIn = false;
    }

    @Override
    public List<Contest> getAllContests() throws PlatformException {
        try {
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
            List<Contest> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject c = arr.getJSONObject(i);
                String id       = c.getString("id");
                String name   = c.getString("name");
                String phase  = c.getString("phase");
                long startSec = c.getLong("startTimeSeconds");
                int  durSec   = c.getInt("durationSeconds");

                ZonedDateTime start = ZonedDateTime.ofInstant(
                        Instant.ofEpochSecond(startSec),
                        ZoneId.systemDefault());
                ZonedDateTime end   = start.plusSeconds(durSec);

                if ("BEFORE".equals(phase) || "FINISHED".equals(phase)) {
                    list.add(new CfContest(id, name, start, end, this));
                }
            }
            return list;
        } catch (IOException e) {
            throw new PlatformException("Błąd sieciowy przy pobieraniu listy konkursów", e);
        }
    }

    @Override
    public Optional<Contest> getContestById(String contestId) throws PlatformException {
        return getAllContests().stream()
                .filter(c -> c.getId().equals(contestId))
                .findFirst();
    }

    /**
     * Wewnętrzna metoda pobierająca zadania dla danego contestu.
     */
    List<Task> fetchTasks(CfContest contest) throws PlatformException {
        try {
            String url = API_BASE + "/contest.standings"
                    + "?contestId=" + contest.getId()
                    + "&from=1&count=1000";
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

            JSONArray problems = root
                    .getJSONObject("result")
                    .getJSONArray("problems");

            List<Task> tasks = new ArrayList<>();
            for (int i = 0; i < problems.length(); i++) {
                JSONObject p = problems.getJSONObject(i);
                String index = p.getString("index");
                String name  = p.getString("name");
                String link  = "https://codeforces.com/contest/"
                        + contest.getId()
                        + "/problem/" + index;
                tasks.add(new CfTask(index, name, link));
            }
            return tasks;
        } catch (IOException e) {
            throw new PlatformException("Błąd pobierania zadań", e);
        }
    }
}
