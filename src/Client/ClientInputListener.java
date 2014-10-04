package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientInputListener implements Runnable {

    private final PrintWriter out;

    public ClientInputListener(Socket socket, String IP) throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);

        //out.println("Username : " + IP + " with username: [USER] connected to the server.");
        out.println("");
    }

    public void run() {
        boolean isMessageCorrect;
        Scanner inputScanner = new Scanner(System.in);
        String message;

        //Get user input for username
        out.println(inputScanner.nextLine());

        while (true) {
            isMessageCorrect = true;
            
            //Get input from user
            message = inputScanner.nextLine();
            
            //Splits the string for parsing
            String[] input = message.split("\\s");

            switch (input[0]) {
                case "POST":
                    if (input.length < 2) {
                        isMessageCorrect = false;
                        System.out.println("Error: there is no meesage to be sent.");
                    }
                    break;
                case "PM":
                    if (input.length < 3) {
                        isMessageCorrect = false;
                        System.out.println("Error: there is no meesage to be sent.");
                    }
                    break;
                default:
                    isMessageCorrect = false;
                    System.out.println("Error: Invalid message format.");
            }
            if (isMessageCorrect) {
                out.println(message);
            }
        }
    }
}
