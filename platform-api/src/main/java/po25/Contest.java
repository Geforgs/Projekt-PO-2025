package po25;

/*
* - lista zadań
* - getter lista zadań
* - tytuł kontestu
* - getTitle
* - opis (opcjonalnie)
* - metoda zwracająca Task dla id [(id Task) -> Task]
* - data rozpoczęcia/zakończenia (opcjonalnie)
* */

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime; // Potrzebny import dla daty i czasu

/**
 * Interfejs reprezentujący pojedynczy konkurs (zawody).
 */
public interface Contest {

    /**
     * Zwraca tytuł konkursu.
     * @return tytuł konkursu.
     */
    String getTitle();

    /**
     * Zwraca listę zadań dostępnych w ramach tego konkursu.
     * @return lista obiektów Task.
     */
    List<Task> getTasks();

    /**
     * Zwraca opis konkursu, jeśli jest dostępny.
     * @return Optional zawierający opis lub pusty Optional.
     */
    Optional<String> getDescription();

    /**
     * Wyszukuje i zwraca zadanie na podstawie jego identyfikatora.
     * @param taskId identyfikator zadania.
     * @return Optional zawierający obiekt Task, jeśli zadanie o danym ID istnieje, w przeciwnym razie pusty Optional.
     */
    Optional<Task> getTaskById(String taskId);

    /**
     * Zwraca datę i czas rozpoczęcia konkursu, jeśli są określone.
     * @return Optional zawierający datę i czas rozpoczęcia lub pusty Optional.
     */
    Optional<LocalDateTime> getStartTime();

    /**
     * Zwraca datę i czas zakończenia konkursu, jeśli są określone.
     * @return Optional zawierający datę i czas zakończenia lub pusty Optional.
     */
    Optional<LocalDateTime> getEndTime();
}