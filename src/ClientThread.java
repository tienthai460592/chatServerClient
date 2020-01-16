import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread extends Thread{

    private String username = "";
    private Socket socket;
    private Server server;
    private boolean running = true;
    private OutputStream outputStream;
    private PrintWriter writer;
    private InputStream inputStream;
    private BufferedReader reader;

    public ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream);
            inputStream = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            write("HELO Welcome", "<");

            ClientServerProcessor clientServerProcessor = new ClientServerProcessor();
            clientServerProcessor.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveBroadcast(String line, String from) {

        write("[" + from + "] " + line, ">");

    }

    public void receiveMessage(String line, String from) {
        write("[" + from + "] [private message] " + line, ">");
    }

    public class ClientServerProcessor implements Runnable {

        public void run() {
            setUsername();
            while (running) {
                try {
                    while (running) {
                        String  line = reader.readLine();
                        String output = ">> " + "[" + username + "] " + line;
                        System.out.println(output);

                        if(line.startsWith("BCST ")) {
                            //message = line.substring(4);
                            write("+OK BCST " + line.substring(4), "<");
                            server.broadcast(line, username);
                        } else if(line.equals("GET USERS")) {
                            write("+OK Users: [" + server.getClients(username) + "]", "<");
                        } else if(line.startsWith("MSG /")) {
                            StringTokenizer tokens = new StringTokenizer(line, " ");

                            tokens.nextToken();

                            String to = tokens.nextToken().substring(1);

                            if (server.getClient(to) == null || to.equals("")) {
                                printErr("User doesn't exist");
                            } else {
                                String message = line.substring(5 + to.length());
                                write("+OK " + line, "<");
                                server.message(message, to, username);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void setUsername() {
            try {
                String clientMessage = reader.readLine();
                //write(clientMessage, ">");
                if (clientMessage.startsWith("HELO ")) {
                    System.out.println(">> " + clientMessage);
                    String name = clientMessage.substring(5);

                    String regex = "^[a-zA-Z0-9_]+$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(name);

                    if (!matcher.matches()) {
                        printErr("Username contains illegal characters");
                        setUsername();
                    } else if (server.getClient(name) != null) {
                        printErr("Username already exists");
                    } else {
                        write("+OK HELO " + name, "<");
                        username = name;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printErr(String message) {

        write("NOT OK " + message, "<");
//        System.out.println("<< NOT OK" + message);
//        writer.println("NOT OK " + message);
//        writer.flush();
    }

    private void write(String message, String direction) {
        if(direction.equals("<")) {
            System.out.println("<< " + message);
        } else {
            System.out.println(">> " + message);
        }
        writer.println(message);
        writer.flush();
    }


    public String getUsername() {
     return username;
    }
}
