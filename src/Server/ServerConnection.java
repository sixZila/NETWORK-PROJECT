package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerConnection implements Runnable {

    private final Socket socket;
    private final ArrayList<User> clientList;
    private final InetAddress clientIP;
    private final BufferedReader inReader;
    private final PrintWriter outWriter;
    private final InputStream inputStream;
    private final DataInputStream fileReader;
    private User userInfo;
    private String username;

    //Class Constructor
    public ServerConnection(ArrayList<User> clientList, Socket socket) throws IOException {
        //Initializations
        this.clientList = clientList;
        this.socket = socket;
        inputStream = socket.getInputStream();
        clientIP = socket.getInetAddress();
        fileReader = new DataInputStream(inputStream);
        inReader = new BufferedReader(new InputStreamReader(inputStream));
        outWriter = new PrintWriter(socket.getOutputStream(), true);
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
                input = inReader.readLine().split("\\s");

                //Check the command of the user.
                switch (input[0]) {
                    case "\"POST\"":
                        if (input.length > 1) {
                            postMessage(input);
                        } else {
                            outWriter.println("There is no message to be posted.");
                        }
                        break;
                    case "\"PM\"":
                        if (input.length > 2) {
                            personalMessage(input);
                        } else {
                            outWriter.println("There is no message to be sent.");
                        }
                        break;
                    case "\"FOLLOW\"":
                        if (input.length > 1) {
                            followerUser(input[1]);
                        } else {
                            outWriter.println("There is no username specified to send a follow request.");
                        }
                        break;
                    case "\"ACCEPT\"":
                        if (input.length > 1) {
                            acceptUser(input[1]);
                        } else {
                            outWriter.println("There is no username specified to accept the follow request.");
                        }
                        break;
                    case "\"UNFOLLOW\"":
                        if (input.length > 1) {
                            unfollowUser(input[1]);
                        } else {
                            outWriter.println("There is no username specified to unfollow.");
                        }
                        break;
                    case "\"FILE\"":
                        if (input.length > 1) {
                            sendFile(input);
                        } else {
                            outWriter.println("There is no file specified to be sent.");
                        }
                        break;

                    //Default: there is not valid command. Meaning: the input is invalid.
                    default:
                        //Send error to the client
                        outWriter.println("Error: Invalid user input.");
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
            outWriter.println("Welcome please enter your username: ");

            //Loop until the username is valid (no spaces, unique and not blank)
            do {
                //Read input from the client.
                username = inReader.readLine();
                isUserLoggedIn = true;

                //Check if the usename is blank.
                if (!(username.isEmpty() || username.equals(""))) {
                    //Check if username contains any spaces.
                    if (username.split("\\s").length == 1) {
                        //Check if the username is already taken.
                        for (User user : clientList) {
                            if (user.equals(username)) {
                                outWriter.println("The username: " + username + " is already taken.");
                                outWriter.println("Please enter your username: ");
                                isUserLoggedIn = false;
                                break;
                            }
                        }
                        //Log in the user
                        if (isUserLoggedIn) {
                            outWriter.println("Logged in as: " + username);
                            userInfo = new User(socket, username);
                            clientList.add(userInfo);
                        }

                    } else {
                        //Send error saying that the username should contain no spasces.
                        outWriter.println("Your username should not contain any spaces.");
                        outWriter.println("Please enter your username: ");
                        isUserLoggedIn = false;
                    }
                } else {
                    //Send error saying that the username can't be blank.
                    outWriter.println("Your username can not be blank.");
                    outWriter.println("Please enter your username: ");
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
            PrintWriter followerWriter = new PrintWriter(followerSocket.getOutputStream(), true);
            //Send the message to the follower
            followerWriter.println(username + " [" + clientIP.getHostAddress() + "] posted: " + message);
        }
        //Send a confirmation to the client that the message has been posted.
        outWriter.println("Your post has been sent to your followers.");
    }

    //This method is responsible for posting messages to the clients followers
    private void personalMessage(String[] input) throws IOException {
        Socket receiverSocket;
        String message = buildMessage(input, 2);

        User client = getClient(input[1]);

        if (client != null) {
            receiverSocket = client.getClientSocket();

            //Initialize the writer to the follower
            PrintWriter followerWriter = new PrintWriter(receiverSocket.getOutputStream(), true);
            //Send the message to the follower
            followerWriter.println(username + " [" + clientIP.getHostAddress() + "] sent you a private message: " + message);

            //Send the message to the user.
            outWriter.println("Your message has been sent to " + input[1] + ".");
        } else {
            //Send a confirmation to the client that the message has been sent.
            outWriter.println("The user you are trying to send to does not exist.");
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
                        PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                        //Send the message to the follower
                        clientWriter.println(username + " [" + clientIP.getHostAddress() + "] wants to follow you.");

                        //Send a confirmation to the client that the message has been sent.
                        outWriter.println("The follow request has been sent to " + client.getUsername() + " [" + client.getIPAddress() + "].");
                    } else {
                        //If the client already sent a request to the user, send this message instead.
                        outWriter.println("You already sent a request to this user.");
                    }
                } else {
                    //If the client is already following the user, send this message instead.
                    outWriter.println("You are already following this user.");
                }
            } else {
                //Send this message if the client does not exist
                outWriter.println("The user that you are trying to follow does not exist.");
            }
        } else {
            //Send this message if the username or IP is the client's
            outWriter.println("You can not follow yourself.");
        }
    }

    private void acceptUser(String clientName) throws IOException {
        if (!(clientName.equals(username) || clientName.equals(clientIP.getHostAddress()))) {

            User client = getClient(clientName);
            if (client != null) {
                //Remove the follow request
                if (userInfo.removeRequest(client.getUsername())) {
                    Socket clientSocket = client.getClientSocket();

                    //Add user to the followers list.
                    userInfo.addFollower(client.getUsername());

                    PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);

                    //Send the message to the user saying that the client already accepted their follow request.
                    clientWriter.println(username + " [" + clientIP.getHostAddress() + "] accepted your follow request.");

                    //Send a confirmation to the client that the user is now following them.
                    outWriter.println(client.getUsername() + " [" + client.getIPAddress() + "] is now following you.");
                } else {
                    //If the request can not be removed, notify that the the user did not send a follow request.
                    outWriter.println("This user did not send you a follow request.");
                }
            } else {
                //If the request can not be removed, notify that the the user did not send a follow request.
                outWriter.println("This user does not exist.");
            } 
        } else {
            //Send this message if the username or IP is the client's
            outWriter.println("You can not follow yourself.");
        }
    }

    private void unfollowUser(String clientName) throws IOException {
        //Check if the username is the same as the client's
        if (!(clientName.equals(username) || clientName.equals(clientIP.getHostAddress()))) {
            //Get the user info of the client
            User client = getClient(clientName);

            //Check if client exists
            if (client != null) {
                //Check if the client is already following the user.
                if (client.removeFollower(username)) {
                    //Send a confirmation to the client that the user is now following them.
                    outWriter.println("You have unfollowed " + client.getUsername() + " [" + client.getIPAddress() + "] anymore.");
                } else {
                    //If the request can not be removed, notify that the the user did not send a follow request.
                    outWriter.println("You are not following this user.");
                }
            } else {
                //If the request can not be removed, notify that the the user did not send a follow request.
                outWriter.println("The user you are trying to unfollow does not exist.");
            }
        } else {
            //Send this message if the username or IP is the client's
            outWriter.println("You can not unfollow yourself.");
        }
    }

    private void sendFile(String[] input) throws SocketException, IOException {
        String directory = buildMessage(input, 1);
        String fileName = getFileName(directory);

        byte[] outFile = new byte[socket.getReceiveBufferSize()];
        int bytesRead = fileReader.read(outFile, 0, outFile.length);

        ArrayList<String> followers = userInfo.getFollowers();
        Socket followerSocket;

        for (String follower : followers) {

            //Get the user info of the follower from the client list.
            followerSocket = getClient(follower).getClientSocket();

            //Initialize the writer to the follower
            PrintWriter followerWriter = new PrintWriter(followerSocket.getOutputStream(), true);
            DataOutputStream fileSender = new DataOutputStream(followerSocket.getOutputStream());
            //Send the message to the follower
            followerWriter.println(username + " [" + clientIP.getHostAddress() + "] sent a file saved at C:/NETWORK/" + fileName);

            fileSender.write(outFile, 0, bytesRead);
            fileSender.flush();

        }
        //Send a confirmation to the client that the message has been posted.
        outWriter.println("Your file has been sent to your followers.");
    }

    //Get the file name from the a directory.
    private String getFileName(String input) {
        boolean fileNameBuilt = false;
        StringBuilder name = new StringBuilder();
        int index = input.length() - 1;

        while (!fileNameBuilt) {
            if (index != -1) {
                if (!(input.charAt(index) == '/' || input.charAt(index) == '\\')) {
                    name.append(input.charAt(index));
                    index--;
                } else {
                    fileNameBuilt = true;
                }
            } else {
                fileNameBuilt = true;
            }
        }
        name = name.reverse();
        return name.toString();
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
