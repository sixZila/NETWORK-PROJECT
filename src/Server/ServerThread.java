package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread implements Runnable {

    private ServerSocket server;
    private final ArrayList<User> clientList;

    public ServerThread() {
        clientList = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(1234);
            System.out.println("Server started on port: " + server.getLocalPort());

            Thread UDPServer = new Thread(new UDPThread());
            UDPServer.start();

            //Accept clients.
            while (true) {
                Socket client = server.accept();
                Thread newClient = new Thread(new ServerConnection(clientList, client));
                newClient.start();
            }
        } catch (IOException ex) {

        }

    }
}
