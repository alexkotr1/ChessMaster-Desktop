package com.alexk.chess;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application implements WebSocketMessageListener {
    private static boolean isHost;
    private static String gameCode;
    private static boolean isOfflineMode;
    private final Stage dialogStage = new Stage();
    private Stage primaryStage = new Stage();
    private static Label messageLabel;
    public static WebSocket webSocket;
    private int timerMinutes = 10;

    @Override
    public void start(Stage primaryStage) {
        VBox root = getDialogStage();
        this.primaryStage = primaryStage;

        Scene scene = new Scene(root, 400, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    public VBox getDialogStage() {
        webSocket = new WebSocket(this);
        dialogStage.initModality(Modality.NONE);
        dialogStage.setTitle("Chess Master");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2C3E50, #4CA1AF);");

        Label titleLabel = new Label("Chess Master");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(5, Color.BLACK));

        Button hostButton = createButton("Host a Game");
        Button joinButton = createButton("Join a Game");
        Button offlineButton = createButton("Offline Mode");
        Button confirmButton = createButton("Confirm");

        TextField codeField = new TextField();
        codeField.setPromptText("Enter game code");
        codeField.setVisible(false);

        messageLabel = new Label();
        messageLabel.setTextFill(Color.GOLD);
        messageLabel.setWrapText(true);

        HBox timerControls = new HBox(10);
        timerControls.setAlignment(Pos.CENTER);
        timerControls.setVisible(false);

        Label timerLabel = new Label("Timer (minutes): ");
        timerLabel.setTextFill(Color.WHITE);

        Label timerValueLabel = new Label(String.valueOf(timerMinutes));
        timerValueLabel.setTextFill(Color.GOLD);
        timerValueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Button increaseTimerButton = createControlButton("▶");
        Button decreaseTimerButton = createControlButton("◀");

        increaseTimerButton.setOnAction(e -> {
            timerMinutes++;
            timerValueLabel.setText(String.valueOf(timerMinutes));
        });

        decreaseTimerButton.setOnAction(e -> {
            if (timerMinutes > 1) {
                timerMinutes--;
                timerValueLabel.setText(String.valueOf(timerMinutes));
            }
        });

        timerControls.getChildren().addAll(timerLabel, decreaseTimerButton, timerValueLabel, increaseTimerButton);

        hostButton.setOnAction(e -> {
            timerControls.setVisible(true);
            isHost = true;
            isOfflineMode = false;
            gameCode = null;
            messageLabel.setText("You have chosen to host a game.");
            codeField.setVisible(false);
        });

        joinButton.setOnAction(e -> {
            timerControls.setVisible(false);
            isHost = false;
            isOfflineMode = false;
            messageLabel.setText("You have chosen to join a game. Please enter the game code.");
            codeField.setVisible(true);
        });

        offlineButton.setOnAction(e -> {
            timerControls.setVisible(true);
            isOfflineMode = true;
            isHost = false;
            gameCode = null;
            messageLabel.setText("You have chosen offline mode.");
            codeField.setVisible(false);
        });

        confirmButton.setOnAction(e -> handleConfirm(codeField));

        root.getChildren().addAll(
                titleLabel,
                timerControls,
                hostButton,
                joinButton,
                offlineButton,
                codeField,
                confirmButton,
                messageLabel
        );

        return root;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setStyle("-fx-background-color: #16A085; -fx-text-fill: white; -fx-background-radius: 10;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #1ABC9C; -fx-text-fill: white; -fx-background-radius: 10;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #16A085; -fx-text-fill: white; -fx-background-radius: 10;"));
        return button;
    }

    private Button createControlButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: #2980B9; -fx-text-fill: white; -fx-background-radius: 5;");
        return button;
    }

    private void handleConfirm(TextField codeField) {
        if (!isHost && !isOfflineMode && codeField.isVisible()) {
            gameCode = codeField.getText();
            Message message = new Message();
            message.setCode(RequestCodes.JOIN_GAME);
            message.setData(gameCode);
            message.send(webSocket);
            message.onReply(res -> {
                if (res.getCode() == RequestCodes.JOIN_GAME_FAILURE) {
                    Platform.runLater(() -> messageLabel.setText("Invalid Code!"));
                    return;
                }
                try { timerMinutes = Message.mapper.readValue(res.getData(),int.class);}
                catch (JsonProcessingException e) { System.err.println(e.getMessage()); }
                Platform.runLater(() -> startGame(false,false));
            });
        } else if (isOfflineMode) {
            startGame(true,true);
        } else if (isHost) {
            Message message = new Message();
            message.setCode(RequestCodes.HOST_GAME);
            message.setData(timerMinutes);
            message.send(webSocket);
            message.onReply(res -> Platform.runLater(() -> messageLabel.setText("Code: " + res.getData())));
        }
    }

    private void startGame(boolean offlineMode, boolean isHost) {
        ChessApplication chessApp = new ChessApplication();
        if (!offlineMode) chessApp.setWebSocket(webSocket);
        chessApp.setMode(offlineMode, isHost);
        chessApp.setMinutesAllowed(timerMinutes);
        chessApp.start(primaryStage);
        dialogStage.close();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message.getCode() == RequestCodes.SECOND_PLAYER_JOINED) {
            Platform.runLater(() -> startGame(false,true));
        }
    }
}
