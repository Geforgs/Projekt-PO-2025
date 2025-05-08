package po25;

/*
* - nazwa zadania
* - treść
* - wyjście/wejście (opcjonalnie)
* - maks. czas wykonywania/limity czasowe (opcjonalnie)
* - limity pamięciowe (opcjonalnie)
* */

import java.util.Optional;

public interface Task {

    /**
     * Zwraca nazwę zadania.
     * @return nazwa zadania.
     */
    String getName();

    /**
     * Zwraca treść zadania. Może zawierać HTML lub inny format zależny od platformy.
     * @return treść zadania.
     */
    String getContent();

    /**
     * Zwraca przykładowe dane wejściowe dla zadania, jeśli są dostępne.
     * @return Optional zawierający przykładowe wejście lub pusty Optional.
     */
    Optional<String> getSampleInput();

    /**
     * Zwraca przykładowe dane wyjściowe dla zadania, jeśli są dostępne.
     * @return Optional zawierający przykładowe wyjście lub pusty Optional.
     */
    Optional<String> getSampleOutput();

    /**
     * Zwraca limit czasowy na wykonanie zadania, jeśli jest określony.
     * Format może być różny (np. "1s", "2000ms").
     * @return Optional zawierający limit czasowy lub pusty Optional.
     */
    Optional<String> getTimeLimit();

    /**
     * Zwraca limit pamięciowy dla zadania, jeśli jest określony.
     * Format może być różny (np. "256MB").
     * @return Optional zawierający limit pamięci lub pusty Optional.
     */
    Optional<String> getMemoryLimit();
}