package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Collection;

import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of its own, for example, for displaying an upcoming block. It also is
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /** Number of columns in the board. */
    private final int cols;

    /** Number of rows in the board. */
    private final int rows;
    private final double width;

    public int getHoverX() {
        return hoverX;
    }

    public int getHoverY() {
        return hoverY;
    }

    private int hoverX = -1;
    private int hoverY = -1;
    GameBlock mouse;
    GameBlock keyboard;

    private final double height;

    /**
     * Listeners
     */
    private BlockClickedListener blockClickedListener;
    protected RightClickedListener onRightClicked;
    protected Game game;
    final Grid grid;

    /** The blocks inside the grid. */
    GameBlock[][] blocks;

    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     *
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        // Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with its own internal grid, specifying the number of columns and rows,
     * along with the visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols, rows);

        // Build the GameBoard
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by its row and column.
     *
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Set the game object related to the game board.
     *
     * @param game the game to manage
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /** Build the GameBoard by creating a block at every x and y column and row. */
    protected void build() {
        logger.info("Building grid: {} x {}", cols, rows);

        this.getStyleClass().add("gameboard");

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x, y);
            }
        }
        dot();

    }
    public void dot(){
        //calculate centre coordinates
        double x = Math.ceil((double) getRows()/2)-1;
        double y = Math.ceil((double) getCols()/2)-1;
        blocks[(int)x][(int) y].setDot();
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     *
     * @param x column
     * @param y row
     */
    protected void createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height/rows;

        // Create a new GameBlock UI component
        var block = new GameBlock(this, x, y, blockWidth, blockHeight);

        // Add to the GridPane
        add(block, x, y);

        // Add to our block directory
        blocks[x][y] = block;

        // Bind the GameBlock component to the value in the Grid
        block.bind(grid.getGridProperty(x, y));

        // Actions for the block being left or right-clicked
        block.setOnMouseClicked(
                (e) -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        this.blockClicked(block);
                    } else {
                        this.rightClick();
                    }
                });

        // Trigger hover methods when the mouse enters or exits a game block
        block.setOnMouseEntered((e) ->{
            if(keyboard != null)
            {
                keyboard.setHovered(false);

            }
            mouse = block;
            block.setHovered(true);
        });
        block.setOnMouseExited((e) -> block.setHovered(false));
    }

    /**
     * Set the listener to handle an event when a block is clicked.
     *
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Reset all game board blocks hovered status.
     * @param block the block to refresh for
     */
    public void hoverRefresh(GameBlock block) {
        //hoverReset();
        //hovered(block);
    }

    /**
     * Right click handler setter.
     *
     * @param handler the handler to set
     */
    public void setOnRightClick(RightClickedListener handler) {
        onRightClicked = handler;
    }
    /** Triggered when a block is clicked. Call the attached listener. */
    private void rightClick() {
        if (this.onRightClicked != null) {
            this.onRightClicked.setOnRightClicked();
        }
    }


    /**
     * Triggered when a block is clicked. Call the attached listener.
     *
     * @param block block clicked on
     */
    private void blockClicked(GameBlock block) {
        // logger.info("Block clicked: {}", block);
        if (blockClickedListener != null) {
            blockClickedListener.blockClicked(block);
        }
    }


    /**
     * Set the blocks to fade from the game.
     *
     * @param blocks The list of blocks to fade out
     */
    public void fadeOut(Collection<GameBlockCoordinate> blocks) {
        for (GameBlockCoordinate block : blocks) {
            getBlock(block.getX(), block.getY()).fadeOut();
        }
    }
    /**
     * Clears the game board by setting all cells to empty.
     * After calling this method, all cells in the grid will be set to zero,
     * indicating that they are empty.
     */
    public void clearBoard() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid.set(x, y, 0);
            }
        }
    }
    /**
     * Clears the currently hovered block by setting its hovered state to false.
     * Resets the hover coordinates to (-1, -1) to indicate that no block is currently hovered.
     * This method should be called when the mouse pointer is no longer hovering over any block.
     */
    public void clearHoveredBlock(){
        if(hoverX >= 0 && hoverY >= 0){
            //clearing blocks
            blocks[hoverX][hoverY].setHovered(false);
            hoverX = -1;
            hoverY = -1;
        }
    }
    /**
     * Updates the hover state to the specified coordinates (x, y) by clearing the previously hovered block
     * and setting the new block at the specified coordinates to be hovered.
     *
     * @param x the x-coordinate of the block to hover
     * @param y the y-coordinate of the block to hover
     */
    public void updateHover(int x, int y){
        if (mouse != null) {
            mouse.setHovered(false);
        }

        keyboard = blocks[x][y];
        clearHoveredBlock();
        hoverX = x;
        hoverY = y;

        blocks[hoverX][hoverY].setHovered(true);
    }

    public int getCols(){
        return cols;
    }
    public int getRows(){
        return rows;
    }



}