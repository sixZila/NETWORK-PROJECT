package Client;

import java.awt.image.BufferedImage;
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
    private final Socket socket;

    public ClientInputListener(Socket socket, Scanner inputScanner) throws IOException {
        this.inputScanner = inputScanner;
        this.socket = socket;
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
                switch (sendMessage[0]) {
                    case "\"FILE\"":
                        try {
                            File file = new File(buildDirectory(sendMessage, 1));

                            //Get bytes of the file to be sent
                            byte[] outFile = new byte[(int) file.length()];
                            if (file.length() < socket.getReceiveBufferSize()) {
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
                            } else {
                                System.out.println("Error: file is too large.");
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            out.writeUTF(message);
                        } catch (FileNotFoundException ex) {
                            System.out.println("Error: file not found.");
                        } catch (IOException ex) {
                            Logger.getLogger(ClientInputListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "\"IMG\"":
                        try {
                            File file = new File(buildDirectory(sendMessage, 1));

                            FileInputStream fileInput = new FileInputStream(file);
                            BufferedInputStream fileReader = new BufferedInputStream(fileInput);

                            byte[] outFile = new byte[(int) file.length()];

                            out.writeUTF(message);

                            out.writeLong(file.length());

                            fileReader.read(outFile, 0, outFile.length);

                            out.write(outFile, 0, outFile.length);

                            out.flush();

                            fileReader.close();
                        } catch (ArrayIndexOutOfBoundsException e) {
                            out.writeUTF(message);
                        } catch (FileNotFoundException ex) {
                            System.out.println("Error: file not found.");
                        } catch (IOException ex) {
                            Logger.getLogger(ClientInputListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    default:
                        out.writeUTF(message);
                        break;
                }

            } catch (IOException ex) {
                Logger.getLogger(ClientInputListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String buildDirectory(String[] input, int start) {
        StringBuilder message = new StringBuilder();

        for (int i = start; i < input.length; i++) {
            message.append(input[i]);
            if (i < input.length - 1) {
                message.append(" ");
            }
        }
        return message.toString();
    }
}
