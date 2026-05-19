package com.chat.server.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.chat.server.model.Message;


public class ChatServer {
    private static final int PORT = 5000;
    private static final int WEB_PORT = 8000;
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final AtomicLong totalMessages = new AtomicLong(0);
    public void start() {
        ServerLogger.log("SERVER","Chat server on port " + PORT);

        WebServer webServer = new WebServer(WEB_PORT, this);
        webServer.start();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            ServerLogger.log("SERVER", "Chat server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ServerLogger.log("CONNECTION", "New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
            
        } catch (IOException e) {
            ServerLogger.log("ERROR", "Error occurred while starting the server: " + e.getMessage());
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
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public int getClientCount() {
        return clients.size();
    }
    public long getTotalMessages() {
        return totalMessages.get();
    }
    public static void main(String[] args) {
        new ChatServer().start();
    }
    
}
