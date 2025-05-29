package po25;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Browser {
    private static boolean started = true;

    static public ChromeDriver getChrome(){
        if(!started){
            throw new RuntimeException("Browser is not started");
        }
        String pahtToChrome = "";
        if(System.getProperty("os.name").startsWith("Mac")){
            pahtToChrome = System.getProperty("user.dir") + "/browser/src/main/java/po25/MacOsChrome/Google Chrome.app/Contents/MacOS/Google Chrome";
        }else if(System.getProperty("os.name").startsWith("Windows")){
            System.out.println("Soon will be added to the chrome driver");
            throw new RuntimeException("Windows is not added to the chrome driver");
        }
        ChromeOptions options = new ChromeOptions().setBinary(pahtToChrome);
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        return new ChromeDriver(options);
    }
}
