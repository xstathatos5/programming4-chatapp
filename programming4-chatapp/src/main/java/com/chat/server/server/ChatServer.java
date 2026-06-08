package com.chat.server.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.chat.server.model.Message;

/**
 * Main Server component for the chat app.
 * The server is responsible for:
 * <ul>
 * <li>Accepting incoming client connections</li>
 * <li>Creating and managing client handler threads</li>
 * <li>Broadcasting messages between connected clients</li>
 * <li>Maintaining a list of active users</li>
 * <li>Providing the server statistics to the web server for logging</li>
 * </ul>
 * 
 * <p>The server uses a thread-safe collection to store multiple clients
 * at the same time.</p>
 * 
 * @author Xen Stathatos
 */

public class ChatServer {
    private static final int CHAT_PORT = 5000;
    private static final int WEB_PORT = 8000;
    
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final AtomicLong totalMessages = new AtomicLong(0);
    private volatile boolean running = true;
    private ServerSocket serverSocket;

    /**
     * Starts the server and begins listening for client
     * connections.
     * 
     * This method:
     * <ul>
     * <li>Starts the dashboard</li>
     * <li>Creates the main server socket</li>
     * <li>Accepts incoming client connections</li>
     * <li>Creates a new {@code ClientHandler} for each client</li>
     * </ul>
     * 
     */
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

    /**
     * Broadcasts a message to all connected clients except the sender
     * @param message
     * @param sender
     */
    public void broadcast(Message message, ClientHandler sender) {
        totalMessages.incrementAndGet();
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Sends the current list of connected usernames to all clients,
     * the method is usually called when users connect or disconnect from the server
     */
    public void broadcastUserList() {
        List<String> usernames = getUsernames();
        for (ClientHandler client : clients) {
            client.sendObject(usernames);
        }
    }
    /**
     * Removes Client
     * @param handler
     */
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

    /**
     * Stops the chat server and closes the socket
     * not allowing any other clients to join
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Application entry point creates a new chat server.
     * @param args
     */
    public static void main(String[] args) {
        new ChatServer().start();
    }
}

