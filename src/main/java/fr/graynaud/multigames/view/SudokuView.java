package fr.graynaud.multigames.view;

import fr.graynaud.multigames.object.common.TimerLabel;
import fr.graynaud.multigames.object.sudoku.SudokuGrid;
import fr.graynaud.multigames.object.sudoku.solver.SudokuSolver;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SudokuView implements GameView {

    private static final ResourceBundle I18N = ResourceBundle.getBundle("i18n.sudoku");

    private static final Logger LOGGER = LoggerFactory.getLogger(SudokuView.class);

    private final SudokuGrid grid;

    private final TimerLabel timerLabel;

    private final BorderPane root;

    private final ExecutorService solverExecutor = Executors.newSingleThreadExecutor();

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
        solveOneButton.setStyle("-fx-text-fill: BLACK");
        solveOneButton.setMaxWidth(Double.MAX_VALUE);

        Button stepButton = new Button(I18N.getString("step"));
        stepButton.setFont(Font.font(16));
        stepButton.setStyle("-fx-text-fill: BLACK");
        stepButton.setMaxWidth(Double.MAX_VALUE);

        Button solveButton = new Button(I18N.getString("solve"));
        solveButton.setFont(Font.font(16));
        solveButton.setStyle("-fx-text-fill: BLACK");
        solveButton.setMaxWidth(Double.MAX_VALUE);

        solveOneButton.setOnAction(event -> this.solverExecutor.submit(() -> {
            solveOneButton.disableProperty().set(true);
            stepButton.disableProperty().set(true);
            solveButton.disableProperty().set(true);
            try {
                SudokuSolver.solveOne(this.grid);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            solveOneButton.disableProperty().set(false);
            stepButton.disableProperty().set(false);
            solveButton.disableProperty().set(false);
        }));
        stepButton.setOnAction(event -> this.solverExecutor.submit(() -> {
            solveOneButton.disableProperty().set(true);
            stepButton.disableProperty().set(true);
            solveButton.disableProperty().set(true);
            try {
                SudokuSolver.step(this.grid, false);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            solveOneButton.disableProperty().set(false);
            stepButton.disableProperty().set(false);
            solveButton.disableProperty().set(false);
        }));
        solveButton.setOnAction(event -> this.solverExecutor.submit(() -> {
            solveOneButton.disableProperty().set(true);
            stepButton.disableProperty().set(true);
            solveButton.disableProperty().set(true);
            try {
                SudokuSolver.solve(this.grid);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            solveOneButton.disableProperty().set(false);
            stepButton.disableProperty().set(false);
            solveButton.disableProperty().set(false);
        }));

        StackPane gridPane = new StackPane(this.grid);
        gridPane.minWidth(minWidth());
        gridPane.minHeight(minHeight());
        gridPane.setBackground(Background.fill(Color.WHITE));

        StackPane timerPane = new StackPane(this.timerLabel);
        timerPane.setBackground(Background.fill(Color.WHITE));
        timerPane.setPadding(new Insets(MainView.PADDING));

        VBox mainBox = new VBox(timerPane, gridPane);
        mainBox.setBackground(Background.fill(Color.WHITE));

        VBox settings = new VBox(16, newGameButton, stepButton, solveOneButton, solveButton, filterPossibilitiesBox, showErrorBox);
        settings.setPadding(new Insets(5, 10, 5, 10));

        this.root = new BorderPane(mainBox);
        this.root.setPadding(new Insets(MainView.PADDING, MainView.PADDING, MainView.PADDING, MainView.PADDING));
        this.root.setRight(settings);

        this.timerLabel.resetAndStart();
    }

    @Override
    public Parent activate() {
//        newGame();
        this.grid.generate("000000000904607000076804100309701080008000300050308702007502610000403208000000000");

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
        this.solverExecutor.shutdownNow();
    }
}
