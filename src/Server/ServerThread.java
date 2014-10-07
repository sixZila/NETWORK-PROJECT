package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerThread implements Runnable {

    private ServerSocket server;
    private final HashMap<String, User> clientList;

    public ServerThread() {
        clientList = new HashMap<>();
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
                ServerIOListener newClient = new ServerIOListener(clientList, client);
                newClient.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
