package fr.graynaud.multigames.object.sudoku.solver;

import fr.graynaud.multigames.object.sudoku.SudokuCell;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class PinedSolver extends SudokuSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PinedSolver.class);

    public static final PinedSolver INSTANCE = new PinedSolver();

    private PinedSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        if (cell.getValue() != null) {
            return 0;
        } else if (CollectionUtils.size(cell.getPossibilities()) == 1) {
            cell.setValue(cell.getPossibilities().iterator().next());
            LOGGER.info("Pined {} for {}", cell.getValue(), cell);
            return 2;
        }

        for (Integer possibility : cell.getPossibilities()) {
            //Possibility is only available in this cell for this box/col/row = pinned
            if (cell.functionToEachContraint(cells -> {
                if (cells.stream().map(SudokuCell::getPossibilities).flatMap(Collection::stream).noneMatch(p -> Objects.equals(p, possibility))) {
                    cell.setValue(possibility);
                    LOGGER.info("Pined {} for {}", possibility, cell);
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
