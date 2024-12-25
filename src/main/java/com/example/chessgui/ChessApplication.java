package com.example.chessgui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
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


public class ChessApplication extends Application {
    private final HashMap<Pioni, ImageView> pieces = new HashMap<>();
    private double mouseX;
    private double mouseY;
    private final DropShadow whiteTurnEffect = new DropShadow();
    private final DropShadow blackTurnEffect = new DropShadow();
    private ChessEngine chessEngine;
    private final HashMap<String,ImageView> possibleMoveIndicators = new HashMap<>();
    private final ArrayList<int[]> allPositions = new ArrayList<>();
    private AnchorPane root;
    private final int tile = 90;
    private final int offBoundsEnd = 40;
    public ChessApplication() {}

    @Override
    public void start(Stage stage) {
        AnchorPane root = new AnchorPane();
        this.root = root;
        ImageView background = new ImageView(new Image("chessBoard.jpeg"));
        background.setFitWidth(800);
        background.setFitHeight(800);
        background.setLayoutX(0);
        background.setLayoutY(0);
        background.setPreserveRatio(false);

        ImageView background2 = new ImageView(new Image("background.png"));
        background2.setFitWidth(200);
        background2.setFitHeight(800);
        background2.setLayoutX(800);
        background2.setLayoutY(0);
        background2.setPreserveRatio(false);

        whiteTurnEffect.setColor(Color.web("#5BC0EB"));
        whiteTurnEffect.setRadius(1);
        whiteTurnEffect.setSpread(1.0);

        blackTurnEffect.setColor(Color.web("#FFD700"));
        blackTurnEffect.setRadius(1);
        blackTurnEffect.setSpread(1.0);


        root.getChildren().addAll(background,background2);
        chessEngine = new ChessEngine();
        chessEngine.playChess();
        ArrayList<Pioni> Pionia = chessEngine.chessBoard.getPionia();
        for (Pioni p : Pionia) {
            addPiece(root, p);
        }
        Scene scene = new Scene(root, 1000, 800);
        stage.setTitle("Chess Game");
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        for (int x = 1;x<=8;x++){
            for (int y = 1;y<=8;y++){
                allPositions.add(new int[]{x,y});
                ImageView indicator = new ImageView(new Image("marker.png"));
                int[] coordinates = getCoordinates(Utilities.int2Char(x),y);
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
            if (chessEngine.chessBoard.getWhiteTurn() != p.getIsWhite() || chessEngine.getGameEnded()) return;
            HashMap<Pioni,ArrayList<int[]>> legalMovesWhenKingThreatened = chessEngine.kingCheckMate(p.isWhite);
            if (legalMovesWhenKingThreatened != null && !legalMovesWhenKingThreatened.isEmpty()){
                if (legalMovesWhenKingThreatened.get(p) == null) return;
                for (int[] dest : legalMovesWhenKingThreatened.get(p)) {
                    possibleMoveIndicators.get(String.valueOf(Utilities.int2Char(dest[0])) + dest[1]).setVisible(true);
                }
            } else {
                for (int[] dest : allPositions) {
                    char destX = Utilities.int2Char(dest[0]);
                    int  destY = dest[1];
                    boolean res = p.isLegalMove(destX, destY);
                    if (res && !chessEngine.checkDumbMove(p,new int[]{ Utilities.char2Int(destX),destY})) possibleMoveIndicators.get(String.valueOf(destX) + destY).setVisible(true);
                }
            }
        });

        piece.setOnMouseDragged(event -> {
            if (chessEngine.chessBoard.getWhiteTurn() != p.getIsWhite() || chessEngine.getGameEnded()) return;
            piece.setEffect(null);
            piece.setLayoutX(event.getSceneX() - mouseX);
            piece.setLayoutY(event.getSceneY() - mouseY);
        });

        piece.setOnMouseReleased(event -> {
            for (ImageView indicator : possibleMoveIndicators.values()) {
                indicator.setVisible(false);
            }
            if (chessEngine.chessBoard.getWhiteTurn() != p.getIsWhite()) return;
            int[] position = coordinatesToPosition((int) (event.getSceneX() - mouseX), (int) (event.getSceneY() - mouseY));
            char posX = Utilities.int2Char(position[0]);
            int posY = position[1];
            HashMap<Pioni,ArrayList<int[]>> legalMovesWhenKingThreatened = chessEngine.kingCheckMate(p.isWhite);
            if (legalMovesWhenKingThreatened != null && !legalMovesWhenKingThreatened.isEmpty()){
                ArrayList<int[]> desiredMoves = legalMovesWhenKingThreatened.get(p);
                if (desiredMoves != null && desiredMoves.stream().noneMatch(arr->arr[0] == position[0] && arr[1] == posY)) {
                    resetToOriginalPosition(p, piece);
                } else legalMovesWhenKingThreatened.clear();
            }
            if (chessEngine.checkDumbMove(p,new int[]{position[0],position[1]})){
                resetToOriginalPosition(p, piece);
                return;
            }
            Pioni pioniAtDest = chessEngine.chessBoard.getPioniAt(posX, posY);
            boolean res = chessEngine.nextMove(p.getXPos(), p.getYPos(), posX, posY);
            if (!res) {
                resetToOriginalPosition(p, piece);
                return;
            }
            int[] newCoordinates = getCoordinates(posX, posY);
            if (p.type.equals("Vasilias") && pioniAtDest != null && pioniAtDest.type.equals("Pyrgos") && p.getIsWhite() == pioniAtDest.getIsWhite()){
                newCoordinates = getCoordinates(p.getXPos(), p.getYPos());
                ImageView destPioniImageView = pieces.get(pioniAtDest);
                int[] destPioniCoordinates = getCoordinates(pioniAtDest.getXPos(), pioniAtDest.getYPos());
                destPioniImageView.setLayoutX(destPioniCoordinates[0] - destPioniImageView.getFitWidth() / 2);
                destPioniImageView.setLayoutY(destPioniCoordinates[1] - destPioniImageView.getFitHeight() / 2);
                piece.setLayoutX(newCoordinates[0] - piece.getFitWidth() / 2);
                piece.setLayoutY(newCoordinates[1] - piece.getFitHeight() / 2);
            } else {
                piece.setLayoutX(newCoordinates[0] - piece.getFitWidth() / 2);
                piece.setLayoutY(newCoordinates[1] - piece.getFitHeight() / 2);
            }
            for (Pioni pioni : pieces.keySet()) {
                if (pioni.getCaptured()) pieces.get(pioni).setVisible(false);
            }
            if (p.type.equals("Stratiotis") && ((p.getIsWhite() && p.getYPos() == 8) || (!p.getIsWhite() && p.getYPos() == 1))) {
                selectUpgrade(p.getIsWhite()).thenAccept(selection ->{
                    Pioni upgraded = chessEngine.upgradePioni(p,selection);
                    if (upgraded != null) {
                        root.getChildren().remove(pieces.get(p));
                        addPiece(root, upgraded);
                    }
                });
            }
            switchTurnAnimation(p.getIsWhite());
            playPiecePlacementSound();
            if (ChessEngine.checkKingMat(chessEngine.chessBoard,!p.getIsWhite())){
                setKingCheckEffect(!p.getIsWhite());
                HashMap<Pioni,ArrayList<int[]>> legalMovesWhenEnemyKingThreatened = chessEngine.kingCheckMate(!p.getIsWhite());
                if (legalMovesWhenEnemyKingThreatened == null || legalMovesWhenEnemyKingThreatened.isEmpty()) {
                    showWinScreen(p.getIsWhite());
                    chessEngine.setGameEnded(true);
                }
            } else if (chessEngine.stalemateCheck(!p.getIsWhite())) showWinScreen(null);
        });
        switchTurnAnimation(p.getIsWhite());
        pieces.put(p, piece);
        root.getChildren().add(piece);
    }

    private void resetToOriginalPosition(Pioni p, ImageView piece) {
        int[] orig = getCoordinates(p.getXPos(), p.getYPos());
        piece.setLayoutX(orig[0] - piece.getFitWidth() / 2);
        piece.setLayoutY(orig[1] - piece.getFitHeight() / 2);
        piece.setEffect(p.isWhite ? whiteTurnEffect : blackTurnEffect);
    }

    private int[] getCoordinates(char x, int y) {
        int[] coordinates = new int[2];
        coordinates[0] = offBoundsEnd + Math.abs(Utilities.char2Int(x) - 1) * tile + tile / 2;
        coordinates[1] = offBoundsEnd + Math.abs(y - 8) * tile + tile / 2;
        return coordinates;
    }
    private int[] coordinatesToPosition(int x, int y) {
        int[] position = new int[2];
        position[0] = Math.abs(x-offBoundsEnd) / tile + 1;
        position[1] = Math.abs(y - tile*8) / tile + 1;
        return position;
    }
    private void showWinScreen(Boolean winnerIsWhite) {
        Pane winScreen = new Pane();
        winScreen.setPrefSize(root.getWidth(), root.getHeight());

        String backgroundColor = winnerIsWhite == null ? "rgba(128, 128, 128, 0.8)" : "rgba(32, 32, 32, 0.8)";
        winScreen.setStyle("-fx-background-color: " + backgroundColor + ";");

        Color value = winnerIsWhite == null ? Color.GOLD : Color.WHITE;

        Rectangle border = new Rectangle(600, 400);
        border.setArcWidth(20);
        border.setArcHeight(20);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(value);
        border.setStrokeWidth(5);
        border.setLayoutX((winScreen.getPrefWidth() - border.getWidth()) / 2);
        border.setLayoutY((winScreen.getPrefHeight() - border.getHeight()) / 2);

        Label winnerLabel = new Label();
        winnerLabel.setFont(Font.font("Arial", 60));
        winnerLabel.setTextFill(value);
        winnerLabel.setText(winnerIsWhite == null ? "It's a Tie!" : (winnerIsWhite ? "White Wins!" : "Black Wins!"));
        winnerLabel.setLayoutX((winScreen.getPrefWidth() - winnerLabel.getWidth()) / 2);
        winnerLabel.setLayoutY(border.getLayoutY() + (border.getHeight() - winnerLabel.getHeight()) / 6);

        Image kingIcon = new Image(winnerIsWhite != null && winnerIsWhite ? "white king.png" : "black king.png");
        ImageView kingIconView = new ImageView(kingIcon);
        kingIconView.setFitWidth(100);
        kingIconView.setFitHeight(100);
        kingIconView.setPreserveRatio(true);
        kingIconView.setLayoutX(border.getLayoutX() + (border.getWidth() - kingIconView.getFitWidth()) / 2);
        kingIconView.setLayoutY(border.getLayoutY() + (border.getHeight() - kingIconView.getFitHeight()) / 1.5);

        winScreen.getChildren().addAll(border, kingIconView, winnerLabel);
        
        root.getChildren().add(winScreen);
        
        Platform.runLater(() -> {
            double centerX = (winScreen.getWidth() - winnerLabel.getWidth()) / 2;
            winnerLabel.setLayoutX(centerX);
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
        chessEngine.chessBoard.getPionia()
                .stream()
                .filter(pioni -> pioni.getIsWhite() == white)
                .forEach(pioni -> {
                    if (pioni.type.equals("Alogo") || pioni.type.equals("Pyrgos") || pioni.type.equals("Stratigos") || pioni.type.equals("Vasilissa")) {
                        imagePaths.put(pioni.type, pioni.getImagePath());
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

    public static void main(String[] args) {
        launch();
    }
    private void setKingCheckEffect(boolean white){
        DropShadow ds = new DropShadow();
        ds.setColor(Color.RED);
        ds.setRadius(2);
        ds.setSpread(1);
        Pioni king = chessEngine.chessBoard.getPionia().stream().filter(p->p.getIsWhite() == white && p.type.equals("Vasilias")).findFirst().orElse(null);
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
}
