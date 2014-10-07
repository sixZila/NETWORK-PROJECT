package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientOutputListener implements Runnable {

    private BufferedReader in;

    public ClientOutputListener(Socket socket) throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        String temp;
        try {
            while (true) {
                temp = in.readLine();
                System.out.println(temp);
            }

        } catch (IOException ex) {

        }
    }

}
