package fr.graynaud.multigames.object.sudoku.solver;

import com.google.common.collect.Lists;
import fr.graynaud.multigames.object.sudoku.SudokuCell;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointingPairSolver extends SudokuSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PointingPairSolver.class);

    public static final PointingPairSolver INSTANCE = new PointingPairSolver();

    private PointingPairSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        if (!cell.isEditable() || cell.getValue() != null) {
            return 0;
        }

        int result = 0;
        for (Integer possibility : cell.getPossibilities()) {
            Set<SudokuCell> samePossibilities = cell.getBoxCells().stream().filter(c -> c.getPossibilities().contains(possibility)).collect(Collectors.toSet());

            if (CollectionUtils.size(samePossibilities) == 1 || CollectionUtils.size(samePossibilities) == 2) {
                if (samePossibilities.stream().allMatch(c -> c.getRow() == cell.getRow())) {
                    for (SudokuCell rowCell : cell.getRowCells()) {
                        if (!rowCell.equals(cell) && !samePossibilities.contains(rowCell)) {
                            result = Math.max(result, rowCell.removePossibility(possibility) ? 1 : 0);
                        }
                    }

                    if (result > 0) {
                        LOGGER.info("Pointing pair/triple {} at {}", possibility, CollectionUtils.union(List.of(cell), samePossibilities));
                        return result;
                    }
                } else if (samePossibilities.stream().allMatch(c -> c.getCol() == cell.getCol())) {
                    for (SudokuCell colCell : cell.getColCells()) {
                        if (!colCell.equals(cell) && !samePossibilities.contains(colCell)) {
                            result = Math.max(result, colCell.removePossibility(possibility) ? 1 : 0);
                        }
                    }

                    if (result > 0) {
                        LOGGER.info("Pointing pair/triple {} at {}", possibility, CollectionUtils.union(List.of(cell), samePossibilities));
                        return result;
                    }
                }
            }

            samePossibilities = cell.getColCells().stream().filter(c -> c.getPossibilities().contains(possibility)).collect(Collectors.toSet());
            if (CollectionUtils.size(samePossibilities) == 1 || CollectionUtils.size(samePossibilities) == 2) {
                if (samePossibilities.stream().allMatch(c -> c.getBox() == cell.getBox())) {
                    for (SudokuCell boxCell : cell.getBoxCells()) {
                        if (!boxCell.equals(cell) && !samePossibilities.contains(boxCell)) {
                            result = Math.max(result, boxCell.removePossibility(possibility) ? 1 : 0);
                        }
                    }

                    if (result > 0) {
                        LOGGER.info("Pointing pair/triple {} at {}", possibility, CollectionUtils.union(List.of(cell), samePossibilities));
                        return result;
                    }
                }
            }

            samePossibilities = cell.getRowCells().stream().filter(c -> c.getPossibilities().contains(possibility)).collect(Collectors.toSet());
            if (CollectionUtils.size(samePossibilities) == 1 || CollectionUtils.size(samePossibilities) == 2) {
                if (samePossibilities.stream().allMatch(c -> c.getBox() == cell.getBox())) {
                    for (SudokuCell boxCell : cell.getBoxCells()) {
                        if (!boxCell.equals(cell) && !samePossibilities.contains(boxCell)) {
                            result = Math.max(result, boxCell.removePossibility(possibility) ? 1 : 0);
                        }
                    }

                    if (result > 0) {
                        LOGGER.info("Pointing pair/triple {} at {}", possibility, CollectionUtils.union(List.of(cell), samePossibilities));
                        return result;
                    }
                }
            }
        }

        return result;
    }
}
