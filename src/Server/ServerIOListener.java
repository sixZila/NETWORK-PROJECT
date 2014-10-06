package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerIOListener extends Thread {

    private final HashMap<String, User> clientList;
    private final BufferedReader inReader;
    private final PrintWriter outWriter;
    private User userInfo;
    private final Socket socket;
    private final InetAddress clientIP;
    private String username;

    public ServerIOListener(HashMap<String, User> clientList, Socket socket) throws IOException {
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
        userInfo = clientList.get(username);

        while (true) {
            try {
                //Read input
                String in = inReader.readLine();

                //Split the input using spaces ie. ("hello world!" => "hello/world!"). 
                String[] input = in.split("\\s");

                //Check the action of the user
                switch (input[0]) {
                    case "POST":
                        if (input.length < 2) {
                            outWriter.println("Error: there is no meesage to be sent.");
                        } else {
                            outWriter.println("Your post is now sent to your followers.");
                            postMessage(input);
                        }
                        break;
                    case "PM":
                        if (input.length < 3) {
                            outWriter.println("Error: there is no meesage to be sent.");
                        } else {
                            outWriter.println("Message sent to: " + input[1]);
                            personalMessage(input);
                        }
                        break;
                    case "FOLLOW":
                        if (input.length != 2) {
                            outWriter.println("Error: Invalid Username Format. Format: FOLLOW [username]");
                        } else {
                            outWriter.println("Follow Request sent to: " + input[1]);
                            followUser(input);
                        }
                        break;
                    case "ACCEPT":
                        if (input.length != 2) {
                            outWriter.println("Error: Invalid Username Format. Format: FOLLOW [username]");
                        } else {
                            acceptUser(input);
                        }
                        break;
                    default:
                        outWriter.println("Error: Invalid Message Format.");
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

    private void followUser(String[] input) {
        User followee = clientList.get(input[1]);
        Socket outSocket = followee.getClientSocket();

        PrintWriter writer;

        try {
            //Open writer to the desired user to follow.
            writer = new PrintWriter(outSocket.getOutputStream(), true);

            //Add request to the list
            followee.addRequest(username);

            //Send a request to the user.
            writer.println(username + " wants to follow you.");
        } catch (IOException ex) {
            Logger.getLogger(ServerIOListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void acceptUser(String[] input) {
        User followee = clientList.get(input[1]);
        Socket outSocket = followee.getClientSocket();

        //Remove the followers name to the list of pending requests. It will return true if the user exists, it will return false if the username did not send a follow request or does not exist.
        if (userInfo.removeRequest(input[1])) {
            //Add the Follower to the follower list.
            userInfo.addFollower(input[1]);

            PrintWriter writer;

            try {
                //Open writer to the follower.
                writer = new PrintWriter(outSocket.getOutputStream(), true);

                //Send the message to follower.
                writer.println(username + " accepted your follow request");
                outWriter.println(input[1] + " is now following you.");
            } catch (IOException ex) {
                Logger.getLogger(ServerIOListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Else, promt user of the error.
        } else {
            outWriter.println("User: " + input[1] + " did not send a follow request or does not exist.");
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
        for (String key : userInfo.getFollowers()) {
            //Get the socket of the followers.
            User client = clientList.get(key);
            PrintWriter writer;

            try {
                //Open writer to the follower
                writer = new PrintWriter(client.getClientSocket().getOutputStream(), true);

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
        try {
            Socket outSocket = clientList.get(input[1]).getClientSocket();
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
        } catch (Exception e) {
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
                        clientList.put(username, new User(socket));
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
