package fr.osallek.object.sudoku.solver;

import fr.osallek.object.sudoku.SudokuCell;

import java.util.concurrent.atomic.AtomicBoolean;

public class NakedPairSolver extends SudokuSolver {

    public static final NakedPairSolver INSTANCE = new NakedPairSolver();

    private NakedPairSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        AtomicBoolean done = new AtomicBoolean(false);
        if (cell.getPossibilities().size() == 2) { //Naked pair
            cell.applyToEachContraint(cells -> {
                for (SudokuCell pairCell : cells) {
                    if (pairCell.getPossibilities().equals(cell.getPossibilities())) {
                        for (SudokuCell otherCell : cells) {
                            if (!otherCell.equals(pairCell)) {
                                for (Integer possibility : cell.getPossibilities()) {
                                    done.set(otherCell.removePossibility(possibility) || done.get());
                                }
                            }
                        }
                    }
                }
            });
        }

        return done.get() ? 1 : 0;
    }
}
