package justinepls;

import Client.ClientInputListener;
import Client.ClientOutputListener;
import Server.ServerThread;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public final class Peer {

    private String address;
    private InetAddress IP;
    private Socket socket;
    private Thread inThread, outThread;

    public Peer() throws IOException {

        try {
            this.IP = InetAddress.getLocalHost();
            this.address = IP.getHostAddress();

        } catch (UnknownHostException u) {
            System.out.println("An Error Occured" + u);
        }

        startListening();
    }

    public void startListening() throws IOException {
        Scanner s = new Scanner(System.in);
        boolean serverRunning;
        String input;

        do {
            System.out.print("Input the IP Address of the server: ");
            input = s.nextLine();

            switch (input) {
                case "Scan":
                    serverRunning = scanNetwork();
                    break;
                case "Start":
                    Thread server = new Thread(new ServerThread());
                    server.start();
                    socket = new Socket(IP, 1234);
                    serverRunning = true;
                    break;
                default:
                    try {
                        socket = new Socket(input, 1234);
                        serverRunning = true;
                    } catch (SocketException e) {
                        System.out.println("Unable to connect to server.");
                        serverRunning = false;
                    }
                    break;
            }
        } while (!serverRunning);
        //If server does not exists, use the current program as a server.
        //Else use program as a client.
        System.out.println("Your IP Address is: " + IP.getHostAddress());

        outThread = new Thread(new ClientInputListener(socket, address));
        inThread = new Thread(new ClientOutputListener(socket));
        outThread.start();
        inThread.start();
    }

    //Scan the network for a server
    public boolean scanNetwork() {
        byte[] outData = new byte[1024];
        byte[] inData = new byte[1024];

        System.out.println("Searching for Server...");

        DatagramSocket clientSocket;
        try {
            DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
            clientSocket = new DatagramSocket();
            //Set a timeout of 4 seconds.
            clientSocket.setSoTimeout(4000);

            //Message for sending to server (needed to send packet).
            String sentence = "";
            outData = sentence.getBytes();

            DatagramPacket outPacket = new DatagramPacket(outData, outData.length, IP, 1234);

            //Send data to server.
            clientSocket.send(outPacket);
            //Recieve Data from server, if no data is recieved after 4 seconds, no server exists.
            clientSocket.receive(inPacket);

            //Print a successful connection.
            System.out.println(new String(inPacket.getData()));

            //Instantiate Socket to server.
            socket = new Socket(inPacket.getAddress(), 1234);
            clientSocket.close();
            return true;
        } catch (SocketException ex) {
            System.out.println("Error");
        } catch (IOException ex) {
            System.out.println("Theres no server running.");
            return false;
        }
        return false;
    }
}
