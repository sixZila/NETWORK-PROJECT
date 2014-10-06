package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerIOListener extends Thread {

    private final HashMap<String, Socket> clientList;
    private final BufferedReader inReader;
    private final PrintWriter outWriter;
    private final Socket socket;
    private final InetAddress clientIP;
    private String username;

    public ServerIOListener(HashMap<String, Socket> clientList, Socket socket) throws IOException {
        //Initializations
        this.clientList = clientList;
        this.socket = socket;
        clientIP = socket.getInetAddress();
        inReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outWriter = new PrintWriter(socket.getOutputStream(), true);

        start();
    }

    /*
     Run the thread
     */
    @Override
    public void run() {
        //Login to the server.
        login();

        while (true) {
            try {
                //Read input
                String in = inReader.readLine();

                //Split the input using spaces ie. ("hello world!" => "hello/world!"). 
                String[] input = in.split("\\s");

                //Check the action of the user
                switch (input[0]) {
                    case "POST":
                        postMessage(input);
                        break;
                    case "PM":
                        personalMessage(input);
                        break;
                }

            } catch (IOException ex) {
                //If user disconnected
                //System.out.println(username + " disconnected.");
                clientList.remove(username);
                break;
            }
        }
    }

    //Builds the message from the String array.
    private StringBuilder buildMessage(String[] input, int start) {
        StringBuilder message = new StringBuilder();

        for (int i = start; i < input.length; i++) {
            message.append(input[i]);
            if (i < input.length - 1) {
                message.append(" ");
            }
        }

        return message;
    }

    private void postMessage(String[] input) {

        //Build the message to be sent.
        StringBuilder message = buildMessage(input, 1);

        //CHANGE THIS TO FOLLOWERS LATER:
        for (String key : clientList.keySet()) {
            //Get the socket of the followers.
            Socket outSocket = clientList.get(key);
            PrintWriter writer;

            try {
                //Open writer to the follower
                writer = new PrintWriter(outSocket.getOutputStream(), true);

                //Send the message to follower.
                writer.println(username + " posted from " + clientIP.getHostAddress() + ": \"" + message.toString() + "\"");
            } catch (IOException ex) {
                Logger.getLogger(ServerIOListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Log the action.
        //System.out.println(username + " posted: " + message.toString());
    }

    private void personalMessage(String[] input) {
        Socket outSocket = clientList.get(input[1]);
        //Check if username exists.
        if (socket != null) {
            try {
                //Open writer to the reciever
                PrintWriter writer = new PrintWriter(outSocket.getOutputStream(), true);

                //Build the message to be sent.
                StringBuilder message = buildMessage(input, 2);

                //Send the message to the desired user.
                writer.println(username + " sent a message from " + clientIP.getHostAddress() + ": \"" + message.toString() + "\"");

                //Log the action.
                //System.out.println(username + " sent a message to " + input[0] + " containing: " + message.toString());
            } catch (IOException ex) {
                Logger.getLogger(ServerIOListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //Else print a message saying that the username is not found in the list.
            outWriter.println("Username: " + input[0] + " not found.");
        }
    }

    //Log in the client to the server.
    private void login() {
        boolean isUsernameExists;

        try {
            if (inReader.readLine().equals("")) {
                //Send welcome message.
                outWriter.println("Welcome! Please enter your username.");

                //Loop until user inputs a unique username.
                do {
                    username = inReader.readLine();

                    //Check if the username exists in the list.
                    isUsernameExists = !(clientList.get(username) == null);

                    if (!isUsernameExists) {
                        clientList.put(username, socket);
                        outWriter.println("Logged in as: " + username);

                        //Log Connection
                        //System.out.println(username + " connected with IP address: " + socket.getInetAddress().getHostAddress());
                    } else {
                        //Send error to client saying that the username already exists.
                        outWriter.println("Username \"" + username + "\" already exists.");
                    }
                } while (isUsernameExists);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
