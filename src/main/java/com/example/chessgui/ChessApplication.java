package com.example.chessgui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;


public class ChessApplication extends Application {
    private HashMap<Pioni, ImageView> pieces = new HashMap<>();
    private double mouseX;
    private double mouseY;
    private final DropShadow turnEffect = new DropShadow();
    private final DropShadow kingCheckEffect = new DropShadow();
    private ChessEngine chessEngine;
    private final HashMap<Pioni,int[]> legalMovesWhenKingThreatened = new HashMap<>();
    HashMap<String,ImageView> possibleMoveIndicators = new HashMap<>();
    ArrayList<int[]> allPositions = new ArrayList<>();
    Stage stage;
    AnchorPane root;
    public ChessApplication() {}

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        AnchorPane root = new AnchorPane();
        this.root = root;
        stage.setHeight(800);
        stage.setWidth(800);

        ImageView background = new ImageView(new Image("chessBoard.png"));
        background.setFitWidth(stage.getWidth());
        background.setFitHeight(stage.getHeight());
        background.setLayoutX(0);
        background.setLayoutY(0);


        turnEffect.setColor(Color.GOLD);
        turnEffect.setRadius(1);
        turnEffect.setSpread(1.0);

        kingCheckEffect.setColor(Color.RED);
        kingCheckEffect.setRadius(2);
        kingCheckEffect.setSpread(1.0);

        root.getChildren().add(background);
        chessEngine = new ChessEngine();
        chessEngine.playChess();
        ArrayList<Pioni> Pionia = chessEngine.chessBoard.getPionia();
        for (Pioni p : Pionia) {
            addPiece(root, p);
        }
        Scene scene = new Scene(root, 800, 800);
        stage.setTitle("Chess Game");
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
        piece.setPreserveRatio(true);
        piece.setOnMousePressed(event -> {
            mouseX = event.getSceneX() - piece.getLayoutX();
            mouseY = event.getSceneY() - piece.getLayoutY();
        });
        piece.setOnMouseDragged(event -> {
            if (chessEngine.chessBoard.getWhiteTurn() == p.getIsWhite()) {
                piece.setEffect(null);
                piece.setLayoutX(event.getSceneX() - mouseX);
                piece.setLayoutY(event.getSceneY() - mouseY);
                System.out.println(legalMovesWhenKingThreatened);
                if (!legalMovesWhenKingThreatened.isEmpty()){
                    if (legalMovesWhenKingThreatened.get(p) == null) return;
                    int[] dest = legalMovesWhenKingThreatened.get(p);
                    possibleMoveIndicators.get(String.valueOf(Utilities.int2Char(dest[0])) + dest[1]).setVisible(true);
                } else {
                    for (int[] dest : allPositions) {
                        char destX = Utilities.int2Char(dest[0]);
                        int  destY = dest[1];
                        boolean res = p.isLegalMove(destX, destY);
                        if (res) possibleMoveIndicators.get(String.valueOf(destX) + destY).setVisible(true);
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
                if (!legalMovesWhenKingThreatened.isEmpty()){
                    int[] desiredMove = legalMovesWhenKingThreatened.get(p);
                    if (desiredMove == null || desiredMove[0] != position[0] || desiredMove[1] != position[1]) {
                        int[] orig = getCoordinates(p.getXPos(), p.getYPos());
                        piece.setLayoutX(orig[0] - piece.getFitWidth() / 2);
                        piece.setLayoutY(orig[1] - piece.getFitHeight() / 2);
                        piece.setEffect(turnEffect);
                        return;
                    } else legalMovesWhenKingThreatened.clear();
                }
                boolean res = chessEngine.nextMove(p.getXPos(), p.getYPos(), posX, posY);
                if (!res) {
                    int[] orig = getCoordinates(p.getXPos(), p.getYPos());
                    piece.setLayoutX(orig[0] - piece.getFitWidth() / 2);
                    piece.setLayoutY(orig[1] - piece.getFitHeight() / 2);
                    piece.setEffect(turnEffect);
                    return;
                }
                for (Pioni pioni : pieces.keySet()) {
                    if (pioni.getCaptured()) pieces.get(pioni).setVisible(false);
                }
                int[] newCoordinates = getCoordinates(posX, posY);
                switchTurnAnimation(p.getIsWhite());
                checkKingMat(chessEngine.chessBoard, !p.getIsWhite());
                piece.setLayoutX(newCoordinates[0] - piece.getFitWidth() / 2);
                piece.setLayoutY(newCoordinates[1] - piece.getFitHeight() / 2);
                playPiecePlacementSound();
                boolean ended;
                ended = kingCheckMate(!p.getIsWhite(),false);
                if (ended){
                    showWinScreen(p.getIsWhite());
                    System.out.println((p.getIsWhite() ? "White" : "Black") + " won");
                }
                if (checkKingMat(chessEngine.chessBoard, !p.getIsWhite())){
                    kingCheckMate(!p.getIsWhite(),true);
                }
            }

        });
        switchTurnAnimation(p.getIsWhite());
        pieces.put(p, piece);
        root.getChildren().add(piece);
    }
    private boolean positionIsThreatened(boolean white, char x, int y){
        for (Pioni p : chessEngine.chessBoard.getPionia()){
            if (p.getIsWhite() == white){
                if (p.isLegalMove(x,y)) return true;
            }
        }
        return false;
    }
    private int[] getCoordinates(char x, int y) {
        int[] coordinates = new int[2];
        coordinates[0] = 50 + Math.abs(Utilities.char2Int(x) - 1) * 100;
        coordinates[1] = 50 + Math.abs(y - 8) * 100;
        return coordinates;
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
    private boolean checkKingMat(ChessBoard chessBoard, boolean white){
        Pioni allyKing = chessBoard.getPionia()
                .stream()
                .filter(p -> p.getIsWhite() == white && p.type.equals("Vasilias"))
                .findFirst()
                .orElse(null);
        assert allyKing != null;
        for (Pioni p : chessBoard.getPionia()) {
            if (p.getIsWhite() != white){
                if (p.isLegalMove(allyKing.getXPos(), allyKing.getYPos())) {
                    if (white) {
                        ImageView kingView = pieces.get(allyKing);
                        if (kingView != null) kingView.setEffect(kingCheckEffect);
                        return true;
                    }
                    else {
                        ImageView kingView = pieces.get(allyKing);
                        if (kingView != null) kingView.setEffect(kingCheckEffect);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean kingCheckMate(boolean white, boolean saveRoutes) {
        ArrayList<Pioni> duplicatePieces = chessEngine.chessBoard.getPionia().stream().filter(pioni -> pioni.getIsWhite() == white && !pioni.getCaptured()).collect(Collectors.toCollection(ArrayList::new));
        for (Pioni p : duplicatePieces) {
            for (int[] pos : allPositions) {
                ChessBoard testChessBoard = chessEngine.chessBoard.clone();
                Pioni duplicatePioni = testChessBoard.getPioniAt(p.getXPos(), p.getYPos());
                if (duplicatePioni.isLegalMove(Utilities.int2Char(pos[0]), pos[1])) {
                    testChessBoard.move(p.getXPos(), p.getYPos(), Utilities.int2Char(pos[0]), pos[1]);
                    if (!saveRoutes && !checkKingMat(testChessBoard, white)) return false;
                    if (!checkKingMat(testChessBoard, white)) legalMovesWhenKingThreatened.put(chessEngine.chessBoard.getPioniAt(p.getXPos(),p.getYPos()),new int[] {pos[0],pos[1]});
                }
            }
        }
        return true;
    }

    private int[] coordinatesToPosition(int x, int y) {
        int[] position = new int[2];
        position[0] = Math.abs(x) / 100 + 1;
        position[1] = Math.abs(y - 800) / 100 + 1;
        return position;
    }
    private void switchTurnAnimation(boolean isWhite) {
        if (isWhite) {
            for (Pioni p : pieces.keySet()) {
                if (!p.isWhite) pieces.get(p).setEffect(turnEffect);
                else pieces.get(p).setEffect(null);
            }
            return;
        }
        for (Pioni p : pieces.keySet()) {
            if (p.isWhite) pieces.get(p).setEffect(turnEffect);
            else pieces.get(p).setEffect(null);
        }

    }

    public static void main(String[] args) {
        launch();
    }

    private void playPiecePlacementSound() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/placement.wav");
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
