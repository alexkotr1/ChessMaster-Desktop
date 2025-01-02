package com.alexk.chess;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application implements WebSocketMessageListener {
    private static boolean isHost;
    private static String gameCode;
    private static boolean isOfflineMode;
    private final Stage dialogStage = new Stage();
    private Stage primaryStage = new Stage();
    private static Label messageLabel;
    public static WebSocket webSocket;
    private ChessApplication chessApp;
    private int timerMinutes = 10; // Default timer value

    @Override
    public void start(Stage primaryStage) {
        VBox root = getDialogStage();
        this.primaryStage = primaryStage;
        Scene scene = new Scene(root, 300, 300);
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

        HBox timerControls = new HBox(10);
        timerControls.setPadding(new Insets(10));
        timerControls.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5;");

        Label timerLabel = new Label("Timer (minutes): ");
        Label timerValueLabel = new Label(String.valueOf(timerMinutes));

        Button increaseTimerButton = new Button("▶");
        increaseTimerButton.setOnAction(e -> {
            timerMinutes++;
            timerValueLabel.setText(String.valueOf(timerMinutes));
        });

        Button decreaseTimerButton = new Button("◀");
        decreaseTimerButton.setOnAction(e -> {
            if (timerMinutes > 1) {
                timerMinutes--;
                timerValueLabel.setText(String.valueOf(timerMinutes));
            }
        });

        timerControls.getChildren().addAll(timerLabel, decreaseTimerButton, timerValueLabel, increaseTimerButton);

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
            codeField.setVisible(true);
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
                message.setCode(RequestCodes.JOIN_GAME);
                message.setData(gameCode);
                message.send(webSocket);
                GameSession.setState(GameSession.GameState.WAITING_FOR_PLAYER_JOIN);
                message.onReply(res -> {
                    if (res.getCode() == RequestCodes.JOIN_GAME_FAILURE) {
                        Platform.runLater(() -> messageLabel.setText("Invalid Code!"));
                        return;
                    }
                    Platform.runLater(() -> {
                        try {
                            chessApp = new ChessApplication();
                            chessApp.setMode(false, false);
                            chessApp.setWebSocket(webSocket);
                            chessApp.setMinutesAllowed(Message.mapper.readValue(res.getData(),int.class));
                            chessApp.start(primaryStage);
                            dialogStage.close();
                        }catch(JsonProcessingException err){
                            System.err.println(err.getMessage());
                        }

                    });
                });
                return;
            }
            if (isOfflineMode) {
                chessApp = new ChessApplication();
                chessApp.setMode(true, true);
                chessApp.setMinutesAllowed(timerMinutes);
                chessApp.start(primaryStage);
                dialogStage.close();
            } else if (isHost) {
                Message message = new Message();
                message.setCode(RequestCodes.HOST_GAME);
                message.setData(timerMinutes);
                message.send(webSocket);
                GameSession.setState(GameSession.GameState.WAITING_FOR_HOST_CODE);
                message.onReply(res -> {
                    GameSession.setState(GameSession.GameState.WAITING_FOR_PLAYER_JOIN);
                    Platform.runLater(() -> messageLabel.setText("Code: " + res.getData()));
                });
            }
        });

        root.getChildren().addAll(timerControls, hostButton, joinButton, offlineButton, codeField, confirmButton, messageLabel);
        return root;
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
        if (message.getCode() == RequestCodes.SECOND_PLAYER_JOINED) {
            Platform.runLater(() -> {
                chessApp = new ChessApplication();
                chessApp.setMode(false, true);
                chessApp.setWebSocket(webSocket);
                chessApp.setMinutesAllowed(timerMinutes); // Pass the timer value
                chessApp.start(primaryStage);
                dialogStage.close();
            });
        }
    }
}
