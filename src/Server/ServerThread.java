/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author MC
 */
public class ServerThread implements Runnable {

    private ServerSocket server;
    private HashMap<String, User> clientList;

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
