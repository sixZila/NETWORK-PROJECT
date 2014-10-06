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
        Scanner inputScanner = new Scanner(System.in);
        String message;

        while (true) {
            //Get input from user
            message = inputScanner.nextLine();
            out.println(message);
        }
    }
}
