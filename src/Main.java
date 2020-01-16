import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private boolean running = true;
    private Socket socket;
    private Scanner sc = new Scanner(System.in);
    private OutputStream outputStream;
    private PrintWriter writer;
    private InputStream inputStream;
    private BufferedReader reader;


    public static void main(String[] args) {
        new Main().run();
    }

    public void run(){
        try {

            socket = new Socket("127.0.0.1", 1337);

            outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream);

            inputStream = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));


            String line = reader.readLine();
            if (line.startsWith("HELO")) {
                System.out.println(line);

                sendUsername();

                line = reader.readLine();

                while (!line.startsWith("+OK HELO")) {
                    System.out.println(line);
                    sendUsername();
                    line = reader.readLine();
                }

                if (line.startsWith("+OK HELO")) {
                    System.out.println(line);
                    PongThread pongThread = new PongThread();
                    Thread thread = new Thread(pongThread);
                    thread.start();

                    while (running) {
                        System.out.println("Your message: (log out to quit)");
                        String ms = sc.nextLine();

                        if(ms.equals("log out")){
                            writer.println("QUIT");
                            writer.flush();
                            running = false;
                        }else {
                            writer.println(ms);
                            writer.flush();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendUsername() {
        System.out.print("Input username: ");
        String username = sc.nextLine();

        writer.println("HELO " + username);
        writer.flush();
    }



    public class PongThread implements Runnable {

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream);

                while (running) {

                    String line = reader.readLine();

                    if (line.equals("PING")) {

                        writer.println("PONG");
                        writer.flush();
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
}