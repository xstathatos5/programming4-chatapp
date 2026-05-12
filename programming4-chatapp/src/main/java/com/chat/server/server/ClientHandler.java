package com.chat.server.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ChatServer server;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            this.username = reader.readLine();
            if (username == null) {
                closeConnection();
                return;
            }
            server.broadcast(username + " has joined the chat!", this);
            System.out.println("User " + username + " is now chatting.");

            String message;
            while ((message = reader.readLine()) != null) {
                ServerLogger.log("MESSAGE", "Received message from " + username + ": " + message);
                server.broadcast(username + ": " + message, this);
                if (message.equalsIgnoreCase("/quit")) {
                    break;
                }
                System.out.println("Received message from " + username + ": " + message);
                server.broadcast(username + ": " + message, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    private void closeConnection() {
        try {
            if (username != null) {
                server.broadcast(username + " has left the chat.", this);
                System.out.println("User " + username + " has left the chat.");
            }
            server.removeClient(this);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
