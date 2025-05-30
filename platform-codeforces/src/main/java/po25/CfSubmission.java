package po25;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CfSubmission implements Submission {
    private final CfTask task;
    private final String submissionId;
    private final String url;
    private final LocalDateTime time;
    private String verdict;
    private boolean completed;

    protected CfSubmission(CfTask task, String id, String url, LocalDateTime time) {
        this.task = task;
        this.submissionId = id;
        this.url = url;
        this.time = time;
        verdict = "Waiting";
        completed = false;
    }

    private void loadVerdict(){
        ChromeDriver driver = Browser.getChrome();
        try{
            driver.get(url);
            List<WebElement> verdicts = driver.findElements(By.className("verdict-rejected"));
            if(verdicts.size() > 0){
                verdict = verdicts.get(0).getText();
                completed = true;
            }else{
                verdicts = driver.findElements(By.className("verdict-accepted"));
                if(verdicts.size() > 0){
                    verdict = verdicts.get(0).getText();
                    completed = true;
                }else{
                    verdict = "Waiting";

                }
            }
        }catch (Exception e){
            verdict = "Unknown";
        }finally {
            driver.quit();
        }
    }

    @Override
    public String getSubmissionId() {
        return this.submissionId;
    }

    @Override
    public String getTaskId() {
        return this.task.getId();
    }

    @Override
    public Optional<String> getContestId() {
        return Optional.of(this.task.contest.getId());
    }

    @Override
    public String getVerdict() {
        if(!completed) this.loadVerdict();
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
