package po25;

/*
* Każda platforma powinna mieć:
* - nazwa platformy (string)
* - login i hasło, przechowywane w bezpiecznym miejscu [ewentualnie logujesz się za każdym razem]
* - system logowania się na daną stronę/przechowywania tokenu
* - system sprawdzania czy obecna sesja jest aktualna
* - pobieranie wszystkich kontestów
* - metoda zwracająca dany kontest [(string/identyfikator) -> kontest]
* */

import java.util.List;
import java.util.Optional;

/**
 * Interfejs reprezentujący platformę konkursową (np. Codeforces, Satori).
 * Odpowiada za interakcję z daną platformą.
 */
public interface Platform {

    /**
     * Zwraca nazwę platformy.
     * @return nazwa platformy.
     */
    String getPlatformName();

    /**
     * Loguje użytkownika na platformę.
     * Implementacja powinna bezpiecznie obsługiwać dane uwierzytelniające.
     * Może rzucać wyjątki w przypadku niepowodzenia logowania (np. PlatformLoginException).
     * @param username nazwa użytkownika.
     * @param password hasło użytkownika.
     */
    void login(String username, String password) throws PlatformException; // Rozważ dedykowany wyjątek

    /**
     * Sprawdza, czy obecna sesja użytkownika jest nadal aktywna/ważna.
     * @return true, jeśli sesja jest ważna, false w przeciwnym razie.
     */
    boolean isSessionValid();

    /**
     * Wylogowuje użytkownika z platformy.
     */
    void logout();

    /**
     * Pobiera listę wszystkich dostępnych (lub np. obserwowanych) konkursów na platformie.
     * Może rzucać wyjątki w przypadku problemów z siecią lub API (np. PlatformRequestException).
     * @return lista obiektów Contest.
     */
    List<Contest> getAllContests() throws PlatformException;

    /**
     * Pobiera konkretny konkurs na podstawie jego identyfikatora.
     * @param contestId identyfikator konkursu.
     * @return Optional zawierający obiekt Contest, jeśli konkurs o danym ID istnieje, w przeciwnym razie pusty Optional.
     */
    Optional<Contest> getContestById(String contestId) throws PlatformException;

    // --- Dodatkowe metody wynikające z ogólnego opisu projektu ---

    /**
     * Przesyła rozwiązanie zadania na platformę.
     * (Wymaga zdefiniowania klas Solution i Submission lub podobnych)
     * @param task zadanie, do którego przesyłane jest rozwiązanie.
     * @param solutionCode kod źródłowy rozwiązania.
     * @param languageId identyfikator języka programowania.
     * @return identyfikator przesłanego zgłoszenia.
     */
    // String submitSolution(Task task, String solutionCode, String languageId) throws PlatformException;

    /**
     * Pobiera status/wynik konkretnego zgłoszenia.
     * (Wymaga zdefiniowania klasy SubmissionResult lub podobnej)
     * @param submissionId identyfikator zgłoszenia.
     * @return obiekt reprezentujący wynik zgłoszenia.
     */
    // SubmissionResult getSubmissionStatus(String submissionId) throws PlatformException;

    /**
     * Pobiera historię zgłoszeń dla danego zadania lub użytkownika.
     * @return lista zgłoszeń.
     */
    // List<Submission> getSubmissionHistory(Task task) throws PlatformException;
}

/**
 * Generyczny wyjątek dla operacji na platformie.
 * Można stworzyć bardziej szczegółowe wyjątki dziedziczące po tym.
 */
class PlatformException extends Exception {
    public PlatformException(String message) {
        super(message);
    }

    public PlatformException(String message, Throwable cause) {
        super(message, cause);
    }
}