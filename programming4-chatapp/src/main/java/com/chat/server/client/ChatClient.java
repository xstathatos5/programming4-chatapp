package com.chat.server.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import com.chat.server.model.Message;

/**
 * Chat client that connects to the chat server, sends messages
 * entered by the user and displays incoming messages from other
 * connected users.
 * 
 * <p>The client establishes a socket connections to the
 * specified port and host, sends the username to the server
 * and starts a thread to continuously listen for messages.</p>
 * 
 * <p>User can quit with</p>
 * {@code /quit}
 * @author Xen Stathatos
 */

public class ChatClient {
    private String hostName;
    private int port;
    private String username;

    /**
     * Creates a new chat client
     * @param hostname
     * @param port
     */
    public ChatClient(String hostname, int port){
        this.hostName = hostname;
        this.port = port;
    }

    /**
     * This method:
     * <ul>
     * <li>Establishes a socket connection</li>
     * <li>Sends username to server</li>
     * <li>Reads user input and sends messages</li>
     * </ul>
     */
    public void execute() {
        try (Socket socket = new Socket(hostName, port);
             ObjectOutputStream writer = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the chat server");
            System.out.print("Enter your username: ");
            this.username = scanner.nextLine();
            writer.writeObject(this.username);
            writer.flush();

            new Thread(() -> {
                try {
                    Object incoming;
                    while ((incoming = reader.readObject()) != null) {
                        if (incoming instanceof Message) {
                            Message message = (Message) incoming;
                            System.out.println(message.getContent());
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }).start();

            String text;
            do {
                System.out.print("> ");
                text = scanner.nextLine();
                writer.writeObject(new Message(username, text));
                writer.flush();
            } while (!text.equalsIgnoreCase("/quit"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Application entry point
     * @param args
     */
    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 5000);
        client.execute();
    }
}
