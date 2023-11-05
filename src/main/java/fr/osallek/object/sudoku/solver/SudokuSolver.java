package fr.osallek.object.sudoku.solver;

import fr.osallek.object.sudoku.SudokuCell;
import fr.osallek.object.sudoku.SudokuGrid;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public abstract class SudokuSolver {

    private static final List<SudokuSolver> SOLVERS = List.of(PinedSolver.INSTANCE, NakedPairSolver.INSTANCE, NakedTripleSolver.INSTANCE,
                                                              HiddenPairSolver.INSTANCE, HiddenTripleSolver.INSTANCE, NakedQuadSolver.INSTANCE);

    public static void solve(SudokuGrid grid) {
        //Keep solving while is not solved or done anything
        while (!grid.isSolved()) {
            if (step(grid, true) == 0) {
                return;
            }
        }
    }

    public static void solveOne(SudokuGrid grid) {
        //Keep solving while is not solved or done anything or solved one cell
        while (!grid.isSolved()) {
            int max = step(grid, true);
            if (max == 0 || max == 2) {
                return;
            }
        }
    }

    public static int step(SudokuGrid grid, boolean solve) {
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
            return 2;
        }

        int value = solver.solveInternal(cell);

        if (solve && CollectionUtils.size(cell.getPossibilities()) == 1) {
            cell.setValue(cell.getPossibilities().iterator().next());
            return 2;
        }

        return value;
    }

    public abstract int solveInternal(SudokuCell cell);
}
