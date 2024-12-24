package com.example.chessgui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;

import java.util.*;


public class ChessApplication extends Application {
    private HashMap<Pioni, ImageView> pieces = new HashMap<>();
    private double mouseX;
    private double mouseY;
    private final DropShadow whiteTurnEffect = new DropShadow();
    private final DropShadow blackTurnEffect = new DropShadow();
    private ChessEngine chessEngine;
    HashMap<String,ImageView> possibleMoveIndicators = new HashMap<>();
    ArrayList<int[]> allPositions = new ArrayList<>();
    Stage stage;
    AnchorPane root;
    private final int tile = 90;
    private final int offBoundsEnd = 40;
    public ChessApplication() {}

    @Override
    public void start(Stage stage) {
        this.stage = stage;
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
        piece.setOnMouseDragged(event -> {
            if (chessEngine.chessBoard.getWhiteTurn() == p.getIsWhite()) {
                piece.setEffect(null);
                piece.setLayoutX(event.getSceneX() - mouseX);
                piece.setLayoutY(event.getSceneY() - mouseY);
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
            }
        });
        piece.setOnMouseReleased(event -> {
            for (ImageView indicator : possibleMoveIndicators.values()) {
                indicator.setVisible(false);
            }
            if (chessEngine.chessBoard.getWhiteTurn() == p.getIsWhite()) {
                int[] position = coordinatesToPosition((int) (event.getSceneX() - mouseX), (int) (event.getSceneY() - mouseY));
                char posX = Utilities.int2Char(position[0]);
                int posY = position[1];
                HashMap<Pioni,ArrayList<int[]>> legalMovesWhenKingThreatened = chessEngine.kingCheckMate(p.isWhite);
                if (legalMovesWhenKingThreatened != null && !legalMovesWhenKingThreatened.isEmpty()){
                    ArrayList<int[]> desiredMoves = legalMovesWhenKingThreatened.get(p);
                    if (desiredMoves != null && desiredMoves.stream().noneMatch(arr->arr[0] == position[0] && arr[1] == posY)) {
                        int[] orig = getCoordinates(p.getXPos(), p.getYPos());
                        piece.setLayoutX(orig[0] - piece.getFitWidth() / 2);
                        piece.setLayoutY(orig[1] - piece.getFitHeight() / 2);
                        piece.setEffect(p.isWhite ? whiteTurnEffect : blackTurnEffect);
                        return;
                    } else legalMovesWhenKingThreatened.clear();
                }
                if (chessEngine.checkDumbMove(p,new int[]{position[0],position[1]})){
                    int[] orig = getCoordinates(p.getXPos(), p.getYPos());
                    piece.setLayoutX(orig[0] - piece.getFitWidth() / 2);
                    piece.setLayoutY(orig[1] - piece.getFitHeight() / 2);
                    piece.setEffect(p.isWhite ? whiteTurnEffect : blackTurnEffect);
                    return;
                }
                boolean res = chessEngine.nextMove(p.getXPos(), p.getYPos(), posX, posY);
                if (!res) {
                    int[] orig = getCoordinates(p.getXPos(), p.getYPos());
                    piece.setLayoutX(orig[0] - piece.getFitWidth() / 2);
                    piece.setLayoutY(orig[1] - piece.getFitHeight() / 2);
                    piece.setEffect(p.isWhite ? whiteTurnEffect : blackTurnEffect);
                    return;
                }
                for (Pioni pioni : pieces.keySet()) {
                    if (pioni.getCaptured()) pieces.get(pioni).setVisible(false);
                }
                int[] newCoordinates = getCoordinates(posX, posY);
                switchTurnAnimation(p.getIsWhite());

                piece.setLayoutX(newCoordinates[0] - piece.getFitWidth() / 2);
                piece.setLayoutY(newCoordinates[1] - piece.getFitHeight() / 2);
                playPiecePlacementSound();
                boolean ended;
                HashMap<Pioni,ArrayList<int[]>> legalMovesWhenEnemyKingThreatened = chessEngine.kingCheckMate(!p.getIsWhite());
                ended = legalMovesWhenEnemyKingThreatened != null;
                if (ended){
                    showWinScreen(p.getIsWhite());
                    System.out.println((p.getIsWhite() ? "White" : "Black") + " won");
                }
                if (ChessEngine.checkKingMat(chessEngine.chessBoard, !p.getIsWhite())){
                    setKingCheckEffect(!p.getIsWhite());
                }
            }

        });
        switchTurnAnimation(p.getIsWhite());
        pieces.put(p, piece);
        root.getChildren().add(piece);
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
    private void showWinScreen(boolean winnerIsWhite) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.TRANSPARENT);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", 40));

        String text = (winnerIsWhite ? "White" : "Black") + " won!";
        double textWidth = gc.getFont().getSize() * text.length() / 2.5;
        double textX = (canvas.getWidth() - textWidth) / 2;
        double textY = canvas.getHeight() / 3;
        gc.fillText(text, textX, textY);

        ImageView mpartzokas = new ImageView(new Image("mpartzokas.png"));
        mpartzokas.setFitWidth(300);
        mpartzokas.setPreserveRatio(true);
        mpartzokas.setLayoutX((canvas.getWidth() - mpartzokas.getFitWidth()) / 2);
        mpartzokas.setLayoutY(textY + 50);

        root.getChildren().addAll(canvas, mpartzokas);
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
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
