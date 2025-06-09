package po25;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Browser {
    private static boolean started = false;
    private static Process process;
    private static String pathToChrome;

    static public ChromeDriver getChrome(){
        if(!started){
            throw new RuntimeException("Browser is not started");
        }
        ChromeOptions options = new ChromeOptions().setBinary(pathToChrome);
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        return new ChromeDriver(options);
    }

    static public void setPathToChrome(String pathToChrome){
        Browser.pathToChrome = pathToChrome;
    }

    static public void start() throws IOException {
        List<String> commands = new ArrayList<>();
        commands.add(pathToChrome);
        commands.add("--remote-debugging-port=9222");
        commands.add("--user-data-dir=" + System.getProperty("user.dir") + "/browser/src/main/java/po25/cash");
        ProcessBuilder build = new ProcessBuilder(commands);
        process = build.start();
        started = true;
    }

    static public void stop(){
        process.destroy();
        started = false;
    }
}
