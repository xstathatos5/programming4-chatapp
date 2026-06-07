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
import javafx.scene.control.ListCell;
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
    private ObjectOutputStream writer;
    private ObjectInputStream reader;
    private ListView<String> userListView;

    private final String BACKGROUND_BLACK = "-fx-background-color: #000000;";
    private final String LABEL_STYLE = "-fx-text-fill: white; -fx-font-family: 'Courier New';";
    private final String INPUT_STYLE = "-fx-background-color: #111111; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-color: #444444;";
    private final String BUTTON_STYLE = "-fx-background-color: #222222; -fx-text-fill: white; -fx-border-color: #555555; -fx-border-width: 1; -fx-cursor: hand;";
    private final String DISCONNECT_BUTTON_STYLE = "-fx-background-color: #8B0000; -fx-text-fill: white; -fx-border-color: #555555; -fx-border-width: 1; -fx-cursor: hand;";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        writer = null;
        reader = null;
        socket = null;
        chatArea = null;
        userListView = null;
        messageField = null;

        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(20));
        loginLayout.setStyle(BACKGROUND_BLACK);

        Label label = new Label("Enter your username:");
        label.setStyle(LABEL_STYLE + " -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(INPUT_STYLE);

        Button loginButton = new Button("Connect");
        loginButton.setPrefWidth(100);
        loginButton.setStyle(BUTTON_STYLE);
        
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(BUTTON_STYLE + " -fx-background-color: #333333;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(BUTTON_STYLE));

        loginLayout.getChildren().addAll(label, usernameField, loginButton);

        Scene loginScene = new Scene(loginLayout, 350, 180);

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
        root.setStyle(BACKGROUND_BLACK);

        HBox topBar = new HBox();
        topBar.setPadding(new Insets(6, 12, 6, 12));
        topBar.setStyle("-fx-background-color: #000000; -fx-border-color: #444444; -fx-border-width: 0 0 1 0;");
        statusLabel = new Label("Connected as: " + username);
        statusLabel.setStyle(LABEL_STYLE);
        topBar.getChildren().add(statusLabel);
        root.setTop(topBar);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setStyle("-fx-control-inner-background: #111111; -fx-background-color: #111111; -fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        root.setCenter(chatArea);

        VBox rightPanel = new VBox(8);
        rightPanel.setPadding(new Insets(0, 0, 0, 10));
        rightPanel.setPrefHeight(150);
        rightPanel.setStyle("-fx-background-color: #000000; -fx-border-color: #444444; -fx-border-width: 0 0 0 1;");

        Label sidebarTitle = new Label("Connected Users");
        sidebarTitle.setStyle(LABEL_STYLE);
        
        userListView = new ListView<>();
        userListView.setStyle("-fx-background-color: #111111; -fx-control-inner-background: #111111; -fx-border-color: #444444;");
        userListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #111111;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #111111; -fx-text-fill: white; -fx-font-family: 'Courier New';");
                }
            }
        });
        
        VBox.setVgrow(userListView, javafx.scene.layout.Priority.ALWAYS);
        userListView.getItems().add(username + " (You)");

        Button disconnectButton = new Button("Disconnect");
        disconnectButton.setPrefWidth(120);
        disconnectButton.setStyle(DISCONNECT_BUTTON_STYLE);
        disconnectButton.setOnMouseEntered(e -> disconnectButton.setStyle(DISCONNECT_BUTTON_STYLE + " -fx-background-color: #A00000;"));
        disconnectButton.setOnMouseExited(e -> disconnectButton.setStyle(DISCONNECT_BUTTON_STYLE));
        disconnectButton.setOnAction(e -> disconnectFromServer());

        rightPanel.getChildren().addAll(sidebarTitle, userListView, disconnectButton);
        root.setRight(rightPanel);

        HBox bottomBar = new HBox(10);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));
        bottomBar.setStyle(BACKGROUND_BLACK);
        
        messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setStyle(INPUT_STYLE);
        HBox.setHgrow(messageField, javafx.scene.layout.Priority.ALWAYS);
        
        Button sendButton = new Button("Send");
        sendButton.setPrefWidth(80);
        sendButton.setStyle(BUTTON_STYLE);
        sendButton.setOnMouseEntered(e -> sendButton.setStyle(BUTTON_STYLE + " -fx-background-color: #333333;"));
        sendButton.setOnMouseExited(e -> sendButton.setStyle(BUTTON_STYLE));
        
        bottomBar.getChildren().addAll(messageField, sendButton);
        root.setBottom(bottomBar);

        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());

        Scene scene = new Scene(root, 600, 400);

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
        } catch (IOException e) {
            appendChatMessage("Failed to send message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void startListening() {
        Thread receiverThread = new Thread(() -> {
            try {
                Object incoming;
                while (socket != null && !socket.isClosed() && (incoming = reader.readObject()) != null) {
                    if (incoming instanceof Message) {
                        Message message = (Message) incoming;
                        Platform.runLater(() -> appendChatMessage(message.toString()));
                    } else if (incoming instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> users = (List<String>) incoming;
                        Platform.runLater(() -> userListView.getItems().setAll(users));
                    }
                }
            } catch (Exception e) {
                if (socket != null) {
                    Platform.runLater(() -> {
                        showAlert("Disconnected", "Lost connection to the server.");
                        disconnectFromServer();
                    });
                }
            }
        });
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    private void appendChatMessage(String message){
        if (chatArea != null) {
            chatArea.appendText(message + "\n");
        }
    }

    private void disconnectFromServer() {
        try {
            if (writer != null) {
                writer.writeObject(new Message("/quit"));
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { if (writer != null) writer.close(); } catch (IOException ignored) {}
            try { if (reader != null) reader.close(); } catch (IOException ignored) {}
            try { if (socket != null) socket.close(); } catch (IOException ignored) {}

            writer = null;
            reader = null;
            socket = null;
        }

        Platform.runLater(() -> showLoginScreen());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        alert.getDialogPane().setStyle("-fx-background-color: #111111;");
        alert.getDialogPane().lookup(".label").setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New';");
        
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}