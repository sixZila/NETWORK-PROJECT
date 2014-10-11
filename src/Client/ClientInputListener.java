package Client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientInputListener implements Runnable {

    private final DataOutputStream out;
    private final Scanner inputScanner;

    public ClientInputListener(Socket socket, Scanner inputScanner) throws IOException {
        this.inputScanner = inputScanner;
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {

        String message;

        while (true) {
            //Get input from user
            message = inputScanner.nextLine();

            try {

                String[] sendMessage = message.split("\\s");
                if (sendMessage[0].equals("\"FILE\"")) {
                    try {
                        File file = new File(sendMessage[1]);

                        //Get bytes of the file to be sent
                        byte[] outFile = new byte[(int) file.length()];
                        FileInputStream fileInput = new FileInputStream(file);
                        BufferedInputStream fileReader = new BufferedInputStream(fileInput);

                        //Send the message
                        out.writeUTF(message);

                        //Read the file
                        fileReader.read(outFile, 0, outFile.length);

                        //Send the file
                        out.write(outFile, 0, outFile.length);

                        //Flush buffer
                        out.flush();
                        fileReader.close();
                    } catch (ArrayIndexOutOfBoundsException e) {

                    } catch (FileNotFoundException ex) {
                        System.out.println("Error: file not found.");
                    } catch (IOException ex) {
                        Logger.getLogger(ClientInputListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    out.writeUTF(message);
                }

            } catch (IOException ex) {
                Logger.getLogger(ClientInputListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
