package fr.graynaud.multigames.object.sudoku.solver;

import com.google.common.collect.Sets;
import fr.graynaud.multigames.object.sudoku.SudokuCell;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiddenTripleSolver extends SudokuSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiddenTripleSolver.class);

    public static final HiddenTripleSolver INSTANCE = new HiddenTripleSolver();

    private HiddenTripleSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        if (cell.getPossibilities().size() > 2) {
            Set<List<Integer>> cartesian = new HashSet<>(Sets.cartesianProduct(cell.getPossibilities(), cell.getPossibilities(), cell.getPossibilities()));
            Iterator<List<Integer>> iterator = cartesian.iterator();

            while (iterator.hasNext()) { //Remove duplicated (i.e., 4,6,7 and 7,6,4 and 6,7,4)
                List<Integer> triple = iterator.next();

                if (triple.size() != new HashSet<>(triple).size()) { //If duplicate number (i.e., 6,6,4 or 6,6,6) not wanted
                    iterator.remove();
                    continue;
                }

                if (cartesian.stream().anyMatch(t -> !t.equals(triple) && CollectionUtils.isEqualCollection(triple, t))) {
                    iterator.remove();
                }
            }

            for (List<Integer> triple : cartesian) {
                AtomicBoolean done = new AtomicBoolean(false);
                cell.applyToEachContraint(cells -> {
                    if (done.get()) {
                        return;
                    }
                    Set<SudokuCell> parableCells = new HashSet<>();
                    for (Integer possibility : triple) {
                        for (SudokuCell otherCell : cells) {
                            if (otherCell.getValue() == null && otherCell.getPossibilities().contains(possibility)) {
                                parableCells.add(otherCell);
                            }
                        }
                    }

                    if (parableCells.size() == 2) {
                        AtomicBoolean anyChanged = new AtomicBoolean(false);
                        Stream.concat(cell.getPossibilities().stream(), parableCells.stream().map(SudokuCell::getPossibilities).flatMap(Collection::stream))
                              .forEach(possibility -> {
                                  if (!triple.contains(possibility)) { //Keep only those 3 possibilities for the cells
                                      anyChanged.set(cell.removePossibility(possibility) || anyChanged.get());
                                      parableCells.forEach(c -> anyChanged.set(c.removePossibility(possibility) || anyChanged.get()));
                                  }
                              });

                        for (SudokuCell otherCell : cells) {
                            if (!parableCells.contains(otherCell)) {
                                //Remove the triple from other cells
                                triple.forEach(possibility -> anyChanged.set(otherCell.removePossibility(possibility) || anyChanged.get()));
                            }
                        }

                        if (anyChanged.get()) {
                            LOGGER.info("Hidden triple {} at {}", triple, List.of(cell, parableCells));
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
