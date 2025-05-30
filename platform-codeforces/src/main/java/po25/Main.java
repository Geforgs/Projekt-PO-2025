package po25;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String COOKIE_FILE =
            System.getProperty("user.home") + "/.cf-cookies";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        /* 1) – cookies */
        String cookieStr = null;
        if (!java.nio.file.Files.exists(Paths.get(COOKIE_FILE))) {
            System.out.println("""
                Skopiuj nagłówek 'cookie:' z DevTools (Network → Headers)
                i wklej go w jednej linii poniżej:
                """);
            System.out.print("Cookies → ");
            cookieStr = sc.nextLine();
        }

        CodeforcesPlatform cf = new CodeforcesPlatform();
        try {
            cf.login("dummy", cookieStr.toCharArray());      // drugi parametr = cookie string
            System.out.println("Zalogowano!\n");

            /* 2) – konkursy */
            List<Contest> contests = cf.getAllContests();
            for (int i = 0; i < contests.size(); i++)
                System.out.printf("%3d) %s%n", i + 1, contests.get(i).getTitle());
            System.out.print("Wybierz konkurs: ");
            int ci = sc.nextInt() - 1;
            Contest sel = contests.get(ci);

            /* 3) – zadania */
            List<Task> tasks = sel.getTasks();
            System.out.println("\n=== Zadania ===");
            for (int i = 0; i < tasks.size(); i++) {
                CfTask t = (CfTask) tasks.get(i);
                System.out.printf("%2d) [%s] %s%n", i + 1, t.getId(), t.getName());
            }

            /* 4) – submit? */
            System.out.print("Submit? (t/n): ");
            if (sc.next().equalsIgnoreCase("t")) {
                System.out.print("Nr zadania: "); int ti = sc.nextInt() - 1; sc.nextLine();
                CfTask t = (CfTask) tasks.get(ti);

                System.out.print("Plik źródłowy: ");
                String path = sc.nextLine();
                String code = Files.readString(Paths.get(path));

                System.out.print("programTypeId (np. 64 = G++17): ");
                String lang = sc.nextLine();


            }
        } catch (Exception e) {
            System.err.println("Błąd: " + e.getMessage());
            e.printStackTrace();
        } finally { }
    }
}
