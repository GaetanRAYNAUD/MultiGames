package fr.graynaud.multigames.object.sudoku.solver;

import fr.graynaud.multigames.object.sudoku.SudokuCell;

public class BasicSolver extends SudokuSolver {

    public static final BasicSolver INSTANCE = new BasicSolver();

    private BasicSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        if (!cell.isEditable() || cell.getValue() != null) {
            return 0;
        }

        boolean reduced = false;
        cell.resetPossibilities();
        for (SudokuCell constrain : cell.getConstrains()) {
            if (!constrain.equals(cell) && constrain.getValue() != null) {
                if (cell.removePossibility(constrain.getValue())) {
                    reduced = true;
                }
            }
        }

        return reduced ? 1 : 0;
    }
}
