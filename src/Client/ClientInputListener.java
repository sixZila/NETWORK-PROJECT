package Client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientInputListener implements Runnable {

    private final PrintWriter out;
    private final Scanner inputScanner;
    private final Socket socket;

    public ClientInputListener(Socket socket, Scanner inputScanner) throws IOException {
        this.inputScanner = inputScanner;
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        String message;

        while (true) {
            //Get input from user
            message = inputScanner.nextLine();

            String[] sendMessage = message.split("\\s");
            if (sendMessage[0].equals("\"FILE\"")) {
                try {
                    File file = new File(sendMessage[1]);

                    //Get bytes of the file to be sent
                    byte[] outFile = new byte[(int) file.length()];
                    if (file.length() < socket.getReceiveBufferSize()) {
                        FileInputStream fileInput = new FileInputStream(file);
                        BufferedInputStream fileReader = new BufferedInputStream(fileInput);
                        DataOutputStream outWriter = new DataOutputStream(socket.getOutputStream());

                        //Send the message
                        out.println(message);

                        //Read the file
                        fileReader.read(outFile, 0, outFile.length);

                        //Send the file
                        outWriter.write(outFile, 0, outFile.length);
                        //Flush buffer
                        outWriter.flush();
                        fileReader.close();
                    } else {
                        System.out.println("Error: file is too large.");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    out.println(message);
                } catch (FileNotFoundException ex) {
                    System.out.println("Error: file not found.");
                } catch (IOException ex) {
                    Logger.getLogger(ClientInputListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                out.println(message);
            }
        }
    }
}
