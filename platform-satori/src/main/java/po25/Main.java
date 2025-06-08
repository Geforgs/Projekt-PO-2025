package po25;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        SatoriPlatform satori = new SatoriPlatform();
        try{
            List<Contest> contests = satori.getAllContests();

            Contest contest = contests.get(0);
            List<Task> tasks = contest.getTasks();
            Task task = tasks.get(0);
            String content = task.getContent();

            System.out.println(content);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
