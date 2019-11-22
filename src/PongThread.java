import akka.actor.AbstractActorWithTimers;

import java.io.*;
import java.net.Socket;

public class PongThread implements Runnable {

    boolean running = true;


    @Override
    public void run() {
        Socket socket = null;

        try {
            System.out.println("Running ...");
            socket = new Socket("127.0.0.1", 1337);


            InputStream inputStream = null;

            inputStream = socket.getInputStream();

            OutputStream outputStream = socket.getOutputStream();

            PrintWriter writer = new PrintWriter(outputStream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while (running) {

                String line = reader.readLine();

                System.out.println(line + "line");
                if (line.equals("PING")) {
                    writer.println("PONG");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRunning(){
        running = false;
    }
}
