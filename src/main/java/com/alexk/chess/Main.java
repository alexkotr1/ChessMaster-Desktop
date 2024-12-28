package com.alexk.chess;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.websocket.Session;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Scanner;

public class Main extends Application implements WebSocketMessageListener{
    private static boolean isHost;
    private static String gameCode;
    private static boolean isOfflineMode;
    private Stage dialogStage = new Stage();
    private Stage primaryStage = new Stage();
    private static Label messageLabel;
    private static WebSocket webSocket;
    @Override
    public void start(Stage primaryStage)  {
        VBox root = getDialogStage();
        this.primaryStage = primaryStage;
        Scene scene = new Scene(root, 300, 250);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

    }

    public VBox getDialogStage() {
        webSocket = new WebSocket(this);
        dialogStage.initModality(Modality.NONE);
        dialogStage.setTitle("Game Options");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Button hostButton = new Button("Host a Game");
        Button joinButton = new Button("Join a Game");
        Button offlineButton = new Button("Offline Mode");

        TextField codeField = new TextField();
        codeField.setPromptText("Enter game code");
        codeField.setVisible(false);
        messageLabel = new Label();

        Button confirmButton = new Button("Confirm");

        hostButton.setOnAction(e -> {
            isHost = true;
            isOfflineMode = false;
            gameCode = null;
            messageLabel.setText("You have chosen to host a game.");
            codeField.setVisible(false);
        });

        joinButton.setOnAction(e -> {
            isHost = false;
            isOfflineMode = false;
            messageLabel.setText("You have chosen to join a game. Please enter the game code.");
            codeField.setVisible(true); // Show the code field
        });

        offlineButton.setOnAction(e -> {
            isOfflineMode = true;
            isHost = false;
            gameCode = null;
            messageLabel.setText("You have chosen offline mode.");
            codeField.setVisible(false);
        });

        confirmButton.setOnAction(e -> {
            if (!isHost && !isOfflineMode && codeField.isVisible()) {
                gameCode = codeField.getText();
                Message message = new Message();
                message.setCode(Message.RequestCodes.JOIN_GAME);
                message.setData(gameCode);
                try {
                    message.send(new WebSocket(this));
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
                GameSession.setState(GameSession.GameState.WAITING_FOR_PLAYER_JOIN);
                return;
            }
            if (isOfflineMode) {
                ChessApplication chessApp = new ChessApplication();
                chessApp.setMode(true);
                chessApp.start(primaryStage);
            } else if (isHost) {
                WebSocket webSocket = new WebSocket(this);
                Message message = new Message();
                message.setCode(Message.RequestCodes.HOST_GAME);
                try {
                    message.send(webSocket);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
                GameSession.setState(GameSession.GameState.WAITING_FOR_HOST_CODE);
            }
        });

        root.getChildren().addAll(hostButton, joinButton, offlineButton, codeField, confirmButton, messageLabel);
        return root;
    }
    public void startOnPlayerJoin(){
        ChessApplication chessApp = new ChessApplication();
        chessApp.setMode(false);
        chessApp.start(primaryStage);
    }
    public static boolean isHost() {
        return isHost;
    }

    public static boolean isOfflineMode() {
        return isOfflineMode;
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void onMessageReceived(Message message) {
        System.out.println("Message received in Main: " + message.getCode() + ", Data: " + message.getData());

        Platform.runLater(() -> {
            if (message.getCode() == Message.RequestCodes.HOST_GAME_RESULT) {
                GameSession.setState(GameSession.GameState.WAITING_FOR_PLAYER_JOIN);
                messageLabel.setText("Code: " + message.getData());
            } else if (message.getCode() == Message.RequestCodes.JOIN_GAME_RESULT) {

            }
        });
    }

}
