import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private ArrayList<ClientThread> clients = new ArrayList<>();
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

                System.out.println("accepted");
                ClientThread clientThread = new ClientThread(socket, this);
                clients.add(clientThread);
                clientThread.start();

                // Your code here:
                // TODO: Start a message processing thread for each connecting client.
                // TODO: Start a ping thread for each connecting client.
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientThread getClient(String username) {
        for(ClientThread clientThread: clients) {
            if (username.equals(clientThread.getUsername())) {
                return clientThread;
            }
        }
        return null;
    }

    public String getClients(String from) {
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

    public void broadcast(String message, String from) {
        for(ClientThread client: clients) {
            if (!from.equals(client.getUsername())) {
                client.receiveBroadcast(message, from);
            }
        }
    }

    public void message(String message, String to, String from) {
        getClient(to).receiveMessage(message, from);
    }
}

