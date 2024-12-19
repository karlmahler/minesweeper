package io.github.karlmahler.minesweeper;

public final class Cell {
    private static final int MINIMUM_ADJACENT_MINES = 0;
    private static final int MAXIMUM_ADJACENT_MINES = 8;

    private static final String MINE_SYMBOL = "*";
    private static final String NON_MINE_SYMBOL = "+";

    private final boolean mine;
    private int adjacentMines;
    private Status status;

    public Cell(final boolean mine) {
        this.mine = mine;
        this.adjacentMines = 0;
        this.status = Status.HIDDEN;
    }

    public boolean hasMine() {
        return mine;
    }

    public void setMarked() {
        if (status != Status.REVEALED) {
            this.status = Status.MARKED;
        }
    }

    public void unsetMarked() {
        if (status != Status.REVEALED) {
            this.status = Status.HIDDEN;
        }
    }

    public void setRevealed() {
        if (status != Status.MARKED) {
            this.status = Status.REVEALED;
        }
    }

    public boolean isHidden() {
        return status == status.HIDDEN;
    }

    public boolean isRevealed() {
        return status == status.REVEALED;
    }

    public boolean isMarked() {
        return status == status.MARKED;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(final int adjacentMines) {
        validateAdjacentMines(adjacentMines);

        this.adjacentMines = adjacentMines;
    }

    private void validateAdjacentMines(final int adjacentMines) {
        if (
            adjacentMines < MINIMUM_ADJACENT_MINES ||
            adjacentMines > MAXIMUM_ADJACENT_MINES
        ) {
            throw new IllegalArgumentException(String.format(
                    "there must be between %d and %d mines",
                    MINIMUM_ADJACENT_MINES,
                    MAXIMUM_ADJACENT_MINES
                )
            );
        }
    }

    @Override
    public String toString() {
        return String.format(
            "(%s %d)",
            mine ? MINE_SYMBOL : NON_MINE_SYMBOL,
            adjacentMines
        );
    }

    private static enum Status {
        HIDDEN,
        MARKED,
        REVEALED;
    }
}
