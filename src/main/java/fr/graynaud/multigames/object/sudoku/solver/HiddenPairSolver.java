package fr.graynaud.multigames.object.sudoku.solver;

import com.google.common.collect.Sets;
import fr.graynaud.multigames.object.sudoku.SudokuCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class HiddenPairSolver extends SudokuSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiddenPairSolver.class);

    public static final HiddenPairSolver INSTANCE = new HiddenPairSolver();

    private HiddenPairSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        if (cell.getPossibilities().size() > 2) {
            Set<List<Integer>> cartesian = new HashSet<>(Sets.cartesianProduct(cell.getPossibilities(), cell.getPossibilities()));
            Iterator<List<Integer>> iterator = cartesian.iterator();

            while (iterator.hasNext()) { //Remove duplicated (i.e., 4,6 and 6,4)
                List<Integer> pair = iterator.next();

                if (pair.get(0).equals(pair.get(1))) { //If duplicate number (i.e., 6,6 or 4,4) not wanted
                    iterator.remove();
                    continue;
                }

                if (cartesian.contains(List.of(pair.get(1), pair.get(0)))) {
                    iterator.remove();
                }
            }

            for (List<Integer> pair : cartesian) {
                AtomicBoolean done = new AtomicBoolean(false);
                cell.applyToEachContraint(cells -> {
                    if (done.get()) {
                        return;
                    }
                    Set<SudokuCell> parableCells = new HashSet<>();
                    for (Integer possibility : pair) {
                        for (SudokuCell otherCell : cells) {
                            if (otherCell.getValue() == null && otherCell.getPossibilities().contains(possibility)) {
                                parableCells.add(otherCell);
                            }
                        }
                    }

                    if (parableCells.size() == 1) {
                        AtomicBoolean anyChanged = new AtomicBoolean(false);
                        for (Integer possibility : new ArrayList<>(cell.getPossibilities())) {
                            if (!pair.contains(possibility)) { //Keep only those 2 possibilities for the cells
                                anyChanged.set(cell.removePossibility(possibility) || anyChanged.get());
                                anyChanged.set(parableCells.iterator().next().removePossibility(possibility) || anyChanged.get());
                            }
                        }

                        for (SudokuCell otherCell : cells) {
                            if (!parableCells.iterator().next().equals(otherCell)) {
                                //Remove the pair from other cells
                                pair.forEach(possibility -> anyChanged.set(otherCell.removePossibility(possibility) || anyChanged.get()));
                            }
                        }

                        if (anyChanged.get()) {
                            LOGGER.info("Hidden pair {} at {}", pair, List.of(cell, parableCells));
                            done.set(true);
                        }
                    }
                });

                if (done.get()) {
                    return 1;
                }
            }
        }

        return 0;
    }
}
