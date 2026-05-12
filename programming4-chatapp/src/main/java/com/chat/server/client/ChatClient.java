package com.chat.server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import com.chat.server.model.Message;

public class ChatClient {
    private String hostName;
    private int port;
    private String username;

    public ChatClient(String hostname, int port){
        this.hostName = hostname;
        this.port = port;
    }

    public void execute() {
        try (Socket socket = new Socket(hostName, port);
             ObjectOutputStream writer = new ObjectOutputStream(socket.getOutputStream());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the chat server");
            System.out.print("Enter your username: ");
            this.username = scanner.nextLine();
            writer.writeObject(new Message(username));
            writer.flush();

            new Thread(() -> {
                try {
                    Object incoming;
                    while ((incoming = reader.readLine()) != null) {
                        if (incoming instanceof Message) {
                            Message message = (Message) incoming;
                            System.out.println(message.getContent());
                        } else {
                            System.out.println(incoming);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            String text;
            do {
                System.out.print("> ");
                text = scanner.nextLine();
                writer.writeObject(new Message(text));
                writer.flush();
            } while (!text.equalsIgnoreCase("/quit"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 5000);
        client.execute();
    }
}
