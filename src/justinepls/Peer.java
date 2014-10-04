package justinepls;

import Client.ClientInputListener;
import Client.ClientOutputListener;
import Server.ServerIOListener;
import Server.UDPThread;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Peer {

    //SERVER NAME FOR CONNECTING TO THE SERVER (USE PC NAME)
    private final String SERVER_NAME = "MC-PC";
    //===================================================//
    private String address;
    private InetAddress IP;
    private HashMap<String, Socket> followers;  //list of known peers
    private Socket socket;
    private ServerSocket server;
    private Thread inThread, outThread;

    public Peer() throws IOException {

        followers = new HashMap<>();

        try {
            this.IP = InetAddress.getLocalHost();
            this.address = IP.getHostAddress();

        } catch (UnknownHostException u) {
            System.out.println("An Error Occured" + u);
        }

        startListening();
        //initialized by providing a port to listen for incoming connections
        //main loop
        /* listen for incoming connections
         separate threads to handle them
         dispatch incoming requests to application handler based on message type*/
    }

    public void startListening() throws IOException {
        //If server does not exists, use the current program as a server.
        if (!checkServerExists()) {
            //s1 = new Server(check);
            //s1.start();
            try {
                server = new ServerSocket(1234);
                System.out.println("Server now listening on port: " + server.getLocalPort());

                Thread UDPServer = new Thread(new UDPThread());
                UDPServer.start();
                //Accept clients.
                while (true) {
                    Socket client = server.accept();
                    ServerIOListener newClient = new ServerIOListener(this, client);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }// end of if
        else {
            //Else use program as a client.
            System.out.println("Your IP Address is: " + IP.getHostAddress());

            outThread = new Thread(new ClientInputListener(socket, address));
            inThread = new Thread(new ClientOutputListener(socket));
            outThread.start();
            inThread.start();
        }
    }// end of startListening();

    //method for deciding on how to route messages
    /*when peer module receives an incoming connection request, set up peer connection obj
     read in the message type
     launch separate thread to handle
     close peer connection when message handler complete*/
    //Check if there is already a server running in the network.
    public boolean checkServerExists() {
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
            System.out.println("No Server Exists, Running as Server.");
            return false;
        }
        /*
         try {
         socket = new Socket(SERVER_NAME, 1234);
         return true;
         } catch (IOException error) {
         System.out.println("No Server is running, Server mode activated.");
         }

         return false;
         */
        return false;
    }

    public HashMap<String, Socket> getFollowers() {
        return followers;
    }

    public void setFollowers(HashMap<String, Socket> followers) {
        this.followers = followers;
    }
}
