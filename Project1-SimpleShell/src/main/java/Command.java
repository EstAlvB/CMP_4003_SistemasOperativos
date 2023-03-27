import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Command {

    private ProcessBuilder pb;
    ArrayList<String> commandParts;
    File dir;

    public Command(ArrayList<String> commandParts, File dir) {
        this.commandParts = commandParts;
        pb = new ProcessBuilder(commandParts);
        this.dir = dir;
    }

    public String executeTask(String operation) {
        String result = "";
        switch (operation) {
            case "cd":
                result = cd();
                break;
            case "ls":
                result = ls();
                break;
            case "ping":
                result = ping();
                break;
            case "echo":
                result = echo();
                break;
            case "ipconfig":
                if (System.getProperty("os.name").startsWith("Windows"))
                    result = ipconfig();
                break;
            case "ifconfig":
                if (!System.getProperty("os.name").startsWith("Windows"))
                    result = ipconfig();
                break;
            default:
                result = "unknown operation";
                break;
        }
        return result;      
    }

    public String cd() {
        if (commandParts.size() < 2) {
            return "not a valid cd command, try with a new one";
        }
        String newPath = commandParts.get(1).replaceAll("^\"|\"$", "");
        Path p = Paths.get(newPath);
        File newDir;
        if (p.isAbsolute())
            newDir = new File(newPath);
        else
            newDir = new File(dir, newPath);

        if (newDir.exists() || newDir.isDirectory()) {
            dir = newDir;
        }

        String result = "";
        try {
            pb.directory(dir);
            Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String ln;
            while ((ln = br.readLine()) != null)
                result = ln;
            br.close();
        } catch (java.io.IOException e) {
            result = e.getMessage();
        }
        return result;
    }

    public String ls() {
        String result = "";
        File[] files = dir.listFiles();
        if (commandParts.size() > 1){
            if (commandParts.get(1).equals("-l")) {
                SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy");
                try {
                    BasicFileAttributes atribute;
                    for (File file : files) {
                        atribute = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                        String fileData = String.format("%-3s %-3s %6s %-3s %-3s %n", System.getProperty("user.name"),
                                file.getParentFile().getName(),
                                (file.isDirectory()) ? "<DIR>" : atribute.size(),
                                df.format(atribute.lastModifiedTime().toMillis()), file.getName());
                        result += fileData;
                    }
                } catch (java.io.IOException e) {
                    result = e.getMessage();
                }
            }
        } else {
            for (File file : files) {
                result += file.getName() + "\n";
            }   
        }
        return result;
    }

    public String ping() {
        String result = "";
        String host = commandParts.get(1);
        try {
            Process pingCommand = Runtime.getRuntime().exec("ping " + host);
            BufferedReader pingReader = new BufferedReader(new InputStreamReader(pingCommand.getInputStream()));
            for (int i = 0; i < 7; i++) {
                result += pingReader.readLine() + "\n";
            }
        } catch (java.io.IOException e) {
            result = e.getMessage();
        }
        return result;
    }

    public String echo() {
        String result = "";
        if (commandParts.size() != 2) {
            return "not valid echo command, echo receives just one string argument";
        }
        result = "Message from echo: " + commandParts.get(1);
        return result;
    }

    public String ipconfig() {
        String result = "";
        try {
            Process ipCommand = Runtime.getRuntime().exec("ipconfig");
            BufferedReader ipReader = new BufferedReader(new InputStreamReader(ipCommand.getInputStream()));
            while (ipReader.readLine() != null) {
                result += ipReader.readLine() + "\n";
            }
        } catch (java.io.IOException e) {
            result = e.getMessage();
        }
        return result;
    }

    public String getCanonicalPath() {
        String p = "";
        try {
            p = dir.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }
    
    public File getDir() {
        return dir;
    }
   
}