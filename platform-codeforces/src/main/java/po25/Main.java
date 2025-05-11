package po25;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Prosty launcher w module platform-codeforces:
 * - wybiera konkurs
 * - wyświetla listę zadań
 * - pozwala wybrać zadanie i wyświetlić jego szczegóły
 * Obsługuje niepoprawne dane wejściowe i powtarza zapytanie.
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            CodeforcesPlatform cf = new CodeforcesPlatform();
            cf.login("", "");  // no-op
            if (!cf.isSessionValid()) {
                System.err.println("Nie udało się zalogować.");
                return;
            }

            List<Contest> contests = cf.getAllContests();
            if (contests.isEmpty()) {
                System.out.println("Brak dostępnych konkursów.");
                return;
            }
            System.out.println("=== Konkursy Codeforces ===");
            for (int i = 0; i < contests.size(); i++) {
                System.out.printf("%2d) %s%n", i + 1, contests.get(i).getTitle());
            }

            int cIdx = readIntInRange(sc,
                    "Wybierz numer konkursu (1-" + contests.size() + "): ",
                    1, contests.size()) - 1;
            Contest selectedContest = contests.get(cIdx);

            List<Task> tasks = selectedContest.getTasks();
            if (tasks.isEmpty()) {
                System.out.println("Brak zadań w wybranym konkursie.");
                cf.logout();
                return;
            }
            System.out.println("\n=== Zadania w „" + selectedContest.getTitle() + "” ===");
            for (int i = 0; i < tasks.size(); i++) {
                System.out.printf("%2d) %s%n", i + 1, tasks.get(i).getName());
            }

            int tIdx = readIntInRange(sc,
                    "Wybierz numer zadania (1-" + tasks.size() + "): ",
                    1, tasks.size()) - 1;
            Task chosen = tasks.get(tIdx);

            System.out.println("\n--- " + chosen.getName() + " ---");
            System.out.println(chosen.getContent());
            chosen.getTimeLimit().ifPresent(tl -> System.out.println("Limit czasu: " + tl));
            chosen.getMemoryLimit().ifPresent(ml -> System.out.println("Limit pamięci: " + ml));
            chosen.getSampleInput().ifPresent(inp -> System.out.println("\nPrzykładowe wejście:\n" + inp));
            chosen.getSampleOutput().ifPresent(out -> System.out.println("\nPrzykładowe wyjście:\n" + out));

            cf.logout();
        } catch (PlatformException e) {
            System.err.println("Błąd platformy: " + e.getMessage());
        } finally {
            sc.close();
        }
    }

    private static int readIntInRange(Scanner sc, String prompt, int min, int max) {
        int value;
        while (true) {
            System.out.print(prompt);
            try {
                value = sc.nextInt();
                if (value >= min && value <= max) {
                    sc.nextLine();
                    return value;
                } else {
                    System.out.println("Wpisz liczbę z przedziału [" + min + ", " + max + "].");
                }
            } catch (InputMismatchException ex) {
                System.out.println("Niepoprawny format. Podaj liczbę całkowitą.");
                sc.nextLine();
            }
        }
    }
}