package po25;

import java.util.List;
import java.util.Scanner;

/**
 * Prosty launcher, który korzysta z CodeforcesPlatform,
 * wyświetla listę konkursów oraz ich zadania.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // 1) Stwórz instancję platformy
            CodeforcesPlatform cf = new CodeforcesPlatform();
            cf.login("", "");  // no-op

            if (!cf.isSessionValid()) {
                System.err.println("Nie udało się zalogować na Codeforces.");
                return;
            }

            // 2) Pobierz wszystkie konkursy (BEFORE i FINISHED)
            List<Contest> contests = cf.getAllContests();
            System.out.println("=== Konkursy Codeforces ===");
            for (int i = 0; i < contests.size(); i++) {
                System.out.printf("%2d) %s%n", i+1, contests.get(i).getTitle());
            }

            // 3) Wybór konkursu
            Scanner sc = new Scanner(System.in);
            System.out.print("Wybierz numer konkursu: ");
            int choice = sc.nextInt() - 1;
            Contest sel = contests.get(choice);

            // 4) Wypisz zadania
            System.out.println("\n=== Zadania w „" + sel.getTitle() + "” ===");
            for (Task t : sel.getTasks()) {
                System.out.printf(" • %s → %s%n", t.getName(), t.getContent());
            }

            cf.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
