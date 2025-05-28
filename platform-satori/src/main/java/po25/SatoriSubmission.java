package po25;

import java.time.LocalDateTime;
import java.util.Optional;

public class SatoriSubmission implements Submission {
    private final SatoriTask task;
    private final String id;
    private final LocalDateTime time;
    private final String briefVerdict;
    private final Optional<String> language;


    protected SatoriSubmission(SatoriTask task, String id, LocalDateTime time, String briefVerdict, Optional<String> language) {
        this.task = task;
        this.id = id;
        this.time = time;
        this.briefVerdict = briefVerdict != null ? briefVerdict : "";
        this.language = language;
    }

    protected SatoriSubmission(SatoriTask task, String id, LocalDateTime time) {
        this(task, id, time, "Status Unknown", Optional.empty());
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

    @Override
    public String getBriefVerdict() {
        return this.briefVerdict;
    }

    @Override
    public Optional<LocalDateTime> getSubmittedAt() {
        return Optional.of(this.time);
    }

    @Override
    public Optional<String> getLanguage() {
        return this.language;
    }
}