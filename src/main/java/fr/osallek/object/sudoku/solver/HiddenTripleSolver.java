package fr.osallek.object.sudoku.solver;

import com.google.common.collect.Sets;
import fr.osallek.object.sudoku.SudokuCell;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class HiddenTripleSolver extends SudokuSolver {

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
                    List<SudokuCell> parableCells = new ArrayList<>();
                    for (Integer possibility : triple) {
                        for (SudokuCell otherCell : cells) {
                            if (otherCell.getValue() == null && otherCell.getPossibilities().contains(possibility)) {
                                parableCells.add(otherCell);
                            } else {
                                parableCells.remove(otherCell);
                            }
                        }
                    }

                    if (parableCells.size() == 2) {
/*                        for (Integer possibility : new ArrayList<>(cell.getPossibilities())) {
                            if (!triple.contains(possibility)) { //Keep only those 2 possibilities for the cells
                                cell.removePossibility(possibility);
                                parableCells.get(0).removePossibility(possibility);
                            }
                        }

                        for (SudokuCell otherCell : cells) {
                            if (!parableCells.get(0).equals(otherCell)) {
                                triple.forEach(otherCell::removePossibility); //Remove the pair from other cells
                            }
                        }*/
                        done.set(true);
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
