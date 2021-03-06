package Server;

import java.net.Socket;
import java.util.ArrayList;

//This class contains the user information of the clients.
public class User {

    private final String IPAddress;
    private final String username;
    private final ArrayList<String> followers;
    private final ArrayList<String> requests;
    private final Socket clientSocket;

    public User(Socket clientSocket, String username) {
        this.clientSocket = clientSocket;
        this.username = username;
        IPAddress = clientSocket.getInetAddress().getHostAddress();
        followers = new ArrayList<>();
        requests = new ArrayList<>();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void addFollower(String follower) {
        followers.add(follower);
    }

    public boolean removeFollower(String follower) {
        if (followers.contains(follower)) {
            followers.remove(follower);
            return true;
        } else {
            return false;
        }
    }

    public boolean checkFollower(String username) {
        return followers.contains(username);
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public boolean removeRequest(String username) {
        if (requests.contains(username)) {
            requests.remove(username);
            return true;
        } else {
            return false;
        }
    }

    public boolean addRequest(String username) {
        if (!requests.contains(username)) {
            requests.add(username);
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<String> getRequests() {
        return requests;
    }

    public boolean equals(String input) {
        return (IPAddress.equals(input) || username.equals(input));
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public String getUsername() {
        return username;
    }

}
