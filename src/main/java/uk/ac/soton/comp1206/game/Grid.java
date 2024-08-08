package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {
    private static final Logger logger = LogManager.getLogger(Grid.class);

    private final int cols;
    private final int rows;
    private final SimpleIntegerProperty[][] grid;
    /**
     * Create a new Grid with the specified number of columns and rows and initialise them.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        grid = new SimpleIntegerProperty[cols][rows];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the IntegerProperty at the specified position in the grid.
     * @param x the x-coordinate of the position
     * @param y the y-coordinate of the position
     * @return the IntegerProperty at the specified position
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }
    /**
     * Checks if the given game piece can be played at the specified position on the game grid.
     *
     * @param piece   the game piece to be played
     * @param xPiece  the x-coordinate of the top-left corner of the piece on the game grid
     * @param yPiece  the y-coordinate of the top-left corner of the piece on the game grid
     * @return true if the piece can be played at the specified position, false otherwise
     */
    public boolean canPlayPiece(GamePiece piece, int xPiece, int yPiece) {
        logger.info("Checking if piece {} can be played at {},{}", piece, xPiece, yPiece);
        if (piece == null) {
            logger.warn("Piece is null");
            return false;
        }

        int[][] blocks = piece.getBlocks();
        for (int blockX = 0; blockX < blocks.length; blockX++) {
            for (int blockY = 0; blockY < blocks[blockX].length; blockY++) {
                if (blocks[blockX][blockY] > 0) {
                    int gridX = xPiece + blockX - 1;
                    int gridY = yPiece + blockY - 1;
                    if (gridX < 0 || gridX >= cols || gridY < 0 || gridY >= rows || get(gridX, gridY) != 0) {
                        logger.info("Block at {},{} cannot be placed", gridX, gridY);
                        return false;
                    }
                }
            }
        }
        return true;
    }
    /**
     * Attempts to play the given game piece at the specified position on the game grid.
     * If successful, the piece is placed on the grid and any necessary actions are taken (e.g., clearing lines).
     *
     * @param piece   the game piece to be played
     * @param xPiece  the x-coordinate of the top-left corner of the piece on the game grid
     * @param yPiece  the y-coordinate of the top-left corner of the piece on the game grid
     * @return true if the piece was successfully played, false otherwise
     */
    public boolean playPiece(GamePiece piece, int xPiece, int yPiece) {
        if (!canPlayPiece(piece, xPiece, yPiece)) {
            logger.info("Piece {} cannot be played at {},{}", piece, xPiece, yPiece);
            return false;
        }

        logger.info("Placing piece {} at {},{}", piece, xPiece, yPiece);
        int[][] blocks = piece.getBlocks();
        for (int blockX = 0; blockX < blocks.length; blockX++) {
            for (int blockY = 0; blockY < blocks[blockX].length; blockY++) {
                if (blocks[blockX][blockY] > 0) {
                    int gridX = xPiece + blockX - 1;
                    int gridY = yPiece + blockY - 1;
                    set(gridX, gridY, piece.getValue());
                }
            }
        }
        return true;
    }

    /**
     * Sets the value at the specified position in the grid.
     *
     * @param x      the x-coordinate of the position
     * @param y      the y-coordinate of the position
     * @param value  the value to set
     */

    public void set(int x, int y, int value) {
        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            grid[x][y].set(value);
        } else {
            logger.error("Attempting to set value outside grid bounds");
        }
    }
    /**
     * Retrieves the value at the specified position in the grid.
     *
     * @param x  the x-coordinate of the position
     * @param y  the y-coordinate of the position
     * @return the value at the specified position, or -1 if the position is outside the grid bounds
     */

    public int get(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) {
            return -1;
        }
        return grid[x][y].get();
    }
    /**
     * Get the number of columns in the grid.
     *
     * @return the number of columns in the grid
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in the grid.
     * @return the number of rows in the grid
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the grid as a 2D array of IntegerProperties.
     * @return the grid as a 2D array of IntegerProperties
     */
    public SimpleIntegerProperty[][] getGrid(){
        return grid;
    }

}
