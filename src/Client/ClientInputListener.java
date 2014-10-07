package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientInputListener implements Runnable {

    private final PrintWriter out;
    Scanner inputScanner;

    public ClientInputListener(Socket socket, Scanner inputScanner) throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        this.inputScanner = inputScanner;
    }

    public void run() {

        String message;

        while (true) {
            //Get input from user
            message = inputScanner.nextLine();
            out.println(message);
        }
    }
}
