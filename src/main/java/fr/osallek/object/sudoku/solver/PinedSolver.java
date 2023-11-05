package fr.osallek.object.sudoku.solver;

import fr.osallek.object.sudoku.SudokuCell;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class PinedSolver extends SudokuSolver {

    public static final PinedSolver INSTANCE = new PinedSolver();

    private PinedSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        for (Integer possibility : cell.getPossibilities()) {
            //Possibility is only available in this cell for this box/col/row = pinned
            if (cell.functionToEachContraint(cells -> {
                if (cells.stream().map(SudokuCell::getPossibilities).flatMap(Collection::stream).noneMatch(p -> Objects.equals(p, possibility))) {
                    cell.setValue(possibility);
                    return Optional.of(2);
                }

                return Optional.empty(); //empty to continue on other contraints
            }).isPresent()) {
                return 2;
            }
        }

        return 0;
    }
}
