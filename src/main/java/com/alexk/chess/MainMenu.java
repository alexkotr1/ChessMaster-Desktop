package com.alexk.chess;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Main menu application for the Chess Game.
 * <p>
 * This class provides the entry point for the chess application with a graphical
 * user interface for starting new games, loading saved games, and viewing match history.
 * It allows configuration of game settings such as time control.
 * </p>
 *
 * @author Alex K
 * @version 1.0
 * @see ChessApplication
 * @see ChessEngine
 */
public class MainMenu extends Application {

    /**
     * Slider for selecting time per move.
     */
    private Slider timeSlider;

    /**
     * Checkbox for enabling/disabling time limits.
     */
    private CheckBox noLimitCheckBox;

    /**
     * Label displaying the current time value.
     */
    private Label timeValueLabel;

    /**
     * Reference to the primary stage.
     */
    private Stage stage;

    /**
     * The main entry point for the JavaFX application.
     * <p>
     * Sets up the main menu interface with game options and controls.
     * </p>
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        StackPane root = new StackPane();
        root.setPadding(new Insets(40));
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #3f2c0e, #2a1c08);" +
                        "-fx-font-family: 'Segoe UI', 'System';"
        );

        // Main card container
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 40, 40, 40));
        card.setMaxWidth(450);

        card.setBackground(new Background(new BackgroundFill(
                Color.web("#feeeba"),
                new CornerRadii(25),
                Insets.EMPTY
        )));

        card.setEffect(new DropShadow(20, Color.color(0, 0, 0, 0.6)));

        // Title section
        Label title = new Label("ChessMaster");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#3f2c0e"));

        Label subtitle = new Label("Main Menu");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        subtitle.setTextFill(Color.web("#7a5a26"));

        VBox titleBox = new VBox(5, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // Time control section
        VBox timeBox = createTimeControlSection();

        // Action buttons
        Button newGameBtn = createMainButton("Start New Game");
        Button loadGameBtn = createMainButton("Load Saved Game");
        Button historyBtn = createMainButton("Check Match History");

        // Button actions
        newGameBtn.setOnAction(e -> {
            boolean noLimit = noLimitCheckBox.isSelected();
            int totalSecondsForMove = noLimit ? 0 : (int) timeSlider.getValue();
            ChessApplication chessApp = new ChessApplication();
            chessApp.setTotalTime(totalSecondsForMove);
            chessApp.start(stage);
        });

        loadGameBtn.setOnAction(e -> showLoadGameDialog(primaryStage));
        historyBtn.setOnAction(e -> showHistoryDialog(primaryStage));

        VBox buttonBox = new VBox(12, newGameBtn, loadGameBtn, historyBtn);
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(titleBox, timeBox, buttonBox);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 800, 600);
        applyGlobalButtonStyles(scene);

        primaryStage.setTitle("Chess Game - Main Menu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the time control section with slider and checkbox.
     *
     * @return a VBox containing the time control UI elements
     */
    private VBox createTimeControlSection() {
        Label sectionTitle = new Label("Time Settings");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#3f2c0e"));

        Label description = new Label("Choose how much time each player has.");
        description.setFont(Font.font("Segoe UI", 13));
        description.setTextFill(Color.web("#7a5a26"));

        timeSlider = new Slider(5, 300, 60);
        timeSlider.setShowTickLabels(true);
        timeSlider.setShowTickMarks(true);
        timeSlider.setMajorTickUnit(60);
        timeSlider.setMinorTickCount(5);
        timeSlider.setBlockIncrement(5);
        timeSlider.setPrefWidth(260);

        timeValueLabel = new Label("60 seconds");
        timeValueLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        timeValueLabel.setTextFill(Color.web("#3f2c0e"));

        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!noLimitCheckBox.isSelected()) {
                int seconds = newVal.intValue();
                timeValueLabel.setText(seconds + " second" + (seconds == 1 ? "" : "s"));
            }
        });

        noLimitCheckBox = new CheckBox("No time limit");
        noLimitCheckBox.setFont(Font.font("Segoe UI", 13));
        noLimitCheckBox.setTextFill(Color.web("#3f2c0e"));

        noLimitCheckBox.selectedProperty().addListener((obs, oldVal, isSelected) -> {
            timeSlider.setDisable(isSelected);
            if (isSelected) {
                timeValueLabel.setText("No limit");
            } else {
                timeValueLabel.setText((int) timeSlider.getValue() + " second" + ((int) timeSlider.getValue() == 1 ? "" : "s"));
            }
        });

        VBox timeBox = new VBox(6,
                sectionTitle,
                description,
                timeSlider,
                timeValueLabel,
                noLimitCheckBox
        );
        timeBox.setAlignment(Pos.CENTER_LEFT);
        timeBox.setPadding(new Insets(10, 0, 10, 0));

        return timeBox;
    }

    /**
     * Creates a styled button for the main menu.
     *
     * @param text the button text
     * @return a styled Button instance
     */
    private Button createMainButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(260);
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        btn.getStyleClass().add("menu-button");
        return btn;
    }

    /**
     * Applies global CSS styles to buttons in the scene.
     *
     * @param scene the scene to apply styles to
     */
    private void applyGlobalButtonStyles(Scene scene) {
        String css = """
            .button.menu-button {
                -fx-background-color: linear-gradient(to bottom, #3f2c0e, #2d1f0b);
                -fx-text-fill: #feeeba;
                -fx-background-radius: 20;
                -fx-border-radius: 20;
                -fx-border-color: rgba(255,255,255,0.3);
                -fx-border-width: 1;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0.5, 0, 3);
            }
            .button.menu-button:hover {
                -fx-background-color: linear-gradient(to bottom, #5b3f13, #3f2c0e);
                -fx-translate-y: -1;
            }
            .button.menu-button:pressed {
                -fx-background-color: #2a1c08;
                -fx-translate-y: 1;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0.4, 0, 1);
            }
        """;

        scene.getStylesheets().add(
                "data:text/css," + css.replace("\n", "%0A").replace(" ", "%20")
        );
    }

    /**
     * Shows a dialog for loading saved games.
     *
     * @param owner the owner stage for the dialog
     */
    private void showLoadGameDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Load Saved Game");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        Label title = new Label("Select a saved game from ./games");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        ListView<GameDetails> listView = new ListView<>();
        listView.setPlaceholder(new Label("No saved games found."));
        listView.setPrefHeight(250);

        List<GameDetails> entries = loadSavedGamesFromFolder(false);
        listView.setItems(FXCollections.observableArrayList(entries));

        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(GameDetails item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getStartedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        });

        Button loadButton = new Button("Load");
        Button cancelButton = new Button("Cancel");

        loadButton.setOnAction(e -> {
            GameDetails selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ChessApplication chessApplication = new ChessApplication(ChessEngine.fromGameDetails(selected));
                chessApplication.start(stage);
            }
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
                loadButton.fire();
        });

        HBox buttons = new HBox(10, loadButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.setTop(title);
        BorderPane.setMargin(title, new Insets(0, 0, 10, 0));
        root.setCenter(listView);
        root.setBottom(buttons);

        Scene scene = new Scene(root, 500, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Shows a dialog for viewing match history.
     *
     * @param owner the owner stage for the dialog
     */
    private void showHistoryDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Load Saved Game");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        Label title = new Label("Select a saved game from ./games");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        ListView<GameDetails> listView = new ListView<>();
        listView.setPlaceholder(new Label("No saved games found."));
        listView.setPrefHeight(250);

        List<GameDetails> entries = loadSavedGamesFromFolder(true);
        listView.setItems(FXCollections.observableArrayList(entries));

        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(GameDetails item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getStartedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        });

        Button showButton = new Button("Show");
        Button cancelButton = new Button("Cancel");

        showButton.setOnAction(e -> {
            GameDetails selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ChessApplication chessApplication = new ChessApplication(ChessEngine.fromGameDetails(selected));
                chessApplication.start(stage);
            }
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
                showButton.fire();
        });

        HBox buttons = new HBox(10, showButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.setTop(title);
        BorderPane.setMargin(title, new Insets(0, 0, 10, 0));
        root.setCenter(listView);
        root.setBottom(buttons);

        Scene scene = new Scene(root, 500, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Loads saved games from the games folder.
     *
     * @param ended if true, loads only ended games; if false, loads only in-progress games
     * @return a list of GameDetails objects
     */
    private List<GameDetails> loadSavedGamesFromFolder(boolean ended) {
        List<GameDetails> result = new ArrayList<>();

        try {
            Path dir = Paths.get("./games");
            if (!Files.exists(dir))
                return result;

            try (Stream<Path> paths = Files.list(dir)) {
                paths
                        .filter(p -> p.toString().endsWith(".game"))
                        .sorted()
                        .forEach(p -> {
                            GameDetails gd = GameDetails.loadFromFile(p.toString());
                            if (gd != null && ((!gd.getWinner().equals(ChessEngine.Result.InProgress) && ended) || (gd.getWinner().equals(ChessEngine.Result.InProgress) && !ended))) result.add(gd);
                        });
            }

        } catch (Exception e) {
            System.err.println("[MainMenu] Error loading saved games:");
        }

        return result;
    }

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}