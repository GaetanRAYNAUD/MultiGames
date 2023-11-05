package fr.graynaud.multigames.view;

import fr.graynaud.multigames.object.common.TimerLabel;
import fr.graynaud.multigames.object.sudoku.SudokuGrid;
import fr.graynaud.multigames.object.sudoku.solver.SudokuSolver;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Objects;
import java.util.ResourceBundle;

public class SudokuView implements GameView {

    private static final ResourceBundle I18N = ResourceBundle.getBundle("i18n.sudoku");

    private final SudokuGrid grid;

    private final TimerLabel timerLabel;

    private final BorderPane root;

    public SudokuView() {
        CheckBox filterPossibilitiesBox = new CheckBox(I18N.getString("filterPossibilities"));
        filterPossibilitiesBox.setFont(Font.font(16));
        filterPossibilitiesBox.setSelected(true);

        CheckBox showErrorBox = new CheckBox(I18N.getString("showError"));
        showErrorBox.setFont(Font.font(16));
        showErrorBox.setSelected(false);

        Button newGameButton = new Button(I18N.getString("newGame"));
        newGameButton.setFont(Font.font(16));
        newGameButton.setOnAction(event -> this.newGame());
        newGameButton.setStyle("-fx-text-fill: BLACK");
        newGameButton.setMaxWidth(Double.MAX_VALUE);

        this.grid = new SudokuGrid(filterPossibilitiesBox.selectedProperty(), showErrorBox.selectedProperty());
        this.timerLabel = new TimerLabel();
        this.grid.solvedProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue) && Boolean.TRUE.equals(newValue)) {
                this.timerLabel.stop();
            }
        });

        showErrorBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue) && Boolean.TRUE.equals(newValue)) {
                this.grid.getActiveCell().requestFocus();
            }
        });

        Button solveOneButton = new Button(I18N.getString("solveOne"));
        solveOneButton.setFont(Font.font(16));
        solveOneButton.setOnAction(event -> Platform.runLater(() -> SudokuSolver.solveOne(this.grid)));
        solveOneButton.setStyle("-fx-text-fill: BLACK");
        solveOneButton.setMaxWidth(Double.MAX_VALUE);

        Button stepButton = new Button(I18N.getString("step"));
        stepButton.setFont(Font.font(16));
        stepButton.setOnAction(event -> Platform.runLater(() -> SudokuSolver.step(this.grid, false)));
        stepButton.setStyle("-fx-text-fill: BLACK");
        stepButton.setMaxWidth(Double.MAX_VALUE);

        Button solveButton = new Button(I18N.getString("solve"));
        solveButton.setFont(Font.font(16));
        solveButton.setOnAction(event -> Platform.runLater(() -> SudokuSolver.solve(this.grid)));
        solveButton.setStyle("-fx-text-fill: BLACK");
        solveButton.setMaxWidth(Double.MAX_VALUE);

        StackPane gridPane = new StackPane(this.grid);
        gridPane.minWidth(minWidth());
        gridPane.minHeight(minHeight());
        gridPane.setBackground(Background.fill(Color.WHITE));

        StackPane timerPane = new StackPane(this.timerLabel);
        timerPane.setBackground(Background.fill(Color.WHITE));
        timerPane.setPadding(new Insets(MainView.PADDING));

        VBox mainBox = new VBox(timerPane, gridPane);
        mainBox.setBackground(Background.fill(Color.WHITE));

        VBox settings = new VBox(16, newGameButton, stepButton, solveButton, filterPossibilitiesBox, showErrorBox);
        settings.setPadding(new Insets(5, 10, 5, 10));

        this.root = new BorderPane(mainBox);
        this.root.setPadding(new Insets(MainView.PADDING, MainView.PADDING, MainView.PADDING, MainView.PADDING));
        this.root.setRight(settings);

        this.timerLabel.resetAndStart();
    }

    @Override
    public Parent activate() {
        newGame();

        return this.root;
    }

    private void newGame() {
        this.grid.generate();
        this.timerLabel.resetAndStart();
    }

    public void resetTimer() {
        this.timerLabel.resetAndStart();
    }

    @Override
    public double minWidth() {
        return this.grid.minWidth() + MainView.PADDING * 2;
    }

    @Override
    public double minHeight() {
        return this.grid.minHeight() + MainView.PADDING * 2 + 75;
    }

    @Override
    public void stop() {
        this.timerLabel.stop();
    }
}
