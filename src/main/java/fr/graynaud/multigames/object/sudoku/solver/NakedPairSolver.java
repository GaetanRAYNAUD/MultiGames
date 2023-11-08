package fr.graynaud.multigames.object.sudoku.solver;

import fr.graynaud.multigames.object.sudoku.SudokuCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NakedPairSolver extends SudokuSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(NakedPairSolver.class);

    public static final NakedPairSolver INSTANCE = new NakedPairSolver();

    private NakedPairSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        AtomicBoolean done = new AtomicBoolean(false);
        if (cell.getPossibilities().size() == 2) { //Naked pair
            cell.applyToEachContraint(cells -> {
                if (done.get()) {
                    return;
                }

                for (SudokuCell pairCell : cells) {
                    if (pairCell.getValue() == null && pairCell.getPossibilities().equals(cell.getPossibilities())) {
                        for (SudokuCell otherCell : cells) {
                            if (otherCell.getValue() == null && !otherCell.equals(pairCell)) {
                                for (Integer possibility : cell.getPossibilities()) {
                                    done.set(otherCell.removePossibility(possibility) || done.get());
                                }
                            }
                        }

                        if (done.get()) {
                            LOGGER.info("Naked pair {} at {}", cell.getPossibilities(), List.of(cell, pairCell));
                            return;
                        }
                    }
                }
            });
        }

        return done.get() ? 1 : 0;
    }
}
