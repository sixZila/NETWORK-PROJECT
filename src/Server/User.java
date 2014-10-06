/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author MC
 */
public class User {

    private final ArrayList<String> followers;
    private final ArrayList<String> requests;
    private final Socket clientSocket;

    public User(Socket clientSocket) {
        this.clientSocket = clientSocket;
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
}
