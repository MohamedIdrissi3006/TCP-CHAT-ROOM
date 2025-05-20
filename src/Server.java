import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<ConnectionHandler>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            // TODO : handle
        }
    }

    public void broadcast(String msg) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(msg);
            }
        }

    }

    public void shutDown() throws IOException {
        if (!server.isClosed()) {
            server.close();
        }
        for (ConnectionHandler ch : connections) {
            ch.shutdown();
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public String getNickname() {
            return nickname;
        }

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                do {
                    out.println("Please enter your nickname: ");
                    nickname = in.readLine();
                } while (nickname == null || nickname.trim().isEmpty());
                System.out.println(nickname + "connected");
                out.println(nickname + "Joined the Chat !");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick")) {
                        String[] messagesplit = message.split(" ", 2);
                        if (messagesplit.length == 2) {
                            broadcast(nickname + " renamed himsleves to " + messagesplit[1]);
                            nickname = messagesplit[1];
                            System.out.println("Successfully changed nickname to : " + nickname);


                        } else {
                            out.println("No nickname provided ");
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + " left the chat. ");
                        safeShutdown();
                    } else if (message.startsWith("/dm")) {
                        String[] splitMsg = message.split(" ", 3);
                        if (splitMsg.length == 3) {
                            String targetNick = splitMsg[1];
                            String privateMsg = splitMsg[2];
                            boolean found = false;
                            for (ConnectionHandler ch : connections) {
                                if (ch.getNickname().equalsIgnoreCase(targetNick)) {
                                    ch.sendMessage("[Private] " + nickname + ": " + privateMsg);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                out.println("User '" + targetNick + "' not found.");
                            }
                        } else {
                            out.println("Invalid /dm command. Usage: /dm <nickname> <message>");
                        }
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                safeShutdown();
            }
        }

        public void shutdown() throws IOException {
            in.close();
            out.close();
            pool.shutdown();
            if (!client.isClosed()) {
                client.close();
            }
        }

        private void safeShutdown() {
            try {
                shutDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }


    }




    public static void main(String[] args) {
        // write your code here
        Server server = new Server();
        server.run();

    }
}
