import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private boolean running = true;
    private Socket socket;
    private PrintWriter writer;


    public static void main(String[] args) {
        new Main().run();
    }

    public void run(){

        try {
            socket = new Socket("127.0.0.1", 1337);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {


            OutputStream outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream);

            Scanner sc = new Scanner(System.in);
            System.out.print("Input username: ");
            String username = sc.nextLine();

            writer.println("HELO "+username);
            writer.flush();

            PongThread pongThread = new PongThread();
            Thread thread = new Thread(pongThread);
            thread.start();
            Scanner scanner = new Scanner(System.in);

            while (running) {
//                System.out.println("Your message: (log out to quit)");
                String ms = scanner.nextLine();

                if(ms.equals("log out")){
                    writer.println("QUIT");
                    writer.flush();
                    running = false;
                }else {
                    writer.println("BCST "+ms);
                    writer.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToServer(ClientMessage cm) {
        writer.println(cm);
        writer.flush();
    }

    public class PongThread implements Runnable {

        @Override
        public void run() {

            try {
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                while (running) {

                    String line = reader.readLine();
                    ServerMessage sm = new ServerMessage(line);
                    if (sm.isPing()) {
                        sendMessageToServer(new PongClientMessage());
                        sendMessageToServer(new BroadcastClientMessage("Hi there"));
                    } else {
                        System.out.println(line);
                        if (!line.equals("HELO Welkom to WhatsUpp!")) {
                            System.out.println("Your message: (log out to quit)");
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ServerMessage {
        private String line;

        public ServerMessage(String line) {
            this.line = line;
        }

        public boolean isPing() {
            return line.equals("PING");
        }

    }

    public class ClientMessage {

    }

    public class PongClientMessage extends ClientMessage {
        @Override
        public String toString() {
            return "PONG";
        }
    }

    public class BroadcastClientMessage extends ClientMessage {
        private String message;

        public BroadcastClientMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "BCST " + message;
        }
    }

}