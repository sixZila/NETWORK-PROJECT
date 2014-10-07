package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPThread implements Runnable {

    //This will send the user the IP address of the server.
    @Override
    public void run() {
        //For sending and recieving.
        byte[] inData = new byte[1024];
        byte[] outData = new byte[1024];
        
        String connectionMessage = "Connected to Server";
        outData = connectionMessage.getBytes();

        try {
            DatagramSocket serverSocket = new DatagramSocket(1234);

            while (true) {
                DatagramPacket inPacket = new DatagramPacket(inData, inData.length);

                //Reicieve data from a client.
                serverSocket.receive(inPacket);

                //System.out.println("Client Connected with IP Address: " + inPacket.getAddress().getHostAddress());

                //Send to client a successful connection and server IP information
                DatagramPacket sendPacket = new DatagramPacket(outData, outData.length, inPacket.getAddress(), inPacket.getPort());
                serverSocket.send(sendPacket);
            }

        } catch (SocketException ex) {
            Logger.getLogger(UDPThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UDPThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
