import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        run();
    }

    public static void run(){
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 13398);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            InputStream inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            OutputStream outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username");
        String userName = scanner.nextLine();
    }
}
