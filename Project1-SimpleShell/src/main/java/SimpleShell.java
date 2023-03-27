/**
 * @author gabriel-lara
 * @author alexander-pastor
 * @author esteban-alvarado
 */
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SimpleShell {
    public static void main(String[] args) {
        
        String user = System.getProperty("user.name");
        String currentPath = System.getProperty("user.dir");
        History commandHistory = new History();
        File dir = new File(currentPath);
        
        while (true) {
            ShowInterface(currentPath + "\n|" + user + "|" + " > ");
            Scanner sc = new Scanner(System.in);
            String request = sc.nextLine();
            
            // condicionales puestos en funcion aparte, para ejecucion por partes
            // aplicar segunda funcion para ejecutar un request
            for (String command : splitRequests(request)) {
                if (command.equals(""))
                    continue;
                if (command.equals("exit")) {
                    showMessage("Goodbye", command);
                    System.exit(0);
                }
                if (command.equals("history")) {
                    showMessage(commandHistory.getHistory(), command);
                    continue;
                }
                if (command.startsWith("!") && request.length() == 2) {
                    showMessage(commandHistory.getCommand(request), command);
                    continue;
                } else {
                    commandHistory.addCommand(request);
                }

                Processor p = new Processor(command);
                String operation = p.prepare();
                Command c = new Command(p.getCommandParts(), dir);
                showMessage(c.executeTask(operation), command);
                dir = c.getDir();
                currentPath = c.getCanonicalPath();
            }
        }
    }
    
    public static void ShowInterface(String message) {
        System.out.print("-----------------------------------------------------------\n" + message);
    }
    
    public static void showMessage(String message, String request) {
        System.out.println(request + ":\n" + message);
    }

    public static String[] splitRequests(String request) {
        return Arrays.stream(request.split("[$]"))
                .map(str -> str.trim())
                .collect(Collectors.toList())
                .toArray(String[]::new);
    }
}
