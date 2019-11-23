import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private boolean running = true;
    private Socket socket;
    private InputStream inputStream;
    private BufferedReader reader;
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

            inputStream = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            Scanner sc = new Scanner(System.in);
            System.out.print("Input username: ");
            String username = sc.nextLine();

            OutputStream outputStream = socket.getOutputStream();

            writer = new PrintWriter(outputStream);
            writer.println("HELO "+username);
            writer.flush();

            PongThread pongThread = new PongThread();
            Thread thread = new Thread(pongThread);
            thread.start();
            Scanner scanner = new Scanner(System.in);

            while (running){
                System.out.println("Your message: (log out to quit)");
                String ms = scanner.nextLine();

                if(ms.equals("log out")){
                    writer.println("QUIT");
                    writer.flush();
                    running = false;
                }else {
                    writer.println("BCST "+ms);
                    writer.flush();

                }

                String line = reader.readLine();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class PongThread implements Runnable {

        @Override
        public void run() {

            try {
                while (running) {

                    String line = reader.readLine();

                    if (line.equals("PING")) {
                        writer.println("PONG");
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
