

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    private ArrayList<ClientThread> clients = new ArrayList<>();
    private HashMap<String, ArrayList<ClientThread>> groups = new HashMap<>();
    private  ServerSocket serverSocket;

    public static void main(String[] args) {
        new Server().run();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(1337);

            while (true) {
                // Wait for an incoming client-connection request (blocking).
                Socket socket = serverSocket.accept();

                ClientThread clientThread = new ClientThread(socket, this);
                clients.add(clientThread);
                clientThread.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized PublicKey findPublicKey(String username) {
        ClientThread client = getClient(username);
        if (client != null) {
            System.out.println(client.getUsername());
            return client.getPublicKey();
        }
        return null;
    }

    public void createGroup(String name, ClientThread clientThread) {
        groups.put(name, new ArrayList<>());
        groups.get(name).add(clientThread);
    }

    public void joinGroup(String groupName, ClientThread client) {

        for ( String key : groups.keySet() ) {
            if (key.equals(groupName)) {
                groups.get(groupName).add(client);
                System.out.println(groups.get(groupName));
            }
        }
    }

    public void leaveGroup(String groupName, String username) {
        ArrayList<ClientThread> clientThreads = groups.get(groupName);

        if (clientThreads.size() <= 1) {
            groups.remove(groupName);
        } else {
            if (isAdmin(groupName, username)) {
                clientThreads.get(1).groupNotify("<" + username + " left " + groupName + ", now you are admin. Congrats.");
            }

            for (int i = 0; i < clientThreads.size(); i++) {
                ClientThread user = clientThreads.get(i);
                if (user.getUsername().equals(username)) {
                    clientThreads.remove(user);
                }
            }
        }
    }

    public void kickMember(String groupName, String userName) {
        ClientThread user = getClient(userName);

        groups.get(groupName).remove(user);
        user.groupNotify("You were kicked from " + groupName);
}

    public synchronized ClientThread getClient(String username) {
        for(ClientThread clientThread: clients) {
            if (username.equals(clientThread.getUsername())) {
                return clientThread;
            }
        }
        return null;
    }

    public synchronized ArrayList<ClientThread> getGroupMembers(String name) {
        try {
            return groups.get(name);
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized boolean isMember(String groupName, String userName) {
        ArrayList<ClientThread> group = getGroupMembers(groupName);
        for(ClientThread clientThread: group) {
            if (clientThread.getUsername().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isAdmin(String groupname, String username) {
        if(getGroupMembers(groupname).get(0).getUsername().equals(username)) {
            return true;
        }
        return false;
    }

    public synchronized String getGroups() {
        StringBuilder response = new StringBuilder();

        Object[] keys = groups.keySet().toArray();

        if(keys.length == 0) {
            return "";
        }

        response.append(keys[0]);

        for (int i = 1; i < keys.length; i++) {
            response.append(", ").append(keys[i]);
        }

        return response.toString();
    }

    public synchronized String getClients(String from) {
        StringBuilder response = new StringBuilder();
        boolean comma = true;

        if (!clients.get(0).getUsername().equals(from)) {
            response.append(clients.get(0).getUsername());
        } else {
            comma = false;
        }

        for(int i = 1; i < clients.size(); i++) {
            if (!clients.get(i).getUsername().equals(from) && !clients.get(i).getUsername().equals("")) {
                if (comma) {
                    response.append(", ");
                }
                response.append(clients.get(i).getUsername());
            }
        }
        return response.toString();
    }

    public synchronized void broadcast(String message, String from) {
        for(ClientThread client: clients) {
            if (!from.equals(client.getUsername())) {
                client.receiveBroadcast("[" + from + "]" + message);
            }
        }
    }

    public synchronized void message(String message, String to, String from) {
        getClient(to).receiveMessage(message, from);
    }

    public synchronized void messageGroup(String message, String to, String from) {
        ArrayList<ClientThread> clients = getGroupMembers(to);

        for (ClientThread clientThread: clients) {
            if (!clientThread.getUsername().equals(from)) {
                clientThread.receiveGroupMessage("[" + from + "]" + " [" + to + "] " + message);
            }
        }
    }
}

