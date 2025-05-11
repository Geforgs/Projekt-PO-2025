package po25;

import java.util.Optional;

/**
 * Implementacja interfejsu Task dla Codeforces.
 */
class CfTask implements Task {
    private final String name;
    private final String contentUrl;

    CfTask(String name, String url) {
        this.name = name;
        // content (treść) pomijamy – będziemy ewentualnie ładować przez HTTP
        this.contentUrl = url;
    }

    CfTask(String index, String name, String url) {
        this.name = index + " - " + name;
        this.contentUrl = url;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContent() {
        // można pobrać HTML zadania przez Jsoup, ale na razie zwracamy URL
        return "Problem URL: " + contentUrl;
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

    public String getUrl() {
        return contentUrl;
    }
}
