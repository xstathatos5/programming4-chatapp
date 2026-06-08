package com.chat.server.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.chat.server.model.Message;

/**
 * Handles communication between the chat server and
 * a single connected client.
 * 
 * Each client connection is managed by its own {@code ClientHandler}
 * running on a separate thread.
 * <ul>
 * <li>Reading incoming data from the client</li>
 * <li>Processing usernames and chat messages</li>
 * <li>Sending messages back to the client.</li>
 * <li>Notifying the server when clients join or leave</li>
 * </ul>
 * 
 * <p>This class acts as the bridge between a connected socket and 
 * the {@link ChatServer} message broadcasting system</p>
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    private String username;
    private volatile boolean running = true;

    /**
     * Creates a new handler for a connected client socket
     * @param socket
     * @param server
     */
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

    /**
     * Main execution for the client handler thread
     * 
     * The method includes:
     * <ul>
     * <li>Reads the username from the client</li>
     * <li>Join notifications</li>
     * <li>Continously listens for messages</li>
     * <li>Handles client connection requests</li>
     * </ul>
     */
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
            ServerLogger.log("ERROR", "Client Error: " + e.getMessage());
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

    /**
     * Closes the client connection and performs clean up.
     * 
     * This method:
     * <ul>
     * <li>Removes clients from the server</li>
     * <li>Leave message to other clients</li>
     * <li>Updates the connected user list</li>
     * <li>Closes the socket connection</li>
     * 
     * <p>Automatically called when the client disconnects or
     * an error occurs</p>
     * </ul>
     */
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

    /**
     * Removes potentially unsafe characters
     * @param input
     * @return a cleaned version of username
     */
    private String sanitize(String input) {
        return input.replaceAll("[<>&\"']", "");
    }
}

