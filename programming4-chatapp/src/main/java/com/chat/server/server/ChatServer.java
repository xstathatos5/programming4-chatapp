package com.chat.server.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ChatServer {
    private static final int PORT = 5000;
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    public void start() {
        ServerLogger.log("SERVER","Chat server on port " + PORT);
        
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
    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
    public static void main(String[] args) {
        new ChatServer().start();
    }
    
}
