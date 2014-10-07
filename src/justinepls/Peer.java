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
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public void startListening() {
        Scanner s = new Scanner(System.in);
        boolean serverRunning;
        String input;

        System.out.println("Your IP Address is: " + IP.getHostAddress());
        //Print empty space
        System.out.println();

        System.out.println("Welcome! Type \"scan\" to automatically search for the server.");
        System.out.println("Type \"start\" to run as a server.");

        do {
            System.out.print("Input the IP Address of the server: ");
            input = s.nextLine();

            switch (input) {
                case "scan":
                    serverRunning = scanNetwork();
                    break;
                case "start":
                    try {
                        Socket temp = new Socket("127.0.0.1" ,1234);
                        System.out.println("There's a server already running.");
                        serverRunning = false;
                    } catch (IOException ex) {
                        Thread server = new Thread(new ServerThread());
                        server.start();
                        serverRunning = true;
                        try {
                            socket = new Socket(IP, 1234);
                        } catch (IOException ex1) {
                            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                    break;
                default:
                    try {
                        socket = new Socket(input, 1234);
                        serverRunning = true;
                    } catch (SocketException e) {
                        System.out.println("Unable to connect to server.");
                        serverRunning = false;
                    } catch (IOException e) {
                        System.out.println("Unable to connect to server.");
                        serverRunning = false;
                    }
                    break;
            }
        } while (!serverRunning);

        try {
            outThread = new Thread(new ClientInputListener(socket, s));
            inThread = new Thread(new ClientOutputListener(socket));
            inThread.start();
            outThread.start();

        } catch (IOException ex) {

        }
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
