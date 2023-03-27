import java.util.ArrayList;

public class Processor {

    private String commandLine;
    private ArrayList<String> commandParts;

    public Processor(String commandLine) {
        this.commandLine = commandLine;
        commandParts = new ArrayList<String>();
    }

    public String prepare() {
        String[] a = commandLine.split(" ");
        for (String str : a) {
            commandParts.add(str);
        }
        return commandParts.get(0);
    }

    public ArrayList<String> getCommandParts() {
        return commandParts;
    }
}
