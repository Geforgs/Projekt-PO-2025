package po25;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        SatoriPlatform satori = new SatoriPlatform();
        try{
            satori.login("login", "hasło");
            List<Contest> contests = satori.getAllContests();
            for (Contest contest : contests) {
                if(contest.getId().equals("9410000")){
                    for(Task task: contest.getTasks()){
                        if(task.getId().equals("9419710")){
//                    ((SatoriTask) task).submit("/Users/pavlotsikalyshyn/Documents/ClionProject/Codeforces/main.cpp");
                            System.out.println(((SatoriTask) task).getSubmissionHistory().size());
                        }
                    }
                }
            }
//            for(Contest contest : contests) {
//                contest.getTasks();
//            }
        }catch (PlatformException e){
            System.out.println(e.getMessage());
        }
    }
}
