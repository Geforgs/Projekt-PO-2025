package po25;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        SatoriPlatform satori = new SatoriPlatform();
        try{
            satori.login("login", "hasło");
            List<Contest> contests = satori.getAllContests();
            System.out.println(contests.get(0).getTasks().get(0).getContent());
//            for(Contest contest : contests) {
//                contest.getTasks();
//            }
        }catch (PlatformException e){
            System.out.println(e.getMessage());
        }
    }
}
