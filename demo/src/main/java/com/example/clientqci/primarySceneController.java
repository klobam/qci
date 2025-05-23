package com.example.clientqci;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class primarySceneController {

    @FXML
    private TextField IPField;
    @FXML
    private TextField UsernameField;

    private static PrintWriter writer;
    private static BufferedReader reader;
    private static Socket socket;

    @FXML
    private void switchToChatScene(ActionEvent event) {
        String serverIP = IPField.getText().trim();
        String nickname = UsernameField.getText().trim();

        if (serverIP.isEmpty() || nickname.isEmpty()) return;

        try {
            socket = new Socket(serverIP, 5050);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer.println(nickname);

            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chatScene.fxml"));
            AnchorPane root = loader.load();

            chatSceneController controller = loader.getController();
            controller.init(writer, reader, nickname);

            Stage stage = (Stage) IPField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Messenger - " + nickname);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}