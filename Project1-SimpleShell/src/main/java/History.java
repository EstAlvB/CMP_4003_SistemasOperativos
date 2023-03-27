import java.util.ArrayList;

public class History {
    private ArrayList<String> list;
    private int n;

    public History() {
        list = new ArrayList<String>(20);
        n = 0;
    }

    public String getHistory() {
        if (!list.isEmpty()) {
            String out = "here is a list of your commands:\nEnter !<number> to get your command.\n";
            int i = 1;
            for (String command : list) {
                out += i + ") " + command + "\n";
                i++;
            }
            return out;
        }
        return "no history available!";
    }

    public void addCommand(String cmd) {
        if (n == 20) {
            list.remove(0);
        }
        list.add(cmd);
        n++;
    }

    public String getCommand(String str) {
        String result = "";
        if (n != 0) {
            if (str.equals("!#")) {
                result = list.get(n-1);
            } else {
                result = list.get(Character.getNumericValue(str.charAt(1))-1);
            }
        } else {
            result = "no command history available!";
        }
        return result;
    }
}
