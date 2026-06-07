package com.chat.server.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.chat.server.model.Message;


public class ChatServer {
    private static final int CHAT_PORT = 5000;
    private static final int WEB_PORT = 8000;
    
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final AtomicLong totalMessages = new AtomicLong(0);
    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public void start() {
        ServerLogger.log("SERVER", "Starting chat server...");
        WebServer webServer = new WebServer(WEB_PORT, this);
        webServer.start();

        try {
            serverSocket = new ServerSocket(CHAT_PORT);
            ServerLogger.log("SERVER", "Chat server listening on port " + CHAT_PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ServerLogger.log("CONNECTION", "New connection from " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            if (running) {
                ServerLogger.log("ERROR", "Server error: " + e.getMessage());
            }
        }
    }

    public void broadcast(Message message, ClientHandler sender) {
        totalMessages.incrementAndGet();
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void broadcastUserList() {
        List<String> usernames = getUsernames();
        for (ClientHandler client : clients) {
            client.sendObject(usernames);
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    public int getClientCount() {
        return clients.size();
    }

    public long getTotalMessages() {
        return totalMessages.get();
    }

    public List<String> getUsernames() {
        if (clients.isEmpty()) {
            return List.of();
        }
        return clients.stream()
            .map(ClientHandler::getUsername)
            .filter(name -> name != null)
            .toList();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        new ChatServer().start();
    }
}

