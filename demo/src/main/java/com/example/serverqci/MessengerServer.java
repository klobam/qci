package com.example.serverqci;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import com.example.Message;

public class MessengerServer {
    private static final int PORT = 5050;
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        String localIP = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Server started. IP: " + localIP + ", port: " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClientConnection(clientSocket)).start();
        }
    }

    private static void handleClientConnection(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String nickname = reader.readLine();
            if (nickname == null || nickname.trim().isEmpty()) {
                clientSocket.close();
                return;
            }

            ClientHandler handler = new ClientHandler(nickname, clientSocket, reader, writer);
            clients.put(nickname, handler);

            System.out.println(nickname + " connected.");
            broadcastUserList();

            new Thread(handler).start();

        } catch (IOException e) {
            e.printStackTrace();
            try {
                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void handleMessage(String xml) {
        try {
            Message message = Message.fromXML(xml);
            ClientHandler receiver = clients.get(message.receiver);
            if (receiver != null) {
                receiver.sendMessage(xml);
                System.out.println("Message from " + message.sender + " to " + message.receiver + " text: " + message.text);
            } else {
                System.out.println("User " + message.receiver + " is offline.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void broadcastUserList() {
        String userList = String.join(",", clients.keySet());
        for (ClientHandler client : clients.values()) {
            client.sendMessage("__users__:" + userList);
        }
    }

    static class ClientHandler implements Runnable {
        private final String nickname;
        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;
        

        ClientHandler(String nickname, Socket socket, BufferedReader reader, PrintWriter writer) {
            this.nickname = nickname;
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public void run() {
            try {
                String xml;
                while ((xml = reader.readLine()) != null) {
                    if (xml.equals("__exit__")) {
                        break;
                    }
                    handleMessage(xml);
                }
            } catch (IOException e) {
                System.out.println("Connection lost with " + nickname);
            } finally {
                close();
            }
        }

        void sendMessage(String message) {
            writer.println(message);
        }

        void close() {
            try {
                clients.remove(nickname);
                socket.close();
                System.out.println(nickname + " disconnected.");
                broadcastUserList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
