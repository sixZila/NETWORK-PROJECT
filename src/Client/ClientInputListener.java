package Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientInputListener implements Runnable {

    private final DataOutputStream out;
    private final Socket socket;
    private final OutputStream outputStream;
    private final Scanner inputScanner;

    public ClientInputListener(Socket socket, Scanner inputScanner) throws IOException {
        this.socket = socket;
        this.inputScanner = inputScanner;
        outputStream = socket.getOutputStream();
        out = new DataOutputStream(outputStream);
    }

    @Override
    public void run() {

        String message;

        while (true) {
            //Get input from user
            message = inputScanner.nextLine();
            
            try {
                out.writeUTF(message);
                /*
                String[] sendMessage = message.split("\\s");
                if (sendMessage[0].equals("\"FILE\"")) {
                try {
                File file = new File(sendMessage[1]);
                
                //Get bytes of the file to be sent
                byte[] outFile = new byte[(int) file.length()];
                FileInputStream fileInput = new FileInputStream(file);
                BufferedInputStream fileReader = new BufferedInputStream(fileInput);
                
                //Send the message
                out.println(message);
                
                //Read the file
                fileReader.read(outFile, 0, outFile.length);
                
                //Send the file
                fileSender.write(outFile, 0, outFile.length);
                
                //Flush buffer
                fileSender.flush();
                fileReader.close();
                } catch (ArrayIndexOutOfBoundsException e) {
                
                } catch (FileNotFoundException ex) {
                System.out.println("Error: file not found.");
                } catch (IOException ex) {
                Logger.getLogger(ClientInputListener.class.getName()).log(Level.SEVERE, null, ex);
                }
                } else {
                out.println(message);
                }
                */
            } catch (IOException ex) {
                Logger.getLogger(ClientInputListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
