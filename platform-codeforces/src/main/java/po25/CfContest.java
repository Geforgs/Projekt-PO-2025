package po25;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Implementacja interfejsu Contest dla Codeforces.
 */
class CfContest implements Contest {
    private final long id;
    private final String title;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final CodeforcesPlatform platform;
    // cache
    private List<Task> tasks = null;

    CfContest(long id, String title,
              java.time.ZonedDateTime startZdt,
              java.time.ZonedDateTime endZdt,
              CodeforcesPlatform platform) {
        this.id = id;
        this.title = title;
        // konwertujemy na LocalDateTime
        this.start = startZdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        this.end   = endZdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        this.platform = platform;
    }

    long getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<Task> getTasks() throws PlatformException {
        if (tasks == null) {
            tasks = platform.fetchTasks(this);
        }
        return tasks;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public Optional<Task> getTaskById(String taskId) throws PlatformException {
        return getTasks().stream()
                .filter(t -> t.getName().startsWith(taskId))
                .findFirst();
    }

    @Override
    public Optional<LocalDateTime> getStartTime() {
        return Optional.of(start);
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.of(end);
    }

    @Override
    public String toString() {
        return title + " (starts: " + start + ")";
    }
}
