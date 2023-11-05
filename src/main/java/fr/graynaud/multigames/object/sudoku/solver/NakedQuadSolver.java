package fr.graynaud.multigames.object.sudoku.solver;

import com.google.common.collect.Sets;
import fr.graynaud.multigames.object.sudoku.SudokuCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NakedQuadSolver extends SudokuSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(NakedQuadSolver.class);

    public static final NakedQuadSolver INSTANCE = new NakedQuadSolver();

    private NakedQuadSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        AtomicBoolean done = new AtomicBoolean(false);
        if (cell.getPossibilities().size() == 2 || cell.getPossibilities().size() == 3 || cell.getPossibilities().size() == 4) {
            cell.applyToEachContraint(cells -> {
                for (List<SudokuCell> triple : Sets.cartesianProduct(cells, cells, cells)) { //Got all possible triples
                    //Remove triples with the same cell
                    if (!triple.get(0).equals(triple.get(1)) && !triple.get(0).equals(triple.get(2)) && !triple.get(1).equals(triple.get(2))) {
                        Set<Integer> allPossibilities = Stream.concat(triple.stream().map(SudokuCell::getPossibilities).flatMap(Collection::stream),
                                                                      cell.getPossibilities().stream()).collect(Collectors.toSet());

                        if (allPossibilities.size() == 4) {
                            LOGGER.info("Naked quad {},{},{},{} at {},{},{},{}", cell.getPossibilities().iterator().next(),
                                        List.of(cell.getPossibilities()).get(1),
                                        List.of(cell.getPossibilities()).get(2), List.of(cell.getPossibilities()).get(3), allPossibilities.iterator().next(),
                                        List.of(allPossibilities).get(1), List.of(allPossibilities).get(2), List.of(allPossibilities).get(3));
                            for (SudokuCell otherCell : cells) {
                                if (otherCell.getValue() == null && !triple.contains(otherCell)) {
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
