package fr.graynaud.multigames.object.sudoku;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SudokuCell extends StackPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(SudokuCell.class);

    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private static final String NUMBERS = "0123456789";

    private static final int MIN_VALUE = 1;

    private static final int MAX_VALUE = 9;

    private final SimpleIntegerProperty value = new SimpleIntegerProperty(0);

    private final int row;

    private final int col;

    private final int box;

    private final SudokuGrid grid;

    private final TextField textField;

    private Set<SudokuCell> constrains;

    private Set<SudokuCell> rowCells;

    private Set<SudokuCell> colCells;

    private Set<SudokuCell> boxCells;

    private final ObservableSet<Integer> possibilities = FXCollections.observableSet(1, 2, 3, 4, 5, 6, 7, 8, 9);

    private final BooleanBinding errorBinding; //Keep reference to prevent being GC

    private boolean resetting = false;

    public SudokuCell(boolean editable, int row, int col, SimpleIntegerProperty activeRow, SimpleIntegerProperty activeCol, SudokuGrid grid) {
        this.grid = grid;
        this.row = row;
        this.col = col;
        this.box = (row / 3) * 3 + col / 3 + 1;
        this.textField = new TextField("");
        this.textField.setEditable(editable);
        this.textField.disableProperty().bind(this.textField.editableProperty().not());

        //Style
        this.textField.setMinWidth(SudokuGrid.CELL_MIN_SIZE);
        this.textField.setMaxWidth(SudokuGrid.CELL_MIN_SIZE);
        this.textField.setMinHeight(SudokuGrid.CELL_MIN_SIZE);
        this.textField.setMaxHeight(SudokuGrid.CELL_MIN_SIZE);
        this.textField.setAlignment(Pos.CENTER);
        setFocused(false);
        getStylesheets().add(SudokuCell.class.getResource("/style/sudoku/style.css").toExternalForm());
        activeRow.addListener((observable, oldValue, newValue) -> {
            if (this.row == newValue.intValue() && this.col == activeCol.get()) {
                this.textField.setStyle("-fx-background-color: #5a7bc0");
            } else if (this.row == newValue.intValue() || this.col == activeCol.get()) {
                this.textField.setStyle("-fx-background-color: #eaeef4");
            } else {
                this.textField.setStyle("-fx-background-color: WHITE");
            }
        });
        activeCol.addListener((observable, oldValue, newValue) -> {
            if (this.col == newValue.intValue() && this.row == activeRow.get()) {
                this.textField.setStyle("-fx-background-color: #5a7bc0");
            } else if (this.col == newValue.intValue() || this.row == activeRow.get()) {
                this.textField.setStyle("-fx-background-color: #eaeef4");
            } else {
                this.textField.setStyle("-fx-background-color: WHITE");
            }
        });
        this.textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue) && Boolean.TRUE.equals(newValue)) {
                activeCol.set(this.col);
                activeRow.set(this.row);

                if (StringUtils.isNotBlank(this.textField.getText())) {
                    Platform.runLater(this.textField::selectAll);
                }
            }
        });

        this.value.addListener((observableValue, oldValue, newValue) -> {
            refreshText(oldValue, newValue);
        });

        this.textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!editable) {
                event.consume();
                return;
            }

            if (!NUMBERS.contains(event.getCharacter())) {
                event.consume();
            }
        });

        this.textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!editable) {
                event.consume();
                return;
            }

            if (KeyCode.DELETE.equals(event.getCode())) {
                setValue(0);
            } else if (!NUMBERS.contains(event.getText())) {
                event.consume();
            }
        });

        this.textField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (this.resetting) {
                return;
            }

            if (!editable) {
                this.textField.textProperty().setValue(oldValue);
            } else if (StringUtils.isNotBlank(newValue)) {
                newValue = StringUtils.deleteWhitespace(StringUtils.trim(newValue));

                if (newValue.length() > 1) { //If type second character, keep only new one to replace
                    newValue = newValue.replace(oldValue, "");
                }

                int intValue = Integer.parseInt(newValue);

                if (intValue < MIN_VALUE || intValue > MAX_VALUE) {
                    this.textField.textProperty().setValue(oldValue);
                } else {
                    this.textField.textProperty().setValue(newValue);
                }
            }

            if (StringUtils.isNotBlank(this.textField.textProperty().get())) {
                this.value.set(Integer.parseInt(this.textField.textProperty().get()));
                Platform.runLater(this.textField::selectAll);
            } else {
                this.value.set(0); //Reset
            }
        });
        this.errorBinding = Bindings.createBooleanBinding(
                () -> this.grid.showErrorProperty().get() && getValue() != null && !this.possibilities.contains(this.value.get()),
                this.grid.showErrorProperty(), this.possibilities, this.value);
        this.errorBinding.addListener((observable, oldValue, newValue) -> {
            if (!this.textField.isEditable() || StringUtils.isBlank(this.textField.getText())) {
                return;
            }

            this.textField.pseudoClassStateChanged(ERROR, Boolean.TRUE.equals(newValue));
        });

        GridPane possibilitiesPane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(33);
        possibilitiesPane.getColumnConstraints().add(columnConstraints);
        possibilitiesPane.getColumnConstraints().add(columnConstraints);
        possibilitiesPane.getColumnConstraints().add(columnConstraints);
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(33);
        possibilitiesPane.getRowConstraints().add(rowConstraints);
        possibilitiesPane.getRowConstraints().add(rowConstraints);
        possibilitiesPane.getRowConstraints().add(rowConstraints);

        possibilitiesPane.setOnMouseClicked(event -> {
            if (this.textField.isEditable()) {
                activeCol.set(this.col);
                activeRow.set(this.row);

                if (StringUtils.isNotBlank(this.textField.getText())) {
                    Platform.runLater(this.textField::selectAll);
                }
            }
        });

        for (Integer b : this.possibilities) {
            Text text = new Text(b.toString());
            text.visibleProperty()
                .bind(Bindings.createBooleanBinding(
                        () -> (!this.grid.filterPossibilitiesProperty().get() || getValue() == null && this.possibilities.contains(b))
                              && getValue() == null,
                        this.possibilities, this.value, this.grid.filterPossibilitiesProperty()));
            text.setOnMouseClicked(event -> setValue(b));
            GridPane.setHalignment(text, HPos.CENTER);
            GridPane.setValignment(text, VPos.CENTER);
            possibilitiesPane.add(text, (b - 1) % 3, (b - 1) / 3);

            activeRow.addListener((observable, oldValue, newValue) -> {
                if (this.row == newValue.intValue() && this.col == activeCol.get()) {
                    text.setFill(Color.WHITE);
                    this.textField.requestFocus();
                } else {
                    text.setFill(Color.GRAY);
                }
            });
            activeCol.addListener((observable, oldValue, newValue) -> {
                if (this.col == newValue.intValue() && this.row == activeRow.get()) {
                    text.setFill(Color.WHITE);
                    this.textField.requestFocus();
                } else {
                    text.setFill(Color.GRAY);
                }
            });
        }

        getChildren().add(this.textField);
        getChildren().add(possibilitiesPane);
    }

    public void postGridFilled() {
        this.constrains = HashSet.newHashSet(20); //8 from row + 8 from col + 4 from box not in row and col
        this.rowCells = HashSet.newHashSet(8);
        this.colCells = HashSet.newHashSet(8);
        this.boxCells = HashSet.newHashSet(8);
        for (SudokuCell[] row : this.grid.getGrid()) {
            for (SudokuCell cell : row) {
                if (!cell.equals(this)) {
                    if (cell.row == this.row) {
                        this.rowCells.add(cell);
                        this.constrains.add(cell);
                    } else if (cell.col == this.col) {
                        this.colCells.add(cell);
                        this.constrains.add(cell);
                    }

                    if (cell.box == this.box) {
                        this.boxCells.add(cell);
                        this.constrains.add(cell);
                    }
                }
            }
        }

        this.constrains = Collections.unmodifiableSet(this.constrains);
        this.rowCells = Collections.unmodifiableSet(this.rowCells);
        this.colCells = Collections.unmodifiableSet(this.colCells);
        this.boxCells = Collections.unmodifiableSet(this.boxCells);
    }

    public void reset() {
        this.resetting = true;
        setValue(null);
        editableProperty().set(true);
        resetPossibilities();
        this.resetting = false;
    }

    public void resetPossibilities() {
        this.possibilities.clear();
        this.possibilities.addAll(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    public void setHint(int value) {
        setValue(value);
        editableProperty().set(false);
        this.possibilities.clear();
        this.possibilities.add(value);
    }

    public void applyToEachContraint(Consumer<Set<SudokuCell>> consumer) {
        consumer.accept(getColCells());
        consumer.accept(getRowCells());
        consumer.accept(getBoxCells());
    }

    public <T> Optional<T> functionToEachContraint(Function<Set<SudokuCell>, Optional<T>> function) {
        return function.apply(getColCells()).or(() -> function.apply(getRowCells())).or(() -> function.apply(getBoxCells()));
    }

    public void refreshText(Number oldValue, Number newValue) {
        if (this.resetting) {
            this.textField.setText("");
            return;
        }

        if (!isEditable()) {
            return;
        }

        if (this.grid.isSolving()) {
            return;
        }

        if (newValue == null || newValue.intValue() == 0) {
            this.textField.setText("");
        } else {
            if (newValue.intValue() < MIN_VALUE) {
                this.value.setValue(MIN_VALUE);
            } else if (newValue.intValue() > MAX_VALUE) {
                this.value.setValue(MAX_VALUE);
            } else {
                if (newValue.intValue() == 0 && StringUtils.isBlank(this.textField.textProperty().get())) {
                    //nothing
                } else {
                    this.textField.setText(newValue.toString());
                }
            }
        }

        if (!Objects.equals(oldValue, getValue())) {
            this.grid.computeConstraints();
        }
    }

    public Integer getValue() {
        return (this.value.get() <= MAX_VALUE && this.value.get() >= MIN_VALUE) ? this.value.get() : null;
    }

    public void setValue(Integer value) {
        if (value == null || value > MAX_VALUE || value < MIN_VALUE) {
            this.value.setValue(0);
        } else {
            this.value.setValue(value);
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getBox() {
        return box;
    }

    public SudokuGrid getGrid() {
        return grid;
    }

    public Set<Integer> getPossibilities() {
        return ImmutableSet.copyOf(this.possibilities);
    }

    public boolean removePossibility(Integer possibility) {
        boolean removed = this.possibilities.remove(possibility);

        if (removed) {
            LOGGER.debug("Removed possibility {} for {}", possibility, this);
        }

        return removed;
    }

    public Set<SudokuCell> getConstrains() {
        return constrains;
    }

    public Set<SudokuCell> getRowCells() {
        return rowCells;
    }

    public Set<SudokuCell> getColCells() {
        return colCells;
    }

    public Set<SudokuCell> getBoxCells() {
        return boxCells;
    }

    public BooleanProperty editableProperty() {
        return this.textField.editableProperty();
    }

    public boolean isEditable() {
        return editableProperty().get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SudokuCell cell = (SudokuCell) o;

        if (row != cell.row) {
            return false;
        }

        if (col != cell.col) {
            return false;
        }

        return grid.equals(cell.grid);
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col;
        result = 31 * result + grid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "[" + row + "," + col + ']';
    }
}
