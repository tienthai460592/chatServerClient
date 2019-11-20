import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        run();
    }

    public static void run(){
        Socket socket = new Socket("localhost://", 13398);
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
    }
}
