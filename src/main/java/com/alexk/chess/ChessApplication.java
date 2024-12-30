package com.alexk.chess;

import com.alexk.chess.ChessEngine.ChessEngine;
import com.alexk.chess.Pionia.Pioni;
import com.alexk.chess.Pionia.Stratiotis;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class ChessApplication extends Application {
    private final HashMap<Pioni, ImageView> pieces = new HashMap<>();
    private double mouseX;
    private double mouseY;
    private final DropShadow whiteTurnEffect = new DropShadow();
    private final DropShadow blackTurnEffect = new DropShadow();
    private final HashMap<String, ImageView> possibleMoveIndicators = new HashMap<>();
    private final ArrayList<int[]> allPositions = new ArrayList<>();
    private AnchorPane root;
    private final int tile = 90;
    private final int offBoundsEnd = 40;
    private VBox whiteCapturedPawns;
    private VBox blackCapturedPawns;
    private boolean whiteTimerRunning = false;
    private long whiteTimerStartTime;
    private long whitePauseTime = 0;
    private boolean blackTimerRunning = false;
    private long blackTimerStartTime = 0;
    private long blackPauseTime = 0;
    private final int minutesAllowed = 600;
    private Label winnerLabel;
    private Button playAgain;
    private AnchorPane rightPanel;
    private Stage stage;
    private Proxy proxy;
    public ChessEngine chessEngine;
    private boolean offlineMode;
    private WebSocket webSocket;

    public ChessApplication() {
    }


    @Override
    public void start(Stage stage) {
        this.stage = stage;
        initialize();
    }

    private void initialize() {
        root = new AnchorPane();
        rightPanel = new AnchorPane();
        proxy = new Proxy(offlineMode, webSocket);
        chessEngine = proxy.chessEngine;
        System.out.println("Offline Mode:" + offlineMode);
        ImageView background = new ImageView(new Image("chessBoard.jpeg"));
        background.setFitWidth(800);
        background.setFitHeight(800);
        background.setLayoutX(0);
        background.setLayoutY(0);
        background.setPreserveRatio(false);

        whiteTurnEffect.setColor(Color.web("#5BC0EB"));
        whiteTurnEffect.setRadius(1);
        whiteTurnEffect.setSpread(1.0);

        blackTurnEffect.setColor(Color.web("#FFD700"));
        blackTurnEffect.setRadius(1);
        blackTurnEffect.setSpread(1.0);

        rightPanel.setPrefWidth(200);
        rightPanel.setPrefHeight(800);
        rightPanel.setLayoutX(800);
        rightPanel.setLayoutY(0);
        rightPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #F0D09F, #3F2C0E);");

        whiteCapturedPawns = new VBox(10);
        whiteCapturedPawns.setPrefHeight(220);
        whiteCapturedPawns.setPrefWidth(170);
        whiteCapturedPawns.setLayoutX(15);
        whiteCapturedPawns.setLayoutY(30);
        whiteCapturedPawns.setStyle(
                "-fx-border-color: black;" +
                        "-fx-border-insets: 5;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-style: solid inside;"
        );

        for (int x = 0; x < 4; x++) {
            HBox hbox = new HBox();
            hbox.setLayoutX(0);
            hbox.setLayoutY(0);
            hbox.setPrefWidth(170);
            hbox.setPrefHeight(180 / 4);
            hbox.setStyle("-fx-background-color: linear-gradient(to bottom, #F0D09F, #3F2C0E);");
            whiteCapturedPawns.getChildren().add(hbox);
        }

        blackCapturedPawns = new VBox(10);
        blackCapturedPawns = new VBox(10);
        blackCapturedPawns.setPrefHeight(240);
        blackCapturedPawns.setPrefWidth(170);
        blackCapturedPawns.setLayoutX(15);
        blackCapturedPawns.setLayoutY(rightPanel.getPrefHeight() - blackCapturedPawns.getPrefHeight() - 30);
        blackCapturedPawns.setStyle(
                "-fx-border-color: white;" +
                        "-fx-border-insets: 5;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-style: solid inside;"
        );
        for (int x = 0; x < 4; x++) {
            HBox hbox = new HBox();
            hbox.setLayoutX(0);
            hbox.setLayoutY(0);
            hbox.setPrefWidth(170);
            hbox.setPrefHeight(180 / 4);
            hbox.setStyle("-fx-background-color: linear-gradient(to bottom, #3F2C0E, #F0D09F );");
            blackCapturedPawns.getChildren().add(hbox);
        }
        winnerLabel = new Label("");
        winnerLabel.setVisible(false);

        playAgain = new Button("Play Again!");
        playAgain.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        playAgain.setStyle("-fx-background-color: linear-gradient(to bottom, #F0D09F, #3F2C0E);");
        playAgain.setTextFill(Color.web("#F0D09F"));
        playAgain.setVisible(false);
        playAgain.setOnAction(event -> {
            whiteTimerRunning = false;
            whiteTimerStartTime = 0;
            whitePauseTime = 0;
            blackTimerRunning = false;
            blackTimerStartTime = 0;
            blackPauseTime = 0;
            initialize();
        });


        Label whiteTimerlabel = new Label();
        whiteTimerlabel.setText("White Time:");
        whiteTimerlabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        whiteTimerlabel.setTextFill(Color.web("#F0D09F")); // Match chessboard's color scheme
        whiteTimerlabel.setStyle("-fx-background-color: #3F2C0E; " +
                "-fx-padding: 10; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-border-width: 2; " +
                "-fx-border-color: #F0D09F;");
        whiteTimerlabel.setLayoutX(50);
        whiteTimerlabel.setLayoutY(blackCapturedPawns.getLayoutY() - 50);


        AnimationTimer whiteTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (whiteTimerRunning) {
                    long elapsedTime = (now - whiteTimerStartTime + whitePauseTime) / 1_000_000_000;
                    long remainingTime = minutesAllowed - elapsedTime;
                    if (remainingTime <= 0) {
                        chessEngine.getBoard().setGameEndedWinner(true, ChessEngine.Winner.Black);
                        showWinScreen();
                    }
                    long minutes = remainingTime / 60;
                    long seconds = remainingTime % 60;
                    whiteTimerlabel.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }
        };

        Label blackTimerLabel = new Label();
        blackTimerLabel.setText("10:00");
        blackTimerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        blackTimerLabel.setTextFill(Color.web("#F0D09F")); // Match chessboard's color scheme
        blackTimerLabel.setStyle("-fx-background-color: #3F2C0E; " +
                "-fx-padding: 10; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-border-width: 2; " +
                "-fx-border-color: #F0D09F;");
        blackTimerLabel.setLayoutX(50);
        blackTimerLabel.setLayoutY(whiteCapturedPawns.getLayoutY() + whiteCapturedPawns.getPrefHeight() + 10);

        AnimationTimer blackTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (blackTimerRunning) {
                    long elapsedTime = (now - blackTimerStartTime + blackPauseTime) / 1_000_000_000;
                    long remainingTime = minutesAllowed - elapsedTime;
                    if (remainingTime <= 0) {
                        chessEngine.getBoard().setGameEndedWinner(true, ChessEngine.Winner.White);
                        showWinScreen();
                    }
                    long minutes = remainingTime / 60;
                    long seconds = remainingTime % 60;
                    blackTimerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }
        };
        blackTimer.start();
        rightPanel.getChildren().addAll(whiteCapturedPawns, blackCapturedPawns, whiteTimerlabel, blackTimerLabel, winnerLabel);

        root.getChildren().addAll(background, rightPanel);

        ArrayList<Pioni> Pionia = chessEngine.getBoard().getPionia();
        for (Pioni p : Pionia) {
            addPiece(root, p);
        }

        Scene scene = new Scene(root, 1000, 800);
        stage.setTitle("Chess Game");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                allPositions.add(new int[]{x, y});
                ImageView indicator = new ImageView(new Image("marker.png"));
                int[] coordinates = getCoordinates(Utilities.int2Char(x), y);
                indicator.setFitWidth(10);
                indicator.setFitHeight(10);
                indicator.setLayoutX(coordinates[0] - indicator.getFitWidth() / 2);
                indicator.setLayoutY(coordinates[1] - indicator.getFitHeight() / 2);
                indicator.setPreserveRatio(true);
                indicator.setVisible(false);
                root.getChildren().add(indicator);
                possibleMoveIndicators.put(String.valueOf(Utilities.int2Char(x)) + y, indicator);
            }
        }
        whiteTimerStartTime = System.nanoTime();
        whiteTimerRunning = true;
        whiteTimer.start();
        blackTimerStartTime = System.nanoTime();
        blackTimerRunning = false;
        blackTimer.start();
    }

    private void addPiece(AnchorPane root, Pioni p) {
        int[] coordinates = getCoordinates(p.getXPos(), p.getYPos());
        ImageView piece = new ImageView(new Image(p.getImagePath()));
        piece.setFitWidth(60);
        piece.setFitHeight(60);
        piece.setLayoutX(coordinates[0] - piece.getFitWidth() / 2);
        piece.setLayoutY(coordinates[1] - piece.getFitHeight() / 2);
        piece.setPreserveRatio(false);
        piece.setOnMousePressed(event -> {
            mouseX = event.getSceneX() - piece.getLayoutX();
            mouseY = event.getSceneY() - piece.getLayoutY();
        });

        piece.setOnDragDetected(c -> {
            ArrayList<int[]> moveIndicators = proxy.onPawnDrag(p);
            if (moveIndicators == null) return;
            for (int[] moveIndicator : moveIndicators) {
                possibleMoveIndicators.get(String.valueOf(Utilities.int2Char(moveIndicator[0])) + moveIndicator[1]).setVisible(true);
            }
        });

        piece.setOnMouseDragged(event -> {
            if (chessEngine.getBoard().getWhiteTurn() != p.getIsWhite() || chessEngine.getBoard().getGameEnded()){
                resetToOriginalPosition(p, piece);
                return;
            }
            piece.setEffect(null);
            piece.setLayoutX(event.getSceneX() - mouseX);
            piece.setLayoutY(event.getSceneY() - mouseY);
        });

        piece.setOnMouseReleased(event -> {
            if (chessEngine.getBoard().getWhiteTurn() != p.getIsWhite() || chessEngine.getBoard().getGameEnded()) return;
            for (ImageView indicator : possibleMoveIndicators.values()) {
                indicator.setVisible(false);
            }
            int[] position = coordinatesToPosition((int) (event.getSceneX() - mouseX), (int) (event.getSceneY() - mouseY));
            ArrayList<Pioni> res = proxy.requestMove(p,position);
            if (res == null) {
                resetToOriginalPosition(p, piece);
                return;
            }
            for (Pioni pioni : res) {
                ImageView pieceImage = pieces.get(pioni);
//                for (Pioni pawn : pieces.keySet()){
//                    if (pawn.hashCode() == pioni.hashCode()) {
//                        pieceImage = pieces.get(pawn);
//                    }
//                }
                assert pieceImage != null;
                if (pioni.getCaptured()) pieceImage.setVisible(false);
                int[] newCoordinates = getCoordinates(pioni.getXPos(), pioni.getYPos());
                pieceImage.setLayoutX(newCoordinates[0] - piece.getFitWidth() / 2);
                pieceImage.setLayoutY(newCoordinates[1] - piece.getFitHeight() / 2);
            }
            switchTurnAnimation();
            playPiecePlacementSound();
            if (chessEngine.getBoard().getGameEnded()) showWinScreen();
            toggleTimer();
            updateCapturedPieces();
        });
        switchTurnAnimation();
        pieces.put(p, piece);
        root.getChildren().add(piece);
    }

    private void updateCapturedPieces() {
        ArrayList<Pioni> whites = chessEngine.getBoard().getPionia().stream()
                .filter(pioni -> pioni.getIsWhite() && pioni.getCaptured())
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Pioni> blacks = chessEngine.getBoard().getPionia().stream()
                .filter(pioni -> !pioni.getIsWhite() && pioni.getCaptured())
                .collect(Collectors.toCollection(ArrayList::new));

        for (int x = 0; x < 8; x++) {
            boolean isBlack = x >= 4;
            ArrayList<Pioni> targetList = isBlack ? blacks : whites;
            VBox targetBox = isBlack ? blackCapturedPawns : whiteCapturedPawns;

            if (targetList.isEmpty()) continue;

            int start = (x % 4) * 4;
            int end = Math.min(start + 4, targetList.size());

            if (start >= end) continue;

            ArrayList<String> imagePaths = targetList.subList(start, end).stream()
                    .map(Pioni::getImagePath)
                    .collect(Collectors.toCollection(ArrayList::new));

            HBox hbox;
            if (x % 4 < targetBox.getChildren().size()) {
                hbox = (HBox) targetBox.getChildren().get(x % 4);
                hbox.getChildren().clear();
            } else {
                hbox = new HBox(5);
                hbox.setAlignment(Pos.CENTER);
                targetBox.getChildren().add(hbox);
            }

            for (String path : imagePaths) {
                ImageView imageView = new ImageView(new Image(path));
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
                hbox.getChildren().add(imageView);
            }
        }
    }



    private void resetToOriginalPosition(Pioni p, ImageView piece) {
        int[] orig = getCoordinates(p.getXPos(), p.getYPos());
        piece.setLayoutX(orig[0] - piece.getFitWidth() / 2);
        piece.setLayoutY(orig[1] - piece.getFitHeight() / 2);
        piece.setEffect(p.getIsWhite() ? whiteTurnEffect : blackTurnEffect);
    }

    private int[] getCoordinates(char x, int y) {
        int[] coordinates = new int[2];
        coordinates[0] = offBoundsEnd + Math.abs(Utilities.char2Int(x) - 1) * tile + tile / 2;
        coordinates[1] = offBoundsEnd + Math.abs(y - 8) * tile + tile / 2;
        return coordinates;
    }

    private int[] coordinatesToPosition(int x, int y) {
        int[] position = new int[2];
        position[0] = Math.abs(x - offBoundsEnd) / tile + 1;
        position[1] = Math.abs(y - tile * 8) / tile + 1;
        return position;
    }

    private void showWinScreen() {
        String winnerText;
        String textColor;
        ChessEngine.Winner winner = chessEngine.getBoard().getWinner();

        if (winner == ChessEngine.Winner.Draw) {
            winnerText = "It's a Tie!";
            textColor = "gold";
        } else if (winner == ChessEngine.Winner.White) {
            winnerText = "White Wins!";
            textColor = "white";
        } else {
            winnerText = "Black Wins!";
            textColor = "black";
        }

        winnerLabel.setText(winnerText);
        winnerLabel.setStyle("-fx-text-fill: " + textColor + "; " +
                "-fx-font-size: 34px; " +
                "-fx-font-weight: bold; " +
                "-fx-alignment: center;");
        winnerLabel.setAlignment(Pos.CENTER);
        winnerLabel.setVisible(true);

        rightPanel.getChildren().clear();
        rightPanel.getChildren().addAll(winnerLabel, playAgain);

        playAgain.setVisible(true);
        Platform.runLater(() -> {
            winnerLabel.setLayoutX((rightPanel.getPrefWidth() - winnerLabel.getWidth()) / 2);
            winnerLabel.setLayoutY((rightPanel.getPrefHeight() - winnerLabel.getHeight()) / 2);
            playAgain.setLayoutX((rightPanel.getPrefWidth() - playAgain.getWidth()) / 2);
            playAgain.setLayoutY(winnerLabel.getLayoutY() + winnerLabel.getHeight() + 30);
        });
    }


    private CompletableFuture<String> selectUpgrade(boolean white) {
        CompletableFuture<String> selection = new CompletableFuture<>();

        Label promptLabel = new Label("Select an Option:");
        promptLabel.setFont(Font.font("Arial", 24));
        promptLabel.setTextFill(Color.WHITE);

        HBox optionsBox = new HBox(20);
        optionsBox.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(20, promptLabel, optionsBox);
        centerBox.setAlignment(Pos.CENTER);
        String backgroundColor = white ? "rgba(0, 0, 0, 0.7)" : "rgba(255, 255, 255, 0.8)";
        String borderColor = white ? "gold" : "black";
        centerBox.setStyle("-fx-background-color: " + backgroundColor + "; " +
                "-fx-padding: 20; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;");
        HashMap<String, String> imagePaths = new HashMap<>();
        chessEngine.getBoard().getPionia()
                .stream()
                .filter(pioni -> pioni.getIsWhite() == white)
                .forEach(pioni -> {
                    if (pioni.getType().equals("Alogo") || pioni.getType().equals("Pyrgos") || pioni.getType().equals("Stratigos") || pioni.getType().equals("Vasilissa")) {
                        imagePaths.put(pioni.getType(), pioni.getImagePath());
                    }
                });

        imagePaths.forEach((type, path) -> {
            Image image = new Image(path);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-cursor: hand;");

            imageView.setOnMouseClicked(event -> {
                selection.complete(type);
                root.getChildren().remove(centerBox);
            });

            optionsBox.getChildren().add(imageView);
        });

        root.getChildren().add(centerBox);

        Platform.runLater(() -> {
            centerBox.setLayoutX((root.getWidth() - centerBox.getWidth()) / 2);
            centerBox.setLayoutY((root.getHeight() - centerBox.getHeight()) / 2);
        });

        return selection;
    }

    private void switchTurnAnimation() {
        boolean turn = chessEngine.getBoard().getWhiteTurn();
        if (!turn) {
            for (Pioni p : pieces.keySet()) {
                if (!p.getIsWhite()) pieces.get(p).setEffect(blackTurnEffect);
                else pieces.get(p).setEffect(null);
            }
            return;
        }
        for (Pioni p : pieces.keySet()) {
            if (p.getIsWhite()) pieces.get(p).setEffect(whiteTurnEffect);
            else pieces.get(p).setEffect(null);
        }

    }

    public void toggleTimer() {
        if (whiteTimerRunning) {
            blackTimerStartTime = System.nanoTime();
            blackTimerRunning = true;
            whitePauseTime += System.nanoTime() - whiteTimerStartTime;
            whiteTimerRunning = false;
        } else {
            whiteTimerStartTime = System.nanoTime();
            whiteTimerRunning = true;
            blackPauseTime += System.nanoTime() - blackTimerStartTime;
            blackTimerRunning = false;
        }
    }
    public void updateAfterEnemyMove() {
        for (Pioni p : chessEngine.getBoard().getPionia()) {
            ImageView piece = pieces.get(p);
            if (piece == null) {
                addPiece(root, p);
            } else {
                int[] coordinates = getCoordinates(p.getXPos(), p.getYPos());
                piece.setLayoutX(coordinates[0] - piece.getFitWidth() / 2);
                piece.setLayoutY(coordinates[1] - piece.getFitHeight() / 2);
                piece.setVisible(!p.getCaptured());
            }
        }
        switchTurnAnimation();
        toggleTimer();
        updateCapturedPieces();
        playPiecePlacementSound();
        if (chessEngine.getBoard().getGameEnded()) showWinScreen();
    }
    public static void main(String[] args) {
        launch();
    }

    private void setKingCheckEffect(boolean white) {
        DropShadow ds = new DropShadow();
        ds.setColor(Color.RED);
        ds.setRadius(2);
        ds.setSpread(1);
        Pioni king = chessEngine.getBoard().getPionia().stream().filter(p -> p.getIsWhite() == white && p.getType().equals("Vasilias")).findFirst().orElse(null);
        if (king == null) return;
        pieces.get(king).setEffect(ds);
    }

    private void playPiecePlacementSound() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/piece placement.wav");
            if (inputStream == null) {
                return;
            }
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedInputStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    try {
                        audioStream.close();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
    public void setMode(boolean offlineMode){
        this.offlineMode = offlineMode;
    }
    public void setWebSocket(WebSocket socket){ this.webSocket = socket; }

//    private void loadingScreen() {
//        String videoPath = getClass().getResource("/startingScreen.mp4").toExternalForm();
//
//        // Create Media, MediaPlayer, and MediaView
//        Media media = new Media(videoPath);
//        MediaPlayer mediaPlayer = new MediaPlayer(media);
//        MediaView mediaView = new MediaView(mediaPlayer);
//
//        // Set the video to fit the window
//        mediaView.setPreserveRatio(false);
//        mediaView.fitWidthProperty().bind(stage.widthProperty());
//        mediaView.fitHeightProperty().bind(stage.heightProperty());
//
//        // Start the video playback in a loop
//        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
//        mediaPlayer.play();
//
//        // Add the MediaView to the scene
//        Pane root = new Pane(mediaView);
//        Scene scene = new Scene(root, 1000, 800);
//
//        stage.setTitle("JavaFX Video Background");
//        stage.setScene(scene);
//        stage.show();
//    }
}
