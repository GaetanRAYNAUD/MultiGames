package fr.osallek.object.sudoku.solver;

import com.google.common.collect.Sets;
import fr.osallek.object.sudoku.SudokuCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class HiddenPairSolver extends SudokuSolver {

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
                    List<SudokuCell> parableCells = new ArrayList<>();
                    for (Integer possibility : pair) {
                        for (SudokuCell otherCell : cells) {
                            if (otherCell.getValue() == null && otherCell.getPossibilities().contains(possibility)) {
                                parableCells.add(otherCell);
                            } else {
                                parableCells.remove(otherCell);
                            }
                        }
                    }

                    if (parableCells.size() == 1) {
                        for (Integer possibility : new ArrayList<>(cell.getPossibilities())) {
                            if (!pair.contains(possibility)) { //Keep only those 2 possibilities for the cells
                                cell.removePossibility(possibility);
                                parableCells.get(0).removePossibility(possibility);
                            }
                        }

                        for (SudokuCell otherCell : cells) {
                            if (!parableCells.get(0).equals(otherCell)) {
                                pair.forEach(otherCell::removePossibility); //Remove the pair from other cells
                            }
                        }
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
