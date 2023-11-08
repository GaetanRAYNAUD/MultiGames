package fr.graynaud.multigames.object.sudoku;

import fr.graynaud.multigames.object.sudoku.solver.SudokuSolver;
import fr.graynaud.multigames.object.sudoku.solver.BasicSolver;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;

public class SudokuGrid extends GridPane {

    public static final int GRID_SIZE = 9;

    public static final int CELL_MIN_SIZE = 85;

    private static final ResourceBundle I18N = ResourceBundle.getBundle("i18n.sudoku");

    private static final Random RANDOM = new Random();

    private final SudokuCell[][] grid = new SudokuCell[GRID_SIZE][GRID_SIZE];

    private final SimpleIntegerProperty activeRow = new SimpleIntegerProperty(-1);

    private final SimpleIntegerProperty activeCol = new SimpleIntegerProperty(-1);

    private final BooleanProperty solved = new SimpleBooleanProperty(false);

    private final BooleanProperty filterPossibilities;

    private final BooleanProperty showError;

    private BooleanProperty solving = new SimpleBooleanProperty(false);

    public SudokuGrid(BooleanProperty filterPossibilities, BooleanProperty showError) {
        this.filterPossibilities = filterPossibilities;
        this.showError = showError;
        setPadding(new Insets(2));
        setBackground(Background.fill(Color.BLACK));
        setHgap(2);
        setVgap(2);
        setMaxHeight(minHeight());
        setMaxWidth(minWidth());
        prepare();

        addEventFilter(KeyEvent.KEY_PRESSED, event -> moveFocus(event.getCode(), 0));

        this.solving.addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue) && BooleanUtils.isFalse(newValue)) {
                for (SudokuCell[] row : this.grid) {
                    for (SudokuCell cell : row) {
                        cell.refreshText(cell.getValue(), cell.getValue()); //Force rerender
                    }
                }
            }
        });
    }

    public void prepare() {
        GridPane[][] boxesPanes = new GridPane[GRID_SIZE / 3][GRID_SIZE / 3];

        for (int i = 0; i < boxesPanes.length; i++) {
            for (int j = 0; j < boxesPanes[i].length; j++) {
                GridPane gridPane = new GridPane();
                gridPane.setBackground(Background.fill(Color.LIGHTGRAY));
                gridPane.setVgap(2);
                gridPane.setHgap(2);
                boxesPanes[i][j] = gridPane;
                add(gridPane, j, i);
            }
        }

        for (int i = 0; i < this.grid.length; i++) {
            for (int j = 0; j < this.grid[i].length; j++) {
                SudokuCell cell = new SudokuCell(true, i, j, this.activeRow, this.activeCol, this);
                this.grid[i][j] = cell;

                boxesPanes[i / 3][j / 3].add(cell, j % 3, i % 3);
            }
        }

        for (SudokuCell[] row : this.grid) {
            for (SudokuCell cell : row) {
                cell.postGridFilled();
            }
        }
    }

    public void generate(String seed) {
        if (StringUtils.isBlank(seed) || StringUtils.length(seed) != 81 || !StringUtils.containsOnly(seed, ".0123456789")) {
            return;
        }

        this.solved.set(false);

        for (SudokuCell[] row : this.grid) {
            for (SudokuCell cell : row) {
                cell.reset();
            }
        }

        for (int i = 0; i < seed.length(); i++) {
            char c = seed.charAt(i);

            if (c != '0' && c != '.') {
                this.grid[i / GRID_SIZE][i % GRID_SIZE].setHint(c - '0');
            }
        }
    }

    public void generate() {
        this.solved.set(false);

        for (SudokuCell[] row : this.grid) {
            for (SudokuCell cell : row) {
                cell.reset();
            }
        }

        int nbFilled = 1;
        while (nbFilled <= 9) {
            int i = RANDOM.nextInt(GRID_SIZE);
            int j = RANDOM.nextInt(GRID_SIZE);

            if (this.grid[i][j].getValue() == null) {
                this.grid[i][j].setHint(nbFilled);
                nbFilled++;
            }
        }
    }

    public void computeSolved() {
        if (this.solved.get()) {
            return;
        }

        for (SudokuCell[] row : this.grid) {
            for (SudokuCell cell : row) {
                if (cell.getValue() != null) {
                    return;
                }
            }
        }

        this.solved.set(true);
    }

    public void computeConstraints() {
        for (SudokuCell[] row : this.grid) {
            for (SudokuCell cell : row) {
                SudokuSolver.solve(cell, BasicSolver.INSTANCE, false);
            }
        }

        computeSolved();
    }

    private void moveFocus(KeyCode code, int count) {
        if (KeyCode.UP.equals(code)) {
            if (this.activeRow.get() == 0) {
                this.activeRow.set(GRID_SIZE - 1);
            } else {
                this.activeRow.set(this.activeRow.get() - 1);
            }
        } else if (KeyCode.DOWN.equals(code)) {
            if (this.activeRow.get() == GRID_SIZE - 1) {
                this.activeRow.set(0);
            } else {
                this.activeRow.set(this.activeRow.get() + 1);
            }
        } else if (KeyCode.LEFT.equals(code)) {
            if (this.activeCol.get() == 0) {
                this.activeCol.set(GRID_SIZE - 1);
            } else {
                this.activeCol.set(this.activeCol.get() - 1);
            }
        } else if (KeyCode.RIGHT.equals(code)) {
            if (this.activeCol.get() == GRID_SIZE - 1) {
                this.activeCol.set(0);
            } else {
                this.activeCol.set(this.activeCol.get() + 1);
            }
        }

        if (!getActiveCell().isEditable() && count < GRID_SIZE) {
            moveFocus(code, ++count);
        }
    }

    public double minWidth() {
        return CELL_MIN_SIZE * GRID_SIZE;
    }

    public double minHeight() {
        return CELL_MIN_SIZE * GRID_SIZE;
    }

    public SudokuCell[][] getGrid() {
        return grid;
    }

    public SudokuCell getCell(int row, int col) {
        return this.grid[row][col];
    }

    public SudokuCell getActiveCell() {
        return getCell(this.activeRow.get(), this.activeCol.get());
    }

    public boolean isSolved() {
        return solved.get();
    }

    public BooleanProperty solvedProperty() {
        return solved;
    }

    public boolean isFilterPossibilities() {
        return filterPossibilities.get();
    }

    public BooleanProperty filterPossibilitiesProperty() {
        return filterPossibilities;
    }

    public boolean isShowError() {
        return showError.get();
    }

    public BooleanProperty showErrorProperty() {
        return showError;
    }

    public boolean isSolving() {
        return solving.get();
    }

    public BooleanProperty solvingProperty() {
        return solving;
    }

    public void setSolving(boolean solving) {
        this.solving.set(solving);
    }
}
