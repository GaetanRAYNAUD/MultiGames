package fr.graynaud.multigames.object.sudoku.solver;

import fr.graynaud.multigames.object.sudoku.SudokuCell;
import fr.graynaud.multigames.object.sudoku.SudokuGrid;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XWingSolver extends SudokuSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(XWingSolver.class);

    public static final XWingSolver INSTANCE = new XWingSolver();

    private XWingSolver() {}

    @Override
    public int solveInternal(SudokuCell cell) {
        if (!cell.isEditable() || cell.getValue() != null) {
            return 0;
        }

        int result = 0;
        for (Integer possibility : cell.getPossibilities()) {
            List<SudokuCell> samePossibilities = cell.getColCells().stream().filter(c -> c.getPossibilities().contains(possibility)).toList();

            if (CollectionUtils.size(samePossibilities) == 1) {
                for (int i = 0; i < SudokuGrid.GRID_SIZE; i++) {
                    Set<SudokuCell> col = cell.getGrid().getCol(i);
                    if (i != cell.getCol()) {
                        List<SudokuCell> otherPossibilities = col.stream().filter(c -> c.getPossibilities().contains(possibility)).toList();

                        if (otherPossibilities.stream().map(SudokuCell::getRow).distinct().count() == 2) {
                            if (Set.of(cell.getRow(), samePossibilities.get(0).getRow())
                                   .equals(otherPossibilities.stream().map(SudokuCell::getRow).collect(Collectors.toSet()))) {
                                for (SudokuCell otherCell : SetUtils.union(cell.getGrid().getRow(cell.getRow()),
                                                                           cell.getGrid().getRow(samePossibilities.get(0).getRow()))) {
                                    if (!otherCell.equals(cell) && !otherCell.equals(samePossibilities.get(0)) && !otherCell.equals(otherPossibilities.get(0))
                                        && !otherCell.equals(otherPossibilities.get(1))) {
                                        result = Math.max(result, otherCell.removePossibility(possibility) ? 1 : 0);
                                    }
                                }

                                if (result > 0) {
                                    LOGGER.info("XWing {} at {}", possibility,
                                                ListUtils.union(ListUtils.union(List.of(cell), samePossibilities), otherPossibilities));
                                    return result;
                                }
                            }
                        }
                    }
                }
            }

            samePossibilities = cell.getRowCells().stream().filter(c -> c.getPossibilities().contains(possibility)).toList();

            if (CollectionUtils.size(samePossibilities) == 1) {
                for (SudokuCell[] row : cell.getGrid().getGrid()) {
                    if (row[0].getRow() != cell.getRow()) {
                        List<SudokuCell> otherPossibilities = Arrays.stream(row).filter(c -> c.getPossibilities().contains(possibility)).toList();

                        if (otherPossibilities.stream().map(SudokuCell::getCol).distinct().count() == 2) {
                            if (Set.of(cell.getCol(), samePossibilities.get(0).getCol())
                                   .equals(otherPossibilities.stream().map(SudokuCell::getCol).collect(Collectors.toSet()))) {
                                for (SudokuCell otherCell : SetUtils.union(cell.getGrid().getCol(cell.getCol()),
                                                                           cell.getGrid().getCol(samePossibilities.get(0).getCol()))) {
                                    if (!otherCell.equals(cell) && !otherCell.equals(samePossibilities.get(0)) && !otherCell.equals(otherPossibilities.get(0))
                                        && !otherCell.equals(otherPossibilities.get(1))) {
                                        result = Math.max(result, otherCell.removePossibility(possibility) ? 1 : 0);
                                    }
                                }

                                if (result > 0) {
                                    LOGGER.info("XWing {} at {}", possibility,
                                                ListUtils.union(ListUtils.union(List.of(cell), samePossibilities), otherPossibilities));
                                    return result;
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}
