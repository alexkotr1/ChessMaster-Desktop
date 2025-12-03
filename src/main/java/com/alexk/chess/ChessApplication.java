package com.alexk.chess;

import com.alexk.chess.Pionia.Pioni;
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

import static com.alexk.chess.ChessEngine.Result;

/**
 * Main chess game application class implementing the graphical user interface.
 * <p>
 * This class extends JavaFX Application and provides the complete chess game interface
 * with piece dragging, move validation, timers, and game state management.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see Application
 * @see ChessEngine
 * @see MainMenu
 */
public class ChessApplication extends Application {

    /**
     * Mapping of chess pieces to their graphical ImageView representations.
     */
    private final HashMap<Pioni, ImageView> pieces = new HashMap<>();

    /**
     * Mouse X coordinate for drag operations.
     */
    private double mouseX;

    /**
     * Mouse Y coordinate for drag operations.
     */
    private double mouseY;

    /**
     * Visual effect for white player's turn.
     */
    private final DropShadow whiteTurnEffect = new DropShadow();

    /**
     * Visual effect for black player's turn.
     */
    private final DropShadow blackTurnEffect = new DropShadow();

    /**
     * The chess engine managing game logic and rules.
     */
    private ChessEngine chessEngine;

    /**
     * Mapping of position strings to move indicator ImageViews.
     */
    private final HashMap<String, ImageView> possibleMoveIndicators = new HashMap<>();

    /**
     * List of all possible board positions.
     */
    private final ArrayList<int[]> allPositions = new ArrayList<>();

    /**
     * Root container for the game interface.
     */
    private AnchorPane root;

    /**
     * Size of each chess board tile in pixels.
     */
    private final int tile = 90;

    /**
     * Offset from the edge of the board in pixels.
     */
    private final int offBoundsEnd = 40;

    /**
     * Container for displaying captured white pieces.
     */
    private VBox whiteCapturedPawns;

    /**
     * Container for displaying captured black pieces.
     */
    private VBox blackCapturedPawns;

    /**
     * Indicates whether the white player's timer is running.
     */
    private boolean whiteTimerRunning = false;

    /**
     * Start time for the white player's timer in nanoseconds.
     */
    private long whiteTimerStartTime;

    /**
     * Accumulated pause time for the white player's timer in nanoseconds.
     */
    private long whitePauseTime = 0;

    /**
     * Indicates whether the black player's timer is running.
     */
    private boolean blackTimerRunning = false;

    /**
     * Start time for the black player's timer in nanoseconds.
     */
    private long blackTimerStartTime = 0;

    /**
     * Accumulated pause time for the black player's timer in nanoseconds.
     */
    private long blackPauseTime = 0;

    /**
     * Total time per player in seconds.
     */
    private int totalTime = 600; // 10 minutes

    /**
     * Time offset for white player to adjust displayed time.
     */
    private long whiteTimeOffset = 0;

    /**
     * Time offset for black player to adjust displayed time.
     */
    private long blackTimeOffset = 0;

    /**
     * Label displaying the winner announcement.
     */
    private Label winnerLabel;

    /**
     * Button to return to the main menu.
     */
    private Button mainMenuButton;

    /**
     * Right panel containing game information and controls.
     */
    private AnchorPane rightPanel;

    /**
     * Reference to the primary stage.
     */
    private Stage stage;

    /**
     * Default constructor.
     */
    public ChessApplication() {}

    /**
     * Constructor with a pre-existing chess engine.
     *
     * @param chessEngine the chess engine to use for this game
     */
    public ChessApplication(ChessEngine chessEngine) {
        this.chessEngine = chessEngine;
    }

    /**
     * The main entry point for the JavaFX chess application.
     * <p>
     * Initializes the game interface, sets up the board, pieces, and timers.
     * </p>
     *
     * @param stage the primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        initialize();
    }

    /**
     * Initializes the game interface and components.
     */
    private void initialize() {
        root = new AnchorPane();
        rightPanel = new AnchorPane();

        // Setup background
        ImageView background = new ImageView(new Image("chessBoard.jpeg"));
        background.setFitWidth(800);
        background.setFitHeight(800);
        background.setLayoutX(0);
        background.setLayoutY(0);
        background.setPreserveRatio(false);

        // Setup turn indicators
        whiteTurnEffect.setColor(Color.web("#5BC0EB"));
        whiteTurnEffect.setRadius(1);
        whiteTurnEffect.setSpread(1.0);

        blackTurnEffect.setColor(Color.web("#FFD700"));
        blackTurnEffect.setRadius(1);
        blackTurnEffect.setSpread(1.0);

        // Setup right panel
        rightPanel.setPrefWidth(200);
        rightPanel.setPrefHeight(800);
        rightPanel.setLayoutX(800);
        rightPanel.setLayoutY(0);
        rightPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #F0D09F, #3F2C0E);");

        // Setup captured pieces containers
        setupCapturedPiecesContainers();

        // Setup winner label and main menu button
        setupGameControls();

        // Setup timers
        setupTimers();

        // Add components to root
        root.getChildren().addAll(background, rightPanel);

        // Initialize chess engine
        initializeChessEngine();

        // Add pieces to the board
        addPiecesToBoard();

        // Setup move indicators
        setupMoveIndicators();

        // Create and show scene
        Scene scene = new Scene(root, 1000, 800);
        stage.setTitle("Chess Game");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        // Start timers
        startTimers();
    }

    /**
     * Sets up the containers for captured pieces.
     */
    private void setupCapturedPiecesContainers() {
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
    }

    /**
     * Sets up game control elements (winner label, main menu button).
     */
    private void setupGameControls() {
        winnerLabel = new Label("");
        winnerLabel.setVisible(false);

        mainMenuButton = new Button("Main Menu");
        mainMenuButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        mainMenuButton.setStyle("-fx-background-color: linear-gradient(to bottom, #F0D09F, #3F2C0E);");
        mainMenuButton.setTextFill(Color.web("#F0D09F"));
        mainMenuButton.setVisible(false);
        mainMenuButton.setOnAction(event -> {
            MainMenu mainMenu = new MainMenu();
            mainMenu.start(stage);
        });
    }

    /**
     * Sets up the game timers.
     */
    private void setupTimers() {
        Label whiteTimerlabel = new Label();
        whiteTimerlabel.setText(formatTime(totalTime));
        whiteTimerlabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        whiteTimerlabel.setTextFill(Color.web("#F0D09F"));
        whiteTimerlabel.setStyle("-fx-background-color: #3F2C0E; " +
                "-fx-padding: 10; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-border-width: 2; " +
                "-fx-border-color: #F0D09F;");
        whiteTimerlabel.setLayoutX(50);
        whiteTimerlabel.setLayoutY(blackCapturedPawns.getLayoutY() - 50);

        // White timer animation
        AnimationTimer whiteTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (whiteTimerRunning) {
                    long elapsedTime = (now - whiteTimerStartTime + whitePauseTime) / 1_000_000_000L;
                    long remainingTime = totalTime - elapsedTime + whiteTimeOffset;
                    chessEngine.getBoard().setWhiteTimeRemaining(remainingTime);
                    if (remainingTime <= 0) {
                        chessEngine.setWinner(ChessEngine.Result.Black);
                        showWinScreen(chessEngine.getWinner());
                    }
                    long minutes = remainingTime / 60;
                    long seconds = remainingTime % 60;
                    whiteTimerlabel.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }
        };

        Label blackTimerLabel = new Label();
        blackTimerLabel.setText(formatTime(totalTime));
        blackTimerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        blackTimerLabel.setTextFill(Color.web("#F0D09F"));
        blackTimerLabel.setStyle("-fx-background-color: #3F2C0E; " +
                "-fx-padding: 10; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-border-width: 2; " +
                "-fx-border-color: #F0D09F;");
        blackTimerLabel.setLayoutX(50);
        blackTimerLabel.setLayoutY(whiteCapturedPawns.getLayoutY() + whiteCapturedPawns.getPrefHeight() + 10);

        // Black timer animation
        AnimationTimer blackTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (blackTimerRunning) {
                    long elapsedTime = (now - blackTimerStartTime + blackPauseTime) / 1_000_000_000L;
                    long remainingTime = totalTime - elapsedTime + blackTimeOffset;
                    chessEngine.getBoard().setBlackTimeRemaining(remainingTime);
                    if (remainingTime <= 0) {
                        chessEngine.setWinner(Result.White);
                        showWinScreen(chessEngine.getWinner());
                    }
                    long minutes = remainingTime / 60;
                    long seconds = remainingTime % 60;
                    blackTimerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }
        };

        blackTimer.start();
        whiteTimer.start();
        rightPanel.getChildren().addAll(whiteCapturedPawns, blackCapturedPawns, whiteTimerlabel, blackTimerLabel, winnerLabel);
    }

    /**
     * Initializes the chess engine.
     */
    private void initializeChessEngine() {
        if (chessEngine == null) {
            chessEngine = new ChessEngine(null);
            chessEngine.playChess();
        } else {
            setWhiteRemainingTime(chessEngine.getBoard().getWhiteTimeRemaining());
            setBlackRemainingTime(chessEngine.getBoard().getBlackTimeRemaining());
            if (!chessEngine.getWinner().equals(ChessEngine.Result.InProgress)) {
                System.out.println(chessEngine.getWinner());
                showWinScreen(chessEngine.getWinner());
            }
        }
    }

    /**
     * Adds all pieces to the game board.
     */
    private void addPiecesToBoard() {
        ArrayList<Pioni> Pionia = chessEngine.chessBoard.getPionia();
        for (Pioni p : Pionia) {
            addPiece(root, p);
        }
    }

    /**
     * Sets up move indicators for all board positions.
     */
    private void setupMoveIndicators() {
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
    }

    /**
     * Starts the game timers.
     */
    private void startTimers() {
        whiteTimerStartTime = System.nanoTime();
        whiteTimerRunning = true;
        blackTimerStartTime = System.nanoTime();
        blackTimerRunning = false;
    }

    /**
     * Adds a chess piece to the game board with event handlers.
     *
     * @param root the root container
     * @param p    the chess piece to add
     */
    private void addPiece(AnchorPane root, Pioni p) {
        int[] coordinates = getCoordinates(p.getXPos(), p.getYPos());
        ImageView piece = new ImageView(new Image(p.getImagePath()));
        piece.setFitWidth(60);
        piece.setFitHeight(60);
        piece.setLayoutX(coordinates[0] - piece.getFitWidth() / 2);
        piece.setLayoutY(coordinates[1] - piece.getFitHeight() / 2);
        piece.setPreserveRatio(false);

        // Mouse press handler
        piece.setOnMousePressed(event -> {
            mouseX = event.getSceneX() - piece.getLayoutX();
            mouseY = event.getSceneY() - piece.getLayoutY();
        });

        // Drag detection handler
        piece.setOnDragDetected(c -> {
            if (chessEngine.chessBoard.getWhiteTurn() != p.getIsWhite() ||
                    !chessEngine.getWinner().equals(Result.InProgress)) return;

            HashMap<Pioni, ArrayList<int[]>> legalMovesWhenKingThreatened = chessEngine.kingCheckMate(p.isWhite);
            if (legalMovesWhenKingThreatened != null && !legalMovesWhenKingThreatened.isEmpty()) {
                if (legalMovesWhenKingThreatened.get(p) == null) return;
                for (int[] dest : legalMovesWhenKingThreatened.get(p)) {
                    possibleMoveIndicators.get(String.valueOf(Utilities.int2Char(dest[0])) + dest[1]).setVisible(true);
                }
            } else {
                for (int[] dest : allPositions) {
                    char destX = Utilities.int2Char(dest[0]);
                    int destY = dest[1];
                    boolean res = p.isLegalMove(destX, destY);
                    if (res && !chessEngine.checkDumbMove(p, new int[]{Utilities.char2Int(destX), destY})) {
                        possibleMoveIndicators.get(String.valueOf(destX) + destY).setVisible(true);
                    }
                }
            }
        });

        // Mouse drag handler
        piece.setOnMouseDragged(event -> {
            if (chessEngine.chessBoard.getWhiteTurn() != p.getIsWhite() ||
                    !chessEngine.getWinner().equals(Result.InProgress)) return;
            piece.setEffect(null);
            piece.setLayoutX(event.getSceneX() - mouseX);
            piece.setLayoutY(event.getSceneY() - mouseY);
        });

        // Mouse release handler
        piece.setOnMouseReleased(event -> {
            for (ImageView indicator : possibleMoveIndicators.values()) {
                indicator.setVisible(false);
            }

            if (chessEngine.chessBoard.getWhiteTurn() != p.getIsWhite() ||
                    !chessEngine.getWinner().equals(Result.InProgress)) return;

            int[] position = coordinatesToPosition((int) (event.getSceneX() - mouseX), (int) (event.getSceneY() - mouseY));
            char posX = Utilities.int2Char(position[0]);
            int posY = position[1];

            HashMap<Pioni, ArrayList<int[]>> legalMovesWhenKingThreatened = chessEngine.kingCheckMate(p.isWhite);
            if (legalMovesWhenKingThreatened != null && !legalMovesWhenKingThreatened.isEmpty()) {
                ArrayList<int[]> desiredMoves = legalMovesWhenKingThreatened.get(p);
                if (desiredMoves != null && desiredMoves.stream().noneMatch(arr -> arr[0] == position[0] && arr[1] == posY)) {
                    resetToOriginalPosition(p, piece);
                    return;
                } else {
                    legalMovesWhenKingThreatened.clear();
                }
            }

            if (chessEngine.checkDumbMove(p, new int[]{position[0], position[1]})) {
                resetToOriginalPosition(p, piece);
                return;
            }

            Pioni pioniAtDest = chessEngine.chessBoard.getPioniAt(posX, posY);
            boolean res = chessEngine.nextMove(p.getXPos(), p.getYPos(), posX, posY);
            if (!res) {
                resetToOriginalPosition(p, piece);
                return;
            }

            toggleTimer();
            int[] newCoordinates = getCoordinates(posX, posY);

            // Handle castling
            if (p.type.equals("Vasilias") && pioniAtDest != null &&
                    pioniAtDest.type.equals("Pyrgos") && p.getIsWhite() == pioniAtDest.getIsWhite()) {
                newCoordinates = getCoordinates(p.getXPos(), p.getYPos());
                ImageView destPioniImageView = pieces.get(pioniAtDest);
                int[] destPioniCoordinates = getCoordinates(pioniAtDest.getXPos(), pioniAtDest.getYPos());
                destPioniImageView.setLayoutX(destPioniCoordinates[0] - destPioniImageView.getFitWidth() / 2);
                destPioniImageView.setLayoutY(destPioniCoordinates[1] - destPioniImageView.getFitHeight() / 2);
            }

            piece.setLayoutX(newCoordinates[0] - piece.getFitWidth() / 2);
            piece.setLayoutY(newCoordinates[1] - piece.getFitHeight() / 2);

            // Hide captured pieces
            for (Pioni pioni : pieces.keySet()) {
                if (pioni.getCaptured()) {
                    pieces.get(pioni).setVisible(false);
                }
            }

            updateCapturedPieces();

            // Handle pawn promotion
            if (p.type.equals("Stratiotis") &&
                    ((p.getIsWhite() && p.getYPos() == 8) || (!p.getIsWhite() && p.getYPos() == 1))) {
                selectUpgrade(p.getIsWhite()).thenAccept(selection -> {
                    Pioni upgraded = chessEngine.upgradePioni(p, selection);
                    if (upgraded != null) {
                        root.getChildren().remove(pieces.get(p));
                        addPiece(root, upgraded);
                    }
                    finishUp(p);
                });
            } else {
                finishUp(p);
            }
        });

        switchTurnAnimation(p.getIsWhite());
        pieces.put(p, piece);
        root.getChildren().add(piece);
    }

    /**
     * Updates the display of captured pieces.
     */
    private void updateCapturedPieces() {
        ArrayList<Pioni> whites = chessEngine.chessBoard.getPionia().stream()
                .filter(pioni -> pioni.getIsWhite() && pioni.getCaptured())
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Pioni> blacks = chessEngine.chessBoard.getPionia().stream()
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

    /**
     * Completes a move and checks for game-ending conditions.
     *
     * @param p the piece that was moved
     */
    private void finishUp(Pioni p) {
        switchTurnAnimation(p.getIsWhite());
        playPiecePlacementSound();

        // Check for checkmate or stalemate
        if (ChessEngine.checkKingMat(chessEngine.chessBoard, !p.getIsWhite())) {
            setKingCheckEffect(!p.getIsWhite());
            HashMap<Pioni, ArrayList<int[]>> legalMovesWhenEnemyKingThreatened =
                    chessEngine.kingCheckMate(!p.getIsWhite());

            if (legalMovesWhenEnemyKingThreatened == null || legalMovesWhenEnemyKingThreatened.isEmpty()) {
                showWinScreen(p.getIsWhite() ? Result.White : Result.Black);
                chessEngine.setWinner(p.getIsWhite() ? Result.White : Result.Black);
            }
        } else if (chessEngine.stalemateCheck(!p.getIsWhite()) ||
                chessEngine.chessBoard.getMovesRemaining() == 0) {
            showWinScreen(Result.Tie);
        }

        System.out.println(chessEngine.chessBoard.getMovesRemaining());
    }

    /**
     * Resets a piece to its original position.
     *
     * @param p     the piece to reset
     * @param piece the ImageView of the piece
     */
    private void resetToOriginalPosition(Pioni p, ImageView piece) {
        int[] orig = getCoordinates(p.getXPos(), p.getYPos());
        piece.setLayoutX(orig[0] - piece.getFitWidth() / 2);
        piece.setLayoutY(orig[1] - piece.getFitHeight() / 2);
        piece.setEffect(p.isWhite ? whiteTurnEffect : blackTurnEffect);
    }

    /**
     * Converts board coordinates to pixel coordinates.
     *
     * @param x the column (A-H)
     * @param y the row (1-8)
     * @return an array containing [x, y] pixel coordinates
     */
    private int[] getCoordinates(char x, int y) {
        int[] coordinates = new int[2];
        coordinates[0] = offBoundsEnd + Math.abs(Utilities.char2Int(x) - 1) * tile + tile / 2;
        coordinates[1] = offBoundsEnd + Math.abs(y - 8) * tile + tile / 2;
        return coordinates;
    }

    /**
     * Converts pixel coordinates to board coordinates.
     *
     * @param x the x pixel coordinate
     * @param y the y pixel coordinate
     * @return an array containing [column, row] board coordinates
     */
    private int[] coordinatesToPosition(int x, int y) {
        int[] position = new int[2];
        position[0] = Math.abs(x - offBoundsEnd) / tile + 1;
        position[1] = Math.abs(y - tile * 8) / tile + 1;
        return position;
    }

    /**
     * Displays the win screen with game results.
     *
     * @param winner the result of the game
     */
    private void showWinScreen(Result winner) {
        String winnerText;
        String textColor;

        if (winner.equals(Result.Tie)) {
            winnerText = "It's a Tie!";
            textColor = "gold";
        } else if (winner.equals(Result.White)) {
            winnerText = "White Wins!";
            textColor = "white";
        } else if (winner.equals(Result.Black)) {
            winnerText = "Black Wins!";
            textColor = "black";
        } else {
            winnerText = "Error!";
            textColor = "red";
        }

        winnerLabel.setText(winnerText);
        winnerLabel.setStyle("-fx-text-fill: " + textColor + "; " +
                "-fx-font-size: 34px; " +
                "-fx-font-weight: bold; " +
                "-fx-alignment: center;");
        winnerLabel.setAlignment(Pos.CENTER);
        winnerLabel.setVisible(true);

        // Get game statistics
        long whiteTime = chessEngine.getBoard().getWhiteTimeRemaining();
        long blackTime = chessEngine.getBoard().getBlackTimeRemaining();

        String whiteTimeStr = formatTime(whiteTime);
        String blackTimeStr = formatTime(blackTime);

        // Count captured pawns
        long whiteCapturedPawnsCount = chessEngine.chessBoard.getPionia().stream()
                .filter(p -> p.getIsWhite() && p.getCaptured() && "Stratiotis".equals(p.type))
                .count();

        long blackCapturedPawnsCount = chessEngine.chessBoard.getPionia().stream()
                .filter(p -> !p.getIsWhite() && p.getCaptured() && "Stratiotis".equals(p.type))
                .count();

        int whiteMoves = chessEngine.getBoard().getWhiteMoves();
        int blackMoves = chessEngine.getBoard().getBlackMoves();

        // Create statistics display
        Label statsTitle = new Label("Match Summary");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        statsTitle.setTextFill(Color.web("#F0D09F"));

        Label whiteHeader = new Label("White");
        whiteHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        whiteHeader.setTextFill(Color.web("#F0D09F"));

        Label whiteTimeLabel = new Label("Time Remaining: " + whiteTimeStr);
        Label whiteCapturedLabel = new Label("Captured pawns: " + whiteCapturedPawnsCount);
        Label whiteMovesLabel = new Label("Total moves: " + whiteMoves);

        Label blackHeader = new Label("Black");
        blackHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        blackHeader.setTextFill(Color.web("#F0D09F"));

        Label blackTimeLabel = new Label("Time Remaining: " + blackTimeStr);
        Label blackCapturedLabel = new Label("Captured pawns: " + blackCapturedPawnsCount);
        Label blackMovesLabel = new Label("Total moves: " + blackMoves);

        String statStyle = "-fx-text-fill: #F0D09F; -fx-font-size: 14px;";

        whiteTimeLabel.setStyle(statStyle);
        whiteCapturedLabel.setStyle(statStyle);
        whiteMovesLabel.setStyle(statStyle);

        blackTimeLabel.setStyle(statStyle);
        blackCapturedLabel.setStyle(statStyle);
        blackMovesLabel.setStyle(statStyle);

        VBox whiteStatsBox = new VBox(4, whiteHeader, whiteTimeLabel, whiteCapturedLabel, whiteMovesLabel);
        whiteStatsBox.setAlignment(Pos.TOP_LEFT);

        VBox blackStatsBox = new VBox(4, blackHeader, blackTimeLabel, blackCapturedLabel, blackMovesLabel);
        blackStatsBox.setAlignment(Pos.TOP_LEFT);

        VBox statsBox = new VBox(10, statsTitle, whiteStatsBox, blackStatsBox);
        statsBox.setAlignment(Pos.TOP_LEFT);
        statsBox.setStyle(
                "-fx-background-color: rgba(63,44,14,0.9); " +
                        "-fx-padding: 15; " +
                        "-fx-border-color: #F0D09F; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );

        // Clear and update right panel
        rightPanel.getChildren().clear();
        rightPanel.getChildren().addAll(winnerLabel, statsBox, mainMenuButton);

        mainMenuButton.setVisible(true);

        Platform.runLater(() -> {
            // Position winner label
            winnerLabel.setLayoutX((rightPanel.getPrefWidth() - winnerLabel.getWidth()) / 2);
            winnerLabel.setLayoutY(80);

            // Position statistics box
            statsBox.setLayoutX((rightPanel.getPrefWidth() - statsBox.getWidth()) / 2);
            statsBox.setLayoutY(winnerLabel.getLayoutY() + winnerLabel.getHeight() + 20);

            // Position main menu button
            mainMenuButton.setLayoutX((rightPanel.getPrefWidth() - mainMenuButton.getWidth()) / 2);
            mainMenuButton.setLayoutY(statsBox.getLayoutY() + statsBox.getHeight() + 30);
        });
    }

    /**
     * Formats time in seconds to MM:SS format.
     *
     * @param totalSeconds total time in seconds
     * @return formatted time string
     */
    private String formatTime(long totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Shows a dialog for pawn promotion selection.
     *
     * @param white true if promoting a white pawn, false for black
     * @return a CompletableFuture that completes with the selected piece type
     */
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

        // Get available piece types for promotion
        HashMap<String, String> imagePaths = new HashMap<>();
        chessEngine.chessBoard.getPionia()
                .stream()
                .filter(pioni -> pioni.getIsWhite() == white)
                .forEach(pioni -> {
                    if (pioni.type.equals("Alogo") || pioni.type.equals("Pyrgos") ||
                            pioni.type.equals("Stratigos") || pioni.type.equals("Vasilissa")) {
                        imagePaths.put(pioni.type, pioni.getImagePath());
                    }
                });

        // Create image buttons for each piece type
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

    /**
     * Updates visual effects to indicate whose turn it is.
     *
     * @param isWhite true if it's white's turn, false if black's
     */
    private void switchTurnAnimation(boolean isWhite) {
        if (isWhite) {
            for (Pioni p : pieces.keySet()) {
                if (!p.isWhite) pieces.get(p).setEffect(blackTurnEffect);
                else pieces.get(p).setEffect(null);
            }
            return;
        }
        for (Pioni p : pieces.keySet()) {
            if (p.isWhite) pieces.get(p).setEffect(whiteTurnEffect);
            else pieces.get(p).setEffect(null);
        }
    }

    /**
     * Toggles between white and black player timers.
     */
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

    /**
     * Sets the remaining time for the white player.
     *
     * @param seconds remaining time in seconds
     */
    public void setWhiteRemainingTime(long seconds) {
        long now = System.nanoTime();
        long elapsedTime = (whiteTimerStartTime == 0L && whitePauseTime == 0L)
                ? 0
                : (now - whiteTimerStartTime + whitePauseTime) / 1_000_000_000L;
        long baseRemaining = totalTime - elapsedTime;
        whiteTimeOffset = seconds - baseRemaining;

        if (chessEngine != null && chessEngine.getBoard() != null) {
            chessEngine.getBoard().setWhiteTimeRemaining(seconds);
        }
    }

    /**
     * Sets the remaining time for the black player.
     *
     * @param seconds remaining time in seconds
     */
    public void setBlackRemainingTime(long seconds) {
        long now = System.nanoTime();
        long elapsedTime = (blackTimerStartTime == 0L && blackPauseTime == 0L)
                ? 0
                : (now - blackTimerStartTime + blackPauseTime) / 1_000_000_000L;
        long baseRemaining = totalTime - elapsedTime;
        blackTimeOffset = seconds - baseRemaining;

        if (chessEngine != null && chessEngine.getBoard() != null) {
            chessEngine.getBoard().setBlackTimeRemaining(seconds);
        }
    }

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Applies a visual effect to indicate when a king is in check.
     *
     * @param white true if the white king is in check, false for black
     */
    private void setKingCheckEffect(boolean white) {
        DropShadow ds = new DropShadow();
        ds.setColor(Color.RED);
        ds.setRadius(2);
        ds.setSpread(1);
        Pioni king = chessEngine.chessBoard.getPionia().stream()
                .filter(p -> p.getIsWhite() == white && p.type.equals("Vasilias"))
                .findFirst().orElse(null);
        if (king == null) return;
        pieces.get(king).setEffect(ds);
    }

    /**
     * Plays a sound effect when a piece is placed.
     */
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
    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
}