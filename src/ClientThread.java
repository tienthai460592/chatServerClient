import jdk.swing.interop.SwingInterOpUtils;

import javax.crypto.Cipher;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread extends Thread{

    private String username = "";
    private Socket socket;
    private Server server;
    private boolean running = true;
    private PrintWriter writer;
    private ClientThread thisOne = this;
    private BufferedReader reader;
    private PingThread pingThread;
    private boolean pong;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Cipher cipher;

    public ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

//            cipher = Cipher.getInstance("RSA");
//            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//            String a = "aa";
//            a = Base64.getEncoder().encodeToString(cipher.doFinal(a.getBytes()));
//
//            System.out.println(a);
//
//            cipher.init(Cipher.DECRYPT_MODE, privateKey);
//            a = new String(cipher.doFinal(Base64.getDecoder().decode(a)));

//            System.out.println(a);

            OutputStream outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream);
            InputStream inputStream = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            write("HELO Welcome", "<");

            ClientServerProcessor clientServerProcessor = new ClientServerProcessor();
            clientServerProcessor.start();

            pingThread = new PingThread();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveBroadcast(String line) {

        write(line, ">");

    }

    public void receiveMessage(String line, String from) {

        try {

            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            line = new String(cipher.doFinal(Base64.getDecoder().decode(line)));


        } catch (Exception e) {
            e.printStackTrace();
        }

        write("[" + from + "] [private message] " + line, "<");
    }

    public void groupNotify(String message) {
        write(message, "<");
    }

    public void receiveGroupMessage(String line) {
        write(line, ">");
    }

    public class ClientServerProcessor extends Thread {

        public void run() {
            setUsername();
            pingThread.start();
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

                            PublicKey targetKey = server.findPublicKey(to);

                            if (targetKey == null) {
                                printErr("User doesn't exist");
                            } else {
                                String message = line.substring(5 + to.length());
                                write("+OK " + line, "<");

                                cipher = Cipher.getInstance("RSA");
                                cipher.init(Cipher.ENCRYPT_MODE, targetKey);
                                message = Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));

                                server.message(message, to, username);
                            }
                        } else if (line.startsWith("CREATE GROUP ")) {
                            String name = line.substring(13);

                            String regex = "^[a-zA-Z0-9_]+$";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(name);

                            if (!matcher.matches()) {
                                printErr("Name contains illegal characters");
                            } else if (server.getGroupMembers(name) == null) {
                                server.createGroup(name, thisOne);
                                write("+OK group " + name + " created", "<");
                            } else {
                                printErr("Group already exists");
                            }
                        } else if(line.equals("GET GROUPS")) {
                            write("+OK Groups: [" + server.getGroups() + "]", "<");
                        } else if (line.startsWith("JOIN ")) {
                            String groupName = line.substring(5);
                            ArrayList group = server.getGroupMembers(groupName);

                            if (group != null && !server.isMember(groupName, username)) {
                                server.joinGroup(groupName, thisOne);
                                write("+OK group " + groupName + " joined", "<");
                            } else if (group != null && server.isMember(groupName, username)) {
                                printErr("Already a member");
                            } else  {
                                printErr("Group doesn't exist");
                            }
                        } else if (line.startsWith("GMSG /")) {
                            StringTokenizer tokens = new StringTokenizer(line, " ");
                            tokens.nextToken();
                            String to = tokens.nextToken().substring(1);

                            if (server.getGroupMembers(to) != null && server.isMember(to, username)) {
                                String message = line.substring(7 + to.length());
                                server.messageGroup(message, to, username);
                                write("+OK " + line, "<");
                            } else {
                                printErr("Not member of group");
                            }
                        } else if (line.startsWith("LEAVE ")) {
                            String name = line.substring(6);

                            if (server.getGroups().contains(name) && server.isMember(name, username)) {
                                server.leaveGroup(name, username);
                                write("+OK you left " + name, "<");
                            } else {
                                printErr("Not member of group");
                            }
                        } else if (line.startsWith("KICK /")) {
                            StringTokenizer tokens = new StringTokenizer(line, " ");
                            tokens.nextToken();
                            String group = tokens.nextToken().substring(1);

                            String user = line.substring(7 + group.length());

                            if (server.getGroups().contains(group) && server.isAdmin(group, username)) {
                                if (user.equals(username)) {
                                    printErr("Why would you try to kick yourself");
                                } else if (server.isMember(group, user)) {
                                    write("+OK " + user + " kicked from " + group, "<");
                                    server.kickMember(group, user);
                                } else printErr("No such member in group");
                            } else {
                                printErr("Not admin of group");
                            }
                        } else if(line.equals("QUIT")) {
                            running = false;
                        } else if (line.equals("PONG"))  {
                            pong = true;
                        } else {
                            write("Command not recognised", "<");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void setUsername() {
            try {
                String clientMessage = reader.readLine();
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
                        setUsername();
                    } else {
                        username = name;
                        write("+OK HELO " + name, "<");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class PingThread extends Thread {

        public void run() {
            while (running) {
                pong = false;
                try {
                    write("PING", "<");

                    Thread.sleep(3000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!pong) {
                    write("Client inactive, disconecting", "<");
                    running = false;
                }
            }
        }
    }

    private void printErr(String message) {
        write("NOT OK " + message, "<");
    }

    private void write(String message, String direction) {
        if(direction.equals("<")) {
            System.out.println("[" + username + "] << " + message);
        } else {
            System.out.println(">> " + message);
        }
        writer.println(message);
        writer.flush();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getUsername() {
     return username;
    }
}
