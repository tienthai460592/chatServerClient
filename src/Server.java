import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) {


        try {
            ServerSocket serverSocket = new ServerSocket(1337);

            while (true) {
                // Wait for an incoming client-connection request (blocking).

                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream);
                writer.println("Welcome!");
                Scanner sc = new Scanner(System.in);
                System.out.print("Input username: ");
                String username = sc.nextLine();

            }
            // Your code here:
            // TODO: Start a message processing thread for each connecting client.
            // TODO: Start a ping thread for each connecting client.
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

