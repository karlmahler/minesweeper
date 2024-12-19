package io.github.karlmahler.minesweeper;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

public final class Launcher extends Application {
    private static final int BUTTON_PADDING = 2;
    private static final int BOARD_SIZE_EASY =  8;
    private static final int BOARD_SIZE_MEDIUM = 12;
    private static final int BOARD_SIZE_HARD = 18;
    private static final double BUTTON_SIZE = 31.0;
    private static final double MINE_PROBABILITY_EASY = 15.0;
    private static final double MINE_PROBABILITY_MEDIUM = 18.0;
    private static final double MINE_PROBABILITY_HARD = 22.0;
    private static final Difficulty DEFAULT_DIFFICULTY = Difficulty.HARD;
    private static final String WINDOW_TITLE = "Minesweeper";
    private static final String MINE_SYMBOL = "💣";
    private static final String EMPTY_AND_HIDDEN_CELL_SYMBOL = "";

    private Board board;
    private List<List<Cell>> grid;
    private GridPane gridPane;
    private ScrollPane scrollPane;
    private Label flagsLabel;
    private int boardSize;
    private double mineProbability;
    private long numberOfMines;
    private long numberOfFlags;
    private long numberOfMarkedCells;
    private long numberOfRevealedCells;
    private long numberOfNonMineCells;
    private boolean gameStarted;

    private void initialize(final Difficulty difficulty) {
        setDifficulty(difficulty);

        this.gameStarted = false;
        this.board = new Board(boardSize, mineProbability);
        this.grid = board.getGrid();
        this.gridPane = createGridPane();
        this.scrollPane = new ScrollPane(gridPane);
        this.numberOfMines = 0;
        this.numberOfFlags = numberOfMines;
        this.numberOfMarkedCells = 0L;
        this.numberOfRevealedCells = 0L;
        this.numberOfNonMineCells = boardSize * boardSize - numberOfMines;
        this.flagsLabel = new Label(String.format("Flags: %d / %d", numberOfFlags, numberOfFlags));
    }

    private void initialize() {
        initialize(DEFAULT_DIFFICULTY);
    }

    private void setDifficulty(final Difficulty difficulty) {
        switch (difficulty) {
            case EASY -> {
                this.boardSize = BOARD_SIZE_EASY;
                this.mineProbability = MINE_PROBABILITY_EASY;
            }
            case MEDIUM -> {
                this.boardSize = BOARD_SIZE_MEDIUM;
                this.mineProbability = MINE_PROBABILITY_MEDIUM;
            } 
            case HARD -> {
                this.boardSize = BOARD_SIZE_HARD;
                this.mineProbability = MINE_PROBABILITY_HARD;
            } 
        }
    }

    @Override
    public void start(final Stage stage) {
        start(stage, Difficulty.EASY);
    }

    public void start(final Stage stage, final Difficulty difficulty) {
        initialize(difficulty);

        var restartButton = new Button("Restart");

        restartButton.setOnAction(event -> {
            start(stage, difficulty);
        });

        stage.setTitle(WINDOW_TITLE);
        scrollPane.setPadding(new Insets(5));

        var menu = new MenuButton("Change difficulty");

        var easyDifficultyOption = new MenuItem("Easy");
        easyDifficultyOption.setOnAction(event -> {
            start(stage, Difficulty.EASY);
        });

        var mediumDifficultyOption = new MenuItem("Medium");
        mediumDifficultyOption.setOnAction(event -> {
            start(stage, Difficulty.MEDIUM);
        });

        var hardDifficultyOption = new MenuItem("Hard");
        hardDifficultyOption.setOnAction(event -> {
            start(stage, Difficulty.HARD);
        });

        menu.getItems().addAll(easyDifficultyOption, mediumDifficultyOption, hardDifficultyOption);

        var horizontalBox = new HBox(restartButton, menu);
        horizontalBox.setAlignment(Pos.BOTTOM_CENTER);

        var verticalBox = new VBox(flagsLabel, scrollPane, horizontalBox);
        verticalBox.setAlignment(Pos.BOTTOM_CENTER);

        stage.setScene(new Scene(verticalBox));
        stage.setResizable(false);
        stage.show();
    }

    private void fillGridPane() {
        this.grid = board.getGrid();
        List<Node> buttons = gridPane.getChildren();

        for (int row = 0; row < boardSize; row++) {
            for (int column = 0; column < boardSize; column++) {
                int buttonIndex = boardSize * row + column;
                var button = (Button) buttons.get(buttonIndex);
                var cell = grid.get(row).get(column);

                button.setOnMouseClicked(onMouseClicked(button, cell, row, column));
            }
        }
    }

    private GridPane createGridPane() {
        var pane = new GridPane();

        pane.setPadding(new Insets(BUTTON_PADDING));
        setGridPaneGaps(pane);

        for (int row = 0; row < boardSize; row++) {
            for (int column = 0; column < boardSize; column++) {
                var button = new Button(EMPTY_AND_HIDDEN_CELL_SYMBOL);

                button.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
                button.setOnAction(onFirstClick(button, row, column));

                pane.add(button, column, row);
            }
        }

        return pane;
    }

    private EventHandler<ActionEvent> onFirstClick(final Button button, final int row, final int column) {
        return event -> {
            if (gameStarted) {
                return;
            }

            this.gameStarted = true;

            board.buildGrid(row, column);
            fillGridPane();

            this.numberOfMines = countMines();
            this.numberOfFlags = numberOfMines;
            this.numberOfNonMineCells = boardSize * boardSize - numberOfMines;
            flagsLabel.setText(String.format("Flags: %d / %d", numberOfFlags, numberOfFlags));
        };
    }

    private EventHandler<MouseEvent> onMouseClicked(final Button button, final Cell cell, final int row, final int column) {
        return event -> {
            if (cell.isRevealed()) {
                return;
            }

            var buttonType = event.getButton();

            switch (buttonType) {
                case MouseButton.PRIMARY -> handlePrimaryClick(button, cell, row, column);
                case MouseButton.SECONDARY -> handleSecondaryClick(button, cell);
            }
        };
    }

    private void handlePrimaryClick(final Button button, final Cell cell, final int row, final int column) {
        if (cell.isMarked()) {
            return;
        }

        revealCell(button, cell);

        if (cell.hasMine()) {
            button.setStyle("-fx-color: red;");
            gameOver();
        } else {
            button.setStyle("-fx-color: blue;");

            if (cell.getAdjacentMines() == 0) {
                revealAdjacentCells(cell, row, column);
            }

            if (numberOfRevealedCells == numberOfNonMineCells) {
                win();
            }
        }
    }

    private void revealCell(final Button button, final Cell cell) {
        cell.setRevealed();
        numberOfRevealedCells++;

        String content = getCellContent(cell);
        button.setText(content);
    }

    public void gameOver() {
        gridPane.setDisable(true);

        List<Node> buttons = gridPane.getChildren();

        for (int row = 0; row < boardSize; row++) {
            for (int column = 0; column < boardSize; column++) {
                var cell = grid.get(row).get(column);

                if (!cell.hasMine()) {
                    continue;
                }

                var buttonIndex = boardSize * row + column;
                var button = (Button) buttons.get(buttonIndex); 

                revealCell(button, cell);
            }
        }
    }

    private void win() {
        gridPane.setDisable(true);

        List<Node> buttons = gridPane.getChildren();

        for (int row = 0; row < boardSize; row++) {
            for (int column = 0; column < boardSize; column++) {
                var cell = grid.get(row).get(column);

                var buttonIndex = boardSize * row + column;
                var button = (Button) buttons.get(buttonIndex); 

                revealCell(button, cell);
            }
        }

        var alert = new Alert(Alert.AlertType.NONE);

        alert.setTitle("Congratulations!");
        alert.setContentText("You won!");

        alert
            .getDialogPane()
            .getButtonTypes()
            .add(
                new ButtonType("Enjoy!", ButtonBar.ButtonData.CANCEL_CLOSE)
            );
        alert.showAndWait();
    }

    private void handleSecondaryClick(final Button button, final Cell cell) {
        if (!cell.isMarked()) {
            markCell(button, cell);
        } else {
            unmarkCell(button, cell);
        }
    }

    private void markCell(final Button button, final Cell cell) {
        if (numberOfFlags == 0) {
            return;
        }

        cell.setMarked();

        numberOfFlags--;
        updateFlagsLabel();

        button.setStyle("-fx-color: green;");
    }

    private void unmarkCell(final Button button, final Cell cell) {
        cell.unsetMarked();

        numberOfFlags++;
        updateFlagsLabel();

        button.setStyle(null);
    }

    private void setGridPaneGaps(final GridPane pane) {
        pane.setHgap(BUTTON_PADDING);
        pane.setVgap(BUTTON_PADDING);
    }

    private String getCellContent(final Cell cell) {
        if (cell.hasMine()) {
            return MINE_SYMBOL;
        }

        var mines = cell.getAdjacentMines();

        return switch (mines) {
            case 0 -> EMPTY_AND_HIDDEN_CELL_SYMBOL;
            default -> String.valueOf(mines);
        };
    }

    private long countMines() {
        return grid
            .stream()
            .flatMap(Collection::stream)
            .filter(Cell::hasMine)
            .count();
    }

    private void updateFlagsLabel() {
        flagsLabel.setText(String.format("Flags: %d / %d", numberOfFlags, numberOfMines));
    }

    private void revealAdjacentCells(final Cell cell, final int row, final int column) {
        List<Cell> adjacentCells = getAdjacentCells(row, column);
        List<List<Integer>> adjacentIndices = getAdjacentCellsIndices(row, column);
        List<Button> adjacentButtons = getAdjacentButtons(row, column);

        for (int i = 0; i < adjacentCells.size(); i++) {
            var currentCell = adjacentCells.get(i);

            if (currentCell.isRevealed() || currentCell.hasMine()) {
                continue;
            }

            int mines = currentCell.getAdjacentMines(); 

            var currentButton = adjacentButtons.get(i);

            revealCell(currentButton, currentCell);
            currentButton.setStyle("-fx-color: blue;");

            if (currentCell.isMarked()) {
                unmarkCell(currentButton, currentCell);
                revealCell(currentButton, currentCell);
                currentButton.setStyle("-fx-color: blue;");
            }

            if (mines == 0) {
                revealAdjacentCells(currentCell, adjacentIndices.get(i).get(0), adjacentIndices.get(i).get(1));
            }
        }
    }

    private List<Button> getAdjacentButtons(final int row, final int column) {
        List<Node> buttons = gridPane.getChildren();
        List<Button> adjacentButtons = new ArrayList<>();
        List<List<Integer>> adjacentIndices = getAdjacentCellsIndices(row, column);

        for (List<Integer> pair : adjacentIndices) {
            int buttonIndex = boardSize * pair.get(0) + pair.get(1);
            var button = (Button) buttons.get(buttonIndex);

            adjacentButtons.add(button);
        }

        return adjacentButtons;
    }

    private List<Cell> getAdjacentCells(final int row, final int column) {
        List<Cell> adjacentCells = new ArrayList<>();
        List<List<Integer>> adjacentIndices = getAdjacentCellsIndices(row, column);

        for (List<Integer> pair : adjacentIndices) {
            adjacentCells.add(grid.get(pair.get(0)).get(pair.get(1)));
        }

        return adjacentCells;
    }

    private List<List<Integer>> getAdjacentCellsIndices(final int row, final int column) {
        List<List<Integer>> adjacentCellsIndices = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (!isValidIndex(row + i) || !isValidIndex(column + j)) {
                    continue;
                }

                if (i != 0 || j != 0) {
                    adjacentCellsIndices.add(List.of(row + i, column + j));
                }
            }
        }

        return adjacentCellsIndices;
    }

    private boolean isValidIndex(final int index) {
        return index >= 0 && index < boardSize;
    }

    private static enum Difficulty {
        EASY,
        MEDIUM,
        HARD;
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
