package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerConnection implements Runnable {

    private final Socket socket;
    private final ArrayList<User> clientList;
    private final InetAddress clientIP;
    private final DataInputStream inReader;
    private final DataOutputStream outWriter;
    private final InputStream inputStream;
    private User userInfo;
    private String username;

    //Class Constructor
    public ServerConnection(ArrayList<User> clientList, Socket socket) throws IOException {
        //Initializations
        this.clientList = clientList;
        this.socket = socket;
        inputStream = socket.getInputStream();
        clientIP = socket.getInetAddress();
        inReader = new DataInputStream(inputStream);
        outWriter = new DataOutputStream(socket.getOutputStream());
    }

    //Main Loop
    @Override
    public void run() {
        String[] input;

        //Log in
        loginUser();

        while (true) {
            try {
                //Read input from the user.
                input = inReader.readUTF().split("\\s");

                //Check the command of the user.
                switch (input[0]) {
                    case "\"POST\"":
                        postMessage(input);
                        break;
                    case "\"PM\"":
                        personalMessage(input);
                        break;
                    case "\"FOLLOW\"":
                        followerUser(input[1]);
                        break;
                    case "\"ACCEPT\"":
                        acceptUser(input[1]);
                        break;
                    case "\"FILE\"":
                        break;

                    //Default: there is not valid command. Meaning: the input is invalid.
                    default:
                        //Send error to the client
                        outWriter.writeUTF("Error: Invalid user input.");
                        break;
                }
            } catch (IOException ex) {
                try {
                    //Close streams.
                    inReader.close();
                    outWriter.close();
                } catch (IOException ex1) {

                }

                //Remove user from the client list
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

    //The method will try to log in the user to the system.
    private void loginUser() {
        boolean isUserLoggedIn;

        try {
            //Send welcome message to the client.
            outWriter.writeUTF("Welcome please enter your username: ");

            //Loop until the username is valid (no spaces, unique and not blank)
            do {
                //Read input from the client.
                username = inReader.readUTF();
                isUserLoggedIn = true;

                //Check if the usename is blank.
                if (!(username.isEmpty() || username.equals(""))) {
                    //Check if username contains any spaces.
                    if (username.split("\\s").length == 1) {
                        //Check if the username is already taken.
                        for (User user : clientList) {
                            if (user.equals(username)) {
                                outWriter.writeUTF("The username: " + username + " is already taken.");
                                outWriter.writeUTF("Please enter your username: ");
                                isUserLoggedIn = false;
                                break;
                            }
                        }
                        //Log in the user
                        if (isUserLoggedIn) {
                            outWriter.writeUTF("Logged in as: " + username);
                            userInfo = new User(socket, username);
                            clientList.add(userInfo);
                        }

                    } else {
                        //Send error saying that the username should contain no spasces.
                        outWriter.writeUTF("Your username should not contain any spaces.");
                        outWriter.writeUTF("Please enter your username: ");
                        isUserLoggedIn = false;
                    }
                } else {
                    //Send error saying that the username can't be blank.
                    outWriter.writeUTF("Your username can not be blank.");
                    outWriter.writeUTF("Please enter your username: ");
                    isUserLoggedIn = false;
                }
            } while (!isUserLoggedIn);

        } catch (IOException ex) {

        }
    }

    //This method is responsible for posting messages to the clients followers
    private void postMessage(String[] input) throws IOException {
        ArrayList<String> followers = userInfo.getFollowers();
        Socket followerSocket;
        String message = buildMessage(input, 1);

        //Traverse the list of followers
        for (String follower : followers) {

            //Get the user info of the follower from the client list.
            followerSocket = getClient(follower).getClientSocket();

            //Initialize the writer to the follower
            DataOutputStream followerWriter = new DataOutputStream(followerSocket.getOutputStream());
            //Send the message to the follower
            followerWriter.writeUTF(username + " [" + clientIP.getHostAddress() + "] posted: " + message);
        }
        //Send a confirmation to the client that the message has been posted.
        outWriter.writeUTF("Your post has been sent to your followers.");
    }

    //This method is responsible for posting messages to the clients followers
    private void personalMessage(String[] input) throws IOException {
        Socket receiverSocket;
        String message = buildMessage(input, 2);

        receiverSocket = getClient(input[1]).getClientSocket();

        if (receiverSocket != null) {
            //Initialize the writer to the follower
            DataOutputStream followerWriter = new DataOutputStream(receiverSocket.getOutputStream());
            //Send the message to the follower
            followerWriter.writeUTF(username + "[" + clientIP.getHostAddress() + "]: " + message);

            //Send the message to the user.
            outWriter.writeUTF("Your message has been sent to " + input[1] + ".");
        } else {
            //Send a confirmation to the client that the message has been sent.
            outWriter.writeUTF("The user you are trying to send to does not exist.");
        }
    }

    private void followerUser(String clientName) throws IOException {

        if (!(clientName.equals(username) || clientName.equals(clientIP.getHostAddress()))) {
            //Get the user info of the client
            User client = getClient(clientName);

            //Check if client exists
            if (client != null) {
                //Check if the client is already following the user.
                if (!client.checkFollower(username)) {
                    //Get the socket
                    Socket clientSocket = client.getClientSocket();

                    //Check if the request could be done
                    if (client.addRequest(username)) {
                        //Initialize the writer to the follower
                        DataOutputStream clientWriter = new DataOutputStream(clientSocket.getOutputStream());
                        //Send the message to the follower
                        clientWriter.writeUTF(username + "[" + clientIP.getHostAddress() + "] wants to follow you.");

                        //Send a confirmation to the client that the message has been sent.
                        outWriter.writeUTF("The follow request has been sent to " + client.getUsername() + "[" + client.getIPAddress() + "].");
                    } else {
                        //If the client already sent a request to the user, send this message instead.
                        outWriter.writeUTF("You already sent a request to this user.");
                    }
                } else {
                    //If the client is already following the user, send this message instead.
                    outWriter.writeUTF("You are already following this user.");
                }
            } else {
                //Send this message if the client does not exist
                outWriter.writeUTF("The user that you are trying to follow does not exist.");
            }
        } else {
            //Send this message if the username or IP is the client's
            outWriter.writeUTF("You can not follow yourself.");
        }
    }

    private void acceptUser(String clientName) throws IOException {
        if (!(clientName.equals(username) || clientName.equals(clientIP.getHostAddress()))) {

            //Remove the follow request
            if (userInfo.removeRequest(clientName)) {
                User client = getClient(clientName);
                Socket clientSocket = client.getClientSocket();
                
                //Add user to the followers list.
                userInfo.addFollower(clientName);

                DataOutputStream clientWriter = new DataOutputStream(clientSocket.getOutputStream());
                
                //Send the message to the user saying that the client already accepted their follow request.
                clientWriter.writeUTF(username + "[" + clientIP.getHostAddress() + "] accepted your follow request.");

                //Send a confirmation to the client that the user is now following them.
                outWriter.writeUTF(client.getUsername() + "[" + client.getIPAddress() + "] is now following you.");
            } else {
                //If the request can not be removed, notify that the the user did not send a follow request.
                outWriter.writeUTF("This user did not send you a follow request.");
            }
        } else {
            //Send this message if the username or IP is the client's
            outWriter.writeUTF("You can not follow yourself.");
        }
    }

    private User getClient(String clientName) {
        User user = null;

        //Get the user info of the receiver from the client list.
        for (User client : clientList) {
            if (client.equals(clientName)) {
                user = client;
                break;
            }
        }

        return user;
    }

    //Builds the message from the String array.
    //String[] to be combined an an offset value
    private String buildMessage(String[] input, int start) {
        StringBuilder message = new StringBuilder();

        for (int i = start; i < input.length; i++) {
            message.append(input[i]);
            if (i < input.length - 1) {
                message.append(" ");
            }
        }

        return message.toString();
    }
}
