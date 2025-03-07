package com.alexk.chess;

import com.alexk.chess.ChessEngine.ChessEngine;
import com.alexk.chess.Pionia.Pioni;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import javafx.util.Duration;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class ChessApplication extends Application implements WebSocketMessageListener {
    private final HashMap<Pioni, ImageView> pieces = new HashMap<>();
    private double mouseX;
    private double mouseY;
    private boolean vsAI = false;
    private final DropShadow whiteTurnEffect = new DropShadow();
    private final DropShadow blackTurnEffect = new DropShadow();
    private final HashMap<String, ImageView> possibleMoveIndicators = new HashMap<>();
    private AnchorPane root;
    private final int tile = 90;
    private final int offBoundsEnd = 40;
    private VBox whiteCapturedPawns;
    private VBox blackCapturedPawns;
    private int secondsAllowed;
    private Label winnerLabel;
    private Button playAgain;
    private AnchorPane rightPanel;
    private ImageView chessboard;
    private AnchorPane leftPanel;
    private TextArea chatArea;
    private Stage stage;
    private Proxy proxy;
    public ChessEngine chessEngine;
    private boolean offlineMode;
    private WebSocket webSocket;
    private boolean blackMode = false;
    private final int[] kingChecked = new int[]{0, 0};
    private Timer whiteTimer;
    private Timer blackTimer;
    public ChessApplication() {}


    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(_-> System.exit(0));
        initialize();
    }

    private void initialize() {
        if (!offlineMode) webSocket.setListener(this);
        root = new AnchorPane();
        leftPanel = new AnchorPane();
        rightPanel = new AnchorPane();
        proxy = new Proxy(offlineMode, webSocket);
        chessEngine = proxy.chessEngine;

        chessboard = new ImageView(new Image("chessBoard.jpeg"));
        chessboard.setFitWidth(800);
        chessboard.setFitHeight(800);
        chessboard.setLayoutX(vsAI ? 0 : 200);
        chessboard.setLayoutY(0);
        chessboard.setPreserveRatio(false);

        whiteTurnEffect.setColor(Color.web("#5BC0EB"));
        whiteTurnEffect.setRadius(1);
        whiteTurnEffect.setSpread(1.0);

        blackTurnEffect.setColor(Color.web("#FFD700"));
        blackTurnEffect.setRadius(1);
        blackTurnEffect.setSpread(1.0);

        rightPanel.setPrefWidth(200);
        rightPanel.setPrefHeight(800);
        rightPanel.setLayoutX(chessboard.getLayoutX() + chessboard.getFitWidth());
        rightPanel.setLayoutY(0);
        rightPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #F0D09F, #3F2C0E);");


        /* CHAT PANEL */
        if (!vsAI) {

            leftPanel.setPrefWidth(200);
            leftPanel.setPrefHeight(800);
            leftPanel.setLayoutX(0);
            leftPanel.setLayoutY(0);
            leftPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #F0D09F, #3F2C0E);");

            chatArea = new TextArea();
            chatArea.setEditable(false);
            chatArea.setWrapText(true);
            chatArea.setPrefSize(200, 950);
            chatArea.setStyle("-fx-background-color: transparent;"
                    + "-fx-control-inner-background: transparent;"
                    + "-fx-text-fill: white;"
                    + "-fx-border-color: transparent;"
                    + "-fx-background-radius: 0;"
                    + "-fx-background-insets: 0;"
                    + "-fx-focus-color: transparent;"
                    + "-fx-faint-focus-color: transparent;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 2, 0.5, 0, 1);"
                    + "-fx-font-weight: bold;");

            ScrollPane scrollPane = new ScrollPane(chatArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(200, 950);
            scrollPane.setStyle("-fx-background-color: transparent;"
                    + "-fx-border-color: transparent;"
                    + "-fx-background-insets: 0;"
                    + "-fx-padding: 0;");

            String scrollPaneCSS =
                    ".scroll-pane .viewport { -fx-background-color: transparent; }";
            scrollPane.getStylesheets().add("data:text/css," + scrollPaneCSS);

            TextField inputField = new TextField();
            inputField.setPrefSize(200, 50);
            inputField.setPromptText("Type a message...");
            inputField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2);"
                    + "-fx-text-fill: white;"
                    + "-fx-prompt-text-fill: rgba(255, 255, 255, 0.5);"
                    + "-fx-border-color: white;"
                    + "-fx-border-radius: 5px;"
                    + "-fx-background-radius: 5px;");

            inputField.setOnAction(e -> {
                String text = inputField.getText().trim();
                if (!text.isEmpty()) {
                    Message chatMessage = new Message();
                    chatMessage.setCode(RequestCodes.CHAT_MESSAGE);
                    chatMessage.setData(text);
                    chatMessage.send(webSocket);
                    inputField.clear();
                }
            });

            VBox chatContainer = new VBox(scrollPane, inputField);
            chatContainer.setSpacing(5);
            chatContainer.setPrefSize(200, 1000);
            chatContainer.setStyle("-fx-background-color: transparent;"
                    + "-fx-border-color: transparent;"
                    + "-fx-background-insets: 0;");

            leftPanel.getChildren().add(chatContainer);

            AnchorPane.setTopAnchor(chatContainer, 0.0);
            AnchorPane.setLeftAnchor(chatContainer, 0.0);
            AnchorPane.setRightAnchor(chatContainer, 0.0);
            AnchorPane.setBottomAnchor(chatContainer, 0.0);
        }

        /*-----------------------------------------*/

        /* CAPTURED PAWNS & TIMERS PANEL */

        whiteCapturedPawns = new VBox(10);
        whiteCapturedPawns.setPrefHeight(240);
        whiteCapturedPawns.setPrefWidth(170);
        whiteCapturedPawns.setLayoutX(15);
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
        playAgain.setOnAction(_ -> {
            if (!offlineMode) {
                Message msg = new Message();
                msg.setCode(RequestCodes.PLAY_AGAIN);
                msg.send(webSocket);
            } else initialize();

        });


        Label whiteTimerLabel = new Label();
        whiteTimerLabel.setText(String.format("%d:%02d", secondsAllowed / 60, secondsAllowed % 60));
        whiteTimerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        whiteTimerLabel.setTextFill(Color.web("#F0D09F"));
        whiteTimerLabel.setStyle("-fx-background-color: #3F2C0E; " +
                "-fx-padding: 10; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-border-width: 2; " +
                "-fx-border-color: #F0D09F;");
        whiteTimerLabel.setLayoutX(50);

        Label blackTimerLabel = new Label();
        blackTimerLabel.setText(String.format("%d:%02d", secondsAllowed / 60, secondsAllowed % 60));
        blackTimerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        blackTimerLabel.setTextFill(Color.web("#F0D09F"));
        blackTimerLabel.setStyle("-fx-background-color: #3F2C0E; " +
                "-fx-padding: 10; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-border-width: 2; " +
                "-fx-border-color: #F0D09F;");
        blackTimerLabel.setLayoutX(50);

        if (blackMode) {
            whiteCapturedPawns.setLayoutY(rightPanel.getPrefHeight() - blackCapturedPawns.getPrefHeight() - 30);
            blackCapturedPawns.setLayoutY(30);
            whiteTimerLabel.setLayoutY(blackCapturedPawns.getLayoutY() + blackCapturedPawns.getPrefHeight() + 10);
            blackTimerLabel.setLayoutY(whiteCapturedPawns.getLayoutY() - 50);
        } else {
            whiteCapturedPawns.setLayoutY(30);
            blackCapturedPawns.setLayoutY(rightPanel.getPrefHeight() - blackCapturedPawns.getPrefHeight() - 30);
            whiteTimerLabel.setLayoutY(blackCapturedPawns.getLayoutY() - 50);
            blackTimerLabel.setLayoutY(whiteCapturedPawns.getLayoutY() + whiteCapturedPawns.getPrefHeight() + 10);
        }
        whiteTimer = new Timer(1000, secondsAllowed * 1000L) {
            @Override
            protected void onTick() {
                Platform.runLater(()->updateTimerLabel(whiteTimerLabel, (int) (getRemainingTime() / 1000)));
            }

            @Override
            protected void onFinish() {
                Platform.runLater(()->{
                    chessEngine.getBoard().setGameEndedWinner(true, ChessEngine.Winner.Black);
                    showWinScreen();
                });
            }
        };
        blackTimer = new Timer(1000, secondsAllowed * 1000L) {
            @Override
            protected void onTick() {
                Platform.runLater(()->updateTimerLabel(blackTimerLabel, (int) (getRemainingTime() / 1000)));
            }

            @Override
            protected void onFinish() {
                Platform.runLater(()->{
                    chessEngine.getBoard().setGameEndedWinner(true, ChessEngine.Winner.White);
                    showWinScreen();
                });
            }
        };

        rightPanel.getChildren().addAll(whiteCapturedPawns, blackCapturedPawns, whiteTimerLabel, blackTimerLabel, winnerLabel);

        /* ------------------------------------------------------ */

        root.getChildren().addAll(chessboard, leftPanel, rightPanel);
        ArrayList<Pioni> Pionia = chessEngine.getBoard().getPionia();
        for (Pioni p : Pionia) {
            addPiece(root, p);
        }

        Scene scene = new Scene(root, vsAI ? 1000 : 1200, 800);
        stage.setTitle("Chess Master");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
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
        whiteTimer.start();
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
            if (!offlineMode && chessEngine.getBoard().getWhiteTurn() == blackMode) return;
            ArrayList<int[]> moveIndicators = proxy.onPawnDrag(p);
            if (moveIndicators == null) return;
            for (int[] moveIndicator : moveIndicators) {
                String str = String.valueOf(Utilities.int2Char(moveIndicator[0])) + moveIndicator[1];
                possibleMoveIndicators.get(str).setVisible(true);
                possibleMoveIndicators.get(str).toFront();
            }
        });

        piece.setOnMouseDragged(event -> {
            if ((!offlineMode && chessEngine.getBoard().getWhiteTurn() == blackMode) || chessEngine.getBoard().getWhiteTurn() != p.getIsWhite() || chessEngine.getBoard().getGameEnded()) {
                resetToOriginalPosition(p, piece);
                return;
            }
            piece.setEffect(null);
            piece.setLayoutX(event.getSceneX() - mouseX);
            piece.setLayoutY(event.getSceneY() - mouseY);
        });

        piece.setOnMouseReleased(event -> {
            System.out.println(chessEngine.toFen());
            if ((!offlineMode && chessEngine.getBoard().getWhiteTurn() == blackMode) || chessEngine.getBoard().getWhiteTurn() != p.getIsWhite() || chessEngine.getBoard().getGameEnded())
                return;
            for (ImageView indicator : possibleMoveIndicators.values()) {
                indicator.setVisible(false);
            }
            int[] position = coordinatesToPosition((int) (event.getSceneX() - mouseX), (int) (event.getSceneY() - mouseY));
            ArrayList<Pioni> res = proxy.requestMove(p, position);
            if (res == null) {
                resetToOriginalPosition(p, piece);
                return;
            }
            for (Pioni pioni : res) {
                ImageView pieceImage = pieces.get(pioni);
                if (pioni.getCaptured()) pieceImage.setVisible(false);
                int[] newCoordinates = getCoordinates(pioni.getXPos(), pioni.getYPos());
                pieceImage.setLayoutX(newCoordinates[0] - piece.getFitWidth() / 2);
                pieceImage.setLayoutY(newCoordinates[1] - piece.getFitHeight() / 2);
            }
            if (p.getType().equals("Stratiotis") && ((p.getIsWhite() && p.getYPos() == 8) || (!p.getIsWhite() && p.getYPos() == 1)) && offlineMode) {
                selectUpgrade(p.getIsWhite()).thenAccept(str -> {
                    Message msg = new Message();
                    msg.setCode(RequestCodes.REQUEST_UPGRADE);
                    int[] selection = new int[3];
                    selection[0] = p.getPosition()[0];
                    selection[1] = p.getPosition()[1];
                    switch (str) {
                        case "Pyrgos" -> selection[2] = 1;
                        case "Stratigos" -> selection[2] = 2;
                        case "Vasilissa" -> selection[2] = 3;
                    }
                    if (!offlineMode){
                        msg.setData(selection);
                        msg.send(webSocket);
                        msg.onReply(_ -> {
                            Platform.runLater(() -> {
                                root.getChildren().remove(pieces.get(p));
                                pieces.remove(p);
                            });
                            chessEngine.refreshBoard(() -> updateAfterEnemyMove(false));
                        });
                    } else {
                        Pioni upgraded = chessEngine.upgradePioni(p,str);
                        if (upgraded != null) {
                            root.getChildren().remove(pieces.get(p));
                            pieces.remove(p);
                            addPiece(root, upgraded);
                        }
                    }
                });
            }
            switchTurnAnimation();
            playPiecePlacementSound();
            if (chessEngine.getBoard().getGameEnded()) showWinScreen();
            toggleTimer();
            updateCapturedPieces();
        });
        switchTurnAnimation();
        pieces.put(p, piece);
        Platform.runLater(() -> root.getChildren().add(piece));
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
                Platform.runLater(() -> hbox.getChildren().clear());
            } else {
                hbox = new HBox(5);
                hbox.setAlignment(Pos.CENTER);
                Platform.runLater(() -> targetBox.getChildren().add(hbox));
            }

            for (String path : imagePaths) {
                ImageView imageView = new ImageView(new Image(path));
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
                Platform.runLater(() -> hbox.getChildren().add(imageView));
            }
        }
    }


    private void resetToOriginalPosition(Pioni p, ImageView piece) {
        int[] orig = getCoordinates(p.getXPos(), p.getYPos());
        piece.setLayoutX(orig[0] - piece.getFitWidth() / 2);
        piece.setLayoutY(orig[1] - piece.getFitHeight() / 2);
        if (offlineMode) piece.setEffect(p.getIsWhite() ? whiteTurnEffect : blackTurnEffect);
        else piece.setEffect(p.getIsWhite() ? (blackMode ? null : whiteTurnEffect) : blackMode ? blackTurnEffect : null);

        if (p.getType().equals("Vasilias")) setKingCheckEffect(p.getIsWhite(), (p.getIsWhite() ? kingChecked[0] : kingChecked[1]) == 1);
    }

    private int[] getCoordinates(char x, int y) {
        int[] pos = blackMode ? chessEngine.getBoard().translateToBlackView(Utilities.char2Int(x), y) : new int[]{Utilities.char2Int(x), y};
        int[] coordinates = new int[2];
        coordinates[0] = (int) chessboard.getLayoutX() + offBoundsEnd + Math.abs(pos[0] - 1) * tile + tile / 2;
        coordinates[1] = offBoundsEnd + Math.abs(pos[1] - 8) * tile + tile / 2;
        return coordinates;
    }

    private int[] coordinatesToPosition(int x, int y) {
        int[] position = new int[2];
        position[0] = Math.abs(x - offBoundsEnd - (int) chessboard.getLayoutX()) / tile + 1;
        position[1] = Math.abs(y - tile * 8) / tile + 1;
        return blackMode ? chessEngine.getBoard().translateToBlackView(position[0], position[1]) : position;
    }

    private void showWinScreen() {
        ChessEngine.Winner winner = chessEngine.getBoard().getWinner();
        String winnerText;
        String textColor;

        if (winner == ChessEngine.Winner.Draw) {
            winnerText = "It's a Tie!";
            textColor = "gold";
        } else if (winner == ChessEngine.Winner.White) {
            winnerText = "White Wins!";
            textColor = "white";
        } else if (winner == ChessEngine.Winner.Black) {
            winnerText = "Black Wins!";
            textColor = "black";
        } else {
            System.err.println("Error: Unknown winner!");
            winnerText = "Error: Unknown winner!";
            textColor = "red";
        }

        Platform.runLater(() -> {
            winnerLabel.setText(winnerText);
            winnerLabel.setStyle("-fx-text-fill: " + textColor + "; " +
                    "-fx-font-size: 34px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-alignment: center;");
            winnerLabel.setAlignment(Pos.CENTER);
            winnerLabel.setVisible(true);

            playAgain.setVisible(true);

            rightPanel.getChildren().clear();
            rightPanel.getChildren().addAll(winnerLabel, playAgain);

            double panelWidth = rightPanel.getPrefWidth();
            double panelHeight = rightPanel.getPrefHeight();

            double winnerLabelY = panelHeight / 3;
            double playAgainY = winnerLabelY + 70;

            winnerLabel.setLayoutX((panelWidth - 195) / 2);
            winnerLabel.setLayoutY(winnerLabelY);
            playAgain.setLayoutX((panelWidth - 140) / 2);
            playAgain.setLayoutY(playAgainY);
        });
    }

    public CompletableFuture<String> selectUpgrade(boolean white) {
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
                if (!offlineMode){
                    if (!p.getIsWhite()) pieces.get(p).setEffect(blackMode ? blackTurnEffect : null);
                    else pieces.get(p).setEffect(null);
                } else {
                    if (!p.getIsWhite()) pieces.get(p).setEffect(blackTurnEffect);
                    else pieces.get(p).setEffect(null);
                }
            }
            setKingCheckEffect(false, kingChecked[1] == 1);
            return;
        }
        for (Pioni p : pieces.keySet()) {
            if (!offlineMode){
                if (p.getIsWhite()) pieces.get(p).setEffect(blackMode ? null : whiteTurnEffect);
                else pieces.get(p).setEffect(null);
            } else {
                if (p.getIsWhite()) pieces.get(p).setEffect(whiteTurnEffect);
                else pieces.get(p).setEffect(null);
            }
        }
        setKingCheckEffect(true, kingChecked[0] == 1);

    }


    public void updateAfterEnemyMove(boolean shouldSwitch) {
        ArrayList<Pioni> currentPionia = chessEngine.getBoard().getPionia();
        ArrayList<Pioni> toRemove = new ArrayList<>();
        for (Pioni p : pieces.keySet()) {
            if (!currentPionia.contains(p)) {
                toRemove.add(p);
            }
        }
        for (Pioni p : toRemove) {
            ImageView pieceImage = pieces.get(p);
            if (pieceImage != null) {
                Platform.runLater(() -> root.getChildren().remove(pieceImage));
            }
            pieces.remove(p);
        }

        for (Pioni p : currentPionia) {
            ImageView piece = pieces.get(p);
            if (piece == null) {
                addPiece(root, p);
            } else {
                int[] coordinates = getCoordinates(p.getXPos(), p.getYPos());
                double currentX = piece.getLayoutX();
                double currentY = piece.getLayoutY();
                double nextX = coordinates[0] - piece.getFitWidth() / 2;
                double nextY = coordinates[1] - piece.getFitHeight() / 2;
                if (Math.abs(currentX - nextX) > 40 || Math.abs(currentY - nextY) > 40) {
                    TranslateTransition pieceTransition = new TranslateTransition();
                    pieceTransition.setNode(piece);
                    int[] origPos = coordinatesToPosition((int) currentX, (int) currentY);
                    int[] nextPos = coordinatesToPosition((int) nextX, (int) nextY);
                    int duration = 300 * (origPos[1] != p.getYPos() ? Math.abs(origPos[1] - nextPos[1]) : Math.abs(origPos[0] - nextPos[0]));
                    pieceTransition.setDuration(new Duration(duration));
                    pieceTransition.setByX(nextX - currentX);
                    pieceTransition.setByY(nextY - currentY);
                    pieceTransition.setCycleCount(1);
                    pieceTransition.setAutoReverse(false);
                    pieceTransition.setOnFinished(event -> {
                        if (shouldSwitch) {
                            playPiecePlacementSound();
                            switchTurnAnimation();
                        }
                        piece.setLayoutX(nextX);
                        piece.setLayoutY(nextY);
                        piece.setTranslateX(0);
                        piece.setTranslateY(0);
                        root.setCacheHint(CacheHint.QUALITY);
                    });
                    root.setCacheHint(CacheHint.SPEED);
                    piece.setEffect(null);
                    pieceTransition.play();
                }

                piece.setVisible(!p.getCaptured());
            }
        }
        updateCapturedPieces();
        if (shouldSwitch) toggleTimer();
        if (chessEngine.getBoard().getGameEnded()) showWinScreen();
    }

    public static void main(String[] args) {
        launch();
    }

    private void setKingCheckEffect(boolean white, boolean isChecked) {
        Pioni king = chessEngine.getBoard().getPionia().stream().filter(p -> p.getIsWhite() == white && p.getType().equals("Vasilias")).findFirst().orElse(null);
        if (!isChecked) return;
        DropShadow ds = new DropShadow();
        ds.setColor(Color.RED);
        ds.setRadius(2);
        ds.setSpread(1);
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

    public void setMode(boolean offlineMode, boolean whiteMode) {
        this.offlineMode = offlineMode;
        this.blackMode = !whiteMode;
    }

    public void setWebSocket(WebSocket socket) {
        this.webSocket = socket;
    }

    public void setMinutesAllowed(int mins) {
        secondsAllowed = mins * 60;
    }
    public void setVsAI(boolean vsAI) {
        this.vsAI = vsAI;
    }

    private void updateTimerLabel(Label label, int remainingTime) {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));
    }


    private void toggleTimer() {
        if (chessEngine.getBoard().getWhiteTurn()){
            whiteTimer.resume();
            blackTimer.pause();
        } else {
            whiteTimer.pause();
            blackTimer.resume();
        }

    }

    private void overrideTimersFromServer(long whiteTime, long blackTime) {
        if (whiteTimer != null) whiteTimer.setRemainingTime(whiteTime);
        if (blackTimer != null) blackTimer.setRemainingTime(blackTime);
    }

    @Override
    public void onMessageReceived(Message message) {
        switch (message.getCode()){
            case RequestCodes.ENEMY_MOVE -> chessEngine.refreshBoard(() -> updateAfterEnemyMove(Boolean.parseBoolean(message.getData())));
            case RequestCodes.KING_CHECK_BLACK -> {
                boolean isChecked = Boolean.parseBoolean(message.getData());
                kingChecked[1] = isChecked ? 1 : 0;
                Platform.runLater(() -> setKingCheckEffect(false, isChecked));
            }
            case RequestCodes.KING_CHECK_WHITE -> {
                boolean isChecked = Boolean.parseBoolean(message.getData());
                kingChecked[0] = isChecked ? 1 : 0;
                Platform.runLater(() -> setKingCheckEffect(true, isChecked));
            }
            case RequestCodes.PLAY_AGAIN_ACCEPTED -> {
                Platform.runLater(() -> {
                    try {
                        Stage newStage = new Stage();
                        stage.close();
                        ChessApplication chessApp = new ChessApplication();
                        chessApp.setMode(false, !blackMode);
                        chessApp.setWebSocket(webSocket);
                        chessApp.setVsAI(vsAI);
                        chessApp.setMinutesAllowed(secondsAllowed / 60);
                        chessApp.start(newStage);
                    } catch (Exception e) {
                        System.err.println("Error restarting application: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
            case RequestCodes.TIMER -> {
                try {
                    long[] times = Message.mapper.readValue(message.getData(), long[].class);
                    overrideTimersFromServer(times[0], times[1]);
                } catch (JsonProcessingException e) {
                    System.err.println("Failed to parse TIMER message: " + e.getMessage());
                }
            }
            case RequestCodes.REQUEST_UPGRADE -> {
                boolean forWhite = Boolean.parseBoolean(message.getData());
                Platform.runLater(()->selectUpgrade(forWhite).thenAccept(str->{
                    Message reply = new Message();
                    reply.setCode(RequestCodes.REQUEST_UPGRADE_RESULT);
                    reply.setData(str);
                    reply.send(webSocket,message);
                }));
            }
            case RequestCodes.CHAT_MESSAGE_NOTIFICATION -> {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map<String, Object> data = mapper.readValue(message.getData(),Map.class);
                    chatArea.appendText(data.get("sender") + ": " + data.get("text") + "\n");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
