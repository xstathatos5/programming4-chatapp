package com.chat.server.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.chat.server.model.Message;


public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    private String username;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        try {
            this.writer = new ObjectOutputStream(socket.getOutputStream());
            this.writer.flush();
            this.reader = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            ServerLogger.log("ERROR", "Failed to create streams: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            Object obj = reader.readObject();
            if (!(obj instanceof String) || ((String) obj).isBlank()) {
                closeConnection();
                return;
            }
            this.username = sanitize((String) obj);
            
            ServerLogger.log("JOIN", username + " connected from " + socket.getInetAddress());
            server.broadcast(Message.systemMessage(username + " has joined the chat"), this);
            server.broadcastUserList();

            while (running) {
                Object incoming = reader.readObject();
                if (incoming instanceof Message msg) {
                    String content = msg.getContent();
                    if (content.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    Message chatMessage = new Message(username, content);
                    ServerLogger.log("MESSAGE", username + ": " + content);
                    server.broadcast(chatMessage, this);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(Message message) {
        try {
            writer.writeObject(message);
            writer.flush();
        } catch (IOException e) {
            ServerLogger.log("ERROR", "Failed to send to " + username + ": " + e.getMessage());
        }
    }

    public void sendObject(Object obj) {
        try {
            writer.writeObject(obj);
            writer.flush();
        } catch (IOException e) {
            ServerLogger.log("ERROR", "Failed to send object to " + username);
        }
    }

    public String getUsername() {
        return username;
    }

    private void closeConnection() {
        running = false;
        server.removeClient(this);
        if (username != null) {
            ServerLogger.log("LEAVE", username + " disconnected");
            server.broadcast(Message.systemMessage(username + " has left the chat"), this);
            server.broadcastUserList();
        }
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    private String sanitize(String input) {
        return input.replaceAll("[<>&\"']", "");
    }
}

