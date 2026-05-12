package com.chat.server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

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
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the chat server");
            System.out.print("Enter your username: ");
            this.username = scanner.nextLine();
            writer.println(username);

            new Thread(() -> {
                try {
                    String response;
                    while ((response = reader.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            String text;
            do {
                System.out.print("> ");
                text = scanner.nextLine();
                writer.println(text);
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
