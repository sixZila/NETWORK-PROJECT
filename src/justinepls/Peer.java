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

public final class Peer {

    private String address;
    private InetAddress IP;
    private boolean isLoggedIn;
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
        //initialized by providing a port to listen for incoming connections
        //main loop
        /* listen for incoming connections
         separate threads to handle them
         dispatch incoming requests to application handler based on message type*/
    }

    public void startListening() throws IOException {
        //If server does not exists, use the current program as a server.
        if (!checkServerExists()) {
            Thread server = new Thread(new ServerThread());
            server.start();
            socket = new Socket(IP, 1234);
        }// end of if
        //Else use program as a client.
        System.out.println("Your IP Address is: " + IP.getHostAddress());

        outThread = new Thread(new ClientInputListener(socket, address));
        inThread = new Thread(new ClientOutputListener(socket));
        outThread.start();
        inThread.start();
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
            System.out.println("No Server Exists, Running server Instance.");
            return false;
        }
        return false;
    }

    public boolean isIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }
}
