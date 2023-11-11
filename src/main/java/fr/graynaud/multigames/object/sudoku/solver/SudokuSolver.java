package fr.graynaud.multigames.object.sudoku.solver;

import fr.graynaud.multigames.object.sudoku.SudokuCell;
import fr.graynaud.multigames.object.sudoku.SudokuGrid;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SudokuSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SudokuSolver.class);

    private static final List<SudokuSolver> SOLVERS = List.of(PinedSolver.INSTANCE, NakedPairSolver.INSTANCE, HiddenPairSolver.INSTANCE,
                                                              NakedTripleSolver.INSTANCE, HiddenTripleSolver.INSTANCE, PointingPairSolver.INSTANCE,
                                                              HiddenQuadSolver.INSTANCE, NakedQuadSolver.INSTANCE, XWingSolver.INSTANCE);

    public static void solve(SudokuGrid grid) {
        //Keep solving while is not solved or done anything
        while (!grid.isSolved()) {
            try {
                grid.setSolving(true);
                if (step(grid, true) == 0) {
                    return;
                }
            } finally {
                grid.setSolving(false);
            }
        }
    }

    public static void solveOne(SudokuGrid grid) {
        //Keep solving while is not solved or done anything or solved one cell
        while (!grid.isSolved()) {
            try {
                grid.setSolving(true);
                int max = step(grid, true);
                if (max == 0 || max == 2) {
                    return;
                }
            } finally {
                grid.setSolving(false);
            }
        }
    }

    public static int step(SudokuGrid grid, boolean solve) {
        try {
            grid.setSolving(true);

            for (SudokuSolver solver : SOLVERS) {
                int max = 0;
                for (SudokuCell[] row : grid.getGrid()) {
                    for (SudokuCell cell : row) {
                        max = Math.max(max, solve(cell, solver, solve));

                        if (!solve && max > 0) {
                            return max;
                        }
                    }
                }

                if (max > 0) {
                    return max;
                }
            }

            return 0;
        } finally {
            grid.setSolving(false);
        }
    }

    /**
     * @return 0 = nothing changed, 1 = reduced possibilities, 2 = solved
     */
    public static int solve(SudokuCell cell, SudokuSolver solver, boolean solve) {
        if (!cell.isEditable() || cell.getValue() != null) {
            return 0;
        }

        if (solve && CollectionUtils.size(cell.getPossibilities()) == 1) {
            cell.setValue(cell.getPossibilities().iterator().next());
            LOGGER.info("{} set to {}, because only possibility", cell, cell.getValue());
            return 2;
        }

        int value = solver.solveInternal(cell);

        if (solve && CollectionUtils.size(cell.getPossibilities()) == 1) {
            cell.setValue(cell.getPossibilities().iterator().next());
            LOGGER.info("{} set to {}, because only possibility", cell, cell.getValue());
            return 2;
        }

        if (value == 2 && CollectionUtils.size(cell.getPossibilities()) > 1) {
            //If solver solved and did not remove all possibilities, clear
            Set<Integer> toRemove = cell.getPossibilities()
                                        .stream()
                                        .filter(possibility -> !Objects.equals(possibility, cell.getValue()))
                                        .collect(Collectors.toSet());
            for (Integer possibility : toRemove) {
                cell.removePossibility(possibility);
            }

            return 2;
        }

        return value;
    }

    public abstract int solveInternal(SudokuCell cell);
}
