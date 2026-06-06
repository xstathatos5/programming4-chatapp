package com.chat.server.GUI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.chat.server.model.Message;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ChatAppGUI extends Application {
    private String hostName = "localhost";
    private int port = 5000;
    private String username;

    private Socket socket;
    private TextField messageField;
    private TextArea chatArea;
    private Label statusLabel;
    private Stage primaryStage;
    private String cssPath;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    private ListView<String> userListView;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        try {
            cssPath = getClass().getResource("/styles.css").toExternalForm();
        } catch (NullPointerException e) {
            System.out.println("CSS file not found. Make sure styles.css is in the resources folder.");  
        }
        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(20));

        Label label = new Label("Enter your username:");
        label.setStyle("-fx-font-family: Courier New; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        Button loginButton = new Button("Connect");
        loginButton.setPrefWidth(100);

        loginLayout.getChildren().addAll(label, usernameField, loginButton);

        Scene loginScene = new Scene(loginLayout, 350, 180);

        if (cssPath != null) {
            loginScene.getStylesheets().add(cssPath);
        }

        primaryStage.setTitle("Chat App Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();

        loginButton.setOnAction(e -> handleLoginAttempt(usernameField.getText().trim()));
        usernameField.setOnAction(e -> handleLoginAttempt(usernameField.getText().trim()));
    }

    private void handleLoginAttempt(String usernameInput){
        if (!usernameInput.isEmpty()){
            this.username = usernameInput;
            if (connectToServer()) {
                showChatScreen();
            } else {
                showAlert("Connection Error", "Unable to connect to the chat server. Please check your connection and try again.");
            }
        }
    }

    private void showChatScreen(){
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox topBar = new HBox();
        topBar.setPadding(new Insets(6, 12, 6, 12));
        topBar.getStyleClass().add("top-bar");
        statusLabel = new Label("Connected as: " + username);
        topBar.getChildren().add(statusLabel);
        root.setTop(topBar);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        root.setCenter(chatArea);

        VBox rightPanel = new VBox(8);
        rightPanel.setPadding(new Insets(0, 0, 0, 10));
        rightPanel.getStyleClass().add("right-panel");
        rightPanel.setPrefHeight(150);

        Label sidebarTitle = new Label("Connected Users");
        sidebarTitle.setStyle("-fx-font-family: Courier New");
        
        userListView = new ListView<>();
        userListView.getStyleClass().add("user-list");
        VBox.setVgrow(userListView, javafx.scene.layout.Priority.ALWAYS);
        userListView.getItems().add(username + " (You)");

        Button disconnectButton = new Button("Disconnect");
        disconnectButton.setPrefWidth(120);
        disconnectButton.getStyleClass().add("disconnect-button");
        disconnectButton.setOnAction(e -> disconnectFromServer());

        rightPanel.getChildren().addAll(sidebarTitle, userListView, disconnectButton);
        root.setRight(rightPanel);

        HBox bottomBar = new HBox(10);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));
        messageField = new TextField();
        messageField.setPromptText("Type your message...");
        HBox.setHgrow(messageField, javafx.scene.layout.Priority.ALWAYS);
        Button sendButton = new Button("Send");
        sendButton.setPrefWidth(80);
        bottomBar.getChildren().addAll(messageField, sendButton);
        root.setBottom(bottomBar);

        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());

        Scene scene = new Scene(root, 600, 400);
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        primaryStage.setTitle("Chat App - " + username);
        primaryStage.setScene(scene);

        startListening();
    }

    private boolean connectToServer() {
        try {
            socket = new Socket(hostName, port);
            writer = new ObjectOutputStream(socket.getOutputStream());
            reader = new ObjectInputStream(socket.getInputStream());

            writer.writeObject(username);
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendMessage(){
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;

        if (text.equalsIgnoreCase("/quit")) {
            disconnectFromServer();
            return;
        }
        Message message = new Message(this.username + ": " + text);
        if (sendMessageToServer(message)) {
            appendChatMessage(message.toString());
            messageField.clear();
        } else {
            showAlert("Error", "Failed to send message. Please try again.");
        }
    }

    private boolean sendMessageToServer(Message message) {
        try {
            writer.writeObject(message);
            writer.flush();
            return true;
            }catch (IOException e) {
            appendChatMessage("Failed to send message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void startListening() {
        Thread recieverThread = new Thread(() -> {
            try {
                Object incoming;
                while(socket != null && !socket.isClosed() && (incoming = reader.readObject()) != null){
                    if (incoming instanceof Message){
                        Message message = (Message) incoming;
                        Platform.runLater(() -> {
                            appendChatMessage(message.toString());
                            handleDirectoryUpdates(message.getContent());
                        });
                    }
                }
            } catch (Exception e) {
            }
        });
        recieverThread.setDaemon(true);
        recieverThread.start();
    }

    private void appendChatMessage(String message){
        if (chatArea != null) {
            chatArea.appendText(message + "\n");
        }
    }
    private void handleDirectoryUpdates(String messageContent) {
        if (messageContent.contains("has joined the chat") || messageContent.contains("has left the chat")) {
            updateUserList();
        }
    }
    private void disconnectFromServer() {
        try{
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateUserList() {
        try {
            writer.writeObject(new Message("/list"));
            writer.flush();
            Object response = reader.readObject();
            if (response instanceof List) {
                List<String> users = (List<String>) response;
                Platform.runLater(() -> {
                    userListView.getItems().setAll(users);
                });
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void main(String[] args) {
        launch(args);
    }




}
