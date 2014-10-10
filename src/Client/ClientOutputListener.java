package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientOutputListener implements Runnable {

    private final DataInputStream in;
    private final InputStream inputStream;
    private final Socket socket;

    public ClientOutputListener(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        in = new DataInputStream(inputStream);
    }

    @Override
    public void run() {
        String temp;
        try {
            while (true) {
                temp = in.readUTF();
                System.out.println(temp);

                /*
                 if (temp.contains("posted a file from") && temp.contains("C:/NETWORK/")) {
                 byte[] inFile = new byte[socket.getReceiveBufferSize()];
                 //Read the byte size
                 int bytesRead = serverInput.read(inFile, 0, inFile.length);
                 System.out.println(bytesRead);
                 //Initialize File Name
                 String fileName = getFileName(temp);

                 //Make the directory
                 File file = new File("C:/NETWORK/");
                 file.mkdir();

                 //Make the file
                 file = new File("C:/NETWORK/" + fileName);
                 file.createNewFile();

                 FileOutputStream fileOutput = new FileOutputStream("C:/NETWORK/" + fileName);
                 BufferedOutputStream fileWriter = new BufferedOutputStream(fileOutput);

                 //Write the file
                 fileWriter.write(inFile, 0, bytesRead);
                 fileWriter.close();
                 }
                 */
            }
        } catch (IOException ex) {

        }
    }

    private String getFileName(String input) {
        boolean fileNameBuilt = false;
        StringBuilder name = new StringBuilder();
        int index = input.length() - 1;

        while (!fileNameBuilt) {
            if (index != -1) {
                if (!(input.charAt(index) == '/' || input.charAt(index) == '\\')) {
                    name.append(input.charAt(index));
                    index--;
                } else {
                    fileNameBuilt = true;
                }
            } else {
                fileNameBuilt = true;
            }
        }

        name = name.reverse();
        return name.toString();
    }
}
