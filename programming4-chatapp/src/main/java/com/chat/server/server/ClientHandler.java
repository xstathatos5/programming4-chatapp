package com.chat.server.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.chat.server.model.Message;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    private ChatServer server;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        try {
            writer = new ObjectOutputStream(socket.getOutputStream()); 
            reader = new ObjectInputStream(socket.getInputStream());
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Object obj = reader.readObject();
            this.username = (String) obj;
            if (username == null) {
                closeConnection();
                return;
            }
            server.broadcast(new Message(username + " has joined the chat"), this);
            System.out.println("User " + username + " is now chatting.");
            
            
            while (true) {
                try {
                    Object incoming = reader.readObject();
                    if (incoming instanceof Message) {
                        Message message = (Message) incoming;
                        if (message.getContent().equalsIgnoreCase("/quit")) {
                            break;
                        }
                        ServerLogger.log("MESSAGE", username + ": " + message);
                        server.broadcast(message, this);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
       
        } catch (IOException | ClassNotFoundException e) {
            closeConnection();
        }
    }

    public void sendMessage(Message message) {
        try {
            writer.writeObject(message);
            writer.flush();  
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (username != null) {
                server.broadcast(new Message(username + " has left the chat."), this);
                System.out.println("User " + username + " has left the chat.");
            }
            server.removeClient(this);
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
