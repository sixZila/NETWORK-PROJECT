package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerIOListener implements Runnable {

    private final ArrayList<User> clientList;
    private final BufferedReader inReader;
    private final PrintWriter outWriter;
    private User userInfo;
    private final Socket socket;
    private final InetAddress clientIP;
    private String username;

    public ServerIOListener(ArrayList<User> clientList, Socket socket) throws IOException {
        //Initializations
        this.clientList = clientList;
        this.socket = socket;
        clientIP = socket.getInetAddress();
        inReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outWriter = new PrintWriter(socket.getOutputStream(), true);
    }

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
                    case "\"POST\"":
                        if (input.length < 2) {
                            outWriter.println("Error: there is no meesage to be sent.");
                        } else {
                            postMessage(input);
                        }
                        break;
                    case "\"PM\"":
                        if (input.length < 3) {
                            outWriter.println("Error: there is no meesage to be sent.");
                        } else {
                            personalMessage(input);
                        }
                        break;
                    case "\"FOLLOW\"":
                        if (input.length != 2) {
                            outWriter.println("Error: Invalid Username Format. Format: FOLLOW [username]");
                        } else {
                            followUser(input);
                        }
                        break;
                    case "\"ACCEPT\"":
                        if (input.length != 2) {
                            outWriter.println("Error: Invalid Username Format. Format: ACCEPT [username]");
                        } else {
                            acceptUser(input);
                        }
                        break;
                    default:
                        outWriter.println("Error: Invalid Message Format.");
                        break;
                }

            } catch (IOException ex) {
                for (Iterator<User> it = clientList.iterator(); it.hasNext();) {
                    User peer = it.next();
                    if (peer.equals(username)) {
                        it.remove();
                    }
                }
                break;
            }
        }
    }

    private void followUser(String[] input) {
        User followee = null;
        //Check if the username of the follow request is the same as the client.
        if (!input[1].equals(username)) {

            for (User peer : clientList) {
                if (peer.equals(input[1])) {
                    followee = peer;
                }
            }
            Socket outSocket;

            //Check if the user you want to follow exists.
            if (followee != null) {
                try {
                    //Check if the user is already following the desired followee
                    if (!followee.checkFollower(username)) {
                        //Add request to the list
                        if (followee.addRequest(username)) {
                            outSocket = followee.getClientSocket();
                            PrintWriter writer;

                            //Open writer to the desired user to follow.
                            writer = new PrintWriter(outSocket.getOutputStream(), true);
                            
                            //Send a success message to the sender.
                            outWriter.println("Follow Request sent to: " + input[1]);
                            
                            //Send a request to the user.
                            writer.println(username + " wants to follow you.");
                        } else {
                            //Notify the user that a request is already sent.
                            outWriter.println("You already sent a request to the user.");
                        }
                    } else {
                        //Notify the user that he/she is already following the desired user.
                        outWriter.println("You are already following this user.");
                    }
                } catch (IOException ex) {

                }
            } else {
                outWriter.println("The username you want to follow does not exist.");
            }
        } else {
            System.out.println("You can not send a follow request to yourself.");
        }
    }

    private void acceptUser(String[] input) {
        //Check if the username is the same client
        if (!input[1].equals(username)) {

            User followee = null;

            for (User peer : clientList) {
                if (peer.equals(input[1])) {
                    followee = peer;
                    break;
                }
            }

            if (followee != null) {
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
                        
                        //Notify that the the user is now a follower.
                        outWriter.println(input[1] + " is now following you.");
                    } catch (IOException ex) {

                    }
                    //Else, promt user of the error.
                } else {
                    outWriter.println("User: " + input[1] + " did not send a follow request.");
                }
            } else {
                outWriter.println("User does not exist.");
            }
        } else {
            outWriter.println("You cannot accept a follow request to yourself.");
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

        //Traverse the followers.
        for (String key : userInfo.getFollowers()) {

            User follower = null;

            //Search for follower
            for (User peer : clientList) {
                if (peer.equals(key)) {
                    follower = peer;
                    break;
                }
            }

            Socket followerSocket = follower.getClientSocket();

            PrintWriter writer;

            try {
                //Open writer to the follower
                writer = new PrintWriter(followerSocket.getOutputStream(), true);

                //Send the message to follower.
                writer.println(username + " posted from " + clientIP.getHostAddress() + ": \"" + message.toString() + "\"");
                
                //Notify the user that the message has to the followers.
                outWriter.println("Your post is now sent to your followers.");
            } catch (IOException ex) {
                Logger.getLogger(ServerIOListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Log the action.
        //System.out.println(username + " posted: " + message.toString());
    }

    private void personalMessage(String[] input) {
        try {
            User follower = null;

            //Search for follower
            for (User peer : clientList) {
                if (peer.equals(input[1])) {
                    follower = peer;
                    break;
                }
            }
            Socket outSocket = follower.getClientSocket();
            //Check if username exists.
            if (socket != null) {
                try {
                    //Open writer to the reciever
                    PrintWriter writer = new PrintWriter(outSocket.getOutputStream(), true);

                    //Build the message to be sent.
                    StringBuilder message = buildMessage(input, 2);

                    //Send the message to the desired user.
                    writer.println(username + " sent a message from " + clientIP.getHostAddress() + ": \"" + message.toString() + "\"");

                    //Notify the user that the message has been sent.
                    outWriter.println("Message sent to: " + input[1]);
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
    public void login() {
        boolean isUsernameValid;
        try {
            //Send welcome message.
            outWriter.println("Welcome! Please enter your username: ");

            //Loop until user inputs a unique username.
            do {
                username = inReader.readLine();
                isUsernameValid = false;

                if (username.split("\\s").length == 1) {

                    //Check if the username exists in the list.
                    for (User peer : clientList) {
                        if (peer.equals(username)) {
                            isUsernameValid = true;
                            break;
                        }
                    }

                    if (!isUsernameValid) {
                        userInfo = new User(socket, username);
                        clientList.add(userInfo);
                        outWriter.println("Logged in as: " + username);

                        //Log Connection
                        //System.out.println(username + " connected with IP address: " + socket.getInetAddress().getHostAddress());
                    } else {
                        //Send error to client saying that the username already exists.
                        outWriter.println("Username \"" + username + "\" already exists.");
                        outWriter.println("Please input your username: ");
                    }
                } else {
                    isUsernameValid = false;

                    //Send error to client saying that the username can't have spaces.
                    outWriter.println("Your username can not have spaces.");
                    outWriter.println("Please input your username: ");
                }
            } while (isUsernameValid);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
