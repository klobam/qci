package com.example.clientqci;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.*;

import com.example.Message;

public class chatSceneController {

    @FXML
    private ListView<String> usersList;
    @FXML
    private TextField textField;
    @FXML
    private VBox msgArea;

    private PrintWriter writer;
    private String nickname;

    private final Map<String, List<Message>> messageHistory = new HashMap<>();

    public void init(PrintWriter writer, BufferedReader reader, String nickname) {
        this.writer = writer;
        this.nickname = nickname;

        
        usersList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> showMessagesFrom(newVal));
        });

        new Thread(() -> {
            String xml;
            try {
                while ((xml = reader.readLine()) != null) {
                    if (xml.startsWith("__users__:")) {
                        String[] users = xml.substring("__users__:".length()).split(",");
                        Platform.runLater(() -> {
                            usersList.getItems().setAll(users);
                            usersList.getItems().remove(nickname);
                        });
                        continue;
                    }

                    Message msg = Message.fromXML(xml);

                    
                    messageHistory.computeIfAbsent(msg.sender, k -> new ArrayList<>()).add(msg);

                    Platform.runLater(() -> {
                        String selected = usersList.getSelectionModel().getSelectedItem();
                        if (msg.sender.equals(selected)) {
                            Label msgLabel = new Label("From " + msg.sender + ": " + msg.text);
                            msgArea.getChildren().add(msgLabel);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void SendMessage() {
        String receiver = usersList.getSelectionModel().getSelectedItem();
        if (receiver == null || textField.getText().isEmpty()) return;

        Message msg = new Message(nickname, receiver, textField.getText());
        writer.println(msg.toXML());

        
        messageHistory.computeIfAbsent(receiver, k -> new ArrayList<>()).add(msg);

        Platform.runLater(() -> {
            if (receiver.equals(usersList.getSelectionModel().getSelectedItem())) {
                Label msgLabel = new Label("You: " + msg.text);
                msgArea.getChildren().add(msgLabel);
            }
        });

        textField.clear();
    }

    @FXML
    private void handleExit() {
        writer.println("__exit__");
        Platform.exit();
    }

    private void showMessagesFrom(String user) {
        msgArea.getChildren().clear();
        List<Message> messages = messageHistory.getOrDefault(user, Collections.emptyList());
        for (Message msg : messages) {
            String displayText = msg.sender.equals(nickname)
                    ? "You: " + msg.text
                    : "From " + msg.sender + ": " + msg.text;
            msgArea.getChildren().add(new Label(displayText));
        }
    }
}
