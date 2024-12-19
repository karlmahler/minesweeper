package io.github.karlmahler.minesweeper;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public final class Board {
    private List<List<Cell>> grid;
    private final int size;
    private final double mineProbability;
    private final Random random;

    public Board(final int size, final double mineProbability) {
        this.random = new Random();
        this.mineProbability = mineProbability / 100.0;
        this.size = size;
        this.grid = Collections.emptyList();
    }

    public void buildGrid(final int startRow, final int startColumn) {
        this.grid = createGrid(startRow, startColumn);
        fillAdjacentMines();
    }

    private List<List<Cell>> createGrid(final int startRow, final int startColumn) {
        List<List<Cell>> grid = new ArrayList<>();

        for (int row = 0; row < size; row++) {
            List<Cell> rowCells = createRows(row, startRow, startColumn);

            grid.add(rowCells);
        }

        return grid;
    }

    private List<Cell> createRows(final int row, final int startRow, final int startColumn) {
        List<Cell> rowCells = new ArrayList<>();

        for (int column = 0; column < size; column++) {
            Cell cell;

            if (isNearStartCell(row, startRow, column, startColumn)) {
                cell = new Cell(false);
            } else {
                cell = createCell();
            }

            rowCells.add(cell);
        }

        return rowCells;
    }

    private boolean isNearStartCell(final int row, final int startRow, final int column, final int startColumn) {
        return (
            row == startRow && column == startColumn ||
            row == startRow - 1 && column == startColumn - 1 ||
            row == startRow - 1 && column == startColumn ||
            row == startRow - 1 && column == startColumn + 1 ||
            row == startRow && column == startColumn - 1 ||
            row == startRow && column == startColumn + 1 ||
            row == startRow + 1 && column == startColumn - 1 ||
            row == startRow + 1 && column == startColumn ||
            row == startRow + 1 && column == startColumn + 1
        );
    }

    private Cell createCell() {
        boolean mine = generateRandomBoolean();
        var cell = new Cell(mine);

        return cell;
    }

    private boolean generateRandomBoolean() {
        return random.nextDouble() < mineProbability;
    }

    public List<List<Cell>> getGrid() {
        return createUnmodifiableGridView();
    }

    private List<List<Cell>> createUnmodifiableGridView() {
        List<List<Cell>> view = new ArrayList<>();

        for (var row : grid) {
            view.add(Collections.unmodifiableList(row));
        }

        return Collections.unmodifiableList(view);
    }

    private void fillAdjacentMines() {
        for (int row = 0; row < size; row++) {
            var rowCells = grid.get(row);

            for (int column = 0; column < size; column++) {
                int topLeft = calculateAdjacentMine(row - 1, column - 1);
                int topMiddle = calculateAdjacentMine(row - 1, column);
                int topRight = calculateAdjacentMine(row - 1, column + 1);

                int middleLeft = calculateAdjacentMine(row, column - 1);
                int middleRight = calculateAdjacentMine(row, column + 1);

                int bottomLeft = calculateAdjacentMine(row + 1, column - 1);
                int bottomMiddle = calculateAdjacentMine(row + 1, column);
                int bottomRight = calculateAdjacentMine(row + 1, column + 1);

                int adjacentMines = (
                    topLeft + topMiddle + topRight +
                    middleLeft + middleRight +
                    bottomLeft + bottomMiddle + bottomRight
                );

                var cell = rowCells.get(column);

                cell.setAdjacentMines(adjacentMines);
            }
        }
    }

    private int calculateAdjacentMine(final int row, final int column) {
        return hasAdjacentCellMine(row, column) ? 1 : 0;
    }

    private boolean hasAdjacentCellMine(final int row, final int column) {
        if (!isValidPosition(row, column)) {
            return false;
        }

        var adjacentCell = grid.get(row).get(column);

        return adjacentCell.hasMine();
    }

    private boolean isValidPosition(final int row, final int column) {
        return isValidIndex(row) && isValidIndex(column);
    }

    private boolean isValidIndex(final int index) {
        return index >= 0 && index < size;
    }
}
