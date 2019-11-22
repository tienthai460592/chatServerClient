import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    boolean running = true;
    PongThread pongThread = new PongThread();
    Thread thread = new Thread(pongThread);



    public static void main(String[] args) {
        new Main().run();
    }

    public void run(){

        Socket socket = null;

        try {
            socket = new Socket("127.0.0.1", 1337);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
//            thread.start();
//            thread.join();

            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            Scanner sc = new Scanner(System.in);
            System.out.println("Input username:");
            String username = sc.nextLine();

            OutputStream outputStream = socket.getOutputStream();

            PrintWriter writer = new PrintWriter(outputStream);
            writer.println("HELO "+username);

            writer.flush();

            while (running){
                Scanner scanner = new Scanner(System.in);
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

                if (line.equals("PING")) {
                    writer.println("PONG");
                    writer.flush();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
