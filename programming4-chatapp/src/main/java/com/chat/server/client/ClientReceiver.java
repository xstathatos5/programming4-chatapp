package com.chat.server.client;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.chat.server.model.Message;


public class ClientReceiver implements Runnable {
    
    private ObjectInputStream reader;

    public ClientReceiver(ChatClient client, ObjectInputStream reader) { 
        this.reader = reader;
    }

    @Override
    public void run() {
        try {
            Object input = reader.readObject();
            Message serverMessage;
            while((serverMessage = (Message) reader.readObject()) != null) {
                System.out.println(serverMessage.getContent());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}
