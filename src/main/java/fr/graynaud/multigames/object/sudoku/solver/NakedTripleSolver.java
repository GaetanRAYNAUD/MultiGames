package fr.graynaud.multigames.object.sudoku.solver;

import com.google.common.collect.Sets;
import fr.graynaud.multigames.object.sudoku.SudokuCell;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NakedTripleSolver extends SudokuSolver {

    public static final NakedTripleSolver INSTANCE = new NakedTripleSolver();

    private NakedTripleSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        AtomicBoolean done = new AtomicBoolean(false);
        if (cell.getPossibilities().size() == 2 || cell.getPossibilities().size() == 3) {
            cell.applyToEachContraint(cells -> {
                for (List<SudokuCell> pair : Sets.cartesianProduct(cells, cells)) { //Got all possible pairs
                    if (!pair.get(0).equals(pair.get(1))) { //Remove pairs with same cell
                        Set<Integer> allPossibilities = Stream.concat(pair.stream().map(SudokuCell::getPossibilities).flatMap(Collection::stream),
                                                                      cell.getPossibilities().stream()).collect(Collectors.toSet());

                        if (allPossibilities.size() == 3) {
                            for (SudokuCell otherCell : cells) {
                                if (!pair.contains(otherCell)) {
                                    for (Integer possibility : allPossibilities) {
                                        done.set(otherCell.removePossibility(possibility) || done.get());
                                    }
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