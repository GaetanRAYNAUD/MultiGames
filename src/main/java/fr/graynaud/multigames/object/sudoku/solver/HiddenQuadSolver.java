package fr.graynaud.multigames.object.sudoku.solver;

import com.google.common.collect.Sets;
import fr.graynaud.multigames.object.sudoku.SudokuCell;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class HiddenQuadSolver extends SudokuSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiddenTripleSolver.class);

    public static final HiddenQuadSolver INSTANCE = new HiddenQuadSolver();

    private HiddenQuadSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        if (cell.getPossibilities().size() > 2) {
            Set<List<Integer>> cartesian = new HashSet<>(
                    Sets.cartesianProduct(cell.getPossibilities(), cell.getPossibilities(), cell.getPossibilities(), cell.getPossibilities()));
            Iterator<List<Integer>> iterator = cartesian.iterator();

            while (iterator.hasNext()) { //Remove duplicated (i.e., 4,6,7,8 and 8,7,6,4 and 6,8,7,4)
                List<Integer> quad = iterator.next();

                if (quad.size() != new HashSet<>(quad).size()) { //If duplicate number (i.e., 6,6,4,8 or 8,6,6,6) not wanted
                    iterator.remove();
                    continue;
                }

                if (cartesian.stream().anyMatch(t -> !t.equals(quad) && CollectionUtils.isEqualCollection(quad, t))) {
                    iterator.remove();
                }
            }

            for (List<Integer> quad : cartesian) {
                AtomicBoolean done = new AtomicBoolean(false);
                cell.applyToEachContraint(cells -> {
                    if (done.get()) {
                        return;
                    }
                    Set<SudokuCell> parableCells = new HashSet<>();
                    for (Integer possibility : quad) {
                        for (SudokuCell otherCell : cells) {
                            if (otherCell.getValue() == null && otherCell.getPossibilities().contains(possibility)) {
                                parableCells.add(otherCell);
                            }
                        }
                    }

                    if (parableCells.size() == 3) {
                        AtomicBoolean anyChanged = new AtomicBoolean(false);
                        for (Integer possibility : new ArrayList<>(cell.getPossibilities())) {
                            if (!quad.contains(possibility)) { //Keep only those 4 possibilities for the cells
                                anyChanged.set(cell.removePossibility(possibility) || anyChanged.get());
                                parableCells.forEach(c -> anyChanged.set(c.removePossibility(possibility) || anyChanged.get()));
                            }
                        }

                        for (SudokuCell otherCell : cells) {
                            if (!parableCells.contains(otherCell)) {
                                //Remove the quad from other cells
                                quad.forEach(possibility -> anyChanged.set(otherCell.removePossibility(possibility) || anyChanged.get()));
                            }
                        }

                        if (anyChanged.get()) {
                            LOGGER.info("Hidden quad {} at {}", quad, List.of(cell, parableCells));
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
